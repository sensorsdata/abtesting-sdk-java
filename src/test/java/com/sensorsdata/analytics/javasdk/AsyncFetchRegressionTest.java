package com.sensorsdata.analytics.javasdk;

import com.fasterxml.jackson.databind.JsonNode;
import com.sensorsdata.analytics.javasdk.bean.ABGlobalConfig;
import com.sensorsdata.analytics.javasdk.bean.Experiment;
import com.sensorsdata.analytics.javasdk.exceptions.InvalidArgumentException;
import com.sensorsdata.analytics.javasdk.util.SensorsAnalyticsUtil;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;

import java.io.IOException;
import java.util.HashMap;

import static org.junit.Assert.*;

/**
 * asyncFetchABTest 接口回归测试用例
 *
 * @author fangzhuo
 * @version 1.0.0
 * @since 2022/01/13 10:38
 */
public class AsyncFetchRegressionTest extends SensorsBaseTest {

  String distinctId = "a123";

  @Before
  public void init(){
    initSASDK();
  }

  /**
   * 验证配置有效值后，初始化正确后调用 AsyncFetchABTest 且缓存存储正确
   */
  @Test
  public void asyncFetchABTest01()
          throws InvalidArgumentException, IOException, NoSuchFieldException, IllegalAccessException {
    //初始化 AB Testing SDK
    initSASDK();
    initInstance(ABGlobalConfig.builder().setApiUrl(url).setSensorsAnalytics(sa).build());
    initInnerClassInfo(sensorsABTest);

    Experiment<String> result =
            sensorsABTest.asyncFetchABTest(distinctId, false, "json_experiment", "{\"color\":\"grey\"}", true);
    initInnerClassInfo(sensorsABTest);

    // 检查试验结果
    assertEquals(distinctId, result.getDistinctId());
    assertEquals(false, result.getIsLoginId());
    assertEquals("2", result.getAbTestExperimentId());
    assertEquals("1", result.getAbTestExperimentGroupId());

    Assert.assertFalse(result.getIsControlGroup());
    Assert.assertFalse(result.getIsWhiteList());

    // asyncFetchABTest 接口固定不存储实验缓存, fastFetchABTest 接口才存储实验缓存
    assertEquals(0, experimentCacheManagerByReflect.getCacheSize());

    // 检查事件缓存
    assertEquals(1, eventCacheByReflect.size());

    if(messageBuffer != null) {
      assertNotEquals(0, messageBuffer.length());
      JsonNode jsonNode = SensorsAnalyticsUtil.getJsonObjectMapper().readValue(messageBuffer.toString(), JsonNode.class);
      assertEquals("\"$ABTestTrigger\"", jsonNode.get("event").toString());
    }
    assertNotNull(result.getResult());
  }

  /**
   * 验证配置有效值后，初始化正确后调用 AsyncFetchABTest 且缓存不存储
   */
  @Test
  public void asyncFetchABTest02()
          throws InvalidArgumentException, NoSuchFieldException, IllegalAccessException {
    //初始化 AB Testing SDK
    initSASDK();
    initInstance(ABGlobalConfig.builder().setApiUrl(url).setSensorsAnalytics(sa).build());

    Experiment<String> result =
            sensorsABTest.asyncFetchABTest(distinctId, false, "json_experiment", "{\"color\":\"grey\"}", false);
    initInnerClassInfo(sensorsABTest);

    assertEquals(distinctId, result.getDistinctId());
    assertEquals(false, result.getIsLoginId());
    assertEquals("2", result.getAbTestExperimentId());
    assertEquals("1", result.getAbTestExperimentGroupId());

    assertFalse(result.getIsControlGroup());
    assertFalse(result.getIsWhiteList());
    assertNotNull(result.getResult());

    // asyncFetchABTest 接口固定不存储实验缓存, fastFetchABTest 接口才存储实验缓存
    assertEquals(0, experimentCacheManagerByReflect.getCacheSize());

    // 检查事件缓存
    assertEquals(0, eventCacheByReflect.size());
    if(messageBuffer != null){
      assertEquals(0, messageBuffer.length());
    }
  }

