package com.etherblood.a.gui.particles;

import com.destrostudios.cardgui.samples.visualization.CustomAttachmentVisualization;
import com.jme3.scene.Geometry;

public class ColoredSphereVisualization extends CustomAttachmentVisualization<Geometry> {

    private final Geometry node;

    public ColoredSphereVisualization(Geometry node) {
        this.node = node;
    }

    @Override
    public Geometry getSpatial() {
        return node;
    }

}
