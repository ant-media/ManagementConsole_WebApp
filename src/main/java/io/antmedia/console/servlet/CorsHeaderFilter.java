package io.antmedia.console.servlet;

import java.io.IOException;

import javax.servlet.Filter;
import javax.servlet.FilterChain;
import javax.servlet.FilterConfig;
import javax.servlet.ServletException;
import javax.servlet.ServletRequest;
import javax.servlet.ServletResponse;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpServletResponseWrapper;

/**
 * This filter is implemented in order to make easy to develop angular app
 * @author mekya
 *
 */
public class CorsHeaderFilter implements Filter {

	@Override
	public void init(FilterConfig filterConfig) throws ServletException {
		//no need to implement
	}

	@Override
	public void doFilter(ServletRequest request, ServletResponse response, FilterChain chain)
			throws IOException, ServletException {
		chain.doFilter(request, new HttpServletResponseWrapper((HttpServletResponse) response) {
			@Override
			public void addHeader(String name, String value) {
	            if (!name.equalsIgnoreCase("Access-Control-Allow-Origin")) {
	                super.setHeader(name, value);
	            }
	            else {
	            		super.setHeader("Access-Control-Allow-Origin", "http://localhost:4200");
	            		super.setHeader("Access-Control-Allow-Credentials", "true");
	            }
	        }
		});
		
	}

	@Override
	public void destroy() {
		//no need to implement
	}

}
