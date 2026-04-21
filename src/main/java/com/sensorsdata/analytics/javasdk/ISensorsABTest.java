package com.sensorsdata.analytics.javasdk;

import com.sensorsdata.analytics.javasdk.bean.AllExperimentsResult;
import com.sensorsdata.analytics.javasdk.bean.Experiment;
import com.sensorsdata.analytics.javasdk.exceptions.InvalidArgumentException;


import lombok.NonNull;

import java.util.Map;

/**
 * @author fangzhuo@sensorsdata.cn
 * @version 1.0.0
 * @since 2021/06/09 17:30
 */
public interface ISensorsABTest {

  /**
   * 立即从服务端请求，忽略内存缓存
   * <p>
   * 默认开启上报 $ABTestTrigger 事件，默认请求超时时间为 3000 ms，自定义属性为空
   * </p>
   *
   * @param <T>           支持数据类型：number｜boolean｜String｜json
   * @param sensorsParams {@code SensorsABParams<T>} 请求参数对象
   * @return {@code Experiment<T> }
   */
  <T> Experiment<T> asyncFetchABTest(@NonNull SensorsABParams<T> sensorsParams);

  /**
   * 立即从服务端请求，忽略内存缓存
   * <p>
   * 默认开启上报 $ABTestTrigger 事件，默认请求超时时间为 3000 ms，自定义属性为空
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
   * 默认请求超时时间为 3000 ms，自定义属性为空，自定义属性为空
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
   * 默认开启自动上报 $ABTestTrigger 事件，默认请求超时时间为 3000 ms，自定义属性为空，自定义属性为空
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
   * 默认开启自动上报 $ABTestTrigger 事件，默认请求超时时间为 3000 ms，自定义属性为空
   * </p>
   *
   * @param distinctId             匿名ID/用户业务ID
   * @param isLoginId              是否为登录ID true:是登录ID，false：匿名ID
   * @param experimentVariableName 试验变量名称
   * @param defaultValue           未命中试验，返回默认值（支持数据类型：number｜boolean｜String｜json）
   * @param properties             自定义属性
   * @param <T>                    支持数据类型：number｜boolean｜String｜json
   * @return {@code Experiment<T> }
   */
  <T> Experiment<T> asyncFetchABTest(String distinctId, boolean isLoginId, String experimentVariableName,
      T defaultValue, Map<String, Object> properties);

  /**
   * 立即从服务端请求，忽略内存缓存
   * <p>
   * 默认请求超时事件为 3000 ms，自定义属性为空
   * </p>
   *
   * @param distinctId             匿名ID/用户业务ID
   * @param isLoginId              是否为登录ID true:是登录ID，false：匿名ID
   * @param experimentVariableName 试验变量名称
   * @param defaultValue           未命中试验，返回默认值（支持数据类型：number｜boolean｜String｜json）
   * @param enableAutoTrackEvent   是否开启自动上报 $ABTestTrigger 事件
   * @param properties             自定义属性
   * @param <T>                    支持数据类型：number｜boolean｜String｜json
   * @return {@code Experiment<T> }
   */
  <T> Experiment<T> asyncFetchABTest(String distinctId, boolean isLoginId, String experimentVariableName,
      T defaultValue, boolean enableAutoTrackEvent, Map<String, Object> properties);

  /**
   * 立即从服务端请求，忽略内存缓存
   * <p>
   * 默认自定义属性为空,自定义属性为空
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
      T defaultValue, boolean enableAutoTrackEvent, int timeoutMilliseconds);

  /**
   * 立即从服务端请求，忽略内存缓存
   * <p>
   * 默认开启自动上报 $ABTestTrigger 事件，自定义属性为空
   * </p>
   *
   * @param distinctId             匿名ID/用户业务ID
   * @param isLoginId              是否为登录ID true:是登录ID，false：匿名ID
   * @param experimentVariableName 试验变量名称
   * @param defaultValue           未命中试验，返回默认值（支持数据类型：number｜boolean｜String｜json）
   * @param timeoutMilliseconds    请求超时设置 ms
   * @param properties             自定义属性
   * @param <T>                    支持数据类型：number｜boolean｜String｜json
   * @return {@code Experiment<T> }
   */
  <T> Experiment<T> asyncFetchABTest(String distinctId, boolean isLoginId, String experimentVariableName,
      T defaultValue, int timeoutMilliseconds, Map<String, Object> properties);

