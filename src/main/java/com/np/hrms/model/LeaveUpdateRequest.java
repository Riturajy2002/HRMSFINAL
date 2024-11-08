package com.np.hrms.model;

import com.np.hrms.entities.LeavePosition;

// Wrapper class for the updation of leave after manager approve or declined.
public class LeaveUpdateRequest {
	private LeavePosition leaveRequest;
	private LeaveStatus leaveStatus;

	public LeavePosition getLeaveRequest() {
		return leaveRequest;
	}

	public void setLeaveRequest(LeavePosition leaveRequest) {
		this.leaveRequest = leaveRequest;
	}

	public LeaveStatus getLeaveStatus() {
		return leaveStatus;
	}

	public void setLeaveStatus(LeaveStatus leaveStatus) {
		this.leaveStatus = leaveStatus;
	}
}
