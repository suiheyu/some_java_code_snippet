package com.inspur.bss.waf.dao.elasticsearch.defense;

import com.inspur.bss.commonsdk.utils.IdWorker;
import com.inspur.bss.waf.common.util.ZonedDateUtils;
import com.inspur.bss.waf.dao.annotation.RepositoryTypeIdentifier;
import com.inspur.bss.waf.dao.enums.RepositoryType;
import com.inspur.bss.waf.elasticsearch.EmptyPage;
import com.inspur.bss.waf.elasticsearch.constant.ElasticsSearchConstant;
import com.inspur.bss.waf.elasticsearch.document.DefendLogDocument;
import com.inspur.bss.waf.manage.defense.bean.WafLogBean;
import com.inspur.bss.waf.task.defense.bean.AttackIpDataPo;
import com.inspur.bss.waf.task.defense.bean.AttackTypeDataPo;
import com.inspur.bss.waf.task.defense.bean.AttackUrlDataPo;
import com.inspur.bss.waf.task.defense.interfaces.DefenderLogRepository;
import org.apache.groovy.util.Maps;
import org.elasticsearch.index.query.QueryBuilders;
import org.elasticsearch.index.query.RangeQueryBuilder;
import org.elasticsearch.script.Script;
import org.elasticsearch.search.aggregations.AbstractAggregationBuilder;
import org.elasticsearch.search.aggregations.AggregationBuilders;
import org.elasticsearch.search.aggregations.bucket.histogram.DateHistogramInterval;
import org.elasticsearch.search.aggregations.pipeline.PipelineAggregatorBuilders;
import org.springframework.data.elasticsearch.core.aggregation.AggregatedPage;
import org.springframework.data.elasticsearch.core.query.NativeSearchQueryBuilder;
import org.springframework.data.elasticsearch.repository.ElasticsearchRepository;
import org.springframework.stereotype.Repository;

import java.util.Date;
import java.util.List;
import java.util.stream.Collectors;

import static com.inspur.bss.waf.elasticsearch.utils.ESUtils.convertMap2Bean;
import static com.inspur.bss.waf.elasticsearch.utils.ESUtils.getResultMap;

/**
 * @author hexinyu
 * @create 2020/03/16 12:03
 */
@Repository
@RepositoryTypeIdentifier(RepositoryType.ES)
public interface DefenderLogElasticsearchRepository extends ElasticsearchRepository<DefendLogDocument,String>, DefenderLogRepository {

    void deleteAllByStatisDateLessThanEqual(Date date);

    @Override
    default void deleteDefenseLogBefore(Date date){
        deleteAllByStatisDateLessThanEqual(date);
    }

    @Override
    default void insertDefenseLog(List<WafLogBean> wafLogBeans, String topDomainId , String subDomainId, String userId){
        saveAll(
                readyDefenseLogDocument(wafLogBeans, topDomainId, subDomainId, userId)
        );
    }

    default List<DefendLogDocument> readyDefenseLogDocument(List<WafLogBean> data, String topDomainId, String domainId, String userId) {
        return data.stream()
                .map( wafLogBean ->
                        DefendLogDocument.builder()
                                .logId(wafLogBean.getLogId())
                                .statisDate(ZonedDateUtils.parseDate(wafLogBean.getStatisDate(),ZonedDateUtils.ZONE_UTC8))
                                .targetUrl(wafLogBean.getTargetUrl())
                                .siteName(wafLogBean.getSiteName())
                                .atkIp(wafLogBean.getAttackIp())
                                .atkType(wafLogBean.getAttackType())
                                .ruleId(wafLogBean.getRuleId())
                                .ruleLevel(wafLogBean.getRuleLevel())
                                .logTime(ZonedDateUtils.parseDateTime(wafLogBean.getLogTime(),ZonedDateUtils.ZONE_UTC8))
                                .topDomainId(topDomainId)
                                .subDomainId(domainId)
                                .userId(wafLogBean.getUserId())
                                .build()
                )
                .collect(Collectors.toList());
    }


