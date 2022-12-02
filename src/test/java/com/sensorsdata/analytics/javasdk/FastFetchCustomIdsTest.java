package com.sensorsdata.analytics.javasdk;

import com.fasterxml.jackson.databind.JsonNode;
import com.google.common.collect.Maps;

import com.sensorsdata.analytics.javasdk.bean.ABGlobalConfig;
import com.sensorsdata.analytics.javasdk.bean.Experiment;
import com.sensorsdata.analytics.javasdk.bean.cache.ExperimentGroupConfig;
import com.sensorsdata.analytics.javasdk.bean.cache.UserHitExperimentGroup;
import com.sensorsdata.analytics.javasdk.bean.cache.UserHitExperiment;
import com.sensorsdata.analytics.javasdk.exceptions.InvalidArgumentException;
import com.sensorsdata.analytics.javasdk.util.SensorsAnalyticsUtil;

import org.junit.Before;
import org.junit.Ignore;
import org.junit.Test;

import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

import static org.junit.Assert.*;

/**
 * 校验 fastFetchABTest 接口
 *
 * @author fangzhuo
 * @version 1.0.0
 * @since 2022/01/13 10:54
 */
public class FastFetchCustomIdsTest extends SensorsBaseTest {

  @Before
  public void init() {
    initSASDK();
  }

  /**
   * fastFetchABTest 携带单个 customIds
   * 期望：命中试验，返回结果字段均有值
   */
  @Test
  public void checkFastFetchWithCustomIds() throws InvalidArgumentException {
    //初始化 AB Testing SDK
    initInstance(ABGlobalConfig.builder().setApiUrl(url).setSensorsAnalytics(sa).build());
    Experiment<String> experiment =
        sensorsABTest.fastFetchABTest(
            SensorsABParams.starter("a123", true, "str_experiment", "qwe")
                .addCustomId("custom_id", "test11")
                .build());
    assertNotNull(experiment);
    assertEquals("a123", experiment.getDistinctId());
    assertTrue(experiment.getIsLoginId());
    assertEquals("test", experiment.getResult());
    assertEquals("2", experiment.getAbTestExperimentId());
    assertEquals("1", experiment.getAbTestExperimentGroupId());
  }

  /**
   * fastFetchABTest 携带多个 customIds
   * 期望：命中试验，返回结果字段均有值
   */
  @Test
  public void checkFastFetchWithMultiCustomIds() throws InvalidArgumentException {
    //初始化 AB Testing SDK
    initInstance(ABGlobalConfig.builder().setApiUrl(url).setSensorsAnalytics(sa).build());
    Experiment<String> experiment =
        sensorsABTest.fastFetchABTest(
            SensorsABParams.starter("a123", true, "str_experiment", "qwe")
                .addCustomId("custom_id1", "test11")
                .addCustomId("custom_id2", "test22")
                .build());
    assertNotNull(experiment);
    assertEquals("a123", experiment.getDistinctId());
    assertTrue(experiment.getIsLoginId());
    assertEquals("test", experiment.getResult());
    assertEquals("2", experiment.getAbTestExperimentId());
    assertEquals("1", experiment.getAbTestExperimentGroupId());
  }

  /**
   * fastFetchABTest 请求，customIds key 为保留字段
   * 期望：SDK 拦截请求，直接返回默认值
   */
  @Test
  public void checkFastFetchWithInvalidCustomIds01() throws InvalidArgumentException {
    String[] customInvaild =
        {"date", "datetime", "distinct_id", "event", "events", "first_id", "id", "original_id", "properties",
            "second_id", "time", "user_id", "users"};
    //初始化 AB Testing SDK
    initInstance(ABGlobalConfig.builder().setApiUrl(url).setSensorsAnalytics(sa).build());
    for (String s : customInvaild) {
      Experiment<String> experiment =
          sensorsABTest.fastFetchABTest(SensorsABParams.starter("a123", true, "str_experiment", "qwe")
              .addCustomId(s, "eee")
              .build());
      assertNotNull(experiment);
      assertEquals("a123", experiment.getDistinctId());
      assertTrue(experiment.getIsLoginId());
      assertEquals("qwe", experiment.getResult());
      assertNull(experiment.getAbTestExperimentId());
      assertNull(experiment.getAbTestExperimentGroupId());
    }
  }

