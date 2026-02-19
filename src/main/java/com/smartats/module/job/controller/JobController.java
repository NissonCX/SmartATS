package com.smartats.module.job.controller;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.smartats.common.result.Result;
import com.smartats.module.job.dto.request.CreateJobRequest;
import com.smartats.module.job.dto.request.JobQueryRequest;
import com.smartats.module.job.dto.request.UpdateJobRequest;
import com.smartats.module.job.dto.response.JobResponse;
import com.smartats.module.job.service.JobService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * 职位管理控制器
 */
@RestController
@RequestMapping("/jobs")
@RequiredArgsConstructor
public class JobController {

    private final JobService jobService;

    /**
     * 创建职位
     * POST /api/v1/jobs
     */
    @PostMapping
    public Result<Long> createJob(@Valid @RequestBody CreateJobRequest request, Authentication authentication) {
        Long creatorId = (Long) authentication.getPrincipal();
        Long jobId = jobService.createJob(request, creatorId);
        return Result.success(jobId);
    }

    /**
     * 更新职位
     * PUT /api/v1/jobs
     */
    @PutMapping
    public Result<Void> updateJob(@Valid @RequestBody UpdateJobRequest request, Authentication authentication) {
        Long operatorId = (Long) authentication.getPrincipal();
        jobService.updateJob(request, operatorId);
        return Result.success();
    }

    /**
     * 获取职位详情
     * GET /api/v1/jobs/{id}
     */
    @GetMapping("/{id}")
    public Result<JobResponse> getJobDetail(@PathVariable Long id) {
        JobResponse response = jobService.getJobDetail(id);
        return Result.success(response);
    }

    /**
     * 职位列表
     * GET /api/v1/jobs
     */
    @GetMapping
    public Result<Page<JobResponse>> getJobList(JobQueryRequest request) {
        Page<JobResponse> page = jobService.getJobList(request);
        return Result.success(page);
    }

    /**
     * 发布职位
     * POST /api/v1/jobs/{id}/publish
     */
    @PostMapping("/{id}/publish")
    public Result<Void> publishJob(@PathVariable Long id, Authentication authentication) {
        Long operatorId = (Long) authentication.getPrincipal();
        jobService.publishJob(id, operatorId);
        return Result.success();
    }

    /**
     * 关闭职位
     * POST /api/v1/jobs/{id}/close
     */
    @PostMapping("/{id}/close")
    public Result<Void> closeJob(@PathVariable Long id, Authentication authentication) {
        Long operatorId = (Long) authentication.getPrincipal();
        jobService.closeJob(id, operatorId);
        return Result.success();
    }

    /**
     * 删除职位
     * DELETE /api/v1/jobs/{id}
     */
    @DeleteMapping("/{id}")
    public Result<Void> deleteJob(@PathVariable Long id, Authentication authentication) {
        Long operatorId = (Long) authentication.getPrincipal();
        jobService.deleteJob(id, operatorId);
        return Result.success();
    }

    /**
     * 热门职位
     * GET /api/v1/jobs/hot
     */
    @GetMapping("/hot")
    public Result<List<JobResponse>> getHotJobs(@RequestParam(defaultValue = "10") Integer limit) {
        List<JobResponse> jobs = jobService.getHotJobs(limit);
        return Result.success(jobs);
    }
}