package com.sensorsdata.analytics.javasdk;

import static org.junit.Assert.assertEquals;

import com.sensorsdata.analytics.javasdk.exception.HttpStatusException;
import com.sensorsdata.analytics.javasdk.util.HttpUtil;
import com.sensorsdata.analytics.javasdk.util.SensorsAnalyticsUtil;

import com.fasterxml.jackson.databind.JsonNode;
import org.junit.Test;

import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

/**
 * 网络请求工具类测试
 *
 * @author fz <fangzhuo@sensorsdata.cn>
 * @version 1.0.0
 * @since 2021/06/18 09:54
 */
public class HttpUtilTest {

  String url =
      "http://abtesting.saas.debugbox.sensorsdata.cn/api/v2/abtest/online/results?project-key=438B9364C98D54371751BA82F6484A1A03A5155E";

  /***
   * 网络请求带参数测试
   * @throws IOException
   * @throws com.sensorsdata.analytics.javasdk.exception.HttpStatusException
   */
  @Test(timeout = 3000)
  public void postABTestWithParams() throws IOException, HttpStatusException {
    Map<String, Object> params = new HashMap<String, Object>();
    params.put("login_id", "AB123456");
    params.put("platform", SensorsABTestConst.PLATFORM);
    params.put("abtest_lib_version", SensorsABTestConst.VERSION);
    String str =
        HttpUtil.postABTest(url, SensorsAnalyticsUtil.getJsonObjectMapper().writeValueAsString(params), 3000);
    JsonNode jsonNode = SensorsAnalyticsUtil.getJsonObjectMapper().readTree(str);
    assertEquals(SensorsABTestConst.SUCCESS, jsonNode.findValue(SensorsABTestConst.STATUS_KEY).asText());
  }


}
