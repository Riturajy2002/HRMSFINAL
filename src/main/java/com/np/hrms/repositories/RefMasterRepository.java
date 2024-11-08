package com.np.hrms.repositories;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import com.np.hrms.entities.RefMaster;
import java.util.List;

@Repository
public interface RefMasterRepository extends JpaRepository<RefMaster, Long> {

	@Query("SELECT d FROM RefMaster d where d.refId = :refId and (:parent IS NULL OR d.parent = :parent) AND d.active = true")
	List<RefMaster> fetchRefData(@Param("refId") String refId, @Param("parent") String parent);
}
