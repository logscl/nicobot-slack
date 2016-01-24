package com.st.nicobot.bot.utils;

/**
 * Created by Logs on 17-08-15.
 */
public enum GommetteColor {
    RED("rouge"),
    GREEN("verte");

    private String gommetteName;

    GommetteColor(String gommetteName) {
        this.gommetteName = gommetteName;
    }

    public static GommetteColor getGommetteByName(String name) {
        for(GommetteColor gommette : GommetteColor.values()) {
            if(gommette.gommetteName.equals(name.toLowerCase())) {
                return gommette;
            }
        }
        return null;
    }

    public String getGommetteName() {
        return gommetteName;
    }
}
