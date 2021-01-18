package org.lazan.t5.offline.services.internal;

import java.io.IOException;
import java.io.OutputStream;
import java.io.PrintWriter;
import java.lang.reflect.Method;
import java.util.LinkedHashMap;
import java.util.Map;

import javax.servlet.ServletOutputStream;
import javax.servlet.WriteListener;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.apache.tapestry5.SymbolConstants;
import org.apache.tapestry5.http.internal.services.RequestImpl;
import org.apache.tapestry5.http.internal.services.ResponseImpl;
import org.apache.tapestry5.http.internal.services.TapestrySessionFactory;
import org.apache.tapestry5.ioc.annotations.Symbol;
import org.apache.tapestry5.commons.services.TypeCoercer;
import org.apache.tapestry5.http.services.Request;
import org.apache.tapestry5.http.services.Response;
import org.apache.tapestry5.http.services.SessionPersistedObjectAnalyzer;
import org.lazan.t5.offline.services.OfflineObjectFactory;
import org.lazan.t5.offline.services.OfflineObjects;
import org.lazan.t5.offline.services.OfflineResponseGlobals;
import org.lazan.t5.offline.services.internal.ProxyBuilder.MethodHandler;

public class OfflineObjectFactoryImpl implements OfflineObjectFactory {
	private final OfflineResponseGlobals offlineResponseGlobals;
	private final String requestEncoding;
	private final TypeCoercer typeCoercer;
	private final TapestrySessionFactory tapestrySessionFactory;
	
	public OfflineObjectFactoryImpl(
			OfflineResponseGlobals offlineResponseGlobals, 
			@Symbol(SymbolConstants.CHARSET) String requestEncoding,
			TypeCoercer typeCoercer, TapestrySessionFactory tapestrySessionFactory) {
		super();
		this.offlineResponseGlobals = offlineResponseGlobals;
		this.requestEncoding = requestEncoding;
		this.typeCoercer = typeCoercer;
		this.tapestrySessionFactory = tapestrySessionFactory;
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
		return new RequestImpl(httpRequest, requestEncoding, tapestrySessionFactory);
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

        MethodHandler encodeUrlHandler = new MethodHandler() {
            @Override
            public Object handle(Method method, Object[] args) {
                return args[0];
            }
        };

        responseValues.put("outputStream", servletOut);
		return new ProxyBuilder(typeCoercer)
		        .withMethodHandler("encodeURL", encodeUrlHandler)
		        .withDefaultValues(responseValues)
		        .build(HttpServletResponse.class);
	}
}