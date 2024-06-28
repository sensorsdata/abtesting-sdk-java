package com.sensorsdata.analytics.javasdk;

import static com.sensorsdata.analytics.javasdk.SensorsABTestConst.OUT_LIST_KEY;
import static com.sensorsdata.analytics.javasdk.SensorsABTestConst.RESULTS_KEY;

import com.sensorsdata.analytics.javasdk.bean.ABGlobalConfig;
import com.sensorsdata.analytics.javasdk.bean.Experiment;
import com.sensorsdata.analytics.javasdk.bean.TrackConfig;
import com.sensorsdata.analytics.javasdk.bean.TrackRecord;
import com.sensorsdata.analytics.javasdk.bean.UserInfo;
import com.sensorsdata.analytics.javasdk.bean.cache.ExperimentGroupConfig;
import com.sensorsdata.analytics.javasdk.bean.cache.UserHitExperiment;
import com.sensorsdata.analytics.javasdk.bean.cache.UserHitExperimentGroup;
import com.sensorsdata.analytics.javasdk.bean.cache.UserHitResult;
import com.sensorsdata.analytics.javasdk.bean.cache.UserOutExperimentGroup;
import com.sensorsdata.analytics.javasdk.bean.cache.Variable;
import com.sensorsdata.analytics.javasdk.cache.EventCacheManager;
import com.sensorsdata.analytics.javasdk.cache.ExperimentCacheManager;
import com.sensorsdata.analytics.javasdk.cache.HitManager;
import com.sensorsdata.analytics.javasdk.common.Pair;
import com.sensorsdata.analytics.javasdk.exceptions.InvalidArgumentException;
import com.sensorsdata.analytics.javasdk.service.ITrackConfigService;
import com.sensorsdata.analytics.javasdk.service.ITrackService;
import com.sensorsdata.analytics.javasdk.service.impl.TrackConfigService;
import com.sensorsdata.analytics.javasdk.service.impl.TrackService;
import com.sensorsdata.analytics.javasdk.util.ABTestUtil;
import com.sensorsdata.analytics.javasdk.util.HttpConsumer;
import com.sensorsdata.analytics.javasdk.util.LogUtil;
import com.sensorsdata.analytics.javasdk.util.SensorsAnalyticsUtil;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.google.common.collect.Maps;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

/**
 * AB Test 逻辑处理
 *
 * @author fangzhuo@sensorsdata.cn
 * @version 1.0.0
 * @since 2021/06/16 15:12
 */
class SensorsABTestWorker {

  private static final Logger LOGGER = LoggerFactory.getLogger(SensorsABTestWorker.class);

  private final ObjectMapper objectMapper;
  /**
   * 全局配置信息
   */
  private final ABGlobalConfig config;
  /**
   * 试验结果缓存管理器
   */
  private ExperimentCacheManager experimentCacheManager;
  /**
   * 试验上报事件缓存
   */
  private EventCacheManager eventCacheManager;

  /**
   * 网络请求对象
   */
  private HttpConsumer httpConsumer;

  private LogUtil log;

  private HitManager hitManager;


  private ITrackConfigService trackConfigService;


  private ITrackService trackService;

  SensorsABTestWorker(ABGlobalConfig config) {
    log = new LogUtil(LOGGER, config.getLogLevel());

    this.config = config;
    this.objectMapper = SensorsAnalyticsUtil.getJsonObjectMapper();
    this.experimentCacheManager = createExperimentCacheManager(config);
    this.eventCacheManager = createEventCacheManager(config);
    this.httpConsumer = createHttpConsumer(config);
    this.hitManager = createHitManager();
    this.trackConfigService = createTrackConfigService();

    this.trackService = createTrackService(config);

    log.info("init SensorsABTest with config info:{}.", config);
  }

  protected HitManager createHitManager() {
    return new HitManager(this.log);
  }

  protected ITrackConfigService createTrackConfigService() {
    return new TrackConfigService(this.log, TrackConfig.getDefaultTrackConfig());
  }

  protected ITrackService createTrackService(ABGlobalConfig config) {
    boolean enableEventCache = config.getEnableEventCache() != null && config.getEnableEventCache();
    return new TrackService(eventCacheManager, log, enableEventCache, config.getSensorsAnalytics(), trackConfigService);
  }

  protected HttpConsumer createHttpConsumer(ABGlobalConfig config) {
    return new HttpConsumer(
        this.log,
        config.getEnableRecordRequestCostTime(),
        config.getHttpClientBuilder(),
        config.getApiUrl(),
        config.getMaxTotal(),
        config.getMaxPerRoute());
  }

