package com.inspur.bss.waf.common.aspect;

import com.inspur.bss.waf.common.annotation.DistributedLock;
import com.inspur.bss.waf.redis.lock.RedisDistributedLock;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang.StringUtils;
import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.annotation.Around;
import org.aspectj.lang.annotation.Aspect;
import org.aspectj.lang.annotation.Pointcut;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

/**
 * 分布式锁切面
 *
 * @author hexinyu
 * @create 2020/04/03 9:52
 */
@Aspect
@Slf4j
@Component
public class DistributedLockAspect {

    @Autowired
    private RedisDistributedLock distributedLock;

    private static final String DEFAULT_LOCK_KEY = "distributed-lock";

    @Pointcut("@annotation(com.inspur.bss.waf.common.annotation.DistributedLock)")
    public void pointCut(){}

    @Around("pointCut()&&@annotation(lockAnnotaion)")
    public Object around(ProceedingJoinPoint joinPoint, DistributedLock lockAnnotaion) throws Throwable{

        String lockKey = lockAnnotaion.lockKey();
        long tryLockTime = lockAnnotaion.tryLockTime();

        lockKey = StringUtils.isEmpty(lockKey) ? DEFAULT_LOCK_KEY + ":" + joinPoint.getSignature().getName() : lockKey;
        boolean isLock = false;
        try {
            log.info("尝试获取redis锁{},currentThread:{}",lockKey,Thread.currentThread().getName());
            isLock = distributedLock.tryLock(lockKey, tryLockTime);
            if(isLock){
                log.info("获取redis锁{}成功,currentThread:{}",lockKey,Thread.currentThread().getName());
                return joinPoint.proceed(joinPoint.getArgs());
            }
            log.info("获取redis锁{}失败,currentThread:{}",lockKey,Thread.currentThread().getName());
        } catch (InterruptedException e) {
            log.error("获取redis锁{}异常,currentThread:{}",lockKey,Thread.currentThread().getName(),e);
        }finally {
            if(isLock){
                distributedLock.unlock(lockKey);
            }
        }
        return null;
    }
}
