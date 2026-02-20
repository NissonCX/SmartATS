-- ==================== SmartATS 候选人表创建脚本 ====================
-- 版本: v1.0
-- 创建日期: 2026-02-20
-- 说明: 存储 AI 解析的结构化候选人信息
-- ========================================================================

-- 如果表已存在，先删除（开发环境）
-- DROP TABLE IF EXISTS candidates;

-- 创建候选人表
CREATE TABLE `candidates` (
    -- 主键
    `id` BIGINT PRIMARY KEY AUTO_INCREMENT COMMENT '主键ID',

    -- 关联简历（1:1 关系）
    `resume_id` BIGINT NOT NULL UNIQUE COMMENT '简历ID',

    -- ========== 基本信息 ==========
    `name` VARCHAR(100) COMMENT '姓名',
    `phone` VARCHAR(20) COMMENT '手机号',
    `email` VARCHAR(100) COMMENT '邮箱',
    `gender` VARCHAR(10) COMMENT '性别（男/女/其他）',
    `age` INT COMMENT '年龄',

    -- ========== 教育信息 ==========
    `education` VARCHAR(50) COMMENT '学历（高中/专科/本科/硕士/博士）',
    `school` VARCHAR(200) COMMENT '毕业院校',
    `major` VARCHAR(200) COMMENT '专业',
    `graduation_year` INT COMMENT '毕业年份',

    -- ========== 工作信息 ==========
    `work_years` INT COMMENT '工作年限（年）',
    `current_company` VARCHAR(200) COMMENT '当前公司',
    `current_position` VARCHAR(200) COMMENT '当前职位',

    -- ========== JSON 字段（复杂结构）==========

    /**
     * 技能列表
     * 存储格式: ["Java", "Spring Boot", "MySQL", "Redis", "RabbitMQ"]
     */
    `skills` JSON COMMENT '技能列表（数组）',

    /**
     * 工作经历
     * 存储格式:
     * [
     *   {
     *     "company": "腾讯科技",
     *     "position": "后端开发工程师",
     *     "startDate": "2020-01",
     *     "endDate": "2023-06",
     *     "description": "负责核心业务系统开发和维护..."
     *   }
     * ]
     */
    `work_experience` JSON COMMENT '工作经历（JSON数组）',

    /**
     * 项目经历
     * 存储格式:
     * [
     *   {
     *     "name": "电商平台重构",
     *     "role": "核心开发",
     *     "startDate": "2022-03",
     *     "endDate": "2022-12",
     *     "description": "使用 Spring Cloud 重构旧系统...",
     *     "technologies": ["Java", "Spring Cloud", "MySQL", "Redis"]
     *   }
     * ]
     */
    `project_experience` JSON COMMENT '项目经历（JSON数组）',

    /**
     * 自我评价
     */
    `self_evaluation` TEXT COMMENT '自我评价',

    -- ========== AI 解析元数据 ==========

    /**
     * AI 解析的原始 JSON
     * 用途：调试、重新解析、审计
     */
    `raw_json` TEXT COMMENT 'AI 解析的原始 JSON 结果',

    /**
     * 置信度分数
     * 范围：0.00 - 1.00
     * 用途：判断解析质量，低于阈值需要人工审核
     */
    `confidence_score` DECIMAL(3,2) COMMENT '置信度分数',

    /**
     * 解析时间
     */
    `parsed_at` DATETIME COMMENT 'AI 解析完成时间',

    -- ========== 审计字段 ==========
    `created_at` DATETIME DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    `updated_at` DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',

    -- ========== 索引 ==========
    INDEX idx_resume_id (resume_id) COMMENT '简历ID索引',
    INDEX idx_name (name) COMMENT '姓名索引',
    INDEX idx_phone (phone) COMMENT '手机号索引',
    INDEX idx_email (email) COMMENT '邮箱索引',
    INDEX idx_education (education) COMMENT '学历索引',
    INDEX idx_work_years (work_years) COMMENT '工作年限索引',
    INDEX idx_created_at (created_at) COMMENT '创建时间索引'

) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='候选人信息表';

-- ========== 外键约束（可选）==========
-- 说明：外键可能影响性能，生产环境可以考虑应用层校验

-- ALTER TABLE candidates
--     ADD CONSTRAINT fk_candidate_resume
--     FOREIGN KEY (resume_id)
--     REFERENCES resumes(id)
--     ON DELETE CASCADE;

-- ========== 数据示例 ==========
-- 插入测试数据（可选）
-- INSERT INTO candidates (
--     resume_id, name, phone, email, gender, age,
--     education, school, major, graduation_year,
--     work_years, current_company, current_position,
--     skills, work_experience, project_experience, self_evaluation,
--     raw_json, confidence_score, parsed_at
-- ) VALUES (
--     1,
--     '张三',
--     '13800138000',
--     'zhangsan@example.com',
--     '男',
--     28,
--     '本科',
--     '清华大学',
--     '计算机科学与技术',
--     2018,
--     5,
--     '腾讯科技',
--     '后端开发工程师',
--     JSON_ARRAY('Java', 'Spring Boot', 'MySQL', 'Redis', 'RabbitMQ'),
--     JSON_ARRAY(
--         JSON_OBJECT(
--             'company', '腾讯科技',
--             'position', '后端开发工程师',
--             'startDate', '2020-01',
--             'endDate', '2023-06',
--             'description', '负责核心业务系统开发和维护'
--         )
--     ),
--     JSON_ARRAY(
--         JSON_OBJECT(
--             'name', '电商平台重构',
--             'role', '核心开发',
--             'startDate', '2022-03',
--             'endDate', '2022-12',
--             'description', '使用 Spring Cloud 重构旧系统',
--             'technologies', JSON_ARRAY('Java', 'Spring Cloud', 'MySQL', 'Redis')
--         )
--     ),
--     '5年后端开发经验，熟悉高并发系统设计',
--     '{"name": "张三", ...}',  -- 完整的原始 JSON
--     0.85,
--     NOW()
-- );

-- ========== 查询示例 ==========

-- 1. 查询所有候选人（按创建时间倒序）
-- SELECT * FROM candidates ORDER BY created_at DESC;

-- 2. 根据姓名搜索
-- SELECT * FROM candidates WHERE name LIKE '%张三%';

-- 3. 查询特定学历的候选人
-- SELECT * FROM candidates WHERE education = '本科';

-- 4. 查询工作年限 >= 3 年的候选人
-- SELECT * FROM candidates WHERE work_years >= 3;

-- 5. 查询包含特定技能的候选人（JSON 查询）
-- SELECT * FROM candidates WHERE JSON_CONTAINS(skills, '"Java"');

-- 6. 查询某个公司背景的候选人（JSON 查询）
-- SELECT * FROM candidates WHERE JSON_CONTAINS(
--     work_experience,
--     JSON_OBJECT('company', '腾讯')
-- );

-- 7. 统计各学历人数
-- SELECT education, COUNT(*) as count
-- FROM candidates
-- GROUP BY education
-- ORDER BY count DESC;

-- 8. 关联查询候选人及其简历信息
-- SELECT
--     c.id as candidate_id,
--     c.name,
--     c.phone,
--     c.email,
--     r.file_name as resume_file_name,
--     r.file_url as resume_file_url
-- FROM candidates c
-- LEFT JOIN resumes r ON c.resume_id = r.id
-- WHERE c.name = '张三';
