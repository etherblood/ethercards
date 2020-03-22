package com.etherblood.a.gui.soprettyboard;

import com.destrostudios.cardgui.transformations.ConstantButTargetedTransformation;
import com.destrostudios.cardgui.transformations.DynamicTransformation;
import com.destrostudios.cardgui.transformations.SimpleTargetPositionTransformation3f;
import com.destrostudios.cardgui.transformations.SimpleTargetRotationTransformation;
import com.destrostudios.cardgui.transformations.speeds.TimeBasedPositionTransformationSpeed3f;
import com.destrostudios.cardgui.transformations.speeds.TimeBasedRotationTransformationSpeed;
import com.jme3.app.Application;
import com.jme3.app.state.AppStateManager;
import com.jme3.input.FlyByCamera;
import com.jme3.math.Quaternion;
import com.jme3.math.Vector3f;
import com.jme3.renderer.Camera;

public class CameraAppState extends MyBaseAppState {

    private DynamicTransformation<Vector3f> positionTransformation;
    private DynamicTransformation<Quaternion> rotationTransformation;
    private Vector3f tmpPosition = new Vector3f();
    private Quaternion tmpRotation = new Quaternion();

    @Override
    public void initialize(AppStateManager stateManager, Application application) {
        super.initialize(stateManager, application);
        Camera camera = mainApplication.getCamera();
        camera.setFrustumPerspective(45, (float) camera.getWidth() / camera.getHeight(), 0.01f, 1000);
        FlyByCamera flyByCamera = mainApplication.getFlyByCamera();
        flyByCamera.setMoveSpeed(30);
        flyByCamera.setEnabled(false);
        moveTo(camera.getLocation(), camera.getRotation());
    }

    public void moveTo(Vector3f position, Quaternion rotation) {
        positionTransformation = new ConstantButTargetedTransformation<>(position);
        rotationTransformation = new ConstantButTargetedTransformation<>(rotation);
    }

    public void moveTo(Vector3f position, Quaternion rotation, float duration) {
        positionTransformation = new SimpleTargetPositionTransformation3f(position, new TimeBasedPositionTransformationSpeed3f(duration));
        rotationTransformation = new SimpleTargetRotationTransformation(rotation, new TimeBasedRotationTransformationSpeed(duration));
        positionTransformation.setCurrentValue(mainApplication.getCamera().getLocation());
        rotationTransformation.setCurrentValue(mainApplication.getCamera().getRotation());
    }

    @Override
    public void update(float lastTimePerFrame) {
        super.update(lastTimePerFrame);
        if (!isFreeCameraEnabled()) {
            positionTransformation.update(lastTimePerFrame);
            rotationTransformation.update(lastTimePerFrame);
            mainApplication.getCamera().setLocation(positionTransformation.getCurrentValue());
            mainApplication.getCamera().setRotation(rotationTransformation.getCurrentValue());
        }
    }

    public void setFreeCameraEnabled(boolean enabled) {
        FlyByCamera flyByCamera = mainApplication.getFlyByCamera();
        mainApplication.getInputManager().setCursorVisible(!enabled);
        flyByCamera.setEnabled(enabled);
        // TODO: Handle via TransformationHandler, defaultTransformationProvider and reset (For now, just resetting to the last transformation is good enough)
        if (enabled) {
            tmpPosition.set(mainApplication.getCamera().getLocation());
            tmpRotation.set(mainApplication.getCamera().getRotation());
        }
        else {
            moveTo(tmpPosition, tmpRotation, 0.25f);
        }
    }

    public boolean isFreeCameraEnabled() {
        return mainApplication.getFlyByCamera().isEnabled();
    }
}