package com.st.nicobot.internal.api.services;

import com.st.nicobot.api.domain.model.response.UnmarshalledResponse;
import com.st.nicobot.api.services.PersistenceStrategy;
import com.st.nicobot.services.PropertiesService;
import com.st.nicobot.utils.NicobotProperty;
import org.apache.commons.lang3.StringUtils;
import org.codehaus.jackson.map.ObjectMapper;
import org.codehaus.jackson.map.SerializationConfig;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.ApplicationContext;

import javax.ws.rs.core.MultivaluedMap;

/**
 * @author Julien
 *
 */
public abstract class APIBaseService<T extends UnmarshalledResponse> {

	private static Logger logger = LoggerFactory.getLogger(APIBaseService.class);

	@Autowired
	private PropertiesService propertiesService;

	@Autowired
	private ApplicationContext ctx;

	private PersistenceStrategy persistenceStrategy;

	private static ObjectMapper objectMapper;

	public abstract T getResponseInstance();

	public abstract String getServiceName();

	ObjectMapper getObjectMapper() {
		if (objectMapper == null) {
			objectMapper = new ObjectMapper();
			objectMapper.configure(SerializationConfig.Feature.WRITE_DATES_AS_TIMESTAMPS, false);
		}

		return objectMapper;
	}

	public PersistenceStrategy getPersistenceStrategy() {
		if (persistenceStrategy == null) {
			String strategyName = propertiesService.get(NicobotProperty.API_PERSISTENCE_STRATEGY);
			try {
				persistenceStrategy = ctx.getBean(strategyName, PersistenceStrategy.class);
			} catch (Exception ex) {
				logger.error("", ex);
			}
		}

		return persistenceStrategy;
	}


	public String getServiceURI() {
		return propertiesService.get(NicobotProperty.API_URI) + "/" + getServiceName();
	}

	T sendGetRequest(String uriContext, MultivaluedMap<String, String> queryParams) {
		String uri = getURI(uriContext);
		String responseString = getPersistenceStrategy().sendGetRequest(uri, queryParams);

		if (responseString != null) {
			return unmarshalResponse(responseString);
		}

		return null;
	}

	T sendGetRequest(MultivaluedMap<String, String> queryParams) {
		return sendGetRequest(null, queryParams);
	}

	T sendPostRequest(String uriContext, Object payload) {
		try {
			String uri = getURI(uriContext);
			String json = getObjectMapper().writeValueAsString(payload);
			String responseString = getPersistenceStrategy().sendPostRequest(uri, json);

			if (responseString != null) {
				return unmarshalResponse(responseString);
			}

			return null;
		}
		catch(Exception ex) {
			logger.error("{}", ex);
		}

		return null;
	}

	T sendPostRequest(Object payload) {
		return sendPostRequest(null, payload);
	}

	private String getURI(String urlContext) {
		String url = getServiceURI();
		if(StringUtils.isNotBlank(urlContext)) {
			url += (!urlContext.startsWith("/") ? "/" : "") + urlContext;
		}
		return url;
	}

	@SuppressWarnings("unchecked")
	T unmarshalResponse(String responseString) {
		try {
			return (T) getObjectMapper().readValue(responseString, getResponseInstance().getClass());
		} catch (Exception ex) {
			logger.error("{}", ex);
		}

		return null;
	}


}
