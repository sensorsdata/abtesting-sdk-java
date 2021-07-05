package com.sensorsdata.analytics.javasdk;

import com.sensorsdata.analytics.javasdk.bean.ABGlobalConfig;
import com.sensorsdata.analytics.javasdk.bean.Experiment;
import com.sensorsdata.analytics.javasdk.exceptions.InvalidArgumentException;

import java.util.Map;

/**
 * @author fz <fangzhuo@sensorsdata.cn>
 * @version 1.0.0
 * @since 2021/06/09 18:50
 */
public class SensorsABTest implements ISensorsABTest {

  private final SensorsABTestWorker worker;

  public SensorsABTest(ABGlobalConfig config) {
    this.worker = new SensorsABTestWorker(config);
  }

  @Override
  public <T> Experiment<T> asyncFetchABTest(String distinctId, boolean isLoginId, String experimentVariableName,
      T defaultValue, boolean enableAutoTrackEvent, int timeoutMilliseconds, Map<String, Object> properties) {
    return worker.fetchABTest(distinctId, isLoginId, experimentVariableName, defaultValue, enableAutoTrackEvent,
        timeoutMilliseconds, properties, false);
  }


  @Override
  public <T> Experiment<T> asyncFetchABTest(String distinctId, boolean isLoginId, String experimentVariableName,
      T defaultValue) {
    return worker.fetchABTest(distinctId, isLoginId, experimentVariableName, defaultValue, true,
        3000, null, false);
  }

  @Override
  public <T> Experiment<T> asyncFetchABTest(String distinctId, boolean isLoginId, String experimentVariableName,
      T defaultValue, boolean enableAutoTrackEvent) {
    return worker.fetchABTest(distinctId, isLoginId, experimentVariableName, defaultValue, enableAutoTrackEvent,
        3000, null, false);
  }

  @Override
  public <T> Experiment<T> asyncFetchABTest(String distinctId, boolean isLoginId, String experimentVariableName,
      T defaultValue, int timeoutMilliseconds) {
    return worker.fetchABTest(distinctId, isLoginId, experimentVariableName, defaultValue, true,
        timeoutMilliseconds, null, false);
  }

  @Override
  public <T> Experiment<T> asyncFetchABTest(String distinctId, boolean isLoginId, String experimentVariableName,
      T defaultValue, Map<String, Object> properties) {
    return worker.fetchABTest(distinctId, isLoginId, experimentVariableName, defaultValue, true,
        3000, properties, false);
  }


  @Override
  public <T> Experiment<T> asyncFetchABTest(String distinctId, boolean isLoginId, String experimentVariableName,
      T defaultValue, boolean enableAutoTrackEvent, Map<String, Object> properties) {
    return worker.fetchABTest(distinctId, isLoginId, experimentVariableName, defaultValue, enableAutoTrackEvent,
        3000, properties, false);
  }


  @Override
  public <T> Experiment<T> asyncFetchABTest(String distinctId, boolean isLoginId, String experimentVariableName,
      T defaultValue, int timeoutMilliseconds, Map<String, Object> properties) {
    return worker.fetchABTest(distinctId, isLoginId, experimentVariableName, defaultValue, true,
        timeoutMilliseconds, properties, false);
  }

  @Override
  public <T> Experiment<T> asyncFetchABTest(String distinctId, boolean isLoginId, String experimentVariableName,
      T defaultValue, boolean enableAutoTrackEvent, int timeoutMilliseconds) {
    return worker.fetchABTest(distinctId, isLoginId, experimentVariableName, defaultValue, enableAutoTrackEvent,
        timeoutMilliseconds, null, false);
  }

  @Override
  public <T> Experiment<T> fastFetchABTest(String distinctId, boolean isLoginId, String experimentVariableName,
      T defaultValue, boolean enableAutoTrackEvent, int timeoutMilliseconds, Map<String, Object> properties) {
    return worker.fetchABTest(distinctId, isLoginId, experimentVariableName, defaultValue, enableAutoTrackEvent,
        timeoutMilliseconds, properties, true);
  }

  @Override
  public <T> Experiment<T> fastFetchABTest(String distinctId, boolean isLoginId, String experimentVariableName,
      T defaultValue) {
    return worker.fetchABTest(distinctId, isLoginId, experimentVariableName, defaultValue, true,
        3000, null, true);
  }

  @Override
  public <T> Experiment<T> fastFetchABTest(String distinctId, boolean isLoginId, String experimentVariableName,
      T defaultValue, boolean enableAutoTrackEvent) {
    return worker.fetchABTest(distinctId, isLoginId, experimentVariableName, defaultValue, enableAutoTrackEvent,
        3000, null, true);
  }

  @Override
  public <T> Experiment<T> fastFetchABTest(String distinctId, boolean isLoginId, String experimentVariableName,
      T defaultValue, int timeoutMilliseconds) {
    return worker.fetchABTest(distinctId, isLoginId, experimentVariableName, defaultValue, true,
        3000, null, true);
  }

  @Override
  public <T> Experiment<T> fastFetchABTest(String distinctId, boolean isLoginId, String experimentVariableName,
      T defaultValue, Map<String, Object> properties) {
    return worker.fetchABTest(distinctId, isLoginId, experimentVariableName, defaultValue, true,
        3000, properties, true);
  }

  @Override
  public <T> Experiment<T> fastFetchABTest(String distinctId, boolean isLoginId, String experimentVariableName,
      T defaultValue, boolean enableAutoTrackEvent, Map<String, Object> properties) {
    return worker.fetchABTest(distinctId, isLoginId, experimentVariableName, defaultValue, enableAutoTrackEvent,
        3000, properties, true);
  }

  @Override
  public <T> Experiment<T> fastFetchABTest(String distinctId, boolean isLoginId, String experimentVariableName,
      T defaultValue, int timeoutMilliseconds, Map<String, Object> properties) {
    return worker.fetchABTest(distinctId, isLoginId, experimentVariableName, defaultValue, true,
        timeoutMilliseconds, properties, true);
  }

  @Override
  public <T> Experiment<T> fastFetchABTest(String distinctId, boolean isLoginId, String experimentVariableName,
      T defaultValue, boolean enableAutoTrackEvent, int timeoutMilliseconds) {
    return worker.fetchABTest(distinctId, isLoginId, experimentVariableName, defaultValue, enableAutoTrackEvent,
        timeoutMilliseconds, null, true);
  }

  @Override
  public <T> void trackABTestTrigger(Experiment<T> experiment) throws InvalidArgumentException {
    this.trackABTestTrigger(experiment, null);
  }


  @Override
  public <T> void trackABTestTrigger(Experiment<T> experiment, Map<String, Object> properties)
      throws InvalidArgumentException {
    this.worker.trackABTestTrigger(experiment, properties);
  }
}
