package com.stringeereactnative;

import android.widget.FrameLayout;

import com.facebook.react.uimanager.ThemedReactContext;
import com.stringee.call.StringeeCall;

public class RNStringeeVideoLayout extends FrameLayout {

    private StringeeCall stringeeCall;
    private String callId;
    private boolean isLocal;

    public void setCallId(String callId) {
        this.callId = callId;
    }

    public void setLocal(boolean local) {
        isLocal = local;
    }

    public RNStringeeVideoLayout(ThemedReactContext context) {
        super(context);
    }

    public void updateView() {
        stringeeCall = StringeeManager.getInstance().getCallsMap().get(callId);
        if (stringeeCall != null) {
            if (isLocal) {
                addView(stringeeCall.getLocalView());
                stringeeCall.renderLocalView(true);
            } else {
                addView(stringeeCall.getRemoteView());
                stringeeCall.renderRemoteView(false);
            }
        }

        invalidate();
    }
}
