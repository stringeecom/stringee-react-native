package com.stringeereactnative.call;

import android.text.TextUtils;

import com.facebook.react.bridge.Callback;
import com.facebook.react.bridge.ReactApplicationContext;
import com.facebook.react.bridge.ReactContextBaseJavaModule;
import com.facebook.react.bridge.ReactMethod;
import com.stringee.call.StringeeCall;
import com.stringee.common.StringeeConstant;
import com.stringeereactnative.ClientWrapper;
import com.stringeereactnative.common.StringeeManager;

import org.json.JSONException;
import org.json.JSONObject;

public class RNStringeeCallModule extends ReactContextBaseJavaModule {
    private StringeeManager stringeeManager;

    public RNStringeeCallModule(ReactApplicationContext reactContext) {
        super(reactContext);
        stringeeManager = StringeeManager.getInstance();
    }

    @Override
    public String getName() {
        return "RNStringeeCall";
    }

    @ReactMethod
    public void makeCall(final String instanceId, final String params, final Callback callback) {
        ClientWrapper clientWrapper = stringeeManager.getClientWrapperMap().get(instanceId);
        if (clientWrapper == null) {
            callback.invoke(false, -1, "StringeeClient is not initialized");
            return;
        }

        if (!clientWrapper.isConnected()) {
            callback.invoke(false, -1, "StringeeClient is not connected.");
            return;
        }

        try {
            JSONObject jsonObject = new JSONObject(params);
            String from = jsonObject.getString("from");
            String to = jsonObject.getString("to");
            boolean isVideoCall = jsonObject.getBoolean("isVideoCall");
            String customData = jsonObject.optString("customData");
            String resolution = jsonObject.optString("videoResolution");

            final StringeeCall stringeeCall = new StringeeCall(clientWrapper.getClient(), from, to);
            stringeeCall.setVideoCall(isVideoCall);
            if (!TextUtils.isEmpty(customData)) {
                stringeeCall.setCustom(customData);
            }
            if (!TextUtils.isEmpty(resolution)) {
                if (resolution.equalsIgnoreCase("NORMAL")) {
                    stringeeCall.setQuality(StringeeConstant.QUALITY_NORMAL);
                } else if (resolution.equalsIgnoreCase("HD")) {
                    stringeeCall.setQuality(StringeeConstant.QUALITY_HD);
                }
            }

            CallWrapper callWrapper = new CallWrapper(getReactApplicationContext(), stringeeCall, callback);
            callWrapper.makeCall();
        } catch (JSONException e) {
            callback.invoke(false, -4, "The parameters format is invalid.", "");
            return;
        }
    }

    @ReactMethod
    public void initAnswer(final String instanceId, final String callId, final Callback callback) {
        ClientWrapper clientWrapper = stringeeManager.getClientWrapperMap().get(instanceId);
        if (clientWrapper == null) {
            callback.invoke(false, -1, "StringeeClient is not initialized");
            return;
        }

        if (!clientWrapper.isConnected()) {
            callback.invoke(false, -1, "StringeeClient is not connected.");
            return;
        }

        if (callId == null || callId.length() == 0) {
            callback.invoke(false, -2, "The call id is invalid.");
            return;
        }

        CallWrapper callWrapper = stringeeManager.getCallWrapperMap().get(callId);

        if (callWrapper == null) {
            callback.invoke(false, -3, "The call is not found.");
            return;
        }

        callWrapper.initAnswer(callback);
    }

    @ReactMethod
    public void answer(final String callId, final Callback callback) {
        if (callId == null || callId.length() == 0) {
            callback.invoke(false, -2, "The call id is invalid.");
            return;
        }

        CallWrapper callWrapper = stringeeManager.getCallWrapperMap().get(callId);
        if (callWrapper == null) {
            callback.invoke(false, -3, "The call is not found.");
            return;
        }

        callWrapper.answer(callback);
    }

    @ReactMethod
    public void reject(final String callId, final Callback callback) {
        if (callId == null || callId.length() == 0) {
            callback.invoke(false, -2, "The call id is invalid.");
            return;
        }

        CallWrapper callWrapper = stringeeManager.getCallWrapperMap().get(callId);
        if (callWrapper == null) {
            callback.invoke(false, -3, "The call is not found.");
            return;
        }

        callWrapper.reject(callback);
    }

    @ReactMethod
    public void hangup(final String callId, final Callback callback) {
        if (callId == null || callId.length() == 0) {
            callback.invoke(false, -2, "The call id is invalid.");
            return;
        }

        CallWrapper callWrapper = stringeeManager.getCallWrapperMap().get(callId);
        if (callWrapper == null) {
            callback.invoke(false, -3, "The call is not found.");
            return;
        }

        callWrapper.hangup(callback);
    }

    @ReactMethod
    public void enableVideo(final String callId, final boolean enabled, final Callback callback) {
        if (callId == null || callId.length() == 0) {
            callback.invoke(false, -2, "The call id is invalid.");
            return;
        }

        CallWrapper callWrapper = stringeeManager.getCallWrapperMap().get(callId);
        if (callWrapper == null) {
            callback.invoke(false, -3, "The call is not found.");
            return;
        }
        callWrapper.enableVideo(enabled, callback);
    }

