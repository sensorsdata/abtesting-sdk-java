package com.sensorsdata.analytics.javasdk.bean;

/*
  存储待上报的事件记录

  @author yanming@sensorsdata.cn
 * @version 1.0.0
 * @since 2022/12/29 2:42 PM
 */

import com.sensorsdata.analytics.javasdk.bean.cache.ExperimentGroupConfig;
import com.sensorsdata.analytics.javasdk.bean.cache.UserHitExperimentGroup;
import com.sensorsdata.analytics.javasdk.bean.cache.UserOutExperimentGroup;

import com.fasterxml.jackson.databind.JsonNode;
import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class TrackRecord {
  private String param;

  private String abtestExperimentResultId;

  private String abtestExperimentId;
  private String abtestExperimentGroupId;

  private String subjectName;

  private String subjectId;

  private UserInfo userInfo;

  private boolean cacheable;

  private JsonNode src;

  private boolean isWhiteList;


  public static TrackRecord createTrackRecord(final UserInfo userInfo,
      final UserHitExperimentGroup userHitExperimentGroup) {
    ExperimentGroupConfig experimentGroupConfig = userHitExperimentGroup.getExperimentGroupConfig();
    return TrackRecord.builder()
        .userInfo(userInfo)
        .abtestExperimentResultId(experimentGroupConfig.getAbtestExperimentResultId())
        .abtestExperimentGroupId(experimentGroupConfig.getAbtestExperimentGroupId())
        .abtestExperimentId(experimentGroupConfig.getAbtestExperimentId())
        .subjectId(userHitExperimentGroup.getSubjectId())
        .subjectName(experimentGroupConfig.getSubjectName())
        .cacheable(userHitExperimentGroup.isCacheable())
        .src(experimentGroupConfig.getSrc())
        .isWhiteList(userHitExperimentGroup.isWhiteList())
        .build();
  }


  public static TrackRecord createOutTrackRecord(final UserInfo userInfo,
      UserOutExperimentGroup userOutExperimentGroup) {
    return TrackRecord.builder()
        .userInfo(userInfo)
        .subjectName(userOutExperimentGroup.getSubjectName())
        .subjectId(userOutExperimentGroup.getSubjectId())
        .abtestExperimentResultId(userOutExperimentGroup.getAbtestExperimentResultId())
        .abtestExperimentId(userOutExperimentGroup.getAbtestExperimentId())
        .abtestExperimentGroupId(userOutExperimentGroup.getAbtestExperimentGroupId())
        .src(userOutExperimentGroup.getSrc())
        .cacheable(false)
        .isWhiteList(userOutExperimentGroup.isWhiteList())
        .build();

  }

}
