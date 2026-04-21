package com.sensorsdata.analytics.javasdk.bean;

import com.sensorsdata.analytics.javasdk.util.ABTestUtil;
import com.sensorsdata.analytics.javasdk.util.SensorsAnalyticsUtil;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;

import java.util.Collections;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Objects;

/**
 * GetAll 接口返回结果。
 *
 * @author yanming@sensorsdata.cn
 * @since 2026/03/09
 */
public class AllExperimentsResult {

  private static final ObjectMapper OBJECT_MAPPER = SensorsAnalyticsUtil.getJsonObjectMapper();

  private final String distinctId;
  private final Boolean isLoginId;
  private final Map<String, String> customIds;
  private final Map<String, Experiment<?>> experiments;
  private final TrackCallback trackCallback;
  /**
   * 原始 response body，用于与 Go SDK 共享 dump/load 协议。
   * 这里保留空字符串 "" 作为“没有可用原始响应体”的语义，
   * 不使用 "{}"，因为 "{}" 表示“存在一个 JSON 对象但内容为空/非法”，
   * 与空字符串的协议语义不同。
   */
  private final String responseBody;
  private final long timestamp;

  private AllExperimentsResult(Builder builder) {
    String distinctId = Objects.requireNonNull(builder.distinctId, "distinctId");
    if (distinctId.isEmpty()) {
      throw new IllegalArgumentException("distinctId must not be empty");
    }
    this.distinctId = distinctId;
    this.isLoginId = Objects.requireNonNull(builder.isLoginId, "isLoginId");
    this.customIds = new HashMap<>(builder.customIds);
    this.experiments = new HashMap<>(builder.experiments);
    this.trackCallback = builder.trackCallback;
    this.responseBody = builder.responseBody;
    this.timestamp = builder.timestamp;
  }

  public static Builder builder() {
    return new Builder();
  }

  public String getDistinctId() {
    return distinctId;
  }

  public Boolean getIsLoginId() {
    return isLoginId;
  }

  public Map<String, String> getCustomIds() {
    return Collections.unmodifiableMap(customIds);
  }

  public long getTimestamp() {
    return timestamp;
  }

  public String dump() {
    // distinctId / isLoginId 非空由构造器保证。
    Map<String, Object> data = new LinkedHashMap<>();
    data.put("distinct_id", distinctId);
    data.put("is_login_id", isLoginId);
    data.put("custom_ids", customIds);
    // 保持与 Go SDK 现有 dump 协议一致：没有原始响应体时序列化为 ""，而不是 "{}"。
    data.put("response_body", responseBody == null ? "" : responseBody);
    data.put("timestamp", timestamp);
    try {
      return OBJECT_MAPPER.writeValueAsString(data);
    } catch (JsonProcessingException e) {
      throw new IllegalStateException("Failed to dump all experiments result.", e);
    }
  }

  public boolean hasParam(String paramName) {
    return experiments.containsKey(paramName);
  }

  @SuppressWarnings("unchecked")
  public <T> T getValue(String paramName, T defaultValue) {
    Experiment<?> experiment = experiments.get(paramName);
    T result = defaultValue;
    if (experiment != null && isCompatibleType(defaultValue, experiment.getResult())) {
      result = (T) experiment.getResult();
    }
    triggerTrack(paramName);
    return result;
  }

  @SuppressWarnings("unchecked")
  public <T> Experiment<T> getExperiment(String paramName, T defaultValue) {
    Experiment<?> experiment = experiments.get(paramName);
    if (experiment == null || !isCompatibleType(defaultValue, experiment.getResult())) {
      return new Experiment<>(distinctId, isLoginId, defaultValue);
    }

    return Experiment.<T>builder()
        .distinctId(experiment.getDistinctId())
        .isLoginId(experiment.getIsLoginId())
        .abTestExperimentId(experiment.getAbTestExperimentId())
        .abTestExperimentGroupId(experiment.getAbTestExperimentGroupId())
        .isControlGroup(experiment.getIsControlGroup())
        .isWhiteList(experiment.getIsWhiteList())
        .abtestExperimentResultId(experiment.getAbtestExperimentResultId())
        .abtestExperimentVersion(experiment.getAbtestExperimentVersion())
        .result((T) experiment.getResult())
        .build();
  }

  private <T> boolean isCompatibleType(T defaultValue, Object result) {
    if (result == null) {
      return false;
    }
    if (defaultValue == null) {
      return ABTestUtil.assertDefaultValueType(result);
    }
    return defaultValue.getClass().isInstance(result);
  }

  private void triggerTrack(String paramName) {
    if (trackCallback != null) {
      trackCallback.track(paramName);
    }
  }

  public interface TrackCallback {
    void track(String paramName);
  }

  public static final class Builder {
    private String distinctId;
    private Boolean isLoginId;
    private final Map<String, String> customIds = new HashMap<>();
    private final Map<String, Experiment<?>> experiments = new HashMap<>();
    private TrackCallback trackCallback;
    private String responseBody;
    private long timestamp;

    public Builder distinctId(String val) {
      distinctId = val;
      return this;
    }

    public Builder isLoginId(Boolean val) {
      isLoginId = val;
      return this;
    }

    public Builder customIds(Map<String, String> val) {
      if (val != null) {
        customIds.putAll(val);
      }
      return this;
    }

    public Builder experiments(Map<String, Experiment<?>> val) {
      if (val != null) {
        experiments.putAll(val);
      }
      return this;
    }

    public Builder trackCallback(TrackCallback val) {
      trackCallback = val;
      return this;
    }

    public Builder responseBody(String val) {
      responseBody = val;
      return this;
    }

    public Builder timestamp(long val) {
      timestamp = val;
      return this;
    }

    public AllExperimentsResult build() {
      return new AllExperimentsResult(this);
    }
  }
}
