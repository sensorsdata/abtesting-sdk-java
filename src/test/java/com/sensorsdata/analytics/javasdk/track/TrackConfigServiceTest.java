package com.sensorsdata.analytics.javasdk.track;

import static org.junit.Assert.assertSame;

import com.sensorsdata.analytics.javasdk.bean.TrackConfig;
import com.sensorsdata.analytics.javasdk.service.ITrackConfigService;
import com.sensorsdata.analytics.javasdk.service.impl.TrackConfigService;
import com.sensorsdata.analytics.javasdk.util.LogUtil;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.runners.MockitoJUnitRunner;

import java.util.ArrayList;

/**
 * @author yanming@sensorsdata.cn
 * @version 1.0.0
 * @since 2023/1/20 11:45 AM
 */
@RunWith(MockitoJUnitRunner.class)
public class TrackConfigServiceTest {

  private TrackConfig trackConfig;
  private ITrackConfigService trackConfigService;
  @Mock
  private LogUtil log;

  @Before
  public void init() {
    trackConfig = TrackConfig.getDefaultTrackConfig();
    trackConfigService = new TrackConfigService(log, trackConfig);
  }

  @Test
  public void whenInputTrackConfigIsNullThenTrackConfigNotUpdate() {

    trackConfigService.updateTrackConfig(null);

    assertSame(trackConfig, trackConfigService.getTrackConfig());
  }

  @Test
  public void whenInputTrackConfigNotChangedThenTrackConfigNotUpdate() {
    TrackConfig newTrackConfig = TrackConfig.getDefaultTrackConfig();
    trackConfigService.updateTrackConfig(newTrackConfig);

    assertSame(trackConfig, trackConfigService.getTrackConfig());

  }

  @Test
  public void whenInputTrackConfigChangedThenUpdateTrackConfig() {
    TrackConfig newTrackConfig = TrackConfig.getDefaultTrackConfig();
    newTrackConfig.setTriggerContentExt(new ArrayList<String>());

    trackConfigService.updateTrackConfig(newTrackConfig);
    assertSame(newTrackConfig, trackConfigService.getTrackConfig());


  }

}