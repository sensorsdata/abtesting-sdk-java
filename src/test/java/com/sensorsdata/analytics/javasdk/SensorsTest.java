package com.sensorsdata.analytics.javasdk;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertTrue;

import com.google.common.cache.LoadingCache;
import com.sensorsdata.analytics.javasdk.bean.ABGlobalConfig;
import com.sensorsdata.analytics.javasdk.bean.Experiment;
import com.sensorsdata.analytics.javasdk.cache.EventCacheManager;
import com.sensorsdata.analytics.javasdk.cache.ExperimentCacheManager;
import com.sensorsdata.analytics.javasdk.consumer.BatchConsumer;
import com.sensorsdata.analytics.javasdk.consumer.ConcurrentLoggingConsumer;
import com.sensorsdata.analytics.javasdk.exceptions.InvalidArgumentException;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.*;
import org.junit.runner.JUnitCore;
import org.junit.runner.Result;
import org.junit.runner.RunWith;
import org.junit.runner.notification.Failure;
import org.junit.runners.Suite;

import java.io.IOException;
import java.lang.reflect.Field;
import java.util.HashMap;
import java.util.Map;

/**
 * 顶层接口，对外暴露的方法测试类
 *
 * @author fz <fangzhuo@sensorsdata.cn>
 * @version 1.0.0
 * @since 2021/06/18 09:54
 */


public class SensorsTest {

  public SensorsTest() throws IOException, InvalidArgumentException {
  }

  //初始化神策分析 SDK
  final ISensorsAnalytics sa = new SensorsAnalytics(new ConcurrentLoggingConsumer("file.log"));
//    String serverUrl = "http://localhost/sa?project=default";
//    final ISensorsAnalytics sa = new SensorsAnalytics(new BatchConsumer(serverUrl));

  String url = "http://localhost/sa?project=default";

  //构建配置 AB Testing 试验全局参数
  ABGlobalConfig abGlobalConfig = ABGlobalConfig.builder()
          .setApiUrl(url)                //分流试验地址
          .setSensorsAnalytics(sa)       //神策分析 SDK 实例
          .enableEventCache(true)
          .build();
  //匿名ID 或者用户登录 ID，配合 isLoginId 使用
//    String distinctId = "xc123456";
  String distinctId = "AB123456";
  //试验名
  String experimentName = "int";


  //execute for each test, before executing test
  @Before
  public void before() throws NoSuchFieldException, IllegalAccessException {
    System.out.println("in before");
    final ISensorsABTest abTest = new SensorsABTest(abGlobalConfig);
    ExperimentCacheManager cacheManager =
            getExperimentCacheManagerByReflect(getSensorsABTestWorkerByReflect(abTest));
    getExperimentResultCacheByReflect(cacheManager).invalidateAll(); //强制清除实验缓存

    EventCacheManager eventCacheManager =
            getEventCacheManagerByReflect(getSensorsABTestWorkerByReflect(abTest));

    getEventCacheByReflect(eventCacheManager).invalidateAll(); //强制清除事件缓存

  }

  //execute for each test, after executing test
  @After
  public void after() {
    System.out.println("in after");
  }

  /**
   * 判断全局默认参数设置
   */
  @Test
  public void globalParam() {
    assertTrue(abGlobalConfig.getEventCacheSize() == 4096);
    assertTrue(abGlobalConfig.getEventCacheTime() == 1440);
    assertTrue(abGlobalConfig.getExperimentCacheSize() == 4096);
    assertTrue(abGlobalConfig.getExperimentCacheTime() == 1440);
    assertTrue(abGlobalConfig.getEnableEventCache());
  }

  /**
   * 立即调用，返回试验JSON类型，SDK JSON类型为JSON 字符串类型。
   */
  @Test
  public void asyncFetchABTestReturnRightResult() throws IOException, InvalidArgumentException {

    //初始化 AB Testing SDK
    final ISensorsABTest abTest = new SensorsABTest(abGlobalConfig);
//        Experiment<String> result = abTest.asyncFetchABTest(distinctId, false, experimentName, "{\"color\":\"grey\"}");
    Experiment<Integer> result = abTest.asyncFetchABTest(distinctId, false, experimentName, -1);
    System.out.println(" ========== " + result.getResult());
    assertNotNull(result);
    assertNotNull(result.getDistinctId());
    assertNotNull(result.getIsLoginId());
    assertNotNull(result.getResult());
    assertTrue(result.getResult() != null);
  }

  /**
   * 立即调用，返回试验JSON类型，SDK JSON类型为JSON 字符串类型。
   */
  @Test
  public void asyncFetchABTestReturnTimeoutResult() throws IOException, InvalidArgumentException {
    //构建配置 AB Testing 试验全局参数
    ABGlobalConfig abGlobalConfig = ABGlobalConfig.builder()
            .setApiUrl("http://localhost:8080/timeoutTest")                //分流试验地址
            .setSensorsAnalytics(sa)       //神策分析 SDK 实例
            .enableEventCache(true)
            .build();

    //初始化 AB Testing SDK
    final ISensorsABTest abTest = new SensorsABTest(abGlobalConfig);
//        Experiment<String> result = abTest.asyncFetchABTest(distinctId, false, experimentName, "{\"color\":\"grey\"}");
    Experiment<Integer> result = abTest.asyncFetchABTest(distinctId, false, experimentName, -1);
    System.out.println(" ========== " + result.getResult());
    assertNotNull(result);
    assertNotNull(result.getDistinctId());
    assertNotNull(result.getIsLoginId());
    assertNotNull(result.getResult());
    assertTrue(result.getResult() != null);
  }

  /**
   * 立即调用，返回试验JSON类型，SDK JSON类型为JSON 字符串类型。
   */
  @Test
  public void asyncFetchABTestReturnTimeout_1() throws IOException, InvalidArgumentException {
    //构建配置 AB Testing 试验全局参数
    ABGlobalConfig abGlobalConfig = ABGlobalConfig.builder()
            .setApiUrl("http://localhost:8080/timeoutTest")                //分流试验地址
            .setSensorsAnalytics(sa)       //神策分析 SDK 实例
            .enableEventCache(true)
            .build();

    //初始化 AB Testing SDK
    final ISensorsABTest abTest = new SensorsABTest(abGlobalConfig);
//        Experiment<String> result = abTest.asyncFetchABTest(distinctId, false, experimentName, "{\"color\":\"grey\"}");
    Experiment<Integer> result = abTest.asyncFetchABTest(distinctId, false, experimentName, -1, -1);
    System.out.println(" ========== " + result.getResult());
    assertNotNull(result);
    assertNotNull(result.getDistinctId());
    assertNotNull(result.getIsLoginId());
    assertNotNull(result.getResult());
    assertTrue(result.getResult() != null);
  }

  /**
   * 立即调用，返回试验JSON类型，SDK JSON类型为JSON 字符串类型。
   */
  @Test
  public void fastFetchABTest_asyncFetchABTest() throws IOException, InvalidArgumentException {
    //构建配置 AB Testing 试验全局参数
    ABGlobalConfig abGlobalConfig = ABGlobalConfig.builder()
            .setApiUrl(url)                //分流试验地址
            .setSensorsAnalytics(sa)       //神策分析 SDK 实例
            .enableEventCache(true)
            .build();

    //初始化 AB Testing SDK
    final ISensorsABTest abTest = new SensorsABTest(abGlobalConfig);

    Experiment<Integer> result = abTest.fastFetchABTest(distinctId, false, experimentName, -1);
    System.out.println("========== " + result.getResult());

    result = abTest.asyncFetchABTest(distinctId, false, experimentName, -1);
    System.out.println("========== " + result.getResult());
  }

  /**
   * 验证配置有效值后，初始化正确后调用 AsyncFetchABTest 且缓存不存储
   */
  @Test
  public void asyncFetchABTesReturnResultJson() throws InvalidArgumentException, IOException, NoSuchFieldException, IllegalAccessException{
    //初始化 AB Testing SDK
    final ISensorsABTest abTest = new SensorsABTest(abGlobalConfig);
//        Experiment<String> result = abTest.asyncFetchABTest(distinctId, false, experimentName, "grey", false);
    String experimentName = "color";
    Experiment<String> result = abTest.asyncFetchABTest(distinctId, false, experimentName, "{\"color\":\"grey\"}", false);
    System.out.println("======= " + result.getResult());

    assertEquals(distinctId, result.getDistinctId());
    assertEquals(false, result.getIsLoginId());
    assertEquals("102",result.getAbTestExperimentId());
    assertEquals("0",result.getAbTestExperimentGroupId());
    Assert.assertTrue(result.getControlGroup());
    Assert.assertTrue(result.getWhiteList());

    //
    ExperimentCacheManager cacheManager =
            getExperimentCacheManagerByReflect(getSensorsABTestWorkerByReflect(abTest));

    assertEquals(0, cacheManager.getCacheSize());

    assertNotNull(result.getResult());
  }

  /**
   * 验证配置有效值后，初始化正确后调用 fastFetchABTest 且缓存不存储
   */
  @Test
  public void fastFetchABTestReturnResultJson() throws InvalidArgumentException, IOException, NoSuchFieldException, IllegalAccessException{
    //初始化 AB Testing SDK
    final ISensorsABTest abTest = new SensorsABTest(abGlobalConfig);
//        Experiment<String> result = abTest.asyncFetchABTest(distinctId, false, experimentName, "grey", false);
    String experimentName = "color";
    Experiment<String> result = abTest.fastFetchABTest(distinctId, false, experimentName, "{\"color\":\"grey\"}", false);
    System.out.println("======= result: " + result.getResult());

    assertEquals(distinctId, result.getDistinctId());
    assertEquals(false, result.getIsLoginId());
    assertEquals("102",result.getAbTestExperimentId());
    assertEquals("0",result.getAbTestExperimentGroupId());
    Assert.assertTrue(result.getControlGroup());
    Assert.assertTrue(result.getWhiteList());

    //
    ExperimentCacheManager cacheManager =
            getExperimentCacheManagerByReflect(getSensorsABTestWorkerByReflect(abTest));

    assertEquals(1, cacheManager.getCacheSize());

    assertNotNull(result.getResult());
  }

  /**
   * 验证配置有效值后，初始化正确后调用 AsyncFetchABTest 且缓存不存储
   */
  @Test
  public void asyncFetchABTesReturnResultInt() throws InvalidArgumentException, IOException, NoSuchFieldException, IllegalAccessException{
    //初始化 AB Testing SDK
    final ISensorsABTest abTest = new SensorsABTest(abGlobalConfig);
//        Experiment<String> result = abTest.asyncFetchABTest(distinctId, false, experimentName, "grey", false);
    String experimentName = "test_int";
    Experiment<Integer> result = abTest.asyncFetchABTest(distinctId, false, experimentName, -1, false);
    System.out.println("======= result: " + result.getResult());

    assertEquals(distinctId, result.getDistinctId());
    assertEquals(false, result.getIsLoginId());
    assertEquals("102",result.getAbTestExperimentId());
    assertEquals("0",result.getAbTestExperimentGroupId());
    Assert.assertTrue(result.getControlGroup());
    Assert.assertTrue(result.getWhiteList());

    //
    ExperimentCacheManager cacheManager =
            getExperimentCacheManagerByReflect(getSensorsABTestWorkerByReflect(abTest));

    assertEquals(0, cacheManager.getCacheSize());

    assertNotNull(result.getResult());
  }