  /**
   * fastFetchABTest 请求，customIds key 为非法字段
   * 期望：SDK 拦截请求，直接返回默认值
   */
  @Test
  public void checkFastFetchWithInvalidCustomIds02() throws InvalidArgumentException {
    String[] customInvaild = {null, // key 为 null
        "",                     // key 为空字符串
        "1122aaaa",             // key 开头是数字
        "$1122aa",              // key 包含字符 $
        "……%%",                 // key 包含特殊字符
        "aaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaa",
        // key 为100个字符
        "aaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaa1"}; // key 超过 100个字符
    //初始化 AB Testing SDK
    initInstance(ABGlobalConfig.builder().setApiUrl(url).setSensorsAnalytics(sa).build());
    for (String s : customInvaild) {
      Experiment<String> experiment =
          sensorsABTest.fastFetchABTest(SensorsABParams.starter("a123", true, "str_experiment", "qwe")
              .addCustomId(s, "eee")
              .build());
      assertNotNull(experiment);
      assertEquals("a123", experiment.getDistinctId());
      assertTrue(experiment.getIsLoginId());
      assertEquals("qwe", experiment.getResult());
      assertNull(experiment.getAbTestExperimentId());
      assertNull(experiment.getAbTestExperimentGroupId());
    }
  }


  /**
   * fastFetchABTest 请求，customIds value值不合法
   * 期望：SDK 拦截请求，直接返回默认值
   */
  @Test
  public void checkFastFetchWithInvalidCustomIds03() throws InvalidArgumentException {
    StringBuilder strInvalid = new StringBuilder();
    for (int i = 0; i < 1024; i++) {
      strInvalid.append(i);
    }
    String[] valueInvalid = {null, // value 为 null
        "",                    // value 为空字符串
        strInvalid.toString(),            // value 长度为 1024
        // value 长度大于 1024
        "a12345678901234567890123456789012345678901234567890123456789012345678901234567890123456789012345678901234567890"
            + "a12345678901234567890123456789012345678901234567890123456789012345678901234567890123456789012345678901234567890"
            + "a12345678901234567890123456789012345678901234567890123456789012345678901234567890123456789012345678901234567890"
            + "a12345678901234567890123456789012345678901234567890123456789012345678901234567890123456789012345678901234567890"
            + "a12345678901234567890123456789012345678901234567890123456789012345678901234567890123456789012345678901234567890"
            + "a12345678901234567890123456789012345678901234567890123456789012345678901234567890123456789012345678901234567890"
            + "a12345678901234567890123456789012345678901234567890123456789012345678901234567890123456789012345678901234567890"
            + "a12345678901234567890123456789012345678901234567890123456789012345678901234567890123456789012345678901234567890"
            + "a12345678901234567890123456789012345678901234567890123456789012345678901234567890123456789012345678901234567890"
            + "a12345678901234567890123456789012345678901234567890123456789012345678901234567890123456789012345678901234567890"
            + "a12345678901234567890123456789012345678901234567890123456789012345678901234567890123456789012345678901234567890"};
    //初始化 AB Testing SDK
    initInstance(ABGlobalConfig.builder().setApiUrl(url).setSensorsAnalytics(sa).build());
    for (String s : valueInvalid) {
      Experiment<String> experiment =
          sensorsABTest.fastFetchABTest(SensorsABParams.starter("a123", true, "str_experiment", "qwe")
              .addCustomId("wee", s)
              .build());
      assertNotNull(experiment);
      assertEquals("a123", experiment.getDistinctId());
      assertTrue(experiment.getIsLoginId());
      assertEquals("qwe", experiment.getResult());
      assertNull(experiment.getAbTestExperimentId());
      assertNull(experiment.getAbTestExperimentGroupId());
    }
  }

