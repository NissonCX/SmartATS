package com.smartats.module.job.dto.request;

import lombok.Data;

@Data
public class JobQueryRequest {
    /**
     * 关键词搜索（标题、描述、要求）
     */
    private String keyword;

    /**
     * 部门
     */
    private String department;

    /**
     * 职位类型
     */
    private String jobType;

    /**
     * 学历要求
     */
    private String education;

    /**
     * 最低经验要求
     */
    private Integer experienceMin;

    /**
     * 薪资下限
     */
    private Integer salaryMin;

    /**
     * 职位状态
     */
    private String status;

    /**
     * 页码（从1开始）
     */
    private Integer pageNum = 1;

    /**
     * 每页大小
     */
    private Integer pageSize = 10;

    /**
     * 排序字段：created_at, salary_max, view_count
     */
    private String orderBy = "created_at";

    /**
     * 排序方向：asc, desc
     */
    private String orderDirection = "desc";
}
