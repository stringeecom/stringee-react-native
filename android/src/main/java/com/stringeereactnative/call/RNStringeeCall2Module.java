package com.stringeereactnative.call;

import android.text.TextUtils;

import com.facebook.react.bridge.Callback;
import com.facebook.react.bridge.ReactApplicationContext;
import com.facebook.react.bridge.ReactContextBaseJavaModule;
import com.facebook.react.bridge.ReactMethod;
import com.stringee.call.StringeeCall2;
import com.stringeereactnative.ClientWrapper;
import com.stringeereactnative.common.StringeeManager;

import org.json.JSONException;
import org.json.JSONObject;

public class RNStringeeCall2Module extends ReactContextBaseJavaModule {
    private StringeeManager stringeeManager;

    public RNStringeeCall2Module(ReactApplicationContext reactContext) {
        super(reactContext);
        stringeeManager = StringeeManager.getInstance();
    }

    @Override
    public String getName() {
        return "RNStringeeCall2";
    }

    @ReactMethod
    public void makeCall(final String instanceId, final String params, final Callback callback) {
        ClientWrapper clientWrapper = stringeeManager.getClientWrapperMap().get(instanceId);
        if (clientWrapper == null) {
            callback.invoke(false, -1, "StringeeClient is not initialized.");
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

            final StringeeCall2 stringeeCall = new StringeeCall2(clientWrapper.getClient(), from, to);
            stringeeCall.setVideoCall(isVideoCall);
            if (!TextUtils.isEmpty(customData)) {
                stringeeCall.setCustom(customData);
            }

            Call2Wrapper call2Wrapper = new Call2Wrapper(getReactApplicationContext(), stringeeCall, callback);
            call2Wrapper.makeCall();
        } catch (JSONException e) {
            callback.invoke(false, -4, "The parameters format is invalid.", "");
            return;
        }
    }

