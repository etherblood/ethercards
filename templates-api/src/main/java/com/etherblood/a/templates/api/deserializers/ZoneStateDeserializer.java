package com.etherblood.a.templates.api.deserializers;

import com.etherblood.a.entities.collections.IntMap;
import com.etherblood.a.rules.templates.ActivatedAbility;
import com.etherblood.a.rules.templates.Effect;
import com.etherblood.a.rules.templates.StatModifier;
import com.etherblood.a.rules.templates.ZoneState;
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

public class ZoneStateDeserializer implements JsonDeserializer<ZoneState> {

    private final ToIntFunction<String> componentAliases;

    public ZoneStateDeserializer(ToIntFunction<String> componentAliases) {
        this.componentAliases = componentAliases;
    }

    @Override
    public ZoneState deserialize(JsonElement je, Type type, JsonDeserializationContext jdc) throws JsonParseException {
        JsonObject obj = je.getAsJsonObject();
        ActivatedAbility activated = jdc.deserialize(obj.get("activated"), ActivatedAbility.class);
        ActivatedAbility cast = jdc.deserialize(obj.get("cast"), ActivatedAbility.class);
        IntMap components = jdc.deserialize(obj.get("components"), IntMap.class);
        LinkedHashMap<Integer, List<StatModifier>> modifiers = new LinkedHashMap<>();
        if (obj.has("componentModifiers")) {
            for (Map.Entry<String, JsonElement> entry : obj.getAsJsonObject("componentModifiers").entrySet()) {
                modifiers.put(componentAliases.applyAsInt(entry.getKey()), jdc.deserialize(entry.getValue(), new TypeToken<List<StatModifier>>() {
                }.getType()));
            }
        }
        LinkedHashMap<Integer, List<Effect>> passive = new LinkedHashMap<>();
        if (obj.has("passive")) {
            for (Map.Entry<String, JsonElement> entry : obj.getAsJsonObject("passive").entrySet()) {
                passive.put(componentAliases.applyAsInt(entry.getKey()), jdc.deserialize(entry.getValue(), new TypeToken<List<Effect>>() {
                }.getType()));
            }
        }
        return new ZoneState(components, modifiers, cast, activated, passive);
    }

}
