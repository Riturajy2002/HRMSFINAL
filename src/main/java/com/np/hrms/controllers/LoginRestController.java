package com.np.hrms.controllers;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ConcurrentHashMap;

import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.np.hrms.auth.AES;
import com.np.hrms.entities.User;
import com.np.hrms.enums.Role;
import com.np.hrms.model.UserLoginInfo;
import com.np.hrms.repositories.SqlDAO;

import jakarta.annotation.PostConstruct;

@RestController
@RequestMapping(path = "/security")
public class LoginRestController {
	private static final Logger LOG = LoggerFactory.getLogger(LoginRestController.class);
	private ConcurrentHashMap<String, User> users = new ConcurrentHashMap<String, User>();
	private ConcurrentHashMap<String, UserLoginInfo> sessions = new ConcurrentHashMap<String, UserLoginInfo>();

	@Autowired
	private SqlDAO sqlDao;

	@PostConstruct
	public void refreshMetaData() {
		List<User> userData = sqlDao.getUsers();
		ConcurrentHashMap<String, User> currentUsers = new ConcurrentHashMap<String, User>();
		for (User user : userData) {
			currentUsers.put(user.getUserId(), user);
		}
		users = currentUsers;
		for (String userId : currentUsers.keySet()) {
			User user = currentUsers.get(userId);

			if (StringUtils.isNotBlank(userId)) {
				String apiKey = getApiKey(user);
				if (!sessions.contains(apiKey)) {
					UserLoginInfo loginInfo = new UserLoginInfo();
					loginInfo.setId(user.getId());
					loginInfo.setUserId(user.getUserId());
					loginInfo.setUserName(user.getName());
					loginInfo.setEmail_id(user.getEmailId());
					loginInfo.setGender(user.getGender());
					loginInfo.setDesignation(user.getDesignation());
					loginInfo.setReportManager(user.getReportManager());
					loginInfo.setOrganization(user.getOrganization());
					loginInfo.setAuthToken(apiKey);
					if (StringUtils.isNotBlank(user.getRole())) {
						loginInfo.setRoles(getRolesTArray(user.getRole()));
					}
					sessions.put(apiKey, loginInfo);
				}
			}
		}
	}

	void refreshUsers() {
		refreshMetaData();
	}

	public UserLoginInfo login(User credentials) {
		String userName = credentials.getUserId();
		String passWord = credentials.getPassword();
		try {
			userName = AES.decryptString(userName, "2fs6828r61oo68rr0su3eurf4serfu675eerf5oeoesr60s17fo5u5u418us5372");
			passWord = AES.decryptString(passWord, "2fs6828r61oo68rr0su3eurf4serfu675eerf5oeoesr60s17fo5u5u418us5372");

			User user = users.get(userName);
			if (user != null && user.getPassword().equals(passWord)) {
				UserLoginInfo loginInfo = new UserLoginInfo();
				loginInfo.setId(user.getId());
				loginInfo.setOrganization((user.getOrganization()));
				loginInfo.setUserId(user.getUserId());
				loginInfo.setUserName(user.getName());
				loginInfo.setPassword(user.getPassword());
				loginInfo.setDesignation(user.getDesignation());
				loginInfo.setReportManager(user.getReportManager());
				loginInfo.setEmail_id(user.getEmailId());
				loginInfo.setContactNo(user.getContactNo());
				loginInfo.setLocation(user.getLocation());
				String key = getApiKey(user);
				sessions.put(key, loginInfo);
				loginInfo.setAuthToken(key);
				if (StringUtils.isNotBlank(user.getRole())) {
					loginInfo.setRoles(getRolesTArray(user.getRole()));
				}
				return loginInfo;
			}
		} catch (Exception ioException) {
			LOG.error("Error");
		}
		return null;
	}

	public String logout(String key) {
		UserLoginInfo loginInfo = sessions.remove(key);
		if (loginInfo != null) {
			return "Success";
		} else {
			return "Failed";
		}
	}

	public UserLoginInfo getUserLoginInfo(String authToken) {
		UserLoginInfo user = sessions.get(authToken);
		return user;
	}

	public List<Role> getRoles(String authToken) {
		List<Role> roles = new ArrayList<Role>();
		UserLoginInfo userlogin = sessions.get(authToken);
		if (userlogin != null) {
			return userlogin.getRoles();
		}
		return roles;
	}

	public List<Role> getRolesTArray(String role) {
		List<Role> roles = new ArrayList<Role>();
		for (String r : role.split(",")) {
			roles.add(Role.valueOf(r));
		}
		return roles;
	}

	public User getLoggedUser(String authToken) {
		UserLoginInfo user = sessions.get(authToken);
		return users.get(user.getUserId());
	}

	private String getApiKey(User user) {
		return "API://" + AES.encrypt(String.format("%s-%s", user.getUserId(), user.getUserKey()), "bitnami@123");
	}

	public static void main(String args[]) {
		System.out.println("API://" + AES.encrypt(String.format("%s-%s", "admin", "NOVTSVFS532"), "bitnami@123"));
	}

	public User getUser(String userId) {
		return users.get(userId);
	}

}