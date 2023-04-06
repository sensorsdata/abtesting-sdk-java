package com.sensorsdata.analytics.javasdk.track;

import static com.sensorsdata.analytics.javasdk.SensorsABTestConst.INVALID_ABTEST_UNIQUE_ID;
import static org.mockito.Matchers.anyBoolean;
import static org.mockito.Matchers.anyMap;
import static org.mockito.Matchers.anyString;
import static org.mockito.Matchers.argThat;
import static org.mockito.Matchers.eq;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.powermock.api.mockito.PowerMockito.when;

import com.sensorsdata.analytics.javasdk.ISensorsAnalytics;
import com.sensorsdata.analytics.javasdk.SensorsABTestConst;
import com.sensorsdata.analytics.javasdk.bean.Experiment;
import com.sensorsdata.analytics.javasdk.bean.TrackConfig;
import com.sensorsdata.analytics.javasdk.bean.TrackRecord;
import com.sensorsdata.analytics.javasdk.bean.UserInfo;
import com.sensorsdata.analytics.javasdk.bean.cache.ExperimentGroupConfig;
import com.sensorsdata.analytics.javasdk.bean.cache.UserHitExperimentGroup;
import com.sensorsdata.analytics.javasdk.bean.cache.UserOutExperimentGroup;
import com.sensorsdata.analytics.javasdk.cache.EventCacheManager;
import com.sensorsdata.analytics.javasdk.exceptions.InvalidArgumentException;
import com.sensorsdata.analytics.javasdk.service.impl.TrackConfigService;
import com.sensorsdata.analytics.javasdk.service.impl.TrackService;
import com.sensorsdata.analytics.javasdk.util.LogUtil;
import com.sensorsdata.analytics.javasdk.util.SensorsAnalyticsUtil;

import com.fasterxml.jackson.databind.JsonNode;
import com.google.common.collect.Lists;
import org.apache.commons.lang3.RandomStringUtils;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.ArgumentMatcher;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.runners.MockitoJUnitRunner;

import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * @author yanming@sensorsdata.cn
 * @version 1.0.0
 * @since 2023/1/20 11:57 AM
 */
@RunWith(MockitoJUnitRunner.class)
public class TrackServiceTest {

  @Mock
  private EventCacheManager eventCacheManager;

  @Mock
  private LogUtil log;


  @Mock
  private ISensorsAnalytics sensorsAnalytics;

  @Mock
  private TrackConfigService trackConfigService;


  private TrackService trackService;


  private List<TrackRecord> toTrack;

  private Map<String, String> customIds;
  private UserInfo userInfo;

  private Map<String, Object> outProperties;
  private Map<String, Object> hitProperties;

  private TrackRecord hitRecord;

  private TrackRecord outRecord;

  @Before
  public void setUp() throws Exception {
    TrackConfig defaultTrackConfig = TrackConfig.getDefaultTrackConfig();
    when(trackConfigService.getTrackConfig()).thenReturn(defaultTrackConfig);
    trackService = new TrackService(eventCacheManager, log, true, sensorsAnalytics, trackConfigService);

    String distinctId = "test_distinct_id";
    boolean isLoginId = false;
    customIds = new HashMap<>();
    customIds.put("custom_id_1", "test_custom_id_1");
    customIds.put("custom_id_2", "test_custom_id_2");

    String experimentId = "334";
    String experimentGroupId = "1";
    String abtestExperimentResultId = "3340101";
    String abtestVersion = "1";

    userInfo = UserInfo.builder()
        .distinctId(distinctId)
        .isLoginId(isLoginId)
        .customIds(customIds)
        .customProperties(new HashMap<String, Object>())
        .build();

    ExperimentGroupConfig hitExperimentGroupConfig =
        ExperimentGroupConfig.builder()
            .abtestExperimentGroupId(experimentGroupId)
            .abtestExperimentId(experimentId)
            .abtestExperimentResultId(abtestExperimentResultId)
            .abtestExperimentVersion(abtestVersion)
            .src(getHitSrc())
            .subjectName("USER")
            .build();


    UserHitExperimentGroup userHitExperimentGroup = UserHitExperimentGroup.builder()
        .subjectId(distinctId)
        .isWhiteList(false)
        .cacheable(true)
        .experimentGroupConfig(hitExperimentGroupConfig)
        .build();

    UserOutExperimentGroup userOutExperimentGroup =
        UserOutExperimentGroup.builder()
            .subjectName("USER")
            .subjectId(distinctId)
            .abtestExperimentId("test_abtest_experiment_id")
            .abtestExperimentResultId(INVALID_ABTEST_UNIQUE_ID)
            .abtestExperimentGroupId(INVALID_ABTEST_UNIQUE_ID)
            .abtestExperimentVersion("1")
            .src(getOutSrc())
            .build();

    hitRecord = TrackRecord.createTrackRecord(userInfo, userHitExperimentGroup);
    outRecord = TrackRecord.createOutTrackRecord(userInfo, userOutExperimentGroup);

    toTrack = Lists.newArrayList(hitRecord, outRecord);

    outProperties = getOutProperties(userOutExperimentGroup);
    hitProperties = getHitProperties(userHitExperimentGroup);


  }

