package com.sensorsdata.analytics.javasdk;

import com.sensorsdata.analytics.javasdk.bean.ABGlobalConfig;
import com.sensorsdata.analytics.javasdk.bean.Experiment;
import com.sensorsdata.analytics.javasdk.cache.EventCacheManager;
import com.sensorsdata.analytics.javasdk.cache.ExperimentCacheManager;
import com.sensorsdata.analytics.javasdk.exceptions.InvalidArgumentException;
import com.sensorsdata.analytics.javasdk.util.ABTestUtil;
import com.sensorsdata.analytics.javasdk.util.HttpConsumer;
import com.sensorsdata.analytics.javasdk.util.SensorsAnalyticsUtil;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.google.common.collect.Lists;
import com.google.common.collect.Maps;
import lombok.NonNull;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.time.DateFormatUtils;

import java.io.IOException;
import java.util.Calendar;
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
@Slf4j
class SensorsABTestWorker {

  private final ObjectMapper objectMapper;
  /**
   * 全局配置信息
   */
  private final ABGlobalConfig config;
  /**
   * 试验结果缓存管理器
   */
  private final ExperimentCacheManager experimentCacheManager;
  /**
   * 试验上报事件缓存
   */
  private final EventCacheManager eventCacheManager;
  /**
   * 是否当天首次触发
   */
  private volatile String trigger;
  /**
   * 网络请求对象
   */
  private final HttpConsumer httpConsumer;

  SensorsABTestWorker(ABGlobalConfig config) {
    this.config = config;
    this.objectMapper = SensorsAnalyticsUtil.getJsonObjectMapper();
    this.experimentCacheManager = new ExperimentCacheManager(
        config.getExperimentCacheTime(),
        config.getExperimentCacheSize());
    this.eventCacheManager = new EventCacheManager(
        config.getEventCacheTime(),
        config.getEventCacheSize());
    this.httpConsumer = new HttpConsumer(
        config.getApiUrl(),
        config.getMaxTotal(),
        config.getMaxPerRoute());
    log.info("init SensorsABTest with config info:{}.", config);
  }

  /**
   * 处理AB Test 结果
   *
   * @param <T> params 请求参数类
   * @return Experiment<T> 返回试验结果
   */
  <T> Experiment<T> fetchABTest(@NonNull SensorsABParams<T> sensorsParams) {
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
    if (ABTestUtil.assertCustomIds(sensorsParams)) {
      return new Experiment<>(
          sensorsParams.getDistinctId(),
          sensorsParams.getIsLoginId(),
          sensorsParams.getDefaultValue());
    }
    JsonNode experiment;
    //判断是否需要读取结果缓存
    if (sensorsParams.getEnableCache()) {
      log.debug("Enable priority read experiment of cache.[distinctId:{};experimentVariableName:{}]",
          sensorsParams.getDistinctId(),
          sensorsParams.getExperimentVariableName());
      experiment = experimentCacheManager.getExperimentResultByCache(
          sensorsParams.getDistinctId(),
          sensorsParams.getIsLoginId(),
          sensorsParams.getCustomIds(),
          sensorsParams.getExperimentVariableName());
      //未命中缓存
      if (experiment == null) {
        log.debug("Not hit experiment cache,making network request.[distinctId:{};experimentVariableName:{}]",
            sensorsParams.getDistinctId(),
            sensorsParams.getExperimentVariableName());
        experiment = getABTestByHttp(
            sensorsParams.getDistinctId(),
            sensorsParams.getIsLoginId(),
            sensorsParams.getExperimentVariableName(),
            sensorsParams.getTimeoutMilliseconds(),
            sensorsParams.getProperties(),
            sensorsParams.getCustomIds());
        if (experiment != null) {
          log.debug("Hit experiment from server,cache the experiment results.[distinctId:{};experimentVariableName:{}]",
              sensorsParams.getDistinctId(),
              sensorsParams.getExperimentVariableName());
          experimentCacheManager.setExperimentResultCache(
              sensorsParams.getDistinctId(),
              sensorsParams.getIsLoginId(),
              sensorsParams.getCustomIds(), experiment);
        }
      }
    } else {
      log.debug("Get results from server.[distinctId:{};experimentVariableName:{}]",
          sensorsParams.getDistinctId(),
          sensorsParams.getExperimentVariableName());
      experiment = getABTestByHttp(
          sensorsParams.getDistinctId(),
          sensorsParams.getIsLoginId(),
          sensorsParams.getExperimentVariableName(),
          sensorsParams.getTimeoutMilliseconds(),
          sensorsParams.getProperties(),
          sensorsParams.getCustomIds());
    }
    Experiment<T> result = convertExperiment(experiment,
        sensorsParams.getDistinctId(),
        sensorsParams.getIsLoginId(),
        sensorsParams.getExperimentVariableName(),
        sensorsParams.getDefaultValue());
    //判断是否需要自动触发上报事件
    if (sensorsParams.getEnableAutoTrackEvent()) {
      try {
        this.trackABTestTrigger(result, null, sensorsParams.getCustomIds());
      } catch (InvalidArgumentException e) {
        log.error("Failed auto track ABTest event.[distinctId:{},isLoginId:{},experimentVariableName:{}]",
            sensorsParams.getDistinctId(),
            sensorsParams.getIsLoginId(),
            sensorsParams.getExperimentVariableName(), e);
      }
    }
    return result;
  }