  /**
   * 验证配置有效值后，初始化正确后调用 fastFetchABTest 且缓存不存储
   */
  @Test
  public void fastFetchABTestReturnResultInt() throws InvalidArgumentException, IOException, NoSuchFieldException, IllegalAccessException{
    //初始化 AB Testing SDK
    final ISensorsABTest abTest = new SensorsABTest(abGlobalConfig);
//        Experiment<String> result = abTest.asyncFetchABTest(distinctId, false, experimentName, "grey", false);
    String experimentName = "test_int";
    Experiment<Integer> result = abTest.fastFetchABTest(distinctId, false, experimentName, -1, false);
    System.out.println("======= result: " + result.getResult());

    assertEquals(distinctId, result.getDistinctId());
    assertEquals(false, result.getIsLoginId());
    assertEquals("102",result.getAbTestExperimentId());
    assertEquals("0",result.getAbTestExperimentGroupId());
    Assert.assertTrue(result.getControlGroup());
    Assert.assertTrue(result.getWhiteList());

    //
    ExperimentCacheManager cacheManager =
            getExperimentCacheManagerByReflect(getSensorsABTestWorkerByReflect(abTest));

    assertEquals(1, cacheManager.getCacheSize());

    assertNotNull(result.getResult());
  }

  /**
   * 验证配置有效值后，初始化正确后调用 AsyncFetchABTest 且缓存不存储
   */
  @Test
  public void asyncFetchABTesReturnResultStr() throws InvalidArgumentException, IOException, NoSuchFieldException, IllegalAccessException{
    //初始化 AB Testing SDK
    final ISensorsABTest abTest = new SensorsABTest(abGlobalConfig);
//        Experiment<String> result = abTest.asyncFetchABTest(distinctId, false, experimentName, "grey", false);
    String experimentName = "test_str";
    Experiment<String> result = abTest.asyncFetchABTest(distinctId, false, experimentName, "...", false);
    System.out.println("======= result: " + result.getResult());

    assertEquals(distinctId, result.getDistinctId());
    assertEquals(false, result.getIsLoginId());
    assertEquals("102",result.getAbTestExperimentId());
    assertEquals("0",result.getAbTestExperimentGroupId());
    Assert.assertTrue(result.getControlGroup());
    Assert.assertTrue(result.getWhiteList());

    //
    ExperimentCacheManager cacheManager =
            getExperimentCacheManagerByReflect(getSensorsABTestWorkerByReflect(abTest));

    assertEquals(0, cacheManager.getCacheSize());

    assertNotNull(result.getResult());
  }

  /**
   * 验证配置有效值后，初始化正确后调用 fastFetchABTest 且缓存不存储
   */
  @Test
  public void fastFetchABTestReturnResultStr() throws InvalidArgumentException, IOException, NoSuchFieldException, IllegalAccessException{
    //初始化 AB Testing SDK
    final ISensorsABTest abTest = new SensorsABTest(abGlobalConfig);
//        Experiment<String> result = abTest.asyncFetchABTest(distinctId, false, experimentName, "grey", false);
    String experimentName = "test_str";
    Experiment<String> result = abTest.fastFetchABTest(distinctId, false, experimentName, "...", false);
    System.out.println("======= result: " + result.getResult());

    assertEquals(distinctId, result.getDistinctId());
    assertEquals(false, result.getIsLoginId());
    assertEquals("102",result.getAbTestExperimentId());
    assertEquals("0",result.getAbTestExperimentGroupId());
    Assert.assertTrue(result.getControlGroup());
    Assert.assertTrue(result.getWhiteList());

    //
    ExperimentCacheManager cacheManager =
            getExperimentCacheManagerByReflect(getSensorsABTestWorkerByReflect(abTest));

    assertEquals(1, cacheManager.getCacheSize());

    assertNotNull(result.getResult());
  }

  /**
   * 验证配置有效值后，初始化正确后调用 AsyncFetchABTest 且缓存不存储
   */
  @Test
  public void asyncFetchABTesReturnResultBool() throws InvalidArgumentException, IOException, NoSuchFieldException, IllegalAccessException{
    //初始化 AB Testing SDK
    final ISensorsABTest abTest = new SensorsABTest(abGlobalConfig);
    String experimentName = "test_bool";
    Experiment<Boolean> result = abTest.asyncFetchABTest(distinctId, false, experimentName, false, false);
    System.out.println("======= result: " + result.getResult());

    assertEquals(distinctId, result.getDistinctId());
    assertEquals(false, result.getIsLoginId());
    assertEquals("102",result.getAbTestExperimentId());
    assertEquals("0",result.getAbTestExperimentGroupId());
    Assert.assertTrue(result.getControlGroup());
    Assert.assertTrue(result.getWhiteList());

    //
    ExperimentCacheManager cacheManager =
            getExperimentCacheManagerByReflect(getSensorsABTestWorkerByReflect(abTest));

    assertEquals(0, cacheManager.getCacheSize());

    assertNotNull(result.getResult());
  }

  /**
   * 验证配置有效值后，初始化正确后调用 fastFetchABTest 且缓存不存储
   */
  @Test
  public void fastFetchABTestReturnResultBool() throws InvalidArgumentException, IOException, NoSuchFieldException, IllegalAccessException{
    //初始化 AB Testing SDK
    final ISensorsABTest abTest = new SensorsABTest(abGlobalConfig);
    String experimentName = "test_bool";
    Experiment<Boolean> result = abTest.fastFetchABTest(distinctId, false, experimentName, false, false);
    System.out.println("======= result: " + result.getResult());

    assertEquals(distinctId, result.getDistinctId());
    assertEquals(false, result.getIsLoginId());
    assertEquals("102",result.getAbTestExperimentId());
    assertEquals("0",result.getAbTestExperimentGroupId());
    Assert.assertTrue(result.getControlGroup());
    Assert.assertTrue(result.getWhiteList());

    //
    ExperimentCacheManager cacheManager =
            getExperimentCacheManagerByReflect(getSensorsABTestWorkerByReflect(abTest));

    assertEquals(1, cacheManager.getCacheSize());

    assertNotNull(result.getResult());
  }

  /**
   * 验证断网情况下调用 AsyncFetchABTest 返回默认值
   */
  @Test
  public void asyncFetchABTest02() throws IOException, InvalidArgumentException {
    //初始化 AB Testing SDK
    final ISensorsABTest abTest = new SensorsABTest(abGlobalConfig);
    Experiment<Integer> result = abTest.asyncFetchABTest(distinctId, false, experimentName, -1, false);
    System.out.println("======= " + result.getResult());

    assertEquals(distinctId, result.getDistinctId());
    assertEquals(false, result.getIsLoginId());
    assertNotNull(result.getResult());
  }

  /**
   * 验证断网情况下调用 AsyncFetchABTest 返回默认值
   */
  @Test
  public void asyncFetchABTest03() throws IOException, InvalidArgumentException {

    //初始化 AB Testing SDK
    final ISensorsABTest abTest = new SensorsABTest(abGlobalConfig);
    Experiment<Integer> result = abTest.asyncFetchABTest(distinctId, false, experimentName, -1, false);
    System.out.println("======= " + result.getResult());

    assertEquals(distinctId, result.getDistinctId());
    assertEquals(false, result.getIsLoginId());
    assertNotNull(result.getResult());
  }

  /**
   * 验证断网情况下调用 AsyncFetchABTest 返回默认值
   */
  @Test
  public void asyncFetchABTest04() throws IOException, InvalidArgumentException {

    //初始化 AB Testing SDK
    final ISensorsABTest abTest = new SensorsABTest(abGlobalConfig);
    Experiment<Integer> result = abTest.asyncFetchABTest(distinctId, false, experimentName, -1, false);
    System.out.println("======= " + result.getResult());

    assertEquals(distinctId, result.getDistinctId());
    assertEquals(false, result.getIsLoginId());
    assertNotNull(result.getResult());
  }


  /**
   *  AsyncFetchABTest 参数值 - isLoginId=false
   */
  @Test
  public void asyncFetchABTest05() throws IOException, InvalidArgumentException {

    //初始化 AB Testing SDK
    final ISensorsABTest abTest = new SensorsABTest(abGlobalConfig);
    Experiment<Integer> result = abTest.asyncFetchABTest(distinctId, false, experimentName, -1, false);
    System.out.println("======= " + result.getResult());

    assertEquals(distinctId, result.getDistinctId());
    assertEquals(false, result.getIsLoginId());
    assertNotNull(result.getResult());
  }

  /**
   *  AsyncFetchABTest 参数值 - isLoginId=true
   */
  @Test
  public void asyncFetchABTest06() throws IOException, InvalidArgumentException, NoSuchFieldException, IllegalAccessException {

    //初始化 AB Testing SDK
    final ISensorsABTest abTest = new SensorsABTest(abGlobalConfig);
    Experiment<Integer> result = abTest.asyncFetchABTest(distinctId, true, experimentName, -1, false);
    System.out.println("======= " + result.getResult());

    assertEquals(distinctId, result.getDistinctId());
    assertEquals(true, result.getIsLoginId());
    assertEquals("115",result.getAbTestExperimentId());
    assertEquals("1",result.getAbTestExperimentGroupId());
    Assert.assertFalse(result.getControlGroup());
    Assert.assertFalse(result.getWhiteList());

    //
    ExperimentCacheManager cacheManager =
            getExperimentCacheManagerByReflect(getSensorsABTestWorkerByReflect(abTest));

    assertEquals(0, cacheManager.getCacheSize());

    assertNotNull(result.getResult());
  }


  /**
   *  AsyncFetchABTest 参数值为异常值- distinctId 超长
   */
  @Test
  public void asyncFetchABTest07() throws IOException, InvalidArgumentException, NoSuchFieldException, IllegalAccessException {

    //初始化 AB Testing SDK

    String distinctId = "123666664444444444444444444666666666666";

    final ISensorsABTest abTest = new SensorsABTest(abGlobalConfig);
    Experiment<Integer> result = abTest.asyncFetchABTest(distinctId, true, experimentName, -1, false);
    System.out.println("======= " + result.getResult());

    assertEquals(distinctId, result.getDistinctId());
    assertEquals(true, result.getIsLoginId());
    assertEquals("115",result.getAbTestExperimentId());
    assertEquals("1",result.getAbTestExperimentGroupId());
    Assert.assertFalse(result.getControlGroup());
    Assert.assertFalse(result.getWhiteList());

    //
    ExperimentCacheManager cacheManager =
            getExperimentCacheManagerByReflect(getSensorsABTestWorkerByReflect(abTest));

    assertEquals(0, cacheManager.getCacheSize());

    assertNotNull(result.getResult());
  }


  /**
   *  AsyncFetchABTest 参数值为异常值- distinctId = null
   */
  @Test
  public void asyncFetchABTest08() throws IOException, InvalidArgumentException, NoSuchFieldException, IllegalAccessException {

    //初始化 AB Testing SDK

    String distinctId = null;

    final ISensorsABTest abTest = new SensorsABTest(abGlobalConfig);
    Experiment<Integer> result = abTest.asyncFetchABTest(distinctId, true, experimentName, -1, false);
    System.out.println("======= " + result.getResult());

    assertEquals(distinctId, result.getDistinctId());
    assertEquals(true, result.getIsLoginId());
    assertEquals("115",result.getAbTestExperimentId());
    assertEquals("1",result.getAbTestExperimentGroupId());
    Assert.assertFalse(result.getControlGroup());
    Assert.assertFalse(result.getWhiteList());

    //
    ExperimentCacheManager cacheManager =
            getExperimentCacheManagerByReflect(getSensorsABTestWorkerByReflect(abTest));

    assertEquals(0, cacheManager.getCacheSize());

    assertNotNull(result.getResult());
  }