  /**
   * defaultValue值 的类型 为 integer
   */
  @Test
  public void asyncFetchABTesReturnResultInt()
          throws InvalidArgumentException, NoSuchFieldException, IllegalAccessException {
    //构建配置 AB Testing 试验全局参数
    ABGlobalConfig abGlobalConfig = ABGlobalConfig.builder()
            .setApiUrl(url)                //分流试验地址
            .setSensorsAnalytics(sa)       //神策分析 SDK 实例
            .enableEventCache(true)
            .build();

    //初始化 AB Testing SDK
    final ISensorsABTest abTest = new SensorsABTest(abGlobalConfig);

    initInstance(abGlobalConfig);
    initInnerClassInfo(sensorsABTest);

    String experimentName = "int_experiment";
    Experiment<Integer> result = abTest.asyncFetchABTest(distinctId, false, experimentName, -1, false);

    assertEquals(distinctId, result.getDistinctId());
    assertEquals(false, result.getIsLoginId());
    assertEquals("2", result.getAbTestExperimentId());
    assertEquals("1", result.getAbTestExperimentGroupId());
    assertFalse(result.getIsControlGroup());
    assertFalse(result.getIsWhiteList());
    assertEquals(123, result.getResult().intValue());

    assertEquals(0, experimentCacheManagerByReflect.getCacheSize());

    assertNotNull(result.getResult());
  }

  /**
   * 默认值与返回值类型不一致
   * 期望：返回默认值，且试验相关字段信息返回为空，只返回默认信息
   */
  @Test
  public void checkDiffReturnType()
      throws NoSuchFieldException, IllegalAccessException, InvalidArgumentException {
    initInstance(ABGlobalConfig.builder().setApiUrl(url).setSensorsAnalytics(sa).build());
    Experiment<String> result = sensorsABTest.asyncFetchABTest("a123", false, "int_experiment", "grey");
    assertEquals("a123", result.getDistinctId());
    assertEquals(false, result.getIsLoginId());
    assertNull(result.getAbTestExperimentGroupId());
    assertNull(result.getAbTestExperimentId());
    assertNull(result.getIsControlGroup());
    assertNull(result.getIsWhiteList());
    initInnerClassInfo(sensorsABTest);
    assertEquals(0, experimentCacheManagerByReflect.getCacheSize());
  }

  /**
   * 开启事件缓存
   * 期望：命中试验，并缓存试验结果
   */
  @Test
  public void checkEnableEventCache()
      throws NoSuchFieldException, IllegalAccessException, InvalidArgumentException {
    initInstance(ABGlobalConfig.builder().setApiUrl(url).setSensorsAnalytics(sa).build());
    sensorsABTest.asyncFetchABTest("AB123", true, "str_experiment", "eee");
    initInnerClassInfo(sensorsABTest);
    assertEquals(1, eventCacheManagerByReflect.getCacheSize());
  }

  /**
   * 关闭事件缓存
   * 期望：事件没有被缓存
   */
  @Test
  public void checkClosedEventCache()
      throws InvalidArgumentException, NoSuchFieldException, IllegalAccessException {
    initInstance(ABGlobalConfig.builder().setApiUrl(url).setSensorsAnalytics(sa).enableEventCache(false).build());
    sensorsABTest.asyncFetchABTest("AB123", true, "str_experiment", "eee");
    initInnerClassInfo(sensorsABTest);
    assertEquals(0, eventCacheManagerByReflect.getCacheSize());
  }

  /**
   * AsyncFetchABTest 参数值 - isLoginId=false
   * 期望：命中试验，返回结果字段均存在值
   */
  @Test
  public void asyncFetchABTestIsLoginId01() throws InvalidArgumentException {
    initInstance(ABGlobalConfig.builder().setApiUrl(url).setSensorsAnalytics(sa).build());
    Experiment<Integer> result = sensorsABTest.asyncFetchABTest("a123", false, "int_experiment", -1, false);
    assertEquals("a123", result.getDistinctId());
    assertNotNull(result.getAbTestExperimentGroupId());
    assertEquals(false, result.getIsLoginId());
    assertNotNull(result.getResult());
  }

