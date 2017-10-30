package com.antstreaming.console.rest;

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
		if (path.equals("/Console/rest/isAuthenticated") ||
			path.equals("/Console/rest/authenticateUser") || 
			path.equals("/Console/rest/addInitialUser") ||
			path.equals("/Console/rest/isFirstLogin") ||
			RestService.isAuthenticated(request.getServletContext())) {
			chain.doFilter(request, response);
		}
		else {
			HttpServletResponse resp = (HttpServletResponse) response;
		   // resp.reset();
		    resp.setStatus(HttpServletResponse.SC_UNAUTHORIZED);
		}
		
	}

	@Override
	public void destroy() {
		
		
	}

}
