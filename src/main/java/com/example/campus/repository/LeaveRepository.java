// LeaveRepository.java
package com.example.campus.repository;

import com.example.campus.entity.LeaveRequest;
import com.example.campus.entity.LeaveStatus;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface LeaveRepository extends JpaRepository<LeaveRequest, Long> {
    List<LeaveRequest> findByStudent_Id(Long studentId); // 修改为 student.id
    List<LeaveRequest> findByStatus(LeaveStatus status);
    List<LeaveRequest> findByStatusNot(LeaveStatus status);
}
