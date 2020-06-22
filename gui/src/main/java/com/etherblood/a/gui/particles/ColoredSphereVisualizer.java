package com.etherblood.a.gui.particles;

import com.destrostudios.cardgui.samples.visualization.CustomAttachmentVisualizer;
import com.jme3.asset.AssetManager;
import com.jme3.material.Material;
import com.jme3.math.ColorRGBA;
import com.jme3.scene.Geometry;
import com.jme3.scene.shape.Sphere;

public class ColoredSphereVisualizer extends CustomAttachmentVisualizer<ColoredSphere, Geometry, ColoredSphereVisualization> {

    @Override
    protected ColoredSphereVisualization createVisualizationObject(AssetManager assetManager) {
        Geometry geometry = new Geometry();
        Material material = new Material(assetManager, "Common/MatDefs/Misc/Unshaded.j3md");
        material.setColor("Color", ColorRGBA.Gray);
        geometry.setMaterial(material);
        geometry.setMesh(new Sphere(8, 16, 0.25f));
        return new ColoredSphereVisualization(geometry);
    }

    @Override
    protected void updateVisualizationObject(ColoredSphereVisualization visualization, ColoredSphere boardObject, AssetManager assetManager) {
        visualization.getSpatial().getMaterial().setColor("Color", boardObject.getModel().getColor());
    }

}
