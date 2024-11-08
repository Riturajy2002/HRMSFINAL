package com.np.hrms.model;

public class Fields {

	private String dataType;
	private String headingName;
	private boolean showInGrid;
	private boolean active;
	private String fieldId;
	private String fieldName;
	private boolean mandatory;

	public String getDataType() {
		return dataType;
	}

	public void setDataType(String dataType) {
		this.dataType = dataType;
	}

	public String getHeadingName() {
		return headingName;
	}

	public void setHeadingName(String headingName) {
		this.headingName = headingName;
	}

	public boolean isShowInGrid() {
		return showInGrid;
	}

	public void setShowInGrid(boolean showInGrid) {
		this.showInGrid = showInGrid;
	}

	public boolean isActive() {
		return active;
	}

	public void setActive(boolean active) {
		this.active = active;
	}

	public String getFieldId() {
		return fieldId;
	}

	public void setFieldId(String fieldId) {
		this.fieldId = fieldId;
	}

	public String getFieldName() {
		return fieldName;
	}

	public void setFieldName(String fieldName) {
		this.fieldName = fieldName;
	}

	public boolean isMandatory() {
		return mandatory;
	}

	public void setMandatory(boolean mandatory) {
		this.mandatory = mandatory;
	}
}
