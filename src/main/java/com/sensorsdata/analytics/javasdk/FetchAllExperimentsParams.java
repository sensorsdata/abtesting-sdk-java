package com.sensorsdata.analytics.javasdk;

import com.google.common.collect.Maps;

import java.util.Map;

/**
 * GetAll 请求参数。
 *
 * @author yanming@sensorsdata.cn
 * @since 2026/03/09
 */
public class FetchAllExperimentsParams {

  private final Boolean enableAutoTrackEvent;
  private final Integer timeoutMilliseconds;
  private final Map<String, Object> properties;
  private final Map<String, String> customIds;

  private FetchAllExperimentsParams(Builder builder) {
    this.enableAutoTrackEvent = builder.enableAutoTrackEvent;
    this.timeoutMilliseconds = builder.timeoutMilliseconds;
    this.properties = builder.properties;
    this.customIds = builder.customIds;
  }

  public static Builder builder() {
    return new Builder();
  }

  public Boolean getEnableAutoTrackEvent() {
    return enableAutoTrackEvent;
  }

  public Integer getTimeoutMilliseconds() {
    return timeoutMilliseconds;
  }

  public Map<String, Object> getProperties() {
    return properties;
  }

  public Map<String, String> getCustomIds() {
    return customIds;
  }

  public static final class Builder {
    private Boolean enableAutoTrackEvent;
    private Integer timeoutMilliseconds;
    private final Map<String, Object> properties = Maps.newHashMap();
    private final Map<String, String> customIds = Maps.newHashMap();

    public FetchAllExperimentsParams build() {
      if (enableAutoTrackEvent == null) {
        enableAutoTrackEvent = true;
      }
      if (timeoutMilliseconds == null || timeoutMilliseconds <= 0) {
        timeoutMilliseconds = 3000;
      }
      return new FetchAllExperimentsParams(this);
    }

    public Builder enableAutoTrackEvent(boolean val) {
      enableAutoTrackEvent = val;
      return this;
    }

    public Builder timeoutMilliseconds(int val) {
      timeoutMilliseconds = val;
      return this;
    }

    public Builder properties(Map<String, Object> val) {
      if (val != null) {
        properties.putAll(val);
      }
      return this;
    }

    public Builder addProperty(String key, Object value) {
      properties.put(key, value);
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
