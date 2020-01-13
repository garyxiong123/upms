/*
 *    Copyright 2019-2020 the original author or authors.
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
package com.yofish.apollo.model.vo;


import framework.apollo.core.dto.ServiceDTO;
import framework.apollo.core.enums.Env;

public class EnvironmentInfo {

  private Env env;
  private boolean active;
  private String metaServerAddress;

  private ServiceDTO[] configServices;
  private ServiceDTO[] adminServices;

  private String errorMessage;

  public Env getEnv() {
    return env;
  }

  public void setEnv(Env env) {
    this.env = env;
  }

  public boolean isActive() {
    return active;
  }

  public void setActive(boolean active) {
    this.active = active;
  }

  public String getMetaServerAddress() {
    return metaServerAddress;
  }

  public void setMetaServerAddress(String metaServerAddress) {
    this.metaServerAddress = metaServerAddress;
  }

  public ServiceDTO[] getConfigServices() {
    return configServices;
  }

  public void setConfigServices(ServiceDTO[] configServices) {
    this.configServices = configServices;
  }

  public ServiceDTO[] getAdminServices() {
    return adminServices;
  }

  public void setAdminServices(ServiceDTO[] adminServices) {
    this.adminServices = adminServices;
  }

  public String getErrorMessage() {
    return errorMessage;
  }

  public void setErrorMessage(String errorMessage) {
    this.errorMessage = errorMessage;
  }
}