  /**
   *  AsyncFetchABTest 参数值为异常值- distinctId = ""
   */
  @Test
  public void asyncFetchABTest09() throws IOException, InvalidArgumentException, NoSuchFieldException, IllegalAccessException {

    //初始化 AB Testing SDK

    String distinctId = "";

    final ISensorsABTest abTest = new SensorsABTest(abGlobalConfig);
    Experiment<Integer> result = abTest.asyncFetchABTest(distinctId, true, experimentName, -1, false);
    System.out.println("======= " + result.getResult());

    assertEquals(distinctId, result.getDistinctId());
    assertEquals(true, result.getIsLoginId());
    assertEquals("115",result.getAbTestExperimentId());
    assertEquals("1",result.getAbTestExperimentGroupId());
    Assert.assertFalse(result.getControlGroup());
    Assert.assertFalse(result.getWhiteList());

    //
    ExperimentCacheManager cacheManager =
            getExperimentCacheManagerByReflect(getSensorsABTestWorkerByReflect(abTest));

    assertEquals(0, cacheManager.getCacheSize());
    assertNotNull(result.getResult());
  }

  /**
   *  fastFetchABTest 参数值 - isLoginId=false
   */
  @Test
  public void fastFetchABTest05() throws IOException, InvalidArgumentException {

    //初始化 AB Testing SDK
    final ISensorsABTest abTest = new SensorsABTest(abGlobalConfig);
    Experiment<Integer> result = abTest.fastFetchABTest(distinctId, false, experimentName, -1, false);
    System.out.println("======= " + result.getResult());

    assertEquals(distinctId, result.getDistinctId());
    assertEquals(false, result.getIsLoginId());
    assertNotNull(result.getResult());
  }

  /**
   *  fastFetchABTest 参数值 - isLoginId=true
   */
  @Test
  public void fastFetchABTest06() throws IOException, InvalidArgumentException, NoSuchFieldException, IllegalAccessException {
    //初始化 AB Testing SDK
    final ISensorsABTest abTest = new SensorsABTest(abGlobalConfig);
    ExperimentCacheManager cacheManager =
            getExperimentCacheManagerByReflect(getSensorsABTestWorkerByReflect(abTest));
    Experiment<Integer> result = abTest.fastFetchABTest(distinctId, true, experimentName, -1, false);
    System.out.println("==== cacheManager ==== " + cacheManager.getExperimentResultByCache(distinctId, true, experimentName));
    System.out.println("==== result ==== " + result.getResult());

    assertEquals(distinctId, result.getDistinctId());
    assertEquals(true, result.getIsLoginId());
    assertEquals("115",result.getAbTestExperimentId());
    assertEquals("1",result.getAbTestExperimentGroupId());
    Assert.assertFalse(result.getControlGroup());
    Assert.assertFalse(result.getWhiteList());


    assertEquals(0, cacheManager.getCacheSize());
    assertNotNull(result.getResult());
  }


  /**
   *  fastFetchABTest 参数值为异常值- distinctId 超长
   */
  @Test
  public void fastFetchABTest07() throws IOException, InvalidArgumentException, NoSuchFieldException, IllegalAccessException {

    //初始化 AB Testing SDK

    String distinctId = "123666664444444444444444444666666666666";

    final ISensorsABTest abTest = new SensorsABTest(abGlobalConfig);
    Experiment<Integer> result = abTest.fastFetchABTest(distinctId, true, experimentName, -1, false);
    System.out.println("======= " + result.getResult());

    assertEquals(distinctId, result.getDistinctId());
    assertEquals(true, result.getIsLoginId());
    assertEquals("115",result.getAbTestExperimentId());
    assertEquals("1",result.getAbTestExperimentGroupId());
    Assert.assertFalse(result.getControlGroup());
    Assert.assertFalse(result.getWhiteList());

    //
    ExperimentCacheManager cacheManager =
            getExperimentCacheManagerByReflect(getSensorsABTestWorkerByReflect(abTest));

    assertEquals(0, cacheManager.getCacheSize());

    assertNotNull(result.getResult());
  }


  /**
   *  fastFetchABTest 参数值为异常值- distinctId = null
   */
  @Test
  public void fastFetchABTest08() throws IOException, InvalidArgumentException, NoSuchFieldException, IllegalAccessException {

    //初始化 AB Testing SDK

    String distinctId = null;

    final ISensorsABTest abTest = new SensorsABTest(abGlobalConfig);
    Experiment<Integer> result = abTest.fastFetchABTest(distinctId, true, experimentName, -1, false);
  }

  /**
   *  fastFetchABTest 参数值为异常值- distinctId = ""
   */
  @Test
  public void fastFetchABTest09() throws IOException, InvalidArgumentException, NoSuchFieldException, IllegalAccessException {

    //初始化 AB Testing SDK

    String distinctId = "";

    final ISensorsABTest abTest = new SensorsABTest(abGlobalConfig);
    Experiment<Integer> result = abTest.fastFetchABTest(distinctId, true, experimentName, -1, false);
  }

  /**
   *  当用户不在白名单内——首次触发 asyncFetchABTest
   */
  @Test
  public void asyncFetchABTestFirst() throws IOException, InvalidArgumentException, NoSuchFieldException, IllegalAccessException {
    //初始化 AB Testing SDK
    final ISensorsABTest abTest = new SensorsABTest(abGlobalConfig);
//        Experiment<String> result = abTest.asyncFetchABTest(distinctId, false, experimentName, "grey", false);
    Experiment<Integer> result = abTest.asyncFetchABTest(distinctId, false, experimentName, -1, true);
    System.out.println("======= " + result.getResult());

    assertEquals(distinctId, result.getDistinctId());
    assertEquals(false, result.getIsLoginId());
    assertEquals("115",result.getAbTestExperimentId());
    assertEquals("1",result.getAbTestExperimentGroupId());
    Assert.assertFalse(result.getControlGroup());
    Assert.assertFalse(result.getWhiteList());

    //
    ExperimentCacheManager cacheManager =
            getExperimentCacheManagerByReflect(getSensorsABTestWorkerByReflect(abTest));

    assertEquals(0, cacheManager.getCacheSize());

    assertNotNull(result.getResult());
  }

  /**
   *  当用户不在白名单内——首次触发 fastFetchABTest
   */
  @Test
  public void fastFetchABTestFirst() throws IOException, InvalidArgumentException, NoSuchFieldException, IllegalAccessException {
    //初始化 AB Testing SDK
    final ISensorsABTest abTest = new SensorsABTest(abGlobalConfig);
//        Experiment<String> result = abTest.asyncFetchABTest(distinctId, false, experimentName, "grey", false);
    Experiment<Integer> result = abTest.fastFetchABTest(distinctId, false, experimentName, -1, true);
    System.out.println("======= " + result.getResult());

//        assertEquals(distinctId, result.getDistinctId());
//        assertEquals(false, result.getIsLoginId());
//        assertEquals("115",result.getAbTestExperimentId());
//        assertEquals("1",result.getAbTestExperimentGroupId());
//        Assert.assertFalse(result.getControlGroup());
//        Assert.assertFalse(result.getWhiteList());

//        ExperimentCacheManager cacheManager =
//                getExperimentCacheManagerByReflect(getSensorsABTestWorkerByReflect(abTest));
//
//        assertEquals(0, cacheManager.getCacheSize());
//
//        assertNotNull(result.getResult());

    // 第二次请求
    result = abTest.fastFetchABTest(distinctId, false, experimentName, -1, true);
    System.out.println("======= " + result.getResult());
  }

  /**
   *  experimentVariableName = null
   */
  @Test
  public void nullExperimentVariableNameF() throws IOException, InvalidArgumentException, NoSuchFieldException, IllegalAccessException {
    //初始化 AB Testing SDK
    final ISensorsABTest abTest = new SensorsABTest(abGlobalConfig);
//        Experiment<String> result = abTest.asyncFetchABTest(distinctId, false, experimentName, "grey", false);
    Experiment<Integer> result = abTest.fastFetchABTest(distinctId, false, null, -1, true);
    System.out.println("======= " + result.getResult());
  }

  /**
   *  experimentVariableName = null
   */
  @Test
  public void nullExperimentVariableNameA() throws IOException, InvalidArgumentException, NoSuchFieldException, IllegalAccessException {
    //初始化 AB Testing SDK
    final ISensorsABTest abTest = new SensorsABTest(abGlobalConfig);
//        Experiment<String> result = abTest.asyncFetchABTest(distinctId, false, experimentName, "grey", false);
    Experiment<Integer> result = abTest.asyncFetchABTest(distinctId, false, null, -1, true);
    System.out.println("======= " + result.getResult());
  }

  /**
   *  experimentVariableName = ""
   */
  @Test
  public void EmptyExperimentVariableNameF() throws IOException, InvalidArgumentException, NoSuchFieldException, IllegalAccessException {
    //初始化 AB Testing SDK
    final ISensorsABTest abTest = new SensorsABTest(abGlobalConfig);
//        Experiment<String> result = abTest.asyncFetchABTest(distinctId, false, experimentName, "grey", false);
    Experiment<Integer> result = abTest.fastFetchABTest(distinctId, false, "", -1, true);
    System.out.println("======= " + result.getResult());
  }

  /**
   *  experimentVariableName = ""
   */
  @Test
  public void EmptyExperimentVariableNameA() throws IOException, InvalidArgumentException, NoSuchFieldException, IllegalAccessException {
    //初始化 AB Testing SDK
    final ISensorsABTest abTest = new SensorsABTest(abGlobalConfig);
//        Experiment<String> result = abTest.asyncFetchABTest(distinctId, false, experimentName, "grey", false);
    Experiment<Integer> result = abTest.asyncFetchABTest(distinctId, false, "", -1, true);
    System.out.println("======= " + result.getResult());
  }

  /**
   *  experimentVariableName = long string
   */
  @Test
  public void longStringExperimentVariableNameF() throws IOException, InvalidArgumentException, NoSuchFieldException, IllegalAccessException {
    //初始化 AB Testing SDK
    final ISensorsABTest abTest = new SensorsABTest(abGlobalConfig);
//        Experiment<String> result = abTest.asyncFetchABTest(distinctId, false, experimentName, "grey", false);
    Experiment<Integer> result = abTest.fastFetchABTest(distinctId, false, "12311111111111111111111111111111111111111111111", -1, true);
    System.out.println("======= " + result.getResult());
  }

  /**
   *  experimentVariableName = long string
   */
  @Test
  public void longStringExperimentVariableNameA() throws IOException, InvalidArgumentException, NoSuchFieldException, IllegalAccessException {
    //初始化 AB Testing SDK
    final ISensorsABTest abTest = new SensorsABTest(abGlobalConfig);
//        Experiment<String> result = abTest.asyncFetchABTest(distinctId, false, experimentName, "grey", false);
    Experiment<Integer> result = abTest.asyncFetchABTest(distinctId, false, "12311111111111111111111111111111111111111111111", -1, true);
    System.out.println("======= " + result.getResult());
  }


  /**
   * 立即调用，默认值与返回值类型不一致，返回默认值，且试验相关字段信息返回为空，只返回默认信息
   */
  @Test
  public void asyncFetchABTestReturnDefaultValue() throws IOException, InvalidArgumentException, NoSuchFieldException, IllegalAccessException {
    //初始化 AB Testing SDK
    final ISensorsABTest abTest = new SensorsABTest(abGlobalConfig);

    String experimentName = "int";
    Experiment<String> result = abTest.asyncFetchABTest(distinctId, false, experimentName, "grey");
    System.out.println(" ==== result ==== " + result.getResult());

    assertEquals(distinctId, result.getDistinctId());
    assertEquals(false, result.getIsLoginId());
    assertNull(result.getAbTestExperimentGroupId());
    assertNull(result.getAbTestExperimentId());
    assertNull(result.getControlGroup());
    assertNull(result.getWhiteList());
    assertEquals("grey", result.getResult());

    //
    ExperimentCacheManager cacheManager =
            getExperimentCacheManagerByReflect(getSensorsABTestWorkerByReflect(abTest));

    assertEquals(0, cacheManager.getCacheSize());
  }

