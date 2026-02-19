package com.smartats.infrastructure.email;

import jakarta.mail.MessagingException;
import jakarta.mail.internet.MimeMessage;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.mail.javamail.MimeMessageHelper;
import org.springframework.stereotype.Service;

/**
 * 邮件发送服务
 * <p>
 * 功能：
 * 1. 发送验证码邮件
 * 2. 支持纯文本邮件
 * 3. 后续可扩展支持 HTML 邮件
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class EmailService {

    private final JavaMailSender mailSender;

    @Value("${spring.mail.username}")
    private String fromEmail;

    @Value("${app.verification-code.expire-time:300}")
    private Integer expireTimeSeconds;

    /**
     * 发送验证码邮件
     *
     * @param to   收件人邮箱
     * @param code 验证码
     * @return 是否发送成功
     */
    public boolean sendVerificationCode(String to, String code) {
        try {
            log.info("发送验证码邮件：to={}, code={}", to, code);

            // 创建邮件消息
            MimeMessage message = mailSender.createMimeMessage();
            MimeMessageHelper helper = new MimeMessageHelper(message, true);

            // 设置发件人
            helper.setFrom(fromEmail);

            // 设置收件人
            helper.setTo(to);

            // 设置主题
            helper.setSubject("【SmartATS】邮箱验证码");

            // 设置邮件内容
            String content = buildEmailContent(code);
            helper.setText(content, true);  // true = HTML 格式

            // 发送邮件
            mailSender.send(message);

            log.info("验证码邮件发送成功：to={}", to);
            return true;

        } catch (MessagingException e) {
            log.error("验证码邮件发送失败：to={}, error={}", to, e.getMessage());
            return false;
        }
    }

    /**
     * 构建邮件内容（Minimalism Tech 风格，中文版）
     * 设计系统：
     * - 风格: Minimalism（最小主义）+ Modern Tech
     * - 字体: 思源黑体/Noto Sans SC（中文友好）+ 系统默认字体
     * - 配色: 深蓝 (#0F172A) + 科技蓝 (#3B82F6) + 成功绿 (#22C55E)
     * - 特点: 大量留白、清晰层次、高对比度、无多余装饰
     *
     * @param code 验证码
     * @return HTML 格式的邮件内容
     */
    private String buildEmailContent(String code) {
        int expireMinutes = expireTimeSeconds / 60;

        return """
            <!DOCTYPE html>
            <html lang="zh-CN">
            <head>
                <meta charset="UTF-8">
                <meta name="viewport" content="width=device-width, initial-scale=1.0">
                <title>邮箱验证 - SmartATS</title>
                <style>
                    /* ========== Reset & Base ========== */
                    *, *::before, *::after {
                        margin: 0;
                        padding: 0;
                        box-sizing: border-box;
                    }

                    body {
                        font-family: -apple-system, BlinkMacSystemFont, "Segoe UI", "PingFang SC", "Hiragino Sans GB", "Microsoft YaHei", "Helvetica Neue", Helvetica, Arial, sans-serif;
                        line-height: 1.6;
                        color: #1E293B;
                        background: #F8FAFC;
                        padding: 24px;
                        min-height: 100vh;
                        -webkit-font-smoothing: antialiased;
                        -moz-osx-font-smoothing: grayscale;
                    }

                    /* ========== Main Container ========== */
                    .email-wrapper {
                        max-width: 600px;
                        margin: 0 auto;
                    }

                    .email-container {
                        background: #FFFFFF;
                        border-radius: 16px;
                        border: 1px solid #E2E8F0;
                        box-shadow: 0 1px 3px rgba(0, 0, 0, 0.05);
                        overflow: hidden;
                    }

                    /* ========== Header Section ========== */
                    .header {
                        padding: 48px 40px 32px;
                        text-align: center;
                        border-bottom: 1px solid #F1F5F9;
                    }

                    .logo {
                        font-size: 32px;
                        font-weight: 700;
                        color: #0F172A;
                        letter-spacing: -0.03em;
                        margin-bottom: 8px;
                    }

                    .tagline {
                        font-size: 14px;
                        font-weight: 500;
                        color: #64748B;
                        letter-spacing: 0.02em;
                    }

                    /* ========== Content Section ========== */
                    .content {
                        padding: 40px;
                    }

                    .greeting {
                        font-size: 22px;
                        font-weight: 600;
                        color: #0F172A;
                        margin-bottom: 16px;
                    }

                    .message {
                        font-size: 15px;
                        color: #475569;
                        line-height: 1.7;
                        margin-bottom: 32px;
                    }

                    /* ========== Verification Code Card ========== */
                    .code-card {
                        background: #F8FAFC;
                        border: 1px solid #E2E8F0;
                        border-radius: 12px;
                        padding: 32px;
                        margin: 24px 0;
                        text-align: center;
                    }

                    .code-label {
                        font-size: 12px;
                        font-weight: 700;
                        color: #64748B;
                        letter-spacing: 0.1em;
                        margin-bottom: 16px;
                    }

                    .verification-code {
                        font-size: 40px;
                        font-weight: 700;
                        color: #0F172A;
                        letter-spacing: 0.1em;
                        margin: 16px 0;
                        user-select: all;
                    }

                    .code-expire {
                        font-size: 13px;
                        color: #64748B;
                        margin-top: 16px;
                    }

                    .code-expire strong {
                        color: #0F172A;
                        font-weight: 700;
                    }

                    /* ========== Info Box ========== */
                    .info-box {
                        background: #EFF6FF;
                        border-left: 3px solid #3B82F6;
                        padding: 16px 20px;
                        margin: 24px 0;
                        border-radius: 0 8px 8px 0;
                    }

                    .info-box-title {
                        font-weight: 700;
                        color: #1E40AF;
                        font-size: 13px;
                        margin-bottom: 4px;
                    }

                    .info-box-content {
                        font-size: 14px;
                        color: #1E40AF;
                        line-height: 1.6;
                    }

                    /* ========== Security Notice ========== */
                    .security-notice {
                        font-size: 13px;
                        color: #94A3B8;
                        text-align: center;
                        margin: 32px 0;
                        padding: 16px;
                    }

                    /* ========== Divider ========== */
                    .divider {
                        height: 1px;
                        background: #F1F5F9;
                        margin: 0;
                    }

                    /* ========== Footer ========== */
                    .footer {
                        padding: 32px 40px;
                        text-align: center;
                    }

                    .footer-text {
                        font-size: 13px;
                        color: #64748B;
                        line-height: 1.6;
                        margin: 8px 0;
                    }

                    .footer-links {
                        margin: 16px 0;
                    }

                    .footer-link {
                        color: #3B82F6;
                        text-decoration: none;
                        font-size: 13px;
                        font-weight: 500;
                        margin: 0 8px;
                    }

                    .footer-link:hover {
                        text-decoration: underline;
                    }

                    .copyright {
                        font-size: 12px;
                        color: #94A3B8;
                        margin-top: 16px;
                    }

                    /* ========== Responsive Design ========== */
                    @media only screen and (max-width: 640px) {
                        body {
                            padding: 16px;
                        }

                        .header, .content, .footer {
                            padding: 32px 24px;
                        }

                        .logo {
                            font-size: 28px;
                        }

                        .greeting {
                            font-size: 20px;
                        }

                        .verification-code {
                            font-size: 32px;
                            letter-spacing: 0.05em;
                        }

                        .code-card {
                            padding: 24px 20px;
                        }
                    }
                </style>
            </head>
            <body>
                <div class="email-wrapper">
                    <div class="email-container">
                        <!-- Header -->
                        <div class="header">
                            <div class="logo">SmartATS</div>
                            <div class="tagline">智能招聘管理平台</div>
                        </div>

                        <!-- Content -->
                        <div class="content">
                            <div class="greeting">您好！</div>

                            <p class="message">
                                感谢您注册 SmartATS。请使用下方的验证码完成注册流程，激活您的账户。
                            </p>

                            <!-- Verification Code Card -->
                            <div class="code-card">
                                <div class="code-label">邮箱验证码</div>
                                <div class="verification-code">%s</div>
                                <div class="code-expire">
                                    验证码有效期为 <strong>%d 分钟</strong>
                                </div>
                            </div>

                            <!-- Info Box -->
                            <div class="info-box">
                                <div class="info-box-title">下一步</div>
                                <div class="info-box-content">
                                    在注册页面输入此验证码即可激活账户，开始使用 SmartATS 智能招聘服务。
                                </div>
                            </div>

                            <!-- Security Notice -->
                            <div class="security-notice">
                                如果您没有请求此验证码，请忽略此邮件。
                            </div>
                        </div>

                        <!-- Divider -->
                        <div class="divider"></div>

                        <!-- Footer -->
                        <div class="footer">
                            <p class="footer-text">
                                此邮件由 SmartATS 系统自动发送，请勿直接回复。<br>
                                如需帮助，请联系客服团队。
                            </p>

                            <div class="footer-links">
                                <a href="https://smartats.com/privacy" class="footer-link">隐私政策</a>
                                <span style="color: #94A3B8">·</span>
                                <a href="https://smartats.com/terms" class="footer-link">服务条款</a>
                                <span style="color: #94A3B8">·</span>
                                <a href="https://smartats.com/support" class="footer-link">联系客服</a>
                            </div>

                            <p class="copyright">
                                &copy; 2026 SmartATS. 保留所有权利。<br>
                                构建智能招聘的未来
                            </p>
                        </div>
                    </div>
                </div>
            </body>
            </html>
            """.formatted(code, expireMinutes);
    }
}
