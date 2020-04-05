package com.etherblood.a.templates;

import com.etherblood.a.entities.collections.IntMap;
import com.google.gson.JsonDeserializationContext;
import com.google.gson.JsonDeserializer;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParseException;
import com.google.gson.JsonPrimitive;
import java.lang.reflect.Type;
import java.util.Map;

public class ComponentsDeserializer implements JsonDeserializer<IntMap> {

    private final Map<String, Integer> componentAliases;

    public ComponentsDeserializer(Map<String, Integer> componentAliases) {
        this.componentAliases = componentAliases;
    }

    @Override
    public IntMap deserialize(JsonElement element, Type type, JsonDeserializationContext context) throws JsonParseException {
        IntMap result = new IntMap();
        JsonObject components = element.getAsJsonObject();
        for (Map.Entry<String, JsonElement> entry : components.entrySet()) {
            int component = componentAliases.get(entry.getKey());
            JsonElement value = entry.getValue();
            if (value.isJsonNull()) {
                result.remove(component);
                continue;
            }
            if (value.isJsonPrimitive()) {
                JsonPrimitive primitive = value.getAsJsonPrimitive();
                if (primitive.isNumber()) {
                    result.set(component, primitive.getAsInt());
                    continue;
                }
                if (primitive.isBoolean()) {
                    result.set(component, primitive.getAsBoolean() ? 1 : 0);
                    continue;
                }
            }
            throw new UnsupportedOperationException(value.toString());
        }
        return result;
    }

}
