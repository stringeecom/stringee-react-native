package com.stringeereactnative;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import com.facebook.react.bridge.Arguments;
import com.facebook.react.bridge.Callback;
import com.facebook.react.bridge.ReactApplicationContext;
import com.facebook.react.bridge.ReactContext;
import com.facebook.react.bridge.ReactContextBaseJavaModule;
import com.facebook.react.bridge.ReactMethod;
import com.facebook.react.bridge.WritableArray;
import com.facebook.react.bridge.WritableMap;
import com.facebook.react.modules.core.DeviceEventManagerModule.RCTDeviceEventEmitter;
import com.stringee.StringeeClient;
import com.stringee.call.StringeeCall2;
import com.stringee.call.StringeeCall2.CallStatsListener;
import com.stringee.call.StringeeCall2.MediaState;
import com.stringee.call.StringeeCall2.SignalingState;
import com.stringee.call.StringeeCall2.StringeeCallListener;
import com.stringee.call.StringeeCall2.StringeeCallStats;
import com.stringee.call.StringeeCall2.VideoQuality;
import com.stringee.common.StringeeAudioManager.AudioDevice;
import com.stringee.common.StringeeAudioManager.AudioManagerEvents;
import com.stringee.exception.StringeeError;
import com.stringee.listener.StatusListener;
import com.stringee.video.StringeeVideoTrack;
import com.stringee.video.StringeeVideoTrack.MediaType;

import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.List;
import java.util.Set;

public class RNStringeeCall2Module extends ReactContextBaseJavaModule implements StringeeCallListener, AudioManagerEvents {

    private Callback mCallback;
    private ArrayList<String> jsEvents = new ArrayList<String>();

    public RNStringeeCall2Module(ReactApplicationContext reactContext) {
        super(reactContext);
    }

    @NonNull
    @Override
    public String getName() {
        return "RNStringeeCall2";
    }

    @ReactMethod
    public void makeCall(String instanceId, String params, Callback callback) {
        mCallback = callback;
        StringeeClient mClient = StringeeManager.getInstance().getClientsMap().get(instanceId);
        if (mClient == null) {
            callback.invoke(false, -1, "StringeeClient is not initialized or connected.", "");
            return;
        }

        if (!mClient.isConnected()) {
            callback.invoke(false, -1, "StringeeClient is not initialized or connected.", "");
            return;
        }
        try {
            JSONObject jsonObject = new JSONObject(params);
            String from = jsonObject.getString("from");
            String to = jsonObject.getString("to");
            boolean isVideoCall = jsonObject.getBoolean("isVideoCall");
            String customData = jsonObject.optString("customData");
            String resolution = jsonObject.optString("videoResolution");

            final StringeeCall2 mStringeeCall = new StringeeCall2(mClient, from, to);
            mStringeeCall.setVideoCall(isVideoCall);
            if (!Utils.isStringEmpty(customData)) {
                mStringeeCall.setCustom(customData);
            }
            if (!Utils.isStringEmpty(resolution)) {
                if (resolution.equalsIgnoreCase("NORMAL")) {
                    mStringeeCall.setQuality(VideoQuality.QUALITY_480P);
                } else if (resolution.equalsIgnoreCase("HD")) {
                    mStringeeCall.setQuality(VideoQuality.QUALITY_720P);
                }
            }

            mStringeeCall.setCallListener(this);

            Utils.startAudioManager(getReactApplicationContext(), this);

            mStringeeCall.makeCall(new StatusListener() {
                @Override
                public void onSuccess() {

                }
            });
        } catch (JSONException e) {
            callback.invoke(false, -4, "The parameters format is invalid.", "");
        }
    }

