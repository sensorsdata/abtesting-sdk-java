package com.sensorsdata.analytics.javasdk.cache;

import static com.sensorsdata.analytics.javasdk.SensorsABTestConst.NAME_KEY;
import static com.sensorsdata.analytics.javasdk.SensorsABTestConst.TYPE_KEY;
import static com.sensorsdata.analytics.javasdk.SensorsABTestConst.VALUE_KEY;

import com.sensorsdata.analytics.javasdk.SensorsABTestConst;
import com.sensorsdata.analytics.javasdk.bean.UserInfo;
import com.sensorsdata.analytics.javasdk.bean.cache.ExperimentGroupConfig;
import com.sensorsdata.analytics.javasdk.bean.cache.UserHitExperiment;
import com.sensorsdata.analytics.javasdk.bean.cache.UserHitExperimentGroup;
import com.sensorsdata.analytics.javasdk.bean.cache.UserOutExperimentGroup;
import com.sensorsdata.analytics.javasdk.bean.cache.Variable;
import com.sensorsdata.analytics.javasdk.util.ABTestUtil;
import com.sensorsdata.analytics.javasdk.util.LogUtil;

import com.fasterxml.jackson.databind.JsonNode;
import com.google.common.cache.CacheBuilder;
import com.google.common.cache.CacheLoader;
import com.google.common.cache.LoadingCache;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.TimeUnit;

/**
 * AB Test 试验结果缓存管理器
 *
 * @author fangzhuo@sensorsdata.cn
 * @version 1.0.0
 * @since 2021/06/16 15:41
 */
public class ExperimentCacheManager {

  private final LoadingCache<String, UserHitExperiment> experimentResultCache;

  //元数据准备长期保留，不过期淘汰，否则引用这里的对象可能会出现各种不可知问题，且元数据占用内存很少，不会造成什么内存压力
  private final ConcurrentHashMap<String, ExperimentGroupConfig> experimentGroupConfigCache;

  private final LogUtil log;


  public ExperimentCacheManager(LogUtil log, int cacheTime, int cacheSize) {
    this.experimentResultCache = CacheBuilder.newBuilder()
        .expireAfterWrite(cacheTime, TimeUnit.MINUTES)
        .maximumSize(cacheSize)
        .build(new CacheLoader<String, UserHitExperiment>() {
          @Override
          public UserHitExperiment load(String s) {
            return null;
          }
        });
    this.log = log;
    this.log.info("Initializing experiment cache:size:{};duration:{}.", cacheSize, cacheTime);

    this.experimentGroupConfigCache = new ConcurrentHashMap<>();
  }

  /**
   * 从缓存中获取试验命中结果
   *
   * @param userInfo       用户信息标识
   * @param experimentName 试验参数名
   * @return 试验命中结果
   */
  public UserHitExperimentGroup getExperimentResultByCache(UserInfo userInfo, String experimentName) {
    String key = ABTestUtil.generateUserResultCacheKey(userInfo, log);
    UserHitExperiment experimentResult = this.experimentResultCache.getIfPresent(key);
    if (experimentResult == null) {
      log.debug("current key not found in cache.[key:{},experiment:{}]", key, experimentName);
      return null;
    }
    return getExperimentResultFromUserHitExperiment(experimentName, experimentResult);
  }

  public UserHitExperimentGroup getExperimentResultFromUserHitExperiment(String param,
      UserHitExperiment userHitExperiment) {

    if (userHitExperiment == null) {
      log.debug("user hit experiment is not exist, param: [{}]", param);
      return null;
    }

    Map<String, UserHitExperimentGroup> userHitExperimentMap = userHitExperiment.getUserHitExperimentMap();
    for (Map.Entry<String, UserHitExperimentGroup> userHitExperimentGroupEntry : userHitExperimentMap.entrySet()) {
      UserHitExperimentGroup userHitExperimentGroup = userHitExperimentGroupEntry.getValue();
      Map<String, Variable> variableMap = userHitExperimentGroup.getExperimentGroupConfig().getVariableMap();
      if (variableMap.get(param) != null) {
        return userHitExperimentGroup;
      }
    }
    return null;

  }

  /**
   * 解析用户分流结果，并加入缓存
   *
   * @param userInfo          用户标识信息
   * @param experimentResults 返回的分流结果
   */
  public UserHitExperiment getUserHitExperimentWithUpdateCache(UserInfo userInfo,
      JsonNode experimentResults) {
    UserHitExperiment userHitExperiment = getUserHitExperimentWithoutUpdateCache(experimentResults);
    String key = ABTestUtil.generateUserResultCacheKey(userInfo, log);
    log.debug("Caches the current experiment to the manager.[key:{},experiment:{}]", key, experimentResults);
    UserHitExperiment cachedUserHitExperiment = getCachedUserHitExperiment(userHitExperiment);
    this.experimentResultCache.put(key, cachedUserHitExperiment);
    return userHitExperiment;
  }


