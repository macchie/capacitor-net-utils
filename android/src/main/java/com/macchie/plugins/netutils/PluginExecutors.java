package com.macchie.plugins.netutils;

import java.util.concurrent.ExecutorService;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.TimeUnit;

public final class PluginExecutors {

  private static final ExecutorService SHARED = new ThreadPoolExecutor(
    2, 8, 60L, TimeUnit.SECONDS,
    new LinkedBlockingQueue<>(64),
    new ThreadPoolExecutor.CallerRunsPolicy()
  );

  public static ExecutorService shared() {
    return SHARED;
  }

  public static void shutdownAll() {
    SHARED.shutdownNow();
  }

  private PluginExecutors() {}
}
