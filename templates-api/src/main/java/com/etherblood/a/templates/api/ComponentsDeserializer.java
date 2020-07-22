package com.etherblood.a.templates.api;

import com.etherblood.a.entities.collections.IntMap;
import com.google.gson.JsonDeserializationContext;
import com.google.gson.JsonDeserializer;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParseException;
import com.google.gson.JsonPrimitive;
import java.lang.reflect.Type;
import java.util.Map;
import java.util.function.Function;

public class ComponentsDeserializer implements JsonDeserializer<IntMap> {

    private final Function<String, Integer> cardAliases;
    private final Function<String, Integer> componentAliases;

    public ComponentsDeserializer(Function<String, Integer> cardAliases, Function<String, Integer> componentAliases) {
        this.cardAliases = cardAliases;
        this.componentAliases = componentAliases;
    }

    @Override
    public IntMap deserialize(JsonElement element, Type type, JsonDeserializationContext context) throws JsonParseException {
        IntMap result = new IntMap();
        JsonObject components = element.getAsJsonObject();
        for (Map.Entry<String, JsonElement> entry : components.entrySet()) {
            Integer component = componentAliases.apply(entry.getKey());
            if (component == null) {
                throw new NullPointerException("No component found for " + entry.getKey() + ".");
            }
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
                if (primitive.isString()) {
                    result.set(component, cardAliases.apply(primitive.getAsString()));
                    continue;
                }
            }
            throw new UnsupportedOperationException(value.toString());
        }
        return result;
    }

}
