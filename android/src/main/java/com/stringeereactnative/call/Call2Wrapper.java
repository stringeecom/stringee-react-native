package com.stringeereactnative.call;

import android.app.Activity;
import android.content.Intent;
import android.os.Build.VERSION;
import android.os.Build.VERSION_CODES;
import android.os.Handler;
import android.os.Looper;
import android.view.Gravity;
import android.widget.FrameLayout;
import android.widget.FrameLayout.LayoutParams;

import com.facebook.react.bridge.Arguments;
import com.facebook.react.bridge.BaseActivityEventListener;
import com.facebook.react.bridge.Callback;
import com.facebook.react.bridge.ReactApplicationContext;
import com.facebook.react.bridge.WritableArray;
import com.facebook.react.bridge.WritableMap;
import com.stringee.call.StringeeCall2;
import com.stringee.call.StringeeCall2.CallStatsListener;
import com.stringee.call.StringeeCall2.MediaState;
import com.stringee.call.StringeeCall2.SignalingState;
import com.stringee.call.StringeeCall2.StringeeCallListener;
import com.stringee.call.StringeeCall2.StringeeCallStats;
import com.stringee.common.StringeeAudioManager;
import com.stringee.common.StringeeAudioManager.AudioDevice;
import com.stringee.common.StringeeAudioManager.AudioManagerEvents;
import com.stringee.exception.StringeeError;
import com.stringee.listener.StatusListener;
import com.stringee.video.StringeeVideo.ScalingType;
import com.stringee.video.StringeeVideoTrack;
import com.stringeereactnative.common.StringeeManager;
import com.stringeereactnative.common.Utils;

import org.json.JSONException;
import org.json.JSONObject;
import org.webrtc.SurfaceViewRenderer;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Random;
import java.util.Set;

public class Call2Wrapper implements StringeeCallListener, AudioManagerEvents {
    private StringeeCall2 call;
    private StringeeManager stringeeManager;
    private Handler handler;
    private ReactApplicationContext context;
    private Callback makeCallCallBack;
    private boolean isResumeVideo = false;
    private boolean isIncomingCall;
    private boolean hasRemoteStream;
    private boolean remoteStreamShowed;
    private MediaState mediaState;

    public Call2Wrapper(ReactApplicationContext context, StringeeCall2 call) {
        this.context = context;
        this.call = call;
        this.handler = new Handler(Looper.getMainLooper());
        this.stringeeManager = StringeeManager.getInstance();
    }

