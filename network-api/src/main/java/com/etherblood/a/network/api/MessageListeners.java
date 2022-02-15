package com.etherblood.ethercards.network.api;

import com.esotericsoftware.kryonet.Connection;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.concurrent.ConcurrentHashMap;

public class MessageListeners {

    private final Map<Class<?>, List<MessageListener<?>>> listeners = new ConcurrentHashMap<>();

    public <T> void register(Class<T> clazz, MessageListener<T> listener) {
        listeners.computeIfAbsent(clazz, x -> new ArrayList<>()).add(listener);
    }

    public void onMessage(Connection connection, Object message) {
        Objects.requireNonNull(message, "Null messages are not supported.");
        List<MessageListener<?>> list = listeners.get(message.getClass());
        if (list != null) {
            for (MessageListener listener : list) {
                listener.onMessage(connection, message);
            }
        }
    }
}
