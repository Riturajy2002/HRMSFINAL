package com.np.hrms.repositories;

import java.sql.CallableStatement;
import org.springframework.transaction.annotation.Transactional;
import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Types;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.dao.DataAccessException;
import org.springframework.jdbc.core.BeanPropertyRowMapper;
import org.springframework.jdbc.core.CallableStatementCreator;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.core.ResultSetExtractor;
import org.springframework.jdbc.core.SqlParameter;
import org.springframework.stereotype.Repository;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.jdbc.core.RowMapper;
import com.np.hrms.auth.PropertyPlaceholder;
import com.np.hrms.dto.EntityTypeMasterDTO;
import com.np.hrms.entities.EntityTypeMaster;
import com.np.hrms.entities.GridConfig;
import com.np.hrms.entities.OperationMaster;
import com.np.hrms.entities.RefMaster;
import com.np.hrms.entities.TypeDataModel;
import com.np.hrms.entities.User;
import com.np.hrms.model.Fields;

@Repository
public class SqlDAO {
	

	@Autowired
	JdbcTemplate jdbcTemplate;
	
	@Autowired
	PropertyPlaceholder env;
	
	public List<User> getUsers() {
		String sqlQuery = "select u.id, u.user_id, u.role, u.user_key, cast(aes_decrypt(password, '"+ env.SECRET_KEY + "') as char),"
				+ " u.name,  u.email_id, u.designation, u.report_manager,u.contact_no, u.gender , u.organization"
				+ " from user u where u.active=true";
		
		
		
		List<User> users = jdbcTemplate.query(sqlQuery, new ResultSetExtractor<List<User>>(){
 
			public List<User> extractData(ResultSet rs) throws SQLException, DataAccessException {
				List<User> usersList = new ArrayList<User>();
				while (rs.next()){
					User user = new User();
					user.setId(rs.getString(1));
					user.setUserId(rs.getString(2));
					user.setRole(rs.getString(3));
					user.setUserKey(rs.getString(4));
					user.setPassword(rs.getString(5));
					user.setName(rs.getString(6));
					user.setEmailId(rs.getNString(7));
					user.setDesignation(rs.getNString(8));
					user.setReportManager(rs.getNString(9));
                    user.setContactNo(rs.getLong(10));
                    user.setGender(rs.getNString(11));
                    user.setOrganization(rs.getNString(12));
					usersList.add(user);		
			}
				return usersList;
			}		
	});
		return users;		
	
}		
	
	// For the Registration of the New User 
	public int saveUser(User user) {
        String query = "INSERT INTO user (id, user_id, name, password, designation, department, role, user_key, contact_no,"
        		+ "email_id, report_manager,birth_date, anniversary_date, location, gender, organization, type, active) " +
                "VALUES (?, ?, ?, aes_encrypt(?,?), ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?) ";
       
       int insertData = jdbcTemplate.update(query , user.getId(), user.getUserId(), user.getName(), user.getPassword(),env.SECRET_KEY,
    		user.getDesignation(), user.getDepartment(), user.getRole(), user.getUserKey(),
        	user.getContactNo(), user.getEmailId(), user.getReportManager(), user.getBirthDate(),
        	user.getAnniversaryDate(), user.getLocation(),  user.getGender(), user.getOrganization(), user.getType(), user.isActive());
       return insertData;
	}
	
	// For the Saving of the New Types.
	public int saveNewTypes(EntityTypeMasterDTO entityTypeMasterDto) {
		int insertData = 0;
		try {

			Map<String, List<Fields>> fieldsWrapper = new HashMap<>();
			fieldsWrapper.put("fields", entityTypeMasterDto.getFields());

			List<Map<String, List<Fields>>> fieldConfigList = List.of(fieldsWrapper);

			ObjectMapper objectMapper = new ObjectMapper();
			String fieldConfigJson = objectMapper.writeValueAsString(fieldConfigList);

			String query = "INSERT INTO entity_type_master (c_type_id, c_type_name, c_parent_type, form_config, active) "
					+ "VALUES (?, ?, ?, ?, ?)";

			insertData = jdbcTemplate.update(query, entityTypeMasterDto.getTypeId(), entityTypeMasterDto.getTypeName(),
					entityTypeMasterDto.getParentName(), fieldConfigJson, entityTypeMasterDto.isActive());

			if (insertData > 0) {
				checkAndCreateOrUpdateTable(entityTypeMasterDto, jdbcTemplate);
			}
		} catch (Exception e) {
			e.printStackTrace();
		}
	    return insertData;
	}
	
