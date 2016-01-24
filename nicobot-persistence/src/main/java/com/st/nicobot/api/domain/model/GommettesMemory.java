package com.st.nicobot.api.domain.model;

import com.st.nicobot.bot.utils.GommetteColor;

import java.io.Serializable;
import java.util.Map;

/**
 * Created by Logs on 17-08-15.
 */
public class GommettesMemory implements Serializable {

    private static final long serialVersionUID = -7649852437645791355L;

    private Map<String, Map<GommetteColor, Integer>> gommettes;

    public void setGommettes(Map<String, Map<GommetteColor, Integer>> gommettes) {
        this.gommettes = gommettes;
    }

    public Map<String, Map<GommetteColor, Integer>> getGommettes() {
        return gommettes;
    }
}
