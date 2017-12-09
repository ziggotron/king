package com.ziggy.king.front.session;

import javax.servlet.http.HttpSession;
import javax.ws.rs.core.UriInfo;

import com.ziggy.king.db.model.User;

public class SessionManager {

	private HttpSession session;

	public SessionManager(HttpSession session) {
		this.session = session;
	}

	public void setUser(User user) {
		session.setAttribute("user", user);
	}

	public User getUser() {
		Object userObject = session.getAttribute("user");
		if (userObject == null) {
			return null;
		}
		User u = (User) userObject;
		return u;
	}

	public Integer getUserId() {
		Object userObject = session.getAttribute("user");
		if (userObject == null) {
			return null;
		}
		User u = (User) userObject;
		return u.getId();
	}

	public void clearUser() {
		session.setAttribute("user", null);
	}

	public void setRedirect(String redirectType, String redirectData) {
		session.setAttribute("redirect_type", redirectType);
		session.setAttribute("redirect_data", redirectData);
	}

	public void setURIRedirect(UriInfo uriInfo) {
		session.setAttribute("redirect_type", "uri");
		session.setAttribute("redirect_data", "/" + uriInfo.getPath());
	}

	public String getRedirectType() {
		Object redirectType = session.getAttribute("redirect_type");
		if (redirectType != null) {
			return (String) redirectType;
		} else {
			return null;
		}
	}

	public String getRedirectData() {
		Object redirectData = session.getAttribute("redirect_data");
		if (redirectData != null) {
			return (String) redirectData;
		} else {
			return null;
		}
	}

	public String getHttpSessionId() {
		return session.getId();
	}

	public void setMessage(String message) {
		session.setAttribute("message", message);
	}

	public String getMessage() {
		Object mo = session.getAttribute("message");
		if (mo != null) {
			setMessage(null);
			return (String) mo;
		} else {
			return null;
		}
	}

	public void setErrorMessage(String message) {
		session.setAttribute("errorMessage", message);
	}

	public String getErrorMessage() {
		Object mo = session.getAttribute("errorMessage");
		if (mo != null) {
			setMessage(null);
			return (String) mo;
		} else {
			return null;
		}
	}

	public void set(String key, Object value) {
		session.setAttribute(key, value);
	}

	public <T extends Object> T get(String key, Class<T> clazz) {
		Object o = session.getAttribute(key);
		if (o != null && o.getClass().isAssignableFrom(clazz)) {
			return clazz.cast(o);
		} else {
			return null;
		}
	}

}