	//Creation of checkAndCreateOrUpdateTable
	public void checkAndCreateOrUpdateTable(EntityTypeMasterDTO entityTypeMasterDto, JdbcTemplate jdbcTemplate) {
	    String tableName = "type_data_" + entityTypeMasterDto.getTypeId();
	    
	    Map<String, String> existingColumns = getTableColumns(tableName, jdbcTemplate);

	    if (existingColumns.isEmpty()) {
	        createTypeDataTable(entityTypeMasterDto, jdbcTemplate);
	    } else {
	    	alterExistingTypeDataTable(entityTypeMasterDto, existingColumns, tableName, jdbcTemplate);
	    }
	}
	
	//Checking if table exist with the same name then find all Columns of that.
	private Map<String, String> getTableColumns(String tableName, JdbcTemplate jdbcTemplate) {
	    String query = "SELECT COLUMN_NAME, DATA_TYPE FROM information_schema.columns "
	                 + "WHERE table_schema = 'hrms' AND table_name = ?";
	    List<Map.Entry<String, String>> columns = jdbcTemplate.query(query, new Object[]{tableName}, new RowMapper<>() {
	        @Override
	        public Map.Entry<String, String> mapRow(ResultSet rs, int rowNum) throws SQLException {
	            return Map.entry(rs.getString("COLUMN_NAME"), rs.getString("DATA_TYPE"));
	        }
	    });
	    
	    Map<String, String> columnMap = new HashMap<>();
	    for (Map.Entry<String, String> column : columns) {
	        columnMap.put(column.getKey(), column.getValue());
	    }
	    return columnMap;
	}
	
	//Creation of table if not exist it Prior.
	private void createTypeDataTable(EntityTypeMasterDTO entityTypeMasterDto, JdbcTemplate jdbcTemplate) {
		
	    String tableName = "type_data_" + entityTypeMasterDto.getTypeId();
	    
	    StringBuilder sqlBuilder = new StringBuilder("CREATE TABLE IF NOT EXISTS `" + tableName + "` (");
	    
	    sqlBuilder.append("id INT NOT NULL AUTO_INCREMENT PRIMARY KEY, ");
	    sqlBuilder.append("name VARCHAR(255), ");
	    
	    for (Fields field : entityTypeMasterDto.getFields()) {
	        String sqlDataType = mapDataTypeToSql(field.getDataType());
	        sqlBuilder.append(field.getFieldId()).append(" ").append(sqlDataType).append(", ");
	    }
	    
	    sqlBuilder.setLength(sqlBuilder.length() - 2);
	    sqlBuilder.append(")");
	    jdbcTemplate.execute(sqlBuilder.toString());
	}
	
	// If table exist then Modification in that table.
	private void alterExistingTypeDataTable(EntityTypeMasterDTO entityTypeMasterDto,
			Map<String, String> existingColumns, String tableName, JdbcTemplate jdbcTemplate) {

		Map<String, String> dtoFieldsMap = new HashMap<>();
		for (Fields field : entityTypeMasterDto.getFields()) {
			dtoFieldsMap.put(field.getFieldId(), mapDataTypeToSql(field.getDataType()));
		}
       
		 // Case 1: Handle Additions
	    for (Map.Entry<String, String> dtoField : dtoFieldsMap.entrySet()) {
	        String fieldId = dtoField.getKey();
	        String newDataType = dtoField.getValue();
	        if (!existingColumns.containsKey(fieldId)) {
	            String alterAddColumn = "ALTER TABLE " + tableName + " ADD COLUMN " + fieldId + " " + newDataType;
	            jdbcTemplate.execute(alterAddColumn);
	        }
	    }
	    
	   // Case 2: Handle Remove Case.
	    for (String existingColumn : existingColumns.keySet()) {
	        if (!dtoFieldsMap.containsKey(existingColumn)) {
	            String alterDropColumn = "ALTER TABLE " + tableName + " DROP COLUMN " + existingColumn;
	            jdbcTemplate.execute(alterDropColumn);
	        }
	    }
		
	    //case 3 : Changing case.(Remaining)
	    for (Map.Entry<String, String> dtoField : dtoFieldsMap.entrySet()) {
	        String fieldId = dtoField.getKey();
	        String newDataType = dtoField.getValue();
	        String expectedDataType = "VARCHAR(255)";

	        if (existingColumns.containsKey(fieldId) && !existingColumns.get(fieldId).equalsIgnoreCase(newDataType)) {
	            String alterModifyColumn = "ALTER TABLE " + tableName + " MODIFY COLUMN " + fieldId + " " + expectedDataType;
	            jdbcTemplate.execute(alterModifyColumn);
	        }
	    } 
		
	}
	
