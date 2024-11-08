package com.np.hrms.repositories;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import com.np.hrms.entities.OperationMaster;

@Repository
public interface OperationMasterRepository extends JpaRepository<OperationMaster, Long> {

	@Query("SELECT d FROM OperationMaster d WHERE d.target = 'Employee' AND d.active = true")
	List<OperationMaster> getEmployeeOperations();

}
