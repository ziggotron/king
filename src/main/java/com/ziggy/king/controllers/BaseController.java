package com.ziggy.king.controllers;

import java.util.Date;

import javax.servlet.http.HttpServletRequest;

import org.apache.log4j.Level;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.RestController;

import com.ziggy.king.game.model.LogEntry;
import com.ziggy.king.model.User;
import com.ziggy.king.security.CurrentUser;
import com.ziggy.king.services.UserService;

@RestController
public class BaseController {

	@Autowired
	private HttpServletRequest request;

	@Autowired
	private UserService userService;

	protected String _debug(String message, Object... params) {
		return log(String.format(message, params), Level.DEBUG);
	}

	protected String _info(String message, Object... params) {
		return log(String.format(message, params), Level.INFO);
	}

	protected String _warn(String message, Object... params) {
		return log(String.format(message, params), Level.WARN);
	}

	protected String _error(String message, Object... params) {
		return log(String.format(message, params), Level.ERROR);
	}

	private String log(String message, Level level) {
		LogEntry le = new LogEntry();
		le.setLevel(level.toString());
		le.setMessage(message);
		le.setMethod(request.getMethod());
		le.setParams(request.getQueryString());
		le.setSession(request.getSession(true).getId());
		le.setTimestamp(new Date());
		le.setUri(request.getRequestURI());
		String paramString = le.getParams().length() > 0 ? "?" + le.getParams() : "";
		return String.format("%s :: %s :: %s :: %s", le.getSession(), le.getMethod(), "/" + le.getUri() + paramString, message);
	}

	protected User getCurrentUser() {
		CurrentUser cu = (CurrentUser) SecurityContextHolder.getContext().getAuthentication().getPrincipal();
		return userService.getUserByEmail(cu.getUsername());
	}

}