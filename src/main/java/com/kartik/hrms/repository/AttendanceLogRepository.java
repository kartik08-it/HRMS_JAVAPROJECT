package com.kartik.hrms.repository;

import java.time.LocalDateTime;
import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import com.kartik.hrms.entity.AttendanceLog;

@Repository
public interface AttendanceLogRepository extends JpaRepository<AttendanceLog, Long> {

    List<AttendanceLog> findTop8ByTimestampBetweenOrderByTimestampDesc(
            LocalDateTime start,
            LocalDateTime end);
}
