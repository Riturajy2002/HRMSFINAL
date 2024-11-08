package com.np.hrms.services;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import org.apache.commons.lang3.time.DateUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import com.np.hrms.dto.LeaveRequestDTO;
import com.np.hrms.entities.HolidayMaster;
import com.np.hrms.entities.LeaveMaster;
import com.np.hrms.entities.LeavePosition;
import com.np.hrms.entities.LeaveRequest;
import com.np.hrms.entities.User;
import com.np.hrms.enums.Role;
import com.np.hrms.exceptions.HRMSException;
import com.np.hrms.model.LeaveStatusBreakdown;
import com.np.hrms.model.UserLoginInfo;
import com.np.hrms.repositories.HolidayMasterRepository;
import com.np.hrms.repositories.LeaveMasterRepository;
import com.np.hrms.repositories.LeavePositionRepository;
import com.np.hrms.repositories.LeaveRequestRepository;
import com.np.hrms.repositories.SqlDAO;
import com.np.hrms.repositories.UserRepository;
import com.np.hrms.utils.DateConverter;

@Service
public class LeaveRequestService {

	@Autowired
	private LeaveRequestRepository leaveRequestRepository;

	@Autowired
	private UserRepository userRepository;

	@Autowired
	private HolidayMasterRepository holidayMasterRepository;

	@Autowired
	private LeaveMasterRepository leaveMasterRepository;

	@Autowired
	private SqlDAO sqlDao;
	
	@Autowired
	private LeavePositionRepository leavePositionRepository;

	public LeaveRequestService(LeaveRequestRepository leaveRequestRepository) {
		this.leaveRequestRepository = leaveRequestRepository;
	}
	
	// Save applied leave requests by a user.
	public LeaveRequest saveLeaveRequest(UserLoginInfo user, LeaveRequest request, int year) throws HRMSException {
		String requestId = sqlDao.fetchNextSequenceIdForSequence("LEV");
		Date currentDate = new Date();
		LeaveRequest leaveRequest = createLeaveRequestEntity(requestId, user, request, year, currentDate);
		String leaveId = leaveRequest.getId();
		createAndSaveLeavePosition(leaveId, user, request, year, currentDate);
		return leaveRequest;

	}
    
	private LeaveRequest createLeaveRequestEntity(String requestId, UserLoginInfo user, LeaveRequest request, int year, Date currentDate)
			throws HRMSException {
		LeaveRequest leaveRequest = new LeaveRequest();
		leaveRequest.setId(requestId);
		leaveRequest.setUserId(user.getUserId());
		leaveRequest.setManager(findManagerForLeaveApproval(user.getReportManager()));
		leaveRequest.setCode(request.getCode());
		leaveRequest.setAppliedDate(request.getAppliedDate());
		leaveRequest.setFromDate(request.getFromDate());
		leaveRequest.setToDate(request.getToDate());
		leaveRequest.setNumberOfDays(request.getNumberOfDays());
		leaveRequest.setReason(request.getReason());
		leaveRequest.setStatus("Pending");
		leaveRequest.setYear(year);
		leaveRequest.setUpdatedBy(user.getUserId());
		leaveRequest.setUpdatedOn(currentDate);
		leaveRequest = leaveRequestRepository.saveAndFlush(leaveRequest);
		return leaveRequest;
	}
    
	private void createAndSaveLeavePosition(String leaveId, UserLoginInfo user, LeaveRequest request, int year, Date currentDate) {
		LeavePosition leavePosition = new LeavePosition();
		leavePosition.setUserId(user.getUserId());
		leavePosition.setLeaveId(leaveId);
		leavePosition.setCode(request.getCode());
		leavePosition.setLeaveOperationType("Debit");
		leavePosition.setNumberOfDays(request.getNumberOfDays());
		leavePosition.setReason(request.getReason());
		leavePosition.setStatus("Pending");
		leavePosition.setRemarks(request.getRemarks());
		leavePosition.setYear(year);
		leavePosition.setUpdatedBy(user.getUserId());
		leavePosition.setUpdatedOn(currentDate);
		leavePositionRepository.save(leavePosition);
	}
 