  /**
   * fastFetchABTest 请求，customIds 为 null
   * 期望：SDK 拦截请求，直接返回默认值
   */
  @Test
  public void checkFastFetchWithInvalidCustomIds04() throws InvalidArgumentException {
    //初始化 AB Testing SDK
    initInstance(ABGlobalConfig.builder().setApiUrl(url).setSensorsAnalytics(sa).build());
    Experiment<String> experiment =
        sensorsABTest.fastFetchABTest(SensorsABParams.starter("a123", true, "str_experiment", "qwe")
            .customIds(null)
            .build());
    assertNotNull(experiment);
    assertEquals("a123", experiment.getDistinctId());
    assertTrue(experiment.getIsLoginId());
    assertEquals("test", experiment.getResult());
    assertEquals("2", experiment.getAbTestExperimentId());
    assertEquals("1", experiment.getAbTestExperimentGroupId());

  }

  /**
   * asyncFetchABTest 请求，customIds key 为保留字段
   * 期望：SDK 拦截请求，直接返回默认值
   */
  @Test
  public void checkFastFetchWithInvalidCustomIds05() throws InvalidArgumentException {
    Map<String, String> customIds = Maps.newHashMap();
    String[] customInvalid =
        {"date", "datetime", "distinct_id", "event", "events", "first_id", "id", "original_id", "properties",
            "second_id", "time", "user_id", "users"};
    //初始化 AB Testing SDK
    initInstance(ABGlobalConfig.builder().setApiUrl(url).setSensorsAnalytics(sa).build());
    for (String s : customInvalid) {
      customIds.clear();
      customIds.put(s, "eee");
      Experiment<String> experiment =
          sensorsABTest.fastFetchABTest(SensorsABParams.starter("a123", true, "str_experiment", "qwe")
              .customIds(customIds)
              .build());
      assertNotNull(experiment);
      assertEquals("a123", experiment.getDistinctId());
      assertTrue(experiment.getIsLoginId());
      assertEquals("qwe", experiment.getResult());
      assertNull(experiment.getAbTestExperimentId());
      assertNull(experiment.getAbTestExperimentGroupId());
    }
  }

  /**
   * asyncFetchABTest 请求，customIds key 为非法字段
   * 期望：SDK 拦截请求，直接返回默认值
   */
  @Test
  public void checkAsyncFetchWithInvalidCustomIds06() throws InvalidArgumentException {
    Map<String, String> customIds = Maps.newHashMap();
    String[] customInvalid = {null, // key 为 null
        "",                     // key 为空字符串
        "1122aaaa",             // key 开头是数字
        "$1122aa",              // key 包含字符 $
        "……%%",                 // key 包含特殊字符
        "aaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaa",
        // key 为100个字符
        "aaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaa1"}; // key 超过 100个字符
    //初始化 AB Testing SDK
    initInstance(ABGlobalConfig.builder().setApiUrl(url).setSensorsAnalytics(sa).build());
    for (String s : customInvalid) {
      customIds.clear();
      customIds.put(s, "eee");
      Experiment<String> experiment =
          sensorsABTest.fastFetchABTest(SensorsABParams.starter("a123", true, "str_experiment", "qwe")
              .customIds(customIds)
              .build());
      assertNotNull(experiment);
      assertEquals("a123", experiment.getDistinctId());
      assertTrue(experiment.getIsLoginId());
      assertEquals("qwe", experiment.getResult());
      assertNull(experiment.getAbTestExperimentId());
      assertNull(experiment.getAbTestExperimentGroupId());
    }
  }

