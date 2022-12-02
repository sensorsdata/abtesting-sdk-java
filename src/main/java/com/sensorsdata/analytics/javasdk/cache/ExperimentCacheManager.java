package com.sensorsdata.analytics.javasdk.cache;

import static com.sensorsdata.analytics.javasdk.SensorsABTestConst.NAME_KEY;
import static com.sensorsdata.analytics.javasdk.SensorsABTestConst.TYPE_KEY;
import static com.sensorsdata.analytics.javasdk.SensorsABTestConst.VALUE_KEY;
import static com.sensorsdata.analytics.javasdk.util.ABTestUtil.map2Str;

import com.sensorsdata.analytics.javasdk.SensorsABTestConst;
import com.sensorsdata.analytics.javasdk.bean.cache.ExperimentGroupConfig;
import com.sensorsdata.analytics.javasdk.bean.cache.UserHitExperiment;
import com.sensorsdata.analytics.javasdk.bean.cache.UserHitExperimentGroup;
import com.sensorsdata.analytics.javasdk.bean.cache.Variable;
import com.sensorsdata.analytics.javasdk.util.SensorsAnalyticsUtil;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.google.common.cache.CacheBuilder;
import com.google.common.cache.CacheLoader;
import com.google.common.cache.LoadingCache;
import lombok.extern.slf4j.Slf4j;

import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.Iterator;
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
@Slf4j
public class ExperimentCacheManager {

  private final LoadingCache<String, UserHitExperiment> experimentResultCache;

  //元数据准备长期保留，不过期淘汰，否则引用这里的对象可能会出现各种不可知问题，且元数据占用内存很少，不会造成什么内存压力
  private final ConcurrentHashMap<String, ExperimentGroupConfig> experimentGroupConfigCache;



  public ExperimentCacheManager(int cacheTime, int cacheSize) {
    this.experimentResultCache = CacheBuilder.newBuilder()
        .expireAfterWrite(cacheTime, TimeUnit.MINUTES)
        .maximumSize(cacheSize)
        .build(new CacheLoader<String, UserHitExperiment>() {
          @Override
          public UserHitExperiment load(String s) {
            return null;
          }
        });
    log.info("Initializing experiment cache:size:{};duration:{}.", cacheSize, cacheTime);

    this.experimentGroupConfigCache = new ConcurrentHashMap<>();
  }

  /**
   * 从缓存中获取试验命中结果
   *
   * @param distinctId     唯一ID
   * @param isLoginId      是否是登录ID
   * @param customIds      自定义主体ID
   * @param experimentName 试验参数名
   * @return 试验命中结果
   */
  public UserHitExperimentGroup getExperimentResultByCache(String distinctId, boolean isLoginId,
      Map<String, String> customIds,
      String experimentName) {
    String key = generateUserResultCacheKey(distinctId, isLoginId, customIds);
    UserHitExperiment experimentResult = this.experimentResultCache.getIfPresent(key);
    if (experimentResult == null) {
      log.debug("current key not found in cache.[key:{},experiment:{}]", key, experimentName);
      return null;
    }
    Map<String, UserHitExperimentGroup> userHitExperimentMap = experimentResult.getUserHitExperimentMap();
    Iterator<Map.Entry<String, UserHitExperimentGroup>> iterator = userHitExperimentMap.entrySet().iterator();
    while (iterator.hasNext()) {
      Map.Entry<String, UserHitExperimentGroup> userHitExperimentGroupEntry = iterator.next();
      UserHitExperimentGroup userHitExperimentGroup = userHitExperimentGroupEntry.getValue();
      Map<String, Variable> variableMap = userHitExperimentGroup.getExperimentGroupConfig().getVariableMap();
      if (variableMap.get(experimentName) != null) {
        return userHitExperimentGroup;
      }
    }
    return null;
  }

  /**
   * 更新缓存
   *
   * @param distinctId 唯一ID
   * @param isLoginId  是否是登录ID
   * @param customIds  自定义主体ID
   * @param experiment 返回的分流结果
   */
  public void setExperimentResultCache(String distinctId, boolean isLoginId, Map<String, String> customIds,
      JsonNode experiment) {
    if (experiment != null) {
      UserHitExperiment userHitExperiment = mergeExperimentGroupConfig(experiment);
      String key = generateUserResultCacheKey(distinctId, isLoginId, customIds);
      log.debug("Caches the current experiment to the manager.[key:{},experiment:{}]", key, experiment);
      this.experimentResultCache.put(key, userHitExperiment);
    }
  }

