package com.sensorsdata.analytics.javasdk;

import com.sensorsdata.analytics.javasdk.bean.ABGlobalConfig;
import com.sensorsdata.analytics.javasdk.bean.Experiment;
import com.sensorsdata.analytics.javasdk.cache.EventCacheManager;
import com.sensorsdata.analytics.javasdk.cache.ExperimentCacheManager;
import com.sensorsdata.analytics.javasdk.exception.HttpStatusException;
import com.sensorsdata.analytics.javasdk.exceptions.InvalidArgumentException;
import com.sensorsdata.analytics.javasdk.util.ABTestUtil;
import com.sensorsdata.analytics.javasdk.util.HttpUtil;
import com.sensorsdata.analytics.javasdk.util.SensorsAnalyticsUtil;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;

import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

/**
 * AB Test 逻辑处理
 *
 * @author fz <fangzhuo@sensorsdata.cn>
 * @version 1.0.0
 * @since 2021/06/16 15:12
 */
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
  private String trigger;

  SensorsABTestWorker(ABGlobalConfig config) {
    this.config = config;
    this.objectMapper = SensorsAnalyticsUtil.getJsonObjectMapper();
    this.experimentCacheManager =
        ExperimentCacheManager.getInstance(config.getExperimentCacheTime(), config.getExperimentCacheSize());
    this.eventCacheManager =
        EventCacheManager.getInstance(config.getEventCacheTime(), config.getEventCacheSize());
  }

  /**
   * 处理AB Test 结果
   *
   * @param distinctId             匿名ID/业务ID
   * @param isLoginId              是否为登录ID
   * @param experimentVariableName 试验名称
   * @param defaultValue           默认值
   * @param enableAutoTrackEvent   是否开启自动上报事件
   * @param enableCache            是否开启优先查询缓存
   * @param timeoutMilliseconds    接口超时设置
   * @param properties             接口请求参数
   * @return Experiment<T>
   */
  <T> Experiment<T> fetchABTest(String distinctId, boolean isLoginId, String experimentVariableName, T defaultValue,
      boolean enableAutoTrackEvent, int timeoutMilliseconds, Map<String, Object> properties, boolean enableCache) {
    if (distinctId == null || distinctId.isEmpty()) {
      ABTestUtil.printLog(config.getEnableLog(), "info message: distinctId is empty or null,return defaultValue");
      return new Experiment<>(distinctId, isLoginId, defaultValue);
    }
    if (experimentVariableName == null || experimentVariableName.isEmpty()) {
      ABTestUtil.printLog(config.getEnableLog(), "info message: experimentVariableName is empty or null,return defaultValue");
      return new Experiment<>(distinctId, isLoginId, defaultValue);
    }
    if (!ABTestUtil.assertDefaultValueType(defaultValue)) {
      ABTestUtil.printLog(config.getEnableLog(), "info message: the type of defaultValue is not Integer,String,Boolean or Json of String,return defaultValue");
      return new Experiment<>(distinctId, isLoginId, defaultValue);
    }
    JsonNode experiment;
    //判断是否需要读取结果缓存
    if (enableCache) {
      experiment = experimentCacheManager.getExperimentResultByCache(distinctId, isLoginId, experimentVariableName);
      //未命中缓存
      if (experiment == null) {
        experiment = getABTestByHttp(distinctId, isLoginId, timeoutMilliseconds, properties);
        experimentCacheManager.setExperimentResultCache(distinctId, isLoginId, experiment);
      }
    } else {
      experiment = getABTestByHttp(distinctId, isLoginId, timeoutMilliseconds, properties);
    }
    Experiment<T> result = convertExperiment(experiment, distinctId, isLoginId, experimentVariableName, defaultValue);
    //判断是否需要自动触发上报事件
    if (enableAutoTrackEvent) {
      try {
        this.trackABTestTrigger(result, null);
      } catch (InvalidArgumentException e) {
        ABTestUtil.printLog(config.getEnableLog(), String.format("error message: auto track ABTest event %s", e.getMessage()));
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
  <T> void trackABTestTrigger(Experiment<T> result, Map<String, Object> properties) throws InvalidArgumentException {
    if (result == null) {
      ABTestUtil.printLog(config.getEnableLog(), "info message: track ABTest event Experiment is null");
      return;
    }
    if (result.getWhiteList() == null || result.getWhiteList() || result.getAbTestExperimentId() == null) {
      ABTestUtil.printLog(config.getEnableLog(), "info message: track ABTest event user not hit experiment or in the whiteList");
      return;
    }
    //缓存中存在 A/B 事件
    if (eventCacheManager.judgeEventCacheExist(result.getDistinctId(), result.getAbTestExperimentId())) {
      ABTestUtil.printLog(config.getEnableLog(), "info message: track ABTest event user trigger event have been cached");
      return;
    }
    if (properties == null) {
      properties = new HashMap<>(16);
    }
    properties.put(SensorsABTestConst.EXPERIMENT_ID, result.getAbTestExperimentId());
    properties.put(SensorsABTestConst.EXPERIMENT_GROUP_ID, result.getAbTestExperimentGroupId());
    //判断是否为当天首次上传，重启服务，升级 JDK 版本都会触发
    String day = this.formatDate(Calendar.getInstance().getTime());
    if (trigger == null || trigger.isEmpty() || !day.equals(trigger)) {
      trigger = day;
      List<String> versions = new ArrayList<>();
      versions.add(String.format("%s:%s", SensorsABTestConst.AB_TEST_EVENT_LIB_VERSION, SensorsABTestConst.VERSION));
      properties.put(SensorsABTestConst.LIB_PLUGIN_VERSION, versions);
    }
    this.config.getSensorsAnalytics().track(result.getDistinctId(), result.getIsLoginId(),
        SensorsABTestConst.EVENT_TYPE, properties);
    //判断是否需要缓存上报事件
    if (config.getEnableEventCache() == null || config.getEnableEventCache()) {
      eventCacheManager.setEventCache(result.getDistinctId(), result.getAbTestExperimentId());
    }
  }

  /**
   * 组合请求参数，然后进行网络请求
   *
   * @return 网络请求成功, 并且返回对象状态为 SUCCESS 和 results 有值，则返回 JsonNode；否则返回 null
   */
  private JsonNode getABTestByHttp(String distinctId, boolean isLoginId, int timeoutMilliseconds,
      Map<String, Object> properties) {
    Map<String, Object> params = new HashMap<>(16);
    if (isLoginId) {
      params.put("login_id", distinctId);
    } else {
      params.put("anonymous_id", distinctId);
    }
    params.put(SensorsABTestConst.PLATFORM, SensorsABTestConst.JAVA);
    params.put(SensorsABTestConst.VERSION_KEY, SensorsABTestConst.VERSION);
    if (properties == null) {
      properties = new HashMap<>(16);
    }
    params.put("properties", properties);
    try {
      String strJson = objectMapper.writeValueAsString(params);
      String result = HttpUtil.postABTest(config.getApiUrl(), strJson, timeoutMilliseconds);
      JsonNode res = objectMapper.readTree(result);
      if (res != null && SensorsABTestConst.SUCCESS.equals(res.findValue(SensorsABTestConst.STATUS_KEY).asText())
          && res.findValue(SensorsABTestConst.RESULTS_KEY).size() > 0) {
        return res;
      }
      return null;
    } catch (HttpStatusException | IOException e) {
      ABTestUtil.printLog(config.getEnableLog(), String.format("error message: %s", e.getMessage()));
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
      ABTestUtil.printLog(config.getEnableLog(),"info message: request result was not obtained,return defaultValue");
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
    ABTestUtil.printLog(config.getEnableLog(),"info message: missing experiment,return defaultValue");
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

  private String formatDate(Date date) {
    return new SimpleDateFormat("yyyy-MM-dd").format(date);
  }

}
