package com.sensorsdata.analytics.javasdk.bean.cache;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@NoArgsConstructor
@AllArgsConstructor
@Data
@Builder
public class UserHitResult {

  private UserHitExperimentGroup userHitExperimentGroup;

  private List<UserOutExperimentGroup> userOutExperimentGroups;


}
