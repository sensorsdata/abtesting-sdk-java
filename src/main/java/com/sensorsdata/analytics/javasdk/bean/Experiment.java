package com.sensorsdata.analytics.javasdk.bean;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.Setter;

import java.io.Serializable;

/**
 * @author fangzhuo@sensorsdata.cn
 * @version 1.0.0
 * @since 2021/06/09 21:47
 */

@Getter
@Setter
@AllArgsConstructor
@Builder
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
   * 试验组唯一ID
   */
  private String abtestExperimentResultId;


  /**
   * 试验组版本
   */
  private String abtestExperimentVersion;


  /**
   * 命中结果
   */
  private T result;

  public Experiment(String distinctId, Boolean isLoginId, T value) {
    this.distinctId = distinctId;
    this.isLoginId = isLoginId;
    this.result = value;
  }



}
