/*
 *    Copyright 2018-2019 the original author or authors.
 *
 *    Licensed under the Apache License, Version 2.0 (the "License");
 *    you may not use this file except in compliance with the License.
 *    You may obtain a copy of the License at
 *
 *        http://www.apache.org/licenses/LICENSE-2.0
 *
 *    Unless required by applicable law or agreed to in writing, software
 *    distributed under the License is distributed on an "AS IS" BASIS,
 *    WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *    See the License for the specific language governing permissions and
 *    limitations under the License.
 */
package com.yofish.gary.biz.service.impl;

import com.yofish.gary.api.dto.req.*;
import com.yofish.gary.api.dto.rsp.UserDetailRspDTO;
import com.yofish.gary.api.dto.rsp.UserLoginRspDTO;
import com.yofish.gary.api.dto.rsp.UserMenuPermissionRspDTO;
import com.yofish.gary.api.dto.rsp.UserQueryRspDTO;
import com.yofish.gary.api.login.UpmsLoginLogoutRealm;
import com.yofish.gary.api.properties.ShiroProperties;
import com.yofish.gary.biz.domain.Permission;
import com.yofish.gary.biz.domain.User;
import com.yofish.gary.biz.helper.PageDataHelper;
import com.yofish.gary.biz.repository.UserRepository;
import com.yofish.gary.biz.service.UserService;
import com.yofish.gary.tuple.Tuple2;
import com.youyu.common.api.PageData;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.*;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.HashSet;
import java.util.List;
import java.util.Set;

import static com.yofish.gary.api.enums.UpmsResultCode.*;
import static com.yofish.gary.api.login.UpmsLoginLogoutRealm.getUserId;
import static com.yofish.gary.biz.helper.exception.ExceptionHelper.loginException;
import static com.yofish.gary.utils.BizExceptionUtil.exception;
import static com.yofish.gary.utils.BizExceptionUtil.exception2MatchingExpression;
import static com.yofish.gary.utils.OrikaCopyUtil.copyProperty;
import static com.yofish.gary.utils.OrikaCopyUtil.copyProperty4List;
import static com.yofish.gary.utils.StringUtil.eq;
import static java.util.Objects.isNull;
import static java.util.Objects.nonNull;

@Service
public class UserServiceImpl implements UserService {


    @Autowired
    private UserRepository userRepository;

    @Autowired(required = false)
    private ShiroProperties shiroProperties;

    @Autowired(required = false)
    private UpmsLoginLogoutRealm shiroAuthRealm;

    @Override
    public UserLoginRspDTO login(UserLoginReqDTO userLoginReqDTO) {
        try {
            return shiroAuthRealm.login(userLoginReqDTO);
        } catch (Exception ex) {
            throw loginException(ex);
        }
    }

    @Override
    public void logout() {
        shiroAuthRealm.logout();
    }

    @Override
    public void modifyPassword(UserModifyPasswordReqDTO userModifyPasswordReqDTO) {
        User user = userRepository.findById(getUserId()).get();
        checkUser(user);

        user.modifyPassword(userModifyPasswordReqDTO, shiroProperties.getHashAlgorithmName());
        userRepository.save(user);
        shiroAuthRealm.logout();
    }