	public String getApproverId(String managerId) throws HRMSException {
		return findManagerForLeaveApproval(managerId);
	}
	
	private String findManagerForLeaveApproval(String managerId) throws HRMSException {
		User manager = userRepository.findByUserId(managerId);

		if (manager == null) {
			throw new HRMSException("Manager is not allocated for your account. Please contact HR.");
		} else if (manager.getRole().contains(Role.Manager.name())) {
			return managerId;
		}
		return findManagerForLeaveApproval(manager.getReportManager());
	}

	// For Canceling the leave Request till the manager have not Approved or Declined.
	public void cancelLeaveRequest(String id) {
		leaveRequestRepository.cancelRequestById(id);
		leavePositionRepository.cancelRequestByLeaveId(id);
	}

	// For getting the Total Available Leave
	public double getTotalAvailableLeave(String userId, int year) {
		List<LeaveMaster> leaveMasterList = leaveMasterRepository.findAll();
		double totalAvailable = 0;
		for (LeaveMaster leaveMaster : leaveMasterList) {
			String code = leaveMaster.getCode();
			double available = sqlDao.getAvailableLeaves(year, userId, code);
			totalAvailable += available;

		}
		return totalAvailable;
	}
    
	// For getting the total Approved Leaves.
	public double getTotalApprovedLeave(String userId, int year) {
		List<LeaveMaster> leaveMasterList = leaveMasterRepository.findAll();
		double totalApproved = 0;
		for (LeaveMaster leaveMaster : leaveMasterList) {
			String code = leaveMaster.getCode();
			double daysApproved = sqlDao.getAllApprovedLeaves(year, userId, code);
			totalApproved += daysApproved;
		}
		return totalApproved;
	}

	// For getting the total Declined leave Requests.
	public double getTotalPendingLeave(String userId, int year) {
		List<LeaveMaster> leaveMasterList = leaveMasterRepository.findAll();
		double totalPending = 0;
		for (LeaveMaster leaveMaster : leaveMasterList) {
			String code = leaveMaster.getCode();
			double totalPendingDays = sqlDao.getAllPendingLeaves(year, userId, code);
			totalPending += totalPendingDays;
		}
		return totalPending;
	}

	// Getting Available leave Breakdown.
	public List<LeaveStatusBreakdown> getLeaveBreakdown(String userId, int year) {
	    List<LeaveMaster> leaveMasterList = leaveMasterRepository.findAll();
	    Map<String, LeaveStatusBreakdown> leaveBreakdownMap = new HashMap<>();

	    for (LeaveMaster leaveMaster : leaveMasterList) {
	        String code = leaveMaster.getCode();
	        double availableDays = sqlDao.getAvailableLeaves(year, userId, code);
	        double approvedDays = sqlDao.getAllApprovedLeaves(year, userId, code);
	        double pendingDays = sqlDao.getAllPendingLeaves(year, userId, code);
	        LeaveStatusBreakdown breakdown = new LeaveStatusBreakdown();
	        breakdown.setTotal(availableDays + approvedDays + pendingDays); 
	        breakdown.setType(code);
	        breakdown.setAvailable(availableDays); 
	        breakdown.setApproved(approvedDays);
	        breakdown.setPending(pendingDays);
	        leaveBreakdownMap.put(code, breakdown);
	    }
	    List<LeaveStatusBreakdown> breakdownList = new ArrayList<>(leaveBreakdownMap.values());
	    breakdownList.sort((b1, b2) -> Double.compare(b2.getAvailable(), b1.getAvailable()));
	    return breakdownList; 
	}