    @ReactMethod
    public void mute(final String callId, final boolean isMute, final Callback callback) {
        if (callId == null || callId.length() == 0) {
            callback.invoke(false, -2, "The call id is invalid.");
            return;
        }

        CallWrapper callWrapper = stringeeManager.getCallWrapperMap().get(callId);
        if (callWrapper == null) {
            callback.invoke(false, -3, "The call is not found.");
            return;
        }

        callWrapper.mute(isMute, callback);
    }

    @ReactMethod
    public void sendCallInfo(final String callId, final String info, final Callback callback) {
        if (callId == null || callId.length() == 0) {
            callback.invoke(false, -2, "The call id is invalid.");
            return;
        }

        CallWrapper callWrapper = stringeeManager.getCallWrapperMap().get(callId);
        if (callWrapper == null) {
            callback.invoke(false, -3, "The call is not found.");
            return;
        }
        try {
            JSONObject jsonObject = new JSONObject(info);
            callWrapper.sendCallInfo(jsonObject, callback);
        } catch (JSONException e) {
            callback.invoke(false, -4, "The call info format is invalid.");
        }
    }

    @ReactMethod
    public void sendDTMF(final String callId, final String key, final Callback callback) {
        if (callId == null || callId.length() == 0) {
            callback.invoke(false, -2, "The call id is invalid.");
            return;
        }

        CallWrapper callWrapper = stringeeManager.getCallWrapperMap().get(callId);
        if (callWrapper == null) {
            callback.invoke(false, -3, "The call is not found.");
            return;
        }
        callWrapper.sendDTMF(key, callback);
    }

    @ReactMethod
    public void switchCamera(final String callId, final Callback callback) {
        if (callId == null || callId.length() == 0) {
            callback.invoke(false, -2, "The call id is invalid.");
            return;
        }

        CallWrapper callWrapper = stringeeManager.getCallWrapperMap().get(callId);
        if (callWrapper == null) {
            callback.invoke(false, -3, "The call is not found.");
            return;
        }
        callWrapper.switchCamera(callback);
    }

    @ReactMethod
    public void switchCameraWithId(final String callId, final int cameraId, final Callback callback) {
        if (callId == null || callId.length() == 0) {
            callback.invoke(false, -2, "The call id is invalid.");
            return;
        }

        CallWrapper callWrapper = stringeeManager.getCallWrapperMap().get(callId);
        if (callWrapper == null) {
            callback.invoke(false, -3, "The call is not found.");
            return;
        }
        callWrapper.switchCameraWithId(cameraId, callback);
    }

    @ReactMethod
    public void getCallStats(final String instanceId, final String callId, final Callback callback) {
        ClientWrapper clientWrapper = stringeeManager.getClientWrapperMap().get(instanceId);
        if (clientWrapper == null) {
            callback.invoke(false, -1, "StringeeClient is not initialized");
            return;
        }

        if (!clientWrapper.isConnected()) {
            callback.invoke(false, -1, "StringeeClient is not connected.");
            return;
        }

        if (callId == null || callId.length() == 0) {
            callback.invoke(false, -2, "The call id is invalid.", "");
            return;
        }

        CallWrapper callWrapper = stringeeManager.getCallWrapperMap().get(callId);
        if (callWrapper == null) {
            callback.invoke(false, -3, "The call is not found.", "");
            return;
        }

        callWrapper.getCallStats(callback);
    }

    @ReactMethod
    public void setSpeakerphoneOn(final String callId, final boolean on, final Callback callback) {
        if (callId == null || callId.length() == 0) {
            callback.invoke(false, -2, "The call id is invalid.");
            return;
        }

        CallWrapper callWrapper = stringeeManager.getCallWrapperMap().get(callId);
        if (callWrapper == null) {
            callback.invoke(false, -3, "The call is not found.");
            return;
        }

        callWrapper.setSpeakerphoneOn(on, callback);
    }

    @ReactMethod
    public void resumeVideo(final String callId, final Callback callback) {
        if (callId == null || callId.length() == 0) {
            callback.invoke(false, -2, "The call id is invalid.");
            return;
        }

        CallWrapper callWrapper = stringeeManager.getCallWrapperMap().get(callId);
        if (callWrapper == null) {
            callback.invoke(false, -3, "The call is not found.");
            return;
        }

        callWrapper.resumeVideo(callback);
    }

    @ReactMethod
    public void setMirror(final String callId, final boolean isLocal, final boolean isMirror, final Callback callback) {
        if (callId == null || callId.length() == 0) {
            callback.invoke(false, -2, "The call id is invalid.");
            return;
        }

        CallWrapper callWrapper = stringeeManager.getCallWrapperMap().get(callId);
        if (callWrapper == null) {
            callback.invoke(false, -3, "The call is not found.");
            return;
        }

        callWrapper.setMirror(isLocal, isMirror, callback);
    }

    @ReactMethod
    public void setNativeEvent(String event) {
        stringeeManager.getCallEvents().add(event);
    }

    @ReactMethod
    public void removeNativeEvent(String event) {
        stringeeManager.getCallEvents().remove(event);
    }

    @ReactMethod
    public void addListener(String eventName) {
        // Keep: Required for RN built in Event Emitter Calls.
    }

    @ReactMethod
    public void removeListeners(Integer count) {
        // Keep: Required for RN built in Event Emitter Calls.
    }
}
