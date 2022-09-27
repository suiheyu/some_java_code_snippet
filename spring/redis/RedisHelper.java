package com.inspur.bss.waf.redis;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.Map;
import java.util.concurrent.TimeUnit;

import static java.util.concurrent.TimeUnit.SECONDS;

/**
 * Redis缓存封装类
 * 【注：目前只是封装了Redis的String/List两种数据结构的方法，直接使用redisTemplate也支持Hash/Set/ZSet
 * 后续如果有用到Hash/Set/ZSet可以再次进行封装到该方法中；String比较特殊，使用jar包自带的StringRedisTemplate】
 *
 */
@SuppressWarnings("all")
@Component("redisHelper")
public class RedisHelper
{
    @Autowired
    private RedisTemplate redisTemplate;

    @Autowired
    private StringRedisTemplate stringRedisTemplate;

    /**
     * 根据key删除缓存
     *
     * @param key 键
     */
    public Boolean removeForValue(String key)
    {
        return stringRedisTemplate.delete(key);
    }

    /**
     * 获取指定key的失效时间
     *
     * @param key 键
     * @return 返回失效时间（单位：秒）
     */
    public Long getExpireForValue(String key)
    {
        return stringRedisTemplate.getExpire(key, TimeUnit.SECONDS);
    }

    /**
     * 判断redis中是否存在指定的key
     *
     * @param key 键
     * @return true:表示存在；false：不存在
     */
    public boolean isExistForValue(String key)
    {
        return stringRedisTemplate.hasKey(key);
    }

    /**
     * 根据key删除缓存
     *
     * @param key 键
     */
    public Boolean removeForList(Object key)
    {
        return redisTemplate.delete(key);
    }

    /**
     * 获取指定key的失效时间
     *
     * @param key 键
     * @return 返回失效时间（单位：秒）
     */
    public Long getExpireForList(Object key)
    {
        return redisTemplate.getExpire(key, TimeUnit.SECONDS);
    }

    /**
     * 判断redis中是否存在指定的key
     *
     * @param key 键
     * @return true:表示存在；false：不存在
     */
    public boolean isExistForList(Object key)
    {
        return redisTemplate.hasKey(key);
    }

    /**
     * 字符串型K-V  塞值
     *
     * @param key   键
     * @param value 值
     */
    public void setForValue(String key, String value)
    {
        stringRedisTemplate.opsForValue().set(key, value);
    }

    public <T> void setForValue(String key, T value)
    {
        redisTemplate.opsForValue().set(key, value);
    }

    public Object getObjectValue(String key)
    {
        return redisTemplate.opsForValue().get(key);
    }

    public String getStringValue(String key)
    {
        return redisTemplate.opsForValue().get(key,0,-1);
    }

    /**
     * 指定有效时间的存值
     *
     * @param key     键
     * @param value   值
     * @param timeout 超时时间
     * @param unit    时间单位，传null 默认为秒
     */
    public void setForValue(String key, String value, long timeout, TimeUnit unit)
    {
        if (null == unit)
        {
            unit = SECONDS;
        }
        stringRedisTemplate.opsForValue().set(key, value, timeout, unit);
    }

    /**
     * 如果缓存中存在该key，则存储失败，否则成功
     *
     * @param key   键
     * @param value 值
     * @return 塞值结果 true成功；false失败
     */
    public Boolean setIfAbsentForValue(String key, String value)
    {
        return stringRedisTemplate.opsForValue().setIfAbsent(key, value);
    }

    /**
     * 为多个键同时设置值
     *
     * @param map 传入<KEY，VALUE>形式的map值，会批量存储
     */
    public void multiSetForValue(Map<String, String> map)
    {
        stringRedisTemplate.opsForValue().multiSet(map);
    }

    /**
     * 获取多个键的值
     *
     * @param list 传入List<KEY>形式的集合，会批量获取
     * @return 返回List<String>结果集
     */
    public List<String> multiGetForValue(List<String> list)
    {
        return stringRedisTemplate.opsForValue().multiGet(list);
    }

