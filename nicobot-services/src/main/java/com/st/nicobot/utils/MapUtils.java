package com.st.nicobot.utils;

import java.util.LinkedHashMap;
import java.util.Map;
import java.util.SortedSet;
import java.util.TreeSet;

/**
 * Created by Logs on 19-08-15.
 */
public class MapUtils {

    public static <K, V extends Comparable<? super V>> Map<K, V> entriesSortedByValues(Map<K, V> map) {
        SortedSet<Map.Entry<K, V>> sortedEntries = new TreeSet<>(
                (e1, e2) -> {
                    int res = e2.getValue().compareTo(e1.getValue());
                    return res != 0 ? res : 1;
                }
        );
        sortedEntries.addAll(map.entrySet());
        LinkedHashMap<K, V> outputMap = new LinkedHashMap<>();
        for (Map.Entry<K, V> entry : sortedEntries) {
            outputMap.put(entry.getKey(), entry.getValue());
        }
        return outputMap;
    }
}
