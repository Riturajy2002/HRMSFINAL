
package com.np.hrms.auth;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.GenericFilterBean;
import org.springframework.web.method.HandlerMethod;
import org.springframework.web.servlet.HandlerExecutionChain;
import org.springframework.web.servlet.mvc.method.annotation.RequestMappingHandlerMapping;

import com.np.hrms.controllers.LoginRestController;
import com.np.hrms.enums.Role;
import com.np.hrms.model.UserLoginInfo;

import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.ServletRequest;
import jakarta.servlet.ServletResponse;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

@Component
public class AuthorizationRequestFilter extends GenericFilterBean {

	@Autowired
	private LoginRestController loginService;

	@Autowired
	private RequestMappingHandlerMapping requestMappingHandlerMapping;

	private static final Logger LOG = LoggerFactory.getLogger(AuthorizationRequestFilter.class);

	@Override
	public void doFilter(ServletRequest request, ServletResponse response, FilterChain chain)
			throws IOException, ServletException {
		HttpServletRequest httpRequest = (HttpServletRequest) request;
		HttpServletResponse httpResponse = (HttpServletResponse) response;
		if (httpRequest.getRequestURI().contains("/api/")) {
			HandlerExecutionChain handler = null;
			try {
				handler = requestMappingHandlerMapping.getHandler(httpRequest);
			} catch (Exception e) {
				System.out.println(e);
			}
			HandlerMethod handlerMethod = null;
			if (handler != null) {
				handlerMethod = (HandlerMethod) handler.getHandler();
				if (handlerMethod != null) {
					List<Role> allowedRoles = extractRoles(handlerMethod);
					if (allowedRoles.size() > 0) {
						String authToken = httpRequest.getHeader("auth-token");
						if (authToken == null) {
							httpResponse.setStatus(HttpServletResponse.SC_UNAUTHORIZED);
							return;
						}
						UserLoginInfo login = loginService.getUserLoginInfo(authToken);
						if (login == null) {
							httpResponse.setStatus(HttpServletResponse.SC_UNAUTHORIZED);
							return;
						}
						List<Role> userRoles = loginService.getRoles(authToken);
						allowedRoles.retainAll(userRoles);
						if (allowedRoles.isEmpty()) {
							httpResponse.setStatus(HttpServletResponse.SC_UNAUTHORIZED);
							return;
						}
					}
					chain.doFilter(request, response);
				}
			}
		} else {
			chain.doFilter(request, response);
		}
	}

	private List<Role> extractRoles(HandlerMethod handlerMethod) {
		if (handlerMethod == null) {
			return new ArrayList<Role>();
		} else {
			Secured secured = handlerMethod.getMethodAnnotation(Secured.class);
			if (secured == null) {
				return new ArrayList<Role>();
			} else {
				Role[] allowedRoles = secured.value();
				List<Role> allowedRolesList = new ArrayList<Role>();
				for (Role role : allowedRoles) {
					allowedRolesList.add(role);
				}
				return allowedRolesList;
			}
		}
	}
}