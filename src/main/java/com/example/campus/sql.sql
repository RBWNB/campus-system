create DATABASE campus_system;




ALTER TABLE leaves
    ADD COLUMN reviewed_at DATETIME,
    ADD COLUMN reviewer VARCHAR(255),
    ADD COLUMN comment TEXT;
-- 修改courses表，添加学分和描述字段
ALTER TABLE courses ADD COLUMN credit DECIMAL(3,1) COMMENT '学分';
ALTER TABLE courses ADD COLUMN description TEXT COMMENT '课程描述';

-- 插入示例课程数据
INSERT INTO courses (code, name, credit, description) VALUES
                                                          ('CS102', '计算机科学导论', 3.0, '计算机科学基础课程，介绍计算机系统的基本概念和原理'),
                                                          ('MATH201', '高等数学', 4.0, '微积分、线性代数等数学基础课程'),
                                                          ('PHY101', '大学物理', 3.5, '物理学基础原理和实验方法'),
                                                          ('ENG101', '大学英语', 2.0, '英语听说读写综合训练'),
                                                          ('CHEM101', '基础化学', 3.0, '化学基本原理和实验技能');