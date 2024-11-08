package com.np.hrms.dto;

import com.np.hrms.entities.User;

public class UserDTO {

	public UserDTO(User user) {
		super();
	}

	private String id;
	private String name;
	private String userId;
	private String designation;
	private Long contactNo;
	private String emailId;
	private String report_manager;
	public String getId() {
		return id;
	}

	public void setId(String string) {
		this.id = string;
	}

	public String getName() {
		return name;
	}

	public void setName(String name) {
		this.name = name;
	}
   
	public String getUserId() {
		return userId;
	}

	public void setUserId(String userId) {
		this.userId = userId;
	}

	public String getDesignation() {
		return designation;
	}

	public void setDesignation(String designation) {
		this.designation = designation;
	}

	public Long getContactNo() {
		return contactNo;
	}

	public void setContactNo(Long contactNo) {
		this.contactNo = contactNo;
	}

	public String getEmailId() {
		return emailId;
	}

	public void setEmailId(String emailId) {
		this.emailId = emailId;
	}

	public String getReport_manager() {
		return report_manager;
	}

	public void setReport_manager(String report_manager) {
		this.report_manager = report_manager;
	}
	
	public UserDTO(String id, String name, String userId) {
	    this.id = id;
	    this.name = name;
	    this.userId = userId;
	}
	
	public UserDTO(String userId, String name) {
		this.userId = userId;
	    this.name = name;
	}
	 
	public UserDTO() {
	}
}
