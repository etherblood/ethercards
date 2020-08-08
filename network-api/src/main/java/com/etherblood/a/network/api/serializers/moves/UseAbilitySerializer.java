package com.etherblood.a.network.api.serializers.moves;

import com.esotericsoftware.kryo.Kryo;
import com.esotericsoftware.kryo.Serializer;
import com.esotericsoftware.kryo.io.Input;
import com.esotericsoftware.kryo.io.Output;
import com.etherblood.a.rules.moves.UseAbility;

public class UseAbilitySerializer extends Serializer<UseAbility> {

    @Override
    public void write(Kryo kryo, Output output, UseAbility t) {
        output.writeInt(t.player);
        output.writeInt(t.source);
        output.writeInt(t.target);
    }

    @Override
    public UseAbility read(Kryo kryo, Input input, Class<UseAbility> type) {
        return new UseAbility(input.readInt(), input.readInt(), input.readInt());
    }

}
