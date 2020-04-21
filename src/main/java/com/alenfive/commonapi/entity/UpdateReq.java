package com.alenfive.commonapi.entity;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import javax.validation.constraints.NotNull;
import java.io.Serializable;
import java.util.Map;


@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class UpdateReq implements Serializable {

    /**
     * 数据源
     */
    private String table;

    /**
     * 过滤条件
     * eg:
     * {
     * 		"account":"test",
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
     *{
     *     "buttonJson":"hello1",
     *     "shareNum":2
     *   }
     * 更新对象
     */
    @NotNull(message = "'updateSet' 不能为空")
    private Map<String,Object> updateSet;

}