  /**
   * 触发 $ABTestTrigger 事件上报
   *
   * @param result     试验结果
   * @param properties 请求参数
   * @throws InvalidArgumentException 上报事件的时候对上报事件参数进行校验，如果参数不合法，则抛出异常 InvalidArgumentException
   */
  <T> void trackABTestTrigger(Experiment<T> result, Map<String, Object> properties, Map<String, String> customIds)
      throws InvalidArgumentException {
    if (result == null) {
      log.info("The track ABTest event experiment result is null.");
      return;
    }
    if (result.getIsWhiteList() == null || result.getIsWhiteList() || result.getAbTestExperimentId() == null) {
      log.info("The track ABTest event user not hit experiment or in the whiteList.[distinctId:{}]",
          result.getDistinctId());
      return;
    }
    //缓存中存在 A/B 事件
    if (eventCacheManager.judgeEventCacheExist(result.getDistinctId(), result.getIsLoginId(),
        result.getAbTestExperimentId(), customIds)) {
      log.info("The event has been triggered.[distinctId:{},experimentId:{}]",
          result.getDistinctId(), result.getAbTestExperimentId());
      return;
    }
    if (properties == null) {
      properties = Maps.newHashMap();
    }
    properties.put(SensorsABTestConst.EXPERIMENT_ID, result.getAbTestExperimentId());
    properties.put(SensorsABTestConst.EXPERIMENT_GROUP_ID, result.getAbTestExperimentGroupId());
    //判断是否为当天首次上传，重启服务，升级 SDK 版本都会触发
    String day = DateFormatUtils.ISO_8601_EXTENDED_DATE_FORMAT.format(Calendar.getInstance());
    if (trigger == null || trigger.isEmpty() || !day.equals(trigger)) {
      trigger = day;
      List<String> versions = Lists.newArrayList();
      versions.add(String.format("%s:%s", SensorsABTestConst.AB_TEST_EVENT_LIB_VERSION, SensorsABTestConst.VERSION));
      properties.put(SensorsABTestConst.LIB_PLUGIN_VERSION, versions);
      log.debug(
          "Meet the conditions:the first event of current day,the first events of server.[distinctId:{},isLoginId:{},experimentId:{}]",
          result.getDistinctId(), result.getIsLoginId(), result.getAbTestExperimentId());
    }
    this.config.getSensorsAnalytics().track(result.getDistinctId(), result.getIsLoginId(),
        SensorsABTestConst.EVENT_TYPE, properties);
    log.debug("Successfully trigger AB event.[distinctId:{},isLoginId:{},experimentId:{}]",
        result.getDistinctId(), result.getIsLoginId(), result.getAbTestExperimentId());
    //判断是否需要缓存上报事件
    if (config.getEnableEventCache() == null || config.getEnableEventCache()) {
      log.debug("Enable event cache,will cache event.[distinctId:{},isLoginId:{},experimentId:{}]",
          result.getDistinctId(), result.getIsLoginId(), result.getAbTestExperimentId());
      eventCacheManager.setEventCache(
          result.getDistinctId(),
          result.getIsLoginId(),
          result.getAbTestExperimentId(),
          customIds);
    }
  }

