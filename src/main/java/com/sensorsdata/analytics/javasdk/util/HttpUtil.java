package com.sensorsdata.analytics.javasdk.util;

import com.sensorsdata.analytics.javasdk.exception.HttpStatusException;

import org.apache.http.client.config.RequestConfig;
import org.apache.http.client.methods.CloseableHttpResponse;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.entity.StringEntity;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.HttpClients;
import org.apache.http.util.EntityUtils;

import java.io.IOException;
import java.nio.charset.StandardCharsets;

/**
 * 网络请求工具类
 *
 * @author fz <fangzhuo@sensorsdata.cn>
 * @version 1.0.0
 * @since 2021/06/15 18:28
 */
public class HttpUtil {

  private HttpUtil() {
  }

  public static String postABTest(String url, String strJson, int timeoutMilliseconds)
      throws IOException, HttpStatusException {
    if (timeoutMilliseconds < 0) {
      timeoutMilliseconds = 3000;
    }
    RequestConfig requestConfig = RequestConfig.custom().setConnectTimeout(timeoutMilliseconds)
        .setConnectionRequestTimeout(timeoutMilliseconds)
        .setSocketTimeout(timeoutMilliseconds)
        .setStaleConnectionCheckEnabled(true)
        .build();
    CloseableHttpClient httpClient = HttpClients.custom().setDefaultRequestConfig(requestConfig).build();
    HttpPost httpPost = new HttpPost(url);
    //设置header
    httpPost.setHeader("Content-type", "application/json");
    if (strJson != null && strJson.length() != 0) {
      StringEntity param = new StringEntity(strJson, StandardCharsets.UTF_8);
      httpPost.setEntity(param);
    }
    CloseableHttpResponse response = null;
    try {
      response = httpClient.execute(httpPost);
      int httpStatusCode = response.getStatusLine().getStatusCode();
      String httpContent = new String(EntityUtils.toByteArray(response.getEntity()), StandardCharsets.UTF_8);
      if (httpStatusCode == 200) {
        return httpContent;
      } else {
        throw new HttpStatusException(
            String.format("Unexpected response %d from Sensors AB Test: %s", httpStatusCode, httpContent),
            httpStatusCode, httpContent);
      }
    } finally {
      if (response != null) {
        response.close();
      }
      httpClient.close();
    }
  }
}