  /**
   * 立即调用，默认值与返回值类型不一致，返回默认值，且试验相关字段信息返回为空，只返回默认信息
   */
  @Test
  public void fastFetchABTestReturnDefaultValue() throws IOException, InvalidArgumentException, NoSuchFieldException, IllegalAccessException {
    //初始化 AB Testing SDK
    final ISensorsABTest abTest = new SensorsABTest(abGlobalConfig);

    String experimentName = "int";
    Experiment<String> result = abTest.fastFetchABTest(distinctId, false, experimentName, "grey");
    System.out.println(" ==== result ==== " + result.getResult());

    assertEquals(distinctId, result.getDistinctId());
    assertEquals(false, result.getIsLoginId());
    assertNull(result.getAbTestExperimentGroupId());
    assertNull(result.getAbTestExperimentId());
    assertNull(result.getControlGroup());
    assertNull(result.getWhiteList());
    assertEquals("grey", result.getResult());

    //
    ExperimentCacheManager cacheManager =
            getExperimentCacheManagerByReflect(getSensorsABTestWorkerByReflect(abTest));

    assertEquals(1, cacheManager.getCacheSize());
  }


  /**
   * 优先查缓存
   */
  @Test
  public void fastFetchABTestReturnResult() throws IOException, InvalidArgumentException {
    //初始化 AB Testing SDK
    final ISensorsABTest abTest = new SensorsABTest(abGlobalConfig);
    Experiment<String> result = abTest.fastFetchABTest(distinctId, false, experimentName, "grey");
    assertNotNull(result);
    assertNotNull(result.getDistinctId());
    assertNotNull(result.getIsLoginId());
    assertNotNull(result.getResult());
  }

  /**
   * 初始化正确后调用 fastFetchABTest 且试验结果缓存存储正确
   */
  @Test
  public void experimentCacheSizeTest()
          throws InvalidArgumentException, IOException, NoSuchFieldException, IllegalAccessException {
    ABGlobalConfig abGlobalConfig = ABGlobalConfig.builder()
            .setExperimentCacheSize(1)      //只缓存一条
            .setApiUrl(url)                //分流试验地址
            .setSensorsAnalytics(sa)       //神策分析 SDK 实例
            .build();
    final ISensorsABTest abTest = new SensorsABTest(abGlobalConfig);
    Experiment<String> s1 = abTest.fastFetchABTest("AB123", true, "int", "1");
    Experiment<String> s2 = abTest.fastFetchABTest("AB12234", true, "int", "2");
    ExperimentCacheManager cacheManager =
            getExperimentCacheManagerByReflect(getSensorsABTestWorkerByReflect(abTest));

    assertEquals(1, cacheManager.getCacheSize());
    System.out.println(cacheManager.getExperimentResultByCache("AB123", true, "int"));
    System.out.println(cacheManager.getExperimentResultByCache("AB12234", true, "int"));
  }

  /**
   * 初始化正确后调用 fastFetchABTest 且试验结果缓存存储正确，默认 ExperimentCacheSize 为 4096
   */
  @Test
  @Ignore
  public void experimentCacheSizeTest_01()
          throws InvalidArgumentException, IOException, NoSuchFieldException, IllegalAccessException {
    ABGlobalConfig abGlobalConfig = ABGlobalConfig.builder()
            .setApiUrl(url)                //分流试验地址
            .setSensorsAnalytics(sa)       //神策分析 SDK 实例
            .build();
    final ISensorsABTest abTest = new SensorsABTest(abGlobalConfig);

    for (int i = 0; i < 5000; i++){
      abTest.fastFetchABTest("AB12234_" + i, true, "int", "2");
    }
    ExperimentCacheManager cacheManager =
            getExperimentCacheManagerByReflect(getSensorsABTestWorkerByReflect(abTest));
    System.out.println(cacheManager.getExperimentResultByCache("AB123", true, "int"));
    System.out.println(cacheManager.getExperimentResultByCache("AB12234", true, "int"));
    assertEquals(4096, cacheManager.getCacheSize());
  }

  //TODO
  /**
   * 初始化正确后调用 fastFetchABTest 且试验结果缓存存储正确 setEventCacheTime
   */
  @Test
  public void experimentCacheSizeTest_02()
          throws InvalidArgumentException, IOException, NoSuchFieldException, IllegalAccessException, InterruptedException {
    ABGlobalConfig abGlobalConfig = ABGlobalConfig.builder()
            .setApiUrl(url)                //分流试验地址
            .setSensorsAnalytics(sa)       //神策分析 SDK 实例
            .setEventCacheTime(1)
            .build();
    final ISensorsABTest abTest = new SensorsABTest(abGlobalConfig);
    Experiment<Integer> s1 = abTest.fastFetchABTest("AB123", true, "int", -1);
    Thread.sleep(60000);
    Experiment<Integer> s2 = abTest.fastFetchABTest("AB12234", true, "int", -2);
    ExperimentCacheManager cacheManager =
            getExperimentCacheManagerByReflect(getSensorsABTestWorkerByReflect(abTest));
  }

  /**
   * 初始化正确后调用 fastFetchABTest 且试验结果缓存存储正确 setEventCacheSize
   */
  @Test@Ignore
  public void experimentCacheSizeTest_03()
          throws InvalidArgumentException, IOException, NoSuchFieldException, IllegalAccessException, InterruptedException {
    ABGlobalConfig abGlobalConfig = ABGlobalConfig.builder()
            .setApiUrl(url)                //分流试验地址
            .setSensorsAnalytics(sa)       //神策分析 SDK 实例
            .setEventCacheSize(100)
            .build();
    final ISensorsABTest abTest = new SensorsABTest(abGlobalConfig);
    EventCacheManager eventCacheManager =
            getEventCacheManagerByReflect(getSensorsABTestWorkerByReflect(abTest));

    for (int i = 0; i < 200; i++){
      Experiment<Integer> s2  = abTest.fastFetchABTest("AB12234_" + i, true, "int", -2);
    }

    ExperimentCacheManager cacheManager =
            getExperimentCacheManagerByReflect(getSensorsABTestWorkerByReflect(abTest));

    System.out.println("========= EventCacheSize:" + eventCacheManager.getCacheSize());
  }

  /**
   * 初始化正确后调用 setApiUrl=null
   */
  @Test
  public void setApiUrlTest_null()
          throws InvalidArgumentException, IOException, NoSuchFieldException, IllegalAccessException, InterruptedException {
    ABGlobalConfig abGlobalConfig = ABGlobalConfig.builder()
            .setApiUrl(null)                //分流试验地址
            .setSensorsAnalytics(sa)       //神策分析 SDK 实例
            .setEventCacheSize(100)
            .build();
    final ISensorsABTest abTest = new SensorsABTest(abGlobalConfig);
    EventCacheManager eventCacheManager =
            getEventCacheManagerByReflect(getSensorsABTestWorkerByReflect(abTest));

    Experiment<Integer> s2  = abTest.fastFetchABTest("AB12234", true, "int", -2);

    ExperimentCacheManager cacheManager =
            getExperimentCacheManagerByReflect(getSensorsABTestWorkerByReflect(abTest));

  }

  /**
   * 初始化正确后调用 setApiUrl=empty
   */
  @Test
  public void setApiUrlTest_empty()
          throws InvalidArgumentException, IOException, NoSuchFieldException, IllegalAccessException, InterruptedException {
    ABGlobalConfig abGlobalConfig = ABGlobalConfig.builder()
            .setApiUrl("")                //分流试验地址
            .setSensorsAnalytics(sa)       //神策分析 SDK 实例
            .setEventCacheSize(100)
            .build();
    final ISensorsABTest abTest = new SensorsABTest(abGlobalConfig);
    EventCacheManager eventCacheManager =
            getEventCacheManagerByReflect(getSensorsABTestWorkerByReflect(abTest));

    Experiment<Integer> s2  = abTest.fastFetchABTest("AB12234", true, "int", -2);

    ExperimentCacheManager cacheManager =
            getExperimentCacheManagerByReflect(getSensorsABTestWorkerByReflect(abTest));
  }

  /**
   * 初始化正确后调用 setApiUrl=empty
   */
  @Test
  public void setApiUrlTest_baidu()
          throws InvalidArgumentException, IOException, NoSuchFieldException, IllegalAccessException, InterruptedException {
    ABGlobalConfig abGlobalConfig = ABGlobalConfig.builder()
            .setApiUrl("www.baidu.com")                //分流试验地址
            .setSensorsAnalytics(sa)       //神策分析 SDK 实例
            .setEventCacheSize(100)
            .build();
    final ISensorsABTest abTest = new SensorsABTest(abGlobalConfig);
    EventCacheManager eventCacheManager =
            getEventCacheManagerByReflect(getSensorsABTestWorkerByReflect(abTest));

    Experiment<Integer> s2  = abTest.fastFetchABTest("AB12234", true, "int", -2);

    ExperimentCacheManager cacheManager =
            getExperimentCacheManagerByReflect(getSensorsABTestWorkerByReflect(abTest));
  }

  /**
   *  初始化不调用 setSensorsAnalytics
   */
  @Test
  public void setSensorsAnalytics_01()
          throws InvalidArgumentException, IOException, NoSuchFieldException, IllegalAccessException, InterruptedException {
    ABGlobalConfig abGlobalConfig = ABGlobalConfig.builder()
            .setApiUrl(url)                //分流试验地址
            .setEventCacheSize(100)
            .build();
    final ISensorsABTest abTest = new SensorsABTest(abGlobalConfig);
    EventCacheManager eventCacheManager =
            getEventCacheManagerByReflect(getSensorsABTestWorkerByReflect(abTest));

    Experiment<Integer> s2 = abTest.fastFetchABTest("AB12234", true, "int", -2);

    ExperimentCacheManager cacheManager =
            getExperimentCacheManagerByReflect(getSensorsABTestWorkerByReflect(abTest));
  }

  /**
   *  初始化调用 setSensorsAnalytics(null)
   */
  @Test
  public void setSensorsAnalytics_02()
          throws InvalidArgumentException, IOException, NoSuchFieldException, IllegalAccessException, InterruptedException {
    ABGlobalConfig abGlobalConfig = ABGlobalConfig.builder()
            .setApiUrl(url)                //分流试验地址
            .setSensorsAnalytics(null)
            .setEventCacheSize(100)
            .build();
    final ISensorsABTest abTest = new SensorsABTest(abGlobalConfig);
    EventCacheManager eventCacheManager =
            getEventCacheManagerByReflect(getSensorsABTestWorkerByReflect(abTest));

    Experiment<Integer> s2 = abTest.fastFetchABTest("AB12234", true, "int", -2);

    ExperimentCacheManager cacheManager =
            getExperimentCacheManagerByReflect(getSensorsABTestWorkerByReflect(abTest));
  }

  /**
   *  初始化调用两次 enableEventCache
   */
  @Test
  public void setSensorsAnalytics_03()
          throws InvalidArgumentException, IOException, NoSuchFieldException, IllegalAccessException, InterruptedException {
    ABGlobalConfig abGlobalConfig = ABGlobalConfig.builder()
            .setApiUrl(url)                //分流试验地址
            .setSensorsAnalytics(sa)
            .setEventCacheSize(100)
            .enableEventCache(true)
            .enableEventCache(false)
            .build();
    final ISensorsABTest abTest = new SensorsABTest(abGlobalConfig);
    EventCacheManager eventCacheManager =
            getEventCacheManagerByReflect(getSensorsABTestWorkerByReflect(abTest));

    Experiment<Integer> s2 = abTest.fastFetchABTest("AB12234", true, "int", -2);

    ExperimentCacheManager cacheManager =
            getExperimentCacheManagerByReflect(getSensorsABTestWorkerByReflect(abTest));

    System.out.println("======== CacheSize: " + eventCacheManager.getCacheSize());
    assertEquals(1, eventCacheManager.getCacheSize());
  }

