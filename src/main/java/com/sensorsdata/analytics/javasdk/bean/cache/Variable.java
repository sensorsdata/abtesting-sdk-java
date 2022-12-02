package com.sensorsdata.analytics.javasdk.bean.cache;

import lombok.Getter;
import lombok.Setter;

import java.io.Serializable;

/**
 * 试验组信息
 *
 * @author yanming@sensorsdata.cn
 * @version 1.0.0
 * @since 2022/11/22 3:18 PM
 */
@Getter
@Setter
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

  @Override
  public boolean equals(Object o) {
    if (this == o) return true;
    if (!(o instanceof Variable)) return false;

    Variable variable = (Variable) o;

    if (!name.equals(variable.name)) return false;
    if (!type.equals(variable.type)) return false;
    return value.equals(variable.value);
  }

  @Override
  public int hashCode() {
    int result = name.hashCode();
    result = 31 * result + type.hashCode();
    result = 31 * result + value.hashCode();
    return result;
  }
}
