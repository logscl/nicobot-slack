package com.st.nicobot.internal.api.services;

import com.st.nicobot.api.domain.model.response.UnmarshalledResponse;
import com.st.nicobot.api.services.PersistenceStrategy;
import com.st.nicobot.bot.utils.NicobotProperty;
import com.st.nicobot.services.PropertiesService;
import org.codehaus.jackson.map.ObjectMapper;
import org.codehaus.jackson.map.SerializationConfig;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import javax.ws.rs.core.MultivaluedMap;

/**
 * @author Julien
 *
 */
@Service
public abstract class APIBaseService<T extends UnmarshalledResponse> {

	private static Logger logger = LoggerFactory.getLogger(APIBaseService.class);

	@Autowired
	private PropertiesService propertiesService;

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
				persistenceStrategy = (PersistenceStrategy) Class.forName(strategyName).newInstance();
			} catch (Exception ex) {
				logger.error("", ex);
			}
		}

		return persistenceStrategy;
	}


	public String getServiceURI() {
		return propertiesService.get(NicobotProperty.API_URI) + "/" + getServiceName();
	}

	T sendGetRequest(MultivaluedMap<String, String> queryParams) {
		String responseString = getPersistenceStrategy().sendGetRequest(getServiceURI(), queryParams);

		if (responseString != null) {
			return unmarshalResponse(responseString);
		}

		return null;
	}

	T sendPostRequest(Object payload) {
		try {
			String json = getObjectMapper().writeValueAsString(payload);
			String responseString = getPersistenceStrategy().sendPostRequest(getServiceURI(), json);

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
