package com.sensorsdata.analytics.javasdk;

import com.sensorsdata.analytics.javasdk.bean.Experiment;
import com.sensorsdata.analytics.javasdk.exceptions.InvalidArgumentException;

import java.util.Map;

/**
 * @author fangzhuo@sensorsdata.cn
 * @version 1.0.0
 * @since 2021/06/09 17:30
 */
public interface ISensorsABTest {

  /**
   * 立即从服务端请求，忽略内存缓存
   *
   * @param distinctId             匿名ID/用户业务ID
   * @param isLoginId              是否为登录ID true:是登录ID，false：匿名ID
   * @param experimentVariableName 试验变量名称
   * @param defaultValue           未命中试验，返回默认值（支持数据类型：number｜boolean｜String｜json）
   * @param enableAutoTrackEvent   是否开启自动上报 $ABTestTrigger 事件
   * @param timeoutMilliseconds    请求超时设置 ms
   * @param properties             请求参数
   * @param <T>                    支持数据类型：number｜boolean｜String｜json
   * @return {@code Experiment<T> }
   */
  <T> Experiment<T> asyncFetchABTest(String distinctId, boolean isLoginId, String experimentVariableName,
      T defaultValue, boolean enableAutoTrackEvent, int timeoutMilliseconds, Map<String, Object> properties);

  /**
   * 立即从服务端请求，忽略内存缓存
   * <p>
   * 默认开启上报 $ABTestTrigger 事件，默认请求超时事件为 3000 ms，properties 为空
   * </p>
   *
   * @param distinctId             匿名ID/用户业务ID
   * @param isLoginId              是否为登录ID true:是登录ID，false：匿名ID
   * @param experimentVariableName 试验变量名称
   * @param defaultValue           未命中试验，返回默认值（支持数据类型：number｜boolean｜String｜json）
   * @param <T>                    支持数据类型：number｜boolean｜String｜json
   * @return {@code Experiment<T> }
   */
  <T> Experiment<T> asyncFetchABTest(String distinctId, boolean isLoginId, String experimentVariableName,
      T defaultValue);

  /**
   * 立即从服务端请求，忽略内存缓存
   * <p>
   * 默认请求超时事件为 3000 ms，properties为空
   * </p>
   *
   * @param distinctId             匿名ID/用户业务ID
   * @param isLoginId              是否为登录ID true:是登录ID，false：匿名ID
   * @param experimentVariableName 试验变量名称
   * @param defaultValue           未命中试验，返回默认值（支持数据类型：number｜boolean｜String｜json）
   * @param enableAutoTrackEvent   是否开启自动上报 $ABTestTrigger 事件
   * @param <T>                    支持数据类型：number｜boolean｜String｜json
   * @return {@code Experiment<T> }
   */
  <T> Experiment<T> asyncFetchABTest(String distinctId, boolean isLoginId, String experimentVariableName,
      T defaultValue, boolean enableAutoTrackEvent);

  /**
   * 立即从服务端请求，忽略内存缓存
   * <p>
   * 默认开启自动上报 $ABTestTrigger 事件，properties为空
   * </p>
   *
   * @param distinctId             匿名ID/用户业务ID
   * @param isLoginId              是否为登录ID true:是登录ID，false：匿名ID
   * @param experimentVariableName 试验变量名称
   * @param defaultValue           未命中试验，返回默认值（支持数据类型：number｜boolean｜String｜json）
   * @param timeoutMilliseconds    请求超时设置 ms
   * @param <T>                    支持数据类型：number｜boolean｜String｜json
   * @return {@code Experiment<T> }
   */
  <T> Experiment<T> asyncFetchABTest(String distinctId, boolean isLoginId, String experimentVariableName,
      T defaultValue, int timeoutMilliseconds);

  /**
   * 立即从服务端请求，忽略内存缓存
   * <p>
   * 默认开启自动上报 $ABTestTrigger 事件，默认请求超时事件为 3000 ms
   * </p>
   *
   * @param distinctId             匿名ID/用户业务ID
   * @param isLoginId              是否为登录ID true:是登录ID，false：匿名ID
   * @param experimentVariableName 试验变量名称
   * @param defaultValue           未命中试验，返回默认值（支持数据类型：number｜boolean｜String｜json）
   * @param properties             请求参数
   * @param <T>                    支持数据类型：number｜boolean｜String｜json
   * @return {@code Experiment<T> }
   */
  <T> Experiment<T> asyncFetchABTest(String distinctId, boolean isLoginId, String experimentVariableName,
      T defaultValue, Map<String, Object> properties);

  /**
   * 立即从服务端请求，忽略内存缓存
   * <p>
   * 默认请求超时事件为 3000 ms
   * </p>
   *
   * @param distinctId             匿名ID/用户业务ID
   * @param isLoginId              是否为登录ID true:是登录ID，false：匿名ID
   * @param experimentVariableName 试验变量名称
   * @param defaultValue           未命中试验，返回默认值（支持数据类型：number｜boolean｜String｜json）
   * @param enableAutoTrackEvent   是否开启自动上报 $ABTestTrigger 事件
   * @param properties             请求参数
   * @param <T>                    支持数据类型：number｜boolean｜String｜json
   * @return {@code Experiment<T> }
   */
  <T> Experiment<T> asyncFetchABTest(String distinctId, boolean isLoginId, String experimentVariableName,
      T defaultValue, boolean enableAutoTrackEvent, Map<String, Object> properties);

