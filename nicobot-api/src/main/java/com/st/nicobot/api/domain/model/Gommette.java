package com.st.nicobot.api.domain.model;

import org.codehaus.jackson.annotate.JsonValue;
import org.joda.time.DateTime;

public class Gommette {

    private String userId;
    private String giverId;
    private String reason;
    private GommetteColor type;
    private Integer yesCount;
    private Integer noCount;
    private DateTime creationDate;
    private Boolean valid;

    public Gommette() {
    }

    public Gommette(String userId, String giverId, String reason, GommetteColor type, Integer yesCount, Integer noCount, DateTime creationDate, Boolean valid) {
        this.userId = userId;
        this.giverId = giverId;
        this.reason = reason;
        this.type = type;
        this.yesCount = yesCount;
        this.noCount = noCount;
        this.creationDate = creationDate;
        this.valid = valid;
    }

    public String getUserId() {
        return userId;
    }

    public String getGiverId() {
        return giverId;
    }

    public String getReason() {
        return reason;
    }

    public GommetteColor getType() {
        return type;
    }

    public Integer getYesCount() {
        return yesCount;
    }

    public Integer getNoCount() {
        return noCount;
    }

    public DateTime getCreationDate() {
        return creationDate;
    }

    public Boolean getValid() {
        return valid;
    }

    public enum GommetteColor {
        RED("rouge"),
        GREEN("verte");

        private String gommetteName;

        GommetteColor(String gommetteName) {
            this.gommetteName = gommetteName;
        }

        public String getGommetteName() {
            return gommetteName;
        }

        @JsonValue
        public int toValue() {
            return ordinal();
        }
    }
}
