package com.smartats.module.job.service;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.smartats.common.constants.RedisKeyConstants;
import com.smartats.common.exception.BusinessException;
import com.smartats.common.result.ResultCode;
import com.smartats.module.job.dto.request.CreateJobRequest;
import com.smartats.module.job.dto.request.JobQueryRequest;
import com.smartats.module.job.dto.request.UpdateJobRequest;
import com.smartats.module.job.dto.response.JobResponse;
import com.smartats.module.job.entity.Job;
import com.smartats.module.job.mapper.JobMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.BeanUtils;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;

import java.util.Arrays;
import java.util.List;
import java.util.concurrent.TimeUnit;
import java.util.stream.Collectors;

/**
 * 职位服务
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class JobService {

    private final JobMapper jobMapper;
    private final StringRedisTemplate redisTemplate;
    private final ObjectMapper objectMapper;

    /**
     * 创建职位
     */
    @Transactional(rollbackFor = Exception.class)
    public Long createJob(CreateJobRequest request, Long creatorId) {
        log.info("创建职位：title={}, creatorId={}", request.getTitle(), creatorId);

        // ━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━
        // 第 1 步：创建职位实体
        // ━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━

        Job job = new Job();
        BeanUtils.copyProperties(request, job);

        // 设置创建者
        job.setCreatorId(creatorId);

        // 设置默认状态为草稿
        job.setStatus("DRAFT");

        // 设置默认浏览次数
        job.setViewCount(0);

        // 处理技能标签（List -> JSON）
        if (request.getRequiredSkills() != null && !request.getRequiredSkills().isEmpty()) {
            try {
                job.setRequiredSkills(objectMapper.writeValueAsString(request.getRequiredSkills()));
            } catch (JsonProcessingException e) {
                log.error("技能标签序列化失败", e);
                throw new BusinessException(ResultCode.INTERNAL_ERROR, "技能标签格式错误");
            }
        }

        // ━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━
        // 第 2 步：保存到数据库
        // ━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━

        int result = jobMapper.insert(job);
        if (result <= 0) {
            log.error("职位创建失败：title={}", request.getTitle());
            throw new BusinessException(ResultCode.INTERNAL_ERROR, "职位创建失败");
        }

        log.info("职位创建成功：id={}, title={}", job.getId(), job.getTitle());
        return job.getId();
    }

    /**
     * 更新职位
     */
    @Transactional(rollbackFor = Exception.class)
    public void updateJob(UpdateJobRequest request, Long creatorId) {
        log.info("更新职位：id={}, operatorId={}", request.getId(), creatorId);

        // ━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━
        // 第 1 步：查询职位是否存在
        // ━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━

        Job job = jobMapper.selectById(request.getId());
        if (job == null) {
            log.warn("职位不存在：id={}", request.getId());
            throw new BusinessException(ResultCode.NOT_FOUND, "职位不存在");
        }

        // ━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━
        // 第 2 步：检查权限（只有创建者可以修改）
        // ━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━

        if (!job.getCreatorId().equals(creatorId)) {
            log.warn("无权限修改职位：jobId={}, creatorId={}, operatorId={}",
                    request.getId(), job.getCreatorId(), creatorId);
            throw new BusinessException(ResultCode.FORBIDDEN, "无权限修改此职位");
        }

        // ━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━
        // 第 3 步：更新职位信息
        // ━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━

        if (StringUtils.hasText(request.getTitle())) {
            job.setTitle(request.getTitle());
        }
        if (StringUtils.hasText(request.getDepartment())) {
            job.setDepartment(request.getDepartment());
        }
        if (StringUtils.hasText(request.getDescription())) {
            job.setDescription(request.getDescription());
        }
        if (StringUtils.hasText(request.getRequirements())) {
            job.setRequirements(request.getRequirements());
        }
        if (request.getRequiredSkills() != null) {
            try {
                job.setRequiredSkills(objectMapper.writeValueAsString(request.getRequiredSkills()));
            } catch (JsonProcessingException e) {
                log.error("技能标签序列化失败", e);
                throw new BusinessException(ResultCode.INTERNAL_ERROR, "技能标签格式错误");
            }
        }
        if (request.getSalaryMin() != null) {
            job.setSalaryMin(request.getSalaryMin());
        }
        if (request.getSalaryMax() != null) {
            job.setSalaryMax(request.getSalaryMax());
        }
        if (request.getExperienceMin() != null) {
            job.setExperienceMin(request.getExperienceMin());
        }
        if (request.getExperienceMax() != null) {
            job.setExperienceMax(request.getExperienceMax());
        }
        if (request.getEducation() != null) {
            job.setEducation(request.getEducation());
        }
        if (request.getJobType() != null) {
            job.setJobType(request.getJobType());
        }

        // ━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━
        // 第 4 步：保存到数据库
        // ━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━

        int result = jobMapper.updateById(job);
        if (result <= 0) {
            log.error("职位更新失败：id={}", request.getId());
            throw new BusinessException(ResultCode.INTERNAL_ERROR, "职位更新失败");
        }

        // ━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━
        // 第 5 步：删除 Redis 缓存
        // ━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━

        String cacheKey = RedisKeyConstants.JWT_TOKEN_KEY_PREFIX.replace("jwt:token:", "cache:job:") + job.getId();
        redisTemplate.delete(cacheKey);

        log.info("职位更新成功：id={}", job.getId());
    }

    /**
     * 获取职位详情
     */
    public JobResponse getJobDetail(Long id) {
        log.info("查询职位详情：id={}", id);

        // ━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━
        // 第 1 步：先查 Redis 缓存
        // ━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━

        String cacheKey = "cache:job:" + id;
        String cachedData = redisTemplate.opsForValue().get(cacheKey);

        if (cachedData != null) {
            try {
                return objectMapper.readValue(cachedData, JobResponse.class);
            } catch (JsonProcessingException e) {
                log.error("缓存数据反序列化失败", e);
            }
        }

        // ━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━
        // 第 2 步：缓存未命中，查询数据库
        // ━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━

        Job job = jobMapper.selectById(id);
        if (job == null) {
            log.warn("职位不存在：id={}", id);
            throw new BusinessException(ResultCode.NOT_FOUND, "职位不存在");
        }

        // ━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━
        // 第 3 步：转换为响应对象
        // ━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━

        JobResponse response = convertToResponse(job);

        // ━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━
        // 第 4 步：回填缓存（TTL 30分钟）
        // ━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━

        try {
            redisTemplate.opsForValue().set(cacheKey, objectMapper.writeValueAsString(response), 30, TimeUnit.MINUTES);
        } catch (JsonProcessingException e) {
            log.error("缓存数据序列化失败", e);
        }

        // ━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━
        // 第 5 步：增加浏览次数（异步更新，不影响响应速度）
        // ━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━

        job.setViewCount(job.getViewCount() + 1);
        jobMapper.updateById(job);

        return response;
    }

    /**
     * 职位列表（分页、筛选）
     */
    public Page<JobResponse> getJobList(JobQueryRequest request) {
        log.info("查询职位列表：{}", request);

        // ━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━
        // 第 1 步：构建查询条件
        // ━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━

        LambdaQueryWrapper<Job> queryWrapper = new LambdaQueryWrapper<>();

        // 关键词搜索（标题、描述、要求）
        if (StringUtils.hasText(request.getKeyword())) {
            queryWrapper.and(wrapper -> wrapper
                    .like(Job::getTitle, request.getKeyword())
                    .or()
                    .like(Job::getDescription, request.getKeyword())
                    .or()
                    .like(Job::getRequirements, request.getKeyword())
            );
        }

        // 部门筛选
        if (StringUtils.hasText(request.getDepartment())) {
            queryWrapper.eq(Job::getDepartment, request.getDepartment());
        }

        // 职位类型筛选
        if (StringUtils.hasText(request.getJobType())) {
            queryWrapper.eq(Job::getJobType, request.getJobType());
        }

        // 学历筛选
        if (StringUtils.hasText(request.getEducation())) {
            queryWrapper.eq(Job::getEducation, request.getEducation());
        }

        // 经验筛选（要求 >= 最低经验）
        if (request.getExperienceMin() != null) {
            queryWrapper.le(Job::getExperienceMin, request.getExperienceMin());
        }

        // 薪资筛选（薪资下限 >= 要求）
        if (request.getSalaryMin() != null) {
            queryWrapper.ge(Job::getSalaryMin, request.getSalaryMin());
        }

        // 状态筛选（默认只显示已发布）
        if (StringUtils.hasText(request.getStatus())) {
            queryWrapper.eq(Job::getStatus, request.getStatus());
        } else {
            queryWrapper.eq(Job::getStatus, "PUBLISHED");
        }

        // ━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━
        // 第 2 步：排序
        // ━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━

        if ("created_at".equals(request.getOrderBy())) {
            if ("asc".equals(request.getOrderDirection())) {
                queryWrapper.orderByAsc(Job::getCreatedAt);
            } else {
                queryWrapper.orderByDesc(Job::getCreatedAt);
            }
        } else if ("salary_max".equals(request.getOrderBy())) {
            if ("asc".equals(request.getOrderDirection())) {
                queryWrapper.orderByAsc(Job::getSalaryMax);
            } else {
                queryWrapper.orderByDesc(Job::getSalaryMax);
            }
        } else if ("view_count".equals(request.getOrderBy())) {
            if ("asc".equals(request.getOrderDirection())) {
                queryWrapper.orderByAsc(Job::getViewCount);
            } else {
                queryWrapper.orderByDesc(Job::getViewCount);
            }
        }

        // ━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━
        // 第 3 步：分页查询
        // ━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━

        Page<Job> page = new Page<>(request.getPageNum(), request.getPageSize());
        Page<Job> jobPage = jobMapper.selectPage(page, queryWrapper);

        // ━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━
        // 第 4 步：转换为响应对象
        // ━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━

        Page<JobResponse> responsePage = new Page<>(jobPage.getCurrent(), jobPage.getSize(), jobPage.getTotal());
        List<JobResponse> responseList = jobPage.getRecords().stream()
                .map(this::convertToResponse)
                .collect(Collectors.toList());

        responsePage.setRecords(responseList);

        return responsePage;
    }

    /**
     * 发布职位
     */
    @Transactional(rollbackFor = Exception.class)
    public void publishJob(Long id, Long operatorId) {
        log.info("发布职位：id={}, operatorId={}", id, operatorId);

        Job job = jobMapper.selectById(id);
        if (job == null) {
            throw new BusinessException(ResultCode.NOT_FOUND, "职位不存在");
        }

        if (!job.getCreatorId().equals(operatorId)) {
            throw new BusinessException(ResultCode.FORBIDDEN, "无权限操作此职位");
        }

        if ("PUBLISHED".equals(job.getStatus())) {
            throw new BusinessException(ResultCode.BAD_REQUEST, "职位已经是发布状态");
        }

        job.setStatus("PUBLISHED");
        jobMapper.updateById(job);

        // 清除缓存
        redisTemplate.delete("cache:job:" + id);

        log.info("职位发布成功：id={}", id);
    }

    /**
     * 关闭职位
     */
    @Transactional(rollbackFor = Exception.class)
    public void closeJob(Long id, Long operatorId) {
        log.info("关闭职位：id={}, operatorId={}", id, operatorId);

        Job job = jobMapper.selectById(id);
        if (job == null) {
            throw new BusinessException(ResultCode.NOT_FOUND, "职位不存在");
        }

        if (!job.getCreatorId().equals(operatorId)) {
            throw new BusinessException(ResultCode.FORBIDDEN, "无权限操作此职位");
        }

        if ("CLOSED".equals(job.getStatus())) {
            throw new BusinessException(ResultCode.BAD_REQUEST, "职位已经是关闭状态");
        }

        job.setStatus("CLOSED");
        jobMapper.updateById(job);

        // 清除缓存
        redisTemplate.delete("cache:job:" + id);

        log.info("职位关闭成功：id={}", id);
    }

    /**
     * 删除职位（软删除）
     */
    @Transactional(rollbackFor = Exception.class)
    public void deleteJob(Long id, Long operatorId) {
        log.info("删除职位：id={}, operatorId={}", id, operatorId);

        Job job = jobMapper.selectById(id);
        if (job == null) {
            throw new BusinessException(ResultCode.NOT_FOUND, "职位不存在");
        }

        if (!job.getCreatorId().equals(operatorId)) {
            throw new BusinessException(ResultCode.FORBIDDEN, "无权限操作此职位");
        }

        // MyBatis-Plus 逻辑删除
        int result = jobMapper.deleteById(id);
        if (result <= 0) {
            throw new BusinessException(ResultCode.INTERNAL_ERROR, "职位删除失败");
        }

        // 清除缓存
        redisTemplate.delete("cache:job:" + id);

        log.info("职位删除成功：id={}", id);
    }

    /**
     * 热门职位列表
     */
    public List<JobResponse> getHotJobs(Integer limit) {
        log.info("查询热门职位：limit={}", limit);

        // 使用浏览次数排序
        LambdaQueryWrapper<Job> queryWrapper = new LambdaQueryWrapper<>();
        queryWrapper.eq(Job::getStatus, "PUBLISHED")
                .orderByDesc(Job::getViewCount)
                .last("LIMIT " + (limit != null ? limit : 10));

        List<Job> jobs = jobMapper.selectList(queryWrapper);
        return jobs.stream()
                .map(this::convertToResponse)
                .collect(Collectors.toList());
    }

    /**
     * 转换为响应对象
     */
    private JobResponse convertToResponse(Job job) {
        JobResponse response = new JobResponse();
        BeanUtils.copyProperties(job, response);

        // 处理技能标签（JSON -> List）
        if (StringUtils.hasText(job.getRequiredSkills())) {
            try {
                response.setRequiredSkills(
                        Arrays.stream(objectMapper.readValue(job.getRequiredSkills(), String[].class))
                                .collect(Collectors.toList())
                );
            } catch (JsonProcessingException e) {
                log.error("技能标签反序列化失败", e);
            }
        }

        // 格式化薪资范围
        if (job.getSalaryMin() != null && job.getSalaryMax() != null) {
            response.setSalaryRange(job.getSalaryMin() + "K-" + job.getSalaryMax() + "K");
        }

        // 格式化经验范围
        if (job.getExperienceMin() != null && job.getExperienceMax() != null) {
            response.setExperienceRange(job.getExperienceMin() + "-" + job.getExperienceMax() + "年");
        }

        // 状态描述
        if ("DRAFT".equals(job.getStatus())) {
            response.setStatusDesc("草稿");
        } else if ("PUBLISHED".equals(job.getStatus())) {
            response.setStatusDesc("已发布");
        } else if ("CLOSED".equals(job.getStatus())) {
            response.setStatusDesc("已关闭");
        }

        return response;
    }
}