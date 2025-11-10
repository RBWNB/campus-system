package com.example.campus.dto;

import com.example.campus.entity.Role;

public class AdminRegisterRequest {
    private String username;
    private String password;
    private String email;
    private String name; // 补充姓名字段（原有页面有该输入框）
    private Role role;
    private StudentInfo student;
    private TeacherInfo teacher; // 新增教师信息字段

    // Getter和Setter方法
    public String getUsername() {
        return username;
    }

    public void setUsername(String username) {
        this.username = username;
    }

    public String getPassword() {
        return password;
    }

    public void setPassword(String password) {
        this.password = password;
    }

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public Role getRole() {
        return role;
    }

    public void setRole(Role role) {
        this.role = role;
    }

    public StudentInfo getStudent() {
        return student;
    }

    public void setStudent(StudentInfo student) {
        this.student = student;
    }

    public TeacherInfo getTeacher() {
        return teacher;
    }

    public void setTeacher(TeacherInfo teacher) {
        this.teacher = teacher;
    }

    // 学生信息内部类（保持不变）
    public static class StudentInfo {
        private String studentNo;
        private String major;
        private String grade;
        private String phone;
        private String address;

        // Getter和Setter方法
        public String getStudentNo() {
            return studentNo;
        }

        public void setStudentNo(String studentNo) {
            this.studentNo = studentNo;
        }

        public String getMajor() {
            return major;
        }

        public void setMajor(String major) {
            this.major = major;
        }

        public String getGrade() {
            return grade;
        }

        public void setGrade(String grade) {
            this.grade = grade;
        }

        public String getPhone() {
            return phone;
        }

        public void setPhone(String phone) {
            this.phone = phone;
        }

        public String getAddress() {
            return address;
        }

        public void setAddress(String address) {
            this.address = address;
        }
    }

    // 新增教师信息内部类
    public static class TeacherInfo {
        private String teacherNo; // 教师编号（唯一）
        private String title; // 职称
        private String department; // 所属部门
        private String phone; // 联系电话
        private String office; // 办公室位置

        // Getter和Setter方法
        public String getTeacherNo() {
            return teacherNo;
        }

        public void setTeacherNo(String teacherNo) {
            this.teacherNo = teacherNo;
        }

        public String getTitle() {
            return title;
        }

        public void setTitle(String title) {
            this.title = title;
        }

        public String getDepartment() {
            return department;
        }

        public void setDepartment(String department) {
            this.department = department;
        }

        public String getPhone() {
            return phone;
        }

        public void setPhone(String phone) {
            this.phone = phone;
        }

        public String getOffice() {
            return office;
        }

        public void setOffice(String office) {
            this.office = office;
        }
    }
}