package com.sensorsdata.analytics.javasdk.bean;

/**
 * 神策日志打印级别,日志信息详细程度
 * <p> DEBUG 大于 INFO 大于 WARN 大于 ERROR 大于 NONE </p>
 *
 * @author fangzhuo
 * @version 1.0.0
 * @since 2022/10/27 10:27
 */
public enum LogLevelEnum {
  DEBUG(0),
  INFO(1),
  WARN(2),
  ERROR(3),
  NONE(4);

  int key;

  LogLevelEnum(int key) {
    this.key = key;
  }

  public int getKey() {
    return this.key;
  }
}