  /**
   *  初始化先 enableEventCache(true) 再 enableEventCache(false)
   */
  @Test
  public void setSensorsAnalytics_04()
          throws InvalidArgumentException, IOException, NoSuchFieldException, IllegalAccessException, InterruptedException {
    ABGlobalConfig abGlobalConfig = ABGlobalConfig.builder()
            .setApiUrl(url)                //分流试验地址
            .setSensorsAnalytics(sa)
            .setEventCacheSize(100)
            .enableEventCache(true)
            .build();
    final ISensorsABTest abTest = new SensorsABTest(abGlobalConfig);
    EventCacheManager eventCacheManager =
            getEventCacheManagerByReflect(getSensorsABTestWorkerByReflect(abTest));

    Experiment<Integer> s1 = abTest.fastFetchABTest("AB12234", true, "int", -2);

    ExperimentCacheManager cacheManager =
            getExperimentCacheManagerByReflect(getSensorsABTestWorkerByReflect(abTest));

    System.out.println("======== CacheSize: " + eventCacheManager.getCacheSize());
    assertEquals(1, eventCacheManager.getCacheSize());

    /**
     *
     */
    abGlobalConfig = ABGlobalConfig.builder()
            .setApiUrl(url)                //分流试验地址
            .setSensorsAnalytics(sa)
            .setEventCacheSize(100)
            .enableEventCache(false)
            .build();
    final ISensorsABTest abTest01 = new SensorsABTest(abGlobalConfig);

    EventCacheManager eventCacheManager01 =
            getEventCacheManagerByReflect(getSensorsABTestWorkerByReflect(abTest));

    Experiment<Integer> s2 = abTest01.fastFetchABTest("AB12234_2", true, "int", -2);

    ExperimentCacheManager cacheManager01 =
            getExperimentCacheManagerByReflect(getSensorsABTestWorkerByReflect(abTest));

    System.out.println("======== CacheSize: " + eventCacheManager.getCacheSize());
    assertEquals(1, eventCacheManager.getCacheSize());
  }

  /**
   *  初始化先 enableEventCache(true) 再 enableEventCache(false)
   */
  @Test
  public void setSensorsAnalytics_05()
          throws InvalidArgumentException, IOException, NoSuchFieldException, IllegalAccessException, InterruptedException {
    ABGlobalConfig abGlobalConfig = ABGlobalConfig.builder()
            .setApiUrl(url)                //分流试验地址
            .setSensorsAnalytics(sa)
            .setEventCacheSize(100)
            .enableEventCache(true)
            .build();
    final ISensorsABTest abTest = new SensorsABTest(abGlobalConfig);
    EventCacheManager eventCacheManager =
            getEventCacheManagerByReflect(getSensorsABTestWorkerByReflect(abTest));

    Experiment<Integer> s1 = abTest.fastFetchABTest("AB12234", true, "int", -2);

    ExperimentCacheManager cacheManager =
            getExperimentCacheManagerByReflect(getSensorsABTestWorkerByReflect(abTest));

    System.out.println("======== CacheSize: " + eventCacheManager.getCacheSize());
    assertEquals(1, eventCacheManager.getCacheSize());
  }

  /**
   *  初始化先 abGlobalConfig
   */
  @Test
  public void setSensorsAnalytics_06()
          throws InvalidArgumentException, IOException, NoSuchFieldException, IllegalAccessException, InterruptedException {
    ABGlobalConfig abGlobalConfig = ABGlobalConfig.builder()
            .setApiUrl(url)                //分流试验地址
            .setSensorsAnalytics(sa)
            .setEventCacheSize(100)
            .enableEventCache(true)
            .build();

    final ISensorsABTest abTest = new SensorsABTest(abGlobalConfig);
    EventCacheManager eventCacheManager =
            getEventCacheManagerByReflect(getSensorsABTestWorkerByReflect(abTest));

    Experiment<Integer> s1 = abTest.fastFetchABTest("AB12234", true, "int", -2);

    ExperimentCacheManager cacheManager =
            getExperimentCacheManagerByReflect(getSensorsABTestWorkerByReflect(abTest));

    System.out.println("======== CacheSize: " + eventCacheManager.getCacheSize());
    assertEquals(1, eventCacheManager.getCacheSize());
  }

  /**
   *  初始化先 abGlobalConfig
   */
  @Test
  public void asyncFetchABTest_07()
          throws InvalidArgumentException, IOException, NoSuchFieldException, IllegalAccessException, InterruptedException {
    ABGlobalConfig abGlobalConfig = ABGlobalConfig.builder()
            .setApiUrl(url)                //分流试验地址
            .setSensorsAnalytics(sa)
            .setEventCacheSize(100)
            .enableEventCache(true)
            .build();

    final ISensorsABTest abTest = new SensorsABTest(abGlobalConfig);
    EventCacheManager eventCacheManager =
            getEventCacheManagerByReflect(getSensorsABTestWorkerByReflect(abTest));

    Experiment<Integer> s1 = abTest.asyncFetchABTest("AB12234", true, "int", -2);

    ExperimentCacheManager cacheManager =
            getExperimentCacheManagerByReflect(getSensorsABTestWorkerByReflect(abTest));

    System.out.println("======== CacheSize: " + eventCacheManager.getCacheSize());
    assertEquals(1, eventCacheManager.getCacheSize());
  }

  /**
   * 开启事件缓存，asyncFetchABTest 验证事件缓存存储正确
   */
  @Test
  public void openEventCacheAsyncTest()
          throws IOException, InvalidArgumentException, NoSuchFieldException, IllegalAccessException {
    final ISensorsABTest abTest = new SensorsABTest(abGlobalConfig);
    Experiment<String> result = abTest.asyncFetchABTest("AB123", true, "btn_type", "eee");
    EventCacheManager eventCacheManager = getEventCacheManagerByReflect(getSensorsABTestWorkerByReflect(abTest));
    assertEquals(1, eventCacheManager.getCacheSize());
  }

  /**
   * 关闭事件缓存，asyncFetchABTest 验证事件缓存
   */
  @Test
  public void closeEventCacheAsyncTest()
          throws InvalidArgumentException, IOException, NoSuchFieldException, IllegalAccessException {
    ABGlobalConfig abGlobalConfig = ABGlobalConfig.builder()
            .enableEventCache(false)        //关闭事件缓存
            .setApiUrl(url)                //分流试验地址
            .setSensorsAnalytics(sa)       //神策分析 SDK 实例
            .build();
    final ISensorsABTest abTest = new SensorsABTest(abGlobalConfig);
    Experiment<String> result = abTest.asyncFetchABTest("AB123", true, "btn_type", "eee");
    EventCacheManager eventCacheManager = getEventCacheManagerByReflect(getSensorsABTestWorkerByReflect(abTest));
    assertEquals(0, eventCacheManager.getCacheSize());
  }

  /**
   * 开启事件缓存，fastFetchABTest 验证事件缓存存储正确
   */
  @Test
  public void openEventCacheFastTest()
          throws IOException, InvalidArgumentException, NoSuchFieldException, IllegalAccessException {
    final ISensorsABTest abTest = new SensorsABTest(abGlobalConfig);
    Experiment<String> result = abTest.fastFetchABTest("AB123", true, "btn_type", "eee");
    EventCacheManager eventCacheManager = getEventCacheManagerByReflect(getSensorsABTestWorkerByReflect(abTest));
    assertEquals(1, eventCacheManager.getCacheSize());
  }

  /**
   * 关闭事件缓存，fastFetchABTest 验证事件缓存
   */
  @Test
  public void closeEventCacheFastTest()
          throws InvalidArgumentException, IOException, NoSuchFieldException, IllegalAccessException {
    ABGlobalConfig abGlobalConfig = ABGlobalConfig.builder()
            .enableEventCache(false)        //关闭事件缓存
            .setApiUrl(url)                //分流试验地址
            .setSensorsAnalytics(sa)       //神策分析 SDK 实例
            .build();
    final ISensorsABTest abTest = new SensorsABTest(abGlobalConfig);
    Experiment<String> result = abTest.fastFetchABTest("AB123", true, "btn_type", "eee");
    EventCacheManager eventCacheManager = getEventCacheManagerByReflect(getSensorsABTestWorkerByReflect(abTest));
    assertEquals(0, eventCacheManager.getCacheSize());
  }

  /**
   * 调用两次fastFetchABTest，验证第二次请求是否进行网络请求
   */
  @Test
  public void experimentCacheTest()
          throws InvalidArgumentException, IOException, NoSuchFieldException, IllegalAccessException {
    ABGlobalConfig abGlobalConfig = ABGlobalConfig.builder()
            .setApiUrl(url)                //分流试验地址
            .setSensorsAnalytics(sa)       //神策分析 SDK 实例
            .build();
    final ISensorsABTest abTest = new SensorsABTest(abGlobalConfig);
    Experiment<String> s1 = abTest.fastFetchABTest("AB123", true, "btn_type", "eee", false);
    ExperimentCacheManager cacheManager =
            getExperimentCacheManagerByReflect(getSensorsABTestWorkerByReflect(abTest));
    assertEquals(1, cacheManager.getCacheSize());
    System.out.println(cacheManager.getExperimentResultByCache("AB123", true, "btn_type"));
    Experiment<String> s2 = abTest.fastFetchABTest("AB123", true, "btn_type", "eee", false);
  }

  /**
   * 调用两次 asyncFetchABTest，验证 AsyncFetchABTest 不会存实验缓存
   */
  @Test
  public void experimentCacheTestFast()
          throws InvalidArgumentException, IOException, NoSuchFieldException, IllegalAccessException {
    ABGlobalConfig abGlobalConfig = ABGlobalConfig.builder()
            .setApiUrl(url)                //分流试验地址
            .setSensorsAnalytics(sa)       //神策分析 SDK 实例
            .build();
    final ISensorsABTest abTest = new SensorsABTest(abGlobalConfig);
    Experiment<Integer> s1 = abTest.fastFetchABTest("AB123_00", true, "int", -111);
    ExperimentCacheManager cacheManager =
            getExperimentCacheManagerByReflect(getSensorsABTestWorkerByReflect(abTest));
    assertEquals(1, cacheManager.getCacheSize());
    System.out.println(cacheManager.getExperimentResultByCache("AB123_00", true, "int"));

    Experiment<Integer> s2 = abTest.fastFetchABTest("AB123_01", true, "int", -222);
  }

  /**
   * 调用两次 asyncFetchABTest，验证 AsyncFetchABTest 不会存实验缓存
   */
  @Test
  public void experimentCacheTestAsync()
          throws InvalidArgumentException, IOException, NoSuchFieldException, IllegalAccessException {
    ABGlobalConfig abGlobalConfig = ABGlobalConfig.builder()
            .setApiUrl(url)                //分流试验地址
            .setSensorsAnalytics(sa)       //神策分析 SDK 实例
            .build();
    final ISensorsABTest abTest = new SensorsABTest(abGlobalConfig);
    Experiment<Integer> s1 = abTest.asyncFetchABTest("AB123_00", true, "int", -111);
    ExperimentCacheManager cacheManager =
            getExperimentCacheManagerByReflect(getSensorsABTestWorkerByReflect(abTest));
    assertEquals(0, cacheManager.getCacheSize());

    System.out.println(cacheManager.getExperimentResultByCache("AB123_00", true, "int"));
    Experiment<Integer> s2 = abTest.asyncFetchABTest("AB123_01", true, "int", -222);
  }

