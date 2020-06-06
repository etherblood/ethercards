package com.etherblood.a.network.api;

import com.esotericsoftware.kryonet.Connection;

/**
 *
 * @author Philipp
 */
public interface MessageListener<T> {

    void onMessage(Connection connection, T message);
}
