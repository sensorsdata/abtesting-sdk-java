package com.sensorsdata.analytics.javasdk.cache;

import com.fasterxml.jackson.databind.JsonNode;
import com.google.common.cache.CacheBuilder;
import com.google.common.cache.CacheLoader;
import com.google.common.cache.LoadingCache;

import java.util.concurrent.TimeUnit;

/**
 * AB Test 事件缓存管理器
 *
 * @author fz <fangzhuo@sensorsdata.cn>
 * @version 1.0.0
 * @since 2021/06/17 11:18
 */
public class EventCacheManager {
  /**
   * 上报事件缓存
   */
  private final LoadingCache<String, Object> eventCache;
  /**
   * 缓存过期时间
   */
  private static long duration;
  /**
   * 缓存最大容量
   */
  private static int size;

  private static final Object OBJ = new Object();

  private EventCacheManager() {
    this.eventCache = CacheBuilder.newBuilder()
        .expireAfterWrite(duration, TimeUnit.MINUTES)
        .maximumSize(size)
        .build(new CacheLoader<String, Object>() {
          @Override
          public JsonNode load(String s) {
            return null;
          }
        });
  }

  private static class SensorsABTestCacheManagerStaticNestedClass {
    private static final EventCacheManager INSTANCE = new EventCacheManager();
  }

  public static EventCacheManager getInstance(int cacheTime, int cacheSize) {
    duration = cacheTime;
    size = cacheSize;
    return EventCacheManager.SensorsABTestCacheManagerStaticNestedClass.INSTANCE;
  }

  public boolean judgeEventCacheExist(String distinctId, String experimentId) {
    return this.eventCache.getIfPresent(generateKey(distinctId, experimentId))
        != null;
  }

  public void setEventCache(String distinctId, String experimentId) {
    this.eventCache.put(generateKey(distinctId, experimentId), OBJ);
  }

  public long getCacheSize() {
    return this.eventCache.size();
  }

  private String generateKey(String distinctId, String experimentId) {
    return String.format("%s_%s", distinctId, experimentId);
  }
}