  /**
   * 立即从服务端请求，忽略内存缓存
   * <p>
   * 默认开启自动上报 $ABTestTrigger 事件
   * </p>
   *
   * @param distinctId             匿名ID/用户业务ID
   * @param isLoginId              是否为登录ID true:是登录ID，false：匿名ID
   * @param experimentVariableName 试验变量名称
   * @param defaultValue           未命中试验，返回默认值（支持数据类型：number｜boolean｜String｜json）
   * @param timeoutMilliseconds    请求超时设置 ms
   * @param properties             请求参数
   * @param <T>                    支持数据类型：number｜boolean｜String｜json
   * @return {@code Experiment<T> }
   */
  <T> Experiment<T> asyncFetchABTest(String distinctId, boolean isLoginId, String experimentVariableName,
      T defaultValue, int timeoutMilliseconds, Map<String, Object> properties);

  /**
   * 立即从服务端请求，忽略内存缓存
   * <p>
   * 默认 properties 为空
   * </p>
   *
   * @param distinctId             匿名ID/用户业务ID
   * @param isLoginId              是否为登录ID true:是登录ID，false：匿名ID
   * @param experimentVariableName 试验变量名称
   * @param defaultValue           未命中试验，返回默认值（支持数据类型：number｜boolean｜String｜json）
   * @param timeoutMilliseconds    请求超时设置 ms
   * @param enableAutoTrackEvent   是否开启自动上报 $ABTestTrigger 事件
   * @param <T>                    支持数据类型：number｜boolean｜String｜json
   * @return {@code Experiment<T> }
   */
  <T> Experiment<T> asyncFetchABTest(String distinctId, boolean isLoginId, String experimentVariableName,
      T defaultValue, boolean enableAutoTrackEvent, int timeoutMilliseconds)
  ;

  /**
   * 优先读取内存缓存，缓存不存在时从再服务端获取试验数据
   *
   * @param distinctId             匿名ID/用户业务ID
   * @param isLoginId              是否为登录ID true:是登录ID，false：匿名ID
   * @param experimentVariableName 试验变量名称
   * @param defaultValue           未命中试验，返回默认值（支持数据类型：number｜boolean｜String｜json）
   * @param enableAutoTrackEvent   是否开启自动上报 $ABTestTrigger 事件
   * @param timeoutMilliseconds    请求超时设置 ms
   * @param properties             请求参数
   * @param <T>                    支持数据类型：number｜boolean｜String｜json
   * @return {@code Experiment<T> }
   */
  <T> Experiment<T> fastFetchABTest(String distinctId, boolean isLoginId, String experimentVariableName,
      T defaultValue, boolean enableAutoTrackEvent, int timeoutMilliseconds, Map<String, Object> properties);

  /**
   * 优先读取内存缓存，缓存不存在时从再服务端获取试验数据
   * <p>
   * 默认开启上报 $ABTestTrigger 事件，默认请求超时事件为 3000 ms
   * </p>
   *
   * @param distinctId             匿名ID/用户业务ID
   * @param isLoginId              是否为登录ID true:是登录ID，false：匿名ID
   * @param experimentVariableName 试验变量名称
   * @param defaultValue           未命中试验，返回默认值（支持数据类型：number｜boolean｜String｜json）
   * @param <T>                    支持数据类型：number｜boolean｜String｜json
   * @return {@code Experiment<T> }
   */
  <T> Experiment<T> fastFetchABTest(String distinctId, boolean isLoginId, String experimentVariableName,
      T defaultValue);

  /**
   * 优先读取内存缓存，缓存不存在时从再服务端获取试验数据
   * <p>
   * 默认请求超时事件为 3000 ms，properties 为空
   * </p>
   *
   * @param distinctId             匿名ID/用户业务ID
   * @param isLoginId              是否为登录ID true:是登录ID，false：匿名ID
   * @param experimentVariableName 试验变量名称
   * @param defaultValue           未命中试验，返回默认值（支持数据类型：number｜boolean｜String｜json）
   * @param enableAutoTrackEvent   是否开启自动上报 $ABTestTrigger 事件
   * @param <T>                    支持数据类型：number｜boolean｜String｜json
   * @return {@code Experiment<T> }
   */
  <T> Experiment<T> fastFetchABTest(String distinctId, boolean isLoginId, String experimentVariableName,
      T defaultValue, boolean enableAutoTrackEvent);

