package com.example.bootutil.config;

public abstract class AbstractDistributedLock implements DistributedLock {
 
	@Override
	public boolean lock(String key) {
		return synchronize(key , TIMEOUT_MILLIS, 0, 0L);
	}
 
//	@Override
//	public boolean lock(String key, int retryTimes) {
//		return lock(key, TIMEOUT_MILLIS, retryTimes, SLEEP_MILLIS);
//	}
 
//	@Override
//	public boolean synchronizedLock(String key, int retryTimes, long sleepMillis) {
//		return lock(key, TIMEOUT_MILLIS, retryTimes, sleepMillis);
//	}
 
	@Override
	public boolean lock(String key, long expireMillis) {
		return synchronize(key, expireMillis, 0, 0);
	}
 
	@Override
	public boolean synchronize(String key, long expireMillis, int retryTimes) {
		return synchronize(key, expireMillis, retryTimes, SLEEP_MILLIS);
	}
 
}