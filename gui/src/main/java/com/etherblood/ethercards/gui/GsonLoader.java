package com.etherblood.ethercards.gui;

import com.etherblood.ethercards.network.api.RecordTypeAdapterFactory;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonElement;
import com.jme3.asset.AssetInfo;
import com.jme3.asset.AssetLoader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;

public class GsonLoader implements AssetLoader {

    private final Gson gson = new GsonBuilder().registerTypeAdapterFactory(new RecordTypeAdapterFactory()).create();

    @Override
    public JsonElement load(AssetInfo assetInfo) throws IOException {
        try (InputStreamReader reader = new InputStreamReader(assetInfo.openStream(), StandardCharsets.UTF_8)) {
            return gson.fromJson(reader, JsonElement.class);
        }
    }

}