  /**
   * AsyncFetchABTest 参数值 - isLoginId=true
   * 期望：命中试验，返回结果字段均存在值
   */
  @Test
  public void asyncFetchABTestIsLoginId02() throws NoSuchFieldException, IllegalAccessException, InvalidArgumentException {
    initInstance(ABGlobalConfig.builder().setApiUrl(url).setSensorsAnalytics(sa).build());
    Experiment<Integer> result = sensorsABTest.asyncFetchABTest("a123", true, "int_experiment", -1, false);
    assertEquals("a123", result.getDistinctId());
    assertEquals(true, result.getIsLoginId());
    assertEquals("2", result.getAbTestExperimentId());
    assertEquals("1", result.getAbTestExperimentGroupId());
    assertFalse(result.getIsControlGroup());
    assertFalse(result.getIsWhiteList());
    initInnerClassInfo(sensorsABTest);
    assertEquals(0, experimentCacheManagerByReflect.getCacheSize());
    assertNotNull(result.getResult());
  }


  /**
   * AsyncFetchABTest 参数值为异常值- distinctId 超长
   * 期望：命中试验，返回结果字段均存在值
   */
  @Test
  public void asyncFetchABTestInvaildDistinctId01() throws NoSuchFieldException, IllegalAccessException, InvalidArgumentException {
    //初始化 AB Testing SDK
    initInstance(ABGlobalConfig.builder().setApiUrl(url).setSensorsAnalytics(sa).build());
    String distinctId = "123666664444444444444444444666666666666";
    Experiment<Integer> result = sensorsABTest.asyncFetchABTest(distinctId, true, "int_experiment", -1, false);
    assertEquals(distinctId, result.getDistinctId());
    assertEquals(true, result.getIsLoginId());
    assertEquals("2", result.getAbTestExperimentId());
    assertEquals("1", result.getAbTestExperimentGroupId());
    assertFalse(result.getIsControlGroup());
    assertFalse(result.getIsWhiteList());
    initInnerClassInfo(sensorsABTest);
    assertEquals(0, experimentCacheManagerByReflect.getCacheSize());
    assertNotNull(result.getResult());
  }


  /**
   * AsyncFetchABTest 参数值为异常值- distinctId = null
   * 期望：SDK 拦截请求，结果中部分字段不存在
   */
  @Test
  public void asyncFetchABTestInvaildDistinctId02() throws InvalidArgumentException {
    //初始化 AB Testing SDK
    initInstance(ABGlobalConfig.builder().setApiUrl(url).setSensorsAnalytics(sa).build());
    Experiment<Integer> result = sensorsABTest.asyncFetchABTest(null, true, "int_experiment", -1, false);
    assertNull(result.getDistinctId());
    assertEquals(true, result.getIsLoginId());
    assertNull(result.getAbTestExperimentGroupId());
    assertNotNull(result.getResult());
  }

  /**
   * AsyncFetchABTest 参数值为异常值- distinctId = ""
   * 期望：SDK 拦截请求，结果中部分字段不存在
   */
  @Test
  public void asyncFetchABTestInvaildDistinctId03() throws InvalidArgumentException {
    //初始化 AB Testing SDK
    initInstance(ABGlobalConfig.builder().setApiUrl(url).setSensorsAnalytics(sa).build());
    Experiment<Integer> result = sensorsABTest.asyncFetchABTest("", true, "int_experiment", -1, false);
    assertEquals("", result.getDistinctId());
    assertEquals(true, result.getIsLoginId());
    assertNull(result.getAbTestExperimentGroupId());
    assertNotNull(result.getResult());
  }

  /**
   * asyncFetchTest 请求，携带一个正确的 customIds
   * 期望：命中试验，返回结果字段均有值
   */
  @Test
  public void checkAsyncFetchTestWithSingleCustomIds() throws InvalidArgumentException {
    //初始化 AB Testing SDK
    initInstance(ABGlobalConfig.builder().setApiUrl(url).setSensorsAnalytics(sa).build());
    Experiment<Integer> experiment =
        sensorsABTest.asyncFetchABTest(SensorsABParams.starter("a123", true, "int_experiment", -1)
            .addCustomId("qwe", "qwe")
            .build());
    assertNotNull(experiment);
    assertEquals("a123", experiment.getDistinctId());
    assertTrue(experiment.getIsLoginId());
    assertEquals(123, (int) experiment.getResult());
    assertEquals("2", experiment.getAbTestExperimentId());
    assertEquals("1", experiment.getAbTestExperimentGroupId());
  }

