package com.sensorsdata.analytics.javasdk;

import com.fasterxml.jackson.databind.JsonNode;
import com.sensorsdata.analytics.javasdk.bean.ABGlobalConfig;
import com.sensorsdata.analytics.javasdk.bean.Experiment;
import com.sensorsdata.analytics.javasdk.exceptions.InvalidArgumentException;
import com.sensorsdata.analytics.javasdk.util.SensorsAnalyticsUtil;
import org.junit.Before;
import org.junit.Ignore;
import org.junit.Test;

import java.io.IOException;

import static org.junit.Assert.*;

/**
 * 校验 fastFetchABTest 接口
 *
 * @author fangzhuo
 * @version 1.0.0
 * @since 2022/01/13 10:54
 */
public class FastFetchRegressionTest extends SensorsBaseTest {

    String distinctId = "a123";

    @Before
    public void init() {
        initSASDK();
    }

    /**
     * 验证配置有效值后，初始化正确后调用 FastFetchABTest 且缓存存储正确
     */
    @Test
    public void fastFetchABTestReturnResult() throws InvalidArgumentException, IOException, NoSuchFieldException, IllegalAccessException {
        initInstance(ABGlobalConfig.builder().setApiUrl(url).setSensorsAnalytics(sa).build());
        initInnerClassInfo(sensorsABTest);
        Experiment<String> result = sensorsABTest.fastFetchABTest("a123", false, "str_experiment", "grey");
        assertNotNull(result);
        assertNotNull(result.getDistinctId());
        assertNotNull(result.getIsLoginId());
        assertNotNull(result.getResult());
        assertNotNull(result.getAbTestExperimentId());
        assertNotNull(result.getIsControlGroup());
        assertNotNull(result.getIsWhiteList());

        // fastFetchABTest 接口存储实验缓存
        assertEquals(1, experimentCacheManagerByReflect.getCacheSize());

        // 检查事件缓存
        assertEquals(1, eventCacheByReflect.size());

        if (messageBuffer != null) {
            assertNotEquals(0, messageBuffer.length());
            JsonNode jsonNode = SensorsAnalyticsUtil.getJsonObjectMapper().readValue(messageBuffer.toString(), JsonNode.class);
            assertEquals("\"$ABTestTrigger\"", jsonNode.get("event").toString());
        }
        assertNotNull(result.getResult());
    }

    /**
     * fastFetchABTest 设置不自动上报事件，手动触发
     */
    @Test
    public void fastFetchABTest_trackABTestTrigger()
            throws IOException, InvalidArgumentException, NoSuchFieldException, IllegalAccessException {
        initInstance(ABGlobalConfig.builder().setApiUrl(url).setSensorsAnalytics(sa).build());
        initInnerClassInfo(sensorsABTest);
        Experiment<String> result = sensorsABTest.fastFetchABTest("AB123", true, "str_experiment", "eee", false);

        // 检查事件缓存
        assertEquals(0, eventCacheByReflect.size());

        sensorsABTest.trackABTestTrigger(result);
        if (messageBuffer != null) {
            assertNotEquals(0, messageBuffer.length());
            JsonNode jsonNode = SensorsAnalyticsUtil.getJsonObjectMapper().readValue(messageBuffer.toString(), JsonNode.class);
            assertEquals("\"$ABTestTrigger\"", jsonNode.get("event").toString());
        }
    }

    /**
     * 测试试验结果缓存过期时间
     * cache清除内存没有使用额外的线程，所以可能不会立即删除，但是用户get操作已经获取不到值，下一次get操作就会清理内存。
     */
    @Test
    @Ignore
    public void experimentCacheTimeTest()
            throws InterruptedException, NoSuchFieldException, IllegalAccessException, InvalidArgumentException {
        ABGlobalConfig abGlobalConfig = ABGlobalConfig.builder()
                .setExperimentCacheTime(1)      //缓存有效期分钟
                .setApiUrl(url)                //分流试验地址
                .setSensorsAnalytics(sa)       //神策分析 SDK 实例
                .build();

        initInstance(abGlobalConfig);
        initInnerClassInfo(sensorsABTest);
        sensorsABTest.fastFetchABTest("AB123", false, "str_experiment", "1");
        assertEquals(1, experimentCacheManagerByReflect.getCacheSize());

        Thread.sleep(61000);
        sensorsABTest.fastFetchABTest("AB123", false, "str_experiment", "1");
        assertEquals(1, experimentCacheManagerByReflect.getCacheSize());
    }

