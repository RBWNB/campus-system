-- 临时关闭外键约束（仅当前会话有效）
SET FOREIGN_KEY_CHECKS = 0;

-- 执行上述所有 DROP TABLE 语句
DROP TABLE IF EXISTS leaves;
DROP TABLE IF EXISTS grades;
DROP TABLE IF EXISTS schedule;
DROP TABLE IF EXISTS announcements;
DROP TABLE IF EXISTS courses;
DROP TABLE IF EXISTS students;
DROP TABLE IF EXISTS teachers;
DROP TABLE IF EXISTS classrooms;
DROP TABLE IF EXISTS users;

-- 恢复外键约束（必须恢复，保证数据完整性）
SET FOREIGN_KEY_CHECKS = 1;
CREATE TABLE users (
                       id INT PRIMARY KEY AUTO_INCREMENT,
                       username VARCHAR(50) NOT NULL UNIQUE,
                       password VARCHAR(255) NOT NULL,
                       role ENUM('ADMIN', 'TEACHER', 'STUDENT') NOT NULL,
                       name VARCHAR(50) NOT NULL,
                       email VARCHAR(100) NOT NULL UNIQUE,
                       created_at DATETIME DEFAULT CURRENT_TIMESTAMP
);
CREATE TABLE teachers (
                          id INT PRIMARY KEY AUTO_INCREMENT,
                          user_id INT NOT NULL,
                          teacher_no VARCHAR(20) NOT NULL UNIQUE,
                          title VARCHAR(50),
                          department VARCHAR(100),
                          phone VARCHAR(20),
                          office VARCHAR(100),
                          FOREIGN KEY (user_id) REFERENCES users(id) ON DELETE CASCADE
);
CREATE TABLE students (
                          id INT PRIMARY KEY AUTO_INCREMENT,
                          user_id INT NOT NULL,
                          student_no VARCHAR(20) NOT NULL UNIQUE,
                          major VARCHAR(100),
                          grade INT,
                          phone VARCHAR(20),
                          address VARCHAR(200),
                          FOREIGN KEY (user_id) REFERENCES users(id) ON DELETE CASCADE
);
CREATE TABLE classrooms (
                            id INT PRIMARY KEY AUTO_INCREMENT,
                            name VARCHAR(20) NOT NULL,
                            capacity INT,
                            location VARCHAR(100)
);
CREATE TABLE courses (
                         id INT PRIMARY KEY AUTO_INCREMENT,
                         code VARCHAR(20) NOT NULL UNIQUE,
                         name VARCHAR(100) NOT NULL,
                         credit DECIMAL(3,1) NOT NULL,
                         description TEXT,
                         teacher_id INT NOT NULL,
                         created_at DATETIME DEFAULT CURRENT_TIMESTAMP,
                         FOREIGN KEY (teacher_id) REFERENCES teachers(id) ON DELETE CASCADE
);
CREATE TABLE announcements (
                               id INT PRIMARY KEY AUTO_INCREMENT,
                               author INT NOT NULL,
                               content TEXT NOT NULL,
                               pinned BOOLEAN DEFAULT FALSE,
                               published_at DATETIME DEFAULT CURRENT_TIMESTAMP,
                               title VARCHAR(100) NOT NULL,
                               FOREIGN KEY (author) REFERENCES users(id) ON DELETE CASCADE
);
CREATE TABLE schedule (
                          id INT PRIMARY KEY AUTO_INCREMENT,
                          course_id INT NOT NULL,
                          classroom_id INT NOT NULL,
                          teacher INT NOT NULL,
                          weekday INT NOT NULL, -- 1-周一，2-周二…7-周日
                          start_time TIME NOT NULL,
                          end_time TIME NOT NULL,
                          term VARCHAR(50) NOT NULL,
                          teacher_user_id INT,
                          created_at DATETIME DEFAULT CURRENT_TIMESTAMP,
                          FOREIGN KEY (course_id) REFERENCES courses(id) ON DELETE CASCADE,
                          FOREIGN KEY (classroom_id) REFERENCES classrooms(id) ON DELETE CASCADE,
                          FOREIGN KEY (teacher) REFERENCES teachers(id) ON DELETE CASCADE,
                          FOREIGN KEY (teacher_user_id) REFERENCES users(id) ON DELETE SET NULL
);
CREATE TABLE grades (
                        id INT PRIMARY KEY AUTO_INCREMENT,
                        student_id INT NOT NULL,
                        course_id INT NOT NULL,
                        score DECIMAL(5,2),
                        grade_type ENUM('MIDTERM', 'ORDINARY', 'TOTAL') NOT NULL,
                        updated_at DATETIME DEFAULT CURRENT_TIMESTAMP,
                        FOREIGN KEY (student_id) REFERENCES students(id) ON DELETE CASCADE,
                        FOREIGN KEY (course_id) REFERENCES courses(id) ON DELETE CASCADE
);
CREATE TABLE leaves (
                        id INT PRIMARY KEY AUTO_INCREMENT,
                        student_id INT NOT NULL,
                        reason TEXT NOT NULL,
                        start_date DATE NOT NULL,
                        end_date DATE NOT NULL,
                        status ENUM('PENDING', 'APPROVED', 'REJECTED') DEFAULT 'PENDING',
                        approved_by INT,
                        applied_at DATETIME DEFAULT CURRENT_TIMESTAMP,
                        approved_at DATETIME,
                        FOREIGN KEY (student_id) REFERENCES students(id) ON DELETE CASCADE,
                        FOREIGN KEY (approved_by) REFERENCES users(id) ON DELETE SET NULL
);


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
