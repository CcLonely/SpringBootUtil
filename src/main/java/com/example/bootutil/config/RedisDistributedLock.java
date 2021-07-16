package com.example.bootutil.config;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.data.redis.core.script.DefaultRedisScript;
import org.springframework.stereotype.Component;
import org.springframework.transaction.support.TransactionSynchronizationAdapter;
import org.springframework.transaction.support.TransactionSynchronizationManager;

import javax.annotation.Resource;
import java.util.Arrays;
import java.util.UUID;
import java.util.concurrent.TimeUnit;

@Component
public class RedisDistributedLock extends AbstractDistributedLock {

	Logger log = LoggerFactory.getLogger(RedisDistributedLock.class);

	@Autowired
	@Resource
	private RedisTemplate<Object, Object> redisTemplate;

	private ThreadLocal<String> lockFlag = new ThreadLocal<String>();

	public static final String UNLOCK_LUA;

	static {
		StringBuilder sb = new StringBuilder();
		sb.append("if redis.call(\"get\",KEYS[1]) == ARGV[1] ");
		sb.append("then ");
		sb.append("    return redis.call(\"del\",KEYS[1]) ");
		sb.append("else ");
		sb.append("    return 0 ");
		sb.append("end ");
		UNLOCK_LUA = sb.toString();
	}


	public RedisDistributedLock() {
		super();
	}

	@Override
	public boolean synchronize(String key, long expireMillis, int retryTimes, long sleepMillis) {
		boolean result = setRedis(key, expireMillis);
		// 如果获取锁失败，按照传入的重试次数进行重试
		while ((!result) && retryTimes-- > 0) {
			try {
				log.info("lock failed, retrying..." + retryTimes);
				Thread.sleep(sleepMillis);
			} catch (InterruptedException e) {
				return false;
			}
			result = setRedis(key, expireMillis);
		}
		return result;
	}

	/**
	 *
	 * @param key
	 * @param expire MILLISECONDS
	 * @return
	 */
	private boolean setRedis(final String key, final long expire) {
		try {
			String uuid = UUID.randomUUID().toString();
			lockFlag.set(uuid);
			return redisTemplate.opsForValue().setIfAbsent(key,uuid,expire,TimeUnit.MILLISECONDS);
		} catch (Exception e) {
			log.info("redis lock error.", e);
		}
		return false;
	}


	@Override
	public boolean releaseLock(String key) {
		// 释放锁的时候，有可能因为持锁之后方法执行时间大于锁的有效期，此时有可能已经被另外一个线程持有锁，所以不能直接删除
		try {
			if (!TransactionSynchronizationManager.isActualTransactionActive()) {
				DefaultRedisScript<Boolean> defaultRedisScript = new DefaultRedisScript<Boolean>(UNLOCK_LUA, Boolean.class);
				return redisTemplate.execute(defaultRedisScript, Arrays.asList(key), lockFlag.get());
			} else {
				log.info("key:{} try release,but found active transaction,release after commit", key);
				try {
					// 注册钩子
					log.info("try register hook");
					TransactionSynchronizationManager.registerSynchronization(new TransactionSynchronizationAdapter() {
						public void afterCommit() {
							log.info("transaction committed,release");
							DefaultRedisScript<Boolean> defaultRedisScript = new DefaultRedisScript<Boolean>(UNLOCK_LUA, Boolean.class);
							final Boolean releaseResult = redisTemplate.execute(defaultRedisScript, Arrays.asList(key), lockFlag.get());
							log.info("transaction committed,release result:{}", releaseResult);
						}
					});
					log.info("register hook success");
				} catch (IllegalStateException e) {
					// 事务在非原子情况下已经提交,释放锁
					log.info("transaction earlier finished,release");
					DefaultRedisScript<Boolean> defaultRedisScript = new DefaultRedisScript<Boolean>(UNLOCK_LUA, Boolean.class);
					final Boolean releaseResult = redisTemplate.execute(defaultRedisScript, Arrays.asList(key), lockFlag.get());
					log.info("transaction earlier finished,release result:{}", releaseResult);
				} catch (Exception e) {
					// 注册钩子失败,其他异常,也释放锁
					log.error("register hook error,release");
					DefaultRedisScript<Boolean> defaultRedisScript = new DefaultRedisScript<Boolean>(UNLOCK_LUA, Boolean.class);
					final Boolean releaseResult = redisTemplate.execute(defaultRedisScript, Arrays.asList(key), lockFlag.get());
					log.error("register hook error,release result:{}", releaseResult);
				}
			}
		} catch (Exception e) {
			log.error("release lock occurred an exception", e);
		} finally {
			// 清除掉ThreadLocal中的数据，避免内存溢出
			lockFlag.remove();
		}
		return false;
	}

	/**
	 * 违背“解铃还须系铃人”原则，不管三七二十一，直接删掉key
	 * @param key
	 * @return
	 */
	@Override
	public boolean releaseLockUnsafe(String key) {
		try {
			return redisTemplate.delete(key);
		} catch (Exception e) {
			log.error("unsafe release lock occured an exception", e);
		}
		return false;
	}
}
	
