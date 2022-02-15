package com.etherblood.ethercards.gui.particles;

import com.destrostudios.cardgui.BoardObjectModel;
import com.jme3.math.ColorRGBA;

public class ColorModel extends BoardObjectModel {

    private ColorRGBA color = ColorRGBA.Gray;

    public ColorRGBA getColor() {
        return color;
    }

    public void setColor(ColorRGBA color) {
        updateIfNotEquals(this.color, color, () -> this.color = color);
    }
}