    @ReactMethod
    public void initAnswer(String instanceId, String callId, Callback callback) {
        StringeeClient mClient = StringeeManager.getInstance().getClientsMap().get(instanceId);
        if (mClient == null || !mClient.isConnected()) {
            callback.invoke(false, -1, "StringeeClient is not initialized or connected.");
            return;
        }

        if (callId == null || callId.length() == 0) {
            callback.invoke(false, -2, "The call id is invalid.");
            return;
        }

        StringeeCall2 call = StringeeManager.getInstance().getCalls2Map().get(callId);
        if (call == null) {
            callback.invoke(false, -3, "The call is not found.");
            return;
        }

        call.setCallListener(this);
        call.ringing(new StatusListener() {
            @Override
            public void onSuccess() {

            }
        });

        callback.invoke(true, 0, "Success");
    }

    @ReactMethod
    public void answer(String callId, Callback callback) {
        if (callId == null || callId.length() == 0) {
            callback.invoke(false, -2, "The call id is invalid.");
            return;
        }

        StringeeCall2 call = StringeeManager.getInstance().getCalls2Map().get(callId);
        if (call == null) {
            callback.invoke(false, -3, "The call is not found.");
            return;
        }

        call.answer(new StatusListener() {
            @Override
            public void onSuccess() {

            }
        });

        Utils.startAudioManager(getReactApplicationContext(), this);

        callback.invoke(true, 0, "Success");
    }

    @ReactMethod
    public void reject(String callId, Callback callback) {
        if (callId == null || callId.length() == 0) {
            callback.invoke(false, -2, "The call id is invalid.");
            return;
        }

        StringeeCall2 call = StringeeManager.getInstance().getCalls2Map().get(callId);
        if (call == null) {
            callback.invoke(false, -3, "The call is not found.");
            return;
        }

        call.reject(new StatusListener() {
            @Override
            public void onSuccess() {

            }
        });

        Utils.stopAudioManager();

        callback.invoke(true, 0, "Success");
    }

    @ReactMethod
    public void hangup(String callId, Callback callback) {
        if (callId == null || callId.length() == 0) {
            callback.invoke(false, -2, "The call id is invalid.");
            return;
        }

        StringeeCall2 call = StringeeManager.getInstance().getCalls2Map().get(callId);
        if (call == null) {
            callback.invoke(false, -3, "The call is not found.");
            return;
        }

        call.hangup(new StatusListener() {
            @Override
            public void onSuccess() {

            }
        });

        Utils.stopAudioManager();

        callback.invoke(true, 0, "Success");
    }

    @ReactMethod
    public void enableVideo(String callId, boolean enabled, Callback callback) {
        if (callId == null || callId.length() == 0) {
            callback.invoke(false, -2, "The call id is invalid.");
            return;
        }

        StringeeCall2 call = StringeeManager.getInstance().getCalls2Map().get(callId);
        if (call == null) {
            callback.invoke(false, -3, "The call is not found.");
            return;
        }
        call.enableVideo(enabled);
        callback.invoke(true, 0, "Success");
    }

    @ReactMethod
    public void mute(String callId, boolean isMute, Callback callback) {
        if (callId == null || callId.length() == 0) {
            callback.invoke(false, -2, "The call id is invalid.");
            return;
        }

        StringeeCall2 call = StringeeManager.getInstance().getCalls2Map().get(callId);
        if (call == null) {
            callback.invoke(false, -3, "The call is not found.");
            return;
        }

        call.mute(isMute);
        callback.invoke(true, 0, "Success");
    }