  /**
   * asyncFetchABTest 请求，customIds key 为非法字段
   * 期望：SDK 拦截请求，直接返回默认值
   */
  @Test
  public void checkAsyncFetchWithInvalidCustomIds07() throws InvalidArgumentException {
    Map<String, String> customIds = Maps.newHashMap();
    StringBuilder strInvalid = new StringBuilder();
    for (int i = 0; i < 1024; i++) {
      strInvalid.append(i);
    }
    String[] valueInvalid = {null, // value 为 null
        "",                    // value 为空字符串
        strInvalid.toString(),            // value 长度为 1024
        // value 长度大于 1024
        "a12345678901234567890123456789012345678901234567890123456789012345678901234567890123456789012345678901234567890"
            + "a12345678901234567890123456789012345678901234567890123456789012345678901234567890123456789012345678901234567890"
            + "a12345678901234567890123456789012345678901234567890123456789012345678901234567890123456789012345678901234567890"
            + "a12345678901234567890123456789012345678901234567890123456789012345678901234567890123456789012345678901234567890"
            + "a12345678901234567890123456789012345678901234567890123456789012345678901234567890123456789012345678901234567890"
            + "a12345678901234567890123456789012345678901234567890123456789012345678901234567890123456789012345678901234567890"
            + "a12345678901234567890123456789012345678901234567890123456789012345678901234567890123456789012345678901234567890"
            + "a12345678901234567890123456789012345678901234567890123456789012345678901234567890123456789012345678901234567890"
            + "a12345678901234567890123456789012345678901234567890123456789012345678901234567890123456789012345678901234567890"
            + "a12345678901234567890123456789012345678901234567890123456789012345678901234567890123456789012345678901234567890"
            + "a12345678901234567890123456789012345678901234567890123456789012345678901234567890123456789012345678901234567890"};
    //初始化 AB Testing SDK
    initInstance(ABGlobalConfig.builder().setApiUrl(url).setSensorsAnalytics(sa).build());
    for (String s : valueInvalid) {
      customIds.clear();
      customIds.put("customIds", s);
      Experiment<String> experiment =
          sensorsABTest.fastFetchABTest(SensorsABParams.starter("a123", true, "str_experiment", "qwe")
              .customIds(customIds)
              .build());
      assertNotNull(experiment);
      assertEquals("a123", experiment.getDistinctId());
      assertTrue(experiment.getIsLoginId());
      assertEquals("qwe", experiment.getResult());
      assertNull(experiment.getAbTestExperimentId());
      assertNull(experiment.getAbTestExperimentGroupId());
    }
  }


  /**
   * fastFetchABTest 请求，检查实验缓存
   * 期望：实验缓存的 key 为 "distinct_id + login_id + anonymous_id + customs_ids"+实验内容
   */
  @Test
  public void checkFastFetchExperimentCatch()
      throws InvalidArgumentException, NoSuchFieldException, IllegalAccessException {

    Map<String, String> customIds = Maps.newHashMap();
    customIds.put("qwe", "qwe");
    //初始化 AB Testing SDK
    initInstance(ABGlobalConfig.builder().setApiUrl(url).setSensorsAnalytics(sa).build());
    initInnerClassInfo(sensorsABTest);
    Experiment<Integer> experiment =
        sensorsABTest.fastFetchABTest(SensorsABParams.starter("a123", true, "int_experiment", -1)
            .addCustomId("qwe", "qwe")
            .build());

    assertEquals("123", experiment.getResult().toString());
    UserHitExperiment userHitExperiment = experimentResultCacheByReflect.getIfPresent(generateCacheKey("a123", true, customIds));

    Map<String, UserHitExperimentGroup> userHitExperimentMap = userHitExperiment.getUserHitExperimentMap();
    assertEquals(5, userHitExperimentMap.size());

    ExperimentGroupConfig experimentGroupConfig = userHitExperimentMap.get("2").getExperimentGroupConfig();
    assertEquals("2", experimentGroupConfig.getExperimentId());
    assertEquals("1", experimentGroupConfig.getExperimentGroupId());
    assertEquals(4, experimentGroupConfig.getVariableMap().size());
    experimentGroupConfig = userHitExperimentMap.get("6").getExperimentGroupConfig();
    assertEquals("test_group_id3", experimentGroupConfig.getVariableMap().get("test_group_id3").getName());
    assertEquals("JSON", experimentGroupConfig.getVariableMap().get("test_group_id3").getType());
    assertEquals("{\"name\":\"helloWord3\"}", experimentGroupConfig.getVariableMap().get("test_group_id3").getValue());
  }