    default List<AttackTypeDataPo> getAttackType(Date startTime, Date endTime){
        RangeQueryBuilder rangeQuery = QueryBuilders.rangeQuery("statis_date")
                .gte(startTime.getTime())
                .lte(endTime.getTime());

        NativeSearchQueryBuilder nativeSearchQueryBuilder = new NativeSearchQueryBuilder();
        //构造builder
        nativeSearchQueryBuilder.withQuery(rangeQuery)
                .withPageable(EmptyPage.INSTANCE)
                .addAggregation(
                        //groupBy user_id,top_domain_id,sub_domain_id, atk_type,DATE_FORMAT(log_time,'%Y-%m-%d %H:00:00')
                        initAttackTypeAggregationBuilder()
                );
        AggregatedPage<DefendLogDocument> aggregatedPage = (AggregatedPage<DefendLogDocument>) this.search(nativeSearchQueryBuilder.build());

        List<AttackTypeDataPo> attackTypeDataPos = convertMap2Bean(getResultMap(aggregatedPage), AttackTypeDataPo.class);
        //获取当日GMT+8的开始时间
        String beginOfDay = ZonedDateUtils.format2DateTime(ZonedDateUtils.getBeginOfDayZoned(ZonedDateUtils.now(),ZonedDateUtils.ZONE_UTC8));
        attackTypeDataPos.forEach( item -> {
            item.setStatisTime(beginOfDay);
            item.setId(String.valueOf(IdWorker.getNextId()));
        });
        return attackTypeDataPos;
    }

    default AbstractAggregationBuilder<?> initAttackTypeAggregationBuilder() {
        return AggregationBuilders.terms("userId").size(ElasticsSearchConstant.DEFAULT_TERM_SIZE).field("user_id.keyword").subAggregation(
                AggregationBuilders.terms("subDomainId").size(ElasticsSearchConstant.DEFAULT_TERM_SIZE).field("sub_domain_id.keyword").subAggregation(
                        AggregationBuilders.terms("attackType").size(ElasticsSearchConstant.DEFAULT_TERM_SIZE).field("atk_type.keyword").subAggregation(
                                AggregationBuilders.terms("domainId").size(ElasticsSearchConstant.DEFAULT_TERM_SIZE).field("top_domain_id.keyword").subAggregation(
                                        // 间隔一小时向下取整
                                        AggregationBuilders.dateHistogram("attackTime").field("log_time").dateHistogramInterval(DateHistogramInterval.HOUR)
                                                .subAggregation(
                                                        PipelineAggregatorBuilders.bucketSelector("null_attack_time_selector", Maps.of("count","attackCount"), new Script("params.count > 0"))
                                                )
                                                .subAggregation( AggregationBuilders.count("attackCount").field("atk_type.keyword") )
                                )
                        )
                )
        );
    }

    default List<AttackIpDataPo> getAttackIp(Date startTime, Date endTime){
        RangeQueryBuilder rangeQuery = QueryBuilders.rangeQuery("statis_date")
                .gte(startTime.getTime())
                .lte(endTime.getTime());

        NativeSearchQueryBuilder nativeSearchQueryBuilder = new NativeSearchQueryBuilder();
        //构造builder
        nativeSearchQueryBuilder.withQuery(rangeQuery)
                .withPageable(EmptyPage.INSTANCE)
                .addAggregation(
                        //groupBy user_id,top_domain_id,sub_domain_id, atk_type,DATE_FORMAT(log_time,'%Y-%m-%d %H:00:00')
                        initAttackIpAggregationBuilder()
                );
        AggregatedPage<DefendLogDocument> aggregatedPage = (AggregatedPage<DefendLogDocument>) this.search(nativeSearchQueryBuilder.build());
        List<AttackIpDataPo> attackIpDataPos = convertMap2Bean(getResultMap(aggregatedPage), AttackIpDataPo.class);
        //获取当日GMT+8的开始时间
        String beginOfDay = ZonedDateUtils.format2DateTime(ZonedDateUtils.getBeginOfDayZoned(ZonedDateUtils.now(),ZonedDateUtils.ZONE_UTC8));
        attackIpDataPos.forEach( item -> {
            item.setStatisTime(beginOfDay);
            item.setId(String.valueOf(IdWorker.getNextId()));
        });
        return attackIpDataPos;
    }

