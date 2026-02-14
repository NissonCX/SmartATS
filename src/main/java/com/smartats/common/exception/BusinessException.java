package com.smartats.common.exception;

import com.smartats.common.result.ResultCode;
import lombok.Getter;

/**
 * 业务异常（已知错误，可恢复）
 * <p>
 * 用法：
 * throw new
 * BusinessException(ResultCode.USER_NOT_FOUND);
 * <p>
 * 与系统异常（RuntimeException）的区别：
 * - 业务异常：预计内，可处理（用户不存在、密码错误）
 * - 系统异常：预料外，不可恢复（数据库宕机、NPE）
 */
@Getter
public class BusinessException extends RuntimeException {

    /**
     * 错误码
     */
    private final int code;

    /**
     * 错误消息
     */
    private final String message;

    /**
     * 构造函数（使用错误码枚举）
     */
    public BusinessException(ResultCode resultCode) {
        super(resultCode.getMessage());
        this.code = resultCode.getCode();
        this.message = resultCode.getMessage();
    }

    /**
     * 构造函数（自定义消息）
     */
    public BusinessException(int code, String message) {
        super(message);
        this.code = code;
        this.message = message;
    }
}