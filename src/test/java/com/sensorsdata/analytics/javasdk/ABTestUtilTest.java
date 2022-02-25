package com.sensorsdata.analytics.javasdk;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;
import static org.junit.Assert.fail;

import com.sensorsdata.analytics.javasdk.exceptions.InvalidArgumentException;
import com.sensorsdata.analytics.javasdk.util.ABTestUtil;

import com.google.common.collect.Maps;
import org.junit.Test;

import java.util.Map;

/**
 * 工具类方法单元测试
 *
 * @author fangzhuo
 * @version 1.0.0
 * @since 2021/10/25 14:55
 */
public class ABTestUtilTest {

  private final Map<String, Object> customProperties = Maps.newHashMap();

  @Test
  public void testKey() {
    customProperties.put("distinct_id", "eeee");
    try {
      ABTestUtil.customPropertiesHandler(customProperties);
      fail();
    } catch (InvalidArgumentException e) {
      assertTrue(true);
    }
  }

  @Test
  public void testDeviceIdKey() {
    customProperties.put("device_id", "eeee");
    try {
      ABTestUtil.customPropertiesHandler(customProperties);
      fail();
    } catch (InvalidArgumentException e) {
      assertTrue(true);
    }
  }

  @Test
  public void testNullKey() {
    customProperties.put(null, "111");
    try {
      ABTestUtil.customPropertiesHandler(customProperties);
      fail();
    } catch (InvalidArgumentException e) {
      assertTrue(true);
    }
  }

  @Test
  public void testKeyLength() {
    String key =
        "123456789012345678901234567890123456789012345678901234567890123456789012345678901234567890123456789012345678901234567890";
    customProperties.put(key, "eeee");
    try {
      ABTestUtil.customPropertiesHandler(customProperties);
      fail();
    } catch (InvalidArgumentException e) {
      assertTrue(true);
    }
  }

  @Test
  public void testValidKey() {
    customProperties.put("_qwer123", "eee");
    try {
      final Map<String, Object> stringObjectMap = ABTestUtil.customPropertiesHandler(customProperties);
      assertNotNull(stringObjectMap);
      assertEquals(1, stringObjectMap.size());
    } catch (InvalidArgumentException e) {
      fail();
    }
  }
}
