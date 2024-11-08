package com.np.hrms.services;

import java.util.Date;
import java.util.List;
import java.util.stream.Collectors;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.np.hrms.entities.HolidayMaster;
import com.np.hrms.repositories.HolidayMasterRepository;

@Service
public class HolidayService {

	@Autowired
	private HolidayMasterRepository holidayMasterRepository;

	public List<Date> getFixedHolidays() {
		List<Date> sqlDates = holidayMasterRepository.getFixedHolidaysForCurrentYear();
		return sqlDates.stream().map(date -> date).collect(Collectors.toList());
	}

	public void saveHoliday(HolidayMaster holiday) {
		holiday.setHolidayDate(holiday.getHolidayDate());
		holidayMasterRepository.save(holiday);
	}
}