  /**
   * 组合请求参数，然后进行网络请求
   *
   * @return 网络请求成功, 并且返回对象状态为 SUCCESS 和 results 有值，则返回 JsonNode；否则返回 null
   */
  private JsonNode getABTestByHttp(String distinctId, boolean isLoginId, String experimentName, int timeoutMilliseconds,
      Map<String, Object> customProperties, Map<String, String> customIds) {
    Map<String, Object> params = Maps.newHashMap();
    if (isLoginId) {
      params.put("login_id", distinctId);
    } else {
      params.put("anonymous_id", distinctId);
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
      log.debug("The parameter for making a network request is {}.", strJson);
      String result = httpConsumer.consume(strJson, timeoutMilliseconds);
      JsonNode res = objectMapper.readTree(result);
      if (res != null && SensorsABTestConst.SUCCESS.equals(res.findValue(SensorsABTestConst.STATUS_KEY).asText())
          && res.findValue(SensorsABTestConst.RESULTS_KEY).size() > 0) {
        return res;
      }
      log.error("The server return incorrect information.[errorMessage:{},distinctId:{},experimentName:{}]",
          result, distinctId, experimentName);
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
   * 将请求返回结果解析成标准返回对象
   *
   * @return ExperimentResult<T>
   */
  private <T> Experiment<T> convertExperiment(JsonNode message, String distinctId, boolean isLoginId,
      String experimentVariableName, T defaultValue) {
    if (message == null) {
      log.info("The experiment result is null,return defaultValue.[distinctId:{},experimentVariableName:{}]",
          distinctId, experimentVariableName);
      return new Experiment<>(distinctId, isLoginId, defaultValue);
    }
    JsonNode results = message.findValue(SensorsABTestConst.RESULTS_KEY);
    Iterator<JsonNode> iterator = results.elements();
    while (iterator.hasNext()) {
      JsonNode experiment = iterator.next();
      Iterator<JsonNode> varIterator = experiment.findValue(SensorsABTestConst.VARIABLES_KEY).elements();
      while (varIterator.hasNext()) {
        JsonNode variable = varIterator.next();
        T value = hitExperimentValue(variable, experimentVariableName, defaultValue);
        if (value != null) {
          return new Experiment<>(distinctId, isLoginId,
              experiment.findValue(SensorsABTestConst.EXPERIMENT_ID_KEY).asText(),
              experiment.findValue(SensorsABTestConst.EXPERIMENT_GROUP_ID_KEY).asText(),
              experiment.findValue(SensorsABTestConst.IS_CONTROL_GROUP_KEY).asBoolean(),
              experiment.findValue(SensorsABTestConst.IS_WHITE_LIST_KEY).asBoolean(), value);
        }
      }
    }
    try {
      log.info(
          "Missing experiment,return defaultValue.[experimentResult:{},distinctId:{},isLoginId:{},experimentVariableName:{},defaultValue:{}]",
          objectMapper.writeValueAsString(message), distinctId, isLoginId, experimentVariableName, defaultValue);
    } catch (JsonProcessingException e) {
      log.error("print log occurred error.", e);
    }
    return new Experiment<>(distinctId, isLoginId, defaultValue);
  }

  /**
   * 判断是否命中试验变量值
   *
   * @param variable               返回试验变量
   * @param experimentVariableName 试验名
   * @param defaultValue           默认值
   * @return 默认值与返回值类型匹配，则返回结果；默认值与返回值类型不匹配则返回null
   */
  @SuppressWarnings("unchecked")
  private <T> T hitExperimentValue(JsonNode variable, String experimentVariableName, T defaultValue) {
    if (!experimentVariableName.equals(variable.findValue("name").asText())) {
      return null;
    }
    JsonNode value = variable.findValue("value");
    if (value == null) {
      return null;
    }
    switch (variable.findValue("type").asText()) {
      case "STRING":
        if (defaultValue instanceof String) {
          return (T) value.asText();
        }
        break;
      case "INTEGER":
        if (defaultValue instanceof Integer) {
          return (T) Integer.valueOf(value.asInt());
        }
        break;
      case "JSON":
        if (defaultValue instanceof String && ((String) defaultValue).startsWith("{")
            && ((String) defaultValue).endsWith("}")) {
          return (T) value.asText();
        }
        break;
      case "BOOLEAN":
        if (defaultValue instanceof Boolean) {
          return (T) Boolean.valueOf(value.asBoolean());
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
