package com.sensorsdata.analytics.javasdk;

import com.fasterxml.jackson.databind.JsonNode;
import com.google.common.cache.LoadingCache;
import com.sensorsdata.analytics.javasdk.bean.ABGlobalConfig;
import com.sensorsdata.analytics.javasdk.bean.cache.ExperimentGroupConfig;
import com.sensorsdata.analytics.javasdk.bean.cache.UserHitExperiment;
import com.sensorsdata.analytics.javasdk.cache.EventCacheManager;
import com.sensorsdata.analytics.javasdk.cache.ExperimentCacheManager;
import com.sensorsdata.analytics.javasdk.consumer.ConcurrentLoggingConsumer;
import com.sensorsdata.analytics.javasdk.consumer.Consumer;
import org.eclipse.jetty.server.Server;
import org.eclipse.jetty.servlet.ServletContextHandler;
import org.eclipse.jetty.servlet.ServletHolder;
import org.junit.AfterClass;
import org.junit.BeforeClass;
import org.junit.Test;

import java.io.IOException;
import java.lang.reflect.Field;
import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

import static com.sensorsdata.analytics.javasdk.util.ABTestUtil.map2Str;
import static org.junit.Assert.*;

/**
 * 参数构建基类,所有测试类继承该类
 */
public class SensorsBaseTest {

  protected String url = "http://localhost:8888/test";

  protected ConcurrentLoggingConsumer consumer;

  //初始化神策分析 SDK

  protected ISensorsAnalytics sa;
  protected StringBuilder messageBuffer;

  //构建 ab 实例
  protected ISensorsABTest sensorsABTest;

  //根据 ab 实例获取 work 对象
  protected SensorsABTestWorker abTestWorkerByReflect;

  //根据 ab 示例获取 ExperimentCacheManager 对象
  protected ExperimentCacheManager experimentCacheManagerByReflect;

  //试验结果缓存对象
  protected LoadingCache<String, UserHitExperiment> experimentResultCacheByReflect;


  protected ConcurrentHashMap<String, ExperimentGroupConfig> experimentGroupConfigCacheByReflect;

  //事件缓存管理器
  protected EventCacheManager eventCacheManagerByReflect;

  //事件缓存对象
  protected LoadingCache<String, Object> eventCacheByReflect;

  protected static Server server;

  public SensorsBaseTest() {
  }

  @BeforeClass
  public static void mockInitServer() throws Exception {
    server = new Server(8888);
    ServletContextHandler handler = new ServletContextHandler();
    handler.addServlet(new ServletHolder(new TestServlet()), "/test");
    server.setHandler(handler);
    server.start();

  }

  @Test
  public void checkServer() {
    assertNotNull(server);
  }

  @AfterClass
  public static void mockCloseServer() throws Exception {
    if (server != null) {
      server.stop();
    }
  }

  //构建一个测试的 Consumer 用于验证最终的消息
  static class TestConsumer implements Consumer {

    @Override
    public void send(Map<String, Object> message) {
      assertNotNull(message);
      assertTrue(message.containsKey("_track_id"));
      assertTrue(message.containsKey("lib"));
      assertTrue(message.containsKey("time"));
      assertTrue(message.containsKey("distinct_id"));
      assertTrue(message.containsKey("type"));
      assertTrue(message.containsKey("event"));
      assertEquals(SensorsABTestConst.EVENT_TYPE, message.get("event"));
      assertEquals("track", message.get("type"));
    }

    @Override
    public void flush() {

    }

    @Override
    public void close() {

    }
  }

  //TODO 传参
  protected void initSASDK(){
    try {
      consumer = new ConcurrentLoggingConsumer("file.log");
      sa = new SensorsAnalytics(consumer);
      Field field = consumer.getClass().getSuperclass().getDeclaredField("messageBuffer");
      field.setAccessible(true);
      messageBuffer = (StringBuilder) field.get(consumer);
    }catch (IllegalAccessException | IOException | NoSuchFieldException e){
      e.printStackTrace();
    }
  }

  /**
   * 实例 ab 使用接口 必须调用
   * 由于部分单测需要修改全局配置参数 所以该方法无法统一初始化，需要由各自方法主动调用
   */
  protected void initInstance(ABGlobalConfig abGlobalConfig) {
    sensorsABTest = new SensorsABTest(abGlobalConfig);
  }

  /**
   * 通过反射获取 ab 示例内部对象信息
   * 需要使用内部信息的需调用
   */
  protected void initInnerClassInfo(ISensorsABTest abTest) throws NoSuchFieldException, IllegalAccessException {
    abTestWorkerByReflect = getSensorsABTestWorkerByReflect(abTest == null ? sensorsABTest : abTest);
    experimentCacheManagerByReflect = getExperimentCacheManagerByReflect(abTestWorkerByReflect);
    experimentResultCacheByReflect = getExperimentResultCacheByReflect(experimentCacheManagerByReflect);
    eventCacheManagerByReflect = getEventCacheManagerByReflect(abTestWorkerByReflect);
    eventCacheByReflect = getEventCacheByReflect(eventCacheManagerByReflect);
    experimentGroupConfigCacheByReflect = getExperimentGroupCacheByReflect(experimentCacheManagerByReflect);
  }