  private Map<String, Object> getOutProperties(UserOutExperimentGroup userOutExperimentGroup) {
    Map<String, Object> properties = new HashMap<>();
    properties.putAll(customIds);
    properties.put(SensorsABTestConst.EXPERIMENT_ID, userOutExperimentGroup.getAbtestExperimentId());
    List<String> versions = new ArrayList<>();
    versions.add(
        String.format("%s:%s", SensorsABTestConst.AB_TEST_EVENT_LIB_VERSION, SensorsABTestConst.VERSION));
    properties.put(SensorsABTestConst.LIB_PLUGIN_VERSION, versions);
    properties.put("$abtest_experiment_result_id", "-1");
    properties.put("$abtest_experiment_group_id", "-1");
    properties.put("$abtest_experiment_version", userOutExperimentGroup.getAbtestExperimentVersion());
    return properties;
  }

  @After
  public void tearDown() throws Exception {
  }

  @Test
  public void whenToTrackIsNotExistThenTrackNothing() throws InvalidArgumentException {
    trackService.trackABTestTrigger(null, new HashMap<String, Object>());
    verify(sensorsAnalytics, times(0)).track(anyString(), anyBoolean(), anyString(), anyMap());
  }


  @Test
  public void whenResultExistAndEventCacheExistThenTrackNothing() throws InvalidArgumentException {

    when(eventCacheManager.judgeEventCacheExist(userInfo, hitRecord.getAbtestExperimentId(),
        hitRecord.getAbtestExperimentGroupId())).thenReturn(true);
    trackService.trackABTestTrigger(Lists.newArrayList(hitRecord), null);

    verify(sensorsAnalytics, times(0)).track(anyString(), anyBoolean(), anyString(), anyMap());
  }

  @Test
  public void whenToTrackExistThenTrackToTrack() throws InvalidArgumentException {

    trackService.trackABTestTrigger(toTrack, null);

    verify(sensorsAnalytics, times(2)).track(anyString(), anyBoolean(), anyString(), anyMap());
    verify(sensorsAnalytics, times(1)).track(eq(userInfo.getDistinctId()), eq(userInfo.isLoginId()),
        eq(SensorsABTestConst.EVENT_TYPE), argThat(new ArgumentMatcher<Map<String, Object>>() {
          @Override
          public boolean matches(Object argument) {
            Map<String, Object> args = (Map<String, Object>) argument;
            args.remove(SensorsABTestConst.LIB_PLUGIN_VERSION);
            outProperties.remove(SensorsABTestConst.LIB_PLUGIN_VERSION);
            return args.equals(outProperties);
          }
        }));
  }

  @Test
  public void whenWhiteListThenTrackNothing() throws InvalidArgumentException {

    this.hitRecord.setWhiteList(true);
    trackService.trackABTestTrigger(Lists.newArrayList(hitRecord), null);

    verify(sensorsAnalytics, times(0)).track(anyString(), anyBoolean(), anyString(), anyMap());
  }

