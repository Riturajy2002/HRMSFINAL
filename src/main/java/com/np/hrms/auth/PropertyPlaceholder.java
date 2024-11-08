package com.np.hrms.auth;

import org.apache.commons.lang3.StringUtils;
import org.springframework.stereotype.Component;

@Component
public class PropertyPlaceholder {

	public String SECRET_KEY;
	public String environment;
	private static final String PASSWORD_PARAM = "pwd";
	private static final String ENVIRONMENT_PARAM_NAME = "env";
	private String pwd;

	public PropertyPlaceholder() {
		pwd = System.getProperty(PASSWORD_PARAM);
		if (StringUtils.isEmpty(pwd)) {
			pwd = "bitnami@123";
		}
		SECRET_KEY = pwd;
		environment = System.getProperty(ENVIRONMENT_PARAM_NAME);
		if (StringUtils.isEmpty(environment)) {
			environment = "dev";
		}
	}

	public String getPwd() {
		return pwd;
	}

	public static void main(String arg[]) {
		String authToken = "API://" + AES.encrypt(String.format("%s-%s", "Abhinav@NP", "LMSHR20246"), "bitnami@123");
		System.out.println(authToken);
	}
}