  /**
   * 立即从服务端请求，忽略内存缓存
   * <p>
   * 默认自定义属性为空
   * </p>
   *
   * @param distinctId             匿名ID/用户业务ID
   * @param isLoginId              是否为登录ID true:是登录ID，false：匿名ID
   * @param experimentVariableName 试验变量名称
   * @param defaultValue           未命中试验，返回默认值（支持数据类型：number｜boolean｜String｜json）
   * @param enableAutoTrackEvent   是否开启自动上报 $ABTestTrigger 事件
   * @param timeoutMilliseconds    请求超时设置 ms
   * @param properties       自定义属性
   * @param <T>                    支持数据类型：number｜boolean｜String｜json
   * @return {@code Experiment<T> }
   */
  <T> Experiment<T> asyncFetchABTest(String distinctId, boolean isLoginId, String experimentVariableName,
      T defaultValue, boolean enableAutoTrackEvent, int timeoutMilliseconds, Map<String, Object> properties);

  /**
   * 优先读取内存缓存，缓存不存在时从再服务端获取试验数据
   * <p>
   * 默认开启上报 $ABTestTrigger 事件，默认请求超时时间为 3000 ms，自定义属性为空，自定义分流主体为空
   * </p>
   *
   * @param <T>           支持数据类型：number｜boolean｜String｜json
   * @param sensorsParams ab 试验请求参数对象
   * @return {@code Experiment<T> }
   */
  <T> Experiment<T> fastFetchABTest(@NonNull SensorsABParams<T> sensorsParams);

  /**
   * 优先读取内存缓存，缓存不存在时从再服务端获取试验数据
   * <p>
   * 默认开启上报 $ABTestTrigger 事件，默认请求超时时间为 3000 ms，自定义属性为空，自定义分流主体为空
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
   * 默认请求超时时间为 3000 ms，自定义属性为空，自定义属性为空
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
   * 默认开启自动上报 $ABTestTrigger 事件，自定义属性为空，自定义属性为空
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
   * 默认开启自动上报 $ABTestTrigger 事件，请求超时时间为 3000 ms，自定义属性为空
   * </p>
   *
   * @param distinctId             匿名ID/用户业务ID
   * @param isLoginId              是否为登录ID true:是登录ID，false：匿名ID
   * @param experimentVariableName 试验变量名称
   * @param defaultValue           未命中试验，返回默认值（支持数据类型：number｜boolean｜String｜json）
   * @param properties             自定义属性
   * @param <T>                    支持数据类型：number｜boolean｜String｜json
   * @return {@code Experiment<T> }
   */
  <T> Experiment<T> fastFetchABTest(String distinctId, boolean isLoginId, String experimentVariableName,
      T defaultValue, Map<String, Object> properties);

  /**
   * 优先读取内存缓存，缓存不存在时从再服务端获取试验数据
   * <p>
   * 默认请求超时时间为 3000 ms，自定义属性为空
   * </p>
   *
   * @param distinctId             匿名ID/用户业务ID
   * @param isLoginId              是否为登录ID true:是登录ID，false：匿名ID
   * @param experimentVariableName 试验变量名称
   * @param defaultValue           未命中试验，返回默认值（支持数据类型：number｜boolean｜String｜json）
   * @param enableAutoTrackEvent   是否开启自动上报 $ABTestTrigger 事件
   * @param properties             自定义属性
   * @param <T>                    支持数据类型：number｜boolean｜String｜json
   * @return {@code Experiment<T> }
   */
  <T> Experiment<T> fastFetchABTest(String distinctId, boolean isLoginId, String experimentVariableName,
      T defaultValue, boolean enableAutoTrackEvent, Map<String, Object> properties);

