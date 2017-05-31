package com.st.nicobot.internal.api.services;

import com.st.nicobot.api.services.PersistenceStrategy;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import javax.ws.rs.core.MultivaluedMap;

@Service
public class NoPersistenceStrategy implements PersistenceStrategy {

	private static Logger logger = LoggerFactory.getLogger(NoPersistenceStrategy.class);

	@Override
	public String sendGetRequest(String serviceURI, MultivaluedMap<String, String> queryParams) {
		logger.debug("*OMIT* GET request to {}", serviceURI);
		return null;
	}

	@Override
	public String sendPostRequest(String serviceURI, Object payload) {
		logger.debug("*OMIT* POST request to {}. Payload : {}", serviceURI, payload);
		return null;
	}

}
