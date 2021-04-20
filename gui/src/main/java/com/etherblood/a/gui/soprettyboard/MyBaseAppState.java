package com.etherblood.a.gui.soprettyboard;

import com.etherblood.a.gui.GameApplication;
import com.jme3.app.Application;
import com.jme3.app.state.AbstractAppState;
import com.jme3.app.state.AppState;
import com.jme3.app.state.AppStateManager;

public class MyBaseAppState extends AbstractAppState {

    public MyBaseAppState() {

    }

    protected GameApplication mainApplication;

    @Override
    public void initialize(AppStateManager stateManager, Application application) {
        super.initialize(stateManager, application);
        mainApplication = (GameApplication) application;
    }

    protected <T extends AppState> T getAppState(Class<T> appStateClass) {
        return mainApplication.getStateManager().getState(appStateClass);
    }
}