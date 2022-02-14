package com.stringeereactnative.call;

import android.os.Handler;
import android.os.Looper;
import android.view.Gravity;
import android.widget.FrameLayout;
import android.widget.FrameLayout.LayoutParams;

import com.facebook.react.bridge.Arguments;
import com.facebook.react.bridge.Callback;
import com.facebook.react.bridge.ReactApplicationContext;
import com.facebook.react.bridge.WritableArray;
import com.facebook.react.bridge.WritableMap;
import com.stringee.call.StringeeCall;
import com.stringee.call.StringeeCall.CallStatsListener;
import com.stringee.call.StringeeCall.MediaState;
import com.stringee.call.StringeeCall.SignalingState;
import com.stringee.call.StringeeCall.StringeeCallListener;
import com.stringee.call.StringeeCall.StringeeCallStats;
import com.stringee.common.StringeeAudioManager;
import com.stringee.common.StringeeAudioManager.AudioDevice;
import com.stringee.common.StringeeAudioManager.AudioManagerEvents;
import com.stringee.exception.StringeeError;
import com.stringee.listener.StatusListener;
import com.stringee.video.StringeeVideo.ScalingType;
import com.stringeereactnative.common.StringeeManager;
import com.stringeereactnative.common.Utils;

import org.json.JSONObject;
import org.webrtc.SurfaceViewRenderer;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Set;

public class CallWrapper implements StringeeCallListener, AudioManagerEvents {
    private StringeeCall call;
    private StringeeManager stringeeManager;
    private Handler handler;
    private ReactApplicationContext context;
    private Callback makeCallCallBack;
    private boolean isResumeVideo = false;
    private boolean isIncomingCall;
    private boolean hasRemoteStream;
    private boolean remoteStreamShowed;
    private MediaState mediaState;

    public CallWrapper(ReactApplicationContext context, StringeeCall call) {
        this.context = context;
        this.call = call;
        this.handler = new Handler(Looper.getMainLooper());
        this.stringeeManager = StringeeManager.getInstance();
        this.isIncomingCall = true;
    }

    public CallWrapper(ReactApplicationContext context, StringeeCall call, Callback callback) {
        this.context = context;
        this.call = call;
        this.handler = new Handler(Looper.getMainLooper());
        this.stringeeManager = StringeeManager.getInstance();
        this.makeCallCallBack = callback;
        this.isIncomingCall = false;
    }

    public void prepareCall() {
        mediaState = null;
        hasRemoteStream = false;
        remoteStreamShowed = false;
        call.setCallListener(this);
        handler.post(new Runnable() {
            @Override
            public void run() {
                StringeeAudioManager audioManager = StringeeAudioManager.create(context);
                audioManager.start(CallWrapper.this);
                stringeeManager.setAudioManager(audioManager);
            }
        });
    }

    public void stopAudioManager() {
        handler.post(new Runnable() {
            @Override
            public void run() {
                StringeeAudioManager audioManager = stringeeManager.getAudioManager();
                if (audioManager != null) {
                    audioManager.stop();
                    audioManager = null;
                }
            }
        });
    }

    public void makeCall() {
        prepareCall();
        call.makeCall();
    }

    public void initAnswer(Callback callback) {
        prepareCall();
        call.ringing(new StatusListener() {
            @Override
            public void onSuccess() {
                callback.invoke(true, 0, "Success");
            }

            @Override
            public void onError(StringeeError stringeeError) {
                callback.invoke(false, stringeeError.getCode(), stringeeError.getMessage());
            }
        });
    }

    public void answer(Callback callback) {
        call.answer();
        callback.invoke(true, 0, "Success");
    }

    public void reject(Callback callback) {
        stopAudioManager();
        mediaState = null;
        hasRemoteStream = false;
        remoteStreamShowed = false;
        call.reject();
        callback.invoke(true, 0, "Success");
    }

    public void hangup(Callback callback) {
        stopAudioManager();
        mediaState = null;
        hasRemoteStream = false;
        remoteStreamShowed = false;
        call.hangup();
        callback.invoke(true, 0, "Success");
    }