  /**
   * 根据返回的分流结果，更新缓存中的试验组元数据
   *
   * @param experiment 返回的分流结果
   */
  public UserHitExperiment mergeExperimentGroupConfig(JsonNode experiment) {
    if (experiment == null) {
      log.debug("Experiment is null");
      return null;
    }

    UserHitExperiment userHitExperiment = new UserHitExperiment();
    JsonNode results = experiment.findValue(SensorsABTestConst.RESULTS_KEY);
    Iterator<JsonNode> resIterator = results.elements();
    while (resIterator.hasNext()) {
      JsonNode node = resIterator.next();
      final JsonNode variables = node.findValue(SensorsABTestConst.VARIABLES_KEY);
      String experimentId = node.findValue(SensorsABTestConst.EXPERIMENT_ID_KEY).asText();
      String experimentGroupId = node.findValue(SensorsABTestConst.EXPERIMENT_GROUP_ID_KEY).asText();

      String abtestUniqueId = null;
      JsonNode abtestUniqueIdNode = node.findValue(SensorsABTestConst.ABTEST_UNIQUE_ID);
      if (abtestUniqueIdNode != null) {
        abtestUniqueId = abtestUniqueIdNode.asText();
      }

      boolean isControlGroup = node.findValue(SensorsABTestConst.IS_CONTROL_GROUP_KEY).asBoolean();
      boolean isWhiteList = node.findValue(SensorsABTestConst.IS_WHITE_LIST_KEY).asBoolean();

      String experimentGroupCacheKey = abtestUniqueId != null && !abtestUniqueId.isEmpty() ?
          abtestUniqueId :
          generateExperimentGroupConfigCacheKey(experimentId, experimentGroupId);

      ExperimentGroupConfig experimentGroupConfig = experimentGroupConfigCache.get(experimentGroupCacheKey);
      ExperimentGroupConfig newExperimentGroupConfig =
          new ExperimentGroupConfig(experimentId, experimentGroupId, isControlGroup, convertToVariable(variables));
      if (experimentGroupConfig == null) {
        experimentGroupConfigCache.putIfAbsent(experimentGroupCacheKey, newExperimentGroupConfig);
      } else {
        experimentGroupConfig.setControlGroup(newExperimentGroupConfig.isControlGroup());
        experimentGroupConfig.setVariableMap(newExperimentGroupConfig.getVariableMap());
      }
      UserHitExperimentGroup userHitExperimentGroup =
          new UserHitExperimentGroup(isWhiteList, experimentGroupConfigCache.get(experimentGroupCacheKey));
      userHitExperiment.addUserHitExperimentGroup(experimentId, userHitExperimentGroup);
    }
    return userHitExperiment;
  }

  /**
   * 将Json的variable转换成缓存中使用的variableMap
   *
   * @param variables Json variable字段
   * @return variable Map
   */
  private Map<String, Variable> convertToVariable(JsonNode variables) {
    ConcurrentHashMap<String, Variable> variableMap = new ConcurrentHashMap<>();
    if (variables == null) {
      return variableMap;
    }
    Iterator<JsonNode> iterator = variables.elements();
    while (iterator.hasNext()) {
      JsonNode next = iterator.next();
      Variable variable = new Variable(next.findValue(NAME_KEY).asText(), next.findValue(TYPE_KEY).asText(),
          next.findValue(VALUE_KEY).asText());
      variableMap.putIfAbsent(variable.getName(), variable);
    }
    return variableMap;
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
   * 生成用户命中缓存key
   *
   * @param distinctId 唯一ID
   * @param isLoginId  是否是登录ID
   * @param customIds  自定义主体ID
   * @return 缓存key
   */
  private String generateUserResultCacheKey(String distinctId, boolean isLoginId, Map<String, String> customIds) {
    String key = String.format("%s_%b_%s", distinctId, isLoginId, map2Str(customIds));
    try {
      MessageDigest instance = MessageDigest.getInstance("MD5");
      instance.update(key.getBytes(StandardCharsets.UTF_8));
      return new String(instance.digest(), StandardCharsets.UTF_8);
    } catch (NoSuchAlgorithmException e) {
      log.error("failed generate cache md5 key,so use original key.[distinctId:{},isLoginId:{},customIds:{}].",
          distinctId, isLoginId, map2Str(customIds));
    }
    return key;
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

}