  /**
   * fastFetchABTest 设置不自动上报事件，手动触发
   */
  @Test
  public void selfTriggerEventByFast()
          throws IOException, InvalidArgumentException, NoSuchFieldException, IllegalAccessException {
    final ISensorsABTest abTest = new SensorsABTest(abGlobalConfig);
    Experiment<String> result = abTest.fastFetchABTest("AB123", true, "btn_type", "eee", false);
    EventCacheManager eventCacheManager = getEventCacheManagerByReflect(getSensorsABTestWorkerByReflect(abTest));
    assertEquals(0, eventCacheManager.getCacheSize());
    abTest.trackABTestTrigger(result);
    //查看当前项目路径下是否存在日志文件，以及里面的日志信息
  }

  /**
   * 测试试验结果缓存过期时间
   * cache清除内存没有使用额外的线程，所以可能不会立即删除，但是用户get操作已经获取不到值，下一次get操作就会清理内存。
   */
  @Test
  public void experimentCacheTimeTest()
          throws InvalidArgumentException, IOException, NoSuchFieldException, IllegalAccessException,
          InterruptedException {
    ABGlobalConfig abGlobalConfig = ABGlobalConfig.builder()
            .setExperimentCacheTime(1)      //缓存有效期分钟
            .setApiUrl(url)                //分流试验地址
            .setSensorsAnalytics(sa)       //神策分析 SDK 实例
            .build();
    final ISensorsABTest abTest = new SensorsABTest(abGlobalConfig);
    Experiment<String> s1 = abTest.fastFetchABTest("AB123", false, "color", "1");
    ExperimentCacheManager cacheManager =
            getExperimentCacheManagerByReflect(getSensorsABTestWorkerByReflect(abTest));
    Thread.sleep(60000);
    assertNull(cacheManager.getExperimentResultByCache("AB123", false, "color"));
    assertEquals(0, cacheManager.getCacheSize());

    abTest.fastFetchABTest("AB123", false, "color", "1");
  }

  /**
   * 测试试验结果缓存过期时间
   * cache清除内存没有使用额外的线程，所以可能不会立即删除，但是用户get操作已经获取不到值，下一次get操作就会清理内存。
   */
  @Test
  public void experimentCacheTimeTestDefault()
          throws InvalidArgumentException, IOException, NoSuchFieldException, IllegalAccessException,
          InterruptedException {
    ABGlobalConfig abGlobalConfig = ABGlobalConfig.builder()
            .setApiUrl(url)                //分流试验地址
            .setSensorsAnalytics(sa)       //神策分析 SDK 实例
            .build();
    final ISensorsABTest abTest = new SensorsABTest(abGlobalConfig);
    Experiment<String> s1 = abTest.fastFetchABTest("AB123", false, "color", "1");
    ExperimentCacheManager cacheManager =
            getExperimentCacheManagerByReflect(getSensorsABTestWorkerByReflect(abTest));
    assertEquals(1440, (int)abGlobalConfig.getExperimentCacheTime());
    System.out.print("======= cacheTime: " + abGlobalConfig.getExperimentCacheTime());
  }

  /**
   * 测试试验结果缓存过期时间
   * cache清除内存没有使用额外的线程，所以可能不会立即删除，但是用户get操作已经获取不到值，下一次get操作就会清理内存。
   */
  @Test
  public void experimentCacheTimeTestDefault01()
          throws InvalidArgumentException, IOException, NoSuchFieldException, IllegalAccessException,
          InterruptedException {
    ABGlobalConfig abGlobalConfig = ABGlobalConfig.builder()
            .setExperimentCacheTime(-1)      //缓存有效期分钟
            .setApiUrl(url)                //分流试验地址
            .setSensorsAnalytics(sa)       //神策分析 SDK 实例
            .build();
    final ISensorsABTest abTest = new SensorsABTest(abGlobalConfig);
    Experiment<String> s1 = abTest.fastFetchABTest("AB123", false, "color", "1");
    ExperimentCacheManager cacheManager =
            getExperimentCacheManagerByReflect(getSensorsABTestWorkerByReflect(abTest));
    assertEquals(1440, (int)abGlobalConfig.getExperimentCacheTime());
    System.out.print("======= cacheTime: " + abGlobalConfig.getExperimentCacheTime());
  }

  /**
   * 测试试验结果缓存过期时间
   * cache清除内存没有使用额外的线程，所以可能不会立即删除，但是用户get操作已经获取不到值，下一次get操作就会清理内存。
   */
  @Test
  public void experimentCacheTimeTestDefault02()
          throws InvalidArgumentException, IOException, NoSuchFieldException, IllegalAccessException,
          InterruptedException {
    ABGlobalConfig abGlobalConfig = ABGlobalConfig.builder()
            .setExperimentCacheTime(1439)      //缓存有效期分钟
            .setApiUrl(url)                //分流试验地址
            .setSensorsAnalytics(sa)       //神策分析 SDK 实例
            .build();
    final ISensorsABTest abTest = new SensorsABTest(abGlobalConfig);
    Experiment<String> s1 = abTest.fastFetchABTest("AB123", false, "color", "1");
    ExperimentCacheManager cacheManager =
            getExperimentCacheManagerByReflect(getSensorsABTestWorkerByReflect(abTest));
    assertEquals(1440, (int)abGlobalConfig.getExperimentCacheTime());
    System.out.print("======= cacheTime: " + abGlobalConfig.getExperimentCacheTime());
  }

  /**
   * 测试试验结果缓存过期时间
   * cache清除内存没有使用额外的线程，所以可能不会立即删除，但是用户get操作已经获取不到值，下一次get操作就会清理内存。
   */
  @Test
  public void experimentCacheTimeTestDefault03()
          throws InvalidArgumentException, IOException, NoSuchFieldException, IllegalAccessException,
          InterruptedException {
    ABGlobalConfig abGlobalConfig = ABGlobalConfig.builder()
            .setExperimentCacheTime(null)      //缓存有效期分钟
            .setApiUrl(url)                //分流试验地址
            .setSensorsAnalytics(sa)       //神策分析 SDK 实例
            .build();
    final ISensorsABTest abTest = new SensorsABTest(abGlobalConfig);
    Experiment<String> s1 = abTest.fastFetchABTest("AB123", false, "color", "1");
    ExperimentCacheManager cacheManager =
            getExperimentCacheManagerByReflect(getSensorsABTestWorkerByReflect(abTest));
    assertEquals(1440, (int)abGlobalConfig.getExperimentCacheTime());
    System.out.print("======= cacheTime: " + abGlobalConfig.getExperimentCacheTime());
  }

  /**
   * asyncFetchABTest 请求携带预制属性
   */
  @Test
  public void setPropertiesByAsyncTest() throws IOException, InvalidArgumentException, NoSuchFieldException, IllegalAccessException {
    final ISensorsABTest abTest = new SensorsABTest(abGlobalConfig);
    Map<String, Object> properties = new HashMap<>();
    properties.put("$os", "windows");
    String experimentName = "test_str";
    Experiment<String> result = abTest.asyncFetchABTest(distinctId, false, experimentName, "...", properties);

    System.out.println("======= result: " + result.getResult());

    assertEquals(distinctId, result.getDistinctId());
    assertEquals(false, result.getIsLoginId());
    assertEquals("102",result.getAbTestExperimentId());
    assertEquals("0",result.getAbTestExperimentGroupId());
    Assert.assertTrue(result.getControlGroup());
    Assert.assertTrue(result.getWhiteList());

    //
    ExperimentCacheManager cacheManager =
            getExperimentCacheManagerByReflect(getSensorsABTestWorkerByReflect(abTest));

    assertEquals(0, cacheManager.getCacheSize());

    assertNotNull(result.getResult());
  }

  /**
   * fastFetchABTest 请求携带预制属性
   */
  @Test
  public void setPropertiesByFastTest() throws IOException, InvalidArgumentException, NoSuchFieldException, IllegalAccessException {
    final ISensorsABTest abTest = new SensorsABTest(abGlobalConfig);
    Map<String, Object> properties = new HashMap<>();
    properties.put("$os", "windows");

    String experimentName = "test_str";
    Experiment<String> result = abTest.fastFetchABTest(distinctId, false, experimentName, "...", properties);
    System.out.println("======= result: " + result.getResult());

    assertEquals(distinctId, result.getDistinctId());
    assertEquals(false, result.getIsLoginId());
    assertEquals("102", result.getAbTestExperimentId());
    assertEquals("0", result.getAbTestExperimentGroupId());
    Assert.assertTrue(result.getControlGroup());
    Assert.assertTrue(result.getWhiteList());

    //
    ExperimentCacheManager cacheManager =
            getExperimentCacheManagerByReflect(getSensorsABTestWorkerByReflect(abTest));

    assertEquals(1, cacheManager.getCacheSize());
    assertNotNull(result.getResult());
  }

  /**
   * asyncFetchABTest 请求携带预制属性
   */
  @Test
  public void setPropertiesByAsyncTest01() throws IOException, InvalidArgumentException, NoSuchFieldException, IllegalAccessException {
    final ISensorsABTest abTest = new SensorsABTest(abGlobalConfig);
    Map<String, Object> properties = new HashMap<>();
    properties.put("test", "test");
    String experimentName = "test_str";
    Experiment<String> result = abTest.asyncFetchABTest(distinctId, false, experimentName, "...", properties);

    System.out.println("======= result: " + result.getResult());

    assertEquals(distinctId, result.getDistinctId());
    assertEquals(false, result.getIsLoginId());
    assertEquals("102",result.getAbTestExperimentId());
    assertEquals("0",result.getAbTestExperimentGroupId());
    Assert.assertTrue(result.getControlGroup());
    Assert.assertTrue(result.getWhiteList());

    //
    ExperimentCacheManager cacheManager =
            getExperimentCacheManagerByReflect(getSensorsABTestWorkerByReflect(abTest));

    assertEquals(0, cacheManager.getCacheSize());

    assertNotNull(result.getResult());
  }

  /**
   * fastFetchABTest 请求携带预制属性
   */
  @Test
  public void setPropertiesByFastTest01() throws IOException, InvalidArgumentException, NoSuchFieldException, IllegalAccessException {
    final ISensorsABTest abTest = new SensorsABTest(abGlobalConfig);
    Map<String, Object> properties = new HashMap<>();
    properties.put("test", "test");

    String experimentName = "test_str";
    Experiment<String> result = abTest.fastFetchABTest(distinctId, false, experimentName, "...", properties);
    System.out.println("======= result: " + result.getResult());

    assertEquals(distinctId, result.getDistinctId());
    assertEquals(false, result.getIsLoginId());
    assertEquals("102", result.getAbTestExperimentId());
    assertEquals("0", result.getAbTestExperimentGroupId());
    Assert.assertTrue(result.getControlGroup());
    Assert.assertTrue(result.getWhiteList());

    //
    ExperimentCacheManager cacheManager =
            getExperimentCacheManagerByReflect(getSensorsABTestWorkerByReflect(abTest));

    assertEquals(1, cacheManager.getCacheSize());
    assertNotNull(result.getResult());
  }

  /**
   * asyncFetchABTest 请求携带预制属性
   */
  @Test
  public void setPropertiesByAsyncTestNull() throws IOException, InvalidArgumentException, NoSuchFieldException, IllegalAccessException {
    final ISensorsABTest abTest = new SensorsABTest(abGlobalConfig);
    Map<String, Object> properties = null;
    String experimentName = "test_str";
    Experiment<String> result = abTest.asyncFetchABTest(distinctId, false, experimentName, "...", properties);

    System.out.println("======= result: " + result.getResult());

    assertEquals(distinctId, result.getDistinctId());
    assertEquals(false, result.getIsLoginId());
    assertEquals("102",result.getAbTestExperimentId());
    assertEquals("0",result.getAbTestExperimentGroupId());
    Assert.assertTrue(result.getControlGroup());
    Assert.assertTrue(result.getWhiteList());

    //
    ExperimentCacheManager cacheManager =
            getExperimentCacheManagerByReflect(getSensorsABTestWorkerByReflect(abTest));

    assertEquals(0, cacheManager.getCacheSize());

    assertNotNull(result.getResult());
  }

