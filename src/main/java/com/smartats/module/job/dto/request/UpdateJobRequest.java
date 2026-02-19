package com.smartats.module.job.dto.request;

import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

/**
 * 更新职位请求
 */
@Data
public class UpdateJobRequest {

    /**
     * 职位ID
     */
    @NotNull(message = "职位ID不能为空")
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
    private java.util.List<String> requiredSkills;

    /**
     * 薪资下限(K)
     * 如果提供了此字段，必须 >= 0
     */
    @Min(value = 0, message = "薪资下限不能小于0")
    private Integer salaryMin;

    /**
     * 薪资上限(K)
     * 如果提供了此字段，必须 >= 0
     */
    @Min(value = 0, message = "薪资上限不能小于0")
    private Integer salaryMax;

    /**
     * 最低经验年限
     */
    @Min(value = 0, message = "经验年限不能小于0")
    private Integer experienceMin;

    /**
     * 最高经验年限
     */
    @Min(value = 0, message = "经验年限不能小于0")
    private Integer experienceMax;

    /**
     * 学历要求
     */
    private String education;

    /**
     * 职位类型
     */
    private String jobType;
}