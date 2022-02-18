package com.stringeereactnative.view;

import android.content.Context;
import android.os.Handler;
import android.os.Looper;
import android.view.Choreographer;
import android.view.Gravity;
import android.view.View;
import android.view.ViewGroup;
import android.widget.FrameLayout;

import androidx.annotation.NonNull;

import com.stringee.video.StringeeVideo.ScalingType;
import com.stringee.video.StringeeVideoTrack.Listener;
import com.stringee.video.StringeeVideoTrack.MediaState;
import com.stringeereactnative.call.Call2Wrapper;
import com.stringeereactnative.call.CallWrapper;
import com.stringeereactnative.common.StringeeManager;
import com.stringeereactnative.common.Utils;
import com.stringeereactnative.conference.VideoTrackManager;

import org.webrtc.SurfaceViewRenderer;

import java.util.HashMap;
import java.util.Map;

public class RNStringeeVideoView extends FrameLayout {
    private int width = 0;
    private int height = 0;
    private String callId;
    private String trackId;
    private boolean isLocal = false;
    private boolean isOverlay = true;
    private boolean isMirror = false;
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

    public void setTrackId(String trackId) {
        this.trackId = trackId;
    }

    public void setLocal(boolean local) {
        isLocal = local;
    }

    public void setOverlay(boolean overlay) {
        isOverlay = overlay;
    }

    public void setMirror(boolean mirror) {
        isMirror = mirror;
    }

    public void setScalingType(String scalingType) {
        switch (scalingType) {
            case "FIT":
                this.scalingType = ScalingType.SCALE_ASPECT_FIT;
                break;
            case "FILL":
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

        if (callId != null) {
            CallWrapper callWrapper = StringeeManager.getInstance().getCallWrapperMap().get(callId);
            Call2Wrapper call2Wrapper = StringeeManager.getInstance().getCall2WrapperMap().get(callId);

            if (callWrapper == null && call2Wrapper == null) {
                return;
            }

            if (isLocal) {
                SurfaceViewRenderer localView;
                if (callWrapper != null) {
                    localView = callWrapper.getLocalView();
                    if (localView.getParent() != null) {
                        ((ViewGroup) localView.getParent()).removeView(localView);
                    }
                    this.addView(localView, layoutParams);
                    callWrapper.renderLocalView(isOverlay, scalingType);
                } else {
                    localView = call2Wrapper.getLocalView();
                    if (localView.getParent() != null) {
                        ((ViewGroup) localView.getParent()).removeView(localView);
                    }
                    this.addView(localView, layoutParams);
                    call2Wrapper.renderLocalView(isOverlay, scalingType);
                }
                localView.setMirror(isMirror);

                //save localView option
                Map<String, Object> localViewOptions = new HashMap<>();
                localViewOptions.put("isMirror", isMirror);
                localViewOptions.put("isOverlay", isOverlay);
                localViewOptions.put("scalingType", scalingType);
                localViewOptions.put("layout", this);
                StringeeManager.getInstance().getLocalViewOption().put(callId, localViewOptions);
            } else {
                SurfaceViewRenderer remoteView;
                if (callWrapper != null) {
                    remoteView = callWrapper.getRemoteView();
                    if (remoteView.getParent() != null) {
                        ((ViewGroup) remoteView.getParent()).removeView(remoteView);
                    }
                    this.addView(remoteView, layoutParams);
                    callWrapper.renderRemoteView(isOverlay, scalingType);
                } else {
                    remoteView = call2Wrapper.getRemoteView();
                    if (remoteView.getParent() != null) {
                        ((ViewGroup) remoteView.getParent()).removeView(remoteView);
                    }
                    this.addView(remoteView, layoutParams);
                    call2Wrapper.renderRemoteView(isOverlay, scalingType);
                }
                remoteView.setMirror(isMirror);
                //save remoteView option
                Map<String, Object> remoteViewOptions = new HashMap<>();
                remoteViewOptions.put("isMirror", isMirror);
                remoteViewOptions.put("isOverlay", isOverlay);
                remoteViewOptions.put("scalingType", scalingType);
                remoteViewOptions.put("layout", this);
                StringeeManager.getInstance().getRemoteViewOption().put(callId, remoteViewOptions);
            }
        } else if (trackId != null) {
            VideoTrackManager videoTrackManager = StringeeManager.getInstance().getTrackMap().get(trackId);

            if (videoTrackManager == null) {
                return;
            }

            videoTrackManager.setListener(new Listener() {
                @Override
                public void onMediaAvailable() {
                    Handler handler = new Handler(Looper.getMainLooper());
                    handler.post(new Runnable() {
                        @Override
                        public void run() {
                            SurfaceViewRenderer trackView = videoTrackManager.getVideoTrack().getView(getContext());
                            if (trackView.getParent() != null) {
                                ((FrameLayout) trackView.getParent()).removeView(trackView);
                            }

                            getThis().addView(trackView, layoutParams);
                            videoTrackManager.getVideoTrack().renderView(isOverlay, ScalingType.SCALE_ASPECT_FIT);
                            trackView.setMirror(isMirror);
                        }
                    });

                }

                @Override
                public void onMediaStateChange(MediaState mediaState) {

                }
            });
        }
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
                View.MeasureSpec.makeMeasureSpec(width, View.MeasureSpec.EXACTLY),
                View.MeasureSpec.makeMeasureSpec(height, View.MeasureSpec.EXACTLY));

        view.layout(0, 0, width, height);
    }

    private RNStringeeVideoView getThis() {
        return this;
    }
}
