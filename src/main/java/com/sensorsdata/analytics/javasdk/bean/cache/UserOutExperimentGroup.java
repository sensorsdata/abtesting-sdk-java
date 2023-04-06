package com.sensorsdata.analytics.javasdk.bean.cache;

/**
 * @author yanming@sensorsdata.cn
 * @version 1.0.0
 * @since 2023/1/20 3:31 PM
 */
import com.fasterxml.jackson.databind.JsonNode;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;

import java.util.Map;

@Data
@Builder
@AllArgsConstructor
public class UserOutExperimentGroup {
  private String subjectName;
  private String subjectId;
  private String param;
  private String abtestExperimentResultId;

  private String abtestExperimentVersion;
  private String abtestExperimentId;

  private String abtestExperimentGroupId;


  private boolean whiteList;


  private Map<String, Variable> variableMap;

  private JsonNode src;
}
