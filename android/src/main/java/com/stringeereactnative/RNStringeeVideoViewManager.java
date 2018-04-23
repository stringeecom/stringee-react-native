package com.stringeereactnative;

import android.util.Log;

import com.facebook.react.uimanager.ThemedReactContext;
import com.facebook.react.uimanager.ViewGroupManager;
import com.facebook.react.uimanager.annotations.ReactProp;

public class RNStringeeVideoViewManager extends ViewGroupManager<RNStringeeVideoLayout> {

    private boolean callIdSet;
    private boolean localSet;

    @Override
    public String getName() {

        return this.getClass().getSimpleName();
    }

    @Override
    protected RNStringeeVideoLayout createViewInstance(ThemedReactContext reactContext) {
        return new RNStringeeVideoLayout(reactContext);
    }

    @ReactProp(name = "callId")
    public void setCallId(RNStringeeVideoLayout layout, String callId) {
        callIdSet = true;
        layout.setCallId(callId);
        if (callId != null && callId.length() > 0 && localSet) {
            layout.updateView();
        }
    }

    @ReactProp(name = "local", defaultBoolean = false)
    public void setLocal(RNStringeeVideoLayout layout, boolean isLocal) {
        layout.setLocal(isLocal);
        if (callIdSet) {
            layout.updateView();
        }
    }
}
