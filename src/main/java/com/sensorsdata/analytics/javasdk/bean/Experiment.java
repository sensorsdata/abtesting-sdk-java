package com.sensorsdata.analytics.javasdk.bean;

import java.io.Serializable;

/**
 * @author fz <fangzhuo@sensorsdata.cn>
 * @version 1.0.0
 * @since 2021/06/09 21:47
 */
public class Experiment<T> implements Serializable {
  private static final long serialVersionUID = -236604631513293247L;
  /**
   * 匿名ID或登录ID
   */
  private String distinctId;
  /**
   * 是否为登录ID
   */
  private Boolean isLoginId;
  /**
   * 试验ID
   */
  private String abTestExperimentId;
  /**
   * 试验分组ID
   */
  private String abTestExperimentGroupId;
  /**
   * 是否对照组
   */
  private Boolean isControlGroup;
  /**
   * 是否在白名单
   */
  private Boolean isWhiteList;
  /**
   * 命中值
   */
  private T result;

  public Experiment(String distinctId, Boolean isLoginId, String abTestExperimentId,
      String abTestExperimentGroupId, Boolean isControlGroup, Boolean isWhiteList, T value) {
    this.distinctId = distinctId;
    this.isLoginId = isLoginId;
    this.abTestExperimentId = abTestExperimentId;
    this.abTestExperimentGroupId = abTestExperimentGroupId;
    this.isControlGroup = isControlGroup;
    this.isWhiteList = isWhiteList;
    this.result = value;
  }

  public Experiment(String distinctId, Boolean isLoginId, T value) {
    this.distinctId = distinctId;
    this.isLoginId = isLoginId;
    this.result = value;
  }

  public String getDistinctId() {
    return distinctId;
  }

  public void setDistinctId(String distinctId) {
    this.distinctId = distinctId;
  }

  public Boolean getIsLoginId() {
    return isLoginId;
  }

  public void isLoginId(Boolean loginId) {
    isLoginId = loginId;
  }

  public String getAbTestExperimentId() {
    return abTestExperimentId;
  }

  public void setAbTestExperimentId(String abTestExperimentId) {
    this.abTestExperimentId = abTestExperimentId;
  }

  public String getAbTestExperimentGroupId() {
    return abTestExperimentGroupId;
  }

  public void setAbTestExperimentGroupId(String abTestExperimentGroupId) {
    this.abTestExperimentGroupId = abTestExperimentGroupId;
  }

  public Boolean getControlGroup() {
    return isControlGroup;
  }

  public void setControlGroup(Boolean controlGroup) {
    isControlGroup = controlGroup;
  }

  public Boolean getWhiteList() {
    return isWhiteList;
  }

  public void setWhiteList(Boolean whiteList) {
    isWhiteList = whiteList;
  }

  public T getResult() {
    return result;
  }

  public void setResult(T result) {
    this.result = result;
  }
}
