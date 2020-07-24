package com.etherblood.a.templates.api.deserializers;

import com.etherblood.a.rules.templates.StatModifier;
import com.etherblood.a.templates.api.deserializers.filedtypes.CardId;
import com.etherblood.a.templates.api.deserializers.filedtypes.ComponentId;
import com.google.gson.JsonArray;
import com.google.gson.JsonDeserializationContext;
import com.google.gson.JsonDeserializer;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParseException;
import java.lang.reflect.Field;
import java.lang.reflect.Type;
import java.util.Map;
import java.util.function.ToIntFunction;
import com.etherblood.a.templates.api.TargetPredicate;

public class TemplateObjectDeserializer<T> implements JsonDeserializer<T> {

    private final Map<String, Class<? extends T>> classes;
    private final ToIntFunction<String> cardAliases;
    private final ToIntFunction<String> componentAliases;

    public TemplateObjectDeserializer(Map<String, Class<? extends T>> classes, ToIntFunction<String> cardAliases, ToIntFunction<String> componentAliases) {
        this.classes = classes;
        this.cardAliases = cardAliases;
        this.componentAliases = componentAliases;
    }

    @Override
    public T deserialize(JsonElement jsonElement, Type type, JsonDeserializationContext context) throws JsonParseException {
        JsonObject jsonObject = jsonElement.getAsJsonObject();
        String typeString = jsonObject.getAsJsonPrimitive("type").getAsString();
        Class<? extends T> clazz = classes.get(typeString);
        if (clazz == null) {
            throw new NullPointerException("No class found for " + typeString + ".");
        }
        for (Field field : clazz.getDeclaredFields()) {
            JsonElement fieldJsonValue = jsonObject.get(field.getName());
            if (field.getType() == int.class) {
                if (field.getAnnotation(CardId.class) != null) {
                    jsonObject.addProperty(field.getName(), cardAliases.applyAsInt(fieldJsonValue.getAsString()));
                } else if (field.getAnnotation(ComponentId.class) != null) {
                    jsonObject.addProperty(field.getName(), componentAliases.applyAsInt(fieldJsonValue.getAsString()));
                }
            } else if (field.getType() == int[].class) {
                if (field.getAnnotation(CardId.class) != null) {
                    JsonArray rawArray = fieldJsonValue.getAsJsonArray();
                    JsonArray intArray = new JsonArray();
                    for (JsonElement item : rawArray) {
                        intArray.add(cardAliases.applyAsInt(item.getAsString()));
                    }
                    jsonObject.add(field.getName(), intArray);
                }
            }
        }
        return context.deserialize(jsonElement, clazz);
    }

}