  @Test
  public void whenPropertySwitchSetThenAddAbtestResultProperty() throws InvalidArgumentException {

    toTrack.remove(1);
    hitProperties.put("abtest_result", Lists.newArrayList(toTrack.get(0).getAbtestExperimentResultId()));

    Mockito.reset(trackConfigService);
    TrackConfig defaultTrackConfig = TrackConfig.getDefaultTrackConfig();
    defaultTrackConfig.setPropertySetSwitch(true);
    when(trackConfigService.getTrackConfig()).thenReturn(defaultTrackConfig);

    trackService.trackABTestTrigger(toTrack, null);

    verify(sensorsAnalytics, times(1)).track(eq(userInfo.getDistinctId()), eq(userInfo.isLoginId()),
        eq(SensorsABTestConst.EVENT_TYPE), eq(hitProperties));
  }


  @Test
  public void whenAddPropertiesThenTrackThese() throws InvalidArgumentException {

    Map<String, Object> properties = new HashMap<String, Object>() {{
      put("add", "add_value");
    }};
    hitProperties.putAll(properties);


    trackService.trackABTestTrigger(Lists.newArrayList(hitRecord), properties);

    verify(sensorsAnalytics, times(1)).track(eq(userInfo.getDistinctId()), eq(userInfo.isLoginId()),
        eq(SensorsABTestConst.EVENT_TYPE), argThat(new ArgumentMatcher<Map<String, Object>>() {
          @Override
          public boolean matches(Object argument) {
            Map<String, Object> args = (Map<String, Object>) argument;
            args.remove(SensorsABTestConst.LIB_PLUGIN_VERSION);
            hitProperties.remove(SensorsABTestConst.LIB_PLUGIN_VERSION);
            return args.equals(hitProperties);
          }
        }));

  }

  @Test
  public void whenHitDeviceExperimentThenTrackAnonymousIdFromDistinctId() throws InvalidArgumentException {

    hitRecord.setSubjectName("DEVICE");
    hitProperties.put("anonymous_id", hitRecord.getUserInfo().getDistinctId());


    trackService.trackABTestTrigger(Lists.newArrayList(hitRecord), null);
    verify(sensorsAnalytics, times(1)).track(eq(userInfo.getDistinctId()), eq(userInfo.isLoginId()),
        eq(SensorsABTestConst.EVENT_TYPE), argThat(new ArgumentMatcher<Map<String, Object>>() {
          @Override
          public boolean matches(Object argument) {
            Map<String, Object> args = (Map<String, Object>) argument;
            args.remove(SensorsABTestConst.LIB_PLUGIN_VERSION);
            hitProperties.remove(SensorsABTestConst.LIB_PLUGIN_VERSION);
            return args.equals(hitProperties);
          }
        }));

  }

  @Test
  public void whenHitDeviceExperimentThenTrackAnonymousIdFromSubjectId() throws InvalidArgumentException {

    hitRecord.setSubjectName("DEVICE");
    hitRecord.getUserInfo().setDistinctId(RandomStringUtils.randomAlphanumeric(11));
    hitProperties.put("anonymous_id", hitRecord.getSubjectId());


    trackService.trackABTestTrigger(Lists.newArrayList(hitRecord), null);
    verify(sensorsAnalytics, times(1)).track(eq(userInfo.getDistinctId()), eq(userInfo.isLoginId()),
        eq(SensorsABTestConst.EVENT_TYPE), argThat(new ArgumentMatcher<Map<String, Object>>() {
          @Override
          public boolean matches(Object argument) {
            Map<String, Object> args = (Map<String, Object>) argument;
            args.remove(SensorsABTestConst.LIB_PLUGIN_VERSION);
            hitProperties.remove(SensorsABTestConst.LIB_PLUGIN_VERSION);
            return args.equals(hitProperties);
          }
        }));

  }

  @Test
  public void whenOutDeviceExperimentThenTrackAnonymousIdFromSubjectId() throws InvalidArgumentException {
    outRecord.setSubjectName("DEVICE");
    outProperties.put("anonymous_id", hitRecord.getUserInfo().getDistinctId());


    trackService.trackABTestTrigger(Lists.newArrayList(outRecord), null);
    verify(sensorsAnalytics, times(1)).track(eq(userInfo.getDistinctId()), eq(userInfo.isLoginId()),
        eq(SensorsABTestConst.EVENT_TYPE), argThat(new ArgumentMatcher<Map<String, Object>>() {
          @Override
          public boolean matches(Object argument) {
            Map<String, Object> args = (Map<String, Object>) argument;
            args.remove(SensorsABTestConst.LIB_PLUGIN_VERSION);
            outProperties.remove(SensorsABTestConst.LIB_PLUGIN_VERSION);
            return args.equals(outProperties);
          }
        }));

  }


