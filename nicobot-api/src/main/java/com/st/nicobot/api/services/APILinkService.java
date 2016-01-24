package com.st.nicobot.api.services;

import com.st.nicobot.api.domain.model.Link;

/**
 * @author Julien
 *
 */
public interface APILinkService {

	/**
	 * Crée (ou met a jour le cas echeant) un lien.
	 * @param link
	 * 		Un lien sous forme de chaine de caracteres
	 * @return
	 * 		Un lien
	 */
	Link createOrUpdateLink(String link);
	
	/**
	 * Retourne le lien
	 * @param link
	 *		Un lien sous forme de chaine de caracteres 
	 * @return
	 * 		Un lien
	 */
	Link getLink(String link);
	
}
