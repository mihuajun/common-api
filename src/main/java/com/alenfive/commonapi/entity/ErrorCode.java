package com.alenfive.commonapi.entity;


public enum ErrorCode {

    SUCCESS(200) //正常
    , SERVER_ERROR(10002)//服务器异常
    , PARAM_ERROR(10001)//参数问题
    , FORMAT_ERROR(10011) //格式化异常
    ;
    private Integer code;

    ErrorCode(Integer code) {
        this.code = code;
    }

    public String getCode() {
        return String.valueOf(code);
    }
}
