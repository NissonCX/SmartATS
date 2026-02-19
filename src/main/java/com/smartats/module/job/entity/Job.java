package com.smartats.module.job.entity;

import com.baomidou.mybatisplus.annotation.*;
import lombok.Data;

import java.time.LocalDateTime;

@Data
@TableName("jobs")
public class Job {
    @TableId(value = "id", type = IdType.AUTO)
    private Long id;

    private String title;

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
     * 必需技能标签 (JSON 格式)
     */
    private String requiredSkills;

    /**
     * 薪资下限(K)
     */
    private Integer salaryMin;

    /**
     * 薪资上限(K)
     */
    private Integer salaryMax;

    /**
     * 最低经验年限
     */
    private Integer experienceMin;

    /**
     * 最高经验年限
     */
    private Integer experienceMax;

    /**
     * 学历要求
     */
    private String education;

    /**
     * 职位类型
     */
    private String jobType;

    /**
     * 状态：DRAFT-草稿, PUBLISHED-已发布, CLOSED-已关闭
     */
    private String status;

    /**
     * 创建者ID
     */
    private Long creatorId;

    /**
     * 浏览次数
     */
    private Integer viewCount;

    /**
     * 逻辑删除标记
     */
    @TableLogic
    private Integer deleted;

    /**
     * 创建时间
     */
    @TableField(fill = FieldFill.INSERT)
    private LocalDateTime createdAt;

    /**
     * 更新时间
     */
    @TableField(fill = FieldFill.INSERT_UPDATE)
    private LocalDateTime updatedAt;
}