  protected String generateCacheKey(String distinctId, boolean isLoginId, Map<String, String> customIds) {
    String key = String.format("%s_%b_%s", distinctId, isLoginId, map2Str(customIds));
    try {
      MessageDigest instance = MessageDigest.getInstance("MD5");
      instance.update(key.getBytes(StandardCharsets.UTF_8));
      return new String(instance.digest(), StandardCharsets.UTF_8);
    } catch (NoSuchAlgorithmException e) {
      fail();
    }
    return key;
  }

  protected String generateKey(String distinctId, Boolean isLoginId, String experimentId, Map<String, String> customIds) {
    String key = String.format("%s_%b_%s_%s", distinctId, isLoginId, experimentId, map2Str(customIds));
    try {
      MessageDigest instance = MessageDigest.getInstance("MD5");
      instance.update(key.getBytes(StandardCharsets.UTF_8));
      return new String(instance.digest(), StandardCharsets.UTF_8);
    } catch (NoSuchAlgorithmException e) {
      fail();
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
  protected String generateExperimentGroupConfigCacheKey(String experimentId, String experimentGroupId) {
    return String.format("%s_%s", experimentId, experimentGroupId);
  }

  /**
   * 清空缓存中的数据
   */
  protected void clearCacheInfo() {
    experimentResultCacheByReflect.invalidateAll();
    eventCacheByReflect.invalidateAll();
  }

  /**
   * 通过反射 abtest 实例获取 SensorsABTestWorker 实例
   *
   * @param abTest ab 实例
   * @return 内部 SensorsABTestWorker 实例
   */
  protected SensorsABTestWorker getSensorsABTestWorkerByReflect(ISensorsABTest abTest)
      throws NoSuchFieldException, IllegalAccessException {
    Class<? extends ISensorsABTest> abTestClazz = abTest.getClass();
    Field field = abTestClazz.getDeclaredField("worker");
    field.setAccessible(true);
    return (SensorsABTestWorker) field.get(abTest);
  }

  /**
   * 通过反射 work 实例获取 ExperimentCacheManager 对象
   *
   * @param worker SensorsABTestWorker
   * @return ExperimentCacheManager
   */
  protected ExperimentCacheManager getExperimentCacheManagerByReflect(SensorsABTestWorker worker)
      throws NoSuchFieldException, IllegalAccessException {
    Class<? extends SensorsABTestWorker> workerClazz = worker.getClass();
    Field cacheManagerField = workerClazz.getDeclaredField("experimentCacheManager");
    cacheManagerField.setAccessible(true);
    return (ExperimentCacheManager) cacheManagerField.get(worker);
  }

  /**
   * 通过反射 experimentCacheManager 实例获取试验结果缓存
   *
   * @param experimentCacheManager ExperimentCacheManager
   * @return LoadingCache<String, Object>
   */
  protected LoadingCache<String, UserHitExperiment> getExperimentResultCacheByReflect(
      ExperimentCacheManager experimentCacheManager)
      throws NoSuchFieldException, IllegalAccessException {

    Class<? extends ExperimentCacheManager> experimentCacheManagerClass = experimentCacheManager.getClass();

    Field cacheField = experimentCacheManagerClass.getDeclaredField("experimentResultCache");
    cacheField.setAccessible(true);
    return (LoadingCache<String, UserHitExperiment>) cacheField.get(experimentCacheManager);
  }

  /**
   * 通过反射 SensorsABTestWorker 实例获取自动上报事件缓存
   *
   * @param worker SensorsABTestWorker
   * @return EventCacheManager
   */
  protected EventCacheManager getEventCacheManagerByReflect(SensorsABTestWorker worker)
      throws NoSuchFieldException, IllegalAccessException {
    Class<? extends SensorsABTestWorker> workerClazz = worker.getClass();
    Field cacheManagerField = workerClazz.getDeclaredField("eventCacheManager");
    cacheManagerField.setAccessible(true);
    return (EventCacheManager) cacheManagerField.get(worker);
  }

  /**
   * 通过反射 eventCacheManager 实例获取试验结果缓存
   *
   * @param eventCacheManager EventCacheManager
   * @return LoadingCache<String, Object>
   */
  protected LoadingCache<String, Object> getEventCacheByReflect(EventCacheManager eventCacheManager)
      throws NoSuchFieldException, IllegalAccessException {
    Class<? extends EventCacheManager> eventCacheManagerClass = eventCacheManager.getClass();
    Field cacheField = eventCacheManagerClass.getDeclaredField("eventCache");
    cacheField.setAccessible(true);
    return (LoadingCache<String, Object>) cacheField.get(eventCacheManager);
  }

  /**
   * 通过反射 experimentCacheManager 实例获取试验组配置缓存
   *
   * @param experimentCacheManager 试验配置管理
   * @return 试验组配置缓存
   * @throws NoSuchFieldException
   * @throws IllegalAccessException
   */
  protected ConcurrentHashMap<String, ExperimentGroupConfig> getExperimentGroupCacheByReflect(
      ExperimentCacheManager experimentCacheManager)
      throws NoSuchFieldException, IllegalAccessException {
    Class<? extends ExperimentCacheManager> eventCacheManagerClass = experimentCacheManager.getClass();
    Field cacheField = eventCacheManagerClass.getDeclaredField("experimentGroupConfigCache");
    cacheField.setAccessible(true);
    return (ConcurrentHashMap<String, ExperimentGroupConfig>) cacheField.get(experimentCacheManager);
  }


}
