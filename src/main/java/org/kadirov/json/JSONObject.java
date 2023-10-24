package org.kadirov.json;

import java.math.BigDecimal;
import java.util.Iterator;
import java.util.List;

public interface JSONObject<T> {
    T getSource();
    String getAsText(String field);
    Integer getAsInteger(String field);
    BigDecimal getAsBigDecimal(String field);

    List<JSONObject<T>> elements();
}
