package com.np.hrms.dto;

// DTO for Monthly Report
public class MonthlyReportDTO {
    private String empId;
    private String empName;
    private String reportYearMonth;
    private String averageWorkingHours;
    private double totalPresentDays;
    private double totalAbsentDays;
    private double totalLeavetakenDays;
	public String getEmpId() {
		return empId;
	}
	public void setEmpId(String empId) {
		this.empId = empId;
	}
	public String getEmpName() {
		return empName;
	}
	public void setEmpName(String empName) {
		this.empName = empName;
	}
	public String getReportYearMonth() {
		return reportYearMonth;
	}
	public void setReportYearMonth(String reportYearMonth) {
		this.reportYearMonth = reportYearMonth;
	}
	public String getAverageWorkingHours() {
		return averageWorkingHours;
	}
	public void setAverageWorkingHours(String averageWorkingHours) {
		this.averageWorkingHours = averageWorkingHours;
	}
	public double getTotalPresentDays() {
		return totalPresentDays;
	}
	public void setTotalPresentDays(double totalPresentDays) {
		this.totalPresentDays = totalPresentDays;
	}
	public double getTotalAbsentDays() {
		return totalAbsentDays;
	}
	public void setTotalAbsentDays(double totalAbsentDays) {
		this.totalAbsentDays = totalAbsentDays;
	}
	public double getTotalLeavetakenDays() {
		return totalLeavetakenDays;
	}
	public void setTotalLeavetakenDays(double totalLeavetakenDays) {
		this.totalLeavetakenDays = totalLeavetakenDays;
	}

}