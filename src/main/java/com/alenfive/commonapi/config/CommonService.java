package com.alenfive.commonapi.config;


import com.alenfive.commonapi.entity.*;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.util.CollectionUtils;

import java.util.*;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

@Service
@Slf4j
public class CommonService {

    @Autowired
    private CommonMapper commonMapper;

    private final List<String> scope = Arrays.asList("$or","$and");
    private final List<String> variables = Arrays.asList("$gt","$gte","$lt","$lte","$ne","$like");
    private final List<String> sortEnum = Arrays.asList("asc","desc");
    private final Pattern tableMetaRegexPat = Pattern.compile("[^(a-zA-Z0-9_)]+");
    private final Pattern valueRegexPat = Pattern.compile("[']+");

    @Autowired
    private CommonApiProperties commonApiProperties;



    public Object query(QueryReq queryReq) {

        String table = buildTable(queryReq.getTable());
        String field = buildField(table,queryReq.getFields());
        String filter = buildFilter("$and",queryReq.getFilter());
        String sort = buildSort(queryReq.getSort());
        String limit = buildLimit(queryReq);

        try {
            return buildResult(table,field,filter,sort,limit,queryReq);
        }catch (Exception e){
            throw new BizException(ErrorCode.SERVER_ERROR.getCode(),e.getCause()==null?e.getMessage():e.getCause().getMessage());
        }
    }

    private Object buildResult(String table, String field, String filter, String sort, String limit,QueryReq queryReq) {
        StringBuilder sql = new StringBuilder("select ")
                .append(field)
                .append(" from ")
                .append(table)
                .append(" where ")
                .append(filter)
                .append(sort)
                .append(limit);
        log.debug("generator sql:{}",sql.toString());

        if (QueryReq.ResultType.first.equals(queryReq.getResultType())){
            List<Map<String,Object>> resultList = commonMapper.query(sql.toString());
            return resultList.size()==0?Collections.EMPTY_MAP:resultList.get(0);
        }

        if (QueryReq.ResultType.list.equals(queryReq.getResultType())){
            return commonMapper.query(sql.toString());
        }

        Map<String,Object> pager = new HashMap<>(5);
        pager.put("pageNo",queryReq.getPageNo());
        pager.put("pageSize",queryReq.getPageSize());
        String countSql = new StringBuilder("select ")
                .append("count(1)")
                .append(" from ")
                .append(table)
                .append(" where ")
                .append(filter).toString();

        Long totalRecords = commonMapper.count(countSql);
        pager.put("totalRecords",totalRecords);
        if (totalRecords == 0){
            return pager;
        }

        List<Map<String,Object>> resultList = commonMapper.query(sql.toString());
        pager.put("data",resultList);

        return pager;
    }

    private String buildLimit(QueryReq queryReq) {
        switch (queryReq.getResultType()){
            case first: return " limit 1 ";
            case list : return queryReq.getPageSize() == null? "":" limit " + queryReq.getPageSize();
            case page : return " limit " + queryReq.getIndex()+","+queryReq.getAblePageSize();
        }
        return "";
    }

    private String buildTable(String table) {
        validateTableMeta(table);
        return commonApiProperties.getTablePrefix()+camelToUnderline(table);
    }

    private String buildField(String table,List<String> fields) {
        List<String> blankFieldList = commonApiProperties.getBlackTableFieldList().get(table);

        if (blankFieldList !=null && blankFieldList.contains("*")){
            throw new BizException(ErrorCode.SERVER_ERROR.getCode(),"无权限访问此数据源任意字段");
        }

        if (CollectionUtils.isEmpty(fields)){
            fields = commonMapper.listField(table).stream().map(this::underlineToCamel).collect(Collectors.toList());
        }

        return fields.stream().map(field->{
            validateTableMeta(field);
            if (blankFieldList !=null && blankFieldList.contains(field)){
                throw new BizException(ErrorCode.SERVER_ERROR.getCode(),"无权限访问此数据源,'"+field+"'字段");
            }
            StringBuilder sb = new StringBuilder();
            if(!tableMetaRegexPat.matcher(field).find()){
                sb.append(camelToUnderline(field)).append(" as ");
            }
            sb.append(field);
            return sb.toString();
        }).collect(Collectors.joining(","));
    }

    private void validateTableMeta(String item) {
        Matcher matcher = tableMetaRegexPat.matcher(item);
        if(matcher.find()){
            String subStr = scopeStr(item,matcher.start(0)-10,matcher.end(0)+10);
            throw new BizException(ErrorCode.FORMAT_ERROR.getCode(),"包含特殊符号：'"+subStr+"'");
        }
    }

    private void validateValue(String item) {
        Matcher matcher = valueRegexPat.matcher(item);
        if(matcher.find()){
            String subStr = scopeStr(item,matcher.start(0)-10,matcher.end(0)+10);
            throw new BizException(ErrorCode.FORMAT_ERROR.getCode(),"包含特殊符号：'"+subStr+"'");
        }
    }

    private String scopeStr(String item, int start, int end) {
        start = start<0?0:start;
        end = end>item.length()?item.length():end;
        return item.substring(start,end);
    }

