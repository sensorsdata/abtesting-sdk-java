package com.sensorsdata.analytics.javasdk.bean.cache;

import lombok.Getter;
import lombok.Setter;

import java.io.Serializable;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

/**
 * 用户命中试验的缓存
 *
 * @author yanming@sensorsdata.cn
 * @version 1.0.0
 * @since 2022/11/22 3:46 PM
 */
@Setter
@Getter
public class UserHitExperiment implements Serializable {

  private static final long serialVersionUID = 6909159841058724181L;

  //key -> experimentId, value -> userHitDetail
  private Map<String, UserHitExperimentGroup> userHitExperimentMap;

  public UserHitExperiment() {
    userHitExperimentMap = new ConcurrentHashMap<>();
  }

  public void addUserHitExperimentGroup(String experimentId, UserHitExperimentGroup userHitExperimentGroup) {
    userHitExperimentMap.put(experimentId, userHitExperimentGroup);
  }
}
