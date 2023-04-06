package com.sensorsdata.analytics.javasdk.bean.cache;

import lombok.Builder;
import lombok.Data;

import java.io.Serializable;

/**
 * 试验组信息
 *
 * @author yanming@sensorsdata.cn
 * @version 1.0.0
 * @since 2022/11/22 3:18 PM
 */
@Data
@Builder
public class Variable implements Serializable {


  private static final long serialVersionUID = -7528133620595246565L;

  private String name;   //参数名
  private String type;   //参数类型
  private String value;  //参数值


  public Variable(String name, String type, String value) {
    this.name = name;
    this.type = type;
    this.value = value;
  }
}
