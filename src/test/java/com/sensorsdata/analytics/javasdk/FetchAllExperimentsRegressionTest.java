package com.sensorsdata.analytics.javasdk;

import com.sensorsdata.analytics.javasdk.bean.ABGlobalConfig;
import com.sensorsdata.analytics.javasdk.bean.AllExperimentsResult;
import com.sensorsdata.analytics.javasdk.bean.Experiment;
import com.sensorsdata.analytics.javasdk.exceptions.InvalidArgumentException;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertTrue;

public class FetchAllExperimentsRegressionTest extends SensorsBaseTest {

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
      + "    },"
      + "    {"
      + "      \"abtest_experiment_id\": \"21\","
      + "      \"abtest_experiment_group_id\": \"210\","
      + "      \"abtest_experiment_result_id\": \"2100\","
      + "      \"abtest_experiment_version\": \"v2\","
      + "      \"is_control_group\": false,"
      + "      \"is_white_list\": false,"
      + "      \"cacheable\": true,"
      + "      \"subject_name\": \"USER\","
      + "      \"subject_id\": \"user_1\","
      + "      \"variables\": ["
      + "        {\"name\": \"switch_flag\", \"type\": \"BOOLEAN\", \"value\": \"true\"},"
      + "        {\"name\": \"overlap_param\", \"type\": \"STRING\", \"value\": \"normal\"}"
      + "      ]"
      + "    },"
      + "    {"
      + "      \"abtest_experiment_id\": \"22\","
      + "      \"abtest_experiment_group_id\": \"220\","
      + "      \"abtest_experiment_result_id\": \"2200\","
      + "      \"abtest_experiment_version\": \"v3\","
      + "      \"is_control_group\": false,"
      + "      \"is_white_list\": true,"
      + "      \"cacheable\": false,"
      + "      \"subject_name\": \"USER\","
      + "      \"subject_id\": \"user_1\","
      + "      \"variables\": ["
      + "        {\"name\": \"overlap_param\", \"type\": \"STRING\", \"value\": \"whitelist\"}"
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
  public void fetchAllExperimentsShouldReturnAllTypedValuesAndDelayTrack()
      throws InvalidArgumentException {
    initInstance(ABGlobalConfig.builder().setApiUrl(url).setSensorsAnalytics(sa).build());

    AllExperimentsResult result = sensorsABTest.fetchAllExperiments(
        "fetch_all_user",
        true,
        FetchAllExperimentsParams.builder().build());

    assertNotNull(result);
    assertEquals("fetch_all_user", result.getDistinctId());
    assertTrue(result.getIsLoginId());
    assertTrue(result.hasParam("button_color"));
    assertTrue(result.hasParam("page_size"));
    assertTrue(result.hasParam("switch_flag"));

    assertEquals(0, messageBuffer.length());

    assertEquals("blue", result.getValue("button_color", "fallback"));
    assertEquals(2, countTriggerEvents());
    assertEquals(10, result.getValue("page_size", -1).intValue());
    assertTrue(result.getValue("switch_flag", false));
    assertEquals("default-color", result.getValue("missing_color", "default-color"));
  }

  @Test
  public void fetchAllExperimentsShouldPreferWhitelistResultAndSupportGetExperiment()
      throws InvalidArgumentException {
    initInstance(ABGlobalConfig.builder().setApiUrl(url).setSensorsAnalytics(sa).build());

    AllExperimentsResult result = sensorsABTest.fetchAllExperiments(
        "fetch_all_user",
        false,
        FetchAllExperimentsParams.builder()
            .enableAutoTrackEvent(false)
            .build());

    assertEquals("whitelist", result.getValue("overlap_param", "fallback"));

    Experiment<String> experiment = result.getExperiment("overlap_param", "fallback");
    assertNotNull(experiment);
    assertEquals("22", experiment.getAbTestExperimentId());
    assertEquals("220", experiment.getAbTestExperimentGroupId());
    assertEquals("whitelist", experiment.getResult());
    assertTrue(experiment.getIsWhiteList());

    assertEquals(0, messageBuffer.length());

    Experiment<String> missingExperiment = result.getExperiment("not_exist", "fallback");
    assertNull(missingExperiment.getAbTestExperimentId());
    assertEquals("fallback", missingExperiment.getResult());
    assertFalse(result.hasParam("not_exist"));
  }

}
