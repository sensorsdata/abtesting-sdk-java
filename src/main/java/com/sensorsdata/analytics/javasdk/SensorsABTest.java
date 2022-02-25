package com.sensorsdata.analytics.javasdk;

import com.sensorsdata.analytics.javasdk.bean.ABGlobalConfig;
import com.sensorsdata.analytics.javasdk.bean.Experiment;
import com.sensorsdata.analytics.javasdk.exceptions.InvalidArgumentException;

import lombok.NonNull;

import java.util.Map;

/**
 * @author fangzhuo@sensorsdata.cn
 * @version 1.0.0
 * @since 2021/06/09 18:50
 */
public class SensorsABTest implements ISensorsABTest {

  private final SensorsABTestWorker worker;

  public SensorsABTest(ABGlobalConfig config) {
    this.worker = new SensorsABTestWorker(config);
  }

  @Override
  public <T> Experiment<T> asyncFetchABTest(@NonNull SensorsABParams<T> sensorsParams) {
    return worker.fetchABTest(sensorsParams);
  }

  @Override
  public <T> Experiment<T> asyncFetchABTest(String distinctId, boolean isLoginId, String experimentVariableName,
      T defaultValue) {
    return worker.fetchABTest(SensorsABParams.<T>builder()
        .distinctId(distinctId)
        .isLoginId(isLoginId)
        .experimentVariableName(experimentVariableName)
        .defaultValue(defaultValue)
        .build());
  }

  @Override
  public <T> Experiment<T> asyncFetchABTest(String distinctId, boolean isLoginId, String experimentVariableName,
      T defaultValue, boolean enableAutoTrackEvent) {
    return worker.fetchABTest(SensorsABParams.<T>builder()
        .distinctId(distinctId)
        .isLoginId(isLoginId)
        .experimentVariableName(experimentVariableName)
        .defaultValue(defaultValue)
        .enableAutoTrackEvent(enableAutoTrackEvent)
        .build());
  }

  @Override
  public <T> Experiment<T> asyncFetchABTest(String distinctId, boolean isLoginId, String experimentVariableName,
      T defaultValue, int timeoutMilliseconds) {
    return worker.fetchABTest(SensorsABParams.<T>builder()
        .distinctId(distinctId)
        .isLoginId(isLoginId)
        .experimentVariableName(experimentVariableName)
        .defaultValue(defaultValue)
        .timeoutMilliseconds(timeoutMilliseconds)
        .build());
  }

  @Override
  public <T> Experiment<T> asyncFetchABTest(String distinctId, boolean isLoginId, String experimentVariableName,
      T defaultValue, Map<String, Object> properties) {
    return worker.fetchABTest(SensorsABParams.<T>builder()
        .distinctId(distinctId)
        .isLoginId(isLoginId)
        .experimentVariableName(experimentVariableName)
        .defaultValue(defaultValue)
        .properties(properties)
        .build());
  }

  @Override
  public <T> Experiment<T> asyncFetchABTest(String distinctId, boolean isLoginId, String experimentVariableName,
      T defaultValue, boolean enableAutoTrackEvent, Map<String, Object> properties) {
    return worker.fetchABTest(SensorsABParams.<T>builder()
        .distinctId(distinctId)
        .isLoginId(isLoginId)
        .experimentVariableName(experimentVariableName)
        .defaultValue(defaultValue)
        .enableAutoTrackEvent(enableAutoTrackEvent)
        .properties(properties)
        .build());
  }

  @Override
  public <T> Experiment<T> asyncFetchABTest(String distinctId, boolean isLoginId, String experimentVariableName,
      T defaultValue, boolean enableAutoTrackEvent, int timeoutMilliseconds) {
    return worker.fetchABTest(SensorsABParams.<T>builder()
        .distinctId(distinctId)
        .isLoginId(isLoginId)
        .experimentVariableName(experimentVariableName)
        .defaultValue(defaultValue)
        .enableAutoTrackEvent(enableAutoTrackEvent)
        .timeoutMilliseconds(timeoutMilliseconds)
        .build());
  }

  @Override
  public <T> Experiment<T> asyncFetchABTest(String distinctId, boolean isLoginId, String experimentVariableName,
      T defaultValue, int timeoutMilliseconds, Map<String, Object> properties) {
    return worker.fetchABTest(SensorsABParams.<T>builder()
        .distinctId(distinctId)
        .isLoginId(isLoginId)
        .experimentVariableName(experimentVariableName)
        .defaultValue(defaultValue)
        .timeoutMilliseconds(timeoutMilliseconds)
        .properties(properties)
        .build());
  }

  @Override
  public <T> Experiment<T> asyncFetchABTest(String distinctId, boolean isLoginId, String experimentVariableName,
      T defaultValue, boolean enableAutoTrackEvent, int timeoutMilliseconds, Map<String, Object> properties) {
    return worker.fetchABTest(SensorsABParams.<T>builder()
        .distinctId(distinctId)
        .isLoginId(isLoginId)
        .experimentVariableName(experimentVariableName)
        .defaultValue(defaultValue)
        .enableAutoTrackEvent(enableAutoTrackEvent)
        .timeoutMilliseconds(timeoutMilliseconds)
        .properties(properties)
        .build());
  }

