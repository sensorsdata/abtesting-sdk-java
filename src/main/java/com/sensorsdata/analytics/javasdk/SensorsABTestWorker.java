package com.sensorsdata.analytics.javasdk;

import static com.sensorsdata.analytics.javasdk.SensorsABTestConst.OUT_LIST_KEY;
import static com.sensorsdata.analytics.javasdk.SensorsABTestConst.RESULTS_KEY;

import com.sensorsdata.analytics.javasdk.bean.ABGlobalConfig;
import com.sensorsdata.analytics.javasdk.bean.AllExperimentsResult;
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
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;

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

  AllExperimentsResult fetchAllExperiments(String distinctId, boolean isLoginId,
      FetchAllExperimentsParams fetchAllParams) {
    if (fetchAllParams == null) {
      throw new NullPointerException("fetchAllParams is marked non-null but is null");
    }

    checkFetchAllParams(distinctId, isLoginId, fetchAllParams);

    UserInfo userInfo = UserInfo.builder()
        .distinctId(distinctId)
        .isLoginId(isLoginId)
        .customIds(fetchAllParams.getCustomIds())
        .customProperties(fetchAllParams.getProperties())
        .build();

    String responseBody = getAllExperimentsResponseBody(userInfo, fetchAllParams.getTimeoutMilliseconds());
    return buildAllExperimentsResult(
        distinctId,
        isLoginId,
        userInfo,
        fetchAllParams.getCustomIds(),
        fetchAllParams.getEnableAutoTrackEvent(),
        responseBody,
        0L,
        false);
  }

  AllExperimentsResult loadAllExperiments(String distinctId, boolean isLoginId,
      LoadAllExperimentsParams loadAllParams, String dumpData) {
    if (loadAllParams == null) {
      throw new NullPointerException("loadAllParams is marked non-null but is null");
    }
    if (dumpData == null) {
      throw new NullPointerException("dumpData is marked non-null but is null");
    }

    JsonNode dumpNode = parseDumpNode(dumpData);
    String dumpDistinctId = requireTextField(dumpNode, "distinct_id");
    boolean dumpIsLoginId = requireBooleanField(dumpNode, "is_login_id");
    Map<String, String> dumpCustomIds = readCustomIds(dumpNode.get("custom_ids"));
    // response_body 允许为空字符串（dump 时没有原始响应体，例如网络请求失败或响应无 results 字段的空结果）。
    String responseBody = readOptionalTextField(dumpNode, "response_body");
    long timestamp = dumpNode.has("timestamp") ? dumpNode.get("timestamp").asLong() : 0L;

    if (!dumpDistinctId.equals(distinctId) || dumpIsLoginId != isLoginId) {
      throw new IllegalArgumentException("user identity (distinctId, isLoginId) mismatch");
    }
    if (!compareCustomIds(dumpCustomIds, loadAllParams.getCustomIds())) {
      throw new IllegalArgumentException("user identity (CustomIDs) mismatch");
    }

    UserInfo userInfo = UserInfo.builder()
        .distinctId(distinctId)
        .isLoginId(isLoginId)
        .customIds(loadAllParams.getCustomIds())
        .build();

    return buildAllExperimentsResult(
        distinctId,
        isLoginId,
        userInfo,
        loadAllParams.getCustomIds(),
        loadAllParams.getEnableAutoTrackEvent(),
        responseBody,
        timestamp,
        true);
  }

  private AllExperimentsResult buildAllExperimentsResult(String distinctId, boolean isLoginId,
      UserInfo userInfo, Map<String, String> customIds, boolean enableAutoTrackEvent, String responseBody,
      long timestamp, boolean strictResponseBody) {
    JsonNode response = parseSuccessfulResponse(responseBody, !strictResponseBody);
    updateTrackConfig(response);
    if (response == null) {
      return createEmptyAllExperimentsResult(distinctId, isLoginId, customIds, responseBody, timestamp);
    }

    JsonNode results = response.findValue(RESULTS_KEY);
    if (results == null) {
      return createEmptyAllExperimentsResult(distinctId, isLoginId, customIds, responseBody, timestamp);
    }

    Map<String, Experiment<?>> experiments = new HashMap<>();
    Map<String, TrackRecord> hitTrackRecordByParam = new HashMap<>();
    Map<String, List<TrackRecord>> outTrackRecordByParam = buildOutTrackRecordByParam(userInfo, response);
    UserHitExperiment userHitExperiment = experimentCacheManager.getUserHitExperimentWithoutUpdateCache(results);
    Iterator<JsonNode> resIterator = results.elements();
    while (resIterator.hasNext()) {
      JsonNode node = resIterator.next();
      String experimentId = getTextValueFromJsonNode(node, SensorsABTestConst.EXPERIMENT_ID_KEY);
      UserHitExperimentGroup userHitExperimentGroup =
          userHitExperiment == null ? null : userHitExperiment.getUserHitExperimentMap().get(experimentId);
      if (userHitExperimentGroup == null) {
        continue;
      }

      TrackRecord hitTrackRecord = TrackRecord.createTrackRecord(userInfo, userHitExperimentGroup);
      for (Map.Entry<String, Variable> variableEntry
          : userHitExperimentGroup.getExperimentGroupConfig().getVariableMap().entrySet()) {
        String paramName = variableEntry.getKey();
        Object result = castExperimentValue(variableEntry.getValue());
        if (result == null) {
          continue;
        }
        Experiment<Object> experiment =
            convertExperiment(userHitExperimentGroup, distinctId, isLoginId, result);
        if (!experiments.containsKey(paramName) || userHitExperimentGroup.isWhiteList()) {
          experiments.put(paramName, experiment);
          hitTrackRecordByParam.put(paramName, hitTrackRecord);
        }
      }
    }

    final Map<String, TrackRecord> finalHitTrackRecordByParam = hitTrackRecordByParam;
    final Map<String, List<TrackRecord>> finalOutTrackRecordByParam = outTrackRecordByParam;
    final String finalDistinctId = distinctId;
    final boolean finalIsLoginId = isLoginId;
    AllExperimentsResult.TrackCallback trackCallback = null;
    if (enableAutoTrackEvent) {
      trackCallback = new AllExperimentsResult.TrackCallback() {
        @Override
        public void track(String paramName) {
          List<TrackRecord> toTrack = new ArrayList<>();
          TrackRecord hitTrackRecord = finalHitTrackRecordByParam.get(paramName);
          if (hitTrackRecord != null) {
            toTrack.add(hitTrackRecord);
          }
          List<TrackRecord> outTrackRecord = finalOutTrackRecordByParam.get(paramName);
          if (outTrackRecord != null) {
            toTrack.addAll(outTrackRecord);
          }
          if (toTrack.isEmpty()) {
            return;
          }
          try {
            trackService.trackABTestTrigger(toTrack, null);
          } catch (InvalidArgumentException e) {
            log.error("Failed auto track fetchAll ABTest event.[distinctId:{},isLoginId:{},paramName:{}]",
                finalDistinctId, finalIsLoginId, paramName, e);
          }
        }
      };
    }

    return AllExperimentsResult.builder()
        .distinctId(distinctId)
        .isLoginId(isLoginId)
        .customIds(customIds)
        .experiments(experiments)
        .trackCallback(trackCallback)
        .responseBody(responseBody)
        .timestamp(timestamp > 0 ? timestamp : System.currentTimeMillis())
        .build();
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

  private void checkFetchAllParams(String distinctId, boolean isLoginId,
      FetchAllExperimentsParams fetchAllParams) {
    if (distinctId == null || distinctId.isEmpty()) {
      throw new IllegalArgumentException("distinctId is required but was null or empty");
    }
    Pair<Boolean, String> customIdCheckRes =
        ABTestUtil.assertCustomIds(distinctId, isLoginId, fetchAllParams.getCustomIds());
    if (customIdCheckRes.getKey()) {
      throw new IllegalArgumentException(customIdCheckRes.getValue());
    }
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
    String responseBody = getABTestByHttp(
        userInfo.getDistinctId(),
        userInfo.isLoginId(),
        param,
        timeoutMilliseconds,
        userInfo.getCustomProperties(),
        userInfo.getCustomIds());
    return parseSuccessfulResponse(responseBody, true);
  }

  private String getAllExperimentsResponseBody(UserInfo userInfo, Integer timeoutMilliseconds) {
    return getFetchAllByHttp(
        userInfo.getDistinctId(),
        userInfo.isLoginId(),
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

  private <T> Experiment<T> convertExperiment(UserHitExperimentGroup userHitExperimentGroup, String distinctId,
      Boolean isLoginId, T result) {
    return Experiment.<T>builder()
        .distinctId(distinctId)
        .isLoginId(isLoginId)
        .abTestExperimentId(userHitExperimentGroup.getExperimentGroupConfig().getAbtestExperimentId())
        .abTestExperimentGroupId(userHitExperimentGroup.getExperimentGroupConfig().getAbtestExperimentGroupId())
        .isControlGroup(userHitExperimentGroup.getExperimentGroupConfig().isControlGroup())
        .isWhiteList(userHitExperimentGroup.isWhiteList())
        .abtestExperimentResultId(userHitExperimentGroup.getExperimentGroupConfig().getAbtestExperimentResultId())
        .abtestExperimentVersion(userHitExperimentGroup.getExperimentGroupConfig().getAbtestExperimentVersion())
        .result(result)
        .build();
  }

  private Object castExperimentValue(Variable variable) {
    if (variable == null) {
      return null;
    }
    switch (variable.getType()) {
      case "STRING":
      case "JSON":
        return variable.getValue();
      case "INTEGER":
        try {
          return Integer.valueOf(variable.getValue());
        } catch (NumberFormatException e) {
          log.warn("invalid integer experiment value, variable: [{}]", variable, e);
          return null;
        }
      case "BOOLEAN":
        return Boolean.valueOf(variable.getValue());
      default:
        return null;
    }
  }

  private Map<String, List<TrackRecord>> buildOutTrackRecordByParam(UserInfo userInfo, JsonNode response) {
    JsonNode outResults = response.findValue(OUT_LIST_KEY);
    if (outResults == null) {
      return Collections.emptyMap();
    }

    Set<String> params = new HashSet<>();
    Iterator<JsonNode> iterator = outResults.elements();
    while (iterator.hasNext()) {
      JsonNode outResult = iterator.next();
      JsonNode variables = outResult.findValue(SensorsABTestConst.VARIABLES_KEY);
      if (variables == null) {
        continue;
      }
      Iterator<JsonNode> variableIterator = variables.elements();
      while (variableIterator.hasNext()) {
        JsonNode variable = variableIterator.next();
        String name = getTextValueFromJsonNode(variable, SensorsABTestConst.NAME_KEY);
        if (name != null) {
          params.add(name);
        }
      }
    }

    Map<String, List<TrackRecord>> outTrackRecordByParam = new HashMap<>();
    for (String param : params) {
      List<UserOutExperimentGroup> userOutExperimentGroups =
          experimentCacheManager.getUserOutExperimentGroups(param, outResults);
      if (userOutExperimentGroups.isEmpty()) {
        continue;
      }
      List<TrackRecord> outTrackRecords = new ArrayList<>();
      for (UserOutExperimentGroup userOutExperimentGroup : userOutExperimentGroups) {
        outTrackRecords.add(TrackRecord.createOutTrackRecord(userInfo, userOutExperimentGroup));
      }
      outTrackRecordByParam.put(param, outTrackRecords);
    }
    return outTrackRecordByParam;
  }

  private AllExperimentsResult createEmptyAllExperimentsResult(String distinctId, boolean isLoginId,
      Map<String, String> customIds, String responseBody, long timestamp) {
    return AllExperimentsResult.builder()
        .distinctId(distinctId)
        .isLoginId(isLoginId)
        .customIds(customIds)
        .experiments(new HashMap<String, Experiment<?>>())
        .responseBody(responseBody)
        .timestamp(timestamp > 0 ? timestamp : System.currentTimeMillis())
        .build();
  }

  private String getTextValueFromJsonNode(JsonNode node, String name) {
    JsonNode value = node.findValue(name);
    if (value != null) {
      return value.asText();
    }
    return null;
  }

  /**
   * 组合请求参数，然后进行网络请求
   *
   * @return 网络请求成功, 并且返回对象状态为 SUCCESS 和 results 有值，则返回 JsonNode；否则返回 null
   */
  private String getABTestByHttp(String distinctId, boolean isLoginId, String experimentName,
      int timeoutMilliseconds,
      Map<String, Object> customProperties, Map<String, String> customIds) {
    return requestByHttp(buildFetchExperimentRequestParams(
        distinctId,
        isLoginId,
        experimentName,
        customProperties,
        customIds), timeoutMilliseconds, experimentName);
  }

  private String getFetchAllByHttp(String distinctId, boolean isLoginId, int timeoutMilliseconds,
      Map<String, Object> customProperties, Map<String, String> customIds) {
    return requestByHttp(buildFetchAllRequestParams(
        distinctId,
        isLoginId,
        customProperties,
        customIds), timeoutMilliseconds, null);
  }

  private Map<String, Object> buildFetchExperimentRequestParams(String distinctId, boolean isLoginId,
      String experimentName, Map<String, Object> customProperties, Map<String, String> customIds) {
    Map<String, Object> params = buildBaseRequestParams(distinctId, isLoginId, customIds);
    try {
      Map<String, Object> objMap = ABTestUtil.customPropertiesHandler(customProperties);
      if (!objMap.isEmpty()) {
        params.put("custom_properties", objMap);
        if (experimentName != null) {
          params.put("param_name", experimentName);
        }
      }
      return params;
    } catch (InvalidArgumentException e) {
      log.error("Invalid custom properties,{},[distinctId:{},isLoginId:{},experimentName:{}]",
          e.getMessage(), distinctId, isLoginId, experimentName);
      return null;
    }
  }

  private Map<String, Object> buildFetchAllRequestParams(String distinctId, boolean isLoginId,
      Map<String, Object> customProperties, Map<String, String> customIds) {
    Map<String, Object> params = buildBaseRequestParams(distinctId, isLoginId, customIds);
    try {
      Map<String, Object> objMap = ABTestUtil.customPropertiesHandler(customProperties);
      if (!objMap.isEmpty()) {
        params.put("custom_properties", objMap);
      }
      return params;
    } catch (InvalidArgumentException e) {
      log.error("Invalid custom properties,{},[distinctId:{},isLoginId:{}]",
          e.getMessage(), distinctId, isLoginId);
      return null;
    }
  }

  private Map<String, Object> buildBaseRequestParams(String distinctId, boolean isLoginId,
      Map<String, String> customIds) {
    Map<String, Object> params = Maps.newHashMap();
    if (isLoginId) {
      params.put("login_id", distinctId);
    } else {
      params.put(SensorsABTestConst.ANONYMOUS_ID, distinctId);
    }
    params.put(SensorsABTestConst.PLATFORM, SensorsABTestConst.JAVA);
    params.put(SensorsABTestConst.VERSION_KEY, SensorsABTestConst.VERSION);
    params.put("properties", Collections.emptyMap());
    if (customIds != null && !customIds.isEmpty()) {
      params.put("custom_ids", customIds);
    }
    return params;
  }

  private String requestByHttp(Map<String, Object> params, int timeoutMilliseconds, String experimentName) {
    if (params == null) {
      return null;
    }
    try {
      String strJson = objectMapper.writeValueAsString(params);
      String result = httpConsumer.consume(strJson, timeoutMilliseconds);
      log.debug("Successfully get the httpConsumer result.[strJson:{},result:{}]", strJson, result);
      return result;
    } catch (IOException e) {
      log.error("Failed to network request.[experimentName:{}]", experimentName, e);
      return null;
    }
  }

  private JsonNode parseSuccessfulResponse(String responseBody, boolean ignoreError) {
    if (responseBody == null) {
      log.warn("Response body is null, return null.[ignoreError:{}]", ignoreError);
      return null;
    }
    if (responseBody.isEmpty()) {
      log.warn("Response body is empty, return null.[ignoreError:{}]", ignoreError);
      return null;
    }
    try {
      JsonNode res = objectMapper.readTree(responseBody);
      JsonNode statusNode = res == null ? null : res.findValue(SensorsABTestConst.STATUS_KEY);
      if (statusNode != null && statusNode.isTextual()
          && SensorsABTestConst.SUCCESS.equals(statusNode.asText())) {
        return res;
      }
      log.warn("Response body status is invalid, return null or throw later.[ignoreError:{},statusNode:{}]",
          ignoreError, statusNode);
      if (ignoreError) {
        return null;
      }
      log.error("Response body status is not SUCCESS in strict mode.");
      throw new IllegalArgumentException("invalid response_body: status is not SUCCESS");
    } catch (IOException e) {
      if (ignoreError) {
        log.error("Failed to parse response body.", e);
        return null;
      }
      log.error("Failed to parse response body in strict mode.", e);
      throw new IllegalArgumentException("invalid response_body: failed to parse json", e);
    }
  }

  private JsonNode parseDumpNode(String dumpData) {
    try {
      return objectMapper.readTree(dumpData);
    } catch (IOException e) {
      throw new IllegalArgumentException("invalid dump data: failed to parse json", e);
    }
  }

  private String requireTextField(JsonNode node, String fieldName) {
    JsonNode fieldNode = node.get(fieldName);
    if (fieldNode == null || !fieldNode.isTextual() || fieldNode.asText().isEmpty()) {
      throw new IllegalArgumentException("invalid dump data: missing " + fieldName + " field");
    }
    return fieldNode.asText();
  }

  /**
   * 读取允许为空字符串的文本字段；字段缺失或类型不正确时抛异常，空字符串视为合法。
   */
  private String readOptionalTextField(JsonNode node, String fieldName) {
    JsonNode fieldNode = node.get(fieldName);
    if (fieldNode == null || fieldNode.isNull()) {
      throw new IllegalArgumentException("invalid dump data: missing " + fieldName + " field");
    }
    if (!fieldNode.isTextual()) {
      throw new IllegalArgumentException("invalid dump data: " + fieldName + " must be string");
    }
    return fieldNode.asText();
  }

  private boolean requireBooleanField(JsonNode node, String fieldName) {
    JsonNode fieldNode = node.get(fieldName);
    if (fieldNode == null || !fieldNode.isBoolean()) {
      throw new IllegalArgumentException("invalid dump data: missing " + fieldName + " field");
    }
    return fieldNode.asBoolean();
  }

  private Map<String, String> readCustomIds(JsonNode customIdsNode) {
    Map<String, String> customIds = new HashMap<>();
    if (customIdsNode == null || customIdsNode.isNull()) {
      return customIds;
    }
    if (!customIdsNode.isObject()) {
      throw new IllegalArgumentException("invalid dump data: custom_ids must be object");
    }
    Iterator<Map.Entry<String, JsonNode>> fields = customIdsNode.fields();
    while (fields.hasNext()) {
      Map.Entry<String, JsonNode> field = fields.next();
      customIds.put(field.getKey(), field.getValue().asText());
    }
    return customIds;
  }

  private boolean compareCustomIds(Map<String, String> expected, Map<String, String> actual) {
    Map<String, String> expectedMap = expected == null ? Collections.<String, String>emptyMap() : expected;
    Map<String, String> actualMap = actual == null ? Collections.<String, String>emptyMap() : actual;
    return expectedMap.equals(actualMap);
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
