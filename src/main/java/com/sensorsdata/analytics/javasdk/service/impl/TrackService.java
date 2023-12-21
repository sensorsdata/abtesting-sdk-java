package com.sensorsdata.analytics.javasdk.service.impl;

import static com.sensorsdata.analytics.javasdk.SensorsABTestConst.INVALID_ABTEST_UNIQUE_ID;

import com.sensorsdata.analytics.javasdk.ISensorsAnalytics;
import com.sensorsdata.analytics.javasdk.SensorsABTestConst;
import com.sensorsdata.analytics.javasdk.bean.TrackConfig;
import com.sensorsdata.analytics.javasdk.bean.TrackRecord;
import com.sensorsdata.analytics.javasdk.cache.EventCacheManager;
import com.sensorsdata.analytics.javasdk.exceptions.InvalidArgumentException;
import com.sensorsdata.analytics.javasdk.service.ITrackConfigService;
import com.sensorsdata.analytics.javasdk.service.ITrackService;
import com.sensorsdata.analytics.javasdk.util.LogUtil;

import com.fasterxml.jackson.databind.JsonNode;
import com.google.common.collect.Lists;
import com.google.common.collect.Maps;
import org.apache.commons.lang3.time.DateFormatUtils;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * 埋点配置管理
 *
 * @author yanming@sensorsdata.cn
 * @version 1.0.0
 * @since 2022/12/29 2:36 PM
 */
public class TrackService implements ITrackService {

  /**
   * 事件上报管理器
   */
  private final EventCacheManager eventCacheManager;

  /**
   * log实例
   */
  private final LogUtil log;

  /**
   * 是否当天首次触发
   */
  private volatile String trigger;

  /**
   * 是否启用事件缓存
   */
  private final boolean enableEventCache;

  /**
   * SA实例，用于上报事件
   */
  private final ISensorsAnalytics sensorsAnalytics;


  private final ITrackConfigService trackConfigService;


  public TrackService(EventCacheManager eventCacheManager, LogUtil log, boolean enableEventCache,
      ISensorsAnalytics sensorsAnalytics, ITrackConfigService trackConfigService) {
    this.eventCacheManager = eventCacheManager;
    this.log = log;
    this.enableEventCache = enableEventCache;
    this.sensorsAnalytics = sensorsAnalytics;
    this.trackConfigService = trackConfigService;
  }


  private String findField(JsonNode node, String name) {
    if (node == null) {
      return null;
    }

    try {
      JsonNode value = node.findValue(name);
      if (value != null) {
        return value.asText();
      }
    } catch (Exception e) {
      log.warn("find field exception, node: [{}], fieldName: [{}]", node, name, e);
    }
    return null;
  }



  private void firstTrigger(TrackRecord trackRecord, Map<String, Object> currentProperties) {
    String abTestExperimentId = trackRecord.getAbtestExperimentId();
    String abTestExperimentGroupId = trackRecord.getAbtestExperimentGroupId();
    //判断是否为当天首次上传，重启服务，升级 SDK 版本都会触发
    String day = DateFormatUtils.ISO_8601_EXTENDED_DATE_FORMAT.format(Calendar.getInstance());
    if (trigger == null || trigger.isEmpty() || !day.equals(trigger)) {
      trigger = day;
      List<String> versions = Lists.newArrayList();
      versions.add(
          String.format("%s:%s", SensorsABTestConst.AB_TEST_EVENT_LIB_VERSION, SensorsABTestConst.VERSION));
      currentProperties.put(SensorsABTestConst.LIB_PLUGIN_VERSION, versions);
      log.debug(
          "Meet the conditions:the first event of current day,the first events of server.[userInfo:{},experimentId:{},abTestExperimentGroupId:{}]",
          trackRecord.getUserInfo(), abTestExperimentId, abTestExperimentGroupId);
    }
  }

  private boolean checkTrackRecord(TrackRecord trackRecord) {
    if (trackRecord.isWhiteList() || trackRecord.getAbtestExperimentId() == null) {
      log.debug("The track ABTest event user not hit experiment or in the whiteList.[userInfo:{}]",
          trackRecord.getUserInfo());
      return true;
    }
    return false;
  }


  private String getExtPropertyName(String ext) {
    return "$" + ext;
  }

  /**
   * 补充$ABTestTrigger事件扩展属性
   *
   * @param trackConfig       埋点配置
   * @param trackRecord       埋点记录
   * @param currentProperties 当前属性Map
   */
  private void supplyExtProperties(TrackConfig trackConfig, TrackRecord trackRecord,
      Map<String, Object> currentProperties) {
    List<String> triggerContentExt = trackConfig.getTriggerContentExt();
    for (String ext : triggerContentExt) {
      String field = findField(trackRecord.getSrc(), ext);
      if (field != null) {
        log.debug("field is not null, set property, name: [{}], value: [{}]", ext, field);
        currentProperties.put(getExtPropertyName(ext), field);
      }
    }
  }


