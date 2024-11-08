package com.np.hrms.entities;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;

@Entity
@Table(name = "entity_type_master")
public class EntityTypeMaster {
	
	@Id
	@Column(name = "c_type_id")
	private String typeId;

	@Column(name = "c_type_name")
	private String typeName;
	
	@Column(name = "c_parent_type")
	private String parentName;
	
	
	@Column(name = "screen_config")
	private String screenConfig;
	
	@Column(name = "form_config")
	private String  formConfig;
	
	@Column(name = "active")
	private boolean active;
        
	public String getTypeId() {
		return typeId;
	}

	public void setTypeId(String typeId) {
		this.typeId = typeId;
	}

	public String getTypeName() {
		return typeName;
	}

	public void setTypeName(String typeName) {
		this.typeName = typeName;
	}

	public String getParentName() {
		return parentName;
	}
	public void setParentName(String parentName) {
		this.parentName = parentName;
	}
	public String getScreenConfig() {
		return screenConfig;
	}
	public void setScreenConfig(String screenConfig) {
		this.screenConfig = screenConfig;
	}
	public String getFormConfig() {
		return formConfig;
	}

	public void setFormConfig(String formConfig) {
		this.formConfig = formConfig;
	}

	public boolean isActive() {
		return active;
	}

	public void setActive(boolean active) {
		this.active = active;
	}

	

}
