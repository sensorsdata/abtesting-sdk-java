package com.sensorsdata.analytics.javasdk.util;

import com.sensorsdata.analytics.javasdk.exceptions.InvalidArgumentException;

import org.apache.commons.lang3.time.DateFormatUtils;

import java.util.Collections;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.regex.Pattern;

/**
 * AB Test 工具类
 *
 * @author fangzhuo@sensorsdata.cn
 * @version 1.0.0
 * @since 2021/06/15 10:28
 */
public class ABTestUtil {

  /**
   * 自定义属性名匹配规则
   */
  private static final Pattern pattern = Pattern.compile("^([a-z]|[A-Z]|_)([a-z]|[A-Z]|[0-9]|_)*$");

  private ABTestUtil() {
  }

  public static <T> boolean assertDefaultValueType(T value) {
    return value instanceof Integer || value instanceof String || value instanceof Boolean;
  }

  public static <T> Map<String, Object> customPropertiesHandler(Map<String, Object> customProperties)
      throws InvalidArgumentException {
    if (customProperties == null || customProperties.isEmpty()) {
      return Collections.emptyMap();
    }
    HashMap<String, Object> tempMap = new HashMap<>(customProperties.size());
    for (String key : customProperties.keySet()) {
      if (key.length() > 100) {
        throw new InvalidArgumentException(String.format("The property name %s is too long, max length is 100.", key));
      }
      if (!pattern.matcher(key).matches()) {
        throw new InvalidArgumentException(String.format("The property name %s is invalid format.", key));
      }
      Object value = customProperties.get(key);
      if (!(value instanceof Number) && !(value instanceof Date) && !(value instanceof String)
          && !(value instanceof Boolean) && !(value instanceof List<?>)) {
        String type = value == null ? "null" : value.getClass().toString();
        throw new InvalidArgumentException(String.format(
            "The property name %s should be a basic type: Number, String, Date, Boolean, List<String>.The current type is %s.",
            key, type));
      }
      if (value instanceof List<?>) {
        for (Object element : (List<?>) value) {
          if (!(element instanceof String)) {
            String type = element == null ? "null" : value.getClass().toString();
            throw new InvalidArgumentException(String.format(
                "The property name %s should be a list of String.The current type is %s.", key, type));
          }
        }
      }
      if (value instanceof String) {
        if (String.valueOf(value).length() > 8192) {
          throw new InvalidArgumentException(
              String.format("The property name %s of value is too long.max length is 8192.", key));
        }
      }
      if (value instanceof Date) {
        tempMap.put(key, DateFormatUtils.format(((Date) value).getTime(), "yyyy-MM-dd HH:mm:ss:SSS"));
      }
      tempMap.put(key, value);
    }
    return tempMap;
  }

}
