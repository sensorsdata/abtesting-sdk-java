package com.sensorsdata.analytics.javasdk;

import com.google.common.collect.Maps;
import lombok.Getter;

import java.util.Map;

/**
 * AB 试验请求参数对象
 *
 * @author fangzhuo
 * @version 1.0.0
 * @since 2021/10/22 15:42
 */
@Getter
public class SensorsABParams<T> {
  /**
   * 匿名ID/业务ID
   */
  private final String distinctId;
  /**
   * 是否为登录ID
   */
  private final Boolean isLoginId;
  /**
   * 试验名称
   */
  private final String experimentVariableName;
  /**
   * 默认值
   */
  private final T defaultValue;
  /**
   * 是否开启自动上报事件，默认自动上报
   */
  private final Boolean enableAutoTrackEvent;
  /**
   * 请求超时时间，默认 3000ms
   */
  private final Integer timeoutMilliseconds;
  /**
   * 是否查询缓存,默认不查询
   */
  private Boolean enableCache;
  /**
   * 自定义属性
   */
  private final Map<String, Object> properties;
  /**
   * 自定义分流主体
   */
  private final Map<String, String> customIds;

  private SensorsABParams(Builder<T> builder) {
    this.distinctId = builder.distinctId;
    this.isLoginId = builder.isLoginId;
    this.experimentVariableName = builder.experimentVariableName;
    this.defaultValue = builder.defaultValue;
    this.enableAutoTrackEvent = builder.enableAutoTrackEvent;
    this.timeoutMilliseconds = builder.timeoutMilliseconds;
    this.enableCache = builder.enableCache;
    this.properties = builder.properties;
    this.customIds = builder.customIds;
  }

  protected SensorsABParams<T> setEnableCache(Boolean enableCache) {
    this.enableCache = enableCache;
    return this;
  }

  /**
   * 默认构造方法，但是使用上不够优雅，向前兼容版本，故不对外暴露该方法
   *
   * @param <T> 支持数据类型：number｜boolean｜String｜json
   * @return {@code Builder<T>}  请求参数构建对象
   */
  protected static <T> Builder<T> builder() {
    return new Builder<>();
  }

  /**
   * 此构造器主要是避免 Java 范型类型推断问题，所以将范型参数立即初始化，从而编译器能正确推断出类型
   *
   * @param <T>          支持数据类型：number｜boolean｜String｜json
   * @param defaultValue 未命中试验，返回默认值（支持数据类型：number｜boolean｜String｜json）
   * @return {@code Builder<T>}   请求参数构建对象
   */
  public static <T> Builder<T> starterWithDefaultValue(T defaultValue) {
    return new Builder<>(defaultValue);
  }

  /**
   * ab 试验请求所有必传参数构造方法
   *
   * @param <T>            支持数据类型：number｜boolean｜String｜json
   * @param distinctId     匿名ID/用户业务ID
   * @param isLoginId      是否为登录ID true:是登录ID，false：匿名ID
   * @param experimentName 试验变量名称
   * @param defaultValue   未命中试验，返回默认值（支持数据类型：number｜boolean｜String｜json）
   * @return {@code Builder<T>}    请求参数构建对象
   */
  public static <T> Builder<T> starter(String distinctId, Boolean isLoginId, String experimentName, T defaultValue) {
    return new Builder<>(distinctId, isLoginId, experimentName, defaultValue);
  }

  public static class Builder<T> {
    private String distinctId;
    private Boolean isLoginId;
    private String experimentVariableName;
    private T defaultValue;
    private Boolean enableAutoTrackEvent;
    private Integer timeoutMilliseconds;
    private Boolean enableCache;
    private final Map<String, Object> properties = Maps.newHashMap();
    private final Map<String, String> customIds = Maps.newHashMap();

    private Builder() {
    }

    private Builder(T defaultValue) {
      this.defaultValue = defaultValue;
    }

    private Builder(String distinctId, Boolean isLoginId, String experimentVariableName, T defaultValue) {
      this.distinctId = distinctId;
      this.isLoginId = isLoginId;
      this.experimentVariableName = experimentVariableName;
      this.defaultValue = defaultValue;
    }

    public SensorsABParams<T> build() {
      if (enableAutoTrackEvent == null) {
        enableAutoTrackEvent = true;
      }
      if (timeoutMilliseconds == null || timeoutMilliseconds <= 0) {
        timeoutMilliseconds = 3000;
      }
      if (enableCache == null) {
        enableCache = false;
      }
      return new SensorsABParams<>(this);
    }

    public SensorsABParams.Builder<T> distinctId(String distinctId) {
      this.distinctId = distinctId;
      return this;
    }

    public SensorsABParams.Builder<T> isLoginId(boolean isLoginId) {
      this.isLoginId = isLoginId;
      return this;
    }

    public SensorsABParams.Builder<T> experimentVariableName(String experimentVariableName) {
      this.experimentVariableName = experimentVariableName;
      return this;
    }

    public SensorsABParams.Builder<T> defaultValue(T defaultValue) {
      this.defaultValue = defaultValue;
      return this;
    }

    public SensorsABParams.Builder<T> enableAutoTrackEvent(boolean enableAutoTrackEvent) {
      this.enableAutoTrackEvent = enableAutoTrackEvent;
      return this;
    }

    public SensorsABParams.Builder<T> timeoutMilliseconds(int timeoutMilliseconds) {
      this.timeoutMilliseconds = timeoutMilliseconds;
      return this;
    }

    protected SensorsABParams.Builder<T> enableCache(boolean enableCache) {
      this.enableCache = enableCache;
      return this;
    }

    public SensorsABParams.Builder<T> properties(Map<String, Object> properties) {
      if (properties != null) {
        this.properties.putAll(properties);
      }
      return this;
    }

    public SensorsABParams.Builder<T> addProperty(String key, Object value) {
      this.properties.put(key, value);
      return this;
    }

    public SensorsABParams.Builder<T> customIds(Map<String, String> customIds) {
      if (customIds != null) {
        this.customIds.putAll(customIds);
      }
      return this;
    }

    public SensorsABParams.Builder<T> addCustomId(String key, String value) {
      this.customIds.put(key, value);
      return this;
    }
  }
}