    @ReactMethod
    public void switchCamera(String callId, Callback callback) {
        if (callId == null || callId.length() == 0) {
            callback.invoke(false, -2, "The call id is invalid.");
            return;
        }

        StringeeCall2 call = StringeeManager.getInstance().getCalls2Map().get(callId);
        if (call == null) {
            callback.invoke(false, -3, "The call is not found.");
            return;
        }
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

    @ReactMethod
    public void getCallStats(String instanceId, String callId, final Callback callback) {
        StringeeClient mClient = StringeeManager.getInstance().getClientsMap().get(instanceId);
        if (mClient == null || !mClient.isConnected()) {
            callback.invoke(false, -1, "StringeeClient is not initialized or connected.", "");
            return;
        }

        if (callId == null || callId.length() == 0) {
            callback.invoke(false, -2, "The call id is invalid.", "");
            return;
        }

        StringeeCall2 call = StringeeManager.getInstance().getCalls2Map().get(callId);
        if (call == null) {
            callback.invoke(false, -3, "The call is not found.", "");
            return;
        }

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

    @ReactMethod
    public void setSpeakerphoneOn(String callId, boolean on, Callback callback) {
        if (callId == null || callId.length() == 0) {
            callback.invoke(false, -2, "The call id is invalid.");
            return;
        }

        Utils.setSpeakerPhone(on);

        callback.invoke(true, 0, "Success");
    }

    @ReactMethod
    public void resumeVideo(String callId, Callback callback) {
        if (callId == null || callId.length() == 0) {
            callback.invoke(false, -2, "The call id is invalid.");
            return;
        }

        StringeeCall2 call = StringeeManager.getInstance().getCalls2Map().get(callId);
        if (call == null) {
            callback.invoke(false, -3, "The call is not found.");
            return;
        }

        call.resumeVideo();
        callback.invoke(true, 0, "Success");
    }

    @ReactMethod
    public void sendCallInfo(String callId, String info, Callback callback) {
        if (callId == null || callId.length() == 0) {
            callback.invoke(false, -2, "The call id is invalid.");
            return;
        }

        StringeeCall2 call = StringeeManager.getInstance().getCalls2Map().get(callId);
        if (call == null) {
            callback.invoke(false, -3, "The call is not found.");
            return;
        }
        try {
            JSONObject jsonObject = new JSONObject(info);
            call.sendCallInfo(jsonObject, new StatusListener() {
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
        } catch (JSONException e) {
            callback.invoke(false, -4, "The call info format is invalid.");
        }
    }

    @ReactMethod
    public void sendDTMF(String callId, String dtmf, Callback callback) {
        if (callId == null || callId.length() == 0) {
            callback.invoke(false, -2, "The call id is invalid.");
            return;
        }

        StringeeCall2 call = StringeeManager.getInstance().getCalls2Map().get(callId);
        if (call == null) {
            callback.invoke(false, -3, "The call is not found.");
            return;
        }

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

    @ReactMethod
    public void setAutoSendTrackMediaStateChangeEvent(String callId, boolean autoSendTrackMediaStateChangeEvent, Callback callback) {
        if (callId == null || callId.length() == 0) {
            callback.invoke(false, -2, "The call id is invalid.");
            return;
        }

        StringeeCall2 call = StringeeManager.getInstance().getCalls2Map().get(callId);
        if (call == null) {
            callback.invoke(false, -3, "The call is not found.");
            return;
        }

        call.setAutoSendTrackMediaStateChangeEvent(autoSendTrackMediaStateChangeEvent);
        callback.invoke(true, 0, "Success");
    }

    @Override
    public void onSignalingStateChange(StringeeCall2 stringeeCall, SignalingState signalingState, String reason, int sipCode, String sipReason) {
        if (contains(jsEvents, "onSignalingStateChange")) {
            if (signalingState == SignalingState.CALLING) {
                StringeeManager.getInstance().getCalls2Map().put(stringeeCall.getCallId(), stringeeCall);
                mCallback.invoke(true, 0, "Success", stringeeCall.getCallId(), stringeeCall.getCustomDataFromYourServer());
            }

            WritableMap params = Arguments.createMap();
            params.putString("callId", stringeeCall.getCallId());
            params.putInt("code", signalingState.getValue());
            params.putString("reason", reason);
            params.putInt("sipCode", sipCode);
            params.putString("sipReason", sipReason);
            sendEvent(getReactApplicationContext(), "onSignalingStateChange", params);
        }
    }

    @Override
    public void onError(StringeeCall2 stringeeCall, int code, String desc) {
        mCallback.invoke(false, code, desc, stringeeCall.getCallId(), stringeeCall.getCustomDataFromYourServer());
    }

    @Override
    public void onHandledOnAnotherDevice(StringeeCall2 stringeeCall, SignalingState signalingState, String s) {
        if (contains(jsEvents, "onHandledOnAnotherDevice")) {
            WritableMap params = Arguments.createMap();
            params.putString("callId", stringeeCall.getCallId());
            params.putInt("code", signalingState.getValue());
            params.putString("description", s);
            sendEvent(getReactApplicationContext(), "onHandledOnAnotherDevice", params);
        }
    }

    @Override
    public void onMediaStateChange(StringeeCall2 stringeeCall, MediaState mediaState) {
        if (contains(jsEvents, "onMediaStateChange")) {
            WritableMap params = Arguments.createMap();
            params.putString("callId", stringeeCall.getCallId());
            int code = -1;
            String desc = "";
            if (mediaState == MediaState.CONNECTED) {
                code = 0;
                desc = "Connected";
            } else if (mediaState == MediaState.DISCONNECTED) {
                code = 1;
                desc = "Disconnected";
            }
            params.putInt("code", code);
            params.putString("description", desc);
            sendEvent(getReactApplicationContext(), "onMediaStateChange", params);
        }
    }

    @Override
    public void onLocalStream(StringeeCall2 stringeeCall) {
        if (contains(jsEvents, "onLocalStream")) {
            WritableMap params = Arguments.createMap();
            params.putString("callId", stringeeCall.getCallId());
            sendEvent(getReactApplicationContext(), "onLocalStream", params);
        }
    }

    @Override
    public void onRemoteStream(StringeeCall2 stringeeCall) {
        if (contains(jsEvents, "onRemoteStream")) {
            WritableMap params = Arguments.createMap();
            params.putString("callId", stringeeCall.getCallId());
            sendEvent(getReactApplicationContext(), "onRemoteStream", params);
        }
    }

    @Override
    public void onVideoTrackAdded(StringeeVideoTrack stringeeVideoTrack) {

    }

    @Override
    public void onVideoTrackRemoved(StringeeVideoTrack stringeeVideoTrack) {

    }

    @Override
    public void onCallInfo(StringeeCall2 stringeeCall, JSONObject jsonObject) {
        if (contains(jsEvents, "onCallInfo")) {
            WritableMap params = Arguments.createMap();
            params.putString("callId", stringeeCall.getCallId());
            params.putString("data", jsonObject.toString());
            sendEvent(getReactApplicationContext(), "onCallInfo", params);
        }
    }

    @Override
    public void onTrackMediaStateChange(String from, MediaType mediaType, boolean enable) {
        if (contains(jsEvents, "onTrackMediaStateChange")) {
            WritableMap params = Arguments.createMap();
            params.putString("from", from);
            params.putInt("mediaType", mediaType.getValue());
            params.putBoolean("enable", enable);
            sendEvent(getReactApplicationContext(), "onTrackMediaStateChange", params);
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

        if (contains(jsEvents, "onAudioDeviceChange")) {
            WritableMap params = Arguments.createMap();
            params.putString("selectedAudioDevice", audioDevice.name());
            params.putArray("availableAudioDevices", availableDevicesMap);
            sendEvent(getReactApplicationContext(), "onAudioDeviceChange", params);
        }
    }

    private void sendEvent(ReactContext reactContext, String eventName, @Nullable WritableMap eventData) {
        reactContext
                .getJSModule(RCTDeviceEventEmitter.class)
                .emit(eventName, eventData);
    }

    @ReactMethod
    public void setNativeEvent(String event) {
        jsEvents.add(event);
    }

    @ReactMethod
    public void removeNativeEvent(String event) {
        jsEvents.remove(event);
    }

    @ReactMethod
    public void addListener(String eventName) {
        // Keep: Required for RN built in Event Emitter Calls.
    }

    @ReactMethod
    public void removeListeners(Integer count) {
        // Keep: Required for RN built in Event Emitter Calls.
    }

    private boolean contains(ArrayList array, String value) {

        for (int i = 0; i < array.size(); i++) {
            if (array.get(i).equals(value)) {
                return true;
            }
        }
        return false;
    }
}