  /**
   * 触发事件上报
   *
   * @param toTrack    带上报的埋点记录
   * @param properties 附加属性
   * @throws com.sensorsdata.analytics.javasdk.exceptions.InvalidArgumentException 参数校验不合法抛出该异常
   */
  @Override
  public void trackABTestTrigger(List<TrackRecord> toTrack, Map<String, Object> properties)
      throws InvalidArgumentException {

    if (properties == null) {
      properties = Maps.newHashMap();
    }
    if (toTrack == null) {
      toTrack = new ArrayList<>();
    }
    // 获取埋点配置
    TrackConfig trackConfig = trackConfigService.getTrackConfig();
    /*
      track record记录的情况大多数为跳组、出组的情况，不能走缓存
      因为在后端多实例部署的情况下，无法确定上次请求命中的是哪个实例，命中的是哪个组
      如果有事件缓存，可能会造成进组事件无法及时上报
     */


    for (TrackRecord trackRecord : toTrack) {
      String distinctId = trackRecord.getUserInfo().getDistinctId();
      boolean isLoginId = trackRecord.getUserInfo().isLoginId();
      String abTestExperimentId = trackRecord.getAbtestExperimentId();
      String abTestExperimentGroupId = trackRecord.getAbtestExperimentGroupId();
      if (checkTrackRecord(trackRecord)) {
        continue;
      }
      if (trackConfig.isTriggerSwitch()) {
        HashMap<String, Object> currentProperties = Maps.newHashMap(properties);

        if (isHitCache(trackRecord)) continue;

        currentProperties.put(SensorsABTestConst.EXPERIMENT_ID, abTestExperimentId);
        //出组的情况没试验组字段
        if (abTestExperimentGroupId != null) {
          currentProperties.put(SensorsABTestConst.EXPERIMENT_GROUP_ID, abTestExperimentGroupId);
        }
        putCustomIds(trackRecord, currentProperties);
        putAnonymousId(trackRecord, currentProperties);
        supplyExtProperties(trackConfig, trackRecord, currentProperties);
        firstTrigger(trackRecord, currentProperties);
        if (trackConfig.isPropertySetSwitch()) {
          propertySet(trackRecord, currentProperties);
        }
        track(trackRecord, distinctId, isLoginId, currentProperties);
      }


    }
  }

  private void putAnonymousId(TrackRecord trackRecord, HashMap<String, Object> currentProperties) {

    if (trackRecord.getSubjectName() != null && SensorsABTestConst.DEVICE_SUBJECT_NAME.equalsIgnoreCase(
        trackRecord.getSubjectName())) {
      if (trackRecord.getSubjectId() != null && !trackRecord.getSubjectId().isEmpty()) {
        currentProperties.put(SensorsABTestConst.ANONYMOUS_ID, trackRecord.getSubjectId());
      } else if (!trackRecord.getUserInfo().isLoginId()) {
        currentProperties.put(SensorsABTestConst.ANONYMOUS_ID, trackRecord.getUserInfo().getDistinctId());
      }
    }

  }

  private void track(TrackRecord trackRecord, String distinctId, boolean isLoginId,
      Map<String, Object> currentProperties) throws InvalidArgumentException {
    String abTestExperimentId = trackRecord.getAbtestExperimentId();
    String abTestExperimentGroupId = trackRecord.getAbtestExperimentGroupId();
    this.sensorsAnalytics.track(distinctId, isLoginId, SensorsABTestConst.EVENT_TYPE, currentProperties);
    log.debug(
        "Successfully trigger AB event.[userInfo:{},experimentId:{},abTestExperimentGroupId:{}]",
        trackRecord.getUserInfo(), abTestExperimentId, abTestExperimentGroupId);
    //判断是否需要缓存上报事件
    if (enableEventCache && trackRecord.isCacheable()) {
      log.debug(
          "Enable event cache,will cache event.[userInfo:{},experimentId:{},abTestExperimentGroupId:{}]",
          trackRecord.getUserInfo(), abTestExperimentId, abTestExperimentGroupId);
      eventCacheManager.setEventCache(trackRecord.getUserInfo(), abTestExperimentId, abTestExperimentGroupId);
    }
  }

  private boolean isHitCache(TrackRecord trackRecord) {
    if (trackRecord.isCacheable() && eventCacheManager.judgeEventCacheExist(
        trackRecord.getUserInfo(), trackRecord.getAbtestExperimentId(), trackRecord.getAbtestExperimentGroupId())) {
      log.info("The event has been triggered.[userInfo:{},experimentId:{},abTestExperimentGroupId:{}]",
          trackRecord.getUserInfo(),
          trackRecord.getAbtestExperimentId(), trackRecord.getAbtestExperimentGroupId());
      return true;
    }
    return false;
  }

  private void putCustomIds(TrackRecord trackRecord, HashMap<String, Object> currentProperties) {
    Map<String, String> customIds = trackRecord.getUserInfo().getCustomIds();
    if (null != customIds) {
      currentProperties.putAll(customIds);
    }
  }

  private void propertySet(TrackRecord trackRecord, Map<String, Object> currentProperties) {
    if (trackRecord.getAbtestExperimentResultId() != null && !INVALID_ABTEST_UNIQUE_ID.equals(
        trackRecord.getAbtestExperimentResultId())) {
      currentProperties.put("abtest_result", Lists.newArrayList(trackRecord.getAbtestExperimentResultId()));
    }
  }
}