  @Override
  public <T> Experiment<T> fastFetchABTest(@NonNull SensorsABParams<T> sensorsParams) {
    return worker.fetchABTest(sensorsParams.setEnableCache(true));
  }

  @Override
  public <T> Experiment<T> fastFetchABTest(String distinctId, boolean isLoginId, String experimentVariableName,
      T defaultValue) {
    return worker.fetchABTest(SensorsABParams.<T>builder()
        .distinctId(distinctId)
        .isLoginId(isLoginId)
        .experimentVariableName(experimentVariableName)
        .defaultValue(defaultValue)
        .enableCache(true)
        .build());
  }

  @Override
  public <T> Experiment<T> fastFetchABTest(String distinctId, boolean isLoginId, String experimentVariableName,
      T defaultValue, boolean enableAutoTrackEvent) {
    return worker.fetchABTest(SensorsABParams.<T>builder()
        .distinctId(distinctId)
        .isLoginId(isLoginId)
        .experimentVariableName(experimentVariableName)
        .defaultValue(defaultValue)
        .enableAutoTrackEvent(enableAutoTrackEvent)
        .enableCache(true)
        .build());
  }

  @Override
  public <T> Experiment<T> fastFetchABTest(String distinctId, boolean isLoginId, String experimentVariableName,
      T defaultValue, int timeoutMilliseconds) {
    return worker.fetchABTest(SensorsABParams.<T>builder()
        .distinctId(distinctId)
        .isLoginId(isLoginId)
        .experimentVariableName(experimentVariableName)
        .defaultValue(defaultValue)
        .timeoutMilliseconds(timeoutMilliseconds)
        .enableCache(true)
        .build());
  }

  @Override
  public <T> Experiment<T> fastFetchABTest(String distinctId, boolean isLoginId, String experimentVariableName,
      T defaultValue, Map<String, Object> properties) {
    return worker.fetchABTest(SensorsABParams.<T>builder()
        .distinctId(distinctId)
        .isLoginId(isLoginId)
        .experimentVariableName(experimentVariableName)
        .defaultValue(defaultValue)
        .properties(properties)
        .enableCache(true)
        .build());
  }

  @Override
  public <T> Experiment<T> fastFetchABTest(String distinctId, boolean isLoginId, String experimentVariableName,
      T defaultValue, boolean enableAutoTrackEvent, Map<String, Object> properties) {
    return worker.fetchABTest(SensorsABParams.<T>builder()
        .distinctId(distinctId)
        .isLoginId(isLoginId)
        .experimentVariableName(experimentVariableName)
        .defaultValue(defaultValue)
        .enableAutoTrackEvent(enableAutoTrackEvent)
        .properties(properties)
        .enableCache(true)
        .build());
  }

  @Override
  public <T> Experiment<T> fastFetchABTest(String distinctId, boolean isLoginId, String experimentVariableName,
      T defaultValue, boolean enableAutoTrackEvent, int timeoutMilliseconds) {
    return worker.fetchABTest(SensorsABParams.<T>builder()
        .distinctId(distinctId)
        .isLoginId(isLoginId)
        .experimentVariableName(experimentVariableName)
        .defaultValue(defaultValue)
        .enableAutoTrackEvent(enableAutoTrackEvent)
        .timeoutMilliseconds(timeoutMilliseconds)
        .enableCache(true)
        .build());
  }

  @Override
  public <T> Experiment<T> fastFetchABTest(String distinctId, boolean isLoginId, String experimentVariableName,
      T defaultValue, int timeoutMilliseconds, Map<String, Object> properties) {
    return worker.fetchABTest(SensorsABParams.<T>builder()
        .distinctId(distinctId)
        .isLoginId(isLoginId)
        .experimentVariableName(experimentVariableName)
        .defaultValue(defaultValue)
        .timeoutMilliseconds(timeoutMilliseconds)
        .properties(properties)
        .enableCache(true)
        .build());
  }

  @Override
  public <T> Experiment<T> fastFetchABTest(String distinctId, boolean isLoginId, String experimentVariableName,
      T defaultValue, boolean enableAutoTrackEvent, int timeoutMilliseconds, Map<String, Object> properties) {
    return worker.fetchABTest(SensorsABParams.<T>builder()
        .distinctId(distinctId)
        .isLoginId(isLoginId)
        .experimentVariableName(experimentVariableName)
        .defaultValue(defaultValue)
        .enableAutoTrackEvent(enableAutoTrackEvent)
        .timeoutMilliseconds(timeoutMilliseconds)
        .properties(properties)
        .enableCache(true)
        .build());
  }

  @Override
  public <T> void trackABTestTrigger(Experiment<T> experiment) throws InvalidArgumentException {
    worker.trackABTestTrigger(experiment, null, null);
  }

  @Override
  public <T> void trackABTestTrigger(Experiment<T> experiment, Map<String, Object> properties)
      throws InvalidArgumentException {
    worker.trackABTestTrigger(experiment, properties, null);
  }

  @Override
  public <T> void trackABTestTrigger(Experiment<T> experiment, Map<String, Object> properties,
      Map<String, String> customIds)
      throws InvalidArgumentException {
    worker.trackABTestTrigger(experiment, properties, customIds);
  }

  @Override
  public void shutdown() {
    worker.shutdown();
  }
}
