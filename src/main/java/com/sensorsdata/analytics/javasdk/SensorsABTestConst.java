package com.sensorsdata.analytics.javasdk;

/**
 * 常量类
 *
 * @author fangzhuo@sensorsdata.cn
 * @version 1.0.0
 * @since 2021/06/15 14:39
 */
public class SensorsABTestConst {

  private SensorsABTestConst() {
  }

  static final String PLATFORM = "platform";

  static final String JAVA = "Java";

  public static final String VERSION = "1.0.1";

  static final String VERSION_KEY = "abtest_lib_version";

  /**
   * 上报事件使用的key
   */
  public static final String EXPERIMENT_ID = "$abtest_experiment_id";
  public static final String EXPERIMENT_GROUP_ID = "$abtest_experiment_group_id";
  public static final String EVENT_TYPE = "$ABTestTrigger";
  public static final String LIB_PLUGIN_VERSION = "$lib_plugin_version";
  public static final String AB_TEST_EVENT_LIB_VERSION = "java_abtesting";

  public static final String ANONYMOUS_ID = "anonymous_id";

  public static final String DEVICE_SUBJECT_NAME = "DEVICE";
  /**
   * 返回json里面的key
   */
  public static final String EXPERIMENT_ID_KEY = "abtest_experiment_id";
  public static final String EXPERIMENT_GROUP_ID_KEY = "abtest_experiment_group_id";

  public static final String ABTEST_EXPERIMENT_RESULT_ID_KEY = "abtest_experiment_result_id";
  public static final String ABTEST_EXPERIMENT_VERSION_KEY = "abtest_experiment_version";

  public static final String ABTEST_TRACK_CONFIG_KEY = "track_config";
  public static final String ABTEST_SUBJECT_NAME_KEY = "subject_name";
  public static final String ABTEST_SUBJECT_ID_KEY = "subject_id";
  public static final String ABTEST_STICKINESS_KEY = "stickiness";
  public static final String ABTEST_CACHEABLE_KEY = "cacheable";
  public static final String IS_CONTROL_GROUP_KEY = "is_control_group";
  public static final String IS_WHITE_LIST_KEY = "is_white_list";
  public static final String VARIABLES_KEY = "variables";
  public static final String RESULTS_KEY = "results";
  public static final String OUT_LIST_KEY = "out_list";
  public static final String STATUS_KEY = "status";

  public static final String NAME_KEY = "name";

  public static final String TYPE_KEY = "type";

  public static final String VALUE_KEY = "value";
  public static final String SUCCESS = "SUCCESS";

  public static final String INVALID_ABTEST_UNIQUE_ID = "-1";

  // 自定义主体 id
  public static final String CUSTOM_ID = "custom_id";


}
