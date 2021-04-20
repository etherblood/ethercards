package com.etherblood.a.gui;

import com.etherblood.a.game.events.api.GameEventListener;
import java.util.ArrayDeque;
import java.util.Deque;
import java.util.Queue;

public class QueueEventListener implements GameEventListener {

    private final Deque<Object> events = new ArrayDeque<>();

    @Override
    public void fire(Object event) {
        events.add(event);
    }

    public Queue<Object> getQueue() {
        return events;
    }

}
