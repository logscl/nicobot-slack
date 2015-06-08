package com.st.nicobot.api.domain.model;

import java.io.Serializable;
import java.util.Map;

/**
 * Created by Logs on 08-06-15.
 */
public class GreetersMemory implements Serializable {
    private int collectionWeek;
    private Map<String, Map<String, Integer>> weeklyGreeters;
    private Map<String, Map<String, Integer>> allTimeGreeters;

    public int getCollectionWeek() {
        return collectionWeek;
    }

    public void setCollectionWeek(int collectionWeek) {
        this.collectionWeek = collectionWeek;
    }

    public Map<String, Map<String, Integer>> getWeeklyGreeters() {
        return weeklyGreeters;
    }

    public void setWeeklyGreeters(Map<String, Map<String, Integer>> weeklyGreeters) {
        this.weeklyGreeters = weeklyGreeters;
    }

    public Map<String, Map<String, Integer>> getAllTimeGreeters() {
        return allTimeGreeters;
    }

    public void setAllTimeGreeters(Map<String, Map<String, Integer>> allTimeGreeters) {
        this.allTimeGreeters = allTimeGreeters;
    }
}
