package com.etherblood.a.network.api;

import com.esotericsoftware.kryonet.Connection;

public interface MessageListener<T> {

    void onMessage(Connection connection, T message);
}