  private Map<String, Object> getDefaultProperties(Experiment<Integer> result) {
    Map<String, Object> expectProperties = new HashMap<>();
    expectProperties.putAll(customIds);
    expectProperties.put(SensorsABTestConst.EXPERIMENT_ID, result.getAbTestExperimentId());
    expectProperties.put(SensorsABTestConst.EXPERIMENT_GROUP_ID, result.getAbTestExperimentGroupId());
    List<String> versions = new ArrayList<>();
    versions.add(
        String.format("%s:%s", SensorsABTestConst.AB_TEST_EVENT_LIB_VERSION, SensorsABTestConst.VERSION));
    expectProperties.put(SensorsABTestConst.LIB_PLUGIN_VERSION, versions);
    return expectProperties;
  }

  private Map<String, Object> getHitProperties(UserHitExperimentGroup userHitExperimentGroup) {
    Map<String, Object> properties = new HashMap<>();
    properties.putAll(customIds);
    properties.put(SensorsABTestConst.EXPERIMENT_ID,
        userHitExperimentGroup.getExperimentGroupConfig().getAbtestExperimentId());
    properties.put(SensorsABTestConst.EXPERIMENT_GROUP_ID,
        userHitExperimentGroup.getExperimentGroupConfig().getAbtestExperimentGroupId());
    List<String> versions = new ArrayList<>();
    versions.add(
        String.format("%s:%s", SensorsABTestConst.AB_TEST_EVENT_LIB_VERSION, SensorsABTestConst.VERSION));
    properties.put(SensorsABTestConst.LIB_PLUGIN_VERSION, versions);
    properties.put("$abtest_experiment_result_id",
        userHitExperimentGroup.getExperimentGroupConfig().getAbtestExperimentResultId());
    properties.put("$abtest_experiment_version",
        userHitExperimentGroup.getExperimentGroupConfig().getAbtestExperimentVersion());
    return properties;
  }



  private JsonNode getOutSrc() {
    String jsonStr = "{\n"
        + "  \"abtest_experiment_id\": \"335\",\n"
        + "  \"abtest_experiment_result_id\": \"-1\",\n"
        + "  \"subject_id\": \"XXX\",\n"
        + "  \"subject_name\": \"XXX\",\n"
        + "  \"abtest_experiment_version\": \"1\", \n"
        + "  \"variables\": [\n"
        + "    {\n"
        + "      \"name\": \"ai\"\n"
        + "    }\n"
        + "  ]\n"
        + "}";
    try {
      return SensorsAnalyticsUtil.getJsonObjectMapper().readTree(jsonStr);
    } catch (IOException e) {
      throw new RuntimeException(e);
    }
  }

  private JsonNode getHitSrc() {
    String jsonStr = "{\n"
        + "                  \"abtest_experiment_id\": \"334\",\n"
        + "                  \"is_white_list\": false,\n"
        + "                  \"abtest_experiment_group_id\": \"1\",\n"
        + "\n"
        + "                  \"abtest_experiment_result_id\": \"3340101\",\n"
        + "                  \"experiment_type\": \"CODE\",\n"
        + "\n"
        + "                  \"subject_id\":\"XXX\",\n"
        + "\n"
        + "                  \"subject_name\":\"XXX\",\n"
        + "\n"
        + "                  \"variables\": [{\n"
        + "\n"
        + "                        \"name\":\"ai\",\n"
        + "\n"
        + "                        \"type\":\"STRING\",\n"
        + "\n"
        + "                        \"value\":\"AI不回复\"\n"
        + "\n"
        + "                  }],\n"
        + "                  \"abtest_experiment_version\": \"1\"    \n"
        + "           }";

    try {
      return SensorsAnalyticsUtil.getJsonObjectMapper().readTree(jsonStr);
    } catch (IOException e) {
      throw new RuntimeException(e);
    }
  }


}