    default AbstractAggregationBuilder<?> initAttackIpAggregationBuilder() {
        return AggregationBuilders.terms("userId").size(ElasticsSearchConstant.DEFAULT_TERM_SIZE).size(ElasticsSearchConstant.DEFAULT_TERM_SIZE).size(ElasticsSearchConstant.DEFAULT_TERM_SIZE).field("user_id.keyword").subAggregation(
                AggregationBuilders.terms("subDomainId").size(ElasticsSearchConstant.DEFAULT_TERM_SIZE).size(ElasticsSearchConstant.DEFAULT_TERM_SIZE).size(ElasticsSearchConstant.DEFAULT_TERM_SIZE).field("sub_domain_id.keyword").subAggregation(
                        AggregationBuilders.terms("attackIp").size(ElasticsSearchConstant.DEFAULT_TERM_SIZE).size(ElasticsSearchConstant.DEFAULT_TERM_SIZE).size(ElasticsSearchConstant.DEFAULT_TERM_SIZE).field("atk_ip.keyword").subAggregation(
                                AggregationBuilders.terms("domainId").size(ElasticsSearchConstant.DEFAULT_TERM_SIZE).size(ElasticsSearchConstant.DEFAULT_TERM_SIZE).size(ElasticsSearchConstant.DEFAULT_TERM_SIZE).field("top_domain_id.keyword").subAggregation(
                                        // 间隔一小时向下取整
                                        AggregationBuilders.dateHistogram("attackTime").field("log_time").dateHistogramInterval(DateHistogramInterval.hours(1))
                                                .subAggregation(
                                                        PipelineAggregatorBuilders.bucketSelector("null_attack_time_selector", Maps.of("count","attackCount"), new Script("params.count > 0"))
                                                )
                                                .subAggregation( AggregationBuilders.count("attackCount").field("atk_ip.keyword") )
                                )
                        )
                )
        );
    }

    default List<AttackUrlDataPo> getAttackUrl(Date startTime, Date endTime){
        RangeQueryBuilder rangeQuery = QueryBuilders.rangeQuery("statis_date")
                .gte(startTime.getTime())
                .lte(endTime.getTime());

        NativeSearchQueryBuilder nativeSearchQueryBuilder = new NativeSearchQueryBuilder();
        //构造builder
        nativeSearchQueryBuilder.withQuery(rangeQuery)
                .withPageable(EmptyPage.INSTANCE)
                .addAggregation(
                        //groupBy user_id,top_domain_id,sub_domain_id, atk_type,DATE_FORMAT(log_time,'%Y-%m-%d %H:00:00')
                        initAttackUrlAggregationBuilder()
                );
        AggregatedPage<DefendLogDocument> aggregatedPage = (AggregatedPage<DefendLogDocument>) this.search(nativeSearchQueryBuilder.build());
        List<AttackUrlDataPo> attackUrlDataPos = convertMap2Bean(getResultMap(aggregatedPage), AttackUrlDataPo.class);
        //获取当日GMT+8的开始时间
        String beginOfDay = ZonedDateUtils.format2DateTime(ZonedDateUtils.getBeginOfDayZoned(ZonedDateUtils.now(),ZonedDateUtils.ZONE_UTC8));
        attackUrlDataPos.forEach( item -> {
            item.setStatistTime(beginOfDay);
            item.setId(String.valueOf(IdWorker.getNextId()));
        });
        return attackUrlDataPos;
    }

    default AbstractAggregationBuilder<?> initAttackUrlAggregationBuilder() {
        return AggregationBuilders.terms("userId").size(ElasticsSearchConstant.DEFAULT_TERM_SIZE).field("user_id.keyword").subAggregation(
                AggregationBuilders.terms("subDomainId").size(ElasticsSearchConstant.DEFAULT_TERM_SIZE).field("sub_domain_id.keyword").subAggregation(
                        AggregationBuilders.terms("targetUrl").size(ElasticsSearchConstant.DEFAULT_TERM_SIZE).field("target_url.keyword").subAggregation(
                                AggregationBuilders.terms("domainId").size(ElasticsSearchConstant.DEFAULT_TERM_SIZE).field("top_domain_id.keyword").subAggregation(
                                        // 间隔一小时向下取整
                                        AggregationBuilders.dateHistogram("attackTime").field("log_time").dateHistogramInterval(DateHistogramInterval.hours(1))
                                                .subAggregation(
                                                        PipelineAggregatorBuilders.bucketSelector("null_attack_time_selector", Maps.of("count", "attackCount"), new Script("params.count > 0"))
                                                )
                                                .subAggregation(AggregationBuilders.count("attackCount").field("target_url.keyword"))
                                                .subAggregation(AggregationBuilders.cardinality("ipCount").field("atk_ip.keyword"))
                                )
                        )
                )
        );
    }
}
