package com.sap.rc.main.multitenancy;

import java.io.IOException;

import javax.servlet.Filter;
import javax.servlet.FilterChain;
import javax.servlet.FilterConfig;
import javax.servlet.ServletException;
import javax.servlet.ServletRequest;
import javax.servlet.ServletResponse;
import javax.servlet.http.HttpServletRequest;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.web.context.WebApplicationContext;
import org.springframework.web.context.support.WebApplicationContextUtils;

import com.sap.hcp.cf.logging.common.LogContext;
import com.sap.xs2.security.container.SecurityContext;
import com.sap.xs2.security.container.UserInfo;
import com.sap.xs2.security.container.UserInfoException;

public class TenantFilter implements Filter {

	private final Logger logger = LoggerFactory.getLogger(getClass());
	public static final String LANGUAGE = "sap-language";

	@Override
	public void init(FilterConfig config) throws ServletException {
	}

	@Override
	public void doFilter(ServletRequest request, ServletResponse response, FilterChain chain)
			throws IOException, ServletException {

		try {
			prepareContextInfo(request);
			chain.doFilter(request, response);
		} finally {

			// Clear context to release the memory
			// logger.trace("Removing current tenantId: {}", TenantContext.getCurrentTenant());
			TenantContext.clear();
			LogContext.remove(TenantContext.TENANT_ID);

		}
	}

	private void prepareContextInfo(ServletRequest request) {

		// Set application context
		WebApplicationContext appContext = WebApplicationContextUtils
				.getRequiredWebApplicationContext(request.getServletContext());
		TenantContext.setAppContext(appContext);

		// Set HTTP session
		HttpServletRequest oCntxt = (HttpServletRequest) request;
		TenantContext.setHttpSession(oCntxt.getSession());
		
		// Set language
		String langu = oCntxt.getParameter(LANGUAGE);
		if ((langu != null && !langu.isEmpty())) {
			oCntxt.getSession().setAttribute(LANGUAGE, langu);
		} else {
			langu = (String) oCntxt.getSession().getAttribute(LANGUAGE);
		}
		if (langu != null && !langu.isEmpty()) {
			if (langu.length() >= 2)
				TenantContext.setLanguage(langu.substring(0, 2).toUpperCase());
			else
				TenantContext.setLanguage(langu);
		} else {
			String isoLangu = request.getLocale().getLanguage().toUpperCase();
			if (isoLangu != null && !isoLangu.isEmpty()) {
				if (isoLangu.length() >= 2)
					TenantContext.setLanguage(isoLangu.substring(0, 2).toUpperCase());
				else
					TenantContext.setLanguage(isoLangu);
			} else {
				TenantContext.setLanguage("EN");
			}
		}

		// Set user. Refer more for roles and user types in RMServiceFactoryFilter
		String user = oCntxt.getRemoteUser();
		logger.trace("Set current user to: {}", user);
		TenantContext.setUser(user);

		try {

			UserInfo userInfo = SecurityContext.getUserInfo();
			// Non-accessible attributes like userInfo.getClientId() are protected
			String tenantId = userInfo.getIdentityZone();

			LogContext.add(TenantContext.TENANT_ID, tenantId);
			// logger.trace("Set current tenantId to: {}", tenantId);
			TenantContext.setCurrentTenant(tenantId);

		} catch (UserInfoException e) {
			
			// logger.error("UserInfoException, no tenant could be determined for this request.", e);
		}
		return;
	}

	@Override
	public void destroy() {
	}
}
