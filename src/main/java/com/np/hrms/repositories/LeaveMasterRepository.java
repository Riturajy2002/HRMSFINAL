package com.np.hrms.repositories;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import com.np.hrms.entities.LeaveMaster;

@Repository
public interface LeaveMasterRepository extends JpaRepository<LeaveMaster, Long> {

	// For getting max Allowed Count.
	@Query(value = "SELECT max_applicable FROM leave_master WHERE leave_name = :leaveName", nativeQuery = true)
	double getMaxAllowedCountByLeaveType(@Param("leaveName") String leaveName);

	@Query(value = "SELECT max_times FROM leave_master WHERE leave_name = :leaveName", nativeQuery = true)
	double getDaysLimitByLeaveType(@Param("leaveName") String leaveName);

	
	// Method to get all leave codes from the leave_master table
	@Query("SELECT lm.code FROM LeaveMaster lm")
	List<String> getAllShortCodes();

	// Finding Monthly Increment For Crediting the leaves.
	@Query(value = "SELECT monthly_increment FROM leave_master WHERE leave_name = :leaveType", nativeQuery = true)
	String findMonthlyIncrement(@Param("leaveType") String leaveType);
    
	@Query("SELECT l FROM LeaveMaster l WHERE l.organization =:organization AND l.year =:year")
	List<LeaveMaster> findAllLeaves(@Param("organization") String organization, @Param("year") int year);
 
	
	@Query("SELECT l FROM LeaveMaster l WHERE l.organization =:organization AND l.active = true")
	List<LeaveMaster> getAllLeaveTypes(@Param("organization") String organization);


}
