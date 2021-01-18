package org.lazan.t5.offline.services.internal;

import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpSession;

import org.apache.commons.collections4.CollectionUtils;
import org.apache.tapestry5.commons.services.TypeCoercer;
import org.lazan.t5.offline.services.OfflineRequestBuilder;
import org.lazan.t5.offline.services.OfflineRequestGlobals;
import org.lazan.t5.offline.services.internal.ProxyBuilder.MethodHandler;

public class OfflineRequestBuilderImpl implements OfflineRequestBuilder {
	private final Map<String, Object> requestValues;
	private Map<String, Object> sessionAttributes = new LinkedHashMap<String, Object>();
	private Map<String, Object> headers = new LinkedHashMap<String, Object>();
	private Map<String, Object> attributes = new LinkedHashMap<String, Object>();
	private Map<String, List<String>> parameters = new LinkedHashMap<String, List<String>>();
	
	private final TypeCoercer typeCoercer;
	private HttpSession session;
	
	public OfflineRequestBuilderImpl(OfflineRequestGlobals offlineRequestGlobals, TypeCoercer typeCoercer) {
		super();
		this.requestValues = new LinkedHashMap<String, Object>(offlineRequestGlobals.getValues());
		this.typeCoercer = typeCoercer;
	}
	
	@Override
	public OfflineRequestBuilder withValue(String name, Object value) {
		requestValues.put(name,  value);
		return this;
	}
	
	@Override
	public OfflineRequestBuilder withLocale(Locale locale) {
		return withValue("locale", locale);
	}
	
	@Override
	public OfflineRequestBuilder withSessionAttribute(String name, Object value) {
		sessionAttributes.put(name,  value);
		return this;
	}

	@Override
	public OfflineRequestBuilder withAttribute(String name, Object value) {
		attributes.put(name,  value);
		return this;
	}
	
	@Override
	public OfflineRequestBuilder withHeader(String name, Object value) {
		headers.put(name.toLowerCase(), value);
		return this;
	}

	@Override
	public OfflineRequestBuilder withParameter(String name, String value) {
	    if (!parameters.containsKey(name)) {
	        parameters.put(name, new ArrayList<>());
	    }
		parameters.get(name).add(value);
		return this;
	}
	
	@Override
	public OfflineRequestBuilder setXHR() {
		return withHeader("X-Requested-With", "XMLHttpRequest");
	}
	
	@Override
	public OfflineRequestBuilder withContentType(String contentType) {
		return withValue("contentType", contentType);
	}

	@Override
	public HttpServletRequest build() {
		MethodHandler getHeaderHandler = new MethodHandler() {
			@Override
			public Object handle(Method method, Object[] args) {
				return headers.get(((String)args[0]).toLowerCase());
			}
		};
        MethodHandler getHeaderNamesHandler = new MethodHandler() {
            @Override
            public Object handle(Method method, Object[] args) {
                return Collections.enumeration(headers.keySet());
            }
        };
        MethodHandler getHeadersHandler = new MethodHandler() {
            @Override
            public Object handle(Method method, Object[] args) {
                Object value = headers.get(((String)args[0]).toLowerCase());
                Set<Object> values = value != null ? Collections.singleton(value) : Collections.emptySet();
                return Collections.enumeration(values);
            }
        };

		MethodHandler getAttributeHandler = new MethodHandler() {
			@Override
			public Object handle(Method method, Object[] args) {
				return attributes.get(args[0]);
			}
		};
        MethodHandler getAttributeNamesHandler = new MethodHandler() {
            @Override
            public Object handle(Method method, Object[] args) {
                return Collections.enumeration(attributes.keySet());
            }
        };
		MethodHandler setAttributeHandler = new MethodHandler() {
			@Override
			public Object handle(Method method, Object[] args) {
				return attributes.put((String) args[0], args[1]);
			}
		};
        MethodHandler removeAttributeHandler = new MethodHandler() {
            @Override
            public Object handle(Method method, Object[] args) {
                return attributes.remove((String) args[0]);
            }
        };

		MethodHandler getParameterHandler = new MethodHandler() {
			@Override
			public Object handle(Method method, Object[] args) {
			    List<String> values = parameters.get(args[0]);
			    return CollectionUtils.isEmpty(values) ? null : values.get(0);
			}
		};
        MethodHandler getParameterMapHandler = new MethodHandler() {
            @Override
            public Object handle(Method method, Object[] args) {
                return parameters.entrySet().stream()
                        .collect(Collectors.toMap(e -> e.getKey(), e -> e.getValue().toArray(new String[e.getValue().size()])));
            }
        };
        MethodHandler getParameterNamesHandler = new MethodHandler() {
            @Override
            public Object handle(Method method, Object[] args) {
                return Collections.enumeration(parameters.keySet());
            }
        };
        MethodHandler getParameterValuesHandler = new MethodHandler() {
            @Override
            public Object handle(Method method, Object[] args) {
                List<String> values = parameters.get(args[0]);
                return values != null ? values.toArray(new String[values.size()]) : null;
            }
        };

		MethodHandler getSessionHandler = createGetSessionHandler();
		return new ProxyBuilder(typeCoercer)
			.withMethodHandler("getSession", getSessionHandler)
            .withMethodHandler("getHeader", getHeaderHandler)
            .withMethodHandler("getHeaderNames", getHeaderNamesHandler)
            .withMethodHandler("getHeaders", getHeadersHandler)
            .withMethodHandler("getParameter", getParameterHandler)
            .withMethodHandler("getParameterNames", getParameterNamesHandler)
            .withMethodHandler("getParameterValues", getParameterValuesHandler)
            .withMethodHandler("getParameterMap", getParameterMapHandler)
			.withMethodHandler("getAttribute", getAttributeHandler)
			.withMethodHandler("setAttribute", setAttributeHandler)
			.withMethodHandler("getAttributeNames", getAttributeNamesHandler)
			.withMethodHandler("removeAttribute", removeAttributeHandler)
			.withDefaultValues(requestValues)
			.build(HttpServletRequest.class);
	}

	private MethodHandler createGetSessionHandler() {
		MethodHandler getSessionHandler = new MethodHandler() {
			@Override
			public Object handle(Method method, Object[] args) {
				if (session != null) return session;
				boolean create = method.getParameterTypes().length == 0 || Boolean.TRUE.equals(args[0]);
				if (!create) return null;
				return createSession();
			}
		};
		return getSessionHandler;
	}

	private HttpSession createSession() {
        MethodHandler getAttributeHandler = new MethodHandler() {
            @Override
            public Object handle(Method method, Object[] args) {
                return sessionAttributes.get((String) args[0]);
            }
        };
        MethodHandler getAttributeNamesHandler = new MethodHandler() {
            @Override
            public Object handle(Method method, Object[] args) {
                return Collections.enumeration(sessionAttributes.keySet());
            }
        };
        MethodHandler setAttributeHandler = new MethodHandler() {
            @Override
            public Object handle(Method method, Object[] args) {
                return sessionAttributes.put((String) args[0], args[1]);
            }
        };
        MethodHandler removeAttributeHandler = new MethodHandler() {
            @Override
            public Object handle(Method method, Object[] args) {
                return sessionAttributes.remove((String) args[0]);
            }
        };
		session = new ProxyBuilder(typeCoercer)
                .withMethodHandler("getAttribute", getAttributeHandler)
                .withMethodHandler("getAttributeNames", getAttributeNamesHandler)
                .withMethodHandler("setAttribute", setAttributeHandler)
                .withMethodHandler("removeAttribute", removeAttributeHandler)
				.build(HttpSession.class);
		return session;
	}
}