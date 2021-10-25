package com.sensorsdata.analytics.javasdk.util;

import lombok.extern.slf4j.Slf4j;
import org.apache.http.HeaderElement;
import org.apache.http.HeaderElementIterator;
import org.apache.http.HttpResponse;
import org.apache.http.client.ResponseHandler;
import org.apache.http.client.config.RequestConfig;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.conn.ConnectionKeepAliveStrategy;
import org.apache.http.entity.StringEntity;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.HttpClients;
import org.apache.http.impl.conn.PoolingHttpClientConnectionManager;
import org.apache.http.message.BasicHeaderElementIterator;
import org.apache.http.protocol.HTTP;
import org.apache.http.protocol.HttpContext;
import org.apache.http.util.EntityUtils;

import java.io.Closeable;
import java.io.IOException;
import java.nio.charset.StandardCharsets;

/**
 * 网络请求模块
 *
 * @author fangzhuo
 * @version 1.0.0
 * @since 2021/08/27
 */
@Slf4j
public class HttpConsumer implements Closeable {

  CloseableHttpClient httpClient;

  String serverUrl;

  static String JSON_MIMETYPE = "application/json";

  static String TIME_OUT = "timeout";

  PoolingHttpClientConnectionManager cm;

  SensorsKeepAliveStrategy strategy = new SensorsKeepAliveStrategy();

  SensorsResponseHandler responseHandler = new SensorsResponseHandler();

  public HttpConsumer(String serverUrl, int maxTotal, int maxPerRoute) {
    this.serverUrl = serverUrl;
    cm = new PoolingHttpClientConnectionManager();
    cm.setMaxTotal(maxTotal);
    cm.setDefaultMaxPerRoute(maxPerRoute);
    httpClient = HttpClients.custom().setUserAgent("SensorsAnalytics AB Test SDK").setConnectionManager(cm)
        .setKeepAliveStrategy(strategy).build();
  }

  public String consume(String data, int timeoutMilliseconds) throws IOException {
    if (httpClient == null) {
      httpClient = HttpClients.custom().setUserAgent("SensorsAnalytics AB Test SDK").setConnectionManager(cm)
          .setKeepAliveStrategy(strategy).build();
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
    return httpClient.execute(httpPost, responseHandler);
  }

  @Override
  public void close() throws IOException {
    if (httpClient != null) {
      httpClient.close();
    }
  }

  static class SensorsKeepAliveStrategy implements ConnectionKeepAliveStrategy {

    @Override
    public long getKeepAliveDuration(HttpResponse response, HttpContext context) {
      HeaderElementIterator it = new BasicHeaderElementIterator(response.headerIterator(HTTP.CONN_KEEP_ALIVE));
      while (it.hasNext()) {
        HeaderElement he = it.nextElement();
        String param = he.getName();
        String value = he.getValue();
        if (value != null && param.equalsIgnoreCase(TIME_OUT)) {
          return Long.parseLong(value) * 1000;
        }
      }
      return 60 * 1000;
    }
  }


  static class SensorsResponseHandler implements ResponseHandler<String> {

    @Override
    public String handleResponse(HttpResponse response) throws IOException {
      return EntityUtils.toString(response.getEntity(), StandardCharsets.UTF_8);
    }
  }
}



