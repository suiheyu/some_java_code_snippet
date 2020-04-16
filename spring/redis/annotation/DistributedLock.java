package com.inspur.bss.waf.common.annotation;

import org.springframework.data.mongodb.core.mapping.Document;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * 分布式锁
 * @author hexinyu
 * @create 2020/04/03 9:49
 */
@Document
@Target(ElementType.METHOD)
@Retention(RetentionPolicy.RUNTIME)
public @interface DistributedLock {

    /**
     * 锁key
     * @return
     */
    String lockKey();

    /**
     * 尝试获取锁时间（单位：秒）
     * @return
     */
    long tryLockTime() default 0;
}
