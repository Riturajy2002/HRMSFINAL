package com.np.hrms.model;

public class LeaveTypeBreakdown {

    private String type;
    private double count;

    // Constructors
    public LeaveTypeBreakdown(String type, double count) {
        this.type = type;
        this.count = count;
    }

    public LeaveTypeBreakdown() {
        // Default constructor
    }

    // Getters and Setters
    public String getType() {
        return type;
    }

    public void setType(String type) {
        this.type = type;
    }

    public double getCount() {
        return count;
    }

    public void setCount(double count) {
        this.count = count;
    }
}
