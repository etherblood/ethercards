package com.etherblood.a.templates.api.deserializers;

import com.etherblood.a.rules.templates.StatModifier;
import com.google.gson.JsonDeserializationContext;
import com.google.gson.JsonDeserializer;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParseException;
import java.lang.reflect.Type;
import java.util.Map;

public class StatModifierDeserializer implements JsonDeserializer<StatModifier> {

    private final Map<String, Class<? extends StatModifier>> classes;

    public StatModifierDeserializer(Map<String, Class<? extends StatModifier>> classes) {
        this.classes = classes;
    }

    @Override
    public StatModifier deserialize(JsonElement jsonElement, Type type, JsonDeserializationContext context) throws JsonParseException {
        JsonObject jsonObject = jsonElement.getAsJsonObject();
        String typeString = jsonObject.getAsJsonPrimitive("type").getAsString();
        Class<? extends StatModifier> clazz = classes.get(typeString);
        if (clazz == null) {
            throw new NullPointerException("No class found for " + typeString + ".");
        }
        return context.deserialize(jsonElement, clazz);
    }

}