  /**
   * asyncFetchTest 请求，携带多个正确的 customIds
   * 期望：命中试验，返回结果字段均有值
   */
  @Test
  public void checkAsyncFetchTestWithMultiCustomIds() throws InvalidArgumentException {
    //初始化 AB Testing SDK
    initInstance(ABGlobalConfig.builder().setApiUrl(url).setSensorsAnalytics(sa).build());
    Experiment<Integer> experiment =
        sensorsABTest.asyncFetchABTest(SensorsABParams.starter("a123", true, "int_experiment", -1)
            .addCustomId("qwe", "qwe")
            .addCustomId("fte", "fre")
            .build());
    assertNotNull(experiment);
    assertEquals("a123", experiment.getDistinctId());
    assertTrue(experiment.getIsLoginId());
    assertEquals(123, (int) experiment.getResult());
    assertEquals("2", experiment.getAbTestExperimentId());
    assertEquals("1", experiment.getAbTestExperimentGroupId());
  }

  /**
   * 调用 asyncFetchABTest 接口触发 $ABTestTrigger 事件，再次调用 asyncFetchABTest 接口，传入相同的试验参数
   * 期望：再次调用后 $ABTestTrigger 事件正常触发
   *      $ABTestTrigger 事件中 $abtest_experiment_group_id 为新的试验组 ID
   */ 
  @Test
  public void checkAsyncFetchTestExperimentGroupIdChange ()
      throws InvalidArgumentException, NoSuchFieldException, IllegalAccessException {
    //初始化 AB Testing SDK
    initSASDK();
    initInstance(ABGlobalConfig.builder().setApiUrl(url).setSensorsAnalytics(sa).build());
    initInnerClassInfo(sensorsABTest);
    HashMap<String, String> customIdMap = new HashMap<>();
    customIdMap.put("custom_id", "test11");
    // 第一次请求
    Experiment<String> result =
        sensorsABTest.asyncFetchABTest(SensorsABParams.starter(distinctId, false, "test_group_id2", "{\"color\":\"grey\"}").customIds(customIdMap).build());
    initInnerClassInfo(sensorsABTest);
    
    // 检查试验结果
    assertEquals(distinctId, result.getDistinctId());
    assertEquals(false, result.getIsLoginId());
    assertEquals("2", result.getAbTestExperimentGroupId());
    assertEquals("{\"name\":\"helloWord2\"}", result.getResult());
    
    
    // asyncFetchABTest 接口固定不存储实验缓存, fastFetchABTest 接口才存储实验缓存
    assertEquals(0, experimentCacheManagerByReflect.getCacheSize());

    // 检查事件缓存
    assertEquals(1, eventCacheByReflect.size());
    assertEquals("2", eventCacheByReflect.getIfPresent(generateKey(result.getDistinctId(), result.getIsLoginId(), result.getAbTestExperimentId(), customIdMap)));


    // 第二次请求
    Experiment<String> result2 =
        sensorsABTest.asyncFetchABTest(SensorsABParams.starter(distinctId, false, "test_group_id3", "{\"color\":\"grey\"}").customIds(customIdMap).build());
    initInnerClassInfo(sensorsABTest);

    assertEquals("a123", result2.getDistinctId());
    assertEquals(false, result2.getIsLoginId());
    assertEquals("3", result2.getAbTestExperimentGroupId());
    assertEquals("{\"name\":\"helloWord3\"}", result2.getResult());


    assertEquals(0, experimentCacheManagerByReflect.getCacheSize());

    // 检查事件缓存
    assertEquals(1, eventCacheByReflect.size());
    assertEquals("3", eventCacheByReflect.getIfPresent(generateKey(result2.getDistinctId(), result2.getIsLoginId(), result2.getAbTestExperimentId(), customIdMap)));
  }

