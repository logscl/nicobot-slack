package com.st.nicobot.api.services;

import javax.ws.rs.core.MultivaluedMap;

public interface PersistenceStrategy {

	String sendGetRequest(String serviceURI, MultivaluedMap<String, String> queryParams);

	String sendPostRequest(String serviceURI, Object payload);

}
