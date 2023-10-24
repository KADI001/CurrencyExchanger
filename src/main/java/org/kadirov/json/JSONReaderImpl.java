package org.kadirov.json;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.ObjectReader;
import com.fasterxml.jackson.databind.ObjectWriter;
import org.kadirov.json.exception.JsonProcessException;

public class JSONReaderImpl implements JSONReader<JsonNode> {

    private final ObjectMapper objectMapper;

    public JSONReaderImpl() {
        objectMapper = new ObjectMapper();
    }

    @Override
    public JSONObject<JsonNode> parse(String json) throws JsonProcessException {
        ObjectReader reader = objectMapper.reader();
        JsonNode jsonNode;

        try {
            jsonNode = reader.readTree(json);
        } catch (JsonProcessingException jpe) {
            throw new JsonProcessException("Error occurred during parsing json", jpe);
        }
        return new JSONObjectImpl(jsonNode);
    }

    @Override
    public <U> U fromJsonToObject(JSONObject<JsonNode> jsonObject, Class<U> clazz) throws JsonProcessException {
        ObjectReader reader = objectMapper.reader();
        U object;

        try {
            object = reader.treeToValue(jsonObject.getSource(), clazz);
        } catch (JsonProcessingException jpe) {
            throw new JsonProcessException("Error occurred during mapping json to object", jpe);
        }

        return object;
    }

    @Override
    public <U> JSONObject<JsonNode> fromObjectToJson(U instance) throws JsonProcessException {
        JsonNode jsonNode = objectMapper.valueToTree(instance);
        return new JSONObjectImpl(jsonNode);
    }

    @Override
    public String fromJsonToString(JSONObject<JsonNode> jsonObject) throws JsonProcessException {
        ObjectWriter writer = objectMapper.writer();
        String jsonString;

        try {
            jsonString = writer.writeValueAsString(jsonObject.getSource());
        } catch (JsonProcessingException e) {
            throw new JsonProcessException("Error occurred during converting from json object to string", e);
        }

        return jsonString;
    }

    @Override
    public <U> String fromObjectToString(U instance) throws JsonProcessException {
        return fromJsonToString(fromObjectToJson(instance));
    }
}
