package com.sensorsdata.analytics.javasdk.cache;

import static com.sensorsdata.analytics.javasdk.util.ABTestUtil.map2Str;

import com.sensorsdata.analytics.javasdk.SensorsABTestConst;

import com.fasterxml.jackson.databind.JsonNode;
import com.google.common.cache.CacheBuilder;
import com.google.common.cache.CacheLoader;
import com.google.common.cache.LoadingCache;
import lombok.extern.slf4j.Slf4j;

import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.Iterator;
import java.util.Map;
import java.util.concurrent.TimeUnit;

/**
 * AB Test 试验结果缓存管理器
 *
 * @author fangzhuo@sensorsdata.cn
 * @version 1.0.0
 * @since 2021/06/16 15:41
 */
@Slf4j
public class ExperimentCacheManager {

  private final LoadingCache<String, JsonNode> experimentResultCache;

  public ExperimentCacheManager(int cacheTime, int cacheSize) {
    this.experimentResultCache = CacheBuilder.newBuilder()
        .expireAfterWrite(cacheTime, TimeUnit.MINUTES)
        .maximumSize(cacheSize)
        .build(new CacheLoader<String, JsonNode>() {
          @Override
          public JsonNode load(String s) {
            return null;
          }
        });
    log.info("Initializing experiment cache:size:{};duration:{}.", cacheSize, cacheTime);
  }

  public JsonNode getExperimentResultByCache(String distinctId, boolean isLoginId, Map<String, String> customIds,
      String experimentName) {
    String key = generateCacheKey(distinctId, isLoginId, customIds);
    JsonNode experimentResult = this.experimentResultCache.getIfPresent(key);
    if (experimentResult == null) {
      log.debug("current key not found in cache.[key:{},experiment:{}]", key, experimentName);
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
    log.debug("current experimentName not hit cache result.[key:{},experiment:{}]", key, experimentName);
    return null;
  }

  public void setExperimentResultCache(String distinctId, boolean isLoginId, Map<String, String> customIds,
      JsonNode experiment) {
    if (experiment != null) {
      String key = generateCacheKey(distinctId, isLoginId, customIds);
      log.debug("Caches the current experiment to the manager.[key:{},experiment:{}]", key, experiment);
      this.experimentResultCache.put(key, experiment);
    }
  }

  public long getCacheSize() {
    return this.experimentResultCache.size();
  }

  private String generateCacheKey(String distinctId, boolean isLoginId, Map<String, String> customIds) {
    String key = String.format("%s_%b_%s", distinctId, isLoginId, map2Str(customIds));
    try {
      MessageDigest instance = MessageDigest.getInstance("MD5");
      instance.update(key.getBytes(StandardCharsets.UTF_8));
      return new String(instance.digest(), StandardCharsets.UTF_8);
    } catch (NoSuchAlgorithmException e) {
      log.error("failed generate cache md5 key,so use original key.[distinctId:{},isLoginId:{},customIds:{}].",
          distinctId, isLoginId, map2Str(customIds));
    }
    return key;
  }

}
