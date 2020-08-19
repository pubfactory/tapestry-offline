package org.lazan.t5.offline.services.internal;

import java.io.IOException;
import java.io.OutputStream;
import java.io.PrintWriter;
import java.util.LinkedHashMap;
import java.util.Map;

import javax.servlet.ServletOutputStream;
import javax.servlet.WriteListener;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.apache.tapestry5.SymbolConstants;
import org.apache.tapestry5.internal.services.RequestImpl;
import org.apache.tapestry5.internal.services.ResponseImpl;
import org.apache.tapestry5.ioc.annotations.Symbol;
import org.apache.tapestry5.ioc.services.TypeCoercer;
import org.apache.tapestry5.services.Request;
import org.apache.tapestry5.services.Response;
import org.apache.tapestry5.services.SessionPersistedObjectAnalyzer;
import org.lazan.t5.offline.services.OfflineObjectFactory;
import org.lazan.t5.offline.services.OfflineObjects;
import org.lazan.t5.offline.services.OfflineResponseGlobals;

public class OfflineObjectFactoryImpl implements OfflineObjectFactory {
	private final OfflineResponseGlobals offlineResponseGlobals;
	private final String requestEncoding;
	private final SessionPersistedObjectAnalyzer sessionFactory;
	private final TypeCoercer typeCoercer;
	
	public OfflineObjectFactoryImpl(
			OfflineResponseGlobals offlineResponseGlobals, 
			@Symbol(SymbolConstants.CHARSET) String requestEncoding,
			SessionPersistedObjectAnalyzer sessionFactory,
			TypeCoercer typeCoercer) {
		super();
		this.offlineResponseGlobals = offlineResponseGlobals;
		this.requestEncoding = requestEncoding;
		this.sessionFactory = sessionFactory;
		this.typeCoercer = typeCoercer;
	}
	
	@Override
	public OfflineObjects createOfflineObjects(HttpServletRequest httpRequest, OutputStream out) {
		Request request = createRequest(httpRequest);
		HttpServletResponse httpResponse = createHttpResponse(out);
		Response response = createResponse(httpRequest, httpResponse);
		return new OfflineObjectsImpl(request, httpResponse, response);
	}
	
	@Override
	public OfflineObjects createOfflineObjects(HttpServletRequest httpRequest, PrintWriter writer) {
		Request request = createRequest(httpRequest);
		HttpServletResponse httpResponse = createHttpResponse(writer);
		Response response = createResponse(httpRequest, httpResponse);
		return new OfflineObjectsImpl(request, httpResponse, response);
	}
	
	protected Request createRequest(HttpServletRequest httpRequest) {
		return new RequestImpl(httpRequest, requestEncoding, sessionFactory);
	}
	
	protected Response createResponse(HttpServletRequest httpRequest, HttpServletResponse httpResponse) {
		return new ResponseImpl(httpRequest, httpResponse);
	}
	
	protected HttpServletResponse createHttpResponse(PrintWriter writer) {
		Map<String, Object> responseValues = new LinkedHashMap<String, Object>(offlineResponseGlobals.getValues());
		responseValues.put("writer", writer);
		return new ProxyBuilder(typeCoercer).withDefaultValues(responseValues).build(HttpServletResponse.class);
	}
	
	protected HttpServletResponse createHttpResponse(final OutputStream out) {
		ServletOutputStream servletOut = new ServletOutputStream() {
			@Override
			public void write(int b) throws IOException {
				out.write(b);
			}

			@Override
			public boolean isReady() {
				// TODO Auto-generated method stub
				return false;
			}

			@Override
			public void setWriteListener(WriteListener writeListener) {
				// TODO Auto-generated method stub
				
			}
		};
		Map<String, Object> responseValues = new LinkedHashMap<String, Object>(offlineResponseGlobals.getValues());
		responseValues.put("outputStream", servletOut);
		return new ProxyBuilder(typeCoercer).withDefaultValues(responseValues).build(HttpServletResponse.class);
	}
}