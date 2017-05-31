package com.st.nicobot.internal.api.services;

import com.st.nicobot.api.services.PersistenceStrategy;
import com.st.nicobot.services.PropertiesService;
import com.st.nicobot.utils.NicobotProperty;
import com.sun.jersey.api.client.Client;
import com.sun.jersey.api.client.ClientResponse;
import com.sun.jersey.api.client.WebResource;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.MultivaluedMap;

@Service
public class RemoteAPIStrategy implements PersistenceStrategy {

	private static Logger logger = LoggerFactory.getLogger(RemoteAPIStrategy.class);

	@Autowired
	private PropertiesService propertiesService;

	/**
	 * Retourne un client pour effectuer une requete sur une url
	 * 
	 * @param url
	 * @return
	 */
	WebResource getClient(String url, MultivaluedMap<String, String> queryParams) {
		Client client = Client.create();

		WebResource resource = client.resource(url);

		resource = resource.queryParam("token", propertiesService.get(NicobotProperty.API_KEY));

		if(queryParams != null) {
			resource = resource.queryParams(queryParams);
		}

		return resource;
	}



	@Override
	public String sendGetRequest(final String serviceURI, final MultivaluedMap<String, String> queryParams) {
		try {
			WebResource resource = getClient(serviceURI, queryParams);

			ClientResponse response = resource.type(MediaType.APPLICATION_JSON).get(ClientResponse.class);

			String msgResp = parseResponse(response);

			if (response.getStatus() != 200) {
				logger.error("Erreur lors de la sauvegarde à distance. Reponse de l'API : {}", msgResp);
				msgResp = null;
			}

			return msgResp;
		} catch (Exception ex) {
			logger.error("", ex);
		}

		return null;
	}

	@Override
	public String sendPostRequest(final String serviceURI, Object payload) {
		try {
			ClientResponse response = getClient(serviceURI, null).type(MediaType.APPLICATION_JSON).post(ClientResponse.class,
					payload);

			String msgResp = parseResponse(response);
			// TODO : faire evol l'api pour ameliorer le retour des POST

			if (response.getStatus() != 201) {
				logger.error("Erreur lors de la sauvegarde à distance. Reponse de l'API : {}", msgResp);
				msgResp = null;
			}

			return msgResp;
		} catch (Exception ex) {
			logger.error("", ex);
		}

		return null;
	}

	String parseResponse(ClientResponse clientResponse) {
		try {
			return clientResponse.getEntity(String.class);
		} catch (Exception ex) {
			logger.error("{}", ex);
		}

		return null;
	}

}
