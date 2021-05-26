package io.antmedia.console.rest;

import java.io.IOException;

import javax.servlet.Filter;
import javax.servlet.FilterChain;
import javax.servlet.FilterConfig;
import javax.servlet.ServletException;
import javax.servlet.ServletRequest;
import javax.servlet.ServletResponse;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

public class AuthenticationFilter implements Filter {

	@Override
	public void init(FilterConfig filterConfig) throws ServletException {
		
		
	}

	@Override
	public void doFilter(ServletRequest request, ServletResponse response, FilterChain chain)
			throws IOException, ServletException {
		
		String path = ((HttpServletRequest) request).getRequestURI();
		if (path.equals("/rest/isAuthenticated") ||
			path.equals("/rest/authenticateUser") || 
			path.equals("/rest/addInitialUser") ||
			path.equals("/rest/isFirstLogin") ||
			
			path.equals("/rest/v2/authentication-status") ||
			path.equals("/rest/v2/users/initial") ||
			path.equals("/rest/v2/first-login-status") ||
			path.equals("/rest/v2/users/authenticate") ||
			
			RestService.isAuthenticated(((HttpServletRequest)request).getSession())) 
		{
			chain.doFilter(request, response);
		}
		else {
			HttpServletResponse resp = (HttpServletResponse) response;
		   // resp.reset();
		    resp.setStatus(HttpServletResponse.SC_FORBIDDEN);
		}
		
	}

	@Override
	public void destroy() {
		
		
	}

}
