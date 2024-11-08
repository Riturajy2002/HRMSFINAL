package com.np.hrms.controllers;

import java.io.IOException;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

import com.np.hrms.auth.Secured;
import com.np.hrms.dto.MonthlyReportDTO;
import com.np.hrms.entities.OperationMaster;
import com.np.hrms.entities.User;
import com.np.hrms.enums.Role;
import com.np.hrms.model.GridDTO;
import com.np.hrms.model.OperationDTO;
import com.np.hrms.model.OperationResponse;
import com.np.hrms.model.UserLoginInfo;
import com.np.hrms.repositories.OperationMasterRepository;
import com.np.hrms.repositories.SqlDAO;
import com.np.hrms.repositories.UserRepository;
import com.np.hrms.services.AttendanceReportService;
import com.np.hrms.services.ExcelReportService;
import com.np.hrms.services.GridConfigService;
import com.np.hrms.services.OperationService;

import jakarta.servlet.http.HttpServletRequest;

@RestController
@RequestMapping("/api")
public class SuperAdminController {

	@Autowired
	private UserRepository userRepository;

	@Autowired
	private SqlDAO sqlDao;
    
	@Autowired
	private LoginRestController loginService;
	
	@Autowired
	private OperationService operationService;
	
	@Autowired
	private ExcelReportService excelReportService;

	@Autowired
	private LoginRestController loginRestController;

	@Autowired
	private AttendanceReportService attendanceReportService;
	
	@Autowired OperationMasterRepository operationMasterRepository;

	@Autowired
	private GridConfigService gridConfigService;

	@Autowired
	HttpServletRequest httpServletRequest;
	
	//For Getting the current User.
	private UserLoginInfo getCurrentUser() {
		String authToken = httpServletRequest.getHeader("auth-token");
		if (StringUtils.isNotBlank(authToken)) {
			return loginService.getUserLoginInfo(authToken);
		} else {
			return null;
		}
	}
	
	// Register for a new user
	@PostMapping("/super-admin/register")
	@Secured({ Role.Admin })
	public ResponseEntity<Map<String, String>> registerNewUser(@RequestBody User user) {
		Map<String, String> response = new HashMap<>();

		// Check User Exist.
		boolean userIdExists = userRepository.findByUserId(user.getUserId()) != null;
		boolean emailExists = userRepository.findByEmailId(user.getEmailId()).isPresent();
		boolean contactNoExists = userRepository.findByContactNo(user.getContactNo()).isPresent();
		boolean idExists = userRepository.findById(user.getId()).isPresent();
		if (userIdExists || emailExists || contactNoExists || idExists) {
			if (userIdExists)
				response.put("userId", "User ID already exists.");
			if (emailExists)
				response.put("email", "Email ID already exists.");
			if (contactNoExists)
				response.put("contactNo", "Contact Number already exists.");
			if (idExists)
				response.put("id", "Employee ID already exists.");
			return ResponseEntity.badRequest().body(response);
		}

		// If user does not exist, save the new user
		int noOfRows = sqlDao.saveUser(user);
		if (noOfRows > 0) {
			loginRestController.refreshUsers();
			response.put("success", "User Registered Succesfully");
			return ResponseEntity.ok(response);
		} else {
			response.put("error", "Failed to register the user.");
			return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(response);
		}
	}

    
	// For updating the registered user.
	@PutMapping("/super-admin/update")
	@Secured({ Role.Admin })
	public ResponseEntity<User> updateUser(@RequestBody User user) {
		int rowsAffected = sqlDao.updateUser(user);
		if (rowsAffected > 0) {
			User updatedUser = userRepository.findById(user.getId()).orElse(null);
			loginRestController.refreshUsers();
			return ResponseEntity.ok(updatedUser);
		} else {
			return ResponseEntity.status(HttpStatus.NOT_FOUND).body(null);
		}
	}