    public void enableVideo(boolean enabled, Callback callback) {
        call.enableVideo(enabled);
        callback.invoke(true, 0, "Success");
    }

    public void mute(boolean isMute, Callback callback) {
        call.mute(isMute);
        callback.invoke(true, 0, "Success");
    }

    public void sendCallInfo(JSONObject info, Callback callback) {
        call.sendCallInfo(info, new StatusListener() {
            @Override
            public void onSuccess() {
                callback.invoke(true, 0, "Success");
            }

            @Override
            public void onError(StringeeError stringeeError) {
                super.onError(stringeeError);
                callback.invoke(false, stringeeError.getCode(), stringeeError.getMessage());
            }
        });
    }

    public void sendDTMF(String dtmf, Callback callback) {
        call.sendDTMF(dtmf, new StatusListener() {
            @Override
            public void onSuccess() {
                callback.invoke(true, 0, "Success");
            }

            @Override
            public void onError(StringeeError stringeeError) {
                super.onError(stringeeError);
                callback.invoke(false, stringeeError.getCode(), stringeeError.getMessage());
            }
        });
    }

    public void switchCamera(Callback callback) {
        call.switchCamera(new StatusListener() {
            @Override
            public void onSuccess() {
                callback.invoke(true, 0, "Success");
            }

            @Override
            public void onError(StringeeError stringeeError) {
                super.onError(stringeeError);
                callback.invoke(false, stringeeError.getCode(), stringeeError.getMessage());
            }
        });
    }

    public void switchCameraWithId(int cameraId, Callback callback) {
        call.switchCamera(new StatusListener() {
            @Override
            public void onSuccess() {
                callback.invoke(true, 0, "Success");
            }

            @Override
            public void onError(StringeeError stringeeError) {
                super.onError(stringeeError);
                callback.invoke(false, stringeeError.getCode(), stringeeError.getMessage());
            }
        }, cameraId);
    }

    public void getCallStats(Callback callback) {
        call.getStats(new CallStatsListener() {
            @Override
            public void onCallStats(StringeeCallStats stringeeCallStats) {
                JSONObject jsonObject = new JSONObject();
                try {
                    jsonObject.put("bytesReceived", stringeeCallStats.callBytesReceived);
                    jsonObject.put("packetsLost", stringeeCallStats.callPacketsLost);
                    jsonObject.put("packetsReceived", stringeeCallStats.callPacketsReceived);
                    jsonObject.put("timeStamp", stringeeCallStats.timeStamp);
                } catch (org.json.JSONException e) {
                    e.printStackTrace();
                }
                callback.invoke(true, 0, "Success", jsonObject.toString());
            }
        });
    }

    public void setSpeakerphoneOn(boolean on, Callback callback) {
        handler.post(new Runnable() {
            @Override
            public void run() {
                StringeeAudioManager audioManager = stringeeManager.getAudioManager();
                if (audioManager != null) {
                    audioManager.setSpeakerphoneOn(on);
                    callback.invoke(true, 0, "Success");
                }
            }
        });
    }

    public void resumeVideo(Callback callback) {
        isResumeVideo = true;
        call.resumeVideo();
        callback.invoke(true, 0, "Success");
    }

    public void setMirror(boolean isLocal, boolean isMirror, Callback callback) {
        if (isLocal) {
            call.getLocalView().setMirror(isMirror);
        } else {
            call.getRemoteView().setMirror(isMirror);
        }
        callback.invoke(true, 0, "Success");
    }

    public SurfaceViewRenderer getLocalView() {
        return call.getLocalView();
    }

    public SurfaceViewRenderer getRemoteView() {
        return call.getRemoteView();
    }

    public void renderLocalView(boolean isOverlay, ScalingType scalingType) {
        call.renderLocalView(isOverlay, scalingType);
    }

    public void renderRemoteView(boolean isOverlay, ScalingType scalingType) {
        call.renderRemoteView(isOverlay, scalingType);
    }

