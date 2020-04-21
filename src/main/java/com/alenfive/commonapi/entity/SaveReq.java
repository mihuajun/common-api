package com.alenfive.commonapi.entity;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.io.Serializable;
import java.util.Map;


@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class SaveReq implements Serializable {

    /**
     * 数据源
     */
    private String table;

    /**
     * {
     *   "account":"test",
     *   "title":爆炸额",
     *   "pic":"http://www.baidu.com",
     *   "remark":"发动机佛大姐夫",
     *   "content":"内容"
     * }
     * 存储对象
     */
    private Map<String,Object> saveObj;
}
