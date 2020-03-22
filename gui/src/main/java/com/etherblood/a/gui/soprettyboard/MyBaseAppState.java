package com.etherblood.a.gui.soprettyboard;

import com.etherblood.a.gui.CardsApp;
import com.jme3.app.Application;
import com.jme3.app.state.AbstractAppState;
import com.jme3.app.state.AppState;
import com.jme3.app.state.AppStateManager;

/**
 *
 * @author Carl
 */
public class MyBaseAppState extends AbstractAppState{

    public MyBaseAppState(){

    }
    protected CardsApp mainApplication;

    @Override
    public void initialize(AppStateManager stateManager, Application application){
        super.initialize(stateManager, application);
        mainApplication = (CardsApp) application;
    }

    protected <T extends AppState> T getAppState(Class<T> appStateClass){
        return mainApplication.getStateManager().getState(appStateClass);
    }
}