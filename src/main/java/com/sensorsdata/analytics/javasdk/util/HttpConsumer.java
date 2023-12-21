package com.sensorsdata.analytics.javasdk.util;

import org.apache.commons.lang3.StringUtils;
import org.apache.http.Header;
import org.apache.http.HeaderElement;
import org.apache.http.HeaderElementIterator;
import org.apache.http.HttpResponse;
import org.apache.http.client.config.RequestConfig;
import org.apache.http.client.methods.CloseableHttpResponse;
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
public class HttpConsumer implements Closeable {

  private CloseableHttpClient httpClient;

  private LogUtil log;

  private String serverUrl;

  private static final String CONTENT_TYPE = "Content-type";

  private static final String JSON_MIMETYPE = "application/json";

  private static final String TIME_OUT = "timeout";

  private static final String REQUEST_ID_HEADER = "X-Request-Id";

  private static final String AB_REQUEST_ID_HEADER = "X-AB-Request-Id";

  private static final String AB_REQUEST_START_TIME_HEADER = "X-AB-Request-Start-Time";

  private static final String AB_REQUEST_PROCESSING_TIME_HEADER = "X-AB-Request-Process-Time";

  private PoolingHttpClientConnectionManager cm;

  private SensorsKeepAliveStrategy strategy = new SensorsKeepAliveStrategy();

  private boolean enableRecordRequestCostTime;


  public HttpConsumer(LogUtil log, boolean enableRecordRequestCostTime, String serverUrl, int maxTotal, int maxPerRoute) {
    this.log = log;
    this.enableRecordRequestCostTime = enableRecordRequestCostTime;
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
    // 设置header
    long requestStartTime = System.currentTimeMillis();
    httpPost.setHeader(AB_REQUEST_START_TIME_HEADER, String.valueOf(requestStartTime));
    httpPost.setHeader(CONTENT_TYPE, JSON_MIMETYPE);
    if (data != null && data.length() != 0) {
      StringEntity param = new StringEntity(data, StandardCharsets.UTF_8);
      httpPost.setEntity(param);
    }
    // 处理请求结果
    try (CloseableHttpResponse response = httpClient.execute(httpPost)) {
      if (enableRecordRequestCostTime) {
        return handleResponseAndRecordRequestTimeCost(response, requestStartTime, System.currentTimeMillis());
      }
      return handleResponse(response);
    } catch (Exception e) {
      // 遇到接错误或其他网络错误，则没有 response ，需自补充耗时记录
      if (enableRecordRequestCostTime) {
        recordABRequestCostTimeFromHeader(null, requestStartTime, System.currentTimeMillis());
      }
      throw e;
    }
  }

  @Override
  public void close() throws IOException {
    if (httpClient != null) {
      httpClient.close();
    }
  }

  private String handleResponse(HttpResponse response) throws IOException {
    return EntityUtils.toString(response.getEntity(), StandardCharsets.UTF_8);
  }

  private String handleResponseAndRecordRequestTimeCost(HttpResponse response, long requestStartTime, long requestEndTime) throws IOException {
    recordABRequestCostTimeFromHeader(response, requestStartTime, requestEndTime);
    return handleResponse(response);
  }

  private void recordABRequestCostTimeFromHeader(HttpResponse response, long requestStartTime, long requestEndTime) {
    try {
      String requestId = getABRequestIdFromResponse(response);
      String requestTotalTime = String.valueOf(requestEndTime - requestStartTime);
      String requestProcessTime = getAbRequestProcessTimeFromResponse(response);

      StringBuilder message = new StringBuilder();
      message.append("record ab request time consumption. [requestId: ").append(requestId)
          .append(", requestTotalTime: ").append(requestTotalTime).append(" ms")
          .append(", requestProcessTime: ").append(requestProcessTime).append(" ms]");
      log.info(message.toString());
    } catch (Exception e) {
      log.warn("failed to record ab request time consumption", e);
    }
  }

  private String getABRequestIdFromResponse(HttpResponse response) {
    if (response != null) {
      Header requestIdHeader = response.getFirstHeader(AB_REQUEST_ID_HEADER);
      if (requestIdHeader == null) {
        requestIdHeader = response.getFirstHeader(REQUEST_ID_HEADER);
      }
      if (requestIdHeader != null && StringUtils.isNotBlank(requestIdHeader.getValue())) {
        return requestIdHeader.getValue();
      }
    }
    return "unknown (not found)";
  }

  private String getAbRequestProcessTimeFromResponse(HttpResponse response) {
    if (response != null) {
      Header requestProcessTimeHeader = response.getFirstHeader(AB_REQUEST_PROCESSING_TIME_HEADER);
      if (requestProcessTimeHeader != null && StringUtils.isNotBlank(requestProcessTimeHeader.getValue())) {
        return requestProcessTimeHeader.getValue();
      }
    }
    return "unknown (not found)";
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

}