	// Getting all leaveRequests Applied by employees To show the Calendars.
	public List<Date> getLeaveRequestsByStatus(UserLoginInfo user, String status, int year, String userId) {
		List<LeaveMaster> leaveMasterList = leaveMasterRepository.findAllLeaves(user.getOrganization(), year);
		List<HolidayMaster> holidayMasterList = holidayMasterRepository.getAllHolidays(year);

		Map<String, Map<Date, HolidayMaster>> holidays = new HashMap<>();
		for (HolidayMaster holidayMaster : holidayMasterList) {
			Date holidayDate = holidayMaster.getHolidayDate();
			holidays.putIfAbsent(holidayMaster.getType(), new HashMap<>());
			holidays.get(holidayMaster.getType()).put(holidayDate, holidayMaster);
		}

		Map<String, LeaveMaster> leaveTypes = new HashMap<>();
		for (LeaveMaster leaveMaster : leaveMasterList) {
			leaveTypes.put(leaveMaster.getCode(), leaveMaster);
		}

		List<LeaveRequest> leaveRequests = leaveRequestRepository.findAppliedLeaveRequest(status, year, userId);

		List<Date> validLeaveDates = new ArrayList<>();
		for (LeaveRequest leaveRequest : leaveRequests) {
			String leaveType = leaveRequest.getCode();
			Date fromDate = leaveRequest.getFromDate();
			Date toDate = leaveRequest.getToDate();

			LeaveMaster leaveMaster = leaveTypes.get(leaveType);
			if (leaveMaster == null) {
				continue;
			}

			Map<String, Object> calculationResult = calculateEffectiveLeaveDays(leaveMaster, fromDate, toDate,
					holidays);
			int totalLeaveApplyDays = (int) calculationResult.get("totalDays");

			if (totalLeaveApplyDays > 0) {
				List<Date> leaveDates = (List<Date>) calculationResult.get("validLeaveDates");
				validLeaveDates.addAll(leaveDates);
			}
		}
		return validLeaveDates;
	}



	// Mapping function to convert entity to DTO
	private LeaveRequestDTO mapToDTO(LeaveRequest leaveRequest) {
		LeaveRequestDTO dto = new LeaveRequestDTO();
		dto.setEmpId(leaveRequest.getId());
		dto.setUserId(leaveRequest.getUserId());
		dto.setFromDate(leaveRequest.getFromDate());
		dto.setToDate(leaveRequest.getToDate());
		dto.setStatus(leaveRequest.getStatus());
		return dto;
	}

	// For Checking the eligibility criteria to apply the leaves.
	public Map<String, Object> checkLeaveEligibility(UserLoginInfo user, int year, String code, Date fromDate,
			Date toDate) {

		Map<String, Object> response = new HashMap<>();

		// Check if the selected dates overlap with existing pending or approved
		List<LeaveRequest> overlappingRequests = leaveRequestRepository.findOverlappingLeaves(user.getUserId(),
				fromDate, toDate, Arrays.asList("Pending", "Approved"));

		if (fromDate ==  null || toDate == null) {
			response.put("message", "From/To leave date cannot be empty");
			return response;
		}
		
		if (!overlappingRequests.isEmpty()) {
			if (overlappingRequests.get(0).getStatus().equals("Approved")) {
				response.put("message", "Approved leave request exists for the selected dates.");
			} else if (overlappingRequests.get(0).getStatus().equals("Pending")) {
				response.put("message", "Pending leave request exists for the selected dates.");
			}
			return response;
		}

		// Checking Eligibility based on the leave Types.
		return checkLeave(user, year, code, fromDate, toDate, response);
	}

