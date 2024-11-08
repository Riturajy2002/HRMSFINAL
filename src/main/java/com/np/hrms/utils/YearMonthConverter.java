package com.np.hrms.utils;

import jakarta.persistence.AttributeConverter;
import jakarta.persistence.Converter;
import java.sql.Date;
import java.time.YearMonth;

// Just a Converter for the YearMonth to Sequel Date format 
@Converter(autoApply = true)
public class YearMonthConverter implements AttributeConverter<YearMonth, Date> {

	@Override
	public Date convertToDatabaseColumn(YearMonth yearMonth) {
		if (yearMonth == null) {
			return null;
		}
		return Date.valueOf(yearMonth.atDay(1)); 
	}

	@Override
	public YearMonth convertToEntityAttribute(Date date) {
		if (date == null) {
			return null;
		}
		return YearMonth.from(date.toLocalDate());
	}
}
