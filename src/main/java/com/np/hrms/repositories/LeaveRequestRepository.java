package com.np.hrms.repositories;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;
import com.np.hrms.entities.LeavePosition;
import com.np.hrms.entities.LeaveRequest;
import java.util.Date;
import java.util.List;

@Repository
public interface LeaveRequestRepository extends JpaRepository<LeaveRequest, Long> {
	
	// Custom query to delete a leave request by id.
	@Modifying
	@Transactional
	@Query("UPDATE LeaveRequest lr SET lr.status = 'Cancelled' WHERE lr.id = :id")
	void cancelRequestById(@Param("id") String id);

	// For Finding the total Applied Leave Count for a given type.
	@Query(value = "SELECT COUNT(*) FROM leave_request " + "WHERE code = :code AND " + "status = 'Approved' AND "
			+ "EXTRACT(YEAR FROM applied_date) = EXTRACT(YEAR FROM CURRENT_DATE) AND "
			+ "user_id = :userId", nativeQuery = true)
	double getAppliedLeaveCount(@Param("code") String code, @Param("userId") String userId);

	@Query(value = "SELECT COALESCE(SUM(l.number_of_days), 0) FROM leave_request l WHERE  AND l.user_id = :userId AND l.code = :code AND l.status = 'Approved' "
			+ " AND YEAR(l.applied_date) = :year", nativeQuery = true)
	double getDaysCreditedForType(@Param("userId") String userId, @Param("code") String code,
			@Param("year") int year);

	@Query(value = "SELECT COALESCE(SUM(l.number_of_days), 0) FROM leave_request l WHERE AND l.user_id = :userId AND l.code = :code "
			+ "AND l.status = 'Approved' AND YEAR(l.applied_date) = :year", nativeQuery = true)
	double getDaysDebitedForType(@Param("userId") String userId, @Param("code") String code,
			@Param("year") int year);
	
	// Fetch leave requests based on status, year, and employee ID
	@Query("SELECT lr FROM LeaveRequest lr WHERE lr.status = :status AND lr.year = :year AND lr.userId = :userId")
	List<LeaveRequest> findAppliedLeaveRequest(@Param("status") String status, @Param("year") int year,
			@Param("userId") String userId);
	
	@Query("SELECT lr FROM LeaveRequest lr WHERE lr.userId = :userId AND "
			+ "(lr.fromDate <= :toDate AND lr.toDate >= :fromDate) " + "AND lr.status IN :statuses")
	List<LeaveRequest> findOverlappingLeaves(@Param("userId") String userId, @Param("fromDate") Date fromDate,
			@Param("toDate") Date toDate, @Param("statuses") List<String> statuses);

	

   void save(LeavePosition leaveRequest);
   
   //For getting the leave Request for updating it by the manager.
   @Query("Select lr from LeaveRequest lr where lr.id = :leaveId")
   LeaveRequest findLeaveById(@Param("leaveId") String leaveId);

}
