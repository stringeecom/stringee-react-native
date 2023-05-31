package com.stringeereactnative;

import android.content.Context;
import android.view.Choreographer;
import android.view.Gravity;
import android.view.View;
import android.view.ViewGroup;
import android.widget.FrameLayout;

import androidx.annotation.NonNull;

import com.stringee.call.StringeeCall;
import com.stringee.call.StringeeCall2;

import org.webrtc.RendererCommon.ScalingType;
import org.webrtc.SurfaceViewRenderer;

public class RNStringeeVideoView extends FrameLayout {
    private int width = 0;
    private int height = 0;
    private String callId;
    private boolean isLocal = false;
    private boolean isOverlay = true;
    private ScalingType scalingType = ScalingType.SCALE_ASPECT_FILL;

    public RNStringeeVideoView(@NonNull Context context) {
        super(context);
    }

    public void setWidth(int width) {
        this.width = Utils.dpiToPx(getContext(), width);
    }

    public void setHeight(int height) {
        this.height = Utils.dpiToPx(getContext(), height);
    }

    public void setCallId(String callId) {
        this.callId = callId;
    }

    public void setLocal(boolean local) {
        isLocal = local;
    }

    public void setOverlay(boolean overlay) {
        isOverlay = overlay;
    }

    public void setScalingType(String scalingType) {
        switch (scalingType) {
            case "fit":
                this.scalingType = ScalingType.SCALE_ASPECT_FIT;
                break;
            case "fill":
            default:
                this.scalingType = ScalingType.SCALE_ASPECT_FILL;
                break;
        }
    }

    public void createView() {
        setupLayout(this);
        this.removeAllViews();
        LayoutParams layoutParams = new LayoutParams(LayoutParams.WRAP_CONTENT, LayoutParams.WRAP_CONTENT);
        layoutParams.gravity = Gravity.CENTER;
        FrameLayout layout = new FrameLayout(getContext());
        if (callId != null) {
            StringeeCall stringeeCall = StringeeManager.getInstance().getCallsMap().get(callId);
            StringeeCall2 stringeeCall2 = StringeeManager.getInstance().getCalls2Map().get(callId);

            if (stringeeCall == null && stringeeCall2 == null) {
                return;
            }

            if (isLocal) {
                SurfaceViewRenderer localView;
                if (stringeeCall != null) {
                    localView = stringeeCall.getLocalView();
                    if (localView.getParent() != null) {
                        ((ViewGroup) localView.getParent()).removeView(localView);
                    }
                    layout.addView(localView, layoutParams);
                    stringeeCall.renderLocalView(isOverlay, scalingType);
                } else {
                    localView = stringeeCall2.getLocalView();
                    if (localView.getParent() != null) {
                        ((ViewGroup) localView.getParent()).removeView(localView);
                    }
                    layout.addView(localView, layoutParams);
                    stringeeCall2.renderLocalView(isOverlay, scalingType);
                }
            } else {
                SurfaceViewRenderer remoteView;
                if (stringeeCall != null) {
                    remoteView = stringeeCall.getRemoteView();
                    if (remoteView.getParent() != null) {
                        ((ViewGroup) remoteView.getParent()).removeView(remoteView);
                    }
                    layout.addView(remoteView, layoutParams);
                    stringeeCall.renderRemoteView(isOverlay, scalingType);
                } else {
                    remoteView = stringeeCall2.getRemoteView();
                    if (remoteView.getParent() != null) {
                        ((ViewGroup) remoteView.getParent()).removeView(remoteView);
                    }
                    layout.addView(remoteView, layoutParams);
                    stringeeCall2.renderRemoteView(isOverlay, scalingType);
                }
            }
        }
        this.addView(layout);
    }

    public void setupLayout(View view) {
        Choreographer.getInstance().postFrameCallback(new Choreographer.FrameCallback() {
            @Override
            public void doFrame(long frameTimeNanos) {
                manuallyLayoutChildren(view);
                view.getViewTreeObserver().dispatchOnGlobalLayout();
                Choreographer.getInstance().postFrameCallback(this);
            }
        });
    }

    /**
     * Layout all children properly
     */
    public void manuallyLayoutChildren(View view) {
        view.measure(
                MeasureSpec.makeMeasureSpec(width, MeasureSpec.EXACTLY),
                MeasureSpec.makeMeasureSpec(height, MeasureSpec.EXACTLY));

        view.layout(0, 0, width, height);
    }
}