    private String buildSort(LinkedHashMap<String,String> sortMap) {
        if (CollectionUtils.isEmpty(sortMap)){
            return "";
        }
        StringBuilder valStr = new StringBuilder(" order by ");
        boolean isFirst = true;
        for(String sort : sortMap.keySet()){
            validateTableMeta(sort);
            if (!sortEnum.contains(sortMap.get(sort))){
                throw new BizException(ErrorCode.FORMAT_ERROR.getCode(),"包含未知匹配符："+sortMap.get(sort));
            }
            if (!isFirst){
                valStr.append(",");
            }
            isFirst = false;
            valStr.append(sort).append(" ").append(sortMap.get(sort)).append(" ");
        }
        return valStr.toString();
    }

    private String buildFilter(String connector, Map<String, Object> filter) {
        connector = connector.replace("$","");
        StringBuilder valStr = new StringBuilder();
        boolean isFirst = true;

        for(String key:filter.keySet()){

            if (!isFirst){
                valStr.append(" ").append(connector).append(" ");
            }
            Object value = filter.get(key);

            isFirst = false;

            if (scope.contains(key)){
                valStr.append("(").append(buildFilter(key, (Map<String, Object>) value)).append(")");
                continue;
            }

            key = buildFieldKey(key);

            if(value instanceof Map){
                valStr.append(buildVariableValue(key,(Map<String, Object>) value));
            }else {
                valStr.append(key).append(buildValue(value));
            }

        }

        return valStr.toString();
    }

    private String buildFieldKey(String key) {
        validateTableMeta(key);
        return camelToUnderline(key);
    }

    private String buildVariableValue(String key, Map<String, Object> filter) {
        StringBuilder valStr = new StringBuilder();
        boolean isFirst = true;
        for(String item :filter.keySet()){
            if (!variables.contains(item)){
                throw new BizException(ErrorCode.FORMAT_ERROR.getCode(),"包含未知匹配符："+item);
            }
            String connector = "";
            switch (item){
                case "$gte":connector=" >= ";break;
                case "$gt":connector=" > ";break;
                case "$lte":connector=" <= ";break;
                case "$lt":connector=" < ";break;
                case "$ne":connector=" != ";break;
                case "$like":connector=" like ";break;
            }
            if (!isFirst){
                valStr.append(" and ");
            }
            isFirst = false;
            valStr.append(key).append(connector).append(buildStrValue(filter.get(item)));
        }
        return valStr.toString();
    }

    private String buildValue(Object val) {
        StringBuilder valStr = new StringBuilder();
        if (val instanceof Collection){
            valStr.append(((Collection)val).stream().map(item->buildStrValue(item)).collect(Collectors.joining(","," in (",")")));
        }else {
            valStr.append(" = ").append(buildStrValue(val));
        }
        return valStr.toString();
    }

    private String buildStrValue(Object val){
        if (val instanceof Number){
            return val.toString();
        }
        validateValue(val.toString());
        return "'"+val.toString()+"'";
    }

    private String camelToUnderline(String value){
        StringBuilder sb = new StringBuilder(value.length());
        for (int i = 0; i < value.length(); i++) {
            char c = value.charAt(i);
            if (Character.isUpperCase(c)) {
                sb.append("_");
            }
            sb.append(Character.toLowerCase(c));
        }
        return sb.toString();
    }

    private String underlineToCamel(String value){
        StringBuilder sb = new StringBuilder(value.length());
        for (int i = 0; i < value.length(); i++) {
            char c = value.charAt(i);
            if ('_' == c) {
                if (++i < value.length()){
                    sb.append(Character.toUpperCase(value.charAt(i)));
                }
            }else {
                sb.append(c);
            }
        }
        return sb.toString();
    }

    public void save(SaveReq saveReq) {
        StringBuilder sql = new StringBuilder("insert into ").append(buildTable(saveReq.getTable())).append("(");
        sql.append(saveReq.getSaveObj().keySet().stream().map(this::camelToUnderline).collect(Collectors.joining(",")));
        sql.append(") values (");
        sql.append(saveReq.getSaveObj().keySet().stream().map(item->{
            validateTableMeta(item);
            return buildStrValue(saveReq.getSaveObj().get(item));
        }).collect(Collectors.joining(",")));
        sql.append(")");
        log.debug("generator sql:{}",sql.toString());
        try {
            commonMapper.save(sql.toString());
        }catch (Exception e){
            throw new BizException(ErrorCode.SERVER_ERROR.getCode(),e.getCause().getMessage());
        }
    }

    public void update(UpdateReq updateReq) {
        StringBuilder sql = new StringBuilder("update ").append(buildTable(updateReq.getTable())).append(" set ");

        boolean isFirst = true;
        for(String key : updateReq.getUpdateSet().keySet()){

            if (!isFirst){
                sql.append(",");
            }
            isFirst = false;

            sql.append(buildFieldKey(key)).append("=").append(buildStrValue(updateReq.getUpdateSet().get(key)));
        }

        String filter = buildFilter("$and",updateReq.getFilter());
        sql.append(" where ").append(filter);
        log.debug(sql.toString());
        try {
            commonMapper.update(sql.toString());
        }catch (Exception e){
            throw new BizException(ErrorCode.SERVER_ERROR.getCode(),e.getCause().getMessage());
        }
    }

    public void delete(UpdateReq updateReq) {
        StringBuilder sql = new StringBuilder("delete from ").append(buildTable(updateReq.getTable()));

        String filter = buildFilter("$and",updateReq.getFilter());
        sql.append(" where ").append(filter);
        log.debug(sql.toString());
        try {
            commonMapper.delete(sql.toString());
        }catch (Exception e){
            throw new BizException(ErrorCode.SERVER_ERROR.getCode(),e.getCause().getMessage());
        }
    }
}