  /**
   * fastFetchABTest 请求两次
   * 期望：第二次调用不会发送网络请求
   * 会返回缓存的实验结果值
   */
  @Test
  public void checkFastFetchExperimentCatch01()
      throws InvalidArgumentException, NoSuchFieldException, IllegalAccessException {

    Map<String, String> customIds = Maps.newHashMap();
    customIds.put("qwe", "qwe");
    //初始化 AB Testing SDK
    initInstance(ABGlobalConfig.builder().setApiUrl(url).setSensorsAnalytics(sa).build());
    initInnerClassInfo(sensorsABTest);

    Experiment<Integer> experiment =
        sensorsABTest.fastFetchABTest(SensorsABParams.starter("a123", true, "int_experiment", -1)
            .addCustomId("qwe", "qwe")
            .build());

    assertEquals("123", experiment.getResult().toString());
    UserHitExperiment userHitExperiment = experimentResultCacheByReflect.getIfPresent(generateCacheKey("a123", true, customIds));
    assertNotNull(userHitExperiment);

    //TODO 现在是否请求网络需要人工检查日志，需要自动化
    experiment = sensorsABTest.fastFetchABTest(SensorsABParams.starter("a123", true, "int_experiment", -1)
        .addCustomId("qwe", "qwe")
        .build());

    assertEquals(123, experiment.getResult().intValue());
  }

  /**
   * fastFetchABTest 请求，检查事件缓存的内容
   * 期望：本地事件缓存内容应该新增 customs_ids
   */
  @Test
  public void checkFastFetchEventCatch01()
      throws InvalidArgumentException, NoSuchFieldException, IllegalAccessException {

    Map<String, String> customIds = Maps.newHashMap();
    customIds.put("qwe", "qwe");
    //初始化 AB Testing SDK
    initInstance(ABGlobalConfig.builder().setApiUrl(url).setSensorsAnalytics(sa).build());
    initInnerClassInfo(sensorsABTest);

    Experiment<Integer> experiment =
        sensorsABTest.fastFetchABTest(SensorsABParams.starter("a123", true, "int_experiment", -1)
            .addCustomId("qwe", "qwe")
            .build());

    // 通过增加 customs_ids 生成的 key，去 get 事件缓存结果，如果不为空，则说明本地事件缓存内容新增了 customs_ids
    Object res =
        eventCacheByReflect.getIfPresent(generateKey("a123", true, experiment.getAbTestExperimentId(), customIds));
    assertNotNull(res);

    experiment = sensorsABTest.fastFetchABTest(SensorsABParams.starter("a123", true, "int_experiment", -1)
        .addCustomId("qwe", "qwe")
        .build());

    assertEquals(123, experiment.getResult().intValue());
  }

  /**
   * fastFetchABTest 请求两次
   * <p>
   * 期望：不会发送请求
   * 不会触发 $ABTestTrigger 事件
   * 会返回实验结果值
   */
  @Test
  public void checkFastFetchEventCatch02()
      throws InvalidArgumentException, NoSuchFieldException, IllegalAccessException {

    Map<String, String> customIds = Maps.newHashMap();
    customIds.put("qwe", "qwe");
    //初始化 AB Testing SDK
    initSASDK();
    initInstance(ABGlobalConfig.builder().setApiUrl(url).setSensorsAnalytics(sa).build());
    initInnerClassInfo(sensorsABTest);

    Experiment<Integer> experiment =
        sensorsABTest.fastFetchABTest(SensorsABParams.starter("a123", true, "int_experiment", -1)
            .addCustomId("qwe", "qwe")
            .build());
    assertEquals(123, experiment.getResult().intValue());
    sa.flush();

    experiment = sensorsABTest.fastFetchABTest(SensorsABParams.starter("a123", true, "int_experiment", -1)
        .addCustomId("qwe", "qwe")
        .build());

    assertEquals(123, experiment.getResult().intValue());
    assertEquals(0, messageBuffer.length());
  }


