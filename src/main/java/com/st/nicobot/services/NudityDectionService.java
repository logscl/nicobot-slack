package com.st.nicobot.services;

import org.codehaus.jettison.json.JSONObject;

/**
 * Created by Logs on 06-01-17.
 */
public interface NudityDectionService {

    JSONObject checkUrl(String url) throws Exception;
}
