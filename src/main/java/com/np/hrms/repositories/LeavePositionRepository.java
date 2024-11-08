package com.np.hrms.repositories;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;

import com.np.hrms.entities.LeavePosition;

@Repository
public interface LeavePositionRepository extends JpaRepository<LeavePosition, Long> {
  
	// For Checking the count of credited leave for a given type.
	@Query(value = "SELECT COALESCE(SUM(number_of_days), 0) FROM leave_position " + "WHERE code = :code AND "
			+ "status = 'Approved' AND leave_operation_type = 'Credit' AND " + "user_id = :userId", nativeQuery = true)
	double getLeaveTypeCredited(@Param("code") String code, @Param("userId") String userId);

	// For Checking the count of Debited leave for a given type.
	@Query(value = "SELECT COALESCE(SUM(number_of_days), 0) FROM leave_position " + "WHERE code = :code AND "
			+ "status IN ('Approved', 'Pending') AND leave_operation_type = 'Debit' AND "
			+ "user_id = :userId", nativeQuery = true)
	double getLeaveTypeDebited(@Param("code") String code, @Param("userId") String userId);
     
	
	//For canceling the Leave request till status is pending.
	@Modifying
	@Transactional
	@Query("UPDATE LeavePosition lp SET lp.status = 'Cancelled' WHERE lp.leaveId = :leaveId")
	void cancelRequestByLeaveId(@Param("leaveId") String leaveId);
    
	//For getting the leave Request By the id for update it by the manager.
	@Query("SELECT lp from LeavePosition lp where lp.leaveId = :leaveId")
	LeavePosition findLeaveById(@Param("leaveId") String leaveId);

}