  /**
   * fastFetchABTest 请求两次，设置不同的 customId
   * <p>
   * 期望：两次都会发送分流请求，且CustomIds为设置的值， 两次都会触发 $ABTestTrigger 事件
   */
  @Test
  public void checkFastFetchEvent() throws InvalidArgumentException, NoSuchFieldException, IllegalAccessException {

    Map<String, String> customIds = Maps.newHashMap();
    customIds.put("qwe", "qwe");
    //初始化 AB Testing SDK
    initSASDK();
    initInstance(ABGlobalConfig.builder().setApiUrl(url).setSensorsAnalytics(sa).build());
    initInnerClassInfo(sensorsABTest);

    Experiment<Integer> experiment =
        sensorsABTest.fastFetchABTest(SensorsABParams.starter("a123", true, "int_experiment", -1)
            .addCustomId("qwe", "qwe")
            .build());
    assertEquals(123, experiment.getResult().intValue());

    sa.flush();

    experiment = sensorsABTest.fastFetchABTest(SensorsABParams.starter("a123", true, "int_experiment", -1)
        .addCustomId("customid2", "qwe")
        .build());
    //TODO 需要自动验证是否发送网络请求

    assertEquals(123, experiment.getResult().intValue());
    assertNotEquals(0, messageBuffer.length());

  }

  /**
   * fastFetchABTest 设置 customId 和 自定义属性
   * <p>
   * 期望：分流请求中的 CustomIds和自定义属性为设置的值,
   */

  @Test
  public void checkFastFetchWithProp() throws InvalidArgumentException {
    //初始化 AB Testing SDK
    initSASDK();
    initInstance(ABGlobalConfig.builder().setApiUrl(url).setSensorsAnalytics(sa).build());
    Experiment<String> experiment =
        sensorsABTest.fastFetchABTest(
            SensorsABParams.starter("a123", true, "str_experiment", "qwe")
                .addCustomId("custom_id", "test11")
                .addProperty("prop1", "value1")
                .build());
    assertNotNull(experiment);
    assertEquals("a123", experiment.getDistinctId());
    assertTrue(experiment.getIsLoginId());
    assertEquals("test", experiment.getResult());
    assertEquals("2", experiment.getAbTestExperimentId());
    assertEquals("1", experiment.getAbTestExperimentGroupId());
  }

  /**
   * SA SDK 设置静态公共属性 {"custom_id1":"xx1", "custom_id2": "xx2"}
   * fastFetchABTest 设置 customId
   * <p>
   * 期望：ab事件中含 新增CustomIds 属性 如：{"custom_id1":"xx1", "custom_id2": "xx2"}
   */
  @Test
  public void checkFastFetchEvent01() throws InvalidArgumentException, IOException {
    //初始化 AB Testing SDK
    initSASDK();
    // 注册公共属性
    Map<String, Object> superProp = new HashMap<>();
    superProp.put("custom_id1", "id1");
    superProp.put("custom_id2", "id2");
    sa.registerSuperProperties(superProp);

    initInstance(ABGlobalConfig.builder().setApiUrl(url).setSensorsAnalytics(sa).build());
    sensorsABTest.fastFetchABTest(
        SensorsABParams.starter("a123", true, "str_experiment", "qwe")
            .addCustomId("custom_id1", "id1")
            .addCustomId("custom_id2", "id2")
            .addProperty("prop1", "value1")
            .build());

    assertNotEquals(0, messageBuffer.length());
    JsonNode jsonNode = SensorsAnalyticsUtil.getJsonObjectMapper().readValue(messageBuffer.toString(), JsonNode.class);
    assertEquals("id1", jsonNode.get("properties").findValue("custom_id1").asText());
    assertEquals("id2", jsonNode.get("properties").findValue("custom_id2").asText());
  }

  /**
   * SA SDK 不设置静态公共属性
   * fastFetchABTest 设置 customId
   * <p>
   * 期望：ab事件中不会新增 CustomIds 属性
   */

  @Test
  public void checkFastFetchEvent02() throws InvalidArgumentException, IOException {
    //初始化 AB Testing SDK
    initSASDK();

    initInstance(ABGlobalConfig.builder().setApiUrl(url).setSensorsAnalytics(sa).build());
    sensorsABTest.fastFetchABTest(
        SensorsABParams.starter("a123", true, "str_experiment", "qwe")
            .addProperty("prop1", "value1")
            .build());

    assertNotEquals(0, messageBuffer.length());
    JsonNode jsonNode = SensorsAnalyticsUtil.getJsonObjectMapper().readValue(messageBuffer.toString(), JsonNode.class);
    assertNull(jsonNode.get("properties").get("custom_id1"));
  }

