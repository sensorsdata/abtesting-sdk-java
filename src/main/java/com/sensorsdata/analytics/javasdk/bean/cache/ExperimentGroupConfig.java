package com.sensorsdata.analytics.javasdk.bean.cache;

import lombok.Getter;
import lombok.Setter;

import java.io.Serializable;
import java.util.Map;

/**
 * 试验组配置元数据
 *
 * @author yanming@sensorsdata.cn
 * @version 1.0.0
 * @since 2022/11/22 11:26 AM
 */
@Getter
@Setter
public class ExperimentGroupConfig implements Serializable {

  private static final long serialVersionUID = -1848565541802260006L;

  private String experimentId;
  private String experimentGroupId;
  private boolean isControlGroup;
  //key -> paramName, value -> Variable
  private Map<String, Variable> variableMap;

  public ExperimentGroupConfig(String experimentId, String experimentGroupId, boolean isControlGroup,
      Map<String, Variable> variableMap) {
    this.experimentId = experimentId;
    this.experimentGroupId = experimentGroupId;
    this.isControlGroup = isControlGroup;
    this.variableMap = variableMap;
  }


  @Override
  public boolean equals(Object o) {
    if (this == o) return true;
    if (!(o instanceof ExperimentGroupConfig)) return false;

    ExperimentGroupConfig that = (ExperimentGroupConfig) o;

    if (isControlGroup != that.isControlGroup) return false;
    if (!experimentId.equals(that.experimentId)) return false;
    if (!experimentGroupId.equals(that.experimentGroupId)) return false;
    return variableMap.equals(that.variableMap);
  }

  @Override
  public int hashCode() {
    int result = experimentId.hashCode();
    result = 31 * result + experimentGroupId.hashCode();
    result = 31 * result + (isControlGroup ? 1 : 0);
    result = 31 * result + variableMap.hashCode();
    return result;
  }
}
