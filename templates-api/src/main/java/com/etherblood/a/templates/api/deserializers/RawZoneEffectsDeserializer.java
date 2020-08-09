package com.etherblood.a.templates.api.deserializers;

import com.etherblood.a.entities.collections.IntMap;
import com.etherblood.a.rules.templates.ActivatedAbility;
import com.etherblood.a.rules.templates.Effect;
import com.etherblood.a.rules.templates.StatModifier;
import com.etherblood.a.templates.api.model.RawZoneEffects;
import com.google.gson.JsonDeserializationContext;
import com.google.gson.JsonDeserializer;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParseException;
import com.google.gson.reflect.TypeToken;
import java.lang.reflect.Type;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.function.ToIntFunction;

public class RawZoneEffectsDeserializer implements JsonDeserializer<RawZoneEffects> {

    private final ToIntFunction<String> componentAliases;

    public RawZoneEffectsDeserializer(ToIntFunction<String> componentAliases) {
        this.componentAliases = componentAliases;
    }

    @Override
    public RawZoneEffects deserialize(JsonElement je, Type type, JsonDeserializationContext jdc) throws JsonParseException {
        JsonObject obj = je.getAsJsonObject();
        RawZoneEffects result = new RawZoneEffects();
        result.activated = jdc.deserialize(obj.get("activated"), ActivatedAbility.class);
        result.cast = jdc.deserialize(obj.get("cast"), ActivatedAbility.class);
        result.components = jdc.deserialize(obj.get("components"), IntMap.class);
        result.componentModifiers = new LinkedHashMap<>();
        if (obj.has("componentModifiers")) {
            for (Map.Entry<String, JsonElement> entry : obj.getAsJsonObject("componentModifiers").entrySet()) {
                result.componentModifiers.put(componentAliases.applyAsInt(entry.getKey()), jdc.deserialize(entry.getValue(), new TypeToken<List<StatModifier>>() {
                }.getType()));
            }
        }
        result.passive = new LinkedHashMap<>();
        if (obj.has("passive")) {
            for (Map.Entry<String, JsonElement> entry : obj.getAsJsonObject("passive").entrySet()) {
                result.passive.put(componentAliases.applyAsInt(entry.getKey()), jdc.deserialize(entry.getValue(), new TypeToken<List<Effect>>() {
                }.getType()));
            }
        }
        return result;
    }

}
