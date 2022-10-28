package com.sensorsdata.analytics.javasdk.util;

import com.sensorsdata.analytics.javasdk.SensorsABParams;
import com.sensorsdata.analytics.javasdk.common.Pair;
import com.sensorsdata.analytics.javasdk.exceptions.InvalidArgumentException;

import org.apache.commons.lang3.ArrayUtils;
import org.apache.commons.lang3.StringUtils;

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
  private static final Pattern pattern = Pattern.compile(
      "^((?!^distinct_id$|^original_id$|^time$|^properties$|^id$|^first_id$|^second_id$|^users$|^events$|^event$|^user_id$|^date$|^datetime$|^device_id$|^user_group|^user_tag|^[0-9])[a-zA-Z0-9_]{0,99})$",
      Pattern.CASE_INSENSITIVE);

  /**
   * 要求 property 里面的 value 长度不得超过 1024
   */
  private static final int MAX_PROPERTY_LENGTH = 1024;

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
    Map<String, Object> newProperties = new HashMap<>(customProperties);
    for (Map.Entry<String, Object> entry : newProperties.entrySet()) {
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
        newProperties.put(entry.getKey(), ArrayUtils.toString(value));
      }
      if (value instanceof String) {
        if (String.valueOf(value).length() > 8192) {
          throw new InvalidArgumentException(
              String.format("The property name %s of value is too long.max length is 8192.", entry.getKey()));
        }
      }
    }
    return newProperties;
  }

  public static <T> Pair<Boolean, String> assertCustomIds(SensorsABParams<T> sensorsParams) {
    Map<String, String> customIds = sensorsParams.getCustomIds();
    if (customIds == null || customIds.isEmpty()) {
      String message = String.format("fetchABTest request without customIds.[distinctId:%s,isLoginId:%s,experiment:%s]",
          sensorsParams.getDistinctId(), sensorsParams.getIsLoginId(), sensorsParams.getExperimentVariableName());
      return Pair.of(false, message);
    }
    for (Map.Entry<String, String> entry : customIds.entrySet()) {
      if (StringUtils.isBlank(entry.getKey())) {
        String message = String.format(
            "fetchABTest request with invalid customIds,the keys of customIds has null or empty.[distinctId:%s,isLoginId:%s,experiment:%s,customIds:%s]",
            sensorsParams.getDistinctId(),
            sensorsParams.getIsLoginId(),
            sensorsParams.getExperimentVariableName(),
            map2Str(customIds));
        return Pair.of(true, message);
      }
      if (!pattern.matcher(entry.getKey()).matches()) {
        String message = String.format(
            "fetchABTest request with invalid customIds,the key mismatch.[distinctId:%s,isLoginId:%s,experiment:%s,customIds:%s]",
            sensorsParams.getDistinctId(),
            sensorsParams.getIsLoginId(),
            sensorsParams.getExperimentVariableName(),
            map2Str(customIds));
        return Pair.of(true, message);
      }
      if (StringUtils.isBlank(entry.getValue())) {
        String message = String.format(
            "fetchABTest request with invalid customIds,the value of customIds has null or empty.[distinctId:%s,isLoginId:%s,experiment:%s,customIds:%s]",
            sensorsParams.getDistinctId(),
            sensorsParams.getIsLoginId(),
            sensorsParams.getExperimentVariableName(),
            map2Str(customIds));
        return Pair.of(true, message);
      }
      if (entry.getValue().length() > MAX_PROPERTY_LENGTH) {
        String message = String.format(
            "fetchABTest request with invalid customIds,the value length is too long.[distinctId:%s,isLoginId:%s,experiment:%s,customIds:%s]",
            sensorsParams.getDistinctId(),
            sensorsParams.getIsLoginId(),
            sensorsParams.getExperimentVariableName(),
            map2Str(customIds));
        return Pair.of(true, message);
      }
    }
    return Pair.of(false, "");
  }

  public static String map2Str(Map<String, String> customIds) {
    StringBuilder res = new StringBuilder();
    if (customIds == null || customIds.isEmpty()) {
      return res.toString();
    }
    for (Map.Entry<String, String> entry : customIds.entrySet()) {
      res.append(String.format("{%s_%s}", entry.getKey(), entry.getValue()));
    }
    return res.toString();
  }
}
