package com.sensorsdata.analytics.javasdk.exception;

/**
 * 网络请求异常
 *
 * @author fz <fangzhuo@sensorsdata.cn>
 * @version 1.0.0
 * @since 2021/06/15 14:04
 */
public class HttpStatusException extends Exception {
  private static final long serialVersionUID = -167873860788060801L;

  private final int httpStatusCode;

  private final String httpContent;

  /**
   * Constructs a new exception with {@code null} as its detail message.
   * The cause is not initialized, and may subsequently be initialized by a
   * call to {@link #initCause}.
   */
  public HttpStatusException(String error, int httpStatusCode, String httpContent) {
    super(error);
    this.httpStatusCode = httpStatusCode;
    this.httpContent = httpContent;
  }
}
