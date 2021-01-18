package org.lazan.t5.offline.services;

import javax.servlet.http.HttpServletResponse;

import org.apache.tapestry5.http.services.Request;
import org.apache.tapestry5.http.services.Response;

public interface OfflineObjects {
	Request getRequest();
	Response getResponse();
	HttpServletResponse getHttpServletResponse();
}
