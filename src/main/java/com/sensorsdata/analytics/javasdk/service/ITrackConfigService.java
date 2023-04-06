package com.sensorsdata.analytics.javasdk.service;


import com.sensorsdata.analytics.javasdk.bean.TrackConfig;

/**
 * @author yanming@sensorsdata.cn
 * @version 1.0.0
 * @since 2023/1/31 2:42 PM
 */
public interface ITrackConfigService {

  TrackConfig getTrackConfig();

  void updateTrackConfig(TrackConfig trackConfig);

}
