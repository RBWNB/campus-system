CREATE DATABASE campus_system;


-- 江，对管理员模块users表的约束
-- 姓名：2-4个汉字
ALTER TABLE users
    ADD CONSTRAINT chk_users_name CHECK (name REGEXP '^[\\u4e00-\\u9fa5]{2,4}$');
-- 邮箱：含@且@前1-20字符
ALTER TABLE users
    ADD CONSTRAINT chk_users_email CHECK (email REGEXP '^.{1,20}@.+$');
-- 密码：同时包含字母和数字
ALTER TABLE users
    ADD CONSTRAINT chk_users_password CHECK (password REGEXP '^(?=.*[a-zA-Z])(?=.*\\d).+$');
-- 用户名：最多16字符
ALTER TABLE users
    ADD CONSTRAINT chk_users_username CHECK (LENGTH(username) <= 16);

-- 江，对管理员模块leaves表的约束
-- 学生请假模块不可空写
ALTER TABLE leaves
    MODIFY COLUMN reason VARCHAR(255) NOT NULL COMMENT '请假原因（必须填写）';
