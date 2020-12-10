package com.stringeereactnative;

import android.view.View;
import android.view.ViewGroup;
import android.widget.FrameLayout;

import com.facebook.react.uimanager.ThemedReactContext;
import com.stringee.call.StringeeCall;
import com.stringee.call.StringeeCall2;
import com.stringee.video.StringeeStream;

public class RNStringeeVideoLayout extends FrameLayout {

    private StringeeCall stringeeCall;
    private StringeeCall2 stringeeCall2;
    private StringeeStream stringeeStream;
    private String callId;
    private boolean isLocal;
    private boolean setLocal;
    private String streamId;
    private FrameLayout mViewContainer;
    private boolean isOverlay;
    private boolean setOverlay;

    public void setCallId(String callId) {
        this.callId = callId;
    }

    public void setLocal(boolean local) {
        isLocal = local;
        setLocal = true;
    }

    public void setStreamId(String streamId) {
        this.streamId = streamId;
    }

    public void setOverlay(boolean isOverlay) {
        this.isOverlay = isOverlay;
        setOverlay = true;
    }

    public RNStringeeVideoLayout(ThemedReactContext context) {
        super(context);
        mViewContainer = new FrameLayout(getContext());
        addView(mViewContainer, 0);
        requestLayout();
    }

    public void updateView() {
        if (callId != null) {
            stringeeCall = StringeeManager.getInstance().getCallsMap().get(callId);
            if (stringeeCall != null && setLocal) {
                if (mViewContainer.getChildCount() > 0) {
                    mViewContainer.removeAllViews();
                }
                if (isLocal) {
                    View v = stringeeCall.getLocalView();
                    if (v.getParent() != null) {
                        ((ViewGroup) v.getParent()).removeView(v);
                    }
                    mViewContainer.addView(stringeeCall.getLocalView());
                    stringeeCall.renderLocalView(isOverlay);
                } else {
                    View v = stringeeCall.getRemoteView();
                    if (v.getParent() != null) {
                        ((ViewGroup) v.getParent()).removeView(v);
                    }
                    mViewContainer.addView(stringeeCall.getRemoteView());
                    stringeeCall.renderRemoteView(isOverlay);
                }
            } else {
                stringeeCall2 = StringeeManager.getInstance().getCalls2Map().get(callId);
                if (stringeeCall2 != null && setLocal) {
                    if (mViewContainer.getChildCount() > 0) {
                        mViewContainer.removeAllViews();
                    }
                    if (isLocal) {
                        View v = stringeeCall2.getLocalView();
                        if (v.getParent() != null) {
                            ((ViewGroup) v.getParent()).removeView(v);
                        }
                        mViewContainer.addView(stringeeCall2.getLocalView());
                        stringeeCall2.renderLocalView(isOverlay);
                    } else {
                        View v = stringeeCall2.getRemoteView();
                        if (v.getParent() != null) {
                            ((ViewGroup) v.getParent()).removeView(v);
                        }
                        mViewContainer.addView(stringeeCall2.getRemoteView());
                        stringeeCall2.renderRemoteView(isOverlay);
                    }
                }
            }
        } else if (streamId != null) {
            stringeeStream = StringeeManager.getInstance().getStreamsMap().get(streamId);
            if (stringeeStream != null && setOverlay) {
                if (mViewContainer.getChildCount() > 0) {
                    mViewContainer.removeAllViews();
                }
                View v = stringeeStream.getView();
                if (v.getParent() != null) {
                    ((ViewGroup) v.getParent()).removeView(v);
                }
                mViewContainer.addView(stringeeStream.getView());
                stringeeStream.renderView(isOverlay);
            }
        }

        invalidate();
    }
}
