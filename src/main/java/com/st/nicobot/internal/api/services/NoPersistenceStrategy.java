package com.st.nicobot.internal.api.services;

import javax.ws.rs.core.MultivaluedMap;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.st.nicobot.api.services.PersistenceStrategy;

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
