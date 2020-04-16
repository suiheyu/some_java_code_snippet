package com.inspur.bss.waf.elasticsearch.document;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.data.annotation.Id;
import org.springframework.data.elasticsearch.annotations.Document;
import org.springframework.data.elasticsearch.annotations.Field;
import org.springframework.data.elasticsearch.annotations.Mapping;

import java.util.Date;

/**
 * @author hexinyu
 * @create 2020/03/16 11:55
 */
@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
@Document(indexName = "waf_defend_log", type = "defend_log")
@Mapping(mappingPath = "/es/mapping/waf_defend_log.json")
public class DefendLogDocument {
    @Id
    @Field(name="log_id")
    private String logId;
    @Field(name="statis_date")
    private Date statisDate;
    @Field(name="target_url")
    private String targetUrl;
    @Field(name="site_name")
    private String siteName;
    @Field(name="atk_ip")
    private String atkIp;
    @Field(name="atk_type")
    private String atkType;
    @Field(name="rule_id")
    private String ruleId;
    @Field(name="rule_level")
    private String ruleLevel;
    @Field(name="log_time")
    private Date logTime;
    @Field(name="top_domain_id")
    private String topDomainId;
    @Field(name="sub_domain_id")
    private String subDomainId;
    @Field(name="user_id")
    private String userId;
}