  protected EventCacheManager createEventCacheManager(ABGlobalConfig config) {
    return new EventCacheManager(
        this.log,
        config.getEventCacheTime(),
        config.getEventCacheSize());
  }

  protected ExperimentCacheManager createExperimentCacheManager(ABGlobalConfig config) {
    return new ExperimentCacheManager(
        this.log,
        config.getExperimentCacheTime(),
        config.getExperimentCacheSize());
  }

  /**
   * 处理AB Test 结果
   *
   * @param <T> params 请求参数类
   * @return Experiment<T> 返回试验结果
   */
  <T> Experiment<T> fetchABTest(SensorsABParams<T> sensorsParams) {

    if (sensorsParams == null) {
      throw new NullPointerException("sensorsParams is marked non-null but is null");
    }

    Experiment<T> invalidSensorsParams = checkSensorsParams(sensorsParams);
    if (invalidSensorsParams != null) return invalidSensorsParams;

    UserInfo userInfo = UserInfo.builder()
        .distinctId(sensorsParams.getDistinctId())
        .isLoginId(sensorsParams.getIsLoginId())
        .customIds(sensorsParams.getCustomIds())
        .customProperties(sensorsParams.getProperties())
        .build();

    String paramName = sensorsParams.getExperimentVariableName();

    UserHitResult userHitResult =
        getUserHitResult(userInfo, paramName, sensorsParams.getEnableCache(), sensorsParams.getTimeoutMilliseconds());

    List<TrackRecord> toTrack =
        getToTrack(userInfo, paramName, sensorsParams.getDefaultValue(), userHitResult);

    Experiment<T> result = convertExperiment(userHitResult.getUserHitExperimentGroup(), sensorsParams.getDistinctId(),
        sensorsParams.getIsLoginId(),
        paramName, sensorsParams.getDefaultValue());

    //判断是否需要自动触发上报事件
    if (sensorsParams.getEnableAutoTrackEvent()) {
      try {
        this.trackService.trackABTestTrigger(toTrack, null);
      } catch (InvalidArgumentException e) {
        log.error("Failed auto track ABTest event.[distinctId:{},isLoginId:{},experimentVariableName:{}, toTrack: {}]",
            sensorsParams.getDistinctId(),
            sensorsParams.getIsLoginId(),
            paramName, toTrack, e);
      }
    }
    return result;
  }

  <T> List<TrackRecord> getToTrack(UserInfo userInfo, String paramName, T defaultValue,
      UserHitResult userHitResult) {
    filterValidResult(userHitResult, paramName, defaultValue);
    return hitManager.getToTrack(userInfo, paramName, userHitResult);
  }

  /**
   * 根据类型判断是否是有效的命中/出组
   *
   * @param userHitResult 命中结果
   * @param paramName     参数名
   * @param defaultValue  默认值
   * @param <T>
   */
  private <T> void filterValidResult(UserHitResult userHitResult, String paramName, T defaultValue) {
    UserHitExperimentGroup userHitExperimentGroup = userHitResult.getUserHitExperimentGroup();
    if (userHitExperimentGroup != null) {
      Variable variable = userHitExperimentGroup.getExperimentGroupConfig()
          .getVariableMap().get(paramName);
      if (getExperimentValue(variable.getType(), defaultValue, variable.getValue()) == null) {
        log.debug("invalid param value type, type: [{}], value: [{}], defaultValue:[{}]", variable.getType(),
            variable.getValue(), defaultValue);
        userHitResult.setUserHitExperimentGroup(null);
      }
    }

    List<UserOutExperimentGroup> userOutExperimentGroups = userHitResult.getUserOutExperimentGroups();
    if (userOutExperimentGroups != null) {
      Iterator<UserOutExperimentGroup> iterator = userOutExperimentGroups.iterator();
      while (iterator.hasNext()) {
        UserOutExperimentGroup out = iterator.next();
        Variable variable = out.getVariableMap().get(paramName);
        if (getExperimentValue(variable.getType(), defaultValue, variable.getValue()) == null) {
          log.debug("invalid out param value type, type: [{}], value: [{}], defaultValue:[{}]", variable.getType(),
              variable.getValue(), defaultValue);
          iterator.remove();
        }
      }
    }

  }


