package com.sensorsdata.analytics.javasdk.bean.cache;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;

import java.io.Serializable;

/**
 * 用户命中详情，比试验组配置多了是否以白名单命中的属性项
 *
 * @author yanming@sensorsdata.cn
 * @version 1.0.0
 * @since 2022/11/22 4:47 PM
 */
@AllArgsConstructor
@Builder
@Data
public class UserHitExperimentGroup implements Serializable {

  private static final long serialVersionUID = -2624648063644487605L;

  /**
   * 本次命中的主体ID
   */
  private String subjectId;
  private boolean isWhiteList;

  private boolean cacheable;
  private ExperimentGroupConfig experimentGroupConfig;

}
