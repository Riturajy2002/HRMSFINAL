package com.np.hrms.model;

public class LeaveStatusBreakdown {
    private String type;
    private double total;
    private double available;
    private double approved;
    private double pending;

    public double getTotal() {
		return total;
	}
	public void setTotal(double total) {
		this.total = total;
	}
	public String getType() { return type; }
    public void setType(String type) { this.type = type; }

    public double getAvailable() { return available; }
    public void setAvailable(double available) { this.available = available; }

    public double getApproved() { return approved; }
    public void setApproved(double approved) { this.approved = approved; }

    public double getPending() { return pending; }
    public void setPending(double pending) { this.pending = pending; }
}