  private UserHitResult getUserHitResult(UserInfo userInfo, String paramName, Boolean enableCache,
      Integer timeoutMilliseconds) {
    UserHitResult userHitResult;
    //判断是否需要读取结果缓存
    if (enableCache) {
      log.debug("Enable priority read experiment of cache.[distinctId:{};experimentVariableName:{}]",
          userInfo.getDistinctId(), paramName);
      userHitResult = getUserHitResultFromCache(userInfo, paramName);
      //未命中缓存
      if (userHitResult.getUserHitExperimentGroup() == null) {
        log.debug("Not hit experiment cache,making network request.[distinctId:{};experimentVariableName:{}]",
            userInfo.getDistinctId(), paramName);
        userHitResult = getHitResultFromResponse(userInfo, paramName, enableCache, timeoutMilliseconds);
      }
    } else {
      log.debug("Get results from server.[distinctId:{};experimentVariableName:{}]",
          userInfo.getDistinctId(), paramName);
      userHitResult = getHitResultFromResponse(userInfo, paramName, enableCache, timeoutMilliseconds);
    }
    return userHitResult;
  }

  private UserHitResult getHitResultFromResponse(UserInfo userInfo, String paramName, Boolean enableCache,
      Integer timeoutMilliseconds) {
    UserHitResult userHitResult;
    JsonNode response;
    response = getDispatcherResponse(userInfo, paramName, timeoutMilliseconds);
    userHitResult = UserHitResult.builder()
        .userHitExperimentGroup(
            getUserHitExperimentGroup(userInfo, paramName, response, enableCache))
        .userOutExperimentGroups(
            getUserOutExperimentGroups(userInfo, paramName, response))
        .build();
    updateTrackConfig(response);
    return userHitResult;
  }

  private UserHitResult getUserHitResultFromCache(UserInfo userInfo, String paramName) {
    UserHitExperimentGroup userHitExperimentGroup;
    userHitExperimentGroup =
        experimentCacheManager.getExperimentResultByCache(userInfo, paramName);
    return UserHitResult.builder()
        .userHitExperimentGroup(userHitExperimentGroup)
        .build();
  }


  private void updateTrackConfig(JsonNode response) {
    if (response == null) {
      return;
    }
    JsonNode trackConfigNode = response.findValue(SensorsABTestConst.ABTEST_TRACK_CONFIG_KEY);
    if (trackConfigNode == null) {
      return;
    }
    TrackConfig trackConfig = objectMapper.convertValue(trackConfigNode, TrackConfig.class);
    trackConfigService.updateTrackConfig(trackConfig);

  }

  private List<UserOutExperimentGroup> getUserOutExperimentGroups(UserInfo userInfo, String param, JsonNode response) {
    if (response == null) {
      log.debug("response from server is null. [UserInfo: {}, param: {}]", userInfo, param);
      return Collections.emptyList();
    }

    JsonNode outResults = response.findValue(OUT_LIST_KEY);
    if (outResults == null) {
      log.debug("response outResults from server is null. [UserInfo: {}, param: {}]", userInfo, param);
      return Collections.emptyList();
    }

    return experimentCacheManager.getUserOutExperimentGroups(param, outResults);
  }


  public <T> void trackABTestTrigger(Experiment<T> result,
      Map<String, Object> properties,
      Map<String, String> customIds,
      List<TrackRecord> toTrack) throws InvalidArgumentException {

    if (toTrack == null) {
      toTrack = new ArrayList<>();
      log.debug("to track list is null, default to empty list");
    }

    if (toTrack.isEmpty()) {
      TrackRecord experimentTrackRecord = experimentToTrackRecord(result, customIds);
      if (experimentTrackRecord != null) {
        toTrack.add(experimentTrackRecord);
        log.debug("Transfer experiment result to track record, experiment result: [{}], track record: [{}]", result,
            experimentTrackRecord);
      }
    }

    this.trackService.trackABTestTrigger(toTrack, properties);

  }

  private <T> TrackRecord experimentToTrackRecord(Experiment<T> experiment, Map<String, String> customIds) {
    if (experiment == null || experiment.getAbTestExperimentId() == null) {
      return null;
    }
    Boolean isWhiteList = experiment.getIsWhiteList();
    ExperimentGroupConfig experimentGroupConfig =
        experimentCacheManager.getExperimentGroupConfig(experiment.getAbTestExperimentId(),
            experiment.getAbTestExperimentGroupId(),
            experiment.getAbtestExperimentResultId());
    return TrackRecord.builder()
        .cacheable(false)
        .abtestExperimentId(experiment.getAbTestExperimentId())
        .abtestExperimentResultId(experiment.getAbtestExperimentResultId())
        .userInfo(
            UserInfo
                .builder()
                .distinctId(experiment.getDistinctId())
                .isLoginId(experiment.getIsLoginId())
                .customIds(customIds)
                .build())
        .abtestExperimentGroupId(experiment.getAbTestExperimentGroupId())
        .isWhiteList(isWhiteList != null && isWhiteList)
        .src(experimentGroupConfig != null ? experimentGroupConfig.getSrc() : null)
        .subjectName(experimentGroupConfig != null ? experimentGroupConfig.getSubjectName() : null)
        .build();
  }

