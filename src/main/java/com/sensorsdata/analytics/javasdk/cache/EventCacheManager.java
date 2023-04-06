package com.sensorsdata.analytics.javasdk.cache;

import static com.sensorsdata.analytics.javasdk.util.ABTestUtil.map2Str;

import com.sensorsdata.analytics.javasdk.bean.UserInfo;
import com.sensorsdata.analytics.javasdk.util.LogUtil;

import com.google.common.cache.CacheBuilder;
import com.google.common.cache.CacheLoader;
import com.google.common.cache.LoadingCache;
import org.apache.commons.lang3.StringUtils;

import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.concurrent.TimeUnit;

/**
 * AB Test 事件缓存管理器
 *
 * @author fangzhuo@sensorsdata.cn
 * @version 1.0.0
 * @since 2021/06/17 11:18
 */
public class EventCacheManager {
  /**
   * 上报事件缓存
   */
  private final LoadingCache<String, String> eventCache;
  private final LogUtil log;

  public EventCacheManager(LogUtil log, int cacheTime, int cacheSize) {
    this.eventCache = CacheBuilder.newBuilder()
        .expireAfterWrite(cacheTime, TimeUnit.MINUTES)
        .maximumSize(cacheSize)
        .build(new CacheLoader<String, String>() {
          @Override
          public String load(String s) {
            return null;
          }
        });
    this.log = log;
    this.log.info("Initializing event cache size:{};duration:{}.", cacheSize, cacheTime);
  }


  public boolean judgeEventCacheExist(UserInfo userInfo, String experimentId, String abTestExperimentGroupId) {
    if (this.eventCache.getIfPresent(generateKey(userInfo, experimentId)) == null) {
      return false;
    }
    return StringUtils.equals(this.eventCache.getIfPresent(generateKey(userInfo, experimentId)),
        abTestExperimentGroupId);
  }

  public void setEventCache(UserInfo userInfo, String experimentId, String abTestExperimentGroupId) {
    this.eventCache.put(generateKey(userInfo, experimentId), abTestExperimentGroupId);
  }

  public long getCacheSize() {
    return this.eventCache.size();
  }

  private String generateKey(UserInfo userInfo, String experimentId) {
    String key = String.format("%s_%b_%s_%s", userInfo.getDistinctId(), userInfo.isLoginId(), experimentId,
        map2Str(userInfo.getCustomIds()));
    try {
      MessageDigest instance = MessageDigest.getInstance("MD5");
      instance.update(key.getBytes(StandardCharsets.UTF_8));
      return new String(instance.digest(), StandardCharsets.UTF_8);
    } catch (NoSuchAlgorithmException e) {
      log.error(
          "failed generate cache md5 key,so use original key.[UserInfo: {}, experimentId: {}, experimentGroupId: {}].",
          userInfo, experimentId);
    }
    return key;
  }


}
