package com.stringeereactnative;

import androidx.annotation.Nullable;
import android.util.Log;
import android.os.Handler;
import android.os.Looper;

import com.facebook.react.bridge.Arguments;
import com.facebook.react.bridge.Callback;
import com.facebook.react.bridge.ReactApplicationContext;
import com.facebook.react.bridge.ReactContext;
import com.facebook.react.bridge.ReactContextBaseJavaModule;
import com.facebook.react.bridge.ReactMethod;
import com.facebook.react.bridge.WritableMap;
import com.facebook.react.modules.core.DeviceEventManagerModule;
import com.stringee.call.StringeeCall2;
import com.stringee.StringeeClient;
import com.stringee.common.StringeeConstant;
import com.stringee.exception.StringeeError;
import com.stringee.listener.StatusListener;

import org.json.JSONException;
import org.json.JSONObject;

import java.util.Set;
import java.util.ArrayList;

public class RNStringeeCall2Module extends ReactContextBaseJavaModule implements StringeeCall2.StringeeCallListener {

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

            handler = new Handler(Looper.getMainLooper());
            handler.post(new Runnable() {
                @Override
                public void run() {
                    StringeeAudioManager audioManager = StringeeAudioManager.create(getReactApplicationContext());
                    audioManager.start(new StringeeAudioManager.AudioManagerEvents() {
                        @Override
                        public void onAudioDeviceChanged(StringeeAudioManager.AudioDevice selectedAudioDevice, Set<StringeeAudioManager.AudioDevice> availableAudioDevices) {
                            if (!mStringeeCall.isVideoCall()) {
                                switch (selectedAudioDevice) {
                                    case WIRED_HEADSET:
                                        audioManager.setSpeakerphoneOn(false);
                                        break;
                                    case BLUETOOTH:
                                        audioManager.setSpeakerphoneOn(false);
                                        break;
                                    case SPEAKER_PHONE:
                                        audioManager.setSpeakerphoneOn(mStringeeCall.isVideoCall());
                                        break;
                                }
                            } else {
                                if (selectedAudioDevice == StringeeAudioManager.AudioDevice.WIRED_HEADSET || selectedAudioDevice == StringeeAudioManager.AudioDevice.BLUETOOTH) {
                                    audioManager.setSpeakerphoneOn(false);
                                } else {
                                    audioManager.setSpeakerphoneOn(true);
                                }
                            }
                            Log.d("Stringee", "onAudioManagerDevicesChanged: " + availableAudioDevices + ", "
                                    + "selected: " + selectedAudioDevice);
                        }
                    });
                    StringeeManager.getInstance().setAudioManager(audioManager);
                }
            });
            mStringeeCall.setCallListener(this);
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

        handler = new Handler(Looper.getMainLooper());
        handler.post(new Runnable() {
            @Override
            public void run() {
                StringeeAudioManager audioManager = StringeeAudioManager.create(getReactApplicationContext());
                audioManager.start(new StringeeAudioManager.AudioManagerEvents() {
                    @Override
                    public void onAudioDeviceChanged(StringeeAudioManager.AudioDevice selectedAudioDevice, Set<StringeeAudioManager.AudioDevice> availableAudioDevices) {
                        if (!call.isVideoCall()) {
                            switch (selectedAudioDevice) {
                                case WIRED_HEADSET:
                                    audioManager.setSpeakerphoneOn(false);
                                    break;
                                case BLUETOOTH:
                                    audioManager.setSpeakerphoneOn(false);
                                    break;
                                case SPEAKER_PHONE:
                                    audioManager.setSpeakerphoneOn(call.isVideoCall());
                                    break;
                            }
                        } else {
                            if (selectedAudioDevice == StringeeAudioManager.AudioDevice.WIRED_HEADSET || selectedAudioDevice == StringeeAudioManager.AudioDevice.BLUETOOTH) {
                                audioManager.setSpeakerphoneOn(false);
                            } else {
                                audioManager.setSpeakerphoneOn(true);
                            }
                        }
                        Log.d("Stringee", "onAudioManagerDevicesChanged: " + availableAudioDevices + ", "
                                + "selected: " + selectedAudioDevice);
                    }
                });
                StringeeManager.getInstance().setAudioManager(audioManager);
            }
        });

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
        call.switchCamera(new com.stringee.listener.StatusListener() {
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

        StringeeCall2 call = StringeeManager.getInstance().getCalls2Map().get(callId);
        if (call == null) {
            callback.invoke(false, -3, "The call is not found.");
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

    @Override
    public void onSignalingStateChange(StringeeCall2 stringeeCall, StringeeCall2.SignalingState signalingState, String reason, int sipCode, String sipReason) {
        if (contains(jsEvents, "onSignalingStateChange")) {
            if (signalingState == StringeeCall2.SignalingState.CALLING) {
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
    public void onHandledOnAnotherDevice(StringeeCall2 stringeeCall, StringeeCall2.SignalingState signalingState, String s) {
        if (contains(jsEvents, "onHandledOnAnotherDevice")) {
            WritableMap params = Arguments.createMap();
            params.putString("callId", stringeeCall.getCallId());
            params.putInt("code", signalingState.getValue());
            params.putString("description", s);
            sendEvent(getReactApplicationContext(), "onHandledOnAnotherDevice", params);
        }
    }

    @Override
    public void onMediaStateChange(StringeeCall2 stringeeCall, StringeeCall2.MediaState mediaState) {
        if (contains(jsEvents, "onMediaStateChange")) {
            WritableMap params = Arguments.createMap();
            params.putString("callId", stringeeCall.getCallId());
            int code = -1;
            String desc = "";
            if (mediaState == StringeeCall2.MediaState.CONNECTED) {
                code = 0;
                desc = "Connected";
            } else if (mediaState == StringeeCall2.MediaState.DISCONNECTED) {
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
    public void onCallInfo(StringeeCall2 stringeeCall, JSONObject jsonObject) {
        if (contains(jsEvents, "onCallInfo")) {
            WritableMap params = Arguments.createMap();
            params.putString("callId", stringeeCall.getCallId());
            params.putString("data", jsonObject.toString());
            sendEvent(getReactApplicationContext(), "onCallInfo", params);
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
