package com.ziggy.king.controllers;

import org.apache.log4j.Logger;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.ziggy.king.model.User;

@RestController
@RequestMapping(path = "/account")
public class AccountController extends BaseController {

	private Logger logger = Logger.getLogger(this.getClass());

	@RequestMapping(path = "/profile")
	public User getProfile() {
		logger.info(SecurityContextHolder.getContext().getAuthentication().getPrincipal());
		User user = getCurrentUser();
		logger.info(_info("Serving profile information for %s", user.getName()));
		return user;
	}
}
