package com.etherblood.ethercards.gui.arrows;

import com.destrostudios.cardgui.BoardObject;
import com.destrostudios.cardgui.TransformedBoardObject;
import com.jme3.math.ColorRGBA;

public class ColoredConnectionArrow extends BoardObject<ColoredConnectionArrowModel> {

    public ColoredConnectionArrow(TransformedBoardObject source, TransformedBoardObject target, ColorRGBA color) {
        super(new ColoredConnectionArrowModel(source, target, color));
    }

    @Override
    public boolean needsVisualizationUpdate() {
        return true;
    }
}