    /**
     * 初始化正确后调用 fastFetchABTest 且试验结果缓存存储正确
     */
    @Test
    public void experimentCacheSizeTest() throws InvalidArgumentException, NoSuchFieldException, IllegalAccessException {
        ABGlobalConfig abGlobalConfig = ABGlobalConfig.builder()
                .setExperimentCacheSize(1)      //只缓存一条
                .setApiUrl(url)                //分流试验地址
                .setSensorsAnalytics(sa)       //神策分析 SDK 实例
                .build();

        initInstance(abGlobalConfig);
        initInnerClassInfo(sensorsABTest);

        sensorsABTest.fastFetchABTest("AB123", true, "int_experiment", -1);
        sensorsABTest.fastFetchABTest("AB12234", true, "int_experiment", -1);

        assertEquals(1, experimentCacheManagerByReflect.getCacheSize());
    }

    /**
     * 初始化正确后调用 fastFetchABTest 且实验结果缓存存储正确 setEventCacheTime
     */
    @Test
    public void eventCacheTimeTest()
            throws InvalidArgumentException, InterruptedException, IOException, NoSuchFieldException, IllegalAccessException {
        ABGlobalConfig abGlobalConfig = ABGlobalConfig.builder()
                .setApiUrl(url)                //分流试验地址
                .setSensorsAnalytics(sa)       //神策分析 SDK 实例
                .setEventCacheTime(1)
                .build();
        initInstance(abGlobalConfig);
        initInnerClassInfo(sensorsABTest);

        sensorsABTest.fastFetchABTest("AB123", true, "int_experiment", -1);
        if (messageBuffer != null) {
            assertNotEquals(0, messageBuffer.length());
            JsonNode jsonNode = SensorsAnalyticsUtil.getJsonObjectMapper().readValue(messageBuffer.toString(), JsonNode.class);
            assertEquals("\"$ABTestTrigger\"", jsonNode.get("event").toString());
        }
        sa.flush();

        Thread.sleep(60000);

        sensorsABTest.fastFetchABTest("AB12234", true, "int_experiment", -2);
        if (messageBuffer != null) {
            assertNotEquals(0, messageBuffer.length());
            JsonNode jsonNode = SensorsAnalyticsUtil.getJsonObjectMapper().readValue(messageBuffer.toString(), JsonNode.class);
            assertEquals("\"$ABTestTrigger\"", jsonNode.get("event").toString());
        }
    }

    /**
     * 初始化正确后调用 fastFetchABTest 且试验结果缓存存储正确 setEventCacheSize
     */
    @Test
    public void eventCacheSizeTest() throws InvalidArgumentException, NoSuchFieldException, IllegalAccessException {
        ABGlobalConfig abGlobalConfig = ABGlobalConfig.builder()
                .setApiUrl(url)                //分流试验地址
                .setSensorsAnalytics(sa)       //神策分析 SDK 实例
                .setEventCacheSize(100)
                .build();
        initInstance(abGlobalConfig);
        initInnerClassInfo(sensorsABTest);

        for (int i = 0; i < 200; i++) {
            sensorsABTest.fastFetchABTest("AB12234_" + i, true, "int_experiment", -2);
        }
        assertEquals(100, eventCacheByReflect.size());
    }

    /**
     * 验证配置有效值后，初始化正确后调用 fastFetchABTest 且缓存不存储
     */
    @Test
    public void fastFetchABTestReturnResultInt()
            throws InvalidArgumentException, NoSuchFieldException, IllegalAccessException {
        //构建配置 AB Testing 试验全局参数
        ABGlobalConfig abGlobalConfig = ABGlobalConfig.builder()
                .setApiUrl(url)                //分流试验地址
                .setSensorsAnalytics(sa)       //神策分析 SDK 实例
                .enableEventCache(true)
                .build();

        //初始化 AB Testing SDK
        initInstance(abGlobalConfig);
        initInnerClassInfo(sensorsABTest);

        String experimentName = "int_experiment";
        Experiment<Integer> result = sensorsABTest.fastFetchABTest(distinctId, false, experimentName, -1, false);

        assertEquals(distinctId, result.getDistinctId());
        assertEquals(false, result.getIsLoginId());
        assertEquals("2", result.getAbTestExperimentId());
        assertEquals("1", result.getAbTestExperimentGroupId());
        assertFalse(result.getIsControlGroup());
        assertFalse(result.getIsWhiteList());
        assertEquals(123, result.getResult().intValue());

        assertEquals(1, experimentCacheManagerByReflect.getCacheSize());

        assertNotNull(result.getResult());
    }