    @Override
    public void onSignalingStateChange(StringeeCall stringeeCall, SignalingState signalingState, String reason, int sipCode, String sipReason) {
        if (Utils.contains(stringeeManager.getCallEvents(), "onSignalingStateChange")) {
            if (signalingState == SignalingState.CALLING) {
                stringeeManager.getCallWrapperMap().put(stringeeCall.getCallId(), CallWrapper.this);
                if (makeCallCallBack != null) {
                    makeCallCallBack.invoke(true, 0, "Success", stringeeCall.getCallId(), stringeeCall.getCustomDataFromYourServer());
                }
            }

            if (isIncomingCall) {
                if (signalingState != SignalingState.ANSWERED) {
                    WritableMap params = Arguments.createMap();
                    params.putString("callId", stringeeCall.getCallId());
                    params.putInt("code", signalingState.getValue());
                    params.putString("reason", reason);
                    params.putInt("sipCode", sipCode);
                    params.putString("sipReason", sipReason);
                    Utils.sendEvent(context, "onSignalingStateChange", params);
                }
            } else {
                WritableMap params = Arguments.createMap();
                params.putString("callId", stringeeCall.getCallId());
                params.putInt("code", signalingState.getValue());
                params.putString("reason", reason);
                params.putInt("sipCode", sipCode);
                params.putString("sipReason", sipReason);
                Utils.sendEvent(context, "onSignalingStateChange", params);
            }
        }
    }

    @Override
    public void onError(StringeeCall stringeeCall, int code, String desc) {
        if (makeCallCallBack != null) {
            makeCallCallBack.invoke(false, code, desc, stringeeCall.getCallId(), stringeeCall.getCustomDataFromYourServer());
        }
    }

    @Override
    public void onHandledOnAnotherDevice(StringeeCall stringeeCall, SignalingState signalingState, String s) {
        if (Utils.contains(stringeeManager.getCallEvents(), "onHandledOnAnotherDevice")) {
            WritableMap params = Arguments.createMap();
            params.putString("callId", stringeeCall.getCallId());
            params.putInt("code", signalingState.getValue());
            params.putString("description", s);
            Utils.sendEvent(context, "onHandledOnAnotherDevice", params);
        }
    }

    @Override
    public void onMediaStateChange(StringeeCall stringeeCall, MediaState mediaState) {
        this.mediaState = mediaState;
        if (Utils.contains(stringeeManager.getCallEvents(), "onMediaStateChange")) {
            WritableMap params = Arguments.createMap();
            params.putString("callId", stringeeCall.getCallId());
            String desc = "";
            if (mediaState == MediaState.CONNECTED) {
                desc = "Connected";
            } else if (mediaState == MediaState.DISCONNECTED) {
                desc = "Disconnected";
            }
            params.putInt("code", mediaState.getValue());
            params.putString("description", desc);
            Utils.sendEvent(context, "onMediaStateChange", params);
        }
        if (this.mediaState == MediaState.CONNECTED && hasRemoteStream && !remoteStreamShowed && stringeeCall.isVideoCall()) {
            remoteStreamShowed = true;
            if (Utils.contains(stringeeManager.getCallEvents(), "onRemoteStream")) {
                WritableMap params = Arguments.createMap();
                params.putString("callId", stringeeCall.getCallId());
                Utils.sendEvent(context, "onRemoteStream", params);
            }
        }
    }

