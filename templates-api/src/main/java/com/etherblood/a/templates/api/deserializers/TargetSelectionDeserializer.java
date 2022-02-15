package com.etherblood.ethercards.templates.api.deserializers;

import com.etherblood.ethercards.rules.templates.TargetSelection;
import com.google.gson.JsonDeserializationContext;
import com.google.gson.JsonDeserializer;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParseException;
import java.lang.reflect.Type;
import java.util.Map;

public class TargetSelectionDeserializer implements JsonDeserializer<TargetSelection> {

    private final Map<String, Class<? extends TargetSelection>> classes;

    public TargetSelectionDeserializer(Map<String, Class<? extends TargetSelection>> classes) {
        this.classes = classes;
    }

    @Override
    public TargetSelection deserialize(JsonElement jsonElement, Type type, JsonDeserializationContext context) throws JsonParseException {
        JsonObject jsonObject = jsonElement.getAsJsonObject();
        String typeString = jsonObject.getAsJsonPrimitive("type").getAsString();
        Class<? extends TargetSelection> clazz = classes.get(typeString);
        if (clazz == null) {
            throw new NullPointerException("No class found for " + typeString + ".");
        }
        return context.deserialize(jsonElement, clazz);
    }

}
