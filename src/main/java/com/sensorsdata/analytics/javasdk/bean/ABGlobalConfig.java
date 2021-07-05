package com.sensorsdata.analytics.javasdk.bean;

import com.sensorsdata.analytics.javasdk.ISensorsAnalytics;
import com.sensorsdata.analytics.javasdk.exceptions.InvalidArgumentException;

import java.io.Serializable;

/**
 * AB Testing 全局参数配置
 *
 * @author fz <fangzhuo@sensorsdata.cn>
 * @version 1.0.0
 * @since 2021/06/09 16:16
 */
public class ABGlobalConfig implements Serializable {
  private static final long serialVersionUID = 3285160955772051586L;
  /**
   * 单用户事件缓存
   */
  private final Integer eventCacheTime;
  /**
   * 事件总缓存用户量
   */
  private final Integer eventCacheSize;
  /**
   * 试验总缓存用户量
   */
  private final Integer experimentCacheSize;
  /**
   * 单用户试验缓存时间
   */
  private final Integer experimentCacheTime;
  /**
   * 是否开启 ABTestTrigger 事件
   */
  private final Boolean enableEventCache;
  /**
   * 分流试验地址
   */
  private final String apiUrl;
  /**
   * 神策分析 sa（要求使用3.2.0及以上版本）
   */
  private final ISensorsAnalytics sensorsAnalytics;
  /**
   * 是否开启运行日志
   */
  private final Boolean enableLog;

  private ABGlobalConfig(Integer eventCacheTime, Integer eventCacheSize, Integer experimentCacheSize,
      Integer experimentCacheTime, Boolean enableEventCache, String apiUrl, ISensorsAnalytics sensorsAnalytics,
      Boolean enableLog) {
    this.eventCacheTime = eventCacheTime;
    this.eventCacheSize = eventCacheSize;
    this.experimentCacheSize = experimentCacheSize;
    this.experimentCacheTime = experimentCacheTime;
    this.enableEventCache = enableEventCache;
    this.apiUrl = apiUrl;
    this.sensorsAnalytics = sensorsAnalytics;
    this.enableLog = enableLog;
  }

  public static Builder builder() {
    return new Builder();
  }

  public Integer getEventCacheTime() {
    return eventCacheTime;
  }

  public Integer getEventCacheSize() {
    return eventCacheSize;
  }

  public Integer getExperimentCacheSize() {
    return experimentCacheSize;
  }

  public Integer getExperimentCacheTime() {
    return experimentCacheTime;
  }

  public Boolean getEnableEventCache() {
    return enableEventCache;
  }

  public String getApiUrl() {
    return apiUrl;
  }

  public ISensorsAnalytics getSensorsAnalytics() {
    return sensorsAnalytics;
  }

  public Boolean getEnableLog() {
    return enableLog;
  }

  public static class Builder {
    private Integer eventCacheTime;
    private Integer eventCacheSize;
    private Integer experimentCacheSize;
    private Integer experimentCacheTime;
    private Boolean enableEventCache;
    private String apiUrl;
    private ISensorsAnalytics sensorsAnalytics;
    private Boolean enableLog;

    private Builder() {
    }

    public ABGlobalConfig build() throws InvalidArgumentException {
      if (apiUrl == null || apiUrl.length() == 0) {
        throw new InvalidArgumentException("The apiUrl is empty.");
      }
      if (sensorsAnalytics == null) {
        throw new InvalidArgumentException("The Sensors Analysis SDK is empty.");
      }
      if (eventCacheTime == null || eventCacheTime > 1440 || eventCacheTime < 0) {
        eventCacheTime = 1440;
      }
      if (eventCacheSize == null || eventCacheSize < 0) {
        eventCacheSize = 4096;
      }
      if (experimentCacheSize == null || experimentCacheSize < 0) {
        experimentCacheSize = 4096;
      }
      if (experimentCacheTime == null || experimentCacheTime > 1440 || experimentCacheTime < 0) {
        experimentCacheTime = 1440;
      }
      if (enableEventCache == null) {
        enableEventCache = true;
      }
      if (enableLog == null) {
        enableLog = false;
      }
      return new ABGlobalConfig(eventCacheTime, eventCacheSize, experimentCacheSize,
          experimentCacheTime, enableEventCache, apiUrl, sensorsAnalytics, enableLog);
    }

    public ABGlobalConfig.Builder setEventCacheTime(Integer eventCacheTime) {
      this.eventCacheTime = eventCacheTime;
      return this;
    }

    public ABGlobalConfig.Builder setEventCacheSize(Integer eventCacheSize) {
      this.eventCacheSize = eventCacheSize;
      return this;
    }

    public ABGlobalConfig.Builder setExperimentCacheSize(Integer experimentCacheSize) {
      this.experimentCacheSize = experimentCacheSize;
      return this;
    }

    public ABGlobalConfig.Builder setExperimentCacheTime(Integer experimentCacheTime) {
      this.experimentCacheTime = experimentCacheTime;
      return this;
    }

    public ABGlobalConfig.Builder enableEventCache(Boolean enableEventCache) {
      this.enableEventCache = enableEventCache;
      return this;
    }

    public ABGlobalConfig.Builder setApiUrl(String apiUrl) {
      this.apiUrl = apiUrl;
      return this;
    }

    public ABGlobalConfig.Builder setSensorsAnalytics(ISensorsAnalytics sensorsAnalytics) {
      this.sensorsAnalytics = sensorsAnalytics;
      return this;
    }

    public ABGlobalConfig.Builder enableLog(Boolean enableLog) {
      this.enableLog = enableLog;
      return this;
    }
  }
}