  /**
   * 兼容性：老环境 + fastFetchABTest 请求，携带一个正确的 customIds
   * 期望：命中试验，返回结果字段均有值
   */
  @Test
  @Ignore
  public void checkFastFetchOldEnv() throws InvalidArgumentException {
    String apiUrl =
        "http://10.130.6.5:8202/api/v2/abtest/online/results?project-key=438B9364C98D54371751BA82F6484A1A03A5155E";
    //初始化 AB Testing SDK
    initInstance(ABGlobalConfig.builder().setApiUrl(apiUrl).setSensorsAnalytics(sa).build());
    Experiment<Integer> experiment =
        sensorsABTest.fastFetchABTest(SensorsABParams.starter("a123", true, "agc", -1)
            .addCustomId("custom_id", "123")
            .build());
    assertNotNull(experiment);
    assertEquals("a123", experiment.getDistinctId());
    assertTrue(experiment.getIsLoginId());
    assertEquals(1, (int) experiment.getResult());
  }

  /**
   * 模拟 SDK 升级
   * 期望：升级后调用 fastFetchABTest 会重新发送分流请求，且请求中含自定义主体
   */
  @Test
  public void checkFastFetchUpdate() throws InvalidArgumentException {
    //初始化 AB Testing SDK
    initInstance(ABGlobalConfig.builder().setApiUrl(url).setSensorsAnalytics(sa).build());
    Experiment<String> experiment =
        sensorsABTest.fastFetchABTest("a123", true, "str_experiment", "grey");

    assertNotNull(experiment);
    assertEquals("a123", experiment.getDistinctId());
    assertTrue(experiment.getIsLoginId());
    assertEquals("test", experiment.getResult());
    assertEquals("2", experiment.getAbTestExperimentId());
    assertEquals("1", experiment.getAbTestExperimentGroupId());

    sensorsABTest.fastFetchABTest(
        SensorsABParams.starter("a123", true, "str_experiment", "qwe")
            .addCustomId("custom_id", "test11")
            .build());
    //TODO 自动化校验请求
  }

  /**
   * fastFetchABTest 请求，携带一个正确的 customIds
   * 期望：properties 字段中存在 custom_id
   */
  @Test
  @Ignore
  public void checkFastFetchWithCustomIds2()
      throws InvalidArgumentException, NoSuchFieldException, IllegalAccessException, IOException {
    //初始化 AB Testing SDK
    initSASDK();
    initInstance(ABGlobalConfig.builder().setApiUrl(url).setSensorsAnalytics(sa).build());
    initInnerClassInfo(sensorsABTest);
    HashMap<String, String> customIdMap = new HashMap<>();
    customIdMap.put("custom_id", "test11");
    Experiment<String> result =
        sensorsABTest.fastFetchABTest(
            SensorsABParams.starter("a123", false, "test_group_id2", "{\"color\":\"grey\"}").customIds(
                customIdMap).build());
    initInnerClassInfo(sensorsABTest);

    // 检查试验结果
    assertEquals("a123", result.getDistinctId());
    assertEquals(false, result.getIsLoginId());
    assertEquals("2", result.getAbTestExperimentGroupId());

    // 检查事件缓存
    assertEquals(1, eventCacheByReflect.size());

    if (messageBuffer != null) {
      assertNotEquals(0, messageBuffer.length());
      JsonNode jsonNode =
          SensorsAnalyticsUtil.getJsonObjectMapper().readValue(messageBuffer.toString(), JsonNode.class);
      assertEquals("\"$ABTestTrigger\"", jsonNode.get("event").toString());
      // 验证属性中是否包含 custom_id
      assertEquals("\"test11\"", jsonNode.get("properties").get("custom_id").toString());
    }
    assertNotNull(result.getResult());
  }

}
