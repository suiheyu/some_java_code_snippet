package com.inspur.bss.waf.common.annotation;


import java.lang.annotation.*;

/**
 * 用于指定枚举类在数据库存的字段
 */
@Target({ ElementType.FIELD })
@Retention(RetentionPolicy.RUNTIME)
@Documented
public @interface EnumPersistenceValue {

}
