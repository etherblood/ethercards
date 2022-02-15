package com.etherblood.ethercards.network.api.serializers.moves;

import com.esotericsoftware.kryo.Kryo;
import com.esotericsoftware.kryo.Serializer;
import com.esotericsoftware.kryo.io.Input;
import com.esotericsoftware.kryo.io.Output;
import com.etherblood.ethercards.rules.moves.UseAbility;

public class UseAbilitySerializer extends Serializer<UseAbility> {

    @Override
    public void write(Kryo kryo, Output output, UseAbility t) {
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
    public UseAbility read(Kryo kryo, Input input, Class<UseAbility> type) {
        int player = input.readInt();
        int source = input.readInt();
        boolean hasTarget = input.readBoolean();
        Integer target = null;
        if (hasTarget) {
            target = input.readInt();
        }
        return new UseAbility(player, source, target);
    }

}
