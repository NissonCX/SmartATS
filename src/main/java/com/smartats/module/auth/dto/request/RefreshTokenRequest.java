package com.smartats.module.auth.dto.request;

import jakarta.validation.constraints.NotBlank;
import lombok.Data;

/**
 * 刷新 Token 请求
 */
@Data
public class RefreshTokenRequest {

    /**
     * 刷新令牌（登录接口返回的 refreshToken）
     */
    @NotBlank(message = "refreshToken 不能为空")
    private String refreshToken;
}
