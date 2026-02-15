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
     * 构造函数 1：使用错误码枚举（使用默认消息）
     * <p>
     * 用法：throw new BusinessException(ResultCode.USER_NOT_FOUND)
     */
    public BusinessException(ResultCode resultCode) {
        super(resultCode.getMessage());
        this.code = resultCode.getCode();
        this.message = resultCode.getMessage();
    }

    /**
     * 构造函数 2：使用错误码枚举 + 自定义消息
     * <p>
     * 用法：throw new BusinessException(ResultCode.INVALID_CREDENTIALS, "用户名或密码错误")
     * <p>
     * 场景：使用 ResultCode 的错误码，但需要自定义返回消息（如统一错误提示）
     */
    public BusinessException(ResultCode resultCode, String customMessage) {
        super(customMessage);
        this.code = resultCode.getCode();
        this.message = customMessage;
    }

    /**
     * 构造函数 3：完全自定义错误码和消息
     * <p>
     * 用法：throw new BusinessException(50001, "系统内部错误")
     * <p>
     * 场景：需要临时定义错误码，不在 ResultCode 枚举中
     */
    public BusinessException(int code, String message) {
        super(message);
        this.code = code;
        this.message = message;
    }
}