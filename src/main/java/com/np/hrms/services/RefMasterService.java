package com.np.hrms.services;

import java.util.List;
import java.util.Map;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;
import com.np.hrms.dto.EntityTypeMasterDTO;
import com.np.hrms.entities.EntityTypeMaster;
import com.np.hrms.model.Fields;
import com.np.hrms.repositories.SqlDAO;

@Service
public class RefMasterService {

	@Autowired
	private SqlDAO sqlDao;

	public EntityTypeMasterDTO loadEntityTypeConfig(String typeId) {
		Gson gsonObj = new Gson();
		EntityTypeMaster entityTypeMasterConfig = sqlDao.loadEntityConfig(typeId).get(0);
		String fieldsBody = entityTypeMasterConfig.getFormConfig();

		// Parse JSON to List of Fields
		List<Map<String, List<Fields>>> fieldsListWrapper = gsonObj.fromJson(fieldsBody,
				new TypeToken<List<Map<String, List<Fields>>>>() {
				}.getType());

		List<Fields> fieldsList = null;

		if (fieldsListWrapper != null && !fieldsListWrapper.isEmpty()) {
			Map<String, List<Fields>> firstItem = fieldsListWrapper.get(0);
			if (firstItem != null && firstItem.containsKey("fields") && firstItem.get("fields") != null) {
				fieldsList = firstItem.get("fields");
			}
		}

		EntityTypeMasterDTO entityDto = new EntityTypeMasterDTO();
		entityDto.setTypeId(entityTypeMasterConfig.getTypeId());
		entityDto.setTypeName(entityTypeMasterConfig.getTypeName());
		entityDto.setParentName(entityTypeMasterConfig.getParentName());
		entityDto.setActive(entityTypeMasterConfig.isActive());
		entityDto.setFields(fieldsList);
		entityDto.setDetails(sqlDao.loadTypeDataFileds(typeId));
		return entityDto;
	}
}
