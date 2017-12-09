package com.ziggy.king.front.views;

import java.net.URI;
import java.net.URISyntaxException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Date;
import java.util.List;

import javax.servlet.http.HttpServletRequest;
import javax.ws.rs.core.Context;
import javax.ws.rs.core.MultivaluedMap;
import javax.ws.rs.core.Response;
import javax.ws.rs.core.UriBuilder;
import javax.ws.rs.core.UriInfo;

import org.apache.log4j.Level;
import org.glassfish.jersey.server.mvc.Viewable;

import com.ziggy.king.front.model.ViewableData;
import com.ziggy.king.front.session.SessionManager;
import com.ziggy.king.model.LogEntry;

import net.sf.uadetector.UserAgent;
import net.sf.uadetector.UserAgentStringParser;
import net.sf.uadetector.service.UADetectorServiceFactory;

public class BaseView {

	@Context
	protected HttpServletRequest request;

	@Context
	protected UriInfo uriInfo;

	protected Response getRedirect() {
		SessionManager sm = new SessionManager(request.getSession(true));
		String rt = sm.getRedirectType();
		String rd = sm.getRedirectData();
		if (rt != null && rt.equals("uri")) {
			if (rd != null) {
				try {
					return Response.seeOther(new URI(rd)).build();
				} catch (URISyntaxException e) {
				}
			}
		}
		try {
			return Response.seeOther(new URI("/")).build();
		} catch (URISyntaxException e) {
			return null;
		}
	}

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
		le.setParams(parseParams(uriInfo.getQueryParameters()));
		le.setSession(request.getSession(true).getId());
		le.setTimestamp(new Date());
		le.setUri(uriInfo.getPath());
		try {
			UserAgentStringParser parser = UADetectorServiceFactory.getResourceModuleParser();
			UserAgent agent = parser.parse(request.getHeader("User-Agent"));
			le.setOperatingSystem(agent.getOperatingSystem().getName() + " (" + agent.getOperatingSystem().getVersionNumber().toVersionString() + ")");
			le.setBrowser(agent.getName() + " " + agent.getVersionNumber().toVersionString());
		} catch (Exception e) {
		}
		String paramString = le.getParams().length() > 0 ? "?" + le.getParams() : "";
		return String.format("%s :: %s :: %s :: %s", le.getSession(), le.getMethod(), "/" + le.getUri() + paramString, message);
	}

	public static String parseParams(MultivaluedMap<String, String> params) {
		String p = "";
		for (String key : params.keySet()) {
			String val = params.getFirst(key);
			p = p + key + "=" + val + "&";
		}
		p = (p.length() > 0) ? p.substring(0, p.length() - 1) : p;
		return p;
	}

	protected SessionManager getSessionManager() {
		return new SessionManager(request.getSession(true));
	}

	protected ViewableData getData() {
		return new ViewableData(getSessionManager());
	}

	protected Viewable getViewable(String page) {
		return new Viewable(page, getData().getMap());
	}

	protected Viewable getViewable(String page, ViewableData data) {
		return new Viewable(page, data.getMap());
	}

	protected String getBaseUri(String... omitParams) {
		List<String> prms = new ArrayList<String>(Arrays.asList(omitParams));
		URI uri = uriInfo.getRequestUri();
		MultivaluedMap<String, String> params = uriInfo.getQueryParameters();
		String base = uri.getPath();
		UriBuilder builder = UriBuilder.fromPath(base);
		boolean hasQueryParams = false;
		for (String k : params.keySet()) {
			List<String> val = params.get(k);
			if (val != null && val.size() > 0 && !prms.contains(k)) {
				hasQueryParams = true;
				String v = val.get(0);
				builder = builder.queryParam(k, v);
			}
		}
		base = builder.build().toString();
		base += (hasQueryParams) ? "&" : "?";
		return base;
	}

	protected Response getRedirect(String uri) {
		try {
			return Response.seeOther(new URI(uri)).build();
		} catch (URISyntaxException e) {
			return null;
		}
	}
}