	// Helper method to map JSON data types to SQL data types
	private String mapDataTypeToSql(String dataType) {
	    switch (dataType.toLowerCase()) {
	        case "text":
	            return "VARCHAR(255)";
	        case "number":
	            return "INT";
	        case "boolean":
	            return "BOOLEAN";
	        case "custom":
	        case "file":
	        case "dropdown":
	        case "radio":
	        case "checkbox":
	            return "VARCHAR(255)";
	        case "date":
	            return "DATE";
	        case "email":
	            return "VARCHAR(255)";
	        default:
	            return "VARCHAR(255)";
	    }
	}
	
	// loading the data from TypeDataTable.
	public List<Map<String, Object>> loadTypeDataFileds(String typeId) {
		String tableName = "type_data_" + typeId;
		String query = "SELECT * FROM " + tableName;

		List<Map<String, Object>> resultList = jdbcTemplate.queryForList(query);
		return resultList;
	}
	
	// For the Saving of the New Types.
	@Transactional
	public int saveRefModelData(EntityTypeMasterDTO entityTypeMasterDto) {
	    String tableName = "type_data_" + entityTypeMasterDto.getTypeId();
	    List<Map<String, Object>> details = entityTypeMasterDto.getDetails();
	    int rowsInserted = 0;
	    
	    try {
	        for (Map<String, Object> detail : details) {
	            StringBuilder columnNames = new StringBuilder();
	            StringBuilder columnValues = new StringBuilder();

	            for (Map.Entry<String, Object> entry : detail.entrySet()) {
	                columnNames.append(entry.getKey()).append(", ");
	                columnValues.append("'").append(entry.getValue()).append("', ");
	            }

	            columnNames.setLength(columnNames.length() - 2);  
	            columnValues.setLength(columnValues.length() - 2);

	            String sql = "INSERT INTO " + tableName + " (" + columnNames + ") VALUES (" + columnValues + ")";
	            jdbcTemplate.execute(sql);
	            rowsInserted++;
	        }
	    } catch (Exception e) {
	        throw new RuntimeException("Failed to save data. Transaction is rolled back.", e);
	    }
	    return rowsInserted;
	}


	// For updating the existed user.
	public int updateUser(User user) {
		String query = "UPDATE user SET name = ?, password = aes_encrypt(?, ?), designation = ?, department = ?, role = ?, "
				+ "user_key = ?, contact_no = ?, email_id = ?, report_manager = ?, birth_date = ?, anniversary_date = ?, "
				+ "gender = ?, organization = ?, location = ?, type = ?, active = ? WHERE id = ?";

		// Execute the update query
		int rowsAffected = jdbcTemplate.update(query, user.getName(), user.getPassword(), env.SECRET_KEY,
				user.getDesignation(), user.getDepartment(), user.getRole(), user.getUserKey(), user.getContactNo(),
				user.getEmailId(), user.getReportManager(), user.getBirthDate(), user.getAnniversaryDate(),
				user.getGender(), user.getOrganization(), user.getLocation(), user.getType(), user.isActive(), user.getId());

		return rowsAffected;
	}
	
	public double getAvailableLeaves(int year, String userId, String code) {
		String query = "select sum(number_of_days * if(leave_operation_type = 'Credit', 1, -1)) as available "
				+ "from hrms.leave_position where `year` = ? "
				+ "and user_id = ? and (`status` = 'Approved' or (leave_operation_type = 'Debit' and `status` = 'Pending')) "
				+ "and `code` = ?";

		  Double result = jdbcTemplate.queryForObject(query, Double.class, year, userId, code);
		  return result != null ? result : 0.0;
	}
	
