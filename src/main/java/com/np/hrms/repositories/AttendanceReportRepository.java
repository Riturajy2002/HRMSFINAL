package com.np.hrms.repositories;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import com.np.hrms.entities.AttendanceReport;

import java.util.List;

@Repository
public interface AttendanceReportRepository extends JpaRepository<AttendanceReport, Long> {
    
    // Method to find records by the report year and month
    List<AttendanceReport> findByReportYearMonth(String reportYearMonth);
}
