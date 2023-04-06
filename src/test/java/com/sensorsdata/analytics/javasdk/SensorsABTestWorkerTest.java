package com.sensorsdata.analytics.javasdk;

import static org.junit.Assert.assertEquals;

import com.sensorsdata.analytics.javasdk.bean.ABGlobalConfig;
import com.sensorsdata.analytics.javasdk.bean.TrackRecord;
import com.sensorsdata.analytics.javasdk.bean.UserInfo;
import com.sensorsdata.analytics.javasdk.bean.cache.ExperimentGroupConfig;
import com.sensorsdata.analytics.javasdk.bean.cache.UserHitExperimentGroup;
import com.sensorsdata.analytics.javasdk.bean.cache.UserHitResult;
import com.sensorsdata.analytics.javasdk.bean.cache.UserOutExperimentGroup;
import com.sensorsdata.analytics.javasdk.bean.cache.Variable;
import com.sensorsdata.analytics.javasdk.cache.EventCacheManager;
import com.sensorsdata.analytics.javasdk.cache.ExperimentCacheManager;
import com.sensorsdata.analytics.javasdk.cache.HitManager;
import com.sensorsdata.analytics.javasdk.exceptions.InvalidArgumentException;
import com.sensorsdata.analytics.javasdk.service.ITrackConfigService;
import com.sensorsdata.analytics.javasdk.service.ITrackService;
import com.sensorsdata.analytics.javasdk.util.HttpConsumer;
import com.sensorsdata.analytics.javasdk.util.LogUtil;

import com.google.common.collect.Lists;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.runners.MockitoJUnitRunner;

import java.util.Collections;
import java.util.HashMap;
import java.util.List;

/**
 * @author yanming@sensorsdata.cn
 * @version 1.0.0
 * @since 2023/2/2 8:13 PM
 */
@RunWith(MockitoJUnitRunner.class)
public class SensorsABTestWorkerTest {
  @Mock
  private LogUtil log;

  @InjectMocks
  private HitManager hitManager;

  @Mock
  private ITrackConfigService trackConfigService;


  @Mock
  private ITrackService trackService;


  @Mock
  private HttpConsumer httpConsumer;


  @Mock
  private EventCacheManager eventCacheManager;


  @Mock
  private ExperimentCacheManager experimentCacheManager;



  private SensorsABTestWorker sensorsABTestWorker;


  @Before
  public void setUp() throws Exception {
    ABGlobalConfig config = ABGlobalConfig.builder()
        .setApiUrl("http://test.com")
        .enableEventCache(true)
        .setSensorsAnalytics(Mockito.mock(ISensorsAnalytics.class))
        .build();
    sensorsABTestWorker = new TestSensorsABTestWorker(config);

  }

  @Test
  public void whenUserHitResultTypeIsMatchWithDefaultValueThenTrackThis() {
    String distinctId = "test_id";
    final String paramName = "test";

    UserInfo userInfo = UserInfo.builder()
        .distinctId(distinctId)
        .isLoginId(false)
        .build();

    UserHitExperimentGroup userHitExperimentGroup =
        getUserHitExperimentGroup(paramName);
    UserOutExperimentGroup userOutExperimentGroup =
        getUserOutExperimentGroup(paramName);

    UserHitResult userHitResult = UserHitResult.builder()
        .userHitExperimentGroup(userHitExperimentGroup)
        .userOutExperimentGroups(Lists.newArrayList(userOutExperimentGroup))
        .build();


    List<TrackRecord> toTrack = sensorsABTestWorker.getToTrack(userInfo, paramName, "test", userHitResult);

    assertEquals(2, toTrack.size());
  }

  @Test
  public void whenUserHitResultTypeIsNotMatchWithDefaultValueThenNotTrackThisOne() throws InvalidArgumentException {

    String distinctId = "test_id";
    final String paramName = "test";

    UserInfo userInfo = UserInfo.builder()
        .distinctId(distinctId)
        .isLoginId(false)
        .build();

    UserHitExperimentGroup userHitExperimentGroup =
        getUserHitExperimentGroup(paramName);
    UserOutExperimentGroup userOutExperimentGroup =
        getUserOutExperimentGroup(paramName);

    UserHitResult userHitResult = UserHitResult.builder()
        .userHitExperimentGroup(userHitExperimentGroup)
        .userOutExperimentGroups(Lists.newArrayList(userOutExperimentGroup))
        .build();


    List<TrackRecord> toTrack = sensorsABTestWorker.getToTrack(userInfo, paramName, 1L, userHitResult);

    assertEquals(Collections.emptyList(), toTrack);

  }

  private static UserOutExperimentGroup getUserOutExperimentGroup(final String paramName) {

    return UserOutExperimentGroup.builder()
        .abtestExperimentId("1023")
        .variableMap(new HashMap<String, Variable>() {{
          put(paramName, Variable.builder()
              .value("1")
              .type("STRING")
              .name(paramName)
              .build());
        }}).build();
  }

  private static UserHitExperimentGroup getUserHitExperimentGroup(final String paramName) {

    return UserHitExperimentGroup.builder()
        .isWhiteList(false)
        .experimentGroupConfig(ExperimentGroupConfig.builder()
            .abtestExperimentId("1024")
            .abtestExperimentGroupId("1")
            .variableMap(new HashMap<String, Variable>() {{
              put(paramName, Variable.builder()
                  .value("1")
                  .type("STRING")
                  .name(paramName)
                  .build());
            }})
            .build())
        .build();

  }

  @After
  public void tearDown() throws Exception {
  }

  class TestSensorsABTestWorker extends SensorsABTestWorker {

    TestSensorsABTestWorker(ABGlobalConfig config) {
      super(config);
    }

    @Override
    protected HitManager createHitManager() {
      return hitManager;
    }

    @Override
    protected ITrackConfigService createTrackConfigService() {
      return trackConfigService;
    }

    @Override
    protected ITrackService createTrackService(ABGlobalConfig config) {
      return trackService;
    }

    @Override
    protected HttpConsumer createHttpConsumer(ABGlobalConfig config) {
      return httpConsumer;
    }

    @Override
    protected EventCacheManager createEventCacheManager(ABGlobalConfig config) {
      return eventCacheManager;
    }

    @Override
    protected ExperimentCacheManager createExperimentCacheManager(ABGlobalConfig config) {
      return experimentCacheManager;
    }
  }
}