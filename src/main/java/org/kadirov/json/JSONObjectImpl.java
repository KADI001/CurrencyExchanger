package org.kadirov.json;

import com.fasterxml.jackson.core.io.BigDecimalParser;
import com.fasterxml.jackson.databind.JsonNode;

import java.math.BigDecimal;
import java.text.DecimalFormat;
import java.util.*;

public class JSONObjectImpl implements JSONObject<JsonNode> {

    private final JsonNode jsonNode;

    public JSONObjectImpl(JsonNode jsonNode) {
        this.jsonNode = jsonNode;
    }

    @Override
    public JsonNode getSource() {
        return jsonNode;
    }

    @Override
    public String getAsText(String field) {
        JsonNode node = jsonNode.get(field);
        return node != null ? node.asText() : null;
    }

    @Override
    public Integer getAsInteger(String field) {
        JsonNode node = jsonNode.get(field);
        return node != null ? node.asInt() : null;
    }

    @Override
    public BigDecimal getAsBigDecimal(String field) {
        String textValue = getAsText(field);
        return BigDecimalParser.parse(textValue);
    }

    @Override
    public List<JSONObject<JsonNode>> elements() {
        List<JSONObject<JsonNode>> result = new ArrayList<>();
        Iterator<JsonNode> elements = jsonNode.elements();
        elements.forEachRemaining(jd -> result.add(new JSONObjectImpl(jd)));
        return result;
    }
}
