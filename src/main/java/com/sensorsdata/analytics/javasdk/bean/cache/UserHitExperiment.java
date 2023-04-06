package com.sensorsdata.analytics.javasdk.bean.cache;


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
public class UserHitExperiment implements Serializable {

  private static final long serialVersionUID = 6909159841058724181L;

  //key -> experimentId, value -> UserHitExperimentGroup
  private Map<String, UserHitExperimentGroup> userHitExperimentMap;

  public UserHitExperiment() {
    userHitExperimentMap = new ConcurrentHashMap<>();
  }

  public void addUserHitExperimentGroup(String experimentId, UserHitExperimentGroup userHitExperimentGroup) {
    userHitExperimentMap.put(experimentId, userHitExperimentGroup);
  }

  public Map<String, UserHitExperimentGroup> getUserHitExperimentMap() {
    return userHitExperimentMap;
  }

  public void setUserHitExperimentMap(
      Map<String, UserHitExperimentGroup> userHitExperimentMap) {
    this.userHitExperimentMap = userHitExperimentMap;
  }
}
