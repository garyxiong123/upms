package com.yofish.apollo.model.model;


import framework.apollo.core.enums.Env;
import common.utils.YyStringUtils;
import lombok.Data;

@Data
public class NamespaceReleaseModel implements Verifiable {

  private Long AppEnvClusterNamespaceId;
  private String releaseTitle;
  private String releaseComment;
  private String releasedBy;
  private boolean isEmergencyPublish;

  @Override
  public boolean isInvalid() {
    return YyStringUtils.isContainEmpty(String.valueOf(AppEnvClusterNamespaceId), releaseTitle);
  }


}
