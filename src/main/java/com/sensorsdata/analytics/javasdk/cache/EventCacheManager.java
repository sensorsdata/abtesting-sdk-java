package com.sensorsdata.analytics.javasdk.cache;

import static com.sensorsdata.analytics.javasdk.util.ABTestUtil.map2Str;

import com.fasterxml.jackson.databind.JsonNode;
import com.google.common.cache.CacheBuilder;
import com.google.common.cache.CacheLoader;
import com.google.common.cache.LoadingCache;
import lombok.extern.slf4j.Slf4j;

import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.Map;
import java.util.concurrent.TimeUnit;

/**
 * AB Test 事件缓存管理器
 *
 * @author fangzhuo@sensorsdata.cn
 * @version 1.0.0
 * @since 2021/06/17 11:18
 */
@Slf4j
public class EventCacheManager {
  /**
   * 上报事件缓存
   */
  private final LoadingCache<String, Object> eventCache;

  private static final Object OBJ = new Object();

  public EventCacheManager(int cacheTime, int cacheSize) {
    this.eventCache = CacheBuilder.newBuilder()
        .expireAfterWrite(cacheTime, TimeUnit.MINUTES)
        .maximumSize(cacheSize)
        .build(new CacheLoader<String, Object>() {
          @Override
          public JsonNode load(String s) {
            return null;
          }
        });
    log.info("Initializing event cache size:{};duration:{}.", cacheSize, cacheTime);
  }


  public boolean judgeEventCacheExist(String distinctId, Boolean isLoginId, String experimentId,
      Map<String, String> customIds) {
    return this.eventCache.getIfPresent(generateKey(distinctId, isLoginId, experimentId, customIds))
        != null;
  }

  public void setEventCache(String distinctId, Boolean isLoginId, String experimentId, Map<String, String> customIds) {
    this.eventCache.put(generateKey(distinctId, isLoginId, experimentId, customIds), OBJ);
  }

  public long getCacheSize() {
    return this.eventCache.size();
  }

  private String generateKey(String distinctId, Boolean isLoginId, String experimentId, Map<String, String> customIds) {
    String key = String.format("%s_%b_%s_%s", distinctId, isLoginId, experimentId, map2Str(customIds));
    try {
      MessageDigest instance = MessageDigest.getInstance("MD5");
      instance.update(key.getBytes(StandardCharsets.UTF_8));
      return new String(instance.digest(), StandardCharsets.UTF_8);
    } catch (NoSuchAlgorithmException e) {
      log.error(
          "failed generate cache md5 key,so use original key.[distinctId:{},isLoginId:{},experimentId:{},customIds:{}].",
          distinctId, isLoginId, experimentId, map2Str(customIds));
    }
    return key;
  }


}
