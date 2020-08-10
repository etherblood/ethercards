package com.etherblood.a.templates.api.deserializers;

import com.etherblood.a.entities.collections.IntMap;
import com.etherblood.a.rules.templates.Effect;
import com.etherblood.a.rules.templates.StatModifier;
import com.etherblood.a.templates.api.model.RawActivatedAbility;
import com.etherblood.a.templates.api.model.RawZoneState;
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

public class RawZoneStateDeserializer implements JsonDeserializer<RawZoneState> {

    private final ToIntFunction<String> componentAliases;

    public RawZoneStateDeserializer(ToIntFunction<String> componentAliases) {
        this.componentAliases = componentAliases;
    }

    @Override
    public RawZoneState deserialize(JsonElement je, Type type, JsonDeserializationContext jdc) throws JsonParseException {
        JsonObject obj = je.getAsJsonObject();
        RawZoneState result = new RawZoneState();
        result.activated = jdc.deserialize(obj.get("activated"), RawActivatedAbility.class);
        result.cast = jdc.deserialize(obj.get("cast"), RawActivatedAbility.class);
        result.components = jdc.deserialize(obj.get("components"), IntMap.class);
        if (result.components == null) {
            result.components = new IntMap();
        }
        result.statModifiers = new LinkedHashMap<>();
        if (obj.has("statModifiers")) {
            for (Map.Entry<String, JsonElement> entry : obj.getAsJsonObject("statModifiers").entrySet()) {
                result.statModifiers.put(componentAliases.applyAsInt(entry.getKey()), jdc.deserialize(entry.getValue(), StatModifier.class));
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
