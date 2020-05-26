package com.alenfive.commonapi.config;


import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Map;


@Repository
public class CommonMapper {

    @Autowired
    private JdbcTemplate jdbcTemplate;

    private final String listFieldSql = "select COLUMN_NAME from information_schema.columns where table_name='%s' and table_schema=database()";

    List<Map<String, Object>> query(String sql){
        return jdbcTemplate.queryForList(sql);
    }

    Long count(String sql){
        return jdbcTemplate.queryForObject(sql,Long.class);
    }

    List<String> listField(String table){
        String sql = String.format(listFieldSql,table);
        return jdbcTemplate.queryForList(sql,String.class);
    }

    void save(String sql){
        jdbcTemplate.execute(sql);
    }

    Integer update(String sql){
        return jdbcTemplate.update(sql);
    }

    public void delete(String sql){
        jdbcTemplate.execute(sql);
    }
}
