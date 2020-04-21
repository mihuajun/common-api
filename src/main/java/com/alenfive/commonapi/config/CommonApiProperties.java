package com.alenfive.commonapi.config;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

import java.util.Collections;
import java.util.List;
import java.util.Map;

@ConfigurationProperties(prefix = "common-api")
@Data
@AllArgsConstructor
@NoArgsConstructor
@Component
public class CommonApiProperties {

    /**
     * 表前缀：t_
     */
    private String tablePrefix = "";

    /**
     * 黑名单配置
     */
    private Map<String, List<String>> blackTableFieldList;

    public Map<String, List<String>> getBlackTableFieldList() {
        return blackTableFieldList == null? Collections.EMPTY_MAP:blackTableFieldList;
    }
}
