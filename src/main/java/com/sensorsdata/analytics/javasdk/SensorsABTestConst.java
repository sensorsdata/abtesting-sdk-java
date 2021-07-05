package com.sensorsdata.analytics.javasdk;

/**
 * 常量类
 *
 * @author fz <fangzhuo@sensorsdata.cn>
 * @version 1.0.0
 * @since 2021/06/15 14:39
 */
public class SensorsABTestConst {

  private SensorsABTestConst() {
  }

  static final String PLATFORM = "platform";

  static final String JAVA = "Java";

  static final String VERSION = "0.0.1";

  static final String VERSION_KEY = "abtest_lib_version";

  /**
   * 上报事件使用的key
   */
  static final String EXPERIMENT_ID = "$abtest_experiment_id";
  static final String EXPERIMENT_GROUP_ID = "$abtest_experiment_group_id";
  static final String EVENT_TYPE = "$ABTestTrigger";
  static final String LIB_PLUGIN_VERSION = "$lib_plugin_version";
  static final String AB_TEST_EVENT_LIB_VERSION = "java_abtesting";
  /**
   * 返回json里面的key
   */
  static final String EXPERIMENT_ID_KEY = "abtest_experiment_id";
  static final String EXPERIMENT_GROUP_ID_KEY = "abtest_experiment_group_id";
  static final String IS_CONTROL_GROUP_KEY = "is_control_group";
  static final String IS_WHITE_LIST_KEY = "is_white_list";
  public static final String VARIABLES_KEY = "variables";
  public static final String RESULTS_KEY = "results";
  public static final String STATUS_KEY = "status";
  public static final String SUCCESS = "SUCCESS";


}
