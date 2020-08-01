package com.etherblood.a.gui.soprettyboard;

import com.destrostudios.cardgui.zones.SimpleIntervalZone;
import com.etherblood.a.gui.PlayerZones;
import com.jme3.math.Quaternion;
import com.jme3.math.Vector2f;
import com.jme3.math.Vector3f;
import com.jme3.math.Vector4f;

public class PlayerZonesTemplate {

    private final Vector4f bounds;
    private final Vector2f cardSize;

    public PlayerZonesTemplate(Vector4f bounds, Vector2f cardSize) {
        this.bounds = bounds;
        this.cardSize = cardSize;
    }

    public PlayerZones create(Vector3f position, Quaternion rotation) {
        float left = bounds.x;
        float width = bounds.z;
        float upper = bounds.y;
        float height = bounds.w;
        float lower = upper + height;
        float cardWidth = cardSize.x;
        float cardHeight = cardSize.y;
        float halfCardWidth = cardWidth / 2;
        float halfCardHeight = cardHeight / 2;

        Vector4f battleBounds = new Vector4f(left + cardWidth, upper, width - cardWidth, height - cardHeight);
        Vector4f handBounds = new Vector4f(left + cardWidth, lower - cardHeight, width - cardWidth, cardHeight);

        ScalingGridCardZone boardZone = new ScalingGridCardZone(rotation.mult(position), rotation, battleBounds, cardSize);
        ScalingGridCardZone handZone = new ScalingGridCardZone(rotation.mult(position), rotation, handBounds, cardSize);

        Vector3f graveyardOffset = new Vector3f(left + halfCardWidth, 0, lower - 2 * cardHeight + halfCardHeight);
        rotation.multLocal(graveyardOffset);
        graveyardOffset.addLocal(position);
        
        Vector3f libraryOffset = new Vector3f(left + halfCardWidth, 0, lower - cardHeight + halfCardHeight);
        rotation.multLocal(libraryOffset);
        libraryOffset.addLocal(position);
        
        Vector3f stackInterval = new Vector3f(0, 0.01f, 0);
        SimpleIntervalZone graveyardZone = new SimpleIntervalZone(graveyardOffset, rotation, stackInterval);
        SimpleIntervalZone libraryZone = new SimpleIntervalZone(libraryOffset, rotation, stackInterval);
        PlayerZones zones = new PlayerZones(libraryZone, graveyardZone, handZone, boardZone);
        return zones;
    }
}
