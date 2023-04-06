package com.sensorsdata.analytics.javasdk.bean;

/**
 * 用户标识
 *
 * @author yanming@sensorsdata.cn
 * @version 1.0.0
 * @since 2023/1/17 3:19 PM
 */
import lombok.Data;

import java.util.HashMap;
import java.util.Map;

@Data
public class UserInfo {
  private String distinctId;
  private boolean isLoginId;
  private Map<String, String> customIds;

  private Map<String, Object> customProperties;

  private UserInfo(Builder builder) {
    setDistinctId(builder.distinctId);
    setLoginId(builder.isLoginId);
    setCustomIds(builder.customIds);
    setCustomProperties(builder.customProperties);
  }

  public static Builder builder() {
    return new Builder();
  }


  public static final class Builder {
    private String distinctId;
    private boolean isLoginId;
    private Map<String, String> customIds;
    private Map<String, Object> customProperties;

    private Builder() {
      customIds = new HashMap<>();
      customProperties = new HashMap<>();
    }

    public Builder distinctId(String val) {
      distinctId = val;
      return this;
    }

    public Builder isLoginId(boolean val) {
      isLoginId = val;
      return this;
    }

    public Builder customIds(Map<String, String> val) {
      customIds = val;
      return this;
    }

    public Builder customProperties(Map<String, Object> val) {
      customProperties = val;
      return this;
    }

    public UserInfo build() {
      return new UserInfo(this);
    }
  }
}