  /**
   * 根据试验组配置字段获取能缓存的用户分流结果
   *
   * @param userHitExperiment 本次所有用户分流结果
   * @return 能缓存的用户分流结果
   */
  private UserHitExperiment getCachedUserHitExperiment(UserHitExperiment userHitExperiment) {
    UserHitExperiment cachedUserHitExperiment = new UserHitExperiment();
    for (Map.Entry<String, UserHitExperimentGroup> userHitExperimentGroupEntry : userHitExperiment.getUserHitExperimentMap().entrySet()) {
      if (isNeedCache(userHitExperimentGroupEntry.getValue())) {
        cachedUserHitExperiment.addUserHitExperimentGroup(userHitExperimentGroupEntry.getKey(),
            userHitExperimentGroupEntry.getValue());
      }
    }
    return cachedUserHitExperiment;
  }

  /**
   * 是否需要缓存
   *
   * @param userHitExperimentGroup 用户本次命中结果
   * @return 是否需要缓存
   */
  private boolean isNeedCache(UserHitExperimentGroup userHitExperimentGroup) {
    //SaaS返回该结果可以缓存且非白名单命中

    return userHitExperimentGroup.isCacheable();

  }

  /**
   * 将Json的variable转换成缓存中使用的variableMap
   *
   * @param variables Json variable字段
   * @return variable Map
   */
  private Map<String, Variable> convertToVariable(JsonNode variables) {
    HashMap<String, Variable> variableMap = new HashMap<>();
    if (variables == null) {
      return variableMap;
    }
    Iterator<JsonNode> iterator = variables.elements();
    while (iterator.hasNext()) {
      JsonNode next = iterator.next();
      Variable variable =
          new Variable(getTextValueFromJsonNode(next, NAME_KEY), getTextValueFromJsonNode(next, TYPE_KEY),
              getTextValueFromJsonNode(next, VALUE_KEY));
      variableMap.put(variable.getName(), variable);
    }
    return variableMap;
  }

  private String getTextValueFromJsonNode(JsonNode node, String name) {
    JsonNode value = node.findValue(name);
    if (value != null) {
      return value.asText();
    }
    return null;
  }

  private boolean getBooleanValueFromJsonNode(JsonNode node, String name, boolean defaultValue) {
    JsonNode value = node.findValue(name);
    if (value != null) {
      return value.asBoolean();
    }
    return defaultValue;
  }


  /**
   * 解析用户分流结果
   *
   * @param experimentResults 返回的分流结果
   */
  public UserHitExperiment getUserHitExperimentWithoutUpdateCache(JsonNode experimentResults) {
    if (experimentResults == null) {
      log.debug("Experiment is null");
      return null;
    }

    UserHitExperiment userHitExperiment = new UserHitExperiment();
    Iterator<JsonNode> resIterator = experimentResults.elements();
    while (resIterator.hasNext()) {
      JsonNode node = resIterator.next();
      //必填字段
      String experimentId = getTextValueFromJsonNode(node, SensorsABTestConst.EXPERIMENT_ID_KEY);
      String experimentGroupId = getTextValueFromJsonNode(node, SensorsABTestConst.EXPERIMENT_GROUP_ID_KEY);
      JsonNode variables = node.findValue(SensorsABTestConst.VARIABLES_KEY);
      boolean isControlGroup = node.findValue(SensorsABTestConst.IS_CONTROL_GROUP_KEY).asBoolean();
      boolean isWhiteList = node.findValue(SensorsABTestConst.IS_WHITE_LIST_KEY).asBoolean();

      //非必填字段
      String abtestExperimentResultId =
          getTextValueFromJsonNode(node, SensorsABTestConst.ABTEST_EXPERIMENT_RESULT_ID_KEY);
      String abtestExperimentVersion = getTextValueFromJsonNode(node, SensorsABTestConst.ABTEST_EXPERIMENT_VERSION_KEY);
      String subjectName = getTextValueFromJsonNode(node, SensorsABTestConst.ABTEST_SUBJECT_NAME_KEY);
      String subjectId = getTextValueFromJsonNode(node, SensorsABTestConst.ABTEST_SUBJECT_ID_KEY);

      boolean cacheable = getBooleanValueFromJsonNode(node, SensorsABTestConst.ABTEST_CACHEABLE_KEY, true);



      ExperimentGroupConfig newExperimentGroupConfig = ExperimentGroupConfig
          .builder()
          .abtestExperimentId(experimentId)
          .abtestExperimentGroupId(experimentGroupId)
          .abtestExperimentResultId(abtestExperimentResultId)
          .isControlGroup(isControlGroup)
          .abtestExperimentVersion(abtestExperimentVersion)
          .src(node)
          .variableMap(convertToVariable(variables))
          .subjectName(subjectName)
          .build();


      //生成cache key
      String experimentGroupCacheKey;
      if (abtestExperimentResultId != null && !abtestExperimentResultId.isEmpty()) {
        experimentGroupCacheKey = abtestExperimentResultId;
      } else {
        experimentGroupCacheKey = generateExperimentGroupConfigCacheKey(experimentId, experimentGroupId);
      }
      //更新试验配置元数据缓存
      saveOrUpdateExperimentGroupConfig(newExperimentGroupConfig, experimentGroupCacheKey);

      UserHitExperimentGroup userHitExperimentGroup
          = UserHitExperimentGroup.builder()
          .subjectId(subjectId)
          .isWhiteList(isWhiteList)
          .cacheable(cacheable)
          .experimentGroupConfig(experimentGroupConfigCache.get(experimentGroupCacheKey))
          .build();

      userHitExperiment.addUserHitExperimentGroup(experimentId, userHitExperimentGroup);
    }

    return userHitExperiment;
  }