  /**
   * 步骤：调用 asyncFetchABTest 接口，传入正确的试验参数
   *      触发 $ABTestTrigger 事件，再次调用 asyncFetchABTest 接口，传入相同的试验参数
   * 期望：再次调用后 $ABTestTrigger 事件不再触发     
   */
  @Test
  public void checkAsyncFetchTestExperimentGroupIdNotChange ()
      throws InvalidArgumentException, NoSuchFieldException, IllegalAccessException {
    //初始化 AB Testing SDK
    initSASDK();
    initInstance(ABGlobalConfig.builder().setApiUrl(url).setSensorsAnalytics(sa).build());
    initInnerClassInfo(sensorsABTest);
    HashMap<String, String> customIdMap = new HashMap<>();
    customIdMap.put("custom_id", "test11");
    // 第一次请求
    Experiment<String> result =
        sensorsABTest.asyncFetchABTest(SensorsABParams.starter(distinctId, false, "test_group_id2", "{\"color\":\"grey\"}").customIds(customIdMap).build());
    initInnerClassInfo(sensorsABTest);

    // 检查试验结果
    assertEquals(distinctId, result.getDistinctId());
    assertEquals(false, result.getIsLoginId());
    assertEquals("2", result.getAbTestExperimentGroupId());
    assertEquals("{\"name\":\"helloWord2\"}", result.getResult());


    // asyncFetchABTest 接口固定不存储实验缓存, fastFetchABTest 接口才存储实验缓存
    assertEquals(0, experimentCacheManagerByReflect.getCacheSize());

    // 检查事件缓存
    assertEquals(1, eventCacheByReflect.size());
    assertEquals("2", eventCacheByReflect.getIfPresent(generateKey(result.getDistinctId(), result.getIsLoginId(), result.getAbTestExperimentId(), customIdMap)));


    // 第二次请求
    Experiment<String> result2 =
        sensorsABTest.asyncFetchABTest(SensorsABParams.starter(distinctId, false, "test_group_id2", "{\"color\":\"grey\"}").customIds(customIdMap).build());
    initInnerClassInfo(sensorsABTest);

    assertEquals("a123", result2.getDistinctId());
    assertEquals(false, result2.getIsLoginId());
    assertEquals("2", result2.getAbTestExperimentGroupId());
    assertEquals("{\"name\":\"helloWord2\"}", result2.getResult());


    assertEquals(0, experimentCacheManagerByReflect.getCacheSize());

    // 检查事件缓存
    assertEquals(1, eventCacheByReflect.size());
    assertEquals("2", eventCacheByReflect.getIfPresent(generateKey(result2.getDistinctId(), result2.getIsLoginId(), result2.getAbTestExperimentId(), customIdMap)));
  }

  /**
   * 步骤：启动 App，调用 asyncFetchABTest 接口，传入正确的试验参数
   *      触发 $ABTestTrigger 事件，再次冷启动，调用 asyncFetchABTest 接口，传入相同的试验参数
   *      检查 $ABTestTrigger 事件触发情况
   * 期望：再次冷启动调用后 $ABTestTrigger 事件不再触发
   */
  @Test
  public void checkAsyncFetchTestExperimentGroupIdNotChangeRestart ()
      throws InvalidArgumentException, NoSuchFieldException, IllegalAccessException {
    //初始化 AB Testing SDK
    initSASDK();
    initInstance(ABGlobalConfig.builder().setApiUrl(url).setSensorsAnalytics(sa).build());
    initInnerClassInfo(sensorsABTest);
    HashMap<String, String> customIdMap = new HashMap<>();
    customIdMap.put("custom_id", "test11");
    // 第一次请求
    Experiment<String> result =
        sensorsABTest.asyncFetchABTest(SensorsABParams.starter(distinctId, false, "test_group_id2", "{\"color\":\"grey\"}").customIds(customIdMap).build());
    initInnerClassInfo(sensorsABTest);

    // 检查试验结果
    assertEquals(distinctId, result.getDistinctId());
    assertEquals(false, result.getIsLoginId());
    assertEquals("2", result.getAbTestExperimentGroupId());
    assertEquals("{\"name\":\"helloWord2\"}", result.getResult());


    // asyncFetchABTest 接口固定不存储实验缓存, fastFetchABTest 接口才存储实验缓存
    assertEquals(0, experimentCacheManagerByReflect.getCacheSize());

    // 检查事件缓存
    assertEquals(1, eventCacheByReflect.size());
    assertEquals("2", eventCacheByReflect.getIfPresent(generateKey(result.getDistinctId(), result.getIsLoginId(), result.getAbTestExperimentId(), customIdMap)));


    // 第二次请求
    //初始化 AB Testing SDK
    initSASDK();
    initInstance(ABGlobalConfig.builder().setApiUrl(url).setSensorsAnalytics(sa).build());
    initInnerClassInfo(sensorsABTest);
    
    Experiment<String> result2 =
        sensorsABTest.asyncFetchABTest(SensorsABParams.starter(distinctId, false, "test_group_id2", "{\"color\":\"grey\"}").customIds(customIdMap).build());
    initInnerClassInfo(sensorsABTest);

    assertEquals("a123", result2.getDistinctId());
    assertEquals(false, result2.getIsLoginId());
    assertEquals("2", result2.getAbTestExperimentGroupId());
    assertEquals("{\"name\":\"helloWord2\"}", result2.getResult());


    assertEquals(0, experimentCacheManagerByReflect.getCacheSize());

    // 检查事件缓存
    assertEquals(1, eventCacheByReflect.size());
    assertEquals("2", eventCacheByReflect.getIfPresent(generateKey(result2.getDistinctId(), result2.getIsLoginId(), result2.getAbTestExperimentId(), customIdMap)));
  }

