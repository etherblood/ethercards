package com.etherblood.ethercards.gui.arrows;

import com.destrostudios.cardgui.BoardObjectModel;
import com.destrostudios.cardgui.TransformedBoardObject;
import com.jme3.math.ColorRGBA;
import java.util.Objects;

public class ColoredConnectionArrowModel extends BoardObjectModel {

    private TransformedBoardObject source;
    private TransformedBoardObject target;
    private ColorRGBA color;

    public ColoredConnectionArrowModel(TransformedBoardObject source, TransformedBoardObject target, ColorRGBA color) {
        this.source = Objects.requireNonNull(source);
        this.target = Objects.requireNonNull(target);
        this.color = Objects.requireNonNull(color);
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
