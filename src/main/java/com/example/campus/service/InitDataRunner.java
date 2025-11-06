package com.example.campus.service;

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
                           CourseRepository courseRepo,
                           ClassroomRepository classroomRepo) {
        return args -> {
            if (!userRepo.findByUsername("admin").isPresent()) {
                User admin = new User();
                admin.setUsername("admin");
                admin.setPassword(passwordEncoder.encode("admin123"));
                admin.setRole(Role.ADMIN);
                admin.setName("系统管理员");
                admin.setEmail("admin@example.com");
                admin.setCreatedAt(Timestamp.from(Instant.now()));
                userRepo.save(admin);
            }

            // 在现有代码中添加教师用户初始化
            if (!userRepo.findByUsername("teacher").isPresent()) {
                User teacher = new User();
                teacher.setUsername("teacher");
                teacher.setPassword(passwordEncoder.encode("teacher123"));
                teacher.setRole(Role.TEACHER);
                teacher.setName("系统教师");
                teacher.setEmail("teacher@example.com");
                teacher.setCreatedAt(Timestamp.from(Instant.now()));
                userRepo.save(teacher);
            }

            if (!userRepo.findByUsername("student").isPresent()) {
                User s = new User();
                s.setUsername("student");
                s.setPassword(passwordEncoder.encode("student123"));
                s.setRole(Role.STUDENT);
                s.setName("示例学生");
                s.setEmail("stu@example.com");
                s.setCreatedAt(Timestamp.from(Instant.now()));
                userRepo.save(s);

                Student st = new Student();
                st.setUser(s);
                st.setStudentNo("S2023001");
                st.setMajor("计算机科学");
                st.setGrade("2023");
                studentRepo.save(st);
            }

            if (courseRepo.count() == 0) {
                Course c1 = new Course();
                c1.setCode("CS101");
                c1.setName("程序设计基础");
                c1.setCredit(BigDecimal.valueOf(3.0));
                c1.setDescription("入门课程，学习Java基础");
                c1.setCreatedAt(Timestamp.from(Instant.now()));
                courseRepo.save(c1);

                Course c2 = new Course();
                c2.setCode("MA101");
                c2.setName("高等数学");
                c2.setCredit(BigDecimal.valueOf(4.0));
                c2.setDescription("数学基础课程");
                c2.setCreatedAt(Timestamp.from(Instant.now()));
                courseRepo.save(c2);
            }

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
            }
        };
    }
}