	public double getAllApprovedLeaves(int year, String userId, String code) {
	    String query = "select sum(number_of_days) as approved from hrms.leave_position where year = ? "
	            + "AND user_id = ? AND status = 'Approved' AND leave_operation_type = 'Debit' AND code = ?";

	    Double result = jdbcTemplate.queryForObject(query, Double.class, year, userId, code);
	    return result != null ? result : 0.0;
	}

	//Getting all the pending leaves.
	public double getAllPendingLeaves(int year, String userId, String code) {
	    String query = "select sum(number_of_days) as pending from hrms.leave_position where `year` = ? "
				+ "and user_id = ?  and `status` = 'Pending' and leave_operation_type = 'Debit' and `code` = ?";

	    Double result = jdbcTemplate.queryForObject(query, Double.class, year, userId, code);
		  return result != null ? result : 0.0;
	}
	
	
	
	//For loading the operation master Data
	public List<OperationMaster> loadOperation(String opCode) {
		String query = "select op_code opCode, op_name opName, op_type opType, op_desc opDesc, params params, target target from hrms.operation_master where op_code = ? And active = true";
		return jdbcTemplate.query(query, new BeanPropertyRowMapper<>(OperationMaster.class), opCode);
		
	}
	
	//For loading grid Config Data.
	public List<GridConfig> loadScreen(String name) {
		String query = "select screen_name screenName, page_title pageTitle, params, actions from hrms.grid_config where screen_name = ? And active = true";
		return jdbcTemplate.query(query, new BeanPropertyRowMapper<>(GridConfig.class), name);
		
	}
	
	//For loading Entity Type Config Data.
	public List<EntityTypeMaster> loadEntityConfig(String typeId) {
		String query = "select c_type_id typeId, c_type_name typeName, c_parent_type parentName, form_config formConfig,screen_config screenConfig, active active from hrms.entity_type_master where c_type_id = ? And active = true";
		return jdbcTemplate.query(query, new BeanPropertyRowMapper<>(EntityTypeMaster.class), typeId);
	}
	
	//For generating the leaveId when any new leave raised.
	public String fetchNextSequenceIdForSequence(String sequence){
		String query="SELECT getSequence('" + sequence + "')";
		return sequence + StringUtils.leftPad(String.valueOf(jdbcTemplate.queryForObject(query, Long.class)), 8, '0');
	}
	public List<Map<String, Object>> fetchGridData(String screenName, String params, int pageNumber, int pageSize) {
		List<SqlParameter> parameters =  Arrays.asList(
                new SqlParameter(Types.VARCHAR),
                new SqlParameter(Types.VARCHAR));
				new SqlParameter(Types.INTEGER);
				new SqlParameter(Types.INTEGER);
        
        
        Map<String, Object> result = jdbcTemplate.call(new CallableStatementCreator() {
            @Override
            public CallableStatement createCallableStatement(Connection con) throws SQLException {
                CallableStatement callableStatement = con.prepareCall("{call getGridData (?, ?, ?, ?)}");
                callableStatement.setString(1, screenName);
                callableStatement.setString(2, params);
                callableStatement.setInt(3, pageNumber);
                callableStatement.setInt(4, pageSize);
                return callableStatement;
            }
        }, parameters);
        List<Map<String, Object>> data =  (List<Map<String, Object>>) result.get("#result-set-1");
        if (result.containsKey("#result-set-2")) {
        	List<Map<String, Object>> rowCount =  (List<Map<String, Object>>) result.get("#result-set-2");
        	if (data != null) {
        		data.add(0, rowCount.get(0));
        	}
        }
        return data;
	}

