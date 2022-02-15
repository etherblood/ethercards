package com.etherblood.ethercards.gui;

import com.jme3.app.state.AbstractAppState;
import com.jme3.app.state.AppStateManager;
import com.jme3.collision.Collidable;
import com.jme3.collision.CollisionResult;
import com.jme3.collision.CollisionResults;
import com.jme3.input.InputManager;
import com.jme3.input.MouseInput;
import com.jme3.input.controls.ActionListener;
import com.jme3.input.controls.MouseButtonTrigger;
import com.jme3.math.ColorRGBA;
import com.jme3.math.Ray;
import com.jme3.math.Vector2f;
import com.jme3.math.Vector3f;
import com.jme3.renderer.Camera;
import com.jme3.scene.Geometry;
import java.util.HashMap;
import java.util.Map;

public class ButtonAppstate extends AbstractAppState {

    private static final String BUTTON_MOUSE_CLICK_MAPPING = "buttonMouseClick";

    private final ActionListener actionListener = this::onMouseButton;
    private InputManager inputManager;
    private Camera camera;

    private final Map<Geometry, Runnable> buttonHandlers = new HashMap<>();
    private final Map<Geometry, ColorRGBA[]> buttonColors = new HashMap<>();
    private Geometry hovered = null;
    private Geometry pressed = null;

    public boolean isHovered(Geometry button) {
        return hovered == button;
    }

    public boolean isPressed(Geometry button) {
        return pressed == button;
    }

    public void registerButton(Geometry button, Runnable handler) {
        buttonHandlers.put(button, handler);
    }

    public void registerButton(Geometry button, Runnable handler, ColorRGBA... stateColors) {
        buttonHandlers.put(button, handler);
        buttonColors.put(button, stateColors);
        updateColor(button);
    }

    public void unregisterButton(Geometry button) {
        buttonHandlers.remove(button);
        if (hovered == button) {
            setHovered(null);
        }
        if (pressed == button) {
            setPressed(null);
        }
        buttonColors.remove(button);
    }

    @Override
    public void update(float tpf) {
        Vector2f click2d = inputManager.getCursorPosition();
        Vector3f click3d = camera.getWorldCoordinates(click2d, 0f);
        Vector3f direction = camera.getWorldCoordinates(click2d, 1f).subtract(click3d).normalizeLocal();

        Ray ray = new Ray(click3d, direction);
        CollisionResults results = new CollisionResults();
        for (Collidable button : buttonHandlers.keySet()) {
            button.collideWith(ray, results);
        }
        CollisionResult collision = results.getClosestCollision();
        if (collision != null) {
            setHovered(collision.getGeometry());
        } else {
            setHovered(null);
        }
    }

    @Override
    public void stateAttached(AppStateManager stateManager) {
        camera = stateManager.getApplication().getCamera();
        inputManager = stateManager.getApplication().getInputManager();
        inputManager.addMapping(BUTTON_MOUSE_CLICK_MAPPING, new MouseButtonTrigger(MouseInput.BUTTON_LEFT));
        inputManager.addListener(actionListener, BUTTON_MOUSE_CLICK_MAPPING);
    }

    @Override
    public void stateDetached(AppStateManager stateManager) {
        inputManager.removeListener(actionListener);
        inputManager.deleteMapping(BUTTON_MOUSE_CLICK_MAPPING);
        inputManager = null;
        camera = null;

        buttonHandlers.clear();
        setHovered(null);
        setPressed(null);
    }

    private void onMouseButton(String name, boolean isPressed, float tpf) {
        if (isPressed) {
            setPressed(hovered);
        } else {
            if (pressed != null && pressed == hovered) {
                buttonHandlers.get(pressed).run();
            }
            setPressed(null);
        }
    }

    private void setPressed(Geometry button) {
        Geometry previous = pressed;
        pressed = button;
        updateColor(pressed);
        updateColor(previous);
    }

    private void setHovered(Geometry button) {
        Geometry previous = hovered;
        hovered = button;
        updateColor(hovered);
        updateColor(previous);
    }

    private void updateColor(Geometry button) {
        if (button == null) {
            return;
        }
        ColorRGBA[] colors = buttonColors.get(button);
        if (colors == null) {
            return;
        }
        String colorKey = "Color";
        if (isPressed(button)) {
            button.getMaterial().setColor(colorKey, colors[0]);
        } else if (isHovered(button)) {
            button.getMaterial().setColor(colorKey, colors[1]);
        } else {
            button.getMaterial().setColor(colorKey, colors[2]);
        }
    }

}
