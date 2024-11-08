package com.np.hrms.controllers;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.np.hrms.auth.Secured;
import com.np.hrms.dto.EntityTypeMasterDTO;
import com.np.hrms.entities.RefMaster;
import com.np.hrms.entities.TypeDataModel;
import com.np.hrms.entities.EntityTypeMaster;
import com.np.hrms.enums.Role;
import com.np.hrms.repositories.RefMasterRepository;
import com.np.hrms.repositories.SqlDAO;
import com.np.hrms.repositories.TypeMasterRepository;
import com.np.hrms.services.RefMasterService;

@RestController
@RequestMapping("/api")
public class RefMasterController {
	@Autowired
	private RefMasterRepository refMasterRepository;
	
	@Autowired 
	private TypeMasterRepository typeMasterRepository;
	
	@Autowired
	private RefMasterService refMasterService; 
	
	@Autowired
	private SqlDAO sqlDao;

	@GetMapping("/fetchRefData")
	@Secured({ Role.Admin, Role.Manager, Role.User })
	public List<RefMaster> fetchRefData(@RequestParam("refId") String refId, @RequestParam(value = "parent", required = false) String parent) {
		return refMasterRepository.fetchRefData(refId, parent);
	}
	
	//For saving the new types.
	@PostMapping("/define-type")
	//@Secured({ Role.Admin })
	public ResponseEntity<Map<String, String>> DefineNewType(@RequestBody EntityTypeMasterDTO entityTypeMasterDto) {
		Map<String, String> response = new HashMap<>();
		if(sqlDao.checkDefineId(entityTypeMasterDto.getTypeId()) != null) {
			response.put("error", "Alredy exists.");
			return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(response);
		}
		
 		int noOfRows = sqlDao.saveNewTypes(entityTypeMasterDto);
		if (noOfRows > 0) {
			response.put("success", "Type Registered Succesfully");
			return ResponseEntity.ok(response);
		} else {
			response.put("error", "Failed to register the Type.");
			return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(response);
		}
	}
	
	//adding the new entry of type Data model and altering the table
	@PostMapping("/add-typeModel")
	//@Secured({ Role.Admin })
	public ResponseEntity<Map<String, String>> addTypeModel(@RequestBody List<TypeDataModel> typeDataModel) {
		Map<String, String> response = new HashMap<>();
		
 		int noOfRows = sqlDao.addDataModelEntry(typeDataModel);
		if (noOfRows > 0) {
			response.put("success", "Type Model Added Succesfully");
			return ResponseEntity.ok(response);
		} else {
			response.put("error", "Failed to add  the Type Model.");
			return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(response);
		}
	}
	
	//For getting the defined types.
	@GetMapping("/get-types") 
	public List<EntityTypeMaster> getAllTypes() {
		return typeMasterRepository.getAllTypes();
	}
	@GetMapping("/get-type-DataModel") 
	public List<TypeDataModel> getAllTypesDataModel() {
		return sqlDao.getAllTypesDataModel();
	}
	@GetMapping("/get-allDataModelValues") 
	public List<Map<String, Object>> getAllDataModelValues(@RequestParam() String tableName) {
		
		return sqlDao.getAllDataModelValues(tableName);
	}
	
	// For saving the Ref Model Data.
	@PostMapping("/save-ref-data")
	public ResponseEntity<Map<String, String>> saveRefModelData(@RequestBody EntityTypeMasterDTO entityTypeMasterDto) {
	    Map<String, String> response = new HashMap<>();
	    try {
	        int noOfRows = sqlDao.saveRefModelData(entityTypeMasterDto);
	        response.put("success", "Ref Model Data Registered Successfully. Rows inserted: " + noOfRows);
	        return ResponseEntity.ok(response);
	    } catch (RuntimeException e) {
	        response.put("error", e.getMessage());
	        return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(response);
	    }
	}

	// For Getting the Entity TypeMaster Config.
	@GetMapping("/get-entity-type-config")
	public EntityTypeMasterDTO getEntityTypeMasterConfig(@RequestParam() String typeId) {
		EntityTypeMasterDTO entityConfigDTO = refMasterService.loadEntityTypeConfig(typeId);
		return entityConfigDTO;
	}
}
