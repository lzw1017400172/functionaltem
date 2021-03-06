package com.deehow.core.util;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import com.deehow.core.Constants;
import com.deehow.core.support.cache.CacheManager;
import com.deehow.core.support.cache.RedisHelper;
import com.deehow.core.support.cache.RedissonHelper;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class CacheUtil {
	private static Logger logger = LogManager.getLogger(CacheUtil.class);
	private static CacheManager cacheManager;
	private static RedisHelper redisHelper;

	@Bean
	public CacheManager setCache() {
		cacheManager = getCache();
		return cacheManager;
	}

	public static CacheManager getCache() {
		if (cacheManager == null) {
			synchronized (CacheUtil.class) {
				if (cacheManager == null) {
					cacheManager = new RedissonHelper();
				}
			}
		}
		return cacheManager;
	}

	@Bean
	public RedisHelper setRedisHelper() {
		redisHelper = getRedisHelper();
		return redisHelper;
	}

	public static RedisHelper getRedisHelper() {
		if (redisHelper == null) {
			synchronized (CacheUtil.class) {
				if (redisHelper == null) {
					redisHelper = new RedisHelper();
				}
			}
		}
		return redisHelper;
	}

	/** 获取锁 */
	public static boolean getLock(String key) {
		try {
			if (!getRedisHelper().exists(key)) {
				synchronized (CacheUtil.class) {
					if (!getRedisHelper().exists(key)) {
						if (getRedisHelper().setnx(key, String.valueOf(System.currentTimeMillis()))) {
							return true;
						}
					}
				}
			}
			int expires = 1000 * 60 * 3;
			String currentValue = (String) getRedisHelper().get(key);
			if (currentValue != null && Long.parseLong(currentValue) < System.currentTimeMillis() - expires) {
				unlock(key);
				return getLock(key);
			}
			return false;
		} catch (Exception e) {
			logger.error(Constants.Exception_Head, e);
			return true;
		}
	}

	public static void unlock(String key) {
		getRedisHelper().unlock(key);
	}
}
