package com.inspur.bss.waf.redis.lock;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.integration.redis.util.RedisLockRegistry;
import org.springframework.stereotype.Component;

import java.util.concurrent.TimeUnit;
import java.util.concurrent.locks.Lock;

/**
 * Distributed lock implemented by redis
 *
 * @author sunguangtao
 * @date 2018/12/18
 */
@Slf4j
@Component
@SuppressWarnings("all")
public class RedisDistributedLock
{
    private final RedisLockRegistry redisLockRegistry;

    @Autowired
    public RedisDistributedLock(RedisLockRegistry redisLockRegistry) {
        this.redisLockRegistry = redisLockRegistry;
    }

    public boolean tryLock(String lockKey, Long expireSeconds) throws InterruptedException{
        Lock lock = redisLockRegistry.obtain(lockKey);
        return lock.tryLock(expireSeconds, TimeUnit.SECONDS);
    }

    public void unlock(String lockKey) {
        Lock lock = redisLockRegistry.obtain(lockKey);
        lock.unlock();
    }
}
