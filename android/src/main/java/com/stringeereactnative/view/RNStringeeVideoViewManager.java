package com.stringeereactnative.view;

import androidx.annotation.NonNull;

import com.facebook.react.uimanager.ThemedReactContext;
import com.facebook.react.uimanager.ViewGroupManager;
import com.facebook.react.uimanager.annotations.ReactProp;

public class RNStringeeVideoViewManager extends ViewGroupManager<RNStringeeVideoLayout> {

    @NonNull
    @Override
    public String getName() {
        return this.getClass().getSimpleName();
    }

    @NonNull
    @Override
    protected RNStringeeVideoLayout createViewInstance(@NonNull ThemedReactContext reactContext) {
        return new RNStringeeVideoLayout(reactContext);
    }

    @ReactProp(name = "callId")
    public void setCallId(RNStringeeVideoLayout layout, String callId) {
        layout.setCallId(callId);
        layout.updateView();
    }

    @ReactProp(name = "trackId")
    public void setTrackId(RNStringeeVideoLayout layout, String trackId) {
        layout.setTrackId(trackId);
        layout.updateView();
    }

    @ReactProp(name = "local", defaultBoolean = false)
    public void setLocal(RNStringeeVideoLayout layout, boolean isLocal) {
        layout.setLocal(isLocal);
        layout.updateView();
    }

    @ReactProp(name = "overlay", defaultBoolean = false)
    public void setOverlay(RNStringeeVideoLayout layout, boolean isOverlay) {
        layout.setOverlay(isOverlay);
        layout.updateView();
    }

    @ReactProp(name = "isMirror", defaultBoolean = false)
    public void setMirror(RNStringeeVideoLayout layout, boolean isMirror) {
        layout.setMirror(isMirror);
        layout.updateView();
    }

    @ReactProp(name = "scalingType")
    public void setScalingType(RNStringeeVideoLayout layout, String scalingType) {
        layout.setScalingType(scalingType);
        layout.updateView();
    }
}
