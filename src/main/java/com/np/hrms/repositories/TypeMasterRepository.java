package com.np.hrms.repositories;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import com.np.hrms.entities.EntityTypeMaster;

public interface TypeMasterRepository extends JpaRepository<EntityTypeMaster, Long> {

	
	@Query("SELECT DISTINCT d FROM EntityTypeMaster d WHERE d.formConfig IS NOT NULL AND d.active = true")
	List<EntityTypeMaster> getAllTypes();

}