  /**
   * fastFetchABTest 请求携带预制属性
   */
  @Test
  public void setPropertiesByFastTestNull() throws IOException, InvalidArgumentException, NoSuchFieldException, IllegalAccessException {
    final ISensorsABTest abTest = new SensorsABTest(abGlobalConfig);
    Map<String, Object> properties = null;

    String experimentName = "test_str";
    Experiment<String> result = abTest.fastFetchABTest(distinctId, false, experimentName, "...", properties);
    System.out.println("======= result: " + result.getResult());

    assertEquals(distinctId, result.getDistinctId());
    assertEquals(false, result.getIsLoginId());
    assertEquals("102", result.getAbTestExperimentId());
    assertEquals("0", result.getAbTestExperimentGroupId());
    Assert.assertTrue(result.getControlGroup());
    Assert.assertTrue(result.getWhiteList());

    //
    ExperimentCacheManager cacheManager =
            getExperimentCacheManagerByReflect(getSensorsABTestWorkerByReflect(abTest));

    assertEquals(1, cacheManager.getCacheSize());
    assertNotNull(result.getResult());
  }

  /**
   * asyncFetchABTest 请求携带预制属性
   */
  @Test
  public void setPropertiesByAsyncTestNull01() throws IOException, InvalidArgumentException, NoSuchFieldException, IllegalAccessException {
    final ISensorsABTest abTest = new SensorsABTest(abGlobalConfig);
    Map<String, Object> properties = new HashMap<>();
    String experimentName = "test_str";
    Experiment<String> result = abTest.asyncFetchABTest(distinctId, false, experimentName, "...", properties);

    System.out.println("======= result: " + result.getResult());

    assertEquals(distinctId, result.getDistinctId());
    assertEquals(false, result.getIsLoginId());
    assertEquals("102",result.getAbTestExperimentId());
    assertEquals("0",result.getAbTestExperimentGroupId());
    Assert.assertTrue(result.getControlGroup());
    Assert.assertTrue(result.getWhiteList());

    //
    ExperimentCacheManager cacheManager =
            getExperimentCacheManagerByReflect(getSensorsABTestWorkerByReflect(abTest));

    assertEquals(0, cacheManager.getCacheSize());

    assertNotNull(result.getResult());
  }

  /**
   * fastFetchABTest 请求携带预制属性
   */
  @Test
  public void setPropertiesByFastTestNull01() throws IOException, InvalidArgumentException, NoSuchFieldException, IllegalAccessException {
    final ISensorsABTest abTest = new SensorsABTest(abGlobalConfig);
    Map<String, Object> properties = null;

    String experimentName = "test_str";
    Experiment<String> result = abTest.fastFetchABTest(distinctId, false, experimentName, "...", properties);
    System.out.println("======= result: " + result.getResult());

    assertEquals(distinctId, result.getDistinctId());
    assertEquals(false, result.getIsLoginId());
    assertEquals("102", result.getAbTestExperimentId());
    assertEquals("0", result.getAbTestExperimentGroupId());
    Assert.assertTrue(result.getControlGroup());
    Assert.assertTrue(result.getWhiteList());

    //
    ExperimentCacheManager cacheManager =
            getExperimentCacheManagerByReflect(getSensorsABTestWorkerByReflect(abTest));

    assertEquals(1, cacheManager.getCacheSize());
    assertNotNull(result.getResult());
  }

  /**
   * asyncFetchABTest 请求携带预制属性
   */
  @Test
  public void setPropertiesByAsyncTest02() throws IOException, InvalidArgumentException, NoSuchFieldException, IllegalAccessException {
    final ISensorsABTest abTest = new SensorsABTest(abGlobalConfig);
    Map<String, Object> properties = new HashMap<>();

    String experimentName = "test_str";
    Experiment<String> result = abTest.asyncFetchABTest(distinctId, false, experimentName, "...", properties);

    System.out.println("======= result: " + result.getResult());

    assertEquals(distinctId, result.getDistinctId());
    assertEquals(false, result.getIsLoginId());
    assertEquals("102",result.getAbTestExperimentId());
    assertEquals("0",result.getAbTestExperimentGroupId());
    Assert.assertTrue(result.getControlGroup());
    Assert.assertTrue(result.getWhiteList());

    //
    ExperimentCacheManager cacheManager =
            getExperimentCacheManagerByReflect(getSensorsABTestWorkerByReflect(abTest));

    assertEquals(0, cacheManager.getCacheSize());

    assertNotNull(result.getResult());
  }

  /**
   * fastFetchABTest 请求携带预制属性
   */
  @Test
  public void setPropertiesByFastTest02() throws IOException, InvalidArgumentException, NoSuchFieldException, IllegalAccessException {
    final ISensorsABTest abTest = new SensorsABTest(abGlobalConfig);
    Map<String, Object> properties = new HashMap<>();
    properties.put("test", null);

    String experimentName = "test_str";
    Experiment<String> result = abTest.fastFetchABTest(distinctId, false, experimentName, "...", properties);
    System.out.println("======= result: " + result.getResult());

    assertEquals(distinctId, result.getDistinctId());
    assertEquals(false, result.getIsLoginId());
    assertEquals("102", result.getAbTestExperimentId());
    assertEquals("0", result.getAbTestExperimentGroupId());
    Assert.assertTrue(result.getControlGroup());
    Assert.assertTrue(result.getWhiteList());

    //
    ExperimentCacheManager cacheManager =
            getExperimentCacheManagerByReflect(getSensorsABTestWorkerByReflect(abTest));

    assertEquals(1, cacheManager.getCacheSize());
    assertNotNull(result.getResult());
  }

  /**
   * 验证配置有效值后，初始化正确后调用 AsyncFetchABTest 且缓存不存储
   */
  @Test
  public void asyncFetchABTestEnableAutoTrackEventTrue() throws InvalidArgumentException, IOException, NoSuchFieldException, IllegalAccessException{
    //初始化 AB Testing SDK
    final ISensorsABTest abTest = new SensorsABTest(abGlobalConfig);
//        Experiment<String> result = abTest.asyncFetchABTest(distinctId, false, experimentName, "grey", false);
    EventCacheManager eventCacheManager =
            getEventCacheManagerByReflect(getSensorsABTestWorkerByReflect(abTest));

    Boolean isEventCacheExist = eventCacheManager.judgeEventCacheExist(distinctId, experimentName);
    System.out.println("======= isEventCacheExist: " + isEventCacheExist);

    String experimentName = "int";
    String distinctId = "xc123";

    Experiment<Integer> result = abTest.asyncFetchABTest(distinctId, false, experimentName, -1, true);
    System.out.println("======= result: " + result.getResult());

    assertEquals(distinctId, result.getDistinctId());
    assertEquals(false, result.getIsLoginId());
    assertEquals("115",result.getAbTestExperimentId());
    assertEquals("0",result.getAbTestExperimentGroupId());
    Assert.assertTrue(result.getControlGroup());
    Assert.assertFalse(result.getWhiteList());

    ExperimentCacheManager cacheManager =
            getExperimentCacheManagerByReflect(getSensorsABTestWorkerByReflect(abTest));

    assertEquals(0, cacheManager.getCacheSize());

    assertNotNull(result.getResult());
  }

  /**
   * 验证配置有效值后，初始化正确后调用 AsyncFetchABTest 且缓存不存储
   */
  @Test
  public void asyncFetchABTestEnableAutoTrackEventFalse() throws InvalidArgumentException, IOException, NoSuchFieldException, IllegalAccessException{
    //初始化 AB Testing SDK
    final ISensorsABTest abTest = new SensorsABTest(abGlobalConfig);
//        Experiment<String> result = abTest.asyncFetchABTest(distinctId, false, experimentName, "grey", false);
    EventCacheManager eventCacheManager =
            getEventCacheManagerByReflect(getSensorsABTestWorkerByReflect(abTest));

    Boolean isEventCacheExist = eventCacheManager.judgeEventCacheExist(distinctId, experimentName);
    System.out.println("======= isEventCacheExist: " + isEventCacheExist);

    String experimentName = "int";
    String distinctId = "xc123";

    Experiment<Integer> result = abTest.asyncFetchABTest(distinctId, false, experimentName, -1, false);
    System.out.println("======= result: " + result.getResult());

    assertEquals(distinctId, result.getDistinctId());
    assertEquals(false, result.getIsLoginId());
    assertEquals("115",result.getAbTestExperimentId());
    assertEquals("0",result.getAbTestExperimentGroupId());
    Assert.assertTrue(result.getControlGroup());
    Assert.assertFalse(result.getWhiteList());

    ExperimentCacheManager cacheManager =
            getExperimentCacheManagerByReflect(getSensorsABTestWorkerByReflect(abTest));

    assertEquals(0, cacheManager.getCacheSize());

    assertNotNull(result.getResult());
  }

  /**
   * 验证配置有效值后，初始化正确后调用 fastFetchABTest 且缓存不存储
   */
  @Test
  public void fastFetchABTestEnableAutoTrackEventTrue() throws InvalidArgumentException, IOException, NoSuchFieldException, IllegalAccessException{
    //初始化 AB Testing SDK
    final ISensorsABTest abTest = new SensorsABTest(abGlobalConfig);
//        Experiment<String> result = abTest.asyncFetchABTest(distinctId, false, experimentName, "grey", false);
    String experimentName = "int";
    String distinctId = "xc123";

    Experiment<Integer> result = abTest.fastFetchABTest(distinctId, false, experimentName, -1, true);
    System.out.println("======= result: " + result.getResult());

    assertEquals(distinctId, result.getDistinctId());
    assertEquals(false, result.getIsLoginId());
    assertEquals("115",result.getAbTestExperimentId());
    assertEquals("0",result.getAbTestExperimentGroupId());
    Assert.assertTrue(result.getControlGroup());
    Assert.assertFalse(result.getWhiteList());

    //
    ExperimentCacheManager cacheManager =
            getExperimentCacheManagerByReflect(getSensorsABTestWorkerByReflect(abTest));

    assertEquals(1, cacheManager.getCacheSize());

    assertNotNull(result.getResult());
  }

  /**
   * 验证配置有效值后，初始化正确后调用 fastFetchABTest 且缓存不存储
   */
  @Test
  public void fastFetchABTestEnableAutoTrackEventFalse() throws InvalidArgumentException, IOException, NoSuchFieldException, IllegalAccessException{
    //初始化 AB Testing SDK
    final ISensorsABTest abTest = new SensorsABTest(abGlobalConfig);
//        Experiment<String> result = abTest.asyncFetchABTest(distinctId, false, experimentName, "grey", false);
    String experimentName = "int";
    String distinctId = "xc123";

    Experiment<Integer> result = abTest.fastFetchABTest(distinctId, false, experimentName, -1, false);
    System.out.println("======= result: " + result.getResult());

    assertEquals(distinctId, result.getDistinctId());
    assertEquals(false, result.getIsLoginId());
    assertEquals("115",result.getAbTestExperimentId());
    assertEquals("0",result.getAbTestExperimentGroupId());
    Assert.assertTrue(result.getControlGroup());
    Assert.assertFalse(result.getWhiteList());

    //
    ExperimentCacheManager cacheManager =
            getExperimentCacheManagerByReflect(getSensorsABTestWorkerByReflect(abTest));

    assertEquals(1, cacheManager.getCacheSize());

    assertNotNull(result.getResult());
  }


