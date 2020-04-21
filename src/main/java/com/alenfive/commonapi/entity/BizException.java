package com.alenfive.commonapi.entity;


public class BizException extends RuntimeException {
    private static final long serialVersionUID = 4941120614090564281L;

    /**
     * 业务异常错误码
     */
    private String code;
    /**
     * 错误描述
     */
    private String message;

    public BizException(String code, String message) {
        super(message);
        this.code = code;
        this.message = message;
    }

    public BizException(String code, String message, Throwable cause) {
        super(message, cause);
        this.code = code;
        this.message = message;
    }

    public String getCode() {
        return code;
    }

    @Override
    public String getMessage() {
        return message;
    }

    @Override
    public String toString() {
        return "BizException{" +
                "code='" + code + '\'' +
                ", message='" + message + '\'' +
                '}';
    }
}
