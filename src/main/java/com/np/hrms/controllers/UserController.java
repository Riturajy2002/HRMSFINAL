package com.np.hrms.controllers;

import java.util.List;
import java.util.Optional;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.np.hrms.auth.Secured;
import com.np.hrms.dto.UserDTO;
import com.np.hrms.entities.HolidayMaster;
import com.np.hrms.entities.User;
import com.np.hrms.enums.Role;
import com.np.hrms.model.UserLoginInfo;
import com.np.hrms.repositories.HolidayMasterRepository;
import com.np.hrms.repositories.UserRepository;

import jakarta.servlet.http.HttpServletRequest;

@RestController
@RequestMapping("/api")
public class UserController {

	public UserController(UserRepository userRepositoryPage) {
	}

	@Autowired
	private LoginRestController loginRestController;

	@Autowired
	private LoginRestController loginService;

	// private LeaveSchedulerService leaveSchedulerService;

	@Autowired
	private HolidayMasterRepository holidayMasterRepository;

	@Autowired
	private HttpServletRequest context;

	@Autowired
	private UserRepository userRepository;

	private User getUser() {
		String authToken = "";
		if (context != null) {
			authToken = context.getHeader("auth-token");
		}
		return (User) loginRestController.getLoggedUser(authToken);
	}

	// Just for Refresh Purpose when entry done in DB directly then calling from
	// Postman.
	@GetMapping(value = "/refreshUsers")
	public String refreshUsers() {
		loginRestController.refreshUsers();
		return "Success";
	}
	
	// For updating leaves from the postman
	@PostMapping("/holidays/add")
	public ResponseEntity<String> addHoliday(@RequestBody HolidayMaster holiday) {
		// Check if a holiday with the same date already exists
		Optional<HolidayMaster> existingHoliday = holidayMasterRepository.findByHolidayDate(holiday.getHolidayDate());
		if (existingHoliday.isPresent()) {
			return ResponseEntity.badRequest().body("Holiday already exists for this date.");
		}

		// Save the new holiday
		holidayMasterRepository.save(holiday);
		return ResponseEntity.ok("Holiday added successfully.");
	}

	@PostMapping(value = "/login", consumes = "application/json", produces = "application/json")
	public UserLoginInfo login(@RequestBody User credentials) {
		return loginRestController.login(credentials);
	}

	// For logged out the user by getting their auth-token and removing it from the
	// HashMap.
	@GetMapping("/logout")
	public ResponseEntity<String> logout(HttpServletRequest request) {
		String authToken = request.getHeader("auth-token");
		if (authToken != null && !authToken.isEmpty()) {
			String logoutResult = loginRestController.logout(authToken);
			if (logoutResult.equals("Success")) {
				return ResponseEntity.ok().body("{\"message\": \"Logged out successfully\"}");
			} else {
				return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body("{\"error\": \"Logout failed\"}");
			}
		} else {
			return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body("{\"error\": \"Unauthorized\"}");
		}
	}

	// Finding the user all information for just showing it.
	@GetMapping("/{id}")
	public ResponseEntity<Optional<User>> getUserById(@PathVariable String id) {
		Optional<User> user = userRepository.findById(id);
		if (user != null) {
			return ResponseEntity.ok(user);
		} else {
			return ResponseEntity.notFound().build();
		}
	}

	// For getting all the departments.
	@GetMapping("/reportmanagers")
	@Secured({ Role.Admin, Role.User, Role.Manager })
	public List<UserDTO> getAllIdAndNameForRManager() {
		return userRepository.findAllIdAndNameForRManager();

	}
}