  /**
   * 验证 $ABTestTrigger 事件缓存
   */
  @Test
  public void asyncFetchABTestAssertEventCache() throws InvalidArgumentException, IOException, NoSuchFieldException, IllegalAccessException{
    //初始化 AB Testing SDK

    final ISensorsABTest abTest = new SensorsABTest(abGlobalConfig);
//        Experiment<String> result = abTest.asyncFetchABTest(distinctId, false, experimentName, "grey", false);
    EventCacheManager eventCacheManager =
            getEventCacheManagerByReflect(getSensorsABTestWorkerByReflect(abTest));

    assertEquals(0, eventCacheManager.getCacheSize());

    String experimentName = "int";
    String distinctId = "xc123";

    Experiment<Integer> result = abTest.asyncFetchABTest(distinctId, false, experimentName, -1, true);
    System.out.println("======= result: " + result.getResult());
    assertTrue(eventCacheManager.judgeEventCacheExist(distinctId, result.getAbTestExperimentId()));

    result = abTest.asyncFetchABTest(distinctId, false, experimentName, -1, true);
    System.out.println("======= result: " + result.getResult());
  }

  /**
   * 验证配置有效值后，初始化正确后调用 fastFetchABTest 且缓存不存储
   */
  @Test
  public void fastFetchABTestAssertEventCache() throws InvalidArgumentException, IOException, NoSuchFieldException, IllegalAccessException{
    //初始化 AB Testing SDK
    final ISensorsABTest abTest = new SensorsABTest(abGlobalConfig);
//        Experiment<String> result = abTest.asyncFetchABTest(distinctId, false, experimentName, "grey", false);
    String experimentName = "int";
    String distinctId = "xc123";

    EventCacheManager eventCacheManager =
            getEventCacheManagerByReflect(getSensorsABTestWorkerByReflect(abTest));

    Experiment<Integer> result = abTest.fastFetchABTest(distinctId, false, experimentName, -1, true);
    System.out.println("======= result: " + result.getResult());

    result = abTest.fastFetchABTest(distinctId, false, experimentName, -1, true);
    System.out.println("======= result: " + result.getResult());
    assertTrue(eventCacheManager.judgeEventCacheExist(distinctId, result.getAbTestExperimentId()));
  }

  /**
   * 验证清除 $ABTestTrigger 事件缓存，可以触发事件
   */
  @Test
  public void asyncFetchABTestinvalidateEventCache() throws InvalidArgumentException, IOException, NoSuchFieldException, IllegalAccessException{
    //初始化 AB Testing SDK

    final ISensorsABTest abTest = new SensorsABTest(abGlobalConfig);
//        Experiment<String> result = abTest.asyncFetchABTest(distinctId, false, experimentName, "grey", false);
    EventCacheManager eventCacheManager =
            getEventCacheManagerByReflect(getSensorsABTestWorkerByReflect(abTest));

    assertEquals(0, eventCacheManager.getCacheSize());

    String experimentName = "int";
    String distinctId = "xc123";

    Experiment<Integer> result = abTest.asyncFetchABTest(distinctId, false, experimentName, -1, true);
    System.out.println("======= result: " + result.getResult());
    assertTrue(eventCacheManager.judgeEventCacheExist(distinctId, result.getAbTestExperimentId()));

    getEventCacheByReflect(eventCacheManager).invalidateAll(); //强制清除缓存

    result = abTest.asyncFetchABTest(distinctId, false, experimentName, -1, true);
    System.out.println("======= result: " + result.getResult());
  }

  /**
   * 验证清除 $ABTestTrigger 事件缓存，可以触发事件
   */
  @Test
  public void fastFetchABTestEventnvalidateCache() throws InvalidArgumentException, IOException, NoSuchFieldException, IllegalAccessException{
    //初始化 AB Testing SDK

    final ISensorsABTest abTest = new SensorsABTest(abGlobalConfig);
//        Experiment<String> result = abTest.asyncFetchABTest(distinctId, false, experimentName, "grey", false);
    EventCacheManager eventCacheManager =
            getEventCacheManagerByReflect(getSensorsABTestWorkerByReflect(abTest));

    assertEquals(0, eventCacheManager.getCacheSize());

    String experimentName = "int";
    String distinctId = "xc123";

    Experiment<Integer> result = abTest.fastFetchABTest(distinctId, false, experimentName, -1, true);
    System.out.println("======= result: " + result.getResult());
    assertTrue(eventCacheManager.judgeEventCacheExist(distinctId, result.getAbTestExperimentId()));

    getEventCacheByReflect(eventCacheManager).invalidateAll(); //强制清除缓存

    result = abTest.fastFetchABTest(distinctId, false, experimentName, -1, true);
    System.out.println("======= result: " + result.getResult());
  }



  /**
   * 上报事件接口测试
   * 第一次携带version，之后请求不携带
   */
  @Test
  public void trackABTestTriggerTest() throws InvalidArgumentException {
    final ISensorsABTest abTest = new SensorsABTest(abGlobalConfig);
    Experiment<String> result = new Experiment<>("AB123456", true, "102", "88", false, false, "grey");
    abTest.trackABTestTrigger(result);
    Experiment<String> result1 = new Experiment<>("AB1234567", true, "103", "99", false, false, "grey");
    abTest.trackABTestTrigger(result1);

    Experiment<String> result2 = new Experiment<>("AB1234567", true, "104", "100", false, false, "grey");
    Map<String, Object> properties = new HashMap<>();
    properties.put("$os", "mac");
    abTest.trackABTestTrigger(result2, properties);
  }

  /**
   * 上报事件接口测试 prop
   */
  @Test
  public void trackABTestTriggerTestProp() throws InvalidArgumentException {
    final ISensorsABTest abTest = new SensorsABTest(abGlobalConfig);

    Experiment<String> result2 = new Experiment<>("AB1234567", true, "104", "100", false, false, "grey");
    Map<String, Object> properties = new HashMap<>();
    properties.put("$os", "mac");
    abTest.trackABTestTrigger(result2, properties);
  }

  /**
   * 上报事件接口测试 prop
   */
  @Test
  public void trackABTestTriggerTestNull() throws InvalidArgumentException {
    String url =
            "http://abtesting.saas.debugbox.sensorsdata.cn/api/v2/abtest/online/results?project-key=438B9364C98D54371751BA82F6484A1A03A5155E";

    //构建配置 AB Testing 试验全局参数
    ABGlobalConfig abGlobalConfig = ABGlobalConfig.builder()
            .setApiUrl(url)                //分流试验地址
            .setSensorsAnalytics(sa)       //神策分析 SDK 实例
            .enableEventCache(true)
            .build();

    final ISensorsABTest abTest = new SensorsABTest(abGlobalConfig);

    Experiment<String> result2 = new Experiment<>("AB1234567", true, "104", "100", false, false, "grey");
    Map<String, Object> properties = new HashMap<>();
    properties.put("$os", "mac");
    abTest.trackABTestTrigger(null, properties);
  }

  @Test
  public void trackABTestTriggerTestMulProp() throws InvalidArgumentException {
    String url =
            "http://abtesting.saas.debugbox.sensorsdata.cn/api/v2/abtest/online/results?project-key=438B9364C98D54371751BA82F6484A1A03A5155E";

    //构建配置 AB Testing 试验全局参数
    ABGlobalConfig abGlobalConfig = ABGlobalConfig.builder()
            .setApiUrl(url)                //分流试验地址
            .setSensorsAnalytics(sa)       //神策分析 SDK 实例
            .enableEventCache(true)
            .build();

    final ISensorsABTest abTest = new SensorsABTest(abGlobalConfig);

    Experiment<String> result2 = new Experiment<>("AB1234567", true, "104", "100", false, false, "grey");
    Map<String, Object> properties = new HashMap<>();
    properties.put("test", "test");
    properties.put("andoter", "andoter");
    properties.put("antway", "dddddd");
    abTest.trackABTestTrigger(result2, properties);
  }

  @Test
  public void trackABTestTriggerTestMulProps() throws InvalidArgumentException {
    String url =
            "http://abtesting.saas.debugbox.sensorsdata.cn/api/v2/abtest/online/results?project-key=438B9364C98D54371751BA82F6484A1A03A5155E";

    //构建配置 AB Testing 试验全局参数
    ABGlobalConfig abGlobalConfig = ABGlobalConfig.builder()
            .setApiUrl(url)                //分流试验地址
            .setSensorsAnalytics(sa)       //神策分析 SDK 实例
            .enableEventCache(true)
            .build();

    final ISensorsABTest abTest = new SensorsABTest(abGlobalConfig);

    Experiment<String> result2 = new Experiment<>("AB1234567", true, "104", "100", false, false, "grey");
    Map<String, Object> properties = new HashMap<>();
    properties.put("$os", "mac");
    properties.put("$lib_version", "000");
    abTest.trackABTestTrigger(result2, properties);
  }

  /**
   * 用户为白名单用户，不触发上报事件
   */
  @Test
  public void notTriggerEventTest()
          throws IOException, InvalidArgumentException, NoSuchFieldException, IllegalAccessException {
    final ISensorsABTest abTest = new SensorsABTest(abGlobalConfig);
    ObjectMapper objectMapper = new ObjectMapper();
    Experiment<String> result = abTest.asyncFetchABTest(distinctId, true, "color", "{\"color\":\"grey\"}");
    assertTrue(result.getWhiteList());
    EventCacheManager eventCacheManager = getEventCacheManagerByReflect(getSensorsABTestWorkerByReflect(abTest));
    assertEquals(0, eventCacheManager.getCacheSize());
  }



  private SensorsABTestWorker getSensorsABTestWorkerByReflect(ISensorsABTest abTest)
          throws NoSuchFieldException, IllegalAccessException {
    Class<? extends ISensorsABTest> abTestClazz = abTest.getClass();
    Field field = abTestClazz.getDeclaredField("worker");
    field.setAccessible(true);
    return (SensorsABTestWorker) field.get(abTest);
  }

  private ExperimentCacheManager getExperimentCacheManagerByReflect(SensorsABTestWorker worker)
          throws NoSuchFieldException, IllegalAccessException {
    Class<? extends SensorsABTestWorker> workerClazz = worker.getClass();
    Field cacheManagerField = workerClazz.getDeclaredField("experimentCacheManager");
    cacheManagerField.setAccessible(true);
    return (ExperimentCacheManager) cacheManagerField.get(worker);
  }

  private LoadingCache<String, Object> getExperimentResultCacheByReflect(ExperimentCacheManager experimentCacheManager) throws NoSuchFieldException, IllegalAccessException {

    Class<? extends ExperimentCacheManager> experimentCacheManagerClass = experimentCacheManager.getClass();

    Field cacheField = experimentCacheManagerClass.getDeclaredField("experimentResultCache");
    cacheField.setAccessible(true);
    return (LoadingCache<String, Object>) cacheField.get(experimentCacheManager);
  }

  private EventCacheManager getEventCacheManagerByReflect(SensorsABTestWorker worker)
          throws NoSuchFieldException, IllegalAccessException {
    Class<? extends SensorsABTestWorker> workerClazz = worker.getClass();
    Field cacheManagerField = workerClazz.getDeclaredField("eventCacheManager");
    cacheManagerField.setAccessible(true);
    return (EventCacheManager) cacheManagerField.get(worker);
  }

  private LoadingCache<String, Object> getEventCacheByReflect(EventCacheManager eventCacheManager) throws NoSuchFieldException, IllegalAccessException {
    Class<? extends EventCacheManager> eventCacheManagerClass = eventCacheManager.getClass();
    Field cacheField = eventCacheManagerClass.getDeclaredField("eventCache");
    cacheField.setAccessible(true);
    return (LoadingCache<String, Object>) cacheField.get(eventCacheManager);
  }
}