package com.inspur.bss.waf.common.util;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import java.util.function.Function;
import java.util.function.Supplier;

/**
 *
 * 反射初始化枚举类
 * @author hexinyu
 * @create 2020/03/11 14:55
 */
public class EnumUtils {

    @Nullable
    public static <T extends Enum<T>, V> T initEnum(@Nonnull T[] enumConstants, @Nonnull Function<T,V> compareFun, @Nullable V value, @Nonnull Supplier<T> defaultEnum){
        for(T enumConstant:enumConstants){
            if(compareFun.apply(enumConstant).equals(value) ){
                return enumConstant;
            }
        }
        return defaultEnum.get();
    }

    public static <T extends Enum<T>, V> T initEnum(Class<T> enumClazz, Function<T,V> compareFun, V value, T defaultEnum){
        return initEnum(enumClazz.getEnumConstants(), compareFun, value, () -> defaultEnum);
    }

    public static <T extends Enum<T>, V> T initEnum(Class<T> enumClazz, Function<T,V> compareFun, V value, int defaultIndex){
        T[] enumConstants = enumClazz.getEnumConstants();
        return initEnum(enumConstants, compareFun, value, () -> enumConstants[defaultIndex % enumConstants.length]);
    }

    @Nullable
    public static <T extends Enum<T>, V> T initEnumDefaultNull(Class<T> enumClazz, Function<T,V> compareFun, V value){
        return initEnum(enumClazz.getEnumConstants(), compareFun, value, () -> null);
    }

    public static <T extends Enum<T>, V> T initEnumDefaultFirst(Class<T> enumClazz, Function<T,V> compareFun, V value){
        T[] enumConstants = enumClazz.getEnumConstants();
        return initEnum(enumConstants, compareFun, value, () -> enumConstants[0]);
    }



}