	// For uploading the excel to fetching the Data.
	@PostMapping("/upload-attendanceSheet")
	@Secured({ Role.Admin })
	public ResponseEntity<Map<String, String>> uploadAttandenceSheet(@RequestParam("file") MultipartFile file) {
		if (file.isEmpty()) {
			return ResponseEntity.status(HttpStatus.BAD_REQUEST)
					.body(Collections.singletonMap("message", "Please select a file!"));
		}
		try {
			attendanceReportService.processExcelFile(file);
			return ResponseEntity.status(HttpStatus.OK)
					.body(Collections.singletonMap("message", "File uploaded and processed successfully"));
		} catch (Exception e) {
			return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
					.body(Collections.singletonMap("message", "Failed to process the file: " + e.getMessage()));
		}
	}

	// For Downloading report By Selection Month and Year
	@GetMapping("/super-admin/download-report")
	@Secured({ Role.Admin })
	public ResponseEntity<byte[]> getMonthlyReport(@RequestParam("month") int month, @RequestParam("year") int year) {
		byte[] excelData;
		try {
			excelData = excelReportService.generateMonthlyReport(month, year);
		} catch (IOException e) {
			return ResponseEntity.status(500).body(null);
		}

		return ResponseEntity.ok()
				.header("Content-Disposition", "attachment; filename=User_Report_" + year + "_" + month + ".xlsx")
				.body(excelData);
	}

	// Fetching the monthly report to show on the UI
	@GetMapping("/super-admin/fetch-report")
	@Secured({ Role.Admin })
	public List<MonthlyReportDTO> fetchMonthlyReport(@RequestParam("month") int month, @RequestParam("year") int year) {
		return attendanceReportService.fetchMonthlyReport(month, year);
	}

	// Finding the user Details for the updation.
	@GetMapping("/employeeDetails")
	@Secured({ Role.Admin })
	public User getEmployeeDetails(@RequestParam("userId") String userId) {
		User user = userRepository.findUserById(userId);
		String decryptedPassword = userRepository.findDecryptedPasswordById(userId);
		user.setPassword(decryptedPassword);
		return user;
	}
	
	// For Getting the all the operations/employeeDetails/
	@GetMapping("/fetchEmployeeOps")
	@Secured({ Role.Admin, Role.User, Role.Manager })
	public List<OperationMaster> getEmployeeOperations() {
		return operationMasterRepository.getEmployeeOperations();
	}
	
	//For Getting the operation params.
	@GetMapping("/operation")
	@Secured({ Role.Admin })
	public OperationDTO getOperation(@RequestParam("opCode") String opCode) {
		 return operationService.loadOperation(opCode);
	}
   
	//For performing the Employee Operation.
	@PostMapping("/performOperation")
	public OperationResponse performOperation(@RequestHeader("auth-token") String authToken, @RequestBody OperationDTO actionDTO) {
		UserLoginInfo user	= loginService.getUserLoginInfo(authToken);
		OperationResponse operationResponse =  operationService.performOperation(user, actionDTO, null);
	    return operationResponse;
	}
	
	//For Getting the Grid config.
	@GetMapping("/getGridConfig")
	@Secured({ Role.Admin, Role.User, Role.Manager })
	public GridDTO getGridConfig(@RequestParam String screenName) {
		UserLoginInfo user = getCurrentUser();
		
		GridDTO gridConfigDTO = gridConfigService.loadScreenConfig(screenName);
		return gridConfigDTO;
	}
	
	//For getting all the Grid Data.
	@PostMapping("/getGridData")
	@Secured({ Role.Admin, Role.User, Role.Manager })
	public List<Map<String, Object>> getFilteredLeaveRequests(@RequestBody GridDTO gridConfigDTO) {
		 List<Map<String, Object>> data = sqlDao.fetchGridData(gridConfigDTO.getScreenName(), gridConfigDTO.getParamsValues(), gridConfigDTO.getPageNo(),gridConfigDTO.getPageSize());
		return data;
	}
}