  private <T> Experiment<T> checkSensorsParams(SensorsABParams<T> sensorsParams) {
    if (sensorsParams.getDistinctId() == null || sensorsParams.getDistinctId().isEmpty()) {
      log.warn("The distinctId is empty or null,return defaultValue.");
      return new Experiment<>(
          sensorsParams.getDistinctId(),
          sensorsParams.getIsLoginId(),
          sensorsParams.getDefaultValue());
    }
    if (sensorsParams.getExperimentVariableName() == null || sensorsParams.getExperimentVariableName().isEmpty()) {
      log.warn("The experimentVariableName is empty or null,return defaultValue,distinctId:{}.",
          sensorsParams.getDistinctId());
      return new Experiment<>(
          sensorsParams.getDistinctId(),
          sensorsParams.getIsLoginId(),
          sensorsParams.getDefaultValue());
    }
    if (!ABTestUtil.assertDefaultValueType(sensorsParams.getDefaultValue())) {
      log.warn(
          "The type of defaultValue is invalid.the current type of defaultValue is {};distinctId:{};experimentVariableName:{}.",
          sensorsParams.getDefaultValue() == null ? "null" : sensorsParams.getDefaultValue().getClass().toString(),
          sensorsParams.getDistinctId(), sensorsParams.getExperimentVariableName());
      return new Experiment<>(
          sensorsParams.getDistinctId(),
          sensorsParams.getIsLoginId(),
          sensorsParams.getDefaultValue());
    }
    Pair<Boolean, String> customIdCheckRes = ABTestUtil.assertCustomIds(sensorsParams);
    if (customIdCheckRes.getKey()) {
      log.warn(customIdCheckRes.getValue());
      return new Experiment<>(
          sensorsParams.getDistinctId(),
          sensorsParams.getIsLoginId(),
          sensorsParams.getDefaultValue());
    }
    return null;
  }

  /**
   * 获取用户命中结果
   *
   * @param userInfo           用户信息标识
   * @param param              请求参数
   * @param dispatcherResponse 原始response
   * @param enableUserCache    是否缓存用户分流结果
   * @return 用户命中结果
   */
  private UserHitExperimentGroup getUserHitExperimentGroup(UserInfo userInfo, String param,
      JsonNode dispatcherResponse, boolean enableUserCache) {

    if (dispatcherResponse == null) {
      log.debug("response from server is null. [UserInfo: {}, param: {}]", userInfo, param);
      return null;
    }

    JsonNode results = dispatcherResponse.findValue(RESULTS_KEY);
    if (results == null) {
      log.debug("response results from server is null. [UserInfo: {}, param: {}]", userInfo, param);
      return null;
    }

    log.debug("Hit experiment from server,cache the experiment results.[userInfo:{};experimentVariableName:{}]",
        userInfo, param);

    UserHitExperiment userHitExperiment;
    if (enableUserCache) {
      userHitExperiment = experimentCacheManager.getUserHitExperimentWithUpdateCache(userInfo, results);
    } else {
      userHitExperiment = experimentCacheManager.getUserHitExperimentWithoutUpdateCache(results);
    }

    return experimentCacheManager.getExperimentResultFromUserHitExperiment(param, userHitExperiment);
  }


  private JsonNode getDispatcherResponse(UserInfo userInfo, String param, Integer timeoutMilliseconds) {
    return getABTestByHttp(
        userInfo.getDistinctId(),
        userInfo.isLoginId(),
        param,
        timeoutMilliseconds,
        userInfo.getCustomProperties(),
        userInfo.getCustomIds());
  }


