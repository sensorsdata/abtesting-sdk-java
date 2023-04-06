package com.sensorsdata.analytics.javasdk.cache;

import com.sensorsdata.analytics.javasdk.bean.TrackRecord;
import com.sensorsdata.analytics.javasdk.bean.UserInfo;
import com.sensorsdata.analytics.javasdk.bean.cache.UserHitExperimentGroup;
import com.sensorsdata.analytics.javasdk.bean.cache.UserHitResult;
import com.sensorsdata.analytics.javasdk.bean.cache.UserOutExperimentGroup;
import com.sensorsdata.analytics.javasdk.util.LogUtil;

import java.util.ArrayList;
import java.util.List;

/**
 * 用户命中记录缓存
 *
 * @author yanming@sensorsdata.cn
 * @version 1.0.0
 * @since 2023/1/3 11:20 AM
 */
public class HitManager {

  private final LogUtil log;

  public HitManager(LogUtil log) {
    this.log = log;
  }

  /**
   * 获取埋点的track列表
   *
   * @param userInfo      用户信息标识
   * @param param         参数
   * @param userHitResult 本次命中、出组结果
   * @return 埋点track列表
   */
  public List<TrackRecord> getToTrack(UserInfo userInfo, String param, UserHitResult userHitResult) {
    List<TrackRecord> toTrack =
        processHitExperiment(userInfo, param, userHitResult.getUserHitExperimentGroup());
    return processOutExperiment(userInfo, param, toTrack, userHitResult.getUserOutExperimentGroups());
  }

  private List<TrackRecord> processOutExperiment(
      UserInfo userInfo, String param, final List<TrackRecord> toTrack,
      List<UserOutExperimentGroup> userOutExperimentGroups) {
    if (userOutExperimentGroups == null || userOutExperimentGroups.isEmpty()) {
      log.debug("out results is not exist or empty, skip! userInfo: [{}], param: [{}]", userInfo, param);
      return toTrack;
    }

    for (UserOutExperimentGroup result : userOutExperimentGroups) {
        TrackRecord outTrackRecord =
            TrackRecord.createOutTrackRecord(userInfo, result);
        toTrack.add(outTrackRecord);
      }
    return toTrack;
  }


  private List<TrackRecord> processHitExperiment(UserInfo userInfo, final String param,
      final UserHitExperimentGroup userHitExperimentGroup) {

    List<TrackRecord> toTrack = new ArrayList<>();
    //本次没命中,但是之前命中过, 需要删掉之前的记录，并上报出组
    if (userHitExperimentGroup == null) {
      log.debug("hit no experiment, skip, userInfo: [{}], param: [{}]", userInfo, param);
      return toTrack;
    }

    TrackRecord newTrackRecord = TrackRecord.createTrackRecord(userInfo, userHitExperimentGroup);
    toTrack.add(newTrackRecord);
    log.debug(
        "add hit track record, userInfo: [{}], param: [{}], experimentId: [{}], experimentGroupId: [{}], abtestExperimentVersion: [{}]",
        userInfo, param, newTrackRecord.getAbtestExperimentId(), newTrackRecord.getAbtestExperimentGroupId(),
        newTrackRecord.getAbtestExperimentResultId());

    return toTrack;

  }



}
