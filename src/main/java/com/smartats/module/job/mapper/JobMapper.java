package com.smartats.module.job.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.smartats.module.job.entity.Job;
import org.apache.ibatis.annotations.Mapper;

/**
 * 职位 Mapper
 */
@Mapper
public interface JobMapper extends BaseMapper<Job> {
    // MyBatis-Plus 自动提供 CRUD 方法，无需编写 XML
}