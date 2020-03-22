package com.etherblood.a.templates;

import com.etherblood.a.rules.templates.casteffects.CardId;
import com.etherblood.a.rules.templates.casteffects.CastEffect;
import com.etherblood.a.rules.templates.casteffects.MinionId;
import com.google.gson.JsonDeserializationContext;
import com.google.gson.JsonDeserializer;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParseException;
import java.lang.reflect.Field;
import java.lang.reflect.Type;
import java.util.Map;
import java.util.function.ToIntFunction;

public class CastEffectDeserializer implements JsonDeserializer<CastEffect>{

    private final Map<String, Class<? extends CastEffect>> classes;
    private final ToIntFunction<String> minionAliases;
    private final ToIntFunction<String> cardAliases;

    public CastEffectDeserializer(Map<String, Class<? extends CastEffect>> classes, ToIntFunction<String> minionAliases, ToIntFunction<String> cardAliases) {
        this.classes = classes;
        this.minionAliases = minionAliases;
        this.cardAliases = cardAliases;
    }
    
    @Override
    public CastEffect deserialize(JsonElement jsonElement, Type type, JsonDeserializationContext context) throws JsonParseException {
        JsonObject jsonObject = jsonElement.getAsJsonObject();
        String typeString = jsonObject.getAsJsonPrimitive("type").getAsString();
        Class<? extends CastEffect> clazz = classes.get(typeString);
        for (Field field : clazz.getDeclaredFields()) {
            JsonElement fieldJsonValue = jsonObject.get(field.getName());
            if(field.getType() == int.class) {
                if(field.getAnnotation(MinionId.class) != null) {
                    jsonObject.addProperty(field.getName(), minionAliases.applyAsInt(fieldJsonValue.getAsString()));
                } else if(field.getAnnotation(CardId.class) != null) {
                    jsonObject.addProperty(field.getName(), cardAliases.applyAsInt(fieldJsonValue.getAsString()));
                }
            }
        }
        return context.deserialize(jsonElement, clazz);
    }

}