  private <T> Experiment<T> convertExperiment(UserHitExperimentGroup userHitExperimentGroup, String distinctId,
      Boolean isLoginId, String experimentVariableName, T defaultValue) {
    if (userHitExperimentGroup == null) {
      log.info("The experiment result is null,return defaultValue.[distinctId:{},experimentVariableName:{}]",
          distinctId, experimentVariableName);
      return new Experiment<>(distinctId, isLoginId, defaultValue);
    }
    Variable variable = userHitExperimentGroup.getExperimentGroupConfig()
        .getVariableMap().get(experimentVariableName);

    T value = hitExperimentValue(variable, experimentVariableName, defaultValue);
    if (value != null) {
      return Experiment.<T>builder()
          .distinctId(distinctId)
          .isLoginId(isLoginId)
          .abTestExperimentId(userHitExperimentGroup.getExperimentGroupConfig().getAbtestExperimentId())
          .abTestExperimentGroupId(userHitExperimentGroup.getExperimentGroupConfig().getAbtestExperimentGroupId())
          .isControlGroup(userHitExperimentGroup.getExperimentGroupConfig().isControlGroup())
          .isWhiteList(userHitExperimentGroup.isWhiteList())
          .abtestExperimentResultId(userHitExperimentGroup.getExperimentGroupConfig().getAbtestExperimentResultId())
          .abtestExperimentVersion(userHitExperimentGroup.getExperimentGroupConfig().getAbtestExperimentVersion())
          .result(value)
          .build();

    }
    return new Experiment<>(distinctId, isLoginId, defaultValue);
  }

  /**
   * 组合请求参数，然后进行网络请求
   *
   * @return 网络请求成功, 并且返回对象状态为 SUCCESS 和 results 有值，则返回 JsonNode；否则返回 null
   */
  private JsonNode getABTestByHttp(String distinctId, boolean isLoginId, String experimentName,
      int timeoutMilliseconds,
      Map<String, Object> customProperties, Map<String, String> customIds) {
    Map<String, Object> params = Maps.newHashMap();
    if (isLoginId) {
      params.put("login_id", distinctId);
    } else {
      params.put(SensorsABTestConst.ANONYMOUS_ID, distinctId);
    }
    params.put(SensorsABTestConst.PLATFORM, SensorsABTestConst.JAVA);
    params.put(SensorsABTestConst.VERSION_KEY, SensorsABTestConst.VERSION);
    params.put("properties", Collections.emptyMap());
    if (!customIds.isEmpty()) {
      params.put("custom_ids", customIds);
    }
    try {
      Map<String, Object> objMap = ABTestUtil.customPropertiesHandler(customProperties);
      if (!objMap.isEmpty()) {
        params.put("custom_properties", objMap);
        params.put("param_name", experimentName);
      }
      String strJson = objectMapper.writeValueAsString(params);
      String result = httpConsumer.consume(strJson, timeoutMilliseconds);
      log.debug("Successfully get the httpConsumer result.[strJson:{},result:{}]", strJson, result);
      JsonNode res = objectMapper.readTree(result);
      if (res != null && SensorsABTestConst.SUCCESS.equals(res.findValue(SensorsABTestConst.STATUS_KEY).asText())) {
        return res;
      }
      return null;
    } catch (InvalidArgumentException e) {
      log.error("Invalid custom properties,{},[distinctId:{},isLoginId:{},experimentName:{}]",
          e.getMessage(), distinctId, isLoginId, experimentName);
      return null;
    } catch (IOException e) {
      log.error("Failed to network request.[distinctId:{},isLoginId:{},experimentName:{}]",
          distinctId, isLoginId, experimentName, e);
      return null;
    }
  }



  /**
   * 判断是否命中试验变量值（缓存状态）
   *
   * @param variable               返回试验变量
   * @param experimentVariableName 试验名
   * @param defaultValue           默认值
   * @param <T>
   * @return 默认值与返回值类型匹配，则返回结果；默认值与返回值类型不匹配则返回null
   */
  private <T> T hitExperimentValue(Variable variable, String experimentVariableName, T defaultValue) {
    if (variable == null || variable.getName() == null || !variable.getName().equals(experimentVariableName)) {
      return null;
    }
    return getExperimentValue(variable.getType(), defaultValue, variable.getValue());
  }

  private <T> T getExperimentValue(String type, T defaultValue, String value) {
    switch (type) {
      case "STRING":
        if (defaultValue instanceof String) {
          return (T) value;
        }
        break;
      case "INTEGER":
        if (defaultValue instanceof Integer) {
          return (T) Integer.valueOf(value);
        }
        break;
      case "JSON":
        if (defaultValue instanceof String && ((String) defaultValue).startsWith("{")
            && ((String) defaultValue).endsWith("}")) {
          return (T) value;
        }
        break;
      case "BOOLEAN":
        if (defaultValue instanceof Boolean) {
          return (T) Boolean.valueOf(value);
        }
        break;
      //未命中类型
      default:
        break;
    }
    return null;
  }

  public void shutdown() {
    if (httpConsumer != null) {
      try {
        httpConsumer.close();
      } catch (IOException e) {
        log.error("Close http consumer occurred error.", e);
      }
    }
  }

}