  /**
   * 步骤：启动 App，调用 asyncFetchABTest 接口，传入正确的试验参数
   *      触发 $ABTestTrigger 事件，再次调用 asyncFetchABTest 接口，传入相同的试验参数
   *      检查 $ABTestTrigger 事件触发情况
   * 期望：再次调用后 $ABTestTrigger 事件正常触发
   *      $ABTestTrigger 事件中 $abtest_experiment_group_id 为新的试验组 ID
   *      更新事件缓存信息
   */
  @Test
  public void checkAsyncFetchTestExperimentGroupIdChangeRestart ()
      throws InvalidArgumentException, NoSuchFieldException, IllegalAccessException {
    //初始化 AB Testing SDK
    initSASDK();
    initInstance(ABGlobalConfig.builder().setApiUrl(url).setSensorsAnalytics(sa).build());
    initInnerClassInfo(sensorsABTest);
    HashMap<String, String> customIdMap = new HashMap<>();
    customIdMap.put("custom_id", "test11");
    // 第一次请求
    Experiment<String> result =
        sensorsABTest.asyncFetchABTest(SensorsABParams.starter(distinctId, false, "test_group_id2", "{\"color\":\"grey\"}").customIds(customIdMap).build());
    initInnerClassInfo(sensorsABTest);

    // 检查试验结果
    assertEquals(distinctId, result.getDistinctId());
    assertEquals(false, result.getIsLoginId());
    assertEquals("2", result.getAbTestExperimentGroupId());
    assertEquals("{\"name\":\"helloWord2\"}", result.getResult());


    // asyncFetchABTest 接口固定不存储实验缓存, fastFetchABTest 接口才存储实验缓存
    assertEquals(0, experimentCacheManagerByReflect.getCacheSize());

    // 检查事件缓存
    assertEquals(1, eventCacheByReflect.size());
    assertEquals("2", eventCacheByReflect.getIfPresent(generateKey(result.getDistinctId(), result.getIsLoginId(), result.getAbTestExperimentId(), customIdMap)));


    // 第二次请求
    initSASDK();
    initInstance(ABGlobalConfig.builder().setApiUrl(url).setSensorsAnalytics(sa).build());
    initInnerClassInfo(sensorsABTest);
    
    Experiment<String> result2 =
        sensorsABTest.asyncFetchABTest(SensorsABParams.starter(distinctId, false, "test_group_id3", "{\"color\":\"grey\"}").customIds(customIdMap).build());
    initInnerClassInfo(sensorsABTest);

    assertEquals("a123", result2.getDistinctId());
    assertEquals(false, result2.getIsLoginId());
    assertEquals("3", result2.getAbTestExperimentGroupId());
    assertEquals("{\"name\":\"helloWord3\"}", result2.getResult());


    assertEquals(0, experimentCacheManagerByReflect.getCacheSize());

    // 检查事件缓存
    assertEquals(1, eventCacheByReflect.size());
    assertEquals("3", eventCacheByReflect.getIfPresent(generateKey(result2.getDistinctId(), result2.getIsLoginId(), result2.getAbTestExperimentId(), customIdMap)));
  }
  
}
