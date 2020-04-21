package com.alenfive.commonapi.entity;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import javax.validation.constraints.NotNull;
import java.io.Serializable;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;


@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class QueryReq implements Serializable {

    /**
     * 数据源
     */
    private String table;

    /**
     * 返回字段
     * eg:["id","head_img headImg"]
     */
    private List<String> fields;

    /**
     * 过滤条件
     * eg:
     * {
     * 		"account":"ZJXHJT",
     * 		"start_time":{
     * 			"$gte":"2020-03-13 00:00:00",
     * 		    "$gt":"2020-03-13 00:00:00",
     * 			"$lte":"2020-03-20 00:00:00",
     * 		    "$lt":"2020-03-20 00:00:00"
     *      },
     * 		"type":{"$ne":2},
     * 	    "name":{"$like":"%hello%"},
     * 		"$and":{
     * 			"status":3,
     * 			"account":"001"
     *        },
     * 		"$or":{
     * 			"status":3,
     * 			"account":"001"
     *        },
     * 		"configure":["1111","0001","1001"]* 	}
     */
    @NotNull(message = "'filter' 不能为空")
    private Map<String,Object> filter;

    /**
     * 返回类型 list(默认) | obj | page
     */
    private ResultType resultType;

    /**
     * 返回记录数
     */
    private Integer pageSize;

    /**
     * 页数
     */
    private Integer pageNo = 1;


    /**
     * 排序,eg:{"id":"desc","name":"asc"}
     */
    private LinkedHashMap<String,String> sort;

    public enum ResultType{
        first,list,page
    }

    public Integer getIndex() {
        return (this.pageNo - 1) * getAblePageSize();
    }

    public Integer getAblePageSize(){
        return this.pageSize == null?15:this.getPageSize();
    }
}
