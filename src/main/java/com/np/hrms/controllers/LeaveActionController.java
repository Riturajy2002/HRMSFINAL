package com.np.hrms.controllers;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import com.np.hrms.auth.Secured;
import com.np.hrms.entities.LeaveRequest;
import com.np.hrms.enums.Role;
import com.np.hrms.model.LeaveStatusBreakdown;
import com.np.hrms.model.UserLoginInfo;
import com.np.hrms.services.LeaveActionService;
import com.np.hrms.services.LeaveRequestService;

import jakarta.servlet.http.HttpServletRequest;

@RestController
@RequestMapping("/api")
public class LeaveActionController {

	@Autowired
	private LeaveActionService leaveActionService;

	@Autowired
	private LeaveRequestService leaveRequestService;
	
	@Autowired
	HttpServletRequest httpServletRequest;
	
	@Autowired
	private LoginRestController loginService;
	
	//Method for getting current Logged in User.
	private UserLoginInfo getCurrentUser() {
		String authToken = httpServletRequest.getHeader("auth-token");
		if (StringUtils.isNotBlank(authToken)) {
			return loginService.getUserLoginInfo(authToken);
		} else {
			return null;
		}
	}
	
	// For Showing the Leave Status on the Leave Action DashBoard.
	@GetMapping("/get-leaves")
	@Secured({ Role.Admin, Role.User, Role.Manager })
	public ResponseEntity<Map<String, Object>> getLeavesForApproval(@RequestParam("userId") String userId, @RequestParam int year) {
		List<LeaveStatusBreakdown> leaveBreakdown = leaveRequestService.getLeaveBreakdown(userId, year);
	    Map<String, Object> response = new HashMap<>();
	    response.put("leaveBreakdown", leaveBreakdown);
	    double approved = leaveRequestService.getTotalApprovedLeave(userId, year);
	    double available = leaveRequestService.getTotalAvailableLeave(userId, year);
	    double pending = leaveRequestService.getTotalPendingLeave(userId, year);
	    response.put("total", approved + available + pending );
	    response.put("available", available);
	    response.put("approved", approved) ;
	    response.put("pending", pending);
	    return ResponseEntity.ok(response);
	}
    
	// Update leave request status from pending to denied or approved by the manager
	@PostMapping("/leave-request/update")
	@Secured({ Role.Manager })
	public LeaveRequest updateLeaveRequest(@RequestParam("leaveId") String leaveId,
			@RequestParam("status") String status, @RequestParam("remarks") String remarks) {
		UserLoginInfo user = getCurrentUser();
		return leaveActionService.updateLeaveRequest(user, leaveId, status, remarks);
	}
}
