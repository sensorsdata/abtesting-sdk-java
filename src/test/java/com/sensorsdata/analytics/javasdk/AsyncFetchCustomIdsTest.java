package com.sensorsdata.analytics.javasdk;

import com.fasterxml.jackson.databind.JsonNode;
import com.google.common.collect.Maps;
import com.sensorsdata.analytics.javasdk.bean.ABGlobalConfig;
import com.sensorsdata.analytics.javasdk.bean.Experiment;
import com.sensorsdata.analytics.javasdk.exceptions.InvalidArgumentException;
import com.sensorsdata.analytics.javasdk.util.SensorsAnalyticsUtil;
import org.junit.Before;
import org.junit.Ignore;
import org.junit.Test;

import java.io.IOException;
import java.util.Map;

import static org.junit.Assert.*;

/**
 * 校验 asyncFetchABTest 接口
 *
 * @author fangzhuo
 * @version 1.0.0
 * @since 2022/01/13 10:38
 */
public class AsyncFetchCustomIdsTest extends SensorsBaseTest {

    @Before
    public void init() {
        initSASDK();
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
     * asyncFetchABTest 请求，customIds key 为保留字段
     * 期望：SDK 拦截请求，直接返回默认值
     */
    @Test
    public void checkAsyncFetchWithInvalidCustomIds01() throws InvalidArgumentException {
        String[] customInvaild = {"date", "datetime", "distinct_id", "event", "events", "first_id", "id", "original_id", "properties", "second_id", "time", "user_id", "users"};
        //初始化 AB Testing SDK
        initInstance(ABGlobalConfig.builder().setApiUrl(url).setSensorsAnalytics(sa).build());
        for (String s : customInvaild) {
            Experiment<String> experiment =
                    sensorsABTest.asyncFetchABTest(SensorsABParams.starter("a123", true, "str_experiment", "qwe")
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
     * asyncFetchABTest 请求，customIds key 为非法字段
     * 期望：SDK 拦截请求，直接返回默认值
     */
    @Test
    public void checkAsyncFetchWithInvalidCustomIds02() throws InvalidArgumentException {
        String[] customInvaild = {null, // key 为 null
                "",                     // key 为空字符串
                "1122aaaa",             // key 开头是数字
                "$1122aa",              // key 包含字符 $
                "……%%",                 // key 包含特殊字符
                "aaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaa", // key 为100个字符
                "aaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaa1"}; // key 超过 100个字符
        //初始化 AB Testing SDK
        initInstance(ABGlobalConfig.builder().setApiUrl(url).setSensorsAnalytics(sa).build());
        for (String s : customInvaild) {
            Experiment<String> experiment =
                    sensorsABTest.asyncFetchABTest(SensorsABParams.starter("a123", true, "str_experiment", "qwe")
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
     * asyncFetchABTest 请求，customIds value=null
     * 期望：SDK 拦截请求，直接返回默认值
     */
    @Test
    public void checkAsyncFetchWithInvalidCustomIds03() throws InvalidArgumentException {
        StringBuilder strInvalid = new StringBuilder();
        for (int i = 0; i < 1024; i++) {
            strInvalid.append(i);
        }
        String[] valueInvalid = {"",
                null,
                strInvalid.toString(),
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
                    sensorsABTest.asyncFetchABTest(SensorsABParams.starter("a123", true, "str_experiment", "qwe")
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
     * asyncFetchABTest 请求，customIds 为 null
     * 期望：SDK 拦截请求，直接返回默认值
     */
    @Test
    public void checkAsyncFetchWithInvalidCustomIds04() throws InvalidArgumentException {
        //初始化 AB Testing SDK
        initInstance(ABGlobalConfig.builder().setApiUrl(url).setSensorsAnalytics(sa).build());
        Experiment<String> experiment =
                sensorsABTest.asyncFetchABTest(SensorsABParams.starter("a123", true, "str_experiment", "qwe")
                        .customIds(null)
                        .build());
        assertNotNull(experiment);
        assertEquals("a123", experiment.getDistinctId());
        assertTrue(experiment.getIsLoginId());
        assertEquals("test", experiment.getResult());
        assertEquals("2", experiment.getAbTestExperimentId());
        assertEquals("1", experiment.getAbTestExperimentGroupId());

        sensorsABTest.fastFetchABTest(SensorsABParams.starter("a123", true, "str_experiment", "qwe")
                .addCustomId("aaa", "bbb")
                .build());
    }


    /**
     * asyncFetchABTest 请求，customIds key 为保留字段
     * 期望：SDK 拦截请求，直接返回默认值
     */
    @Test
    public void checkAsyncFetchWithInvalidCustomIds05() throws InvalidArgumentException {
        Map<String, String> customIds = Maps.newHashMap();
        String[] customInvalid = {"date", "datetime", "distinct_id", "event", "events", "first_id", "id", "original_id", "properties", "second_id", "time", "user_id", "users"};
        //初始化 AB Testing SDK
        initInstance(ABGlobalConfig.builder().setApiUrl(url).setSensorsAnalytics(sa).build());
        for (String s : customInvalid) {
            customIds.put(s, "eee");
            Experiment<String> experiment =
                    sensorsABTest.asyncFetchABTest(SensorsABParams.starter("a123", true, "str_experiment", "qwe")
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
    public void checkAsyncFetchWithInvalidCustomIds08() throws InvalidArgumentException {
        Map<String, String> customIds = Maps.newHashMap();
        String[] customInvalid = {null, "",
                "1122aaaa", "$1122aa", "……%%",
                "aaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaa",
                "aaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaa1"};
        //初始化 AB Testing SDK
        initInstance(ABGlobalConfig.builder().setApiUrl(url).setSensorsAnalytics(sa).build());
        for (String s : customInvalid) {
            customIds.put(s, "eee");
            Experiment<String> experiment =
                    sensorsABTest.asyncFetchABTest(SensorsABParams.starter("a123", true, "str_experiment", "qwe")
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
     * asyncFetchABTest 设置 customId 和 自定义属性
     * <p>
     * 期望：分流请求中的 CustomIds和自定义属性为设置的值,
     */

    @Test
    public void checkAsyncFetchWithProp() throws InvalidArgumentException {
        //初始化 AB Testing SDK
        initSASDK();
        initInstance(ABGlobalConfig.builder().setApiUrl(url).setSensorsAnalytics(sa).build());
        Experiment<String> experiment =
                sensorsABTest.asyncFetchABTest(
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
     * asyncFetchABTest 请求两次，设置不同的 customId
     * <p>
     * 期望：两次都会发送分流请求，且CustomIds为设置的值， 两次都会触发 $ABTestTrigger 事件
     */
    @Test
    public void checkAsyncFetch() throws InvalidArgumentException, NoSuchFieldException, IllegalAccessException, IOException {

        Map<String, String> customIds = Maps.newHashMap();
        customIds.put("qwe", "qwe");
        //初始化 AB Testing SDK
        initSASDK();
        initInstance(ABGlobalConfig.builder().setApiUrl(url).setSensorsAnalytics(sa).build());
        initInnerClassInfo(sensorsABTest);

        Experiment<Integer> experiment =
                sensorsABTest.asyncFetchABTest(SensorsABParams.starter("a123", true, "int_experiment", -1)
                        .addCustomId("qwe", "qwe")
                        .build());
        assertEquals(123, experiment.getResult().intValue());
        sa.flush();

        experiment = sensorsABTest.asyncFetchABTest(SensorsABParams.starter("a123", true, "int_experiment", -1)
                .addCustomId("customid2", "qwe")
                .build());
        //TODO 需要自动验证是否发送网络请求

        assertEquals(123, experiment.getResult().intValue());
        assertNotEquals(0, messageBuffer.length());

        JsonNode jsonNode = SensorsAnalyticsUtil.getJsonObjectMapper().readValue(messageBuffer.toString(), JsonNode.class);
        assertEquals("\"$ABTestTrigger\"", jsonNode.get("event").toString());
    }

    /**
     * 新环境 + asyncFetchTest 请求，携带一个正确的 customIds
     * 期望：命中试验，返回结果字段均有值
     */
    @Test
    @Ignore
    public void checkAsyncFetchTestNewEnv() throws InvalidArgumentException {
        String apiUrl = "http://10.120.103.57:8202/api/v2/abtest/online/results?project-key=03611F77720CB13DA4E8B726122D2EE2F95B7654";
        //初始化 AB Testing SDK
        initInstance(ABGlobalConfig.builder().setApiUrl(apiUrl).setSensorsAnalytics(sa).build());
        Experiment<Integer> experiment =
                sensorsABTest.asyncFetchABTest(SensorsABParams.starter("a123", true, "cqs_type", -1)
                        .addCustomId("custom_id", "123")
                        .build());
        assertNotNull(experiment);
        assertEquals("a123", experiment.getDistinctId());
        assertTrue(experiment.getIsLoginId());
        assertEquals(0, (int) experiment.getResult());
    }

    /**
     * 兼容性：老环境 + asyncFetchTest 请求，携带一个正确的 customIds
     * 期望：命中试验，返回结果字段均有值
     */
    @Test
    @Ignore
    public void checkAsyncFetchOldEnv() throws InvalidArgumentException {
        String apiUrl = "http://10.130.6.5:8202/api/v2/abtest/online/results?project-key=438B9364C98D54371751BA82F6484A1A03A5155E";
        //初始化 AB Testing SDK
        initInstance(ABGlobalConfig.builder().setApiUrl(apiUrl).setSensorsAnalytics(sa).build());
        Experiment<Integer> experiment =
                sensorsABTest.asyncFetchABTest(SensorsABParams.starter("a123", true, "agc", -1)
                        .addCustomId("custom_id", "123")
                        .build());
        assertNotNull(experiment);
        assertEquals("a123", experiment.getDistinctId());
        assertTrue(experiment.getIsLoginId());
        assertEquals(1, (int) experiment.getResult());
    }

}
