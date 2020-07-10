package com.etherblood.a.gui;

import com.jme3.collision.Collidable;
import com.jme3.collision.CollisionResults;
import com.jme3.scene.Node;

public class UncollidableNode extends Node {

    @Override
    public int collideWith(Collidable other, CollisionResults results) {
        return 0;
    }
}
