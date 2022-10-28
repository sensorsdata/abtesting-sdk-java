package com.sensorsdata.analytics.javasdk.common;


public class Pair<K, V> {

  K key;
  V value;

  Pair(K key, V value) {
    this.key = key;
    this.value = value;
  }

  public K getKey() {
    return key;
  }

  public V getValue() {
    return value;
  }

  public static <K, V> Pair<K, V> of(K key, V value) {
    return new Pair<>(key, value);
  }
}
