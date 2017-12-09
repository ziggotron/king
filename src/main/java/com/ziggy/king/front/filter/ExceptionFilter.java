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

public class ExceptionFilter implements Filter {

	private static Logger log = Logger.getLogger(ExceptionFilter.class);

	public void doFilter(ServletRequest request, ServletResponse response, FilterChain chain) throws IOException, ServletException {
		if (request instanceof HttpServletRequest) {
			try {
				chain.doFilter(request, response);
			} catch (Exception e) {
				try {
					log.error("Exception - " + e.getMessage());
					HttpServletResponse resp = (HttpServletResponse) response;
					resp.sendError(404);
				} catch (Exception e2) {
				}
			}
		} else {
			chain.doFilter(request, response);
		}
	}

	public void destroy() {

	}

	@Override
	public void init(FilterConfig filterConfig) throws ServletException {

	}
}
