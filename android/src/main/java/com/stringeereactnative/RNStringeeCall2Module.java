package com.stringeereactnative;

import android.os.Handler;
import android.os.Looper;

import androidx.annotation.Nullable;

import com.facebook.react.bridge.Arguments;
import com.facebook.react.bridge.Callback;
import com.facebook.react.bridge.ReactApplicationContext;
import com.facebook.react.bridge.ReactContext;
import com.facebook.react.bridge.ReactContextBaseJavaModule;
import com.facebook.react.bridge.ReactMethod;
import com.facebook.react.bridge.WritableArray;
import com.facebook.react.bridge.WritableMap;
import com.facebook.react.modules.core.DeviceEventManagerModule;
import com.stringee.StringeeClient;
import com.stringee.call.StringeeCall2;
import com.stringee.common.StringeeAudioManager;
import com.stringee.common.StringeeAudioManager.AudioDevice;
import com.stringee.common.StringeeAudioManager.AudioManagerEvents;
import com.stringee.listener.StatusListener;
import com.stringee.video.StringeeVideoTrack;

import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.List;
import java.util.Set;

public class RNStringeeCall2Module extends ReactContextBaseJavaModule implements StringeeCall2.StringeeCallListener, AudioManagerEvents {

    private Callback mCallback;
    private ArrayList<String> jsEvents = new ArrayList<String>();
    private Handler handler;

    public RNStringeeCall2Module(ReactApplicationContext reactContext) {
        super(reactContext);
    }

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

            final StringeeCall2 mStringeeCall = new StringeeCall2(mClient, from, to);
            mStringeeCall.setVideoCall(isVideoCall);
            if (customData != null) {
                mStringeeCall.setCustom(customData);
            }

            mStringeeCall.setCallListener(this);

            handler = new Handler(Looper.getMainLooper());
            handler.post(new Runnable() {
                @Override
                public void run() {
                    StringeeAudioManager audioManager = StringeeAudioManager.create(getReactApplicationContext());
                    audioManager.start(RNStringeeCall2Module.this);
                    StringeeManager.getInstance().setAudioManager(audioManager);
                }
            });

