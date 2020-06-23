package com.etherblood.a.gui.arrows;

import com.destrostudios.cardgui.BoardObjectModel;
import com.destrostudios.cardgui.TransformedBoardObject;
import com.jme3.math.ColorRGBA;

public class ColoredConnectionArrowModel extends BoardObjectModel {

    private TransformedBoardObject source;
    private TransformedBoardObject target;
    private ColorRGBA color;

    public ColoredConnectionArrowModel(TransformedBoardObject source, TransformedBoardObject target, ColorRGBA color) {
        this.source = source;
        this.target = target;
        this.color = color;
    }

    public void setSource(TransformedBoardObject source) {
        this.source = source;
    }

    public void setTarget(TransformedBoardObject target) {
        this.target = target;
    }

    public void setColor(ColorRGBA color) {
        this.color = color;
    }

    public TransformedBoardObject getSource() {
        return source;
    }

    public TransformedBoardObject getTarget() {
        return target;
    }

    public ColorRGBA getColor() {
        return color;
    }
}
