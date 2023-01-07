package com.github.zavier.jsonpath;

import com.google.gson.*;
import com.jayway.jsonpath.JsonPath;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Iterator;
import java.util.Map;
import java.util.Set;

public class GsonConverter {

    private static final Logger logger = LoggerFactory.getLogger(GsonConverter.class);

    private Gson gson = new Gson();

    public JsonElement toJson(JsonElement templateJsonElement, Object data) {
        if (templateJsonElement.isJsonObject()) {
            final JsonObject jsonObject = templateJsonElement.getAsJsonObject();
            final Set<Map.Entry<String, JsonElement>> entries = jsonObject.entrySet();
            for (Map.Entry<String, JsonElement> entry : entries) {
                final String key = entry.getKey();
                final JsonElement value = toJson(entry.getValue(), data);
                jsonObject.add(key, value);
            }
            return jsonObject;
        } else if (templateJsonElement.isJsonArray()) {
            final JsonArray jsonArray = templateJsonElement.getAsJsonArray();
            final Iterator<JsonElement> iterator = jsonArray.iterator();
            int index = 0;
            while (iterator.hasNext()) {
                final JsonElement next = iterator.next();
                jsonArray.set(index++, toJson(next, data));
            }
            return jsonArray;
        } else if (templateJsonElement.isJsonPrimitive()) {
            JsonPrimitive primitive = (JsonPrimitive) templateJsonElement;
            final boolean isString = primitive.isString();
            if (isString) {
                final String string = primitive.getAsString();
                if (string.startsWith("$")) {
                    final Object read;
                    try {
                        read = JsonPath.read(data, string);
                        return gson.toJsonTree(read);
                    } catch (Exception e) {
                        logger.warn("jsonpath获取失败", e);
                        return null;
                    }
                }
            }

            return primitive;

        } else if (templateJsonElement.isJsonNull()) {
            return null;
        }

        throw new RuntimeException("不支持的数据类型:" + templateJsonElement);
    }


}
