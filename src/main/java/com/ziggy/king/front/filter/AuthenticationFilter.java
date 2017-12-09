package com.ziggy.king.front.filter;

import java.io.IOException;

import javax.servlet.Filter;
import javax.servlet.FilterChain;
import javax.servlet.FilterConfig;
import javax.servlet.ServletException;
import javax.servlet.ServletRequest;
import javax.servlet.ServletResponse;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.apache.log4j.Logger;

import com.ziggy.king.front.session.SessionManager;

public class AuthenticationFilter implements Filter {

	private static Logger logger = Logger.getLogger(AuthenticationFilter.class);

	public void doFilter(ServletRequest request, ServletResponse response, FilterChain chain) throws IOException, ServletException {
		if (request instanceof HttpServletRequest) {
			HttpServletRequest hsr = (HttpServletRequest) request;
			String sq = hsr.getQueryString();
			SessionManager sm = new SessionManager(hsr.getSession(true));

			// check if user is logged in
			if (sm.getUserId() != null) {
				// logger.trace("User logged in. Chaining request.");
				chain.doFilter(request, response);
			} else {
				logger.debug(String.format("Request received for %s but no user logged in. Sending to login screen", hsr.getRequestURL() + (sq != null ? "?" + sq : "")));
				((HttpServletResponse) response).sendRedirect("/account/login");
			}
		}
	}

	public void destroy() {

	}

	@Override
	public void init(FilterConfig filterConfig) throws ServletException {

	}

}
