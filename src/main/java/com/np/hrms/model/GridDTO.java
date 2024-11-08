package com.np.hrms.model;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import com.np.hrms.entities.RefMaster;

public class GridDTO {

	private String screenName;
	 private String pageTitle;

	private String screenDesc;

	private String screenQuery;
	private String paramsValues;

	private List<Map<String, Object>> data;
	
    private Map<String, Map<String, Param>> params = new HashMap<String, Map<String, Param>>();
	
	private Map<String, List<RefMaster>> refData = new HashMap<String,List<RefMaster>>();
	
	private Map<String, Action> actions;
	private int pageNo;
	private int pageSize;

	public String getScreenName() {
		return screenName;
	}
 
	public String getPageTitle() {
		return pageTitle;
	}

	public void setPageTitle(String pageTitle) {
		this.pageTitle = pageTitle;
	}

	public void setScreenName(String screenName) {
		this.screenName = screenName;
	}

	public String getScreenDesc() {
		return screenDesc;
	}

	public void setScreenDesc(String screenDesc) {
		this.screenDesc = screenDesc;
	}

	public String getScreenQuery() {
		return screenQuery;
	}

	public void setScreenQuery(String screenQuery) {
		this.screenQuery = screenQuery;
	}
    
	
	
	public String getParamsValues() {
		return paramsValues;
	}

	public void setParamsValues(String paramsValues) {
		this.paramsValues = paramsValues;
	}

	public List<Map<String, Object>> getData() {
		return data;
	}

	public void setData(List<Map<String, Object>> data) {
		this.data = data;
	}

	public Map<String, Map<String, Param>> getParams() {
		return params;
	}

	public void setParams(Map<String, Map<String, Param>> params) {
		this.params = params;
	}

	public Map<String, List<RefMaster>> getRefData() {
		return refData;
	}

	public void setRefData(Map<String, List<RefMaster>> refData) {
		this.refData = refData;
	}


	public Map<String, Action> getActions() {
		return actions;
	}

	public void setActions(Map<String, Action> actions) {
		this.actions = actions;
	}

	public int getPageNo() {
		return pageNo;
	}

	public void setPageNo(int pageNo) {
		this.pageNo = pageNo;
	}

	public int getPageSize() {
		return pageSize;
	}

	public void setPageSize(int pageSize) {
		this.pageSize = pageSize;
	}
	
}