  /**
   * 优先读取内存缓存，缓存不存在时从再服务端获取试验数据
   * <p>
   * 默认开启上报 $ABTestTrigger 事件, properties 为空
   * </p>
   *
   * @param distinctId             匿名ID/用户业务ID
   * @param isLoginId              是否为登录ID true:是登录ID，false：匿名ID
   * @param experimentVariableName 试验变量名称
   * @param defaultValue           未命中试验，返回默认值（支持数据类型：number｜boolean｜String｜json）
   * @param timeoutMilliseconds    请求超时设置 ms
   * @param <T>                    支持数据类型：number｜boolean｜String｜json
   * @return {@code Experiment<T> }
   */
  <T> Experiment<T> fastFetchABTest(String distinctId, boolean isLoginId, String experimentVariableName,
      T defaultValue, int timeoutMilliseconds);

  /**
   * 优先读取内存缓存，缓存不存在时从再服务端获取试验数据
   * <p>
   * 默认开启上报 $ABTestTrigger 事件,默认请求超时事件为 3000 ms
   * </p>
   *
   * @param distinctId             匿名ID/用户业务ID
   * @param isLoginId              是否为登录ID true:是登录ID，false：匿名ID
   * @param experimentVariableName 试验变量名称
   * @param defaultValue           未命中试验，返回默认值（支持数据类型：number｜boolean｜String｜json）
   * @param properties             请求参数
   * @param <T>                    支持数据类型：number｜boolean｜String｜json
   * @return {@code Experiment<T> }
   */
  <T> Experiment<T> fastFetchABTest(String distinctId, boolean isLoginId, String experimentVariableName,
      T defaultValue, Map<String, Object> properties);

  /**
   * 优先读取内存缓存，缓存不存在时从再服务端获取试验数据
   * <p>
   * 默认请求超时事件为 3000 ms
   * </p>
   *
   * @param distinctId             匿名ID/用户业务ID
   * @param isLoginId              是否为登录ID true:是登录ID，false：匿名ID
   * @param experimentVariableName 试验变量名称
   * @param defaultValue           未命中试验，返回默认值（支持数据类型：number｜boolean｜String｜json）
   * @param enableAutoTrackEvent   是否开启自动上报 $ABTestTrigger 事件
   * @param properties             请求参数
   * @param <T>                    支持数据类型：number｜boolean｜String｜json
   * @return {@code Experiment<T> }
   */
  <T> Experiment<T> fastFetchABTest(String distinctId, boolean isLoginId, String experimentVariableName,
      T defaultValue, boolean enableAutoTrackEvent, Map<String, Object> properties);

  /**
   * 优先读取内存缓存，缓存不存在时从再服务端获取试验数据
   * <p>
   * 默认开启上报 $ABTestTrigger 事件
   * </p>
   *
   * @param distinctId             匿名ID/用户业务ID
   * @param isLoginId              是否为登录ID true:是登录ID，false：匿名ID
   * @param experimentVariableName 试验变量名称
   * @param defaultValue           未命中试验，返回默认值（支持数据类型：number｜boolean｜String｜json）
   * @param timeoutMilliseconds    请求超时设置 ms
   * @param properties             请求参数
   * @param <T>                    支持数据类型：number｜boolean｜String｜json
   * @return {@code Experiment<T> }
   */
  <T> Experiment<T> fastFetchABTest(String distinctId, boolean isLoginId, String experimentVariableName,
      T defaultValue, int timeoutMilliseconds, Map<String, Object> properties);

  /**
   * 优先读取内存缓存，缓存不存在时从再服务端获取试验数据
   * <p>
   * 默认 properties 为空
   * </p>
   *
   * @param distinctId             匿名ID/用户业务ID
   * @param isLoginId              是否为登录ID true:是登录ID，false：匿名ID
   * @param experimentVariableName 试验变量名称
   * @param defaultValue           未命中试验，返回默认值（支持数据类型：number｜boolean｜String｜json）
   * @param timeoutMilliseconds    请求超时设置 ms
   * @param enableAutoTrackEvent   是否开启自动上报 $ABTestTrigger 事件
   * @param <T>                    支持数据类型：number｜boolean｜String｜json
   * @return {@code Experiment<T> }
   */
  <T> Experiment<T> fastFetchABTest(String distinctId, boolean isLoginId, String experimentVariableName,
      T defaultValue, boolean enableAutoTrackEvent, int timeoutMilliseconds);

  /**
   * 手动上报 $ABTestTrigger 事件
   *
   * @param <T>        支持数据类型：number｜boolean｜String｜json
   * @param experiment 试验结果
   * @throws com.sensorsdata.analytics.javasdk.exceptions.InvalidArgumentException 参数校验不合法抛出该异常
   */
  <T> void trackABTestTrigger(Experiment<T> experiment) throws InvalidArgumentException;

  /**
   * 手动上报 $ABTestTrigger 事件
   *
   * @param <T>        支持数据类型：number｜boolean｜String｜json
   * @param experiment 试验结果
   * @param properties 请求参数
   * @throws com.sensorsdata.analytics.javasdk.exceptions.InvalidArgumentException 参数校验不合法抛出该异常
   */
  <T> void trackABTestTrigger(Experiment<T> experiment, Map<String, Object> properties) throws InvalidArgumentException;

  void shutdown();
}
