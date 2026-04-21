package com.sensorsdata.analytics.javasdk;

import com.google.common.collect.Maps;

import java.util.Map;

/**
 * LoadAllExperiments 请求参数。
 *
 * @author yanming@sensorsdata.cn
 * @since 2026/04/21
 */
public class LoadAllExperimentsParams {

  private final Boolean enableAutoTrackEvent;
  private final Map<String, String> customIds;

  private LoadAllExperimentsParams(Builder builder) {
    this.enableAutoTrackEvent = builder.enableAutoTrackEvent;
    this.customIds = builder.customIds;
  }

  public static Builder builder() {
    return new Builder();
  }

  public Boolean getEnableAutoTrackEvent() {
    return enableAutoTrackEvent;
  }

  public Map<String, String> getCustomIds() {
    return customIds;
  }

  public static final class Builder {
    private Boolean enableAutoTrackEvent;
    private final Map<String, String> customIds = Maps.newHashMap();

    public LoadAllExperimentsParams build() {
      if (enableAutoTrackEvent == null) {
        enableAutoTrackEvent = true;
      }
      return new LoadAllExperimentsParams(this);
    }

    public Builder enableAutoTrackEvent(boolean val) {
      enableAutoTrackEvent = val;
      return this;
    }

    public Builder customIds(Map<String, String> val) {
      if (val != null) {
        customIds.putAll(val);
      }
      return this;
    }

    public Builder addCustomId(String key, String value) {
      customIds.put(key, value);
      return this;
    }
  }
}
