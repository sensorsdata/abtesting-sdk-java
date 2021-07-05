package com.sensorsdata.analytics.javasdk;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertTrue;

import com.sensorsdata.analytics.javasdk.bean.ABGlobalConfig;
import com.sensorsdata.analytics.javasdk.bean.Experiment;
import com.sensorsdata.analytics.javasdk.cache.EventCacheManager;
import com.sensorsdata.analytics.javasdk.cache.ExperimentCacheManager;
import com.sensorsdata.analytics.javasdk.consumer.ConcurrentLoggingConsumer;
import com.sensorsdata.analytics.javasdk.exceptions.InvalidArgumentException;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.Test;

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
  String url =
      "http://abtesting.saas.debugbox.sensorsdata.cn/api/v2/abtest/online/results?project-key=438B9364C98D54371751BA82F6484A1A03A5155E";

  //构建配置 AB Testing 试验全局参数
  ABGlobalConfig abGlobalConfig = ABGlobalConfig.builder()
      .setApiUrl(url)                //分流试验地址
      .setSensorsAnalytics(sa)       //神策分析 SDK 实例
      .build();
  //匿名ID 或者用户登录 ID，配合 isLoginId 使用
  String distinctId = "AB123456";
  //试验名
  String experimentName = "color";

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
  public void asyncFetchABTestReturnRightResult() throws InvalidArgumentException {
    //初始化 AB Testing SDK
    final ISensorsABTest abTest = new SensorsABTest(abGlobalConfig);
    Experiment<String> result = abTest.asyncFetchABTest(distinctId, false, experimentName, "{\"color\":\"grey\"}");
    assertNotNull(result);
    assertNotNull(result.getDistinctId());
    assertNotNull(result.getIsLoginId());
    assertNotNull(result.getResult());
    assertTrue(result.getResult() != null);
  }

  /**
   * 立即调用，默认值与返回值类型不一致，返回默认值，且试验相关字段信息返回为空，只返回默认信息
   */
  @Test
  public void asyncFetchABTesReturnResult() throws IOException, InvalidArgumentException {
    //初始化 AB Testing SDK
    final ISensorsABTest abTest = new SensorsABTest(abGlobalConfig);
    Experiment<String> result = abTest.asyncFetchABTest(distinctId, false, experimentName, "grey");
    assertNotNull(result);
    assertNotNull(result.getDistinctId());
    assertNotNull(result.getIsLoginId());
    assertNotNull(result.getResult());
    assertNull(result.getAbTestExperimentGroupId());
    assertNull(result.getAbTestExperimentId());
    assertNull(result.getControlGroup());
    assertNull(result.getWhiteList());
    assertEquals("grey", result.getResult());
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
   * 初始化正确后调用asyncFetchABTest 且试验结果缓存存储正确
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
    Experiment<String> s1 = abTest.fastFetchABTest("AB123", true, "btn_type", "1");
    Experiment<String> s2 = abTest.fastFetchABTest("AB12234", true, "btn_type", "2");
    ExperimentCacheManager cacheManager =
        getExperimentCacheManagerByReflect(getSensorsABTestWorkerByReflect(abTest));
    assertEquals(1, cacheManager.getCacheSize());
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
    Experiment<String> s2 = abTest.fastFetchABTest("AB123", true, "btn_type", "eee", false);
    ExperimentCacheManager cacheManager =
        getExperimentCacheManagerByReflect(getSensorsABTestWorkerByReflect(abTest));
    assertEquals(1, cacheManager.getCacheSize());
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
  }

  /**
   * asyncFetchABTest 请求携带预制属性
   */
  @Test
  public void setPropertiesByAsyncTest() throws IOException, InvalidArgumentException {
    final ISensorsABTest abTest = new SensorsABTest(abGlobalConfig);
    Map<String, Object> properties = new HashMap<>();
    properties.put("$os", "windows");
    Experiment<String> result = abTest.asyncFetchABTest(distinctId, true, "a", "1", properties);
    assertNotNull(result);
    assertNotNull(result.getAbTestExperimentId());
  }

  /**
   * fastFetchABTest 请求携带预制属性
   */
  @Test
  public void setPropertiesByFastTest() throws IOException, InvalidArgumentException {
    final ISensorsABTest abTest = new SensorsABTest(abGlobalConfig);
    Map<String, Object> properties = new HashMap<>();
    properties.put("$os", "windows");
    Experiment<String> result = abTest.fastFetchABTest(distinctId, true, "a", "1", properties);
    assertNotNull(result);
    assertNotNull(result.getAbTestExperimentId());
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
    Field cacheManagerField = workerClazz.getDeclaredField("experimentResultCacheManager");
    cacheManagerField.setAccessible(true);
    return (ExperimentCacheManager) cacheManagerField.get(worker);
  }

  private EventCacheManager getEventCacheManagerByReflect(SensorsABTestWorker worker)
      throws NoSuchFieldException, IllegalAccessException {
    Class<? extends SensorsABTestWorker> workerClazz = worker.getClass();
    Field cacheManagerField = workerClazz.getDeclaredField("eventCacheManager");
    cacheManagerField.setAccessible(true);
    return (EventCacheManager) cacheManagerField.get(worker);
  }

}
