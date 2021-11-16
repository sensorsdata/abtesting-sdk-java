package com.sensorsdata.analytics.javasdk.util;

import com.sensorsdata.analytics.javasdk.exceptions.InvalidArgumentException;

import org.apache.commons.lang3.ArrayUtils;

import java.util.Collections;
import java.util.Date;
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
  private static final Pattern pattern = Pattern.compile(
      "^((?!^distinct_id$|^original_id$|^time$|^properties$|^id$|^first_id$|^second_id$|^users$|^events$|^event$|^user_id$|^date$|^datetime$|^device_id$|^user_group|^user_tag|^[0-9])[a-zA-Z0-9_]{0,99})$",
      Pattern.CASE_INSENSITIVE);

  private ABTestUtil() {
  }

  public static <T> boolean assertDefaultValueType(T value) {
    return value instanceof Integer || value instanceof String || value instanceof Boolean;
  }

  public static Map<String, Object> customPropertiesHandler(Map<String, Object> customProperties)
      throws InvalidArgumentException {
    if (customProperties == null || customProperties.isEmpty()) {
      return Collections.emptyMap();
    }
    for (Map.Entry<String, Object> entry : customProperties.entrySet()) {
      if (entry.getKey() == null) {
        throw new InvalidArgumentException("The property name is null.");
      }
      if (entry.getKey().length() > 100) {
        throw new InvalidArgumentException(
            String.format("The property name %s is too long, max length is 100.", entry.getKey()));
      }
      if (!pattern.matcher(entry.getKey()).matches()) {
        throw new InvalidArgumentException(String.format("The property name %s is invalid format.", entry.getKey()));
      }
      Object value = entry.getValue();
      if (!(value instanceof Number) && !(value instanceof Date) && !(value instanceof String)
          && !(value instanceof Boolean) && !(value instanceof List<?>)) {
        String type = value == null ? "null" : value.getClass().toString();
        throw new InvalidArgumentException(String.format(
            "The property name %s should be a basic type: Number, String, Date, Boolean, List<String>.The current type is %s.",
            entry.getKey(), type));
      }
      if (value instanceof List<?>) {
        for (Object element : (List<?>) value) {
          if (!(element instanceof String)) {
            String type = element == null ? "null" : value.getClass().toString();
            throw new InvalidArgumentException(String.format(
                "The property name %s should be a list of String.The current type is %s.", entry.getKey(), type));
          }
        }
        customProperties.put(entry.getKey(), ArrayUtils.toString(value));
      }
      if (value instanceof String) {
        if (String.valueOf(value).length() > 8192) {
          throw new InvalidArgumentException(
              String.format("The property name %s of value is too long.max length is 8192.", entry.getKey()));
        }
      }
    }
    return customProperties;
  }

}