  /**
   * 优先读取内存缓存，缓存不存在时从再服务端获取试验数据
   * <p>
   * 默认自定义属性为空
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
   * 优先读取内存缓存，缓存不存在时从再服务端获取试验数据
   * <p>
   * 默认开启自动上报 $ABTestTrigger 事件
   * </p>
   *
   * @param distinctId             匿名ID/用户业务ID
   * @param isLoginId              是否为登录ID true:是登录ID，false：匿名ID
   * @param experimentVariableName 试验变量名称
   * @param defaultValue           未命中试验，返回默认值（支持数据类型：number｜boolean｜String｜json）
   * @param timeoutMilliseconds    请求超时设置 ms
   * @param properties             自定义属性
   * @param <T>                    支持数据类型：number｜boolean｜String｜json
   * @return {@code Experiment<T> }
   */
  <T> Experiment<T> fastFetchABTest(String distinctId, boolean isLoginId, String experimentVariableName,
      T defaultValue, int timeoutMilliseconds, Map<String, Object> properties);

  /**
   * 优先读取内存缓存，缓存不存在时从再服务端获取试验数据
   * <p>
   * 默认自定义属性为空
   * </p>
   *
   * @param distinctId             匿名ID/用户业务ID
   * @param isLoginId              是否为登录ID true:是登录ID，false：匿名ID
   * @param experimentVariableName 试验变量名称
   * @param defaultValue           未命中试验，返回默认值（支持数据类型：number｜boolean｜String｜json）
   * @param enableAutoTrackEvent   是否开启自动上报 $ABTestTrigger 事件
   * @param timeoutMilliseconds    请求超时设置 ms
   * @param properties             自定义属性
   * @param <T>                    支持数据类型：number｜boolean｜String｜json
   * @return {@code Experiment<T> }
   */
  <T> Experiment<T> fastFetchABTest(String distinctId, boolean isLoginId, String experimentVariableName,
      T defaultValue, boolean enableAutoTrackEvent, int timeoutMilliseconds, Map<String, Object> properties);

  /**
   * 一次获取用户当前命中的全部试验结果。
   *
   * <p>严格校验入口参数：与 {@link #fastFetchABTest(com.sensorsdata.analytics.javasdk.SensorsABParams)}（非法入参时记录 warn 并返回 defaultValue）
   * 的宽松策略不同，本方法对非法入参直接抛 {@link IllegalArgumentException}，不返回兜底结果。
   *
   * @param distinctId 用户业务 ID / 匿名 ID，<b>必填且非空</b>
   * @param isLoginId  是否为登录 ID
   * @param params     GetAll 请求参数，不可为 {@code null}
   * @return {@code AllExperimentsResult}
   * @throws IllegalArgumentException 当 {@code distinctId} 为空、或 {@code params.customIds} 不合法时
   * @throws NullPointerException     当 {@code params} 为 {@code null} 时
   */
  AllExperimentsResult fetchAllExperiments(String distinctId, boolean isLoginId,
      FetchAllExperimentsParams params);

  /**
   * 从 dump 字符串中恢复全部试验结果。
   *
   * <p>要求 {@code distinctId}/{@code isLoginId}/{@code customIds} 与 {@code dumpData} 中的用户身份完全一致，
   * 否则视为非法调用直接抛异常。
   *
   * @param distinctId 用户业务 ID / 匿名 ID，<b>必填且非空</b>，须与 dump 中的 {@code distinct_id} 一致
   * @param isLoginId  是否为登录 ID，须与 dump 中的 {@code is_login_id} 一致
   * @param params     LoadAll 请求参数，不可为 {@code null}
   * @param dumpData   dump 字符串，不可为 {@code null}
   * @return {@code AllExperimentsResult}
   * @throws IllegalArgumentException 当 {@code dumpData} 不合法、或用户身份与 dump 不一致时
   * @throws NullPointerException     当 {@code params} 或 {@code dumpData} 为 {@code null} 时
   */
  AllExperimentsResult loadAllExperiments(String distinctId, boolean isLoginId,
      LoadAllExperimentsParams params, String dumpData);


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
  <T> void trackABTestTrigger(Experiment<T> experiment, Map<String, Object> properties)
      throws InvalidArgumentException;

  /**
   * 手动上报 $ABTestTrigger 事件
   *
   * @param <T>        支持数据类型：number｜boolean｜String｜json
   * @param experiment 试验结果
   * @param properties 请求参数
   * @param customIds  自定义分流主体
   * @throws com.sensorsdata.analytics.javasdk.exceptions.InvalidArgumentException 参数校验不合法抛出该异常
   */
  <T> void trackABTestTrigger(Experiment<T> experiment, Map<String, Object> properties,
      Map<String, String> customIds) throws InvalidArgumentException;

  void shutdown();
}
