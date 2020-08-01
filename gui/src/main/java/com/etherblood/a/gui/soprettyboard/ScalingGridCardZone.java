package com.etherblood.a.gui.soprettyboard;

import com.destrostudios.cardgui.CardZone;
import com.jme3.math.Quaternion;
import com.jme3.math.Vector2f;
import com.jme3.math.Vector3f;
import com.jme3.math.Vector4f;

public class ScalingGridCardZone extends CardZone {

    private final Vector4f zoneBounds;
    private final Vector2f cardSize;

    private int numCards = -1;
    private int columns;
    private int rows;
    private float scale;

    public ScalingGridCardZone(Vector3f position, Quaternion rotation, Vector4f zoneBounds, Vector2f cardSize) {
        super(position, rotation);
        this.zoneBounds = zoneBounds;
        this.cardSize = cardSize;
    }

    public ScalingGridCardZone(Vector3f position, Quaternion rotation, Vector3f scale, Vector4f zoneBounds, Vector2f cardSize) {
        super(position, rotation, scale);
        this.zoneBounds = zoneBounds;
        this.cardSize = cardSize;
    }

    @Override
    public Vector3f getCardPosition(Vector3f zonePosition) {
        return position().getCurrentValue().add(getCardRotation(zonePosition).mult(getLocalCardPosition(zonePosition)));
    }

    @Override
    protected Vector3f getLocalCardPosition(Vector3f zonePosition) {
        updateScalingGrid();
        int index = (int) zonePosition.x;
        int x = index % columns;
        int y = index / columns;

        float cellWidth = zoneBounds.z / columns;
        float cellHeight = zoneBounds.w / rows;

        return new Vector3f((x + 0.5f) * cellWidth + zoneBounds.x, 0, (y + 0.5f) * cellHeight + zoneBounds.y);
    }

    @Override
    protected Vector3f getLocalCardScale(Vector3f zonePosition) {
        updateScalingGrid();
        return Vector3f.UNIT_XYZ.mult(scale);
    }

    private void updateScalingGrid() {
        if (cards.size() == numCards) {
            return;
        }
        numCards = cards.size();
        float width = zoneBounds.z / cardSize.x;
        float height = zoneBounds.w / cardSize.y;

        float scaleNumerator = 1;
        float scaleDenominator = 1;
        int x = (int) width;
        int y = (int) height;
        while (x * y < numCards) {
            if (width * (y + 1) >= height * (x + 1)) {
                scaleNumerator = width;
                scaleDenominator = ++x;
            } else {
                scaleNumerator = height;
                scaleDenominator = ++y;
            }
        }
        assert scaleNumerator <= scaleDenominator;
        columns = x;
        rows = y;
        scale = scaleNumerator / scaleDenominator;
    }
}
