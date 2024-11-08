package com.np.hrms.controllers;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import com.np.hrms.auth.Secured;
import com.np.hrms.entities.HolidayMaster;
import com.np.hrms.entities.LeaveMaster;
import com.np.hrms.entities.LeaveRequest;
import com.np.hrms.enums.Role;
import com.np.hrms.exceptions.HRMSException;
import com.np.hrms.model.LeaveStatusBreakdown;
import com.np.hrms.model.UserLoginInfo;
import com.np.hrms.repositories.HolidayMasterRepository;
import com.np.hrms.repositories.LeaveMasterRepository;
import com.np.hrms.repositories.UserRepository;
import com.np.hrms.services.LeaveRequestService;

import jakarta.servlet.http.HttpServletRequest;

@RestController
@RequestMapping("/api")
public class LeaveRequestController {

	@Autowired
	private LeaveRequestService leaveRequestService;
	
	@Autowired
	private HolidayMasterRepository holidayMasterRepository;

	@Autowired
	private LeaveMasterRepository leaveMasterRepository;
	
	@Autowired 
    private UserRepository userRepository;
	
	@Autowired
	HttpServletRequest httpServletRequest;
	
	@Autowired
	private LoginRestController loginService;

	// Method for getting current Logged in User.
	private UserLoginInfo getCurrentUser() {
		String authToken = httpServletRequest.getHeader("auth-token");
		if (StringUtils.isNotBlank(authToken)) {
			return loginService.getUserLoginInfo(authToken);
		} else {
			return null;
		}
	}
	
	// For Showing the Leave Status on the User DashBoard.
	@GetMapping("/leave-status")
	@Secured({ Role.Admin, Role.User, Role.Manager })
	public ResponseEntity<Map<String, Object>> getLeaveStatus(@RequestParam int year) {
		UserLoginInfo user = getCurrentUser();
		List<LeaveStatusBreakdown> leaveBreakdown = leaveRequestService.getLeaveBreakdown(user.getUserId(), year);
		Map<String, Object> response = new HashMap<>();
		response.put("leaveBreakdown", leaveBreakdown);
		double approved = leaveRequestService.getTotalApprovedLeave(user.getUserId(), year);
		double available = leaveRequestService.getTotalAvailableLeave(user.getUserId(), year);
		double pending = leaveRequestService.getTotalPendingLeave(user.getUserId(), year);
		response.put("total", approved + available + pending );
		response.put("available", available);
		response.put("approved", approved) ;
		response.put("pending", pending);
		  return ResponseEntity.ok(response);
		}
	
	// Create a new leave request
	@PostMapping("/leave-request")
	@Secured({ Role.User })
	public LeaveRequest createLeaveRequest(@RequestBody LeaveRequest leaveRequest, @RequestParam int year) throws HRMSException {
	UserLoginInfo user = getCurrentUser();
	leaveRequestService.checkLeaveEligibility(user, year, leaveRequest.getCode() , leaveRequest.getFromDate() , leaveRequest.getToDate());
		return leaveRequestService.saveLeaveRequest(user, leaveRequest, year);
	}
	
	// For Checking the eligibility criteria to apply the leaves.
	@PostMapping("/checkLeave")
	@Secured({ Role.Manager, Role.User, Role.Admin })
	public ResponseEntity<Map<String, Object>> checkLeave(@RequestParam String type, @RequestParam int year, @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) Date fromDate,
			@DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) Date toDate) {
		UserLoginInfo user = getCurrentUser();
		Map<String, Object> response = leaveRequestService.checkLeaveEligibility(user, year, type, fromDate, toDate);
		return ResponseEntity.ok(response);
	}
    
	// For Finding the leave Types from the Leave Master table.
	@GetMapping("/leave-types")
	@Secured({ Role.Admin, Role.User, Role.Manager })
	public Map<String, Object> getAllLeaveTypes(@RequestParam("year") int year) {
		UserLoginInfo user = getCurrentUser();
		List<LeaveMaster> leaveTypes = leaveMasterRepository.findAllLeaves(user.getOrganization(), year);
		List<HolidayMaster> fixedHolidays = holidayMasterRepository.getAllFixedHolidaysForCurrentYear(year);
		List<HolidayMaster> flexiHolidays = holidayMasterRepository.findAllFlexiLeavesForYear(year);
		
		Map<String, Object> map = new HashMap<String, Object>();
		map.put("leaveTypes", leaveTypes);
		map.put("fixedHolidays", fixedHolidays);
		map.put("flexiHolidays", flexiHolidays);
		return map;
	}
	
	// Cancel leave request by requestId
	@PutMapping("/leave-request/cancel-leave-request/{id}")
	@Secured({ Role.User })
	public ResponseEntity<?> cancelLeaveRequest(@PathVariable("id") String id) {
		try {
			// Call service to cancel leave request by requestId
			leaveRequestService.cancelLeaveRequest(id);
			return ResponseEntity.ok().build();
		} catch (Exception e) {
			return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body("Error cancelling leave request.");
		}
	}

	// Getting all leaveDates to show in the calendar Applied by employees.
	@GetMapping("/leaveDates")
	@Secured({ Role.User })
	public ResponseEntity<List<Date>> getLeaveRequestsByStatus(@RequestParam String status, @RequestParam int year, @RequestParam String userId) {
		UserLoginInfo user = getCurrentUser();
		List<Date> validLeaveDates = leaveRequestService.getLeaveRequestsByStatus(user, status, year, userId);
		return ResponseEntity.ok(validLeaveDates);
	}
		 
	// For Getting All Company holiday(Fixed Holidays)
	@GetMapping("/fixed-holidays")
	@Secured({ Role.Manager, Role.User, Role.Admin })
	public List<HolidayMaster> getFixedHolidays(@RequestParam(name = "year") int currentYear) {
		return holidayMasterRepository.getAllFixedHolidaysForCurrentYear(currentYear);
	}

	// Getting all Flexi Days Count.
	@GetMapping("/allFlexiLeaves")
	@Secured({ Role.Admin, Role.User, Role.Manager })
	public List<HolidayMaster> getFlexiLeaveDays(@RequestParam int year) {
		return holidayMasterRepository.findAllFlexiLeavesForYear(year);
	}
    
   //For Getting Flexi Limit.
	@GetMapping("/flexi-limit")
	@Secured({ Role.User, Role.Admin, Role.Manager })
	public String getFlexiLimitForYear() {
		return Double.toString(leaveMasterRepository.getDaysLimitByLeaveType("Flexi"));
	}	
	
	// For Getting the report Manager of logged In employee.
	@GetMapping("/reportManager")
	@Secured({ Role.Admin, Role.Manager, Role.User })
	public ResponseEntity<Map<String, String>> getEmployeeNameOfEmp() throws HRMSException {
		UserLoginInfo user = getCurrentUser();
		String ApproverId = leaveRequestService.getApproverId(user.getReportManager());
		String ApproverName = userRepository.getApproverNameById(ApproverId);
		// Wrap the string in a JSON object
		Map<String, String> response = new HashMap<>();
		response.put("ApproverId", ApproverId);
        response.put("ApproverName", ApproverName);
		return ResponseEntity.ok(response);
	}
}