	public EntityTypeMaster checkDefineId(String typeId) {
	    String query = "SELECT * FROM entity_type_master WHERE c_type_id = ?";
	    List<EntityTypeMaster> results = jdbcTemplate.query(query, new BeanPropertyRowMapper<>(EntityTypeMaster.class), typeId);
	    if (results.isEmpty()) {
	        return null;  
	    } else {
	        return results.get(0);
	    }
	}

	
	public int insertDataModelEntry(EntityTypeMaster typeMaster) {
		/*
		 * public int[] batchUpdate(final List<Actor> actors) { List<Object[]> batch =
		 * new ArrayList<>(); for (Actor actor : actors) { Object[] values = new
		 * Object[] { actor.getFirstName(), actor.getLastName(), actor.getId()};
		 * batch.add(values); } return this.jdbcTemplate.batchUpdate(
		 * "update t_actor set first_name = ?, last_name = ? where id = ?", batch); }
		 */
	    String query = "INSERT INTO type_data_model (type, field_id, field_name, data_type, mandatory, active) VALUES (?, ?, ?, ?, ?, ?)";
	    
	    String tableName = "type_data" + "_" + typeMaster.getTypeId();

	    List<Object[]> batchArgs = Arrays.asList(
	            new Object[]{typeMaster.getTypeId(), "id", "Id", "text", 1, 1},
	            new Object[]{typeMaster.getTypeId(), "name", "Name", "text", 1, 1}
	    );
	    
	    int[] insertDataResults = jdbcTemplate.batchUpdate(query, batchArgs);

		// Check if at least one row was inserted
		int rowsInserted = Arrays.stream(insertDataResults).sum();
		if (rowsInserted > 0) {
			String sql = "CREATE TABLE IF NOT EXISTS `" + tableName + "` (" + "id VARCHAR(255) NOT NULL PRIMARY KEY, "
					+ "name VARCHAR(255) NOT NULL)";
			jdbcTemplate.execute(sql);

		}
		return rowsInserted;
	}
	public int addDataModelEntry(List<TypeDataModel> typeDataModel) {
		/*
		 * public int[] batchUpdate(final List<Actor> actors) { List<Object[]> batch =
		 * new ArrayList<>(); for (Actor actor : actors) { Object[] values = new
		 * Object[] { actor.getFirstName(), actor.getLastName(), actor.getId()};
		 * batch.add(values); } return this.jdbcTemplate.batchUpdate(
		 * "update t_actor set first_name = ?, last_name = ? where id = ?", batch); }
		 */
 
	    String query = "INSERT INTO type_data_model (type, field_id, field_name, data_type, mandatory, active) VALUES (?, ?, ?, ?, ?, ?)";
	    List<Object[]> batch = new ArrayList<>();
	    String columnsToAdd = "";
	    for(int i = 0;i<typeDataModel.size();i++) {
	    	Object[] values = new Object[] { typeDataModel.get(i).getType(), typeDataModel.get(i).getFieldId(), typeDataModel.get(i).getFieldName(), typeDataModel.get(i).getDataType(), typeDataModel.get(i).isMandatory(), typeDataModel.get(i).isActive()};
	    	if(i == 0) {
	    		columnsToAdd +=  "(" +typeDataModel.get(i).getFieldId() + " "+typeDataModel.get(i).getDataType() + " "+ (typeDataModel.get(i).isMandatory()? "NOT NULL" :"")+", ";
	    	} else if (i != typeDataModel.size() -1){
	    		columnsToAdd +=  typeDataModel.get(i).getFieldId() + " "+typeDataModel.get(i).getDataType() + " "+ (typeDataModel.get(i).isMandatory()? "NOT NULL" :"")+", ";
	    	} else {
	    		columnsToAdd +=  typeDataModel.get(i).getFieldId() + " "+typeDataModel.get(i).getDataType() + " "+ (typeDataModel.get(i).isMandatory()? "NOT NULL" :"")+" )";
	    	}
	    	
	    	batch.add(values); 
	    }
	    
	    String tableName = "type_data" + "_" + typeDataModel.get(0).getType();

	    
	    int[] insertDataResults = jdbcTemplate.batchUpdate(query, batch);

		// Check if at least one row was inserted
		int rowsInserted = Arrays.stream(insertDataResults).sum();
		if (rowsInserted > 0) {
			String sql = "ALTER TABLE `" + tableName + "` ADD  "+ columnsToAdd;
			jdbcTemplate.execute(sql);

		}
		return rowsInserted;
	}

	public List<TypeDataModel> getAllTypesDataModel() {
		String query = "select * from type_data_model";
		return jdbcTemplate.query(query, new BeanPropertyRowMapper<>(TypeDataModel.class));
	}

	public List<Map<String, Object>> getAllDataModelValues(String tableName) {
		// TODO Auto-generated method stub
		String query = "select *  from type_data_"+tableName+"";
		return jdbcTemplate.queryForList(query) ;
	}



}
	
