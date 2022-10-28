package com.sensorsdata.analytics.javasdk.util;

import com.sensorsdata.analytics.javasdk.bean.LogLevelEnum;

import org.slf4j.Logger;


/**
 * 神策日志打印与 slf4j 结合使用，为满足个性化打印日志需求。SDK 提供了对于日志级别的控制。
 * <p>目前提供 debug info warn error none 五个级别；前四个级别对应 slf4j 的日志级别。none 表示屏蔽 SDK 所有日志</p>
 * SDK 和 项目 slf4j 日志级别优先级，取两者最低级别。所以如果想打印 SDK debug 级别日志，
 * 需将 SDK 日志级别和所依赖项目日志级别同时设置为 debug 及以上级别
 * <p>举例：SDK 日志级别 debug,项目日志级别 info。那么最终的日志文件中也不会生成 SDK debug 级别的日志,只打印出 info 级别以下日志</p>
 *
 * @author fangzhuo
 * @version 1.0.0
 * @since 2022/10/27 10:44
 */
public class LogUtil {

  private final LogLevelEnum logLevel;
  private final Logger logger;

  public LogUtil(Logger log, LogLevelEnum logLevel) {
    this.logLevel = logLevel;
    this.logger = log;
  }

  public void debug(String message, Object... params) {
    if (logLevel.getKey() <= 0) {
      logger.debug(message, params);
    }
  }

  public void info(String message, Object... params) {
    if (logLevel.getKey() <= 1) {
      logger.info(message, params);
    }
  }

  public void warn(String message, Object... params) {
    if (logLevel.getKey() <= 2) {
      logger.warn(message, params);
    }
  }

  public void error(String message, Object... params) {
    if (logLevel.getKey() <= 3) {
      logger.error(message, params);
    }
  }


}
