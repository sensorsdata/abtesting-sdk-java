package com.sensorsdata.analytics.javasdk.cache;

import static com.sensorsdata.analytics.javasdk.SensorsABTestConst.INVALID_ABTEST_UNIQUE_ID;
import static org.junit.Assert.assertEquals;

import com.sensorsdata.analytics.javasdk.bean.TrackRecord;
import com.sensorsdata.analytics.javasdk.bean.UserInfo;
import com.sensorsdata.analytics.javasdk.bean.cache.ExperimentGroupConfig;
import com.sensorsdata.analytics.javasdk.bean.cache.UserHitExperimentGroup;
import com.sensorsdata.analytics.javasdk.bean.cache.UserHitResult;
import com.sensorsdata.analytics.javasdk.bean.cache.UserOutExperimentGroup;
import com.sensorsdata.analytics.javasdk.bean.cache.Variable;
import com.sensorsdata.analytics.javasdk.util.LogUtil;

import com.google.common.collect.Lists;
import org.apache.commons.lang3.RandomStringUtils;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.runners.MockitoJUnitRunner;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

/**
 * @author yanming@sensorsdata.cn
 * @version 1.0.0
 * @since 2023/1/18 3:25 PM
 */
@RunWith(MockitoJUnitRunner.class)
public class HitManagerTest {

  @Mock
  private LogUtil log;

  private HitManager hitManager;


  @Before
  public void setUp() throws Exception {
    hitManager = new HitManager(log);
  }

  @After
  public void tearDown() throws Exception {
  }

  private UserInfo getRandomUserInfo() {
    return UserInfo.builder()
        .distinctId(RandomStringUtils.randomAlphanumeric(11))
        .isLoginId(false)
        .customIds(new HashMap<String, String>())
        .build();
  }

  private String getParam() {
    return RandomStringUtils.randomAlphanumeric(3);
  }

  /**
   * 本次没命中也没出组，返回空列表
   */
  @Test
  public void shouldReturnEmptyListWhenGetToTrackIfNoHitAndNoCache() {
    UserInfo randomUserInfo = getRandomUserInfo();

    assertEquals(new ArrayList<>(),
        hitManager.getToTrack(randomUserInfo, getParam(), UserHitResult.builder().build()));

  }

  /**
   * 本次命中,且有出组，返回命中记录和出组记录
   */
  @Test
  public void shouldReturnHitTrackWhenGetToTrackIfHitAndNoCache() {
    UserInfo userInfo = getRandomUserInfo();
    String param = getParam();

    List<UserOutExperimentGroup> userOutExperimentGroups = getUserOutExperimentGroups(param);
    UserOutExperimentGroup userOutExperimentGroup = UserOutExperimentGroup
        .builder()
        .abtestExperimentId(userOutExperimentGroups.get(0).getAbtestExperimentId())
        .abtestExperimentResultId(INVALID_ABTEST_UNIQUE_ID)
        .build();


    UserHitExperimentGroup userHitExperimentGroup = getUserHitExperimentGroup();
    TrackRecord expectHitTrackRecord = TrackRecord.createTrackRecord(userInfo, userHitExperimentGroup);
    TrackRecord expectOutTrackRecord =
        TrackRecord.createOutTrackRecord(userInfo, userOutExperimentGroup);
    UserHitResult userHitResult = UserHitResult.builder()
        .userHitExperimentGroup(userHitExperimentGroup)
        .userOutExperimentGroups(userOutExperimentGroups)
        .build();

    List<TrackRecord> toTrack =
        hitManager.getToTrack(userInfo, param, userHitResult);

    assertEquals(Lists.newArrayList(expectHitTrackRecord, expectOutTrackRecord), toTrack);
  }




  private static UserHitExperimentGroup getUserHitExperimentGroup() {

    String subjectName = "USER";
    ExperimentGroupConfig experimentGroupConfig = ExperimentGroupConfig
        .builder()
        .abtestExperimentId(RandomStringUtils.randomAlphanumeric(11))
        .abtestExperimentGroupId(RandomStringUtils.randomAlphanumeric(11))
        .abtestExperimentResultId(RandomStringUtils.randomAlphanumeric(11))
        .abtestExperimentVersion(RandomStringUtils.randomAlphanumeric(11))
        .subjectName(subjectName)
        .build();
    String subjectId = RandomStringUtils.randomAlphanumeric(11);
    boolean isWhiteList = false;

    return UserHitExperimentGroup.builder()
        .subjectId(subjectId)
        .isWhiteList(isWhiteList)
        .cacheable(true)
        .experimentGroupConfig(experimentGroupConfig)
        .build();

  }

  private static List<UserOutExperimentGroup> getUserOutExperimentGroups(String param) {
    final String finalParam = param;
    return Lists.newArrayList(UserOutExperimentGroup
        .builder()
        .abtestExperimentId(RandomStringUtils.randomAlphanumeric(11))
        .abtestExperimentResultId(INVALID_ABTEST_UNIQUE_ID)
        .variableMap(new HashMap<String, Variable>() {{
          put(finalParam, Variable
              .builder()
              .name(finalParam)
              .type("STRING")
              .value(RandomStringUtils.randomAlphanumeric(11))
              .build());
        }}).build());

  }
}