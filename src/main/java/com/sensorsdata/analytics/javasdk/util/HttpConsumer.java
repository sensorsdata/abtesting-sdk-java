package com.sensorsdata.analytics.javasdk.util;

import com.sensorsdata.analytics.javasdk.exception.HttpStatusException;

import org.apache.http.client.config.RequestConfig;
import org.apache.http.client.methods.CloseableHttpResponse;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.entity.StringEntity;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.HttpClients;
import org.apache.http.impl.conn.PoolingHttpClientConnectionManager;
import org.apache.http.util.EntityUtils;

import java.io.Closeable;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;

/**
 * 网络请求模块
 *
 * @author fangzhuo
 * @version 1.0.0
 * @since 2021/08/27
 */
public class HttpConsumer implements Closeable {

  CloseableHttpClient httpClient;

  final String serverUrl;

  final String JSON_MIMETYPE = "application/json";

  PoolingHttpClientConnectionManager cm;

  private Lock lock = new ReentrantLock();

  public HttpConsumer(String serverUrl, int maxTotal, int maxPerRoute) {
    this.serverUrl = serverUrl;
    cm = new PoolingHttpClientConnectionManager();
    cm.setMaxTotal(maxTotal);
    cm.setDefaultMaxPerRoute(maxPerRoute);
    httpClient = HttpClients.custom().setUserAgent("SensorsAnalytics AB Test SDK").setConnectionManager(cm).build();
  }

  public String consume(String data, int timeoutMilliseconds) throws HttpStatusException, IOException {
    try {
      lock.lock();
      if (httpClient == null) {
        httpClient = HttpClients.custom().setUserAgent("SensorsAnalytics AB Test SDK").setConnectionManager(cm).build();
      }
      HttpPost httpPost = new HttpPost(serverUrl);
      RequestConfig requestConfig = RequestConfig.custom().setConnectTimeout(timeoutMilliseconds)
          .setConnectionRequestTimeout(timeoutMilliseconds)
          .setSocketTimeout(timeoutMilliseconds)
          .setStaleConnectionCheckEnabled(true)
          .build();
      httpPost.setConfig(requestConfig);
      //设置header
      httpPost.setHeader("Content-type", JSON_MIMETYPE);
      if (data != null && data.length() != 0) {
        StringEntity param = new StringEntity(data, StandardCharsets.UTF_8);
        httpPost.setEntity(param);
      }
      CloseableHttpResponse response = httpClient.execute(httpPost);
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
      lock.unlock();
    }
  }


  @Override
  public void close() throws IOException {
    if (httpClient != null) {
      httpClient.close();
    }
  }
}
