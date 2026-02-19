package com.smartats.module.job.dto.response;

import com.fasterxml.jackson.annotation.JsonFormat;
import lombok.Data;

import java.time.LocalDateTime;
import java.util.List;

/**
 * 职位响应
 */
@Data
public class JobResponse {

    /**
     * 职位ID
     */
    private Long id;

    /**
     * 职位标题
     */
    private String title;

    /**
     * 部门
     */
    private String department;

    /**
     * 职位描述
     */
    private String description;

    /**
     * 任职要求
     */
    private String requirements;

    /**
     * 必需技能标签
     */
    private List<String> requiredSkills;

    /**
     * 薪资下限(K)
     */
    private Integer salaryMin;

    /**
     * 薪资上限(K)
     */
    private Integer salaryMax;

    /**
     * 薪资范围（格式化显示）
     */
    private String salaryRange;

    /**
     * 最低经验年限
     */
    private Integer experienceMin;

    /**
     * 最高经验年限
     */
    private Integer experienceMax;

    /**
     * 经验要求（格式化显示）
     */
    private String experienceRange;

    /**
     * 学历要求
     */
    private String education;

    /**
     * 职位类型
     */
    private String jobType;

    /**
     * 状态
     */
    private String status;

    /**
     * 状态描述
     */
    private String statusDesc;

    /**
     * 创建者ID
     */
    private Long creatorId;

    /**
     * 浏览次数
     */
    private Integer viewCount;

    /**
     * 创建时间
     */
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private LocalDateTime createdAt;

    /**
     * 更新时间
     */
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private LocalDateTime updatedAt;
}