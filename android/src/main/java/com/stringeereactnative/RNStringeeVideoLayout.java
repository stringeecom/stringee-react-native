package com.stringeereactnative;

import android.widget.FrameLayout;

import com.facebook.react.uimanager.ThemedReactContext;
import com.stringee.call.StringeeCall;

public class RNStringeeVideoLayout extends FrameLayout {

    private StringeeCall stringeeCall;
    private String callId;
    private boolean isLocal;
    private FrameLayout mViewContainer;

    public void setCallId(String callId) {
        this.callId = callId;
    }

    public void setLocal(boolean local) {
        isLocal = local;
    }

    public RNStringeeVideoLayout(ThemedReactContext context) {
        super(context);
        mViewContainer = new FrameLayout(getContext());
        addView(mViewContainer, 0);
        requestLayout();
    }

    public void updateView() {
        stringeeCall = StringeeManager.getInstance().getCallsMap().get(callId);
        if (stringeeCall != null) {
            if (mViewContainer.getChildCount() > 0) {
                mViewContainer.removeAllViews();
            }
            if (isLocal) {
                mViewContainer.addView(stringeeCall.getLocalView());
                stringeeCall.renderLocalView(true);
            } else {
                mViewContainer.addView(stringeeCall.getRemoteView());
                stringeeCall.renderRemoteView(false);
            }
        }

        invalidate();
    }
}
