package com.sensorsdata.analytics.javasdk.bean.cache;


import com.fasterxml.jackson.databind.JsonNode;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.io.Serializable;
import java.util.Map;

@NoArgsConstructor
@AllArgsConstructor
@Data
@Builder
public class ExperimentGroupConfig implements Serializable {

  private static final long serialVersionUID = -1848565541802260006L;

  private String abtestExperimentId;
  private String abtestExperimentGroupId;
  private boolean isControlGroup;

  private String abtestExperimentResultId;
  //key -> paramName, value -> Variable
  private Map<String, Variable> variableMap;

  private String abtestExperimentVersion;

  private transient JsonNode src;

  private String subjectName;


}
