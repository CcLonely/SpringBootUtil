package com.example.bootutil.config;

public interface DistributedLock {

    long TIMEOUT_MILLIS = 30000;

    int RETRY_TIMES = 2;

    long SLEEP_MILLIS = 500;

    boolean lock(String key);


    boolean lock(String key, long expireMillis);

    /**
     * 注：此方法不再使用。请不要使用过期的方法
     * @param key
     * @param expireMillis
     * @param useless 有很多地方在用，不得不定义这个寂寞参数
     * @return
     */
    @Deprecated
    default boolean lock(String key, long expireMillis,int useless){
        return lock(key, expireMillis);
    }

    //    boolean lock(String key, long expireMillis, int retryTimes);
    boolean synchronize(String key, long expireMillis, int retryTimes);

    //    boolean lock(String key, long expireMillis, int retryTimes, long sleepMillis);
    boolean synchronize(String key, long expireMillis, int retryTimes, long sleepMillis);

    boolean releaseLock(String key);

    boolean releaseLockUnsafe(String key);
}