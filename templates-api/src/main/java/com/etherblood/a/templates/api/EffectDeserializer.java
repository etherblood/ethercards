package com.etherblood.a.templates.api;

import com.etherblood.a.rules.templates.instances.effects.filedtypes.CardId;
import com.etherblood.a.rules.templates.instances.effects.Effect;
import com.google.gson.JsonDeserializationContext;
import com.google.gson.JsonDeserializer;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParseException;
import java.lang.reflect.Field;
import java.lang.reflect.Type;
import java.util.Map;
import java.util.function.ToIntFunction;

public class EffectDeserializer implements JsonDeserializer<Effect> {

    private final Map<String, Class<? extends Effect>> classes;
    private final ToIntFunction<String> cardAliases;

    public EffectDeserializer(Map<String, Class<? extends Effect>> classes, ToIntFunction<String> cardAliases) {
        this.classes = classes;
        this.cardAliases = cardAliases;
    }

    @Override
    public Effect deserialize(JsonElement jsonElement, Type type, JsonDeserializationContext context) throws JsonParseException {
        JsonObject jsonObject = jsonElement.getAsJsonObject();
        String typeString = jsonObject.getAsJsonPrimitive("type").getAsString();
        Class<? extends Effect> clazz = classes.get(typeString);
        if (clazz == null) {
            throw new NullPointerException("No class found for " + typeString + ".");
        }
        for (Field field : clazz.getDeclaredFields()) {
            JsonElement fieldJsonValue = jsonObject.get(field.getName());
            if (field.getType() == int.class) {
                if (field.getAnnotation(CardId.class) != null) {
                    jsonObject.addProperty(field.getName(), cardAliases.applyAsInt(fieldJsonValue.getAsString()));
                }
            }
        }
        return context.deserialize(jsonElement, clazz);
    }

}