    /**
     * 根据Key获取对应的值
     * [支持模糊查询，使用通配符 * ?  []]
     * *:表示不限制个数；?:限制字符的个数;[a,b] 表示只能查到中括号里面的字符
     *
     * @param key 键
     * @return 返回结果字符串
     */
    public String getForValue(String key)
    {
        return stringRedisTemplate.opsForValue().get(key);
    }

    /**
     * 根据key获取到存储的字符串的长度
     *
     * @param key 键
     * @return 返回结果字符串的长度
     */
    public Long sizeForValue(String key)
    {
        return stringRedisTemplate.opsForValue().size(key);
    }

    /**
     * Redis的List数据结构
     * 【获取字符串列表】
     *
     * @param key   键
     * @param start 索引开始位置 0 是从列表的第一个位置开始
     * @param end   索引的结束位置  -1表示到最后一位
     * @return
     */
    public List<String> getForList(Object key, long start, long end)
    {
        return redisTemplate.opsForList().range(key, start, end);
    }

    /**
     * Redis的List数据结构
     * 【修剪现有列表，使其只包含指定的指定范围的元素，起始和停止都是基于0的索引】
     *
     * @param key   键
     * @param start 索引开始位置 0 是从列表的第一个位置开始
     * @param end   索引的结束位置  -1表示到最后一位
     */
    public void trimForList(Object key, long start, long end)
    {
        redisTemplate.opsForList().trim(key, start, end);
    }

    /**
     * 返回存储在键中的列表的长度
     * [如果键不存在，则将其解释为空列表，并返回0。当key存储的值不是列表时返回错误]
     *
     * @param key 键
     * @return 返回列表的长度
     */
    public Long sizeForList(Object key)
    {
        return redisTemplate.opsForList().size(key);
    }

    /**
     * 将指定的值插入存储在键的列表的头部
     * 【如果键不存在，则在执行推送操作之前将其创建为空列表。（从左边插入）】
     *
     * @param key   键
     * @param value 值
     * @return 返回的结果为推送操作后的列表的长度
     */
    public Long leftPushForList(Object key, String value)
    {
        return redisTemplate.opsForList().leftPush(key, value);
    }

    /**
     * 批量把一个数组插入到列表中
     * 【如果键不存在，则在执行推送操作之前将其创建为空列表】
     *
     * @param key    键
     * @param values 数组值
     * @return 返回的结果为推送操作后的列表的长度
     */
    public Long leftPushAllForList(Object key, String[] values)
    {
        return redisTemplate.opsForList().leftPushAll(key, values);
    }

    /**
     * 批量把一个List集合插入到列表中
     * 【如果键不存在，则在执行推送操作之前将其创建为空列表】
     *
     * @param key       键
     * @param valueList 集合列表值
     * @return 返回的结果为推送操作后的列表的长度
     */
    public Long leftPushAllForList(Object key, List<Object> valueList)
    {
        return redisTemplate.opsForList().leftPushAll(key, valueList);
    }

    /**
     * 将指定的值插入存储在键的列表的尾部
     * 【如果键不存在，则在执行推送操作之前将其创建为空列表。（从右边插入）】
     *
     * @param key   键
     * @param value 值
     * @return 返回的结果为推送操作后的列表的长度
     */
    public Long rightPushForList(Object key, String value)
    {
        return redisTemplate.opsForList().rightPush(key, value);
    }

    /**
     * 批量把一个数组插入到列表尾部
     * 【如果键不存在，则在执行推送操作之前将其创建为空列表】
     *
     * @param key    键
     * @param values 数组值
     * @return 返回的结果为推送操作后的列表的长度
     */
    @SuppressWarnings("ALL")
    public Long rightPushAllForList(Object key, String[] values)
    {
        return redisTemplate.opsForList().rightPushAll(key, values);
    }

