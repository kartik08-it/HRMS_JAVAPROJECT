package com.kartik.hrms.repository;

import java.time.LocalDate;
import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import com.kartik.hrms.entity.AttendanceRecord;

@Repository
public interface AttendanceRecordRepository extends JpaRepository<AttendanceRecord, Long> {

    @Query("select ar from AttendanceRecord ar where ar.date = :date")
    List<AttendanceRecord> findByDate(LocalDate date);

    @Query("select ar from AttendanceRecord ar where ar.date between :startDate and :endDate")
    List<AttendanceRecord> findByDateBetween(LocalDate startDate, LocalDate endDate);

    @Query(value = "select current_date", nativeQuery = true)
    LocalDate fetchCurrentDate();
}