    @ReactMethod
    public void initAnswer(final String instanceId, final String callId, final Callback callback) {
        ClientWrapper clientWrapper = stringeeManager.getClientWrapperMap().get(instanceId);
        if (clientWrapper == null) {
            callback.invoke(false, -1, "StringeeClient is not initialized.");
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

        Call2Wrapper call2Wrapper = stringeeManager.getCall2WrapperMap().get(callId);
        if (call2Wrapper == null) {
            callback.invoke(false, -3, "The call is not found.");
            return;
        }

        call2Wrapper.initAnswer(callback);
    }

    @ReactMethod
    public void answer(final String callId, final Callback callback) {
        if (callId == null || callId.length() == 0) {
            callback.invoke(false, -2, "The call id is invalid.");
            return;
        }

        Call2Wrapper call2Wrapper = stringeeManager.getCall2WrapperMap().get(callId);
        if (call2Wrapper == null) {
            callback.invoke(false, -3, "The call is not found.");
            return;
        }

        call2Wrapper.answer(callback);
    }

    @ReactMethod
    public void reject(final String callId, final Callback callback) {
        if (callId == null || callId.length() == 0) {
            callback.invoke(false, -2, "The call id is invalid.");
            return;
        }

        Call2Wrapper call2Wrapper = stringeeManager.getCall2WrapperMap().get(callId);
        if (call2Wrapper == null) {
            callback.invoke(false, -3, "The call is not found.");
            return;
        }

        call2Wrapper.reject(callback);
    }

    @ReactMethod
    public void hangup(final String callId, final Callback callback) {
        if (callId == null || callId.length() == 0) {
            callback.invoke(false, -2, "The call id is invalid.");
            return;
        }

        Call2Wrapper call2Wrapper = stringeeManager.getCall2WrapperMap().get(callId);
        if (call2Wrapper == null) {
            callback.invoke(false, -3, "The call is not found.");
            return;
        }

        call2Wrapper.hangup(callback);
    }

    @ReactMethod
    public void enableVideo(final String callId, final boolean enabled, final Callback callback) {
        if (callId == null || callId.length() == 0) {
            callback.invoke(false, -2, "The call id is invalid.");
            return;
        }

        Call2Wrapper call2Wrapper = stringeeManager.getCall2WrapperMap().get(callId);
        if (call2Wrapper == null) {
            callback.invoke(false, -3, "The call is not found.");
            return;
        }
        call2Wrapper.enableVideo(enabled, callback);
    }

    @ReactMethod
    public void mute(final String callId, final boolean isMute, final Callback callback) {
        if (callId == null || callId.length() == 0) {
            callback.invoke(false, -2, "The call id is invalid.");
            return;
        }

        Call2Wrapper call2Wrapper = stringeeManager.getCall2WrapperMap().get(callId);
        if (call2Wrapper == null) {
            callback.invoke(false, -3, "The call is not found.");
            return;
        }

        call2Wrapper.mute(isMute, callback);
    }

    @ReactMethod
    public void switchCamera(final String callId, final Callback callback) {
        if (callId == null || callId.length() == 0) {
            callback.invoke(false, -2, "The call id is invalid.");
            return;
        }

        Call2Wrapper call2Wrapper = stringeeManager.getCall2WrapperMap().get(callId);
        if (call2Wrapper == null) {
            callback.invoke(false, -3, "The call is not found.");
            return;
        }
        call2Wrapper.switchCamera(callback);
    }

    @ReactMethod
    public void switchCameraWithId(final String callId, final int cameraId, final Callback callback) {
        if (callId == null || callId.length() == 0) {
            callback.invoke(false, -2, "The call id is invalid.");
            return;
        }

        Call2Wrapper call2Wrapper = stringeeManager.getCall2WrapperMap().get(callId);
        if (call2Wrapper == null) {
            callback.invoke(false, -3, "The call is not found.");
            return;
        }
        call2Wrapper.switchCameraWithId(cameraId, callback);
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

        Call2Wrapper call2Wrapper = stringeeManager.getCall2WrapperMap().get(callId);
        if (call2Wrapper == null) {
            callback.invoke(false, -3, "The call is not found.", "");
            return;
        }

        call2Wrapper.getCallStats(callback);
    }

    @ReactMethod
    public void setSpeakerphoneOn(final String callId, final boolean on, final Callback callback) {
        if (callId == null || callId.length() == 0) {
            callback.invoke(false, -2, "The call id is invalid.");
            return;
        }

        Call2Wrapper call2Wrapper = stringeeManager.getCall2WrapperMap().get(callId);
        if (call2Wrapper == null) {
            callback.invoke(false, -3, "The call is not found.", "");
            return;
        }

        call2Wrapper.setSpeakerphoneOn(on, callback);
    }

    @ReactMethod
    public void resumeVideo(final String callId, final Callback callback) {
        if (callId == null || callId.length() == 0) {
            callback.invoke(false, -2, "The call id is invalid.");
            return;
        }

        Call2Wrapper call2Wrapper = stringeeManager.getCall2WrapperMap().get(callId);
        if (call2Wrapper == null) {
            callback.invoke(false, -3, "The call is not found.");
            return;
        }

        call2Wrapper.resumeVideo(callback);
    }

    @ReactMethod
    public void sendCallInfo(final String callId, final String info, final Callback callback) {
        if (callId == null || callId.length() == 0) {
            callback.invoke(false, -2, "The call id is invalid.");
            return;
        }

        Call2Wrapper call2Wrapper = stringeeManager.getCall2WrapperMap().get(callId);
        if (call2Wrapper == null) {
            callback.invoke(false, -3, "The call is not found.");
            return;
        }
        try {
            JSONObject jsonObject = new JSONObject(info);
            call2Wrapper.sendCallInfo(jsonObject, callback);
        } catch (JSONException e) {
            callback.invoke(false, -4, "The call info format is invalid.");
        }
    }

    @ReactMethod
    public void sendDTMF(final String callId, final String dtmf, final Callback callback) {
        if (callId == null || callId.length() == 0) {
            callback.invoke(false, -2, "The call id is invalid.");
            return;
        }

        Call2Wrapper call2Wrapper = stringeeManager.getCall2WrapperMap().get(callId);
        if (call2Wrapper == null) {
            callback.invoke(false, -3, "The call is not found.");
            return;
        }

        call2Wrapper.sendDTMF(dtmf, callback);
    }

    @ReactMethod
    public void setMirror(final String callId, final boolean isLocal, final boolean isMirror, final Callback callback) {
        if (callId == null || callId.length() == 0) {
            callback.invoke(false, -2, "The call id is invalid.");
            return;
        }

        Call2Wrapper call2Wrapper = stringeeManager.getCall2WrapperMap().get(callId);
        if (call2Wrapper == null) {
            callback.invoke(false, -3, "The call is not found.");
            return;
        }

        call2Wrapper.setMirror(isLocal, isMirror, callback);
    }

    @ReactMethod
    public void startCapture(final String callId, final Callback callback) {
        if (callId == null || callId.length() == 0) {
            callback.invoke(false, -2, "The call id is invalid.");
            return;
        }

        Call2Wrapper call2Wrapper = stringeeManager.getCall2WrapperMap().get(callId);
        if (call2Wrapper == null) {
            callback.invoke(false, -3, "The call is not found.");
            return;
        }

        call2Wrapper.startCapture(callback);
    }

    @ReactMethod
    public void stopCapture(final String callId, final Callback callback) {
        if (callId == null || callId.length() == 0) {
            callback.invoke(false, -2, "The call id is invalid.");
            return;
        }

        Call2Wrapper call2Wrapper = stringeeManager.getCall2WrapperMap().get(callId);
        if (call2Wrapper == null) {
            callback.invoke(false, -3, "The call is not found.");
            return;
        }

        call2Wrapper.stopCapture(callback);
    }

    @ReactMethod
    public void setNativeEvent(String event) {
        stringeeManager.getCall2Events().add(event);
    }

    @ReactMethod
    public void removeNativeEvent(String event) {
        stringeeManager.getCall2Events().remove(event);
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
