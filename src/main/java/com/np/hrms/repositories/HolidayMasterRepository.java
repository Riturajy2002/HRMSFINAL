package com.np.hrms.repositories;

import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.CrudRepository;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import com.np.hrms.entities.HolidayMaster;
import java.util.Date;
import java.util.List;
import java.util.Optional;

@Repository
public interface HolidayMasterRepository extends CrudRepository<HolidayMaster, Long> {

	// Getting all flexi Days Count.
	@Query("SELECT h FROM HolidayMaster h WHERE h.type = 'Flexi' AND YEAR(h.holidayDate) = :year")
	List<HolidayMaster> findAllFlexiLeavesForYear(@Param("year") int year);

	// Fetch fixed holidays from the database
	@Query("SELECT h.holidayDate FROM HolidayMaster h WHERE h.type = 'Fixed' AND YEAR(h.holidayDate) = YEAR(CURRENT_DATE) ORDER BY h.holidayDate ASC ")
	List<Date> getFixedHolidaysForCurrentYear();

	// In HolidayMasterRepository.java
	@Query("SELECT h FROM HolidayMaster h WHERE h.type = 'Fixed' AND  YEAR(h.holidayDate) = :currentYear ORDER BY h.holidayDate ASC")
	List<HolidayMaster> getAllFixedHolidaysForCurrentYear(@Param("currentYear") int currentYear);
	
	Optional<HolidayMaster> findByHolidayDate(Date holidayDate);

	@Query("SELECT h FROM HolidayMaster h WHERE h.year =:year AND h.active = true")
	List<HolidayMaster> getAllHolidays(@Param("year") int year);
}
