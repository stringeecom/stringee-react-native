package com.stringeereactnative.view;

import android.os.Build.VERSION;
import android.os.Build.VERSION_CODES;
import android.view.Gravity;
import android.view.ViewGroup;
import android.widget.FrameLayout;

import com.facebook.react.uimanager.ThemedReactContext;
import com.stringee.video.StringeeVideo.ScalingType;
import com.stringee.video.StringeeVideoTrack.Listener;
import com.stringee.video.StringeeVideoTrack.MediaState;
import com.stringeereactnative.call.Call2Wrapper;
import com.stringeereactnative.call.CallWrapper;
import com.stringeereactnative.common.StringeeManager;
import com.stringeereactnative.conference.VideoTrackManager;

import org.webrtc.SurfaceViewRenderer;

import java.util.HashMap;
import java.util.Map;

public class RNStringeeVideoLayout extends FrameLayout {
    private FrameLayout mViewContainer;
    private String callId;
    private String trackId;
    private boolean isLocal = false;
    private boolean isOverlay = true;
    private boolean isMirror = false;
    private ScalingType scalingType = ScalingType.SCALE_ASPECT_FILL;

    public void setCallId(String callId) {
        this.callId = callId;
    }

    public void setTrackId(String trackId) {
        this.trackId = trackId;
    }

    public void setLocal(boolean isLocal) {
        this.isLocal = isLocal;
    }

    public void setOverlay(boolean isOverlay) {
        if (VERSION.SDK_INT <= VERSION_CODES.N_MR1) {
            this.isOverlay = true;
        } else {
            this.isOverlay = isOverlay;
        }
    }

    public void setMirror(boolean isMirror) {
        this.isMirror = isMirror;
    }

    public void setScalingType(String scalingType) {
        if (scalingType.equals("FILL")) {
            this.scalingType = ScalingType.SCALE_ASPECT_FILL;
        } else if (scalingType.equals("FIT")) {
            this.scalingType = ScalingType.SCALE_ASPECT_FIT;
        }
    }

    public RNStringeeVideoLayout(ThemedReactContext context) {
        super(context);
        mViewContainer = new FrameLayout(getContext());
        addView(mViewContainer, 0);
        requestLayout();
    }

    public void updateView() {
        mViewContainer.removeAllViews();
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
                    mViewContainer.addView(localView, layoutParams);
                    callWrapper.renderLocalView(isOverlay, scalingType);
                } else {
                    localView = call2Wrapper.getLocalView();
                    if (localView.getParent() != null) {
                        ((ViewGroup) localView.getParent()).removeView(localView);
                    }
                    mViewContainer.addView(localView, layoutParams);
                    call2Wrapper.renderLocalView(isOverlay, scalingType);
                }
                localView.setMirror(isMirror);

                //save localView option
                Map<String, Object> localViewOptions = new HashMap<>();
                localViewOptions.put("isMirror", isMirror);
                localViewOptions.put("isOverlay", isOverlay);
                localViewOptions.put("scalingType", scalingType);
                localViewOptions.put("layout", mViewContainer);
                StringeeManager.getInstance().getLocalViewOption().put(callId, localViewOptions);
            } else {
                SurfaceViewRenderer remoteView;
                if (callWrapper != null) {
                    remoteView = callWrapper.getRemoteView();
                    if (remoteView.getParent() != null) {
                        ((ViewGroup) remoteView.getParent()).removeView(remoteView);
                    }
                    mViewContainer.addView(remoteView, layoutParams);
                    callWrapper.renderRemoteView(isOverlay, scalingType);
                } else {
                    remoteView = call2Wrapper.getRemoteView();
                    if (remoteView.getParent() != null) {
                        ((ViewGroup) remoteView.getParent()).removeView(remoteView);
                    }
                    mViewContainer.addView(remoteView, layoutParams);
                    call2Wrapper.renderRemoteView(isOverlay, scalingType);
                }
                remoteView.setMirror(isMirror);
                //save remoteView option
                Map<String, Object> remoteViewOptions = new HashMap<>();
                remoteViewOptions.put("isMirror", isMirror);
                remoteViewOptions.put("isOverlay", isOverlay);
                remoteViewOptions.put("scalingType", scalingType);
                remoteViewOptions.put("layout", mViewContainer);
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
                    android.os.Handler handler = new android.os.Handler(android.os.Looper.getMainLooper());
                    handler.post(new Runnable() {
                        @Override
                        public void run() {
                            SurfaceViewRenderer trackView = videoTrackManager.getVideoTrack().getView(getContext());
                            if (trackView.getParent() != null) {
                                ((FrameLayout) trackView.getParent()).removeView(trackView);
                            }

                            mViewContainer.addView(trackView, layoutParams);
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

        invalidate();
    }
}
