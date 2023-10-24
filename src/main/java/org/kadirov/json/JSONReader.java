package org.kadirov.json;


import org.kadirov.json.exception.JsonProcessException;

public interface JSONReader<T> {
    JSONObject<T> parse(String json) throws JsonProcessException;
    <U> U fromJsonToObject(JSONObject<T> jsonObject, Class<U> clazz) throws JsonProcessException;
    <U> JSONObject<T> fromObjectToJson(U instance) throws JsonProcessException;
    String fromJsonToString(JSONObject<T> jsonObject) throws JsonProcessException;
    <U> String fromObjectToString(U instance) throws JsonProcessException;
}
