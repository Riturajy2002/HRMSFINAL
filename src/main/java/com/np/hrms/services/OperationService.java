package com.np.hrms.services;
import java.util.Date;
import java.util.List;
import java.util.Map;

import org.json.JSONException;
import org.json.JSONObject;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.google.gson.Gson;
import com.np.hrms.entities.AuditLog;
import com.np.hrms.entities.LeavePosition;
import com.np.hrms.entities.OperationMaster;
import com.np.hrms.model.OperationDTO;
import com.np.hrms.model.OperationResponse;
import com.np.hrms.model.Param;
import com.np.hrms.model.UserLoginInfo;
import com.np.hrms.repositories.AuditLogRepository;
import com.np.hrms.repositories.LeaveRequestRepository;
import com.np.hrms.repositories.RefMasterRepository;
import com.np.hrms.repositories.SqlDAO;

@Service
public class OperationService {
	
	@Autowired 
	private SqlDAO sqlDao;
	
	@Autowired
	private RefMasterRepository refMasterRepository;
	
	@Autowired
	private AuditLogRepository auditLogRepository;
	
	@Autowired 
	private LeaveRequestRepository leaveRequestRepository;
	
	public OperationDTO loadOperation(String opCode) {
		Gson gsonObj = new Gson();
		OperationMaster opMaster = sqlDao.loadOperation(opCode).get(0);
		String paramBody = opMaster.getParams();
		OperationDTO opDto = gsonObj.fromJson(paramBody, OperationDTO.class);
        
		opDto.setOpName(opMaster.getOpName());
		opDto.setOpCode(opMaster.getOpCode());
		opDto.setOpType(opMaster.getOpType());
		opDto.setOpDesc(opMaster.getOpDesc());
		
		
		for(String name : opDto.getParams().keySet()) {
			Param pr = opDto.getParams().get(name);
			if(pr.getSourceType().equals("ref_master")) {
				opDto.getRefData().put(pr.getSourceId(), refMasterRepository.fetchRefData(pr.getSourceId(), null));
			}
			
		}
		//opDto.getRefData().put("year", refMasterRepository.fetchRefData("year", null));
		return opDto;

	}
	
	
	public OperationResponse performOperation(UserLoginInfo user, OperationDTO opDto, String serviceName) {
		Map<String, Param> prs = opDto.getParams();
		List<String> selectedUserIds = opDto.getSelectedUserIds();
		int year = Integer.parseInt(opDto.getYear());
		String reason = opDto.getReason();
		OperationResponse result = null;
		Long auditId = addOpInAuditLog(user, selectedUserIds, opDto.getParams(),
				 year, reason, opDto.getOpCode(), serviceName);
		switch (opDto.getOpName()) {
		case "add_remove_leave":
			String leaveType = prs.get("leave_type").getValue();
			String action = prs.get("action").getValue();
			int quantity = Integer.parseInt(prs.get("quantity").getValue());
			result = addRemoveLeave(user,selectedUserIds, leaveType,
					quantity, action, year, reason, auditId);
			break;
		}
		auditLogRepository.updateStatus(auditId, result.isError() ? "error" : ("success"));
		return result;
	}
	
	


	private Long addOpInAuditLog(UserLoginInfo user, List<String> selectedUserIds,  Map<String, Param> params,
			int year, String reason, String opCode, String serviceName) {
		
		JSONObject opParam = new JSONObject();
		Date currentDate = new Date();
		AuditLog auditLog = new AuditLog();
		auditLog.setOpCode(opCode);
		
		for(String key: params.keySet()) {
			try {
				opParam.put(key, params.get(key).getValue());
				opParam.put("users", selectedUserIds);
			} catch (JSONException e) {
				e.printStackTrace();
			}
        }
		
		auditLog.setOpParams(opParam.toString());
		
		auditLog.setReason(reason);
		auditLog.setYear(year);
		auditLog.setUpdatedBy(user.getUserId());
		auditLog.setUpdatedOn(currentDate);
		auditLog.setServiceName(serviceName);
		auditLog = auditLogRepository.saveAndFlush(auditLog);
		return auditLog.getId();

		
	}
	
	private OperationResponse addRemoveLeave(UserLoginInfo user, List<String> selectedUserIds, String code, int quantity, String action, int year, String reason, Long auditId) {
		Date currentDate = new Date();
		for (String userId : selectedUserIds) {
			LeavePosition leaveRequest = new LeavePosition();
			leaveRequest.setUserId(userId);
			leaveRequest.setCode(code);
			leaveRequest.setLeaveOperationType(action);
			leaveRequest.setNumberOfDays(quantity);
			leaveRequest.setStatus("Approved");
			leaveRequest.setReason(reason);
			leaveRequest.setYear(year);
			leaveRequest.setUpdatedBy(user.getUserId());
			leaveRequest.setUpdatedOn(currentDate);
			leaveRequest.setAuditId(auditId);
			leaveRequestRepository.save(leaveRequest);
		}
		OperationResponse operationResponse = new OperationResponse();
		operationResponse.setMessage("Leaves " + (action.equals("Credit") ? "Added" : "Removed") + (" successfully"));
	    return operationResponse;
	}

	
}
