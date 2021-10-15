package com.sensorsdata.analytics.javasdk.util;

/**
 * AB Test 工具类
 *
 * @author fangzhuo@sensorsdata.cn
 * @version 1.0.0
 * @since 2021/06/15 10:28
 */
public class ABTestUtil {

  private ABTestUtil() {
  }

  public static <T> boolean assertDefaultValueType(T value) {
    if (value instanceof Integer || value instanceof String || value instanceof Boolean) {
      return true;
    }
    return false;
  }

}
