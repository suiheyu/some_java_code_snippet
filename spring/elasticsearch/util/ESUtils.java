package com.inspur.bss.waf.elasticsearch.utils;

import com.fasterxml.jackson.annotation.JsonProperty;
import org.apache.commons.beanutils.BeanUtils;
import org.apache.commons.collections.ListUtils;
import org.apache.commons.collections.MapUtils;
import org.elasticsearch.search.aggregations.Aggregation;
import org.elasticsearch.search.aggregations.ParsedMultiBucketAggregation;
import org.elasticsearch.search.aggregations.bucket.MultiBucketsAggregation;
import org.elasticsearch.search.aggregations.metrics.NumericMetricsAggregation;
import org.joda.time.DateTime;
import org.springframework.data.elasticsearch.core.aggregation.AggregatedPage;
import org.springframework.stereotype.Component;
import org.springframework.util.CollectionUtils;
import org.springframework.util.ReflectionUtils;

import java.util.*;
import java.util.stream.Collectors;

/**
 * @author hexinyu
 * @create 2020/03/18 13:04
 */
@Component
public class ESUtils {

    public final static Map<Class<?>,Map<String,String>> aliasMapCache = new HashMap<>();

    /**
     * 从AggregatedPage取得聚合结果
     * @param aggregatedPage
     * @return
     */
    public static List<Map<String,Object>> getResultMap(AggregatedPage<?> aggregatedPage){
        List<Aggregation> aggregations = aggregatedPage.getAggregations().asList();
        return transferAggTree2Map(aggregations,null);
    }

    /**
     * 获取带@JsonProperty 的字段别名信息
     * @param clazz
     * @return
     */
    private static Map<String,String> getAliasMapByClass(Class<?> clazz){
        Map<String, String> aliasMap;
        if( aliasMapCache.containsKey(clazz) ){
            aliasMap = aliasMapCache.get(clazz);
        }else {
            aliasMap = new HashMap<>();
            ReflectionUtils.doWithFields(clazz,(filed) -> {
                JsonProperty annotation = filed.getAnnotation(JsonProperty.class);
                if(annotation != null){
                    aliasMap.put(filed.getName(),annotation.value());
                }
            });
            aliasMapCache.put(clazz, aliasMap);
        }
        return aliasMap;
    }

    private static void doAliase(Map<String,Object> sourceMap,Map<String, String> aliasMap){
        aliasMap.forEach(
                (sourceName, aliasName) -> Optional.ofNullable(sourceMap.get(aliasName))
            .ifPresent(value -> sourceMap.put(sourceName, value))
        );
    }

    /**
     * 把Map转换为对应的bean
     * @param maps
     * @param clazz
     * @param <T>
     * @return
     */
    public static <T> List<T> convertMap2Bean(List<Map<String,Object>> maps, Class<T> clazz){
        Map<String, String> aliasMap = getAliasMapByClass(clazz);
        return maps.stream()
                .peek(mapItem -> doAliase(mapItem,aliasMap))
                .map( mapItem -> {
                    T tempBean;
                    try {
                        tempBean  = clazz.getConstructor(null).newInstance();
                        BeanUtils.populate(tempBean,mapItem);
                    } catch (Exception e) {
                        return null;
                    }
                    return tempBean;
                }).filter(Objects::nonNull).collect(Collectors.toList());
    }

    /**
     * 把 树型的Aggregation转换为 二维的List<map>
     * @param aggregations
     * @param partenMap
     * @return
     */
    public static List<Map<String,Object>> transferAggTree2Map(List<Aggregation> aggregations, Map<String,Object> partenMap){
        List<Map<String, Object>> resultMaps = new LinkedList<>();
        if(MapUtils.isEmpty(partenMap)){
            partenMap = new HashMap<>();
        }

        //遍历聚合结果
        for( Aggregation aggregation: aggregations){
            if( aggregation instanceof  ParsedMultiBucketAggregation) {
                MultiBucketsAggregation multiBucketsAggregation = (MultiBucketsAggregation) aggregation;
                String aggregationName = multiBucketsAggregation.getName();
                List<? extends MultiBucketsAggregation.Bucket> buckets = multiBucketsAggregation.getBuckets();
                //遍历桶,取得所有对应的值
                List<Map<String, Object>> tempMaps = new LinkedList<>();
                for (MultiBucketsAggregation.Bucket bucket : buckets) {
                    Map<String, Object> tempMap = copyMap(partenMap);
                    tempMap.put(aggregationName, convertType(bucket.getKey()));
                    tempMaps.addAll(transferAggTree2Map(bucket.getAggregations().asList(),tempMap));
                }
                resultMaps = cartesianProductMerge(resultMaps, tempMaps);
            }else if( aggregation instanceof NumericMetricsAggregation.SingleValue){
                NumericMetricsAggregation.SingleValue singleValueAggregation = (NumericMetricsAggregation.SingleValue) aggregation;
                Map<String, Object> tempMap = copyMap(partenMap);
                tempMap.put(singleValueAggregation.getName(),convertType(singleValueAggregation.value()));
                resultMaps = cartesianProductMerge(resultMaps,Collections.singletonList(tempMap));
            }
        }
        return resultMaps;
    }

    /**
     * 笛卡尔积合并map
     * @param mapsOld
     * @param mapsNew
     * @return
     */
    private static List<Map<String, Object>> cartesianProductMerge(List<Map<String, Object>> mapsOld, List<Map<String, Object>> mapsNew){
        if(CollectionUtils.isEmpty(mapsOld) && CollectionUtils.isEmpty(mapsNew)){
            return ListUtils.EMPTY_LIST;
        }
        int size = mapsOld.size() * mapsNew.size();
        List<Map<String, Object>> resultMaps = new ArrayList<>( size == 0 ? Math.max(mapsNew.size(),mapsOld.size())  : size);
        if(mapsOld.size() == 0 || mapsNew.size() == 0){
            resultMaps.addAll(mapsNew);
            resultMaps.addAll(mapsOld);
        }else{
            for (Map<String, Object> oldItem : mapsOld) {
                for (Map<String, Object> newItem : mapsNew) {
                    Map<String, Object> tempMap = new HashMap<>();
                    tempMap.putAll(oldItem);
                    tempMap.putAll(newItem);
                    resultMaps.add(tempMap);
                }
            }
        }
        return resultMaps;
    }

    private static Object convertType(Object object){
        if(object instanceof DateTime){
            return new Date( ((DateTime)object).getMillis() );
        }
        return object;
    }

    /**
     * 复制map对象
     * @param map
     * @return
     */
    public static Map<String,Object> copyMap(Map<String,Object> map){
        return map.entrySet().stream()
                .collect(Collectors.toMap(Map.Entry::getKey, Map.Entry::getValue));
    }

    public static <T> List<List<T>> split(List<T> list,int splitLength){
        List<List<T>> lists = new LinkedList<>();
        for( int i = 0; i < list.size() ; i = i + splitLength){
            lists.add( list.subList(i, Math.min(i + splitLength, list.size())));
        }
        return lists;
    }
}
