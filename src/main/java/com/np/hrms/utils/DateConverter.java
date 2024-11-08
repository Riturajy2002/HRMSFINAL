package com.np.hrms.utils;

import java.util.Date;
import java.time.LocalDate;
import java.time.ZoneId;

public class DateConverter {
	
	// Convert LocalDate to java.sql.Date
	public static Date toDate(LocalDate localDate) {
		return localDate == null ? null : Date.from(localDate.atStartOfDay(ZoneId.systemDefault()).toInstant());
	}

	// Convert java.sql.Date to LocalDate
	public static LocalDate toLocalDate(Date date) {
		return date == null ? null : date.toInstant().atZone(ZoneId.systemDefault()).toLocalDate();
	}
}
