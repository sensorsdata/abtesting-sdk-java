package com.sensorsdata.analytics.javasdk.service;

import com.sensorsdata.analytics.javasdk.bean.TrackRecord;
import com.sensorsdata.analytics.javasdk.exceptions.InvalidArgumentException;

import java.util.List;
import java.util.Map;

/**
 * @author yanming@sensorsdata.cn
 * @version 1.0.0
 * @since 2023/1/31 10:44 AM
 */
public interface ITrackService {

  void trackABTestTrigger(List<TrackRecord> toTrack, Map<String, Object> additionalProperties) throws InvalidArgumentException;
}