    /**
     * 批量把一个List集合插入到列表尾部
     * 【如果键不存在，则在执行推送操作之前将其创建为空列表】
     *
     * @param key       键
     * @param valueList 集合列表值
     * @return 返回的结果为推送操作后的列表的长度
     */
    public Long rightPushAllForList(Object key, List<String> valueList)
    {
        return redisTemplate.opsForList().rightPushAll(key, valueList);
    }

    /**
     * 只有存在key对应的列表才能将这个value值插入到key所对应的列表中
     * [从左边表头插入]
     *
     * @param key   键
     * @param value 值
     * @return 返回0表示插入失败；插入成功则返回列表的长度
     */
    public Long leftPushIfPresentForList(Object key, String value)
    {
        return redisTemplate.opsForList().leftPushIfPresent(key, value);
    }

    /**
     * 只有存在key对应的列表才能将这个value值插入到key所对应的列表中
     * [从右边表头插入]
     *
     * @param key   键
     * @param value 值
     * @return 返回0表示插入失败；插入成功则返回列表的长度
     */
    public Long rightPushIfPresentForList(Object key, String value)
    {
        return redisTemplate.opsForList().rightPushIfPresent(key, value);
    }

    /**
     * 在列表中index的位置设置value值
     *
     * @param key
     * @param index
     * @param value
     */
    public void setForlist(Object key, long index, String value)
    {
        redisTemplate.opsForList().set(key, index, value);
    }

    /**
     * 从存储在键中的列表中删除等于value值的第一个值，具体删除规则 根据count判断
     * count> 0：删除等于从头到尾移动的值第一个元素。
     * count <0：删除等于从尾到头移动的值第一个元素。
     * count = 0：删除等于value的所有元素。
     *
     * @param key   键
     * @param count 索引方向
     * @param value 值
     * @return
     */
    public Long removeForlist(Object key, long count, Object value)
    {
        return redisTemplate.opsForList().remove(key, count, value);
    }

    /**
     * 获取指定下标的list中存储的值
     *
     * @param key   键
     * @param index 下标
     * @return 返回String值
     */
    public String indexForlist(Object key, long index)
    {
        Object o = redisTemplate.opsForList().index(key, index);
        if (null != o)
        {
            return o.toString();
        }
        return null;
    }

    /**
     * 弹出最左边的元素，弹出之后该值在列表中将不复存在
     *
     * @param key 键
     * @return 返回表头第一条数据
     */
    public String leftPopForList(Object key)
    {
        Object o = redisTemplate.opsForList().leftPop(key);
        if (null != o)
        {
            return o.toString();
        }
        return null;
    }

    /**
     * 移出并获取列表的第一个元素
     * [如果列表没有元素会阻塞列表直到等待超时或发现可弹出元素为止]
     *
     * @param key
     * @param timeout 超时时间
     * @param unit    时间单位 传值为null,则默认为秒
     * @return 返回
     */
    public String leftPopForList(Object key, long timeout, TimeUnit unit)
    {
        if (null == unit)
        {
            unit = SECONDS;
        }

        Object o = redisTemplate.opsForList().leftPop(key, timeout, unit);

        if (null != o)
        {
            return o.toString();
        }
        return null;
    }

    /**
     * 弹出最右边的元素，弹出之后该值在列表中将不复存在
     *
     * @param key 键
     * @return 返回最后一条数据
     */
    public String rightPopForList(Object key)
    {
        Object o = redisTemplate.opsForList().rightPop(key);
        if (null != o)
        {
            return o.toString();
        }
        return null;
    }

    /**
     * 移出并获取列表的第后一个元素
     * [如果列表没有元素会阻塞列表直到等待超时或发现可弹出元素为止]
     *
     * @param key
     * @param timeout 超时时间
     * @param unit    时间单位 传值为null,则默认为秒
     * @return 返回
     */
    public String rightPopForList(Object key, long timeout, TimeUnit unit)
    {
        if (null == unit)
        {
            unit = SECONDS;
        }

        Object o = redisTemplate.opsForList().rightPop(key, timeout, unit);

        if (null != o)
        {
            return o.toString();
        }
        return null;
    }
}
