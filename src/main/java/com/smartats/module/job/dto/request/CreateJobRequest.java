package com.smartats.module.job.dto.request;

import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

import java.util.List;

@Data
public class CreateJobRequest {
    /**
     * 职位标题
     */
    @NotBlank(message = "职位标题不能为空")
    private String title;

    /**
     * 部门
     */
    private String department;

    /**
     * 职位描述
     */
    @NotBlank(message = "职位描述不能为空")
    private String description;

    /**
     * 任职要求
     */
    @NotBlank(message = "任职要求不能为空")
    private String requirements;

    /**
     * 必需技能标签
     */
    private List<String> requiredSkills;

    /**
     * 薪资下限(K)
     */
    @NotNull(message = "薪资下限不能为空")
    @Min(value = 0, message = "薪资下限不能小于0")
    private Integer salaryMin;

    /**
     * 薪资上限(K)
     */
    @NotNull(message = "薪资上限不能为空")
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
