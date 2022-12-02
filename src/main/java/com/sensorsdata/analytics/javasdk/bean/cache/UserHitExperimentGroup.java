package com.sensorsdata.analytics.javasdk.bean.cache;

import lombok.Getter;
import lombok.Setter;

import java.io.Serializable;

/**
 * 用户命中详情，比试验组配置多了是否以白名单命中的属性项
 *
 * @author yanming@sensorsdata.cn
 * @version 1.0.0
 * @since 2022/11/22 4:47 PM
 */
@Getter
@Setter
public class UserHitExperimentGroup implements Serializable {

  private static final long serialVersionUID = -2624648063644487605L;
  private boolean isWhiteList;
  private ExperimentGroupConfig experimentGroupConfig;

  public UserHitExperimentGroup(boolean isWhiteList, ExperimentGroupConfig experimentGroupConfig) {
    this.isWhiteList = isWhiteList;
    this.experimentGroupConfig = experimentGroupConfig;
  }
}