            mStringeeCall.makeCall();
        } catch (JSONException e) {
            callback.invoke(false, -4, "The parameters format is invalid.", "");
            return;
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

        handler = new Handler(Looper.getMainLooper());
        handler.post(new Runnable() {
            @Override
            public void run() {
                StringeeAudioManager audioManager = StringeeAudioManager.create(getReactApplicationContext());
                audioManager.start(RNStringeeCall2Module.this);
                StringeeManager.getInstance().setAudioManager(audioManager);
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

        call.answer();
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

        call.reject();

        handler = new Handler(Looper.getMainLooper());
        handler.post(new Runnable() {
            @Override
            public void run() {
                StringeeAudioManager audioManager = StringeeManager.getInstance().getAudioManager();
                if (audioManager != null) {
                    audioManager.stop();
                    audioManager = null;
                }
            }
        });
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

        call.hangup();

        handler = new Handler(Looper.getMainLooper());
        handler.post(new Runnable() {
            @Override
            public void run() {
                StringeeAudioManager audioManager = StringeeManager.getInstance().getAudioManager();
                if (audioManager != null) {
                    audioManager.stop();
                    audioManager = null;
                }
            }
        });
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

        call.getStats(new StringeeCall2.CallStatsListener() {
            @Override
            public void onCallStats(StringeeCall2.StringeeCallStats stringeeCallStats) {
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

        handler = new Handler(Looper.getMainLooper());
        handler.post(new Runnable() {
            @Override
            public void run() {
                StringeeAudioManager audioManager = StringeeManager.getInstance().getAudioManager();
                if (audioManager != null) {
                    audioManager.setSpeakerphoneOn(on);
                }
            }
        });

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
            });
        } catch (JSONException e) {
            callback.invoke(false, -4, "The call info format is invalid.");
        }
    }

    @Override
    public void onSignalingStateChange(StringeeCall2 stringeeCall, StringeeCall2.SignalingState signalingState, String reason, int sipCode, String sipReason) {
        if (contains(jsEvents, "onSignalingStateChange")) {
            if (signalingState == StringeeCall2.SignalingState.CALLING) {
                StringeeManager.getInstance().getCalls2Map().put(stringeeCall.getCallId(), stringeeCall);
                mCallback.invoke(true, 0, "Success", stringeeCall.getCallId(), stringeeCall.getCustomDataFromYourServer());
            }

            WritableMap params = Arguments.createMap();
            params.putString("eventType", "StringeeCall2");
            WritableMap bodyParams = Arguments.createMap();
            bodyParams.putString("callId", stringeeCall.getCallId());
            bodyParams.putInt("code", signalingState.getValue());
            bodyParams.putString("reason", reason);
            bodyParams.putInt("sipCode", sipCode);
            bodyParams.putString("sipReason", sipReason);
            params.putMap("body", bodyParams);
            sendEvent(getReactApplicationContext(), "onSignalingStateChange", params);
        }
    }

    @Override
    public void onError(StringeeCall2 stringeeCall, int code, String desc) {
        mCallback.invoke(false, code, desc, stringeeCall.getCallId(), stringeeCall.getCustomDataFromYourServer());
    }

    @Override
    public void onHandledOnAnotherDevice(StringeeCall2 stringeeCall, StringeeCall2.SignalingState signalingState, String s) {
        if (contains(jsEvents, "onHandledOnAnotherDevice")) {
            WritableMap params = Arguments.createMap();
            params.putString("eventType", "StringeeCall2");
            WritableMap bodyParams = Arguments.createMap();
            bodyParams.putString("callId", stringeeCall.getCallId());
            bodyParams.putInt("code", signalingState.getValue());
            bodyParams.putString("description", s);
            params.putMap("body", bodyParams);
            sendEvent(getReactApplicationContext(), "onHandledOnAnotherDevice", params);
        }
    }

    @Override
    public void onMediaStateChange(StringeeCall2 stringeeCall, StringeeCall2.MediaState mediaState) {
        if (contains(jsEvents, "onMediaStateChange")) {
            WritableMap params = Arguments.createMap();
            params.putString("eventType", "StringeeCall2");
            WritableMap bodyParams = Arguments.createMap();
            bodyParams.putString("callId", stringeeCall.getCallId());
            int code = -1;
            String desc = "";
            if (mediaState == StringeeCall2.MediaState.CONNECTED) {
                code = 0;
                desc = "Connected";
            } else if (mediaState == StringeeCall2.MediaState.DISCONNECTED) {
                code = 1;
                desc = "Disconnected";
            }
            bodyParams.putInt("code", code);
            bodyParams.putString("description", desc);
            params.putMap("body", bodyParams);
            sendEvent(getReactApplicationContext(), "onMediaStateChange", params);
        }
    }

    @Override
    public void onLocalStream(StringeeCall2 stringeeCall) {
        if (contains(jsEvents, "onLocalStream")) {
            WritableMap params = Arguments.createMap();
            params.putString("eventType", "StringeeCall2");
            WritableMap bodyParams = Arguments.createMap();
            bodyParams.putString("callId", stringeeCall.getCallId());
            params.putMap("body", bodyParams);
            sendEvent(getReactApplicationContext(), "onLocalStream", params);
        }
    }

    @Override
    public void onRemoteStream(StringeeCall2 stringeeCall) {
        if (contains(jsEvents, "onRemoteStream")) {
            WritableMap params = Arguments.createMap();
            params.putString("eventType", "StringeeCall2");
            WritableMap bodyParams = Arguments.createMap();
            bodyParams.putString("callId", stringeeCall.getCallId());
            params.putMap("body", bodyParams);
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
            params.putString("eventType", "StringeeCall2");
            WritableMap bodyParams = Arguments.createMap();
            bodyParams.putString("callId", stringeeCall.getCallId());
            bodyParams.putString("data", jsonObject.toString());
            params.putMap("body", bodyParams);
            sendEvent(getReactApplicationContext(), "onCallInfo", params);
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
            params.putString("eventType", "StringeeCall2");
            WritableMap bodyParams = Arguments.createMap();
            bodyParams.putString("selectedAudioDevice", audioDevice.name());
            bodyParams.putArray("availableAudioDevices", availableDevicesMap);
            params.putMap("body", bodyParams);
            sendEvent(getReactApplicationContext(), "onAudioDeviceChange", params);
        }
    }

    private void sendEvent(ReactContext reactContext, String eventName, @Nullable WritableMap eventData) {
        reactContext
                .getJSModule(DeviceEventManagerModule.RCTDeviceEventEmitter.class)
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

    private boolean contains(ArrayList array, String value) {

        for (int i = 0; i < array.size(); i++) {
            if (array.get(i).equals(value)) {
                return true;
            }
        }
        return false;
    }
}
