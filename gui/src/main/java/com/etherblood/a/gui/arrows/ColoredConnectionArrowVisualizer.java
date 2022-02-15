package com.etherblood.ethercards.gui.arrows;

import com.destrostudios.cardgui.JMonkeyUtil;
import com.destrostudios.cardgui.samples.boardobjects.targetarrow.SimpleTargetArrowMesh;
import com.destrostudios.cardgui.samples.boardobjects.targetarrow.SimpleTargetArrowSettings;
import com.destrostudios.cardgui.samples.boardobjects.targetarrow.SimpleTargetArrowUtil;
import com.destrostudios.cardgui.samples.visualization.SimpleAttachmentVisualizer;
import com.jme3.asset.AssetManager;
import com.jme3.math.Vector3f;
import com.jme3.scene.Geometry;

public class ColoredConnectionArrowVisualizer extends SimpleAttachmentVisualizer<ColoredConnectionArrow, Geometry> {

    private final SimpleTargetArrowSettings settings;

    public ColoredConnectionArrowVisualizer(SimpleTargetArrowSettings settings) {
        this.settings = settings;
    }

    @Override
    protected Geometry createVisualizationObject(AssetManager assetManager) {
        return SimpleTargetArrowUtil.create(assetManager, settings);
    }

    @Override
    protected void updateVisualizationObject(Geometry geometry, ColoredConnectionArrow boardObject, AssetManager assetManager) {
        ColoredConnectionArrowModel arrowModel = boardObject.getModel();
        Vector3f sourceLocation = arrowModel.getSource().position().getCurrentValue();
        Vector3f targetLocation = arrowModel.getTarget().position().getCurrentValue();
        geometry.setLocalTranslation(sourceLocation);
        geometry.getMaterial().setColor("Color", arrowModel.getColor());
        JMonkeyUtil.setLocalRotation(geometry, targetLocation.subtract(sourceLocation));
        SimpleTargetArrowMesh simpleTargetArrowMesh = (SimpleTargetArrowMesh) geometry.getMesh();
        simpleTargetArrowMesh.updatePositions(sourceLocation, targetLocation);
    }

}
