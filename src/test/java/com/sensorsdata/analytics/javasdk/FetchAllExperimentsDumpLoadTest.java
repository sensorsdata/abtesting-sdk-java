package com.sensorsdata.analytics.javasdk;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.sensorsdata.analytics.javasdk.bean.ABGlobalConfig;
import com.sensorsdata.analytics.javasdk.bean.AllExperimentsResult;
import com.sensorsdata.analytics.javasdk.exceptions.InvalidArgumentException;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;

import java.io.IOException;
import java.util.Collections;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

public class FetchAllExperimentsDumpLoadTest extends SensorsBaseTest {

  private static final ObjectMapper OBJECT_MAPPER = new ObjectMapper();

  private static final String FETCH_ALL_RESPONSE = "{"
      + "  \"status\": \"SUCCESS\","
      + "  \"track_config\": {"
      + "    \"trigger_switch\": true,"
      + "    \"property_set_switch\": false,"
      + "    \"item_switch\": false,"
      + "    \"trigger_content_ext\": []"
      + "  },"
      + "  \"results\": ["
      + "    {"
      + "      \"abtest_experiment_id\": \"20\","
      + "      \"abtest_experiment_group_id\": \"200\","
      + "      \"abtest_experiment_result_id\": \"2000\","
      + "      \"abtest_experiment_version\": \"v1\","
      + "      \"is_control_group\": false,"
      + "      \"is_white_list\": false,"
      + "      \"cacheable\": true,"
      + "      \"subject_name\": \"USER\","
      + "      \"subject_id\": \"user_1\","
      + "      \"variables\": ["
      + "        {\"name\": \"button_color\", \"type\": \"STRING\", \"value\": \"blue\"},"
      + "        {\"name\": \"page_size\", \"type\": \"INTEGER\", \"value\": \"10\"}"
      + "      ]"
      + "    }"
      + "  ],"
      + "  \"out_list\": ["
      + "    {"
      + "      \"abtest_experiment_id\": \"30\","
      + "      \"abtest_experiment_group_id\": \"-1\","
      + "      \"abtest_experiment_result_id\": \"-1\","
      + "      \"abtest_experiment_version\": \"v4\","
      + "      \"is_white_list\": false,"
      + "      \"subject_name\": \"USER\","
      + "      \"subject_id\": \"user_1\","
      + "      \"variables\": ["
      + "        {\"name\": \"button_color\", \"type\": \"STRING\", \"value\": \"legacy\"}"
      + "      ]"
      + "    }"
      + "  ]"
      + "}";

  @Before
  public void init() {
    initSASDK();
    TestServlet.setResponseBody(FETCH_ALL_RESPONSE);
  }

  @After
  public void resetServlet() {
    TestServlet.resetResponseBody();
  }

  @Test
  public void dumpShouldUseGoCompatibleWireFormat() throws IOException, InvalidArgumentException {
    initInstance(ABGlobalConfig.builder().setApiUrl(url).setSensorsAnalytics(sa).build());

    AllExperimentsResult result = sensorsABTest.fetchAllExperiments(
        "fetch_all_user",
        true,
        FetchAllExperimentsParams.builder()
            .addCustomId("custom_id", "device_1")
            .build());

    String dump = result.dump();
    JsonNode dumpNode = OBJECT_MAPPER.readTree(dump);

    assertEquals("fetch_all_user", dumpNode.get("distinct_id").asText());
    assertTrue(dumpNode.get("is_login_id").asBoolean());
    assertEquals("device_1", dumpNode.get("custom_ids").get("custom_id").asText());
    assertEquals(FETCH_ALL_RESPONSE, dumpNode.get("response_body").asText());
    assertTrue(dumpNode.get("timestamp").asLong() > 0);
  }

  @Test
  public void loadAllExperimentsShouldLoadGoDumpAndRespectAutoTrackFlag()
      throws InvalidArgumentException, IOException {
    initInstance(ABGlobalConfig.builder().setApiUrl(url).setSensorsAnalytics(sa).build());

    String dump = "{"
        + "\"distinct_id\":\"fetch_all_user\","
        + "\"is_login_id\":true,"
        + "\"custom_ids\":{\"custom_id\":\"device_1\"},"
        + "\"response_body\":" + OBJECT_MAPPER.writeValueAsString(FETCH_ALL_RESPONSE) + ","
        + "\"timestamp\":1741493784244"
        + "}";

    AllExperimentsResult result = sensorsABTest.loadAllExperiments(
        "fetch_all_user",
        true,
        LoadAllExperimentsParams.builder()
            .customIds(Collections.singletonMap("custom_id", "device_1"))
            .enableAutoTrackEvent(false)
            .build(),
        dump);

    assertEquals("blue", result.getValue("button_color", "fallback"));
    assertEquals(0, messageBuffer.length());

    AllExperimentsResult trackedResult = sensorsABTest.loadAllExperiments(
        "fetch_all_user",
        true,
        LoadAllExperimentsParams.builder()
            .customIds(Collections.singletonMap("custom_id", "device_1"))
            .enableAutoTrackEvent(true)
            .build(),
        dump);

    assertEquals("blue", trackedResult.getValue("button_color", "fallback"));
    assertEquals(2, countTriggerEvents());
  }