    public Call2Wrapper(ReactApplicationContext context, StringeeCall2 call, Callback callback) {
        this.context = context;
        this.call = call;
        this.handler = new Handler(Looper.getMainLooper());
        this.stringeeManager = StringeeManager.getInstance();
        this.makeCallCallBack = callback;
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
                audioManager.start(Call2Wrapper.this);
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
                } catch (JSONException e) {
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
                StringeeAudioManager audioManager = StringeeManager.getInstance().getAudioManager();
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

    public void setMirror(boolean isLocal, boolean isMirror, Callback callback) {
        if (isLocal) {
            call.getLocalView().setMirror(isMirror);
        } else {
            call.getRemoteView().setMirror(isMirror);
        }
        callback.invoke(true, 0, "Success");
    }

    public void startCapture(Callback callback) {
        if (VERSION.SDK_INT >= VERSION_CODES.LOLLIPOP) {
            final int REQUEST_CODE = new Random().nextInt(65536);

            stringeeManager.getCaptureManager().getActivityResult(new BaseActivityEventListener() {
                @Override
                public void onActivityResult(Activity activity, int requestCode, int resultCode, Intent data) {
                    if (requestCode == REQUEST_CODE && resultCode == Activity.RESULT_OK) {
                        stringeeManager.getCaptureManager().getScreenCapture().createCapture(data);
                    }
                }
            });

            call.startCaptureScreen(stringeeManager.getCaptureManager().getScreenCapture(), REQUEST_CODE, new StatusListener() {
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

        } else {
            callback.invoke(false, -5, "This feature requires android api level >= 21");
        }
    }

    public void stopCapture(Callback callback) {
        call.stopCaptureScreen(new StatusListener() {
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
    public void onSignalingStateChange(StringeeCall2 stringeeCall2, SignalingState signalingState, String reason, int sipCode, String sipReason) {
        if (Utils.contains(stringeeManager.getCall2Events(), "onSignalingStateChange")) {
            if (signalingState == SignalingState.CALLING) {
                stringeeManager.getCall2WrapperMap().put(stringeeCall2.getCallId(), Call2Wrapper.this);
                if (makeCallCallBack != null) {
                    makeCallCallBack.invoke(true, 0, "Success", stringeeCall2.getCallId(), stringeeCall2.getCustomDataFromYourServer());
                }
            }
            if (isIncomingCall) {
                if (signalingState != SignalingState.ANSWERED) {
                    WritableMap params = Arguments.createMap();
                    params.putString("callId", stringeeCall2.getCallId());
                    params.putInt("code", signalingState.getValue());
                    params.putString("reason", reason);
                    params.putInt("sipCode", sipCode);
                    params.putString("sipReason", sipReason);
                    Utils.sendEvent(context, "onSignalingStateChange", params);
                }
            } else {
                WritableMap params = Arguments.createMap();
                params.putString("callId", stringeeCall2.getCallId());
                params.putInt("code", signalingState.getValue());
                params.putString("reason", reason);
                params.putInt("sipCode", sipCode);
                params.putString("sipReason", sipReason);
                Utils.sendEvent(context, "onSignalingStateChange", params);
            }
        }
    }

    @Override
    public void onError(StringeeCall2 stringeeCall2, int code, String desc) {
        if (makeCallCallBack != null) {
            makeCallCallBack.invoke(false, code, desc, stringeeCall2.getCallId(), stringeeCall2.getCustomDataFromYourServer());
        }
    }

    @Override
    public void onHandledOnAnotherDevice(StringeeCall2 stringeeCall2, SignalingState signalingState, String s) {
        if (Utils.contains(stringeeManager.getCall2Events(), "onHandledOnAnotherDevice")) {
            WritableMap params = Arguments.createMap();
            params.putString("callId", stringeeCall2.getCallId());
            params.putInt("code", signalingState.getValue());
            params.putString("description", s);
            Utils.sendEvent(context, "onHandledOnAnotherDevice", params);
        }
    }

    @Override
    public void onMediaStateChange(StringeeCall2 stringeeCall2, MediaState mediaState) {
        this.mediaState = mediaState;
        if (Utils.contains(stringeeManager.getCall2Events(), "onMediaStateChange")) {
            WritableMap params = Arguments.createMap();
            params.putString("callId", stringeeCall2.getCallId());
            String desc = "";
            if (mediaState == MediaState.CONNECTED) {
                desc = "Connected";
            } else if (mediaState == MediaState.DISCONNECTED) {
                desc = "Disconnected";
            }
            params.putInt("code", this.mediaState.getValue());
            params.putString("description", desc);
            Utils.sendEvent(context, "onMediaStateChange", params);
        }
        if (this.mediaState == MediaState.CONNECTED && hasRemoteStream && !remoteStreamShowed && stringeeCall2.isVideoCall()) {
            remoteStreamShowed = true;
            if (Utils.contains(stringeeManager.getCallEvents(), "onRemoteStream")) {
                WritableMap params = Arguments.createMap();
                params.putString("callId", stringeeCall2.getCallId());
                Utils.sendEvent(context, "onRemoteStream", params);
            }
        }
    }

    @Override
    public void onLocalStream(StringeeCall2 stringeeCall2) {
        if (stringeeCall2.isVideoCall()) {
            if (Utils.contains(stringeeManager.getCall2Events(), "onLocalStream")) {
                WritableMap params = Arguments.createMap();
                params.putString("callId", stringeeCall2.getCallId());
                if (isResumeVideo) {
                    Map<String, Object> localViewOptions = stringeeManager.getLocalViewOption().get(stringeeCall2.getCallId());
                    FrameLayout localView = (FrameLayout) localViewOptions.get("layout");
                    boolean isMirror = (Boolean) localViewOptions.get("isMirror");
                    boolean isOverlay = (Boolean) localViewOptions.get("isOverlay");
                    ScalingType scalingType = (ScalingType) localViewOptions.get("scalingType");

                    localView.removeAllViews();
                    if (stringeeCall2.getLocalView().getParent() != null) {
                        ((FrameLayout) stringeeCall2.getLocalView().getParent()).removeView(stringeeCall2.getLocalView());
                    }
                    LayoutParams layoutParams = new LayoutParams(LayoutParams.WRAP_CONTENT, LayoutParams.WRAP_CONTENT);
                    layoutParams.gravity = Gravity.CENTER;
                    localView.addView(stringeeCall2.getLocalView(), layoutParams);
                    stringeeCall2.renderLocalView(isOverlay, scalingType);
                    stringeeCall2.getLocalView().setMirror(isMirror);

                    isResumeVideo = false;
                }
                Utils.sendEvent(context, "onLocalStream", params);
            }
        }
    }

    @Override
    public void onRemoteStream(StringeeCall2 stringeeCall2) {
        if (stringeeCall2.isVideoCall()) {
            if (mediaState == MediaState.CONNECTED && !remoteStreamShowed) {
                remoteStreamShowed = true;
                if (Utils.contains(stringeeManager.getCall2Events(), "onRemoteStream")) {
                    WritableMap params = Arguments.createMap();
                    params.putString("callId", stringeeCall2.getCallId());
                    Utils.sendEvent(context, "onRemoteStream", params);
                }
            } else {
                hasRemoteStream = true;
            }

            Map<String, Object> remoteViewOptions = stringeeManager.getRemoteViewOption().get(stringeeCall2.getCallId());
            if (remoteViewOptions != null) {
                FrameLayout remoteView = (FrameLayout) remoteViewOptions.get("layout");
                boolean isMirror = (Boolean) remoteViewOptions.get("isMirror");
                boolean isOverlay = (Boolean) remoteViewOptions.get("isOverlay");
                ScalingType scalingType = (ScalingType) remoteViewOptions.get("scalingType");

                if (remoteView != null) {
                    remoteView.removeAllViews();
                    if (stringeeCall2.getRemoteView().getParent() != null) {
                        ((FrameLayout) stringeeCall2.getRemoteView().getParent()).removeView(stringeeCall2.getRemoteView());
                    }
                    LayoutParams layoutParams = new LayoutParams(LayoutParams.WRAP_CONTENT, LayoutParams.WRAP_CONTENT);
                    layoutParams.gravity = Gravity.CENTER;
                    remoteView.addView(stringeeCall2.getRemoteView(), layoutParams);
                    stringeeCall2.renderRemoteView(isOverlay, scalingType);
                    stringeeCall2.getRemoteView().setMirror(isMirror);
                }
            }
        }
    }

    @Override
    public void onVideoTrackAdded(StringeeVideoTrack stringeeVideoTrack) {
//        if (Utils.contains(stringeeManager.getCall2Events(), "onAddVideoTrack")) {
//            stringeeManager.getTrackMap().put(stringeeVideoTrack.getId(), stringeeVideoTrack);
//            WritableMap params = Arguments.createMap();
//            params.putMap("data", Utils.getVideoTrackMap(stringeeVideoTrack, instanceId));
//            Utils.sendEvent(context, "onAddVideoTrack", params);
//        }
    }

    @Override
    public void onVideoTrackRemoved(StringeeVideoTrack stringeeVideoTrack) {
//        if (Utils.contains(stringeeManager.getCall2Events(), "onRemoveVideoTrack")) {
//            WritableMap params = Arguments.createMap();
//            params.putMap("data", Utils.getVideoTrackMap(stringeeVideoTrack, instanceId));
//            Utils.sendEvent(context, "onRemoveVideoTrack", params);
//        }
    }

    @Override
    public void onCallInfo(StringeeCall2 stringeeCall2, JSONObject jsonObject) {
        if (Utils.contains(stringeeManager.getCall2Events(), "onCallInfo")) {
            WritableMap params = Arguments.createMap();
            params.putString("callId", stringeeCall2.getCallId());
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

        if (Utils.contains(stringeeManager.getCall2Events(), "onAudioDeviceChange")) {
            WritableMap params = Arguments.createMap();
            params.putString("selectedAudioDevice", audioDevice.name());
            params.putArray("availableAudioDevices", availableDevicesMap);
            Utils.sendEvent(context, "onAudioDeviceChange", params);
        }
    }
}
