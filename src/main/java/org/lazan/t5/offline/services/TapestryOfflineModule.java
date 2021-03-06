package org.lazan.t5.offline.services;

import java.io.IOException;
import java.util.Locale;

import javax.servlet.ServletContext;
import javax.servlet.http.HttpServletResponse;

import org.apache.tapestry5.SymbolConstants;
import org.apache.tapestry5.commons.MappedConfiguration;
import org.apache.tapestry5.ioc.ServiceBinder;
import org.apache.tapestry5.ioc.annotations.Contribute;
import org.apache.tapestry5.ioc.annotations.Symbol;
import org.apache.tapestry5.http.services.ApplicationGlobals;
import org.apache.tapestry5.http.services.RequestGlobals;
import org.lazan.t5.offline.services.internal.OfflineComponentRendererImpl;
import org.lazan.t5.offline.services.internal.OfflineObjectFactoryImpl;
import org.lazan.t5.offline.services.internal.OfflineRequestBuilderFactoryImpl;
import org.lazan.t5.offline.services.internal.OfflineRequestGlobalsImpl;
import org.lazan.t5.offline.services.internal.OfflineResponseGlobalsImpl;

public class TapestryOfflineModule {
	public static void bind(ServiceBinder binder) {
		binder.bind(OfflineComponentRenderer.class, OfflineComponentRendererImpl.class);
		binder.bind(OfflineRequestGlobals.class, OfflineRequestGlobalsImpl.class);
		binder.bind(OfflineResponseGlobals.class, OfflineResponseGlobalsImpl.class);
		binder.bind(OfflineObjectFactory.class, OfflineObjectFactoryImpl.class);
		binder.bind(OfflineRequestBuilderFactory.class, OfflineRequestBuilderFactoryImpl.class);
		
	}
	
	@Contribute(OfflineRequestGlobals.class)
	public void contributeOfflineRequestGlobals(MappedConfiguration<String, Object> config, 
			ApplicationGlobals applicationGlobals, 
			RequestGlobals requestGlobals,
			@Symbol(SymbolConstants.CHARSET) String charset) {
		config.add("locale", Locale.getDefault());
		config.add("secure", false);
		config.add("servletContext", applicationGlobals.getServletContext());
		config.add("contextPath", applicationGlobals.getServletContext().getContextPath());
		config.add("contentType", "text/html");
		config.add("protocol", "http");
		config.add("characterEncoding", charset);
		config.add("queryString", createQueryString(applicationGlobals.getServletContext()));

		// if you want to add info from the HttpServletRequest into the config map, do so in
		// the arachne OfflineRenderingServiceImpl.getOfflineRequest method as it will allow
		// null values and sometimes the request has nulls (request.getPathInfo(), etc.).
	}

	@Contribute(OfflineResponseGlobals.class)
	public void contributeOfflineResponseGlobals(MappedConfiguration<String, Object> config, 
			RequestGlobals requestGlobals,
			@Symbol(SymbolConstants.CHARSET) String charset) throws IOException {
		config.add("characterEncoding", charset);
		HttpServletResponse httpResponse = requestGlobals.getHTTPServletResponse();
		if (httpResponse != null) {
			config.add("outputStream", httpResponse.getOutputStream());
		}
	}
	
	private String createQueryString(ServletContext servletContext) {
		String[] urlParts = servletContext.getContextPath().split("\\?");
		if (urlParts.length > 1) {
			return new StringBuilder("?").append(urlParts[1]).toString();
		} else {
			return "";
		}
	}
}
