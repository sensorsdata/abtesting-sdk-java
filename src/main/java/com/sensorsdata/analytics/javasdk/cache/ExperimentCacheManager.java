package com.sensorsdata.analytics.javasdk.cache;

import com.sensorsdata.analytics.javasdk.SensorsABTestConst;

import com.fasterxml.jackson.databind.JsonNode;
import com.google.common.cache.CacheBuilder;
import com.google.common.cache.CacheLoader;
import com.google.common.cache.LoadingCache;

import java.util.Iterator;
import java.util.concurrent.TimeUnit;

/**
 * AB Test 试验结果缓存管理器
 *
 * @author fz <fangzhuo@sensorsdata.cn>
 * @version 1.0.0
 * @since 2021/06/16 15:41
 */
public class ExperimentCacheManager {

  private final LoadingCache<String, JsonNode> experimentResultCache;
  /**
   * 缓存过期时间
   */
  private static long duration;
  /**
   * 缓存最大容量
   */
  private static int size;

  private ExperimentCacheManager() {
    this.experimentResultCache = CacheBuilder.newBuilder()
        .expireAfterWrite(duration, TimeUnit.MINUTES)
        .maximumSize(size)
        .build(new CacheLoader<String, JsonNode>() {
          @Override
          public JsonNode load(String s) {
            return null;
          }
        });
  }

  private static class SensorsABTestCacheManagerStaticNestedClass {
    private static final ExperimentCacheManager INSTANCE = new ExperimentCacheManager();
  }

  public static ExperimentCacheManager getInstance(int cacheTime, int cacheSize) {
    duration = cacheTime;
    size = cacheSize;
    return SensorsABTestCacheManagerStaticNestedClass.INSTANCE;
  }

  public JsonNode getExperimentResultByCache(String distinctId, boolean isLoginId, String experimentName) {
    JsonNode experimentResult = this.experimentResultCache.getIfPresent(generateKey(distinctId, isLoginId));
    if (experimentResult == null) {
      return null;
    }
    JsonNode results = experimentResult.findValue(SensorsABTestConst.RESULTS_KEY);
    Iterator<JsonNode> resIterator = results.elements();
    while (resIterator.hasNext()) {
      JsonNode experiment = resIterator.next();
      Iterator<JsonNode> varIterator = experiment.findValue(SensorsABTestConst.VARIABLES_KEY).elements();
      while (varIterator.hasNext()) {
        JsonNode variable = varIterator.next();
        if (variable.findValue("name").asText().equals(experimentName)) {
          return experimentResult;
        }
      }
    }
    return null;
  }

  public void setExperimentResultCache(String distinctId, boolean isLoginId, JsonNode experiment) {
    if (experiment != null) {
      this.experimentResultCache.put(generateKey(distinctId, isLoginId), experiment);
    }
  }

  public long getCacheSize() {
    return this.experimentResultCache.size();
  }

  private String generateKey(String distinctId, boolean isLoginId) {
    return String.format("%s_%b", distinctId, isLoginId);
  }
}
