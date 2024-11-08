package com.np.hrms.services;
import com.np.hrms.entities.LeavePosition;
import com.np.hrms.entities.LeaveRequest;
import com.np.hrms.model.UserLoginInfo;
import com.np.hrms.repositories.LeavePositionRepository;
import com.np.hrms.repositories.LeaveRequestRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import java.util.Date;

@Service
public class LeaveActionService {
	
	@Autowired
	private LeaveRequestRepository leaveRequestRepository;
	
	@Autowired
	private LeavePositionRepository leavePositionRepository;
	
	// For updating LeaveRequest Status from pending to denied or Approve.
	public LeaveRequest updateLeaveRequest(UserLoginInfo user, String leaveId, String status, String remarks) {
		/*
		 * String additionalRemarks = (remarks != null && !remarks.isEmpty()) ?
		 * ", Remarks: " + remarks : ""; String finalRemarks = null; if
		 * (status.equalsIgnoreCase("Approved")) { finalRemarks = "Leave Approved By: "
		 * + user.getUserName() + " (" + user.getUserId() + ")" + additionalRemarks; }
		 * else if(status.equalsIgnoreCase("Declined")) { finalRemarks =
		 * "Leave Declined By: " + user.getUserName() + " (" + user.getUserId() + ")" +
		 * additionalRemarks; }
		 */
	  Date currentDate = new Date();
	  LeaveRequest leaveRequest = updateInLeaveRequest(leaveId, user, status, currentDate, remarks);
	  String posLeaveId = leaveRequest.getId();
	  updateInLeavePosition(posLeaveId, user, status, currentDate, remarks);
	  return leaveRequest;
    }

	//Updating the leave status in LeaveRequest Table with remarks after Manger activity.
	private LeaveRequest updateInLeaveRequest(String leaveId, UserLoginInfo user, String status, Date currentDate, String remarks) {
		LeaveRequest leaveRequest = leaveRequestRepository.findLeaveById(leaveId);
		leaveRequest.setStatus(status);
		leaveRequest.setRemarks(remarks);
		leaveRequest.setUpdatedBy(user.getUserId());
		leaveRequest.setUpdatedOn(currentDate);
		leaveRequestRepository.save(leaveRequest);
		return leaveRequest;
	}

	//Updating the leave status in LeavePosition Table with remarks after Manger activity.
	private void updateInLeavePosition(String leaveId, UserLoginInfo user, String status, Date currentDate, String remarks) {
		LeavePosition  leavePosition = leavePositionRepository.findLeaveById(leaveId);
		leavePosition.setStatus(status);
		leavePosition.setRemarks(remarks);
		leavePosition.setUpdatedBy(user.getUserId());
		leavePosition.setUpdatedOn(currentDate);
		leavePositionRepository.save(leavePosition);
	}
}
