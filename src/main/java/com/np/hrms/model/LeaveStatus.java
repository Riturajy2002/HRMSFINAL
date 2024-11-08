package com.np.hrms.model;

import java.util.List;
import com.np.hrms.dto.UserDTO;

public class LeaveStatus {

    private Long id;
    private double total;
    private double available;
    private double approved;
    private double pending;
    private double lwpCount;
    private List<LeaveTypeBreakdown> leaveBreakdown;
    private UserDTO userDto;

    // Default constructor
    public LeaveStatus() {
    }

    // Getters and Setters
    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public double getTotal() {
        return total;
    }

    public void setTotal(double total) {
        this.total = total;
    }

    public double getAvailable() {
        return available;
    }

    public void setAvailable(double available) {
        this.available = available;
    }

    public double getApproved() {
        return approved;
    }

    public void setApproved(double approved) {
        this.approved = approved;
    }

    public double getPending() {
        return pending;
    }

    public void setPending(double pending) {
        this.pending = pending;
    }

    public double getLwpCount() {
        return lwpCount;
    }

    public void setLwpCount(double lwpCount) {
        this.lwpCount = lwpCount;
    }

    public List<LeaveTypeBreakdown> getLeaveBreakdown() {
        return leaveBreakdown;
    }

    public void setLeaveBreakdown(List<LeaveTypeBreakdown> leaveBreakdown) {
        this.leaveBreakdown = leaveBreakdown;
    }

    public UserDTO getUserDto() {
        return userDto;
    }

    public void setUserDto(UserDTO userDto) {
        this.userDto = userDto;
    }
}
