package com.ziggy.king.front.views;

import javax.ws.rs.FormParam;
import javax.ws.rs.GET;
import javax.ws.rs.POST;
import javax.ws.rs.Path;
import javax.ws.rs.core.Response;

import org.apache.log4j.Logger;

import com.ziggy.king.db.dao.UserDAO;
import com.ziggy.king.db.model.User;
import com.ziggy.king.util.PasswordSecurityUtil;

@Path("/account")
public class Account extends BaseView {
	private Logger logger = Logger.getLogger(this.getClass());

	@GET
	@Path("/login")
	public Response getLogin() {
		logger.info(_info("Serving login page"));
		return Response.ok(getViewable("/login")).build();
	}

	@GET
	@Path("/logout")
	public Response logout() {
		getSessionManager().clearUser();
		logger.info(_info("Logged out"));
		return getRedirect("/account/login");
	}

	@POST
	@Path("/login")
	public Response login(@FormParam("email") String email, @FormParam("password") String password) {
		User user = new UserDAO().getUserByEmail(email);
		if (user == null) {
			return Response.ok(getViewable("/login", getData().addErrorMessage("Invalid e-mail or password"))).build();
		}
		PasswordSecurityUtil psu = new PasswordSecurityUtil();
		String pwHash = psu.hashPassword(password, user.getSalt());
		if (!user.getPassword().equals(pwHash)) {
			return Response.ok(getViewable("/login", getData().addErrorMessage("Invalid e-mail or password"))).build();
		}
		getSessionManager().setUser(user);
		logger.info(_info("Logged in"));
		return getRedirect();
	}
}
