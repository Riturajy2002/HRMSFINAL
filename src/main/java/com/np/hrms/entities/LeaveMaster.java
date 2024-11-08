package com.np.hrms.entities;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;

@Entity
@Table(name = "leave_master")
public class LeaveMaster {

	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	@Column(name = "leave_type_id")
	private Long id;
  
	@Column(name = "leave_name")
	private String leaveName;
	
	@Column(name = "leave_desc")
	private String leaveDesc;
	
	@Column(name = "applicable_for")
	private String applicableFor;
	
	@Column(name ="increment")
	private Double increment;
	
	@Column(name = "frequency")
	private String frequency;
	
	@Column(name = "sandwich_included")
	private boolean sandwichIncluded;
	
	@Column(name = "type")
	private String type;

	@Column(name = "max_times")
	private Integer maxTimes;
	
	@Column(name = "max_days")
	private Double maxDays;
	
	@Column(name = "short_code")
	private String code;
    
	@Column(name = "year")
	private int year;
	
	@Column(name = "organization")
	private String organization;
	
	@Column(name = "active")
	private boolean active;
	
	
	
	public LeaveMaster() {
		super();
	}

	public LeaveMaster(Long id, String leaveName, String applicableFor, Double increment, String frequency,
			boolean sandwichIncluded, String type, Integer maxTimes, Double maxDays, String code) {
		super();
		this.id = id;
		this.leaveName = leaveName;
		this.applicableFor = applicableFor;
		this.increment = increment;
		this.frequency = frequency;
		this.sandwichIncluded = sandwichIncluded;
		this.type = type;
		this.maxTimes = maxTimes;
		this.maxDays = maxDays;
		this.code = code;
	}

	public Long getId() {
		return id;
	}

	public void setId(Long id) {
		this.id = id;
	}

	public String getLeaveName() {
		return leaveName;
	}

	public void setLeaveName(String leaveName) {
		this.leaveName = leaveName;
	}
	
	public String getLeaveDesc() {
		return leaveDesc;
	}

	public void setLeaveDesc(String leaveDesc) {
		this.leaveDesc = leaveDesc;
	}

	public String getApplicableFor() {
		return applicableFor;
	}

	public void setApplicableFor(String applicableFor) {
		this.applicableFor = applicableFor;
	}

	public Double getIncrement() {
		return increment;
	}

	public void setIncrement(Double increment) {
		this.increment = increment;
	}

	public String getFrequency() {
		return frequency;
	}

	public void setFrequency(String frequency) {
		this.frequency = frequency;
	}

	public boolean isSandwichIncluded() {
		return sandwichIncluded;
	}

	public void setSandwichIncluded(boolean sandwichIncluded) {
		this.sandwichIncluded = sandwichIncluded;
	}

	public String getType() {
		return type;
	}

	public void setType(String type) {
		this.type = type;
	}

	public Integer getMaxTimes() {
		return maxTimes;
	}
	public void setMaxTimes(Integer maxTimes) {
		this.maxTimes = maxTimes;
	}

	public Double getMaxDays() {
		return maxDays;
	}

	public void setMaxDays(Double maxDays) {
		this.maxDays = maxDays;
	}

	public String getCode() {
		return code;
	}

	public void setCode(String code) {
		this.code = code;
	}
    
	
	public int getYear() {
		return year;
	}

	public void setYear(int year) {
		this.year = year;
	}

	public String getOrganization() {
		return organization;
	}

	public void setOrganization(String organization) {
		this.organization = organization;
	}

	public boolean isActive() {
		return active;
	}

	public void setActive(boolean active) {
		this.active = active;
	}

}