  private void saveOrUpdateExperimentGroupConfig(ExperimentGroupConfig newExperimentGroupConfig,
      String experimentGroupCacheKey) {
    ExperimentGroupConfig experimentGroupConfig = experimentGroupConfigCache.get(experimentGroupCacheKey);
    if (experimentGroupConfig == null) {
      experimentGroupConfigCache.putIfAbsent(experimentGroupCacheKey, newExperimentGroupConfig);
    } else {
      experimentGroupConfig.setControlGroup(newExperimentGroupConfig.isControlGroup());
      experimentGroupConfig.setVariableMap(newExperimentGroupConfig.getVariableMap());
      experimentGroupConfig.setSrc(newExperimentGroupConfig.getSrc());
    }
  }



  /**
   * 获取当前缓存大小
   *
   * @return 缓存大小
   */
  public long getCacheSize() {
    return this.experimentResultCache.size();
  }


  /**
   * 生成元数据缓存的key experimentId_experimentGroupId
   *
   * @param experimentId      试验ID
   * @param experimentGroupId 试验组ID
   * @return 元数据缓存key
   */
  private String generateExperimentGroupConfigCacheKey(String experimentId, String experimentGroupId) {
    return experimentId + "_" + experimentGroupId;
  }

  public List<UserOutExperimentGroup> getUserOutExperimentGroups(String param, JsonNode outResults) {

    if (outResults == null) {
      log.debug("Experiment is null");
      return Collections.emptyList();
    }

    List<UserOutExperimentGroup> userOutExperimentGroups = new ArrayList<>();

    Iterator<JsonNode> resIterator = outResults.elements();
    while (resIterator.hasNext()) {
      JsonNode node = resIterator.next();
      //必填字段
      String experimentId = getTextValueFromJsonNode(node, SensorsABTestConst.EXPERIMENT_ID_KEY);
      String experimentGroupId = getTextValueFromJsonNode(node, SensorsABTestConst.EXPERIMENT_GROUP_ID_KEY);
      String abtestExperimentResultId =
          getTextValueFromJsonNode(node, SensorsABTestConst.ABTEST_EXPERIMENT_RESULT_ID_KEY);
      String abtestExperimentVersion = getTextValueFromJsonNode(node, SensorsABTestConst.ABTEST_EXPERIMENT_VERSION_KEY);
      String subjectName = getTextValueFromJsonNode(node, SensorsABTestConst.ABTEST_SUBJECT_NAME_KEY);
      String subjectId = getTextValueFromJsonNode(node, SensorsABTestConst.ABTEST_SUBJECT_ID_KEY);
      JsonNode variables = node.findValue(SensorsABTestConst.VARIABLES_KEY);
      boolean isWhiteList = getBooleanValueFromJsonNode(node, SensorsABTestConst.IS_WHITE_LIST_KEY, false);

      Map<String, Variable> variableMap = convertToVariable(variables);
      if (variableMap.containsKey(param)) {
        userOutExperimentGroups.add(UserOutExperimentGroup.builder()
            .param(param)
            .subjectId(subjectId)
            .subjectName(subjectName)
            .abtestExperimentId(experimentId)
            .abtestExperimentGroupId(experimentGroupId)
            .abtestExperimentResultId(abtestExperimentResultId)
            .variableMap(variableMap)
            .whiteList(isWhiteList)
            .abtestExperimentVersion(abtestExperimentVersion)
            .src(node).build());
      }
    }
    return userOutExperimentGroups;
  }


  public ExperimentGroupConfig getExperimentGroupConfig(String experimentId, String experimentGroupId,
      String abtestExperimentResultId) {
    //生成cache key
    String experimentGroupCacheKey;
    if (abtestExperimentResultId != null && !abtestExperimentResultId.isEmpty()) {
      experimentGroupCacheKey = abtestExperimentResultId;
    } else {
      experimentGroupCacheKey = generateExperimentGroupConfigCacheKey(experimentId, experimentGroupId);
    }

    return experimentGroupConfigCache.get(experimentGroupCacheKey);
  }

}