  @Test
  public void loadAllExperimentsShouldEnableAutoTrackByDefault()
      throws InvalidArgumentException, IOException {
    initInstance(ABGlobalConfig.builder().setApiUrl(url).setSensorsAnalytics(sa).build());

    String dump = "{"
        + "\"distinct_id\":\"fetch_all_user\","
        + "\"is_login_id\":true,"
        + "\"custom_ids\":{\"custom_id\":\"device_1\"},"
        + "\"response_body\":" + OBJECT_MAPPER.writeValueAsString(FETCH_ALL_RESPONSE) + ","
        + "\"timestamp\":1741493784244"
        + "}";

    AllExperimentsResult result = sensorsABTest.loadAllExperiments(
        "fetch_all_user",
        true,
        LoadAllExperimentsParams.builder()
            .customIds(Collections.singletonMap("custom_id", "device_1"))
            .build(),
        dump);

    assertEquals("blue", result.getValue("button_color", "fallback"));
    assertEquals(2, countTriggerEvents());
  }

  /**
   * 回归：dump 出的空 response_body（例如网络失败 / distinctId 为空时构造的空结果）必须能被 load 回来，
   * 否则 dump/load 往返在错误路径上会直接抛异常。
   */
  @Test
  public void loadAllExperimentsShouldAcceptEmptyResponseBody() throws InvalidArgumentException {
    initInstance(ABGlobalConfig.builder().setApiUrl(url).setSensorsAnalytics(sa).build());

    String dump = "{"
        + "\"distinct_id\":\"fetch_all_user\","
        + "\"is_login_id\":true,"
        + "\"custom_ids\":{},"
        + "\"response_body\":\"\","
        + "\"timestamp\":1741493784244"
        + "}";

    AllExperimentsResult result = sensorsABTest.loadAllExperiments(
        "fetch_all_user",
        true,
        LoadAllExperimentsParams.builder().build(),
        dump);

    assertEquals("fallback", result.getValue("button_color", "fallback"));
    assertFalse(result.hasParam("button_color"));
    assertEquals(1741493784244L, result.getTimestamp());
  }

  @Test(expected = IllegalArgumentException.class)
  public void loadAllExperimentsShouldRejectMissingResponseBodyField() throws InvalidArgumentException {
    initInstance(ABGlobalConfig.builder().setApiUrl(url).setSensorsAnalytics(sa).build());

    String dump = "{"
        + "\"distinct_id\":\"fetch_all_user\","
        + "\"is_login_id\":true,"
        + "\"custom_ids\":{},"
        + "\"timestamp\":1741493784244"
        + "}";

    sensorsABTest.loadAllExperiments(
        "fetch_all_user",
        true,
        LoadAllExperimentsParams.builder().build(),
        dump);
  }

  /**
   * 构造器级断言：distinctId / isLoginId 为 null 时 Builder.build() 直接抛 NPE，
   * 不变量由构造器保证，而不是延迟到 dump() / getValue()。
   */
  @Test(expected = NullPointerException.class)
  public void builderShouldRejectNullDistinctId() {
    AllExperimentsResult.builder()
        .isLoginId(true)
        .build();
  }

  @Test(expected = NullPointerException.class)
  public void builderShouldRejectNullIsLoginId() {
    AllExperimentsResult.builder()
        .distinctId("u1")
        .build();
  }

  /**
   * distinctId 是必填参数，null 必须在入口立即抛异常——不再返回 sentinel 空结果。
   */
  @Test(expected = IllegalArgumentException.class)
  public void fetchAllExperimentsShouldRejectNullDistinctId() throws InvalidArgumentException {
    initInstance(ABGlobalConfig.builder().setApiUrl(url).setSensorsAnalytics(sa).build());
    sensorsABTest.fetchAllExperiments(
        null,
        true,
        FetchAllExperimentsParams.builder().build());
  }

  @Test(expected = IllegalArgumentException.class)
  public void fetchAllExperimentsShouldRejectEmptyDistinctId() throws InvalidArgumentException {
    initInstance(ABGlobalConfig.builder().setApiUrl(url).setSensorsAnalytics(sa).build());
    sensorsABTest.fetchAllExperiments(
        "",
        true,
        FetchAllExperimentsParams.builder().build());
  }

  /**
   * customIds key 不合法（空值）时，fetchAllExperiments 必须直接抛 IllegalArgumentException，
   * 而不是静默返回空结果——与 distinctId 非法保持一致的严格契约。
   */
  @Test(expected = IllegalArgumentException.class)
  public void fetchAllExperimentsShouldRejectMalformedCustomIds() throws InvalidArgumentException {
    initInstance(ABGlobalConfig.builder().setApiUrl(url).setSensorsAnalytics(sa).build());
    sensorsABTest.fetchAllExperiments(
        "fetch_all_user",
        true,
        FetchAllExperimentsParams.builder()
            .addCustomId("custom_id", "")
            .build());
  }

  /**
   * Builder 构造器层：空串 distinctId 必须被拒绝，防止产出一个 "可 dump 但不可 load" 的半成品对象。
   */
  @Test(expected = IllegalArgumentException.class)
  public void builderShouldRejectEmptyDistinctId() {
    AllExperimentsResult.builder()
        .distinctId("")
        .isLoginId(true)
        .build();
  }

  @Test(expected = IllegalArgumentException.class)
  public void loadAllExperimentsShouldRejectMismatchedCustomIds() throws InvalidArgumentException, IOException {
    initInstance(ABGlobalConfig.builder().setApiUrl(url).setSensorsAnalytics(sa).build());

    String dump = "{"
        + "\"distinct_id\":\"fetch_all_user\","
        + "\"is_login_id\":true,"
        + "\"custom_ids\":{\"custom_id\":\"device_1\"},"
        + "\"response_body\":\"{}\","
        + "\"timestamp\":1741493784244"
        + "}";

    sensorsABTest.loadAllExperiments(
        "fetch_all_user",
        true,
        LoadAllExperimentsParams.builder()
            .customIds(Collections.singletonMap("custom_id", "device_2"))
            .enableAutoTrackEvent(false)
            .build(),
        dump);
  }

}
