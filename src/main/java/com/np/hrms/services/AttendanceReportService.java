package com.np.hrms.services;

import org.apache.poi.hssf.usermodel.HSSFWorkbook;
import org.apache.poi.ss.usermodel.*;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import com.np.hrms.dto.MonthlyReportDTO;
import com.np.hrms.entities.AttendanceReport;
import com.np.hrms.repositories.AttendanceReportRepository;

import java.io.IOException;
import java.io.InputStream;
import java.time.Duration;
import java.time.LocalTime;
import java.time.Month;
import java.time.YearMonth;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Service
public class AttendanceReportService {

	@Autowired
	private AttendanceReportRepository attendanceReportRepository;

	public void processExcelFile(MultipartFile file) throws IOException {
		List<AttendanceReport> attendanceReports = new ArrayList<>();
		Workbook workbook;
		int year = 0;
		Month month = null;

		try (InputStream fis = file.getInputStream()) {
			String fileName = file.getOriginalFilename();
			if (fileName != null && fileName.endsWith(".xls")) {
				workbook = new HSSFWorkbook(fis);
			} else if (fileName != null && fileName.endsWith(".xlsx")) {
				workbook = new XSSFWorkbook(fis);
			} else {
				throw new IOException("Unsupported file format");
			}

			// Map to convert month abbreviations to Month enums
			Map<String, Month> monthMap = new HashMap<>();
			monthMap.put("JAN", Month.JANUARY);
			monthMap.put("FEB", Month.FEBRUARY);
			monthMap.put("MAR", Month.MARCH);
			monthMap.put("APR", Month.APRIL);
			monthMap.put("MAY", Month.MAY);
			monthMap.put("JUN", Month.JUNE);
			monthMap.put("JUL", Month.JULY);
			monthMap.put("AUG", Month.AUGUST);
			monthMap.put("SEP", Month.SEPTEMBER);
			monthMap.put("OCT", Month.OCTOBER);
			monthMap.put("NOV", Month.NOVEMBER);
			monthMap.put("DEC", Month.DECEMBER);

			// Process all sheets
			for (int i = 0; i < workbook.getNumberOfSheets(); i++) {
				Sheet sheet = workbook.getSheetAt(i);
				if (sheet == null) {
					continue;
				}

				// Find the Month and Year from the cell
				Row dateRow = sheet.getRow(2);
				if (dateRow == null) {
					continue;
				}

				Cell dateCell = dateRow.getCell(1);
				if (dateCell == null || dateCell.getCellType() != CellType.STRING) {
					continue;
				}

				String dateStr = dateCell.getStringCellValue().trim();
				String[] dateRangeParts = dateStr.split("\\s+");
				if (dateRangeParts.length < 3) {
					continue;
				}

				month = monthMap.get(dateRangeParts[0].toUpperCase());
				try {
					year = Integer.parseInt(dateRangeParts[2]);
				} catch (NumberFormatException e) {
					continue;
				}

				// Iterate over each row to process employee data
				for (int rowIndex = 0; rowIndex <= sheet.getLastRowNum(); rowIndex++) {
					Row row = sheet.getRow(rowIndex);
					if (row != null) {
						Cell cell0 = row.getCell(0);
						Cell cell1 = row.getCell(3);
						Cell cell2 = row.getCell(8);
						Cell cell3 = row.getCell(13);

						String empCode = null;
						String empName = null;

						if (cell0 != null && cell0.getCellType() == CellType.STRING
								&& "Emp. Code:".equalsIgnoreCase(cell0.getStringCellValue().trim())) {
							if (cell1 != null && cell1.getCellType() == CellType.STRING) {
								empCode = cell1.getStringCellValue().trim();
							}
						}

						if (cell2 != null && cell2.getCellType() == CellType.STRING
								&& "Emp. Name:".equalsIgnoreCase(cell2.getStringCellValue().trim())) {
							if (cell3 != null && cell3.getCellType() == CellType.STRING) {
								empName = cell3.getStringCellValue().trim();
							}
						}

						// If both employee code and name are found, proceed to extract related data
						if (empCode != null && empName != null) {
							Row statusRow = sheet.getRow(rowIndex + 1);
							Row inTimeRow = sheet.getRow(rowIndex + 2);
							Row outTimeRow = sheet.getRow(rowIndex + 3);
							Row totalRow = sheet.getRow(rowIndex + 4);

							for (int colIndex = 2; colIndex < statusRow.getLastCellNum(); colIndex++) {
								Date date = null;
								String dateString = sheet.getRow(6).getCell(colIndex).getStringCellValue();
								if (dateString == null || dateString.trim().isEmpty()) {
									continue;
								}
								String[] dateParts = dateString.split("\\s+");
								if (dateParts.length > 0) {
									String dateFirstPart = dateParts[0];
									if (dateFirstPart.matches("\\d+")) {
										try {
											int day = Integer.parseInt(dateFirstPart);
											date = new Date(year - 1900, month.getValue() - 1, day);
										} catch (NumberFormatException e) {
										}
									}
								}

								String status = statusRow.getCell(colIndex).getStringCellValue();
								String inTime = inTimeRow.getCell(colIndex).getStringCellValue();
								String outTime = outTimeRow.getCell(colIndex).getStringCellValue();

								String total = totalRow.getCell(colIndex).getStringCellValue();
								// Create an AttendanceReport entity and populate fields
								AttendanceReport attendanceReport = new AttendanceReport();
								attendanceReport.setUserId(empCode);
								attendanceReport.setUserName(empName);
								attendanceReport.setDate(date);
								attendanceReport.setInTime(inTime);
								attendanceReport.setOutTime(outTime);

								DateTimeFormatter timeFormatter = DateTimeFormatter.ofPattern("HH:mm");

								String totalTime = "00:00";
								if (inTime != null && !inTime.isEmpty() && outTime != null && !outTime.isEmpty()) {
									try {
										LocalTime in = LocalTime.parse(inTime, timeFormatter);
										LocalTime out = LocalTime.parse(outTime, timeFormatter);
										Duration workDuration = Duration.between(in, out);

										long hours = workDuration.toHours();
										long minutes = workDuration.toMinutes() % 60;

										// Format the total time
										totalTime = String.format("%02d:%02d", hours, minutes);

									} catch (Exception e) {

									}
								}

								attendanceReport.setWorkingHour(totalTime);
								attendanceReport.setStatus(status);

								if (month != null) {
									Month monthEnum = month;
									int monthNumber = monthEnum.getValue();
									YearMonth yearMonth = YearMonth.of(year, monthNumber);

									DateTimeFormatter formatter = DateTimeFormatter.ofPattern("MM-yyyy");
									String formattedYearMonth = yearMonth.format(formatter);
									attendanceReport.setReportYearMonth(formattedYearMonth);
								}

								attendanceReports.add(attendanceReport);

							}
						}
					}
				}

				// Save all reports to the database
				attendanceReportRepository.saveAll(attendanceReports);
			}
		}
	}

