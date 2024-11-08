package com.np.hrms.model;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import com.np.hrms.entities.RefMaster;

public class OperationDTO {
    
	private String opName; 
	
	private String opCode;
	
	private String opType;
	
	private String opDesc;
	
	
	private Map<String, Param> params;
	
	private Map<String, List<RefMaster>> refData = new HashMap<String,List<RefMaster>>();
    
	private List<String> selectedUserIds;
	
	private String reason;
	
	private String year;
	
	private int colSize;
	
	private boolean users;
	
	public String getOpName() {
		return opName;
	}

	public void setOpName(String opName) {
		this.opName = opName;
	}
     
	public String getOpCode() {
		return opCode;
	}

	public void setOpCode(String opCode) {
		this.opCode = opCode;
	}
    
	public String getOpType() {
		return opType;
	}

	public void setOpType(String opType) {
		this.opType = opType;
	}
	

	public String getOpDesc() {
		return opDesc;
	}

	public void setOpDesc(String opDesc) {
		this.opDesc = opDesc;
	}

	public Map<String, Param> getParams() {
		return params;
	}

	public void setParams(Map<String, Param> params) {
		this.params = params;
	}

	public Map<String, List<RefMaster>> getRefData() {
		return refData;
	}

	public void setRefData(Map<String, List<RefMaster>> refData) {
		this.refData = refData;
	}

	public List<String> getSelectedUserIds() {
		return selectedUserIds;
	}

	public void setSelectedUserIds(List<String> selectedUserIds) {
		this.selectedUserIds = selectedUserIds;
	}

	public String getReason() {
		return reason;
	}

	public void setReason(String reason) {
		this.reason = reason;
	}

	public String getYear() {
		return year;
	}

	public void setYear(String year) {
		this.year = year;
	}

	public int getColSize() {
		return colSize;
	}

	public void setColSize(int colSize) {
		this.colSize = colSize;
	}

	public boolean isUsers() {
		return users;
	}

	public void setUsers(boolean users) {
		this.users = users;
	}
}
