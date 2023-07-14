package com.etherblood.ethercards.gui;

import com.jme3.app.Application;
import com.jme3.app.state.AbstractAppState;
import com.jme3.app.state.AppStateManager;
import com.jme3.math.Vector3f;
import com.jme3.scene.Node;
import com.jme3.system.AppSettings;
import com.simsilica.lemur.Button;
import com.simsilica.lemur.Container;
import java.io.IOException;

public class MenuAppstate extends AbstractAppState {

    private final UserSettings settings = UserSettings.instance();
    private final Node guiNode;
    private Application application;
    private final Container container;
    private final Button continueButton;
    private final Button surrenderButton;
    private final Button fullscreenButton;
    private final Button exitButton;

    public MenuAppstate(Node guiNode) {
        this.guiNode = guiNode;

        continueButton = new Button("Continue");
        continueButton.addClickCommands(x -> application.getStateManager().detach(this));

        surrenderButton = new Button("Surrender");
        surrenderButton.addClickCommands(x -> {
            application.getStateManager().getState(GameAppstate.class).surrender();
            application.getStateManager().detach(this);
        });

        fullscreenButton = new Button("");
        fullscreenButton.addClickCommands(x -> {
            boolean fullscreen = !settings.isFullscreen();
            settings.setFullscreen(fullscreen);
            save();

            AppSettings appSettings = application.getContext().getSettings();
            appSettings.setFullscreen(fullscreen);
            appSettings.setWidth(fullscreen ? -1 : settings.getScreenWidth());
            appSettings.setHeight(fullscreen ? -1 : settings.getScreenHeight());
            appSettings.setResizable(true);
            application.restart();
            appSettings.setResizable(true);
        });

        exitButton = new Button("Exit");
        exitButton.addClickCommands(x -> application.stop());

        container = new Container();
    }

    @Override
    public void stateAttached(AppStateManager stateManager) {
        guiNode.attachChild(container);
        application = stateManager.getApplication();
    }

    @Override
    public void stateDetached(AppStateManager stateManager) {
        guiNode.detachChild(container);
    }

    @Override
    public void update(float tpf) {
        container.clearChildren();
        container.addChild(continueButton);
        if (application.getStateManager().getState(GameAppstate.class) != null) {
            container.addChild(surrenderButton);
        }
        container.addChild(fullscreenButton);
        container.addChild(exitButton);
        fullscreenButton.setText(settings.isFullscreen() ? "Fullscreen" : "Windowed");

        AppSettings appSettings = application.getContext().getSettings();
        Vector3f size = container.getSize();
        container.setLocalTranslation((appSettings.getWidth() - size.x) / 2, (appSettings.getHeight() + size.y) / 2, 10);
    }

    private void save() {
        try {
            settings.save();
        } catch (IOException ex) {
            throw new RuntimeException(ex);
        }
    }
}