    @Override
    public User getAuthenticationUser(String username, String password) {
        User user = userRepository.findUserByUsernameAndPassword(username, password);
        checkUser(user);
        return user;
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public Long add(UserAddReqDTO userAddReqDTO) {
        checkUserAdd(userAddReqDTO);
        User user = new User(userAddReqDTO, shiroProperties.getHashAlgorithmName());
        userRepository.save(user);
        return user.getId();
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void delete(UserDeleteReqDTO userDeleteReqDTO) {
        Long userId = userDeleteReqDTO.getUserId();
        userRepository.deleteById(userId);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void edit(UserEditReqDTO userEditReqDTO) {
        User existUser = userRepository.findById(userEditReqDTO.getUserId()).get();
        if (!eq(existUser.getEmail(), userEditReqDTO.getEmail())) {
            checkUserEmail(userEditReqDTO.getEmail());
        }

        User user = new User(userEditReqDTO, shiroProperties.getHashAlgorithmName());
        userRepository.save(user);
        updateUserRoles(user, userEditReqDTO.getRoleIds());
    }

    @Override
    public PageData<UserQueryRspDTO> getPage(UserQueryReqDTO userQueryReqDTO) {
        Pageable pageable = PageRequest.of(userQueryReqDTO.getPageNo(), userQueryReqDTO.getPageSize());

        Example example = this.toExample(userQueryReqDTO);

        Page userPage = this.userRepository.findAll(example, pageable);
        List<UserQueryRspDTO> userQueryRsps = copyProperty4List(userPage.getContent(), UserQueryRspDTO.class);

        PageData<UserQueryRspDTO> pageData = PageDataHelper.toPageData(userPage, userQueryRsps);

        return pageData;
    }

    @Override
    public List<UserQueryRspDTO> getList(UserQueryReqDTO userQueryReqDTO) {
        Example example = this.toExample(userQueryReqDTO);
        List userList = this.userRepository.findAll(example);

        List<UserQueryRspDTO> userQueryRsps = copyProperty4List(userList, UserQueryRspDTO.class);

        return userQueryRsps;
    }

    private Example toExample(UserQueryReqDTO userQueryReqDTO) {
        User user = User.builder()
                .id(userQueryReqDTO.getUserId())
                .username(userQueryReqDTO.getUserName())
                .realName(userQueryReqDTO.getRealName())
                .phone(userQueryReqDTO.getPhone())
                .email(userQueryReqDTO.getEmail())
                .status(userQueryReqDTO.getStatus())
                .build();

        Example example = Example.of(user, ExampleMatcher.matching()
                .withMatcher("username", matcher -> matcher.stringMatcher(ExampleMatcher.StringMatcher.CONTAINING))
                .withMatcher("realName", matcher -> matcher.stringMatcher(ExampleMatcher.StringMatcher.CONTAINING))
                .withMatcher("phone", matcher -> matcher.stringMatcher(ExampleMatcher.StringMatcher.CONTAINING))
                .withMatcher("status", matcher -> matcher.stringMatcher(ExampleMatcher.StringMatcher.CONTAINING))
        );
        return example;
    }

    @Override
    public void unauthorized() {
        exception(USER_UNAUTHORIZED);
    }

    @Override
    public void needLogin() {
        exception(ACCESS_EXCEPTION_NEED_LOGIN);
    }

    @Override
    public Tuple2<Set<String>, Set<String>> getRolePermissionTuple2(Long userId) {
        User user = userRepository.findById(userId).get();

        return new Tuple2<>(new HashSet<>(null), new HashSet<>(null));
    }

    @Override
    public boolean hasUrlPermission(Long userId, String url) {
        int count = 1;
        return count == 0 ? false : true;
    }

    @Override
    public List<UserMenuPermissionRspDTO> getUserMenuPermissions(UserMenuPermissionReqDTO userPermissionReqDTO) {
        List<Permission> rootPermissions = null;

        return copyProperty4List(rootPermissions, UserMenuPermissionRspDTO.class);
    }

    @Override
    public UserDetailRspDTO getUserDetail(Long userId) {
        User user = getUser(userId);
        return copyProperty(user, UserDetailRspDTO.class);
    }

    @Override
    public Boolean isAdmin(Long userId) {
        userId = isNull(userId) ? getUserId() : userId;
        User user = getUser(userId);
        return user.isAdmin();
    }

    /**
     * 根据用户id查询用户信息
     *
     * @param userId
     * @return
     */
    private User getUser(Long userId) {
        User user = userRepository.findById(userId).orElse(null);
        return user;
    }

    /**
     * 填充用户角色信息
     *
     * @param users
     */

    /**
     * 更新用户角色
     *
     * @param user
     * @param roleIds
     */
    private void updateUserRoles(User user, List<Long> roleIds) {
//        userRepository.deleteByUserId(user.getId());
//        saveUserRoles(user, roleIds);
    }

    /**
     * 检查用户添加
     *
     * @param userAddReqDTO
     */
    private void checkUserAdd(UserAddReqDTO userAddReqDTO) {
        User user = userRepository.findUserByUsernameAndEmail(userAddReqDTO.getUsername(), userAddReqDTO.getEmail());
        exception2MatchingExpression(nonNull(user), USERNAME_OR_EMAIL_ALREADY_EXIST);
    }

    /**
     * 验证user
     *
     * @param user
     */
    private void checkUser(User user) {
        exception2MatchingExpression(isNull(user), USERNAME_OR_PASSWORD_ERROR);
        user.checkStatus();
    }

    /**
     * 检查用户邮箱是否存在
     *
     * @param email
     */
    private void checkUserEmail(String email) {
        User queryUser = new User();
        queryUser.setEmail(email);
        User user = userRepository.findUserByEmail(email);
        exception2MatchingExpression(nonNull(user), EMAIL_ALREADY_EXIST);
    }

}
