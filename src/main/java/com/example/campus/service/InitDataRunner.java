package com.example.campus.service;

import com.example.campus.entity.Role; // 🔥 必须导入独立的 Role 枚举
import com.example.campus.entity.*;
import com.example.campus.repository.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.CommandLineRunner;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.crypto.password.PasswordEncoder;

import java.math.BigDecimal;
import java.sql.Timestamp;
import java.time.Instant;

@Configuration
public class InitDataRunner {

    @Autowired
    private PasswordEncoder passwordEncoder;

    @Bean
    CommandLineRunner init(UserRepository userRepo,
                           StudentRepository studentRepo,
                           TeacherRepository teacherRepo,
                           CourseRepository courseRepo,
                           ClassroomRepository classroomRepo,
                           GradeRepository gradeRepo,
                           AnnouncementRepository announcementRepo,
                           LeaveRepository leaveRepo,
                           ScheduleRepository scheduleRepo) {
        return args -> {
            // 初始化管理员：使用独立的 Role.ADMIN
            if (!userRepo.findByUsername("admin").isPresent()) {
                User admin = new User();
                admin.setUsername("admin");
                admin.setPassword(passwordEncoder.encode("admin123"));
                admin.setRole(Role.ADMIN); // 🔥 无前缀，直接用独立枚举
                admin.setName("系统管理员");
                admin.setEmail("admin@example.com");
                admin.setCreatedAt(Timestamp.from(Instant.now()));
                userRepo.save(admin);
            }

            // 初始化教师用户：使用 Role.TEACHER
            User teacherUser = null;
            Teacher teacher = null;
            if (!userRepo.findByUsername("teacher").isPresent()) {
                teacherUser = new User();
                teacherUser.setUsername("teacher");
                teacherUser.setPassword(passwordEncoder.encode("teacher123"));
                teacherUser.setRole(Role.TEACHER); // 🔥 独立枚举
                teacherUser.setName("张老师");
                teacherUser.setEmail("teacher@example.com");
                teacherUser.setCreatedAt(Timestamp.from(Instant.now()));
                userRepo.save(teacherUser);

                teacher = new Teacher();
                teacher.setUser(teacherUser);
                teacher.setTeacherNo("T2023001");
                teacher.setTitle("副教授");
                teacher.setDepartment("计算机科学学院");
                teacher.setPhone("13800138000");
                teacher.setOffice("教学楼A-301");
                teacherRepo.save(teacher);
            } else {
                teacherUser = userRepo.findByUsername("teacher").get();
                teacher = teacherRepo.findByUserId(teacherUser.getId()).orElse(null);
                if (teacher == null) {
                    teacher = new Teacher();
                    teacher.setUser(teacherUser);
                    teacher.setTeacherNo("T2023001");
                    teacher.setTitle("副教授");
                    teacher.setDepartment("计算机科学学院");
                    teacher.setPhone("13800138000");
                    teacher.setOffice("教学楼A-301");
                    teacherRepo.save(teacher);
                }
            }

            // 初始化学生：使用 Role.STUDENT
            User studentUser = null;
            Student student = null;
            if (!userRepo.findByUsername("student").isPresent()) {
                studentUser = new User();
                studentUser.setUsername("student");
                studentUser.setPassword(passwordEncoder.encode("student123"));
                studentUser.setRole(Role.STUDENT); // 🔥 独立枚举
                studentUser.setName("李同学");
                studentUser.setEmail("stu@example.com");
                studentUser.setCreatedAt(Timestamp.from(Instant.now()));
                userRepo.save(studentUser);

                student = new Student();
                student.setUser(studentUser);
                student.setStudentNo("S2023001");
                student.setMajor("计算机科学");
                student.setGrade("2023");
                student.setPhone("13900139000");
                student.setAddress("学生宿舍1号楼101室");
                studentRepo.save(student);
            } else {
                studentUser = userRepo.findByUsername("student").get();
                student = studentRepo.findByUserId(studentUser.getId()).orElse(null);
            }

            // 初始化课程（不变）
            if (courseRepo.count() == 0) {
                Course c1 = new Course();
                c1.setCode("CS101");
                c1.setName("程序设计基础");
                c1.setCredit(BigDecimal.valueOf(3.0));
                c1.setDescription("入门课程，学习Java基础");
                c1.setTeacher(teacher);
                c1.setCreatedAt(Timestamp.from(Instant.now()));
                courseRepo.save(c1);

                Course c2 = new Course();
                c2.setCode("MA101");
                c2.setName("高等数学");
                c2.setCredit(BigDecimal.valueOf(4.0));
                c2.setDescription("数学基础课程");
                c2.setTeacher(teacher);
                c2.setCreatedAt(Timestamp.from(Instant.now()));
                courseRepo.save(c2);

                Course c3 = new Course();
                c3.setCode("EN101");
                c3.setName("大学英语");
                c3.setCredit(BigDecimal.valueOf(2.0));
                c3.setDescription("英语基础课程");
                c3.setTeacher(teacher);
                c3.setCreatedAt(Timestamp.from(Instant.now()));
                courseRepo.save(c3);

                // 初始化成绩（不变）
                if (student != null) {
                    Grade grade1 = new Grade();
                    grade1.setStudent(student);
                    grade1.setCourse(c1);
                    grade1.setScore(BigDecimal.valueOf(85.5));
                    grade1.setGradeType("期中成绩");
                    grade1.setUpdatedAt(Timestamp.from(Instant.now()));
                    gradeRepo.save(grade1);

                    Grade grade2 = new Grade();
                    grade2.setStudent(student);
                    grade2.setCourse(c1);
                    grade2.setScore(BigDecimal.valueOf(90.0));
                    grade2.setGradeType("平时成绩");
                    grade2.setUpdatedAt(Timestamp.from(Instant.now()));
                    gradeRepo.save(grade2);

                    Grade grade3 = new Grade();
                    grade3.setStudent(student);
                    grade3.setCourse(c2);
                    grade3.setScore(BigDecimal.valueOf(92.0));
                    grade3.setGradeType("期中成绩");
                    grade3.setUpdatedAt(Timestamp.from(Instant.now()));
                    gradeRepo.save(grade3);

                    Grade grade4 = new Grade();
                    grade4.setStudent(student);
                    grade4.setCourse(c3);
                    grade4.setScore(BigDecimal.valueOf(88.5));
                    grade4.setGradeType("期中成绩");
                    grade4.setUpdatedAt(Timestamp.from(Instant.now()));
                    gradeRepo.save(grade4);
                }
            }

            // 初始化教室（不变）
            if (classroomRepo.count() == 0) {
                Classroom r1 = new Classroom();
                r1.setName("A101");
                r1.setCapacity(60);
                r1.setLocation("教学楼A-101");
                classroomRepo.save(r1);

                Classroom r2 = new Classroom();
                r2.setName("B201");
                r2.setCapacity(40);
                r2.setLocation("教学楼B-201");
                classroomRepo.save(r2);

                Classroom r3 = new Classroom();
                r3.setName("C301");
                r3.setCapacity(80);
                r3.setLocation("教学楼C-301");
                classroomRepo.save(r3);

                Classroom r4 = new Classroom();
                r4.setName("D102");
                r4.setCapacity(50);
                r4.setLocation("教学楼D-102");
                classroomRepo.save(r4);
            }
        };
    }
}