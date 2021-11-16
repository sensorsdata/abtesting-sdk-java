package com.sensorsdata.analytics.javasdk;

import lombok.AllArgsConstructor;
import lombok.Getter;

import java.util.Map;

/**
 * 请求参数
 *
 * @author fangzhuo
 * @version 1.0.0
 * @since 2021/10/22 15:42
 */
@Getter
@AllArgsConstructor
class Params<T> {
  /**
   * 匿名ID/业务ID
   */
  String distinctId;
  /**
   * 是否为登录ID
   */
  Boolean isLoginId;
  /**
   * 试验名称
   */
  String experimentVariableName;
  /**
   * 默认值
   */
  T defaultValue;
  /**
   * 是否开启自动上报事件，默认自动上报
   */
  Boolean enableAutoTrackEvent;
  /**
   * 请求超时时间，默认 3000ms
   */
  Integer timeoutMilliseconds;
  /**
   * 是否查询缓存,默认不查询
   */
  Boolean enableCache;
  /**
   * 自定义属性
   */
  Map<String, Object> properties;

  public static <T> Builder<T> builder() {
    return new Builder<>();
  }

  public static class Builder<T> {
    String distinctId;
    Boolean isLoginId;
    String experimentVariableName;
    T defaultValue;
    Boolean enableAutoTrackEvent;
    Integer timeoutMilliseconds;
    Boolean enableCache;
    Map<String, Object> properties;

    private Builder() {
    }

    public Params<T> build() {
      if (enableAutoTrackEvent == null) {
        enableAutoTrackEvent = true;
      }
      if (timeoutMilliseconds == null || timeoutMilliseconds <= 0) {
        timeoutMilliseconds = 3000;
      }
      if (enableCache == null) {
        enableCache = false;
      }
      return new Params<>(distinctId, isLoginId, experimentVariableName, defaultValue, enableAutoTrackEvent,
          timeoutMilliseconds, enableCache, properties);
    }

    public Params.Builder<T> distinctId(String distinctId) {
      this.distinctId = distinctId;
      return this;
    }

    public Params.Builder<T> isLoginId(boolean isLoginId) {
      this.isLoginId = isLoginId;
      return this;
    }

    public Params.Builder<T> experimentVariableName(String experimentVariableName) {
      this.experimentVariableName = experimentVariableName;
      return this;
    }

    public Params.Builder<T> defaultValue(T defaultValue) {
      this.defaultValue = defaultValue;
      return this;
    }

    public Params.Builder<T> enableAutoTrackEvent(boolean enableAutoTrackEvent) {
      this.enableAutoTrackEvent = enableAutoTrackEvent;
      return this;
    }

    public Params.Builder<T> timeoutMilliseconds(int timeoutMilliseconds) {
      this.timeoutMilliseconds = timeoutMilliseconds;
      return this;
    }

    public Params.Builder<T> enableCache(boolean enableCache) {
      this.enableCache = enableCache;
      return this;
    }

    public Params.Builder<T> properties(Map<String, Object> properties) {
      this.properties = properties;
      return this;
    }
  }
}
