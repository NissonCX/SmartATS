package com.smartats.common.result;

import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;

/**
 * 统一响应结果包装器
 *
 * @Data 注解：自动生成 getter/setter/toString/equals
 * @NoArgsConstructor: 自动生成无参构造
 * @AllArgsConstructor: 自动生成全参构造
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
public class Result<T> {

    /**
     * 响应码（200=成功，其他=错误码）
     */
    private int code;

    /**
     * 响应消息
     */
    private String message;

    /**
     * 响应数据（成功时有数据，失败时为 null）
     */
    private T data;

    /**
     * 时间戳
     */
    private Long timestamp;

    // ==================== 快速构建方法

    /**
     * 成功响应（有数据）
     */
    public static <T> Result<T> success(T data) {
        return new Result<>(200, "success", data, System.currentTimeMillis());
    }

    /**
     * 成功响应（无数据）
     */
    public static <T> Result<T> success() {
        return success(null);
    }

    /**
     * 失败响应（使用错误码枚举）
     */
    public static <T> Result<T> error(ResultCode resultCode) {
        return new Result<>(resultCode.getCode(), resultCode.getMessage(), null, System.currentTimeMillis());
    }

    /**
     * 失败响应（自定义错误）
     */
    public static <T> Result<T> error(int code, String message) {
        return new Result<>(code, message, null, System.currentTimeMillis());
    }
}