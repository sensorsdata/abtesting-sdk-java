package com.sensorsdata.analytics.javasdk.service.impl;

import com.sensorsdata.analytics.javasdk.bean.TrackConfig;
import com.sensorsdata.analytics.javasdk.service.ITrackConfigService;
import com.sensorsdata.analytics.javasdk.util.LogUtil;

import java.util.Objects;

/**
 * @author yanming@sensorsdata.cn
 * @version 1.0.0
 * @since 2023/1/9 11:59 AM
 */

public class TrackConfigService implements ITrackConfigService {

  private volatile TrackConfig trackConfig;
  private final LogUtil log;

  public TrackConfigService(LogUtil log, TrackConfig trackConfig) {
    this.log = log;
    this.trackConfig = trackConfig;
  }

  @Override
  public TrackConfig getTrackConfig() {
    return trackConfig;
  }

  @Override
  public void updateTrackConfig(TrackConfig trackConfig) {
    if (trackConfig == null) {
      log.debug("track config response is null, skip update");
      return;
    }

    if (Objects.equals(this.trackConfig, trackConfig)) {
      log.debug("track config is not changed, skip update");
      return;
    }
    this.trackConfig = trackConfig;
    log.info("update track config success, new track config: [{}]", this.trackConfig);
  }


}