	private Map<String, Object> checkLeave(UserLoginInfo user, int year, String code, Date fromDate,
			Date toDate, Map<String, Object> response) {
        
		Map<String, LeaveMaster> leaveTypes = new HashMap<String, LeaveMaster>();
		
		
		Map<String, Map<Date, HolidayMaster>> holidays = new HashMap<String, Map<Date, HolidayMaster>>();

		List<LeaveMaster> leaveMasterList = leaveMasterRepository.findAllLeaves(user.getOrganization(), year);

		for (LeaveMaster leaveMaster : leaveMasterList) {
			leaveTypes.put(leaveMaster.getCode(), leaveMaster);
		}

		List<HolidayMaster> holidayMasterList = holidayMasterRepository.getAllHolidays(year);

		for (HolidayMaster holidayMaster : holidayMasterList) {
			// Assuming holidayMaster.getHolidayDate() returns a java.sql.Date
			Date holidayDate = holidayMaster.getHolidayDate();

			holidays.putIfAbsent(holidayMaster.getType(), new HashMap<Date, HolidayMaster>());
			holidays.get(holidayMaster.getType()).put(holidayDate, holidayMaster);
		}

		if (toDate.before(fromDate)) {
			response.put("message", "To date should be greater than from date.");
			return response;
		}

		Date firstDayOfYear = new Date(year - 1900, 0, 1);
		Date lastDayOfYear = new Date(year - 1900, 11, 31);

		if (fromDate.before(firstDayOfYear) || toDate.after(lastDayOfYear)) {
			response.put("message", "Leave application must be within the current year.");
			return response;
		}

		LeaveMaster leaveType = leaveTypes.get(code);

		if (leaveType == null
				|| (leaveType.getApplicableFor() != null && !leaveType.getApplicableFor().equals(user.getGender()))) {
			response.put("message", "Leave not applicable");
			return response;
		}

		double leaveAppliedCount = leaveRequestRepository.getAppliedLeaveCount(leaveType.getCode(), user.getUserId());
		if (leaveType.getMaxTimes() != null && leaveAppliedCount >= leaveType.getMaxTimes()) {
			response.put("message", "You have exceeded the number of times allowed for this leave.");
			return response;
		}

		if (leaveType.getType() != null) {
			Date currentDate = fromDate;

			while (!currentDate.after(toDate)) {

				if (!(holidays.get(leaveType.getType()).containsKey(currentDate))) {
					response.put("message",
							currentDate + " " + "Should be a Valid " + leaveType.getType() + " holiday");
				}
				currentDate = DateUtils.addDays(currentDate, 1);
			}
		}

		Map<String, Object> calculationResult = calculateEffectiveLeaveDays(leaveType, fromDate, toDate, holidays);
		int totalLeaveApplyDays = (int) calculationResult.get("totalDays");

		if (totalLeaveApplyDays <= 0) {
			response.put("message", "Number of applied leave should be greater than zero.");
			return response;
		} else {

			List<Date> validLeaveDates = (List<Date>) calculationResult.get("validLeaveDates");

			double leaveTypeCredited = leavePositionRepository.getLeaveTypeCredited(leaveType.getCode(), user.getUserId());
			double leaveTypeDebited = leavePositionRepository.getLeaveTypeDebited(leaveType.getCode(), user.getUserId());
			double maxLeaveDaysCanApply = leaveTypeCredited - leaveTypeDebited;

			
			  if (totalLeaveApplyDays > maxLeaveDaysCanApply) { response.put("message",
			 "You have only " + maxLeaveDaysCanApply + " days of " + leaveType.getCode() +
			 " leave available, so you cannot apply for " + totalLeaveApplyDays +
			 " days of leave."); return response; }
			 
			response.put("validLeaveDates", validLeaveDates);
			response.put("message", "Total " + code + " leave days applied: " + totalLeaveApplyDays);
			return response;
		}
	}

	// Method for calculating the number of Days Excluding weekends and
	// FixedHolidays as well.
	private Map<String, Object> calculateEffectiveLeaveDays(LeaveMaster leaveType, Date fromDate, Date toDate,
			Map<String, Map<Date, HolidayMaster>> holidays) {
		List<LocalDate> validLeaveDates = new ArrayList<>();
		int totalDays = 0;
		Date currentDate = fromDate;

		while (!currentDate.after(toDate)) {
			// Count days only if it's a working day or it's a sandwitch leave.
			if (isWorkingDay(currentDate, holidays) || leaveType.isSandwichIncluded()) {
				validLeaveDates.add(DateConverter.toLocalDate(currentDate));
				totalDays++;
			}
			currentDate = DateUtils.addDays(currentDate, 1);
		}
		Map<String, Object> result = new HashMap<>();
		result.put("totalDays", totalDays);
		result.put("validLeaveDates", validLeaveDates);
		return result;
	}

	private boolean isWorkingDay(Date date, Map<String, Map<Date, HolidayMaster>> holidays) {
		return isWeekDay(date) && !holidays.get("Fixed").containsKey(date);
	}

	private boolean isWeekDay(Date date) {
		int dayOfWeek = date.getDay();
		return !(dayOfWeek == 6 || dayOfWeek == 0); //sat and sun
	}
}