	public List<MonthlyReportDTO> fetchMonthlyReport(int month, int year) {
		YearMonth yearMonth = YearMonth.of(year, month);
		String reportYearMonth = yearMonth.format(DateTimeFormatter.ofPattern("MM-yyyy"));

		// Fetch all attendance records for the specified month and year
		List<AttendanceReport> reports = attendanceReportRepository.findByReportYearMonth(reportYearMonth);

		// Group the reports by user
		Map<String, List<AttendanceReport>> reportsByUser = reports.stream()
				.collect(Collectors.groupingBy(AttendanceReport::getUserId));

		// Calculate statistics for each user
		return reportsByUser.values().stream().map(this::calculateStatistics).collect(Collectors.toList());
	}

	private MonthlyReportDTO calculateStatistics(List<AttendanceReport> reports) {
		if (reports.isEmpty()) {
			return new MonthlyReportDTO();
		}

		// Extract the first report to get user details
		AttendanceReport firstReport = reports.get(0);
		MonthlyReportDTO reportDTO = new MonthlyReportDTO();
		reportDTO.setEmpId(firstReport.getUserId());
		reportDTO.setEmpName(firstReport.getUserName());
		reportDTO.setReportYearMonth(firstReport.getReportYearMonth());

		// Calculate total working hours and counts based on status
		double totalWorkingHours = 0;
		int presentDays = 0;
		int absentDays = 0;
		int leaveTakenDays = 0;

		for (AttendanceReport report : reports) {
			String status = report.getStatus().toUpperCase();

			if (status.contains("P")) {
				presentDays++;
				totalWorkingHours += parseWorkingHours(report.getWorkingHour());
			} else if (status.contains("A")) {
				absentDays++;
			} else if (status.contains("CL") || status.contains("FLH")) {

				leaveTakenDays++;
			}
		}

		// Calculate average working hours
		double averageWorkingHours = presentDays > 0 ? totalWorkingHours / presentDays : 0;


		String formattedAverageWorkingHours = formatHours(averageWorkingHours);

		// Set the formatted value in the DTO as a double
		reportDTO.setAverageWorkingHours(formattedAverageWorkingHours); 
		reportDTO.setTotalPresentDays(presentDays);
		reportDTO.setTotalAbsentDays(absentDays);
		reportDTO.setTotalLeavetakenDays(leaveTakenDays);

		return reportDTO;
	}

	private double parseWorkingHours(String workingHour) {
		if (workingHour == null || workingHour.isEmpty()) {
			return 0;
		}
		String[] parts = workingHour.split(":");
		if (parts.length != 2) {
			return 0;
		}

		try {
			int hours = Integer.parseInt(parts[0]);
			int minutes = Integer.parseInt(parts[1]);
			return hours + minutes / 60.0;
		} catch (NumberFormatException e) {
			return 0;
		}
	}

	public String formatHours(double averageWorkingHours) {
	    // Convert hours to minutes
	    int totalMinutes = (int) Math.round(averageWorkingHours * 60);
	    
	    // Calculate hours and minutes
	    int hours = totalMinutes / 60;
	    int minutes = totalMinutes % 60;
	    
	    // Format hours and minutes as HH:mm
	    return String.format("%02d:%02d", hours, minutes);
	}

}