    /**
     * fastFetchABTest 参数值 - isLoginId=false
     * 期望：命中试验，返回结果字段均存在值
     */
    @Test
    public void fastFetchABTest05() throws InvalidArgumentException {
        //初始化 AB Testing SDK
        initInstance(ABGlobalConfig.builder().setApiUrl(url).setSensorsAnalytics(sa).build());
        Experiment<Integer> result = sensorsABTest.fastFetchABTest("a123", false, "int_experiment", -1, false);
        assertEquals("a123", result.getDistinctId());
        assertEquals(false, result.getIsLoginId());
        assertNotNull(result.getResult());
    }

    /**
     * fastFetchABTest 参数值 - isLoginId=true
     * 期望：命中试验，返回结果字段均存在值
     */
    @Test
    public void fastFetchABTest06() throws NoSuchFieldException, IllegalAccessException, InvalidArgumentException {
        //初始化 AB Testing SDK
        initInstance(ABGlobalConfig.builder().setApiUrl(url).setSensorsAnalytics(sa).build());
        Experiment<Integer> result = sensorsABTest.fastFetchABTest("a123", true, "int_experiment", -1, false);
        assertEquals("a123", result.getDistinctId());
        assertEquals(true, result.getIsLoginId());
        assertEquals("2", result.getAbTestExperimentId());
        assertEquals("1", result.getAbTestExperimentGroupId());
        assertFalse(result.getIsControlGroup());
        assertFalse(result.getIsWhiteList());
        initInnerClassInfo(sensorsABTest);
        assertEquals(1, experimentCacheManagerByReflect.getCacheSize());
        assertNotNull(result.getResult());
    }


    /**
     * fastFetchABTest 参数值为异常值- distinctId 超长
     * 期望：命中试验，返回结果字段均存在值
     */
    @Test
    public void fastFetchABTest07() throws NoSuchFieldException, IllegalAccessException, InvalidArgumentException {
        //初始化 AB Testing SDK
        initInstance(ABGlobalConfig.builder().setApiUrl(url).setSensorsAnalytics(sa).build());
        String distinctId = "123666664444444444444444444666666666666";
        Experiment<Integer> result = sensorsABTest.fastFetchABTest(distinctId, true, "int_experiment", -1, false);
        assertEquals(distinctId, result.getDistinctId());
        assertEquals(true, result.getIsLoginId());
        assertEquals("2", result.getAbTestExperimentId());
        assertEquals("1", result.getAbTestExperimentGroupId());
        assertFalse(result.getIsControlGroup());
        assertFalse(result.getIsWhiteList());
        initInnerClassInfo(sensorsABTest);
        assertEquals(1, experimentCacheManagerByReflect.getCacheSize());
        assertNotNull(result.getResult());
    }

    /**
     * 校验不同实例之间的缓存是否相互影响
     * 期望：缓存隔离，互不影响
     */
    @Test
    public void checkDiffInstancesCacheInteractive()
            throws InvalidArgumentException, NoSuchFieldException, IllegalAccessException {
        ABGlobalConfig build = ABGlobalConfig.builder().setApiUrl(url).setSensorsAnalytics(sa).build();
        SensorsABTest abTest1 = new SensorsABTest(build);
        SensorsABTest abTest2 = new SensorsABTest(build);
        abTest1.fastFetchABTest("a123", true, "int_experiment", -1);
        initInnerClassInfo(abTest1);
        assertEquals(1, experimentCacheManagerByReflect.getCacheSize());
        initInnerClassInfo(abTest2);
        assertEquals(0, experimentCacheManagerByReflect.getCacheSize());
    }

    /**
     * fastFetchABTest 携带正常 customIds
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

}
