package com.sensorsdata.analytics.javasdk.bean;

/*
  埋点配置

  @author yanming@sensorsdata.cn
 * @version 1.0.0
 * @since 2022/12/28 3:09 PM
 */
import com.fasterxml.jackson.annotation.JsonProperty;
import com.google.common.collect.ImmutableList;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;

import java.util.Collections;
import java.util.List;

@Builder
@Data
@AllArgsConstructor

public class TrackConfig {
  /**
   * 上报item表开关, 默认关闭
   */
  @JsonProperty(defaultValue = "false")
  private boolean itemSwitch;
  /**
   * 上报trigger事件开关, 默认开启
   */
  @JsonProperty(defaultValue = "true")
  private boolean triggerSwitch;

  /**
   * 是否将分流、命中记录添加到事件属性，默认关闭
   */
  @JsonProperty(defaultValue = "false")
  private boolean propertySetSwitch;

  /**
   * 上报扩展内容，SDK 上报 $ABTestTrigger 事件需要遍历集合中字段追加对应的属性，比如默认情况下，需要上报 abtest_experiment_version、abtest_experiment_result_id 内容
   */
  private List<String> triggerContentExt;


  /**
   * 初始化给默认参数,只返回改动的配置，因此需要都给一个默认配置
   */
  public TrackConfig() {
    this.itemSwitch = false;
    this.triggerSwitch = true;
    this.propertySetSwitch = false;
    this.triggerContentExt = Collections.emptyList();
  }

  public static TrackConfig getDefaultTrackConfig() {
    TrackConfig trackConfig = new TrackConfig();

    trackConfig.setTriggerContentExt(ImmutableList.of("abtest_experiment_result_id", "abtest_experiment_version"));
    return trackConfig;
  }
}
