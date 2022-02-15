package com.etherblood.ethercards.network.api.serializers.moves;

import com.esotericsoftware.kryo.Kryo;
import com.esotericsoftware.kryo.Serializer;
import com.esotericsoftware.kryo.io.Input;
import com.esotericsoftware.kryo.io.Output;
import com.etherblood.ethercards.rules.moves.Cast;

public class CastSerializer extends Serializer<Cast> {

    @Override
    public void write(Kryo kryo, Output output, Cast t) {
        output.writeInt(t.player);
        output.writeInt(t.source);
        if (t.target != null) {
            output.writeBoolean(true);
            output.writeInt(t.target);
        } else {
            output.writeBoolean(false);
        }
    }

    @Override
    public Cast read(Kryo kryo, Input input, Class<Cast> type) {
        int player = input.readInt();
        int source = input.readInt();
        boolean hasTarget = input.readBoolean();
        Integer target = null;
        if (hasTarget) {
            target = input.readInt();
        }
        return new Cast(player, source, target);
    }

}
