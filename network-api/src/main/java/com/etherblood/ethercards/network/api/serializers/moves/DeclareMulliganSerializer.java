package com.etherblood.ethercards.network.api.serializers.moves;

import com.esotericsoftware.kryo.Kryo;
import com.esotericsoftware.kryo.Serializer;
import com.esotericsoftware.kryo.io.Input;
import com.esotericsoftware.kryo.io.Output;
import com.etherblood.ethercards.rules.moves.DeclareMulligan;

public class DeclareMulliganSerializer extends Serializer<DeclareMulligan> {

    @Override
    public void write(Kryo kryo, Output output, DeclareMulligan t) {
        output.writeInt(t.player);
        output.writeInt(t.card);
    }

    @Override
    public DeclareMulligan read(Kryo kryo, Input input, Class<DeclareMulligan> type) {
        return new DeclareMulligan(input.readInt(), input.readInt());
    }

}
