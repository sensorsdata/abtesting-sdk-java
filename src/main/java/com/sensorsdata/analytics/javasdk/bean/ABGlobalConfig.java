package com.sensorsdata.analytics.javasdk.bean;

import com.sensorsdata.analytics.javasdk.ISensorsAnalytics;
import com.sensorsdata.analytics.javasdk.exceptions.InvalidArgumentException;


import org.apache.http.impl.client.HttpClientBuilder;
import org.apache.http.impl.client.HttpClients;

import java.io.Serializable;

/**
 * AB Testing 全局参数配置
 *
 * @author fangzhuo@sensorsdata.cn
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
   * 网络请求连接池最大请求次数
   */
  
  private final Integer maxTotal;
  /**
   * 网络请求连接池并行接收请求数量
   */
  
  private final Integer maxPerRoute;
  /**
   * 是否开启请求耗时的记录
   */

  private final Boolean enableRecordRequestCostTime;
  
  private final LogLevelEnum logLevel;

  private final transient HttpClientBuilder httpClientBuilder;


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

  public Integer getMaxTotal() {
    return maxTotal;
  }

  public Integer getMaxPerRoute() {
    return maxPerRoute;
  }

  public Boolean getEnableRecordRequestCostTime() {
    return enableRecordRequestCostTime;
  }

  public LogLevelEnum getLogLevel() {
    return logLevel;
  }

  public HttpClientBuilder getHttpClientBuilder() {
    return httpClientBuilder;
  }

  public static Builder builder() {
    return new Builder();
  }

  private ABGlobalConfig(Integer eventCacheTime, Integer eventCacheSize, Integer experimentCacheSize,
      Integer experimentCacheTime, Boolean enableEventCache, String apiUrl, ISensorsAnalytics sensorsAnalytics,
      Integer maxTotal, Integer maxPerRoute, Boolean enableRecordRequestCostTime, LogLevelEnum logLevel, HttpClientBuilder httpClientBuilder) {
    this.eventCacheTime = eventCacheTime;
    this.eventCacheSize = eventCacheSize;
    this.experimentCacheSize = experimentCacheSize;
    this.experimentCacheTime = experimentCacheTime;
    this.enableEventCache = enableEventCache;
    this.apiUrl = apiUrl;
    this.sensorsAnalytics = sensorsAnalytics;
    this.maxTotal = maxTotal;
    this.maxPerRoute = maxPerRoute;
    this.enableRecordRequestCostTime = enableRecordRequestCostTime;
    this.logLevel = logLevel;
    this.httpClientBuilder = httpClientBuilder;
  }

  @Override
  public String toString() {
    return "ABGlobalConfig{" +
        "eventCacheTime=" + eventCacheTime +
        ", eventCacheSize=" + eventCacheSize +
        ", experimentCacheSize=" + experimentCacheSize +
        ", experimentCacheTime=" + experimentCacheTime +
        ", enableEventCache=" + enableEventCache +
        ", apiUrl='" + apiUrl +
        ", sensorsAnalytics=" + sensorsAnalytics +
        ", maxTotal=" + maxTotal +
        ", maxPerRoute=" + maxPerRoute +
        ", enableRecordRequestCostTime=" + enableRecordRequestCostTime +
        ", logLevel=" + logLevel +
        '}';
  }

  public static class Builder {
    private Integer eventCacheTime;
    private Integer eventCacheSize;
    private Integer experimentCacheSize;
    private Integer experimentCacheTime;
    private Boolean enableEventCache;
    private String apiUrl;
    private ISensorsAnalytics sensorsAnalytics;
    private Integer maxTotal;
    private Integer maxPerRoute;
    private Boolean enableRecordRequestCostTime;
    private LogLevelEnum logLevel;

    private HttpClientBuilder httpClientBuilder;

    private Builder() {
    }

    public ABGlobalConfig build() throws InvalidArgumentException {
      if (apiUrl == null || apiUrl.length() == 0) {
        throw new InvalidArgumentException("The apiUrl is empty.");
      }
      if (sensorsAnalytics == null) {
        throw new InvalidArgumentException("The Sensors Analysis SDK instance is empty.");
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
      if (maxTotal == null) {
        maxTotal = 1000;
      }
      if (maxPerRoute == null) {
        maxPerRoute = 400;
      }
      if (enableRecordRequestCostTime == null) {
        enableRecordRequestCostTime = false;
      }
      if (logLevel == null) {
        logLevel = LogLevelEnum.INFO;
      }

      if(httpClientBuilder == null){
        httpClientBuilder = HttpClients.custom();
      }

      return new ABGlobalConfig(eventCacheTime, eventCacheSize, experimentCacheSize,
          experimentCacheTime, enableEventCache, apiUrl, sensorsAnalytics, maxTotal, maxPerRoute, enableRecordRequestCostTime, logLevel, httpClientBuilder);
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

    public ABGlobalConfig.Builder setMaxTotal(Integer maxTotal) {
      this.maxTotal = maxTotal;
      return this;
    }

    public ABGlobalConfig.Builder setMaxPerRoute(Integer maxPerRoute) {
      this.maxPerRoute = maxPerRoute;
      return this;
    }

    public ABGlobalConfig.Builder enableRecordRequestCostTime(Boolean enableRecordRequestCostTime) {
      this.enableRecordRequestCostTime = enableRecordRequestCostTime;
      return this;
    }

    public ABGlobalConfig.Builder setLogLevel(LogLevelEnum logLevel) {
      this.logLevel = logLevel;
      return this;
    }

    public Builder setHttpClientBuilder(HttpClientBuilder httpClientBuilder) {
      this.httpClientBuilder = httpClientBuilder;
      return this;
    }
  }
}