    @Override
    public void onLocalStream(StringeeCall stringeeCall) {
        if (stringeeCall.isVideoCall()) {
            if (Utils.contains(stringeeManager.getCallEvents(), "onLocalStream")) {
                WritableMap params = Arguments.createMap();
                params.putString("callId", stringeeCall.getCallId());
                if (isResumeVideo) {
                    Map<String, Object> localViewOptions = stringeeManager.getLocalViewOption().get(stringeeCall.getCallId());
                    FrameLayout localView = (FrameLayout) localViewOptions.get("layout");
                    boolean isMirror = (Boolean) localViewOptions.get("isMirror");
                    boolean isOverlay = (Boolean) localViewOptions.get("isOverlay");
                    ScalingType scalingType = (ScalingType) localViewOptions.get("scalingType");

                    localView.removeAllViews();
                    if (stringeeCall.getLocalView().getParent() != null) {
                        ((FrameLayout) stringeeCall.getLocalView().getParent()).removeView(stringeeCall.getLocalView());
                    }
                    LayoutParams layoutParams = new LayoutParams(LayoutParams.WRAP_CONTENT, LayoutParams.WRAP_CONTENT);
                    layoutParams.gravity = Gravity.CENTER;
                    localView.addView(stringeeCall.getLocalView(), layoutParams);
                    stringeeCall.renderLocalView(isOverlay, scalingType);
                    stringeeCall.getLocalView().setMirror(isMirror);

                    isResumeVideo = false;
                }
                Utils.sendEvent(context, "onLocalStream", params);
            }
        }
    }

    @Override
    public void onRemoteStream(StringeeCall stringeeCall) {
        if (stringeeCall.isVideoCall()) {
            if (mediaState == MediaState.CONNECTED && !remoteStreamShowed) {
                remoteStreamShowed = true;
                if (Utils.contains(stringeeManager.getCallEvents(), "onRemoteStream")) {
                    WritableMap params = Arguments.createMap();
                    params.putString("callId", stringeeCall.getCallId());
                    Utils.sendEvent(context, "onRemoteStream", params);
                }
            } else {
                hasRemoteStream = true;
            }

            Map<String, Object> remoteViewOptions = stringeeManager.getRemoteViewOption().get(stringeeCall.getCallId());
            if (remoteViewOptions != null) {
                FrameLayout remoteView = (FrameLayout) remoteViewOptions.get("layout");
                boolean isMirror = (Boolean) remoteViewOptions.get("isMirror");
                boolean isOverlay = (Boolean) remoteViewOptions.get("isOverlay");
                ScalingType scalingType = (ScalingType) remoteViewOptions.get("scalingType");

                if (remoteView != null) {
                    remoteView.removeAllViews();
                    if (stringeeCall.getRemoteView().getParent() != null) {
                        ((FrameLayout) stringeeCall.getRemoteView().getParent()).removeView(stringeeCall.getRemoteView());
                    }
                    LayoutParams layoutParams = new LayoutParams(LayoutParams.WRAP_CONTENT, LayoutParams.WRAP_CONTENT);
                    layoutParams.gravity = Gravity.CENTER;
                    remoteView.addView(stringeeCall.getRemoteView(), layoutParams);
                    stringeeCall.renderRemoteView(isOverlay, scalingType);
                    stringeeCall.getRemoteView().setMirror(isMirror);
                }
            }
        }
    }

    @Override
    public void onCallInfo(StringeeCall stringeeCall, JSONObject jsonObject) {
        if (Utils.contains(stringeeManager.getCallEvents(), "onCallInfo")) {
            WritableMap params = Arguments.createMap();
            params.putString("callId", stringeeCall.getCallId());
            params.putString("data", jsonObject.toString());
            Utils.sendEvent(context, "onCallInfo", params);
        }
    }

    @Override
    public void onAudioDeviceChanged(AudioDevice audioDevice, Set<AudioDevice> set) {
        List<AudioDevice> listAvailableDevices = new ArrayList<>(set);
        WritableArray availableDevicesMap = Arguments.createArray();
        for (int j = 0; j < listAvailableDevices.size(); j++) {
            AudioDevice device = listAvailableDevices.get(j);
            availableDevicesMap.pushString(device.name());
        }

        if (Utils.contains(stringeeManager.getCallEvents(), "onAudioDeviceChange")) {
            WritableMap params = Arguments.createMap();
            params.putString("selectedAudioDevice", audioDevice.name());
            params.putArray("availableAudioDevices", availableDevicesMap);
            Utils.sendEvent(context, "onAudioDeviceChange", params);
        }
    }
}
