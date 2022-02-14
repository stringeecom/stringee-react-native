package com.stringeereactnative.conference;

import com.facebook.react.bridge.Callback;
import com.facebook.react.bridge.ReactApplicationContext;
import com.facebook.react.bridge.ReactContextBaseJavaModule;
import com.facebook.react.bridge.ReactMethod;
import com.facebook.react.bridge.ReadableMap;
import com.stringee.video.StringeeRoom;
import com.stringee.video.StringeeVideoTrack;
import com.stringee.video.StringeeVideoTrack.Options;
import com.stringee.video.VideoDimensions;
import com.stringeereactnative.ClientWrapper;
import com.stringeereactnative.common.StringeeManager;

import org.json.JSONException;
import org.json.JSONObject;

public class RNStringeeVideoModule extends ReactContextBaseJavaModule {
    private StringeeManager stringeeManager;
    private ReactApplicationContext reactContext;

    public RNStringeeVideoModule(ReactApplicationContext reactContext) {
        super(reactContext);
        this.reactContext = reactContext;
        stringeeManager = StringeeManager.getInstance();
    }

    @Override
    public String getName() {
        return "RNStringeeVideo";
    }

    @ReactMethod
    public void joinRoom(final String instanceId, final String roomToken, final Callback callback) {
        ClientWrapper clientWrapper = stringeeManager.getClientWrapperMap().get(instanceId);
        if (clientWrapper == null) {
            callback.invoke(false, -1, "StringeeClient is not initialized.");
            return;
        }

        if (!clientWrapper.isConnected()) {
            callback.invoke(false, -1, "StringeeClient is not connected.");
            return;
        }

        clientWrapper.getConferenceWrapper().joinRoom(roomToken, callback);
    }

    @ReactMethod
    public void createLocalVideoTrack(final String instanceId, final ReadableMap optionsMap, final Callback callback) {
        ClientWrapper clientWrapper = stringeeManager.getClientWrapperMap().get(instanceId);
        if (clientWrapper == null) {
            callback.invoke(false, -1, "StringeeClient is not initialized.");
            return;
        }

        if (!clientWrapper.isConnected()) {
            callback.invoke(false, -1, "StringeeClient is not connected.");
            return;
        }

        Options options = new Options();
        options.audio(optionsMap.getBoolean("audio"));
        options.video(optionsMap.getBoolean("video"));
        options.screen(optionsMap.getBoolean("screen"));
        String videoDimensions = optionsMap.getString("videoDimension");
        switch (videoDimensions) {
            case "288":
                options.videoDimensions(VideoDimensions.CIF_VIDEO_DIMENSIONS);
                break;
            case "480":
                options.videoDimensions(VideoDimensions.WVGA_VIDEO_DIMENSIONS);
                break;
            case "720":
                options.videoDimensions(VideoDimensions.HD_720P_VIDEO_DIMENSIONS);
                break;
            case "1080":
                options.videoDimensions(VideoDimensions.HD_1080P_VIDEO_DIMENSIONS);
                break;
        }

        clientWrapper.getConferenceWrapper().createLocalVideoTrack(options, callback);
    }

    @ReactMethod
    public void createCaptureScreenTrack(final String instanceId, final Callback callback) {
        ClientWrapper clientWrapper = stringeeManager.getClientWrapperMap().get(instanceId);
        if (clientWrapper == null) {
            callback.invoke(false, -1, "StringeeClient is not initialized.");
            return;
        }

        if (!clientWrapper.isConnected()) {
            callback.invoke(false, -1, "StringeeClient is not connected.");
            return;
        }

        clientWrapper.getConferenceWrapper().createCaptureScreenTrack(callback);
    }

    @ReactMethod
    public void publish(final String instanceId, final String roomId, final String localId, final Callback callback) {
        ClientWrapper clientWrapper = stringeeManager.getClientWrapperMap().get(instanceId);
        if (clientWrapper == null) {
            callback.invoke(false, -1, "StringeeClient is not initialized.");
            return;
        }

        if (!clientWrapper.isConnected()) {
            callback.invoke(false, -1, "StringeeClient is not connected.");
            return;
        }

        if (roomId == null || roomId.length() == 0) {
            callback.invoke(false, -2, "The room id is invalid.");
            return;
        }

        StringeeRoom room = stringeeManager.getRoomMap().get(roomId);
        if (room == null) {
            callback.invoke(false, -3, "The room is not found.");
            return;
        }

        if (localId == null || localId.length() == 0) {
            callback.invoke(false, -2, "The local id is invalid.");
            return;
        }

        VideoTrackManager trackManager = stringeeManager.getTrackMap().get(localId);
        if (trackManager == null) {
            callback.invoke(false, -3, "The video track is not found.");
            return;
        }

        clientWrapper.getConferenceWrapper().publish(room, trackManager, localId, callback);
    }

    @ReactMethod
    public void unpublish(final String instanceId, final String roomId, final String localId, final Callback callback) {
        ClientWrapper clientWrapper = stringeeManager.getClientWrapperMap().get(instanceId);
        if (clientWrapper == null) {
            callback.invoke(false, -1, "StringeeClient is not initialized.");
            return;
        }

        if (!clientWrapper.isConnected()) {
            callback.invoke(false, -1, "StringeeClient is not connected.");
            return;
        }

        if (roomId == null || roomId.length() == 0) {
            callback.invoke(false, -2, "The room id is invalid.");
            return;
        }

        StringeeRoom room = stringeeManager.getRoomMap().get(roomId);
        if (room == null) {
            callback.invoke(false, -3, "The room is not found.");
            return;
        }

        if (localId == null || localId.length() == 0) {
            callback.invoke(false, -2, "The track id is invalid.");
            return;
        }

        VideoTrackManager trackManager = stringeeManager.getTrackMap().get(localId);
        if (trackManager == null) {
            callback.invoke(false, -3, "The video track is not found.");
            return;
        }

        clientWrapper.getConferenceWrapper().unpublish(room, trackManager, callback);
    }

    @ReactMethod
    public void subscribe(final String instanceId, final String roomId, final String trackId, final ReadableMap optionsMap, final Callback callback) {
        ClientWrapper clientWrapper = stringeeManager.getClientWrapperMap().get(instanceId);
        if (clientWrapper == null) {
            callback.invoke(false, -1, "StringeeClient is not initialized.");
            return;
        }

        if (!clientWrapper.isConnected()) {
            callback.invoke(false, -1, "StringeeClient is not connected.");
            return;
        }

        if (roomId == null || roomId.length() == 0) {
            callback.invoke(false, -2, "The room id is invalid.");
            return;
        }

        StringeeRoom room = stringeeManager.getRoomMap().get(roomId);
        if (room == null) {
            callback.invoke(false, -3, "The room is not found.");
            return;
        }

        if (trackId == null || trackId.length() == 0) {
            callback.invoke(false, -2, "The track id is invalid.");
            return;
        }

        VideoTrackManager trackManager = stringeeManager.getTrackMap().get(trackId);
        if (trackManager == null) {
            callback.invoke(false, -3, "The video track is not found.");
            return;
        }

        Options options = new Options();
        options.audio(optionsMap.getBoolean("audio"));
        options.video(optionsMap.getBoolean("video"));
        options.screen(optionsMap.getBoolean("screen"));
        String videoDimensions = optionsMap.getString("videoDimension");
        switch (videoDimensions) {
            case "288":
                options.videoDimensions(VideoDimensions.CIF_VIDEO_DIMENSIONS);
                break;
            case "480":
                options.videoDimensions(VideoDimensions.WVGA_VIDEO_DIMENSIONS);
                break;
            case "720":
                options.videoDimensions(VideoDimensions.HD_720P_VIDEO_DIMENSIONS);
                break;
            case "1080":
                options.videoDimensions(VideoDimensions.HD_1080P_VIDEO_DIMENSIONS);
                break;
        }

        clientWrapper.getConferenceWrapper().subscribe(room, trackManager, options, callback);
    }

    @ReactMethod
    public void unsubscribe(final String instanceId, final String roomId, final String trackId, final Callback callback) {
        ClientWrapper clientWrapper = stringeeManager.getClientWrapperMap().get(instanceId);
        if (clientWrapper == null) {
            callback.invoke(false, -1, "StringeeClient is not initialized.");
            return;
        }

        if (!clientWrapper.isConnected()) {
            callback.invoke(false, -1, "StringeeClient is not connected.");
            return;
        }

        if (roomId == null || roomId.length() == 0) {
            callback.invoke(false, -2, "The room id is invalid.");
            return;
        }

        StringeeRoom room = stringeeManager.getRoomMap().get(roomId);
        if (room == null) {
            callback.invoke(false, -3, "The room is not found.");
            return;
        }

        if (trackId == null || trackId.length() == 0) {
            callback.invoke(false, -2, "The track id is invalid.");
            return;
        }

        VideoTrackManager trackManager = stringeeManager.getTrackMap().get(trackId);
        if (trackManager == null) {
            callback.invoke(false, -3, "The video track is not found.");
            return;
        }

        clientWrapper.getConferenceWrapper().unsubscribe(room, trackManager, callback);
    }

    @ReactMethod
    public void leave(final String instanceId, final String roomId, final boolean allClient, final Callback callback) {
        ClientWrapper clientWrapper = stringeeManager.getClientWrapperMap().get(instanceId);
        if (clientWrapper == null) {
            callback.invoke(false, -1, "StringeeClient is not initialized.");
            return;
        }

        if (!clientWrapper.isConnected()) {
            callback.invoke(false, -1, "StringeeClient is not connected.");
            return;
        }

        if (roomId == null || roomId.length() == 0) {
            callback.invoke(false, -2, "The room id is invalid.");
            return;
        }

        StringeeRoom room = stringeeManager.getRoomMap().get(roomId);
        if (room == null) {
            callback.invoke(false, -3, "The room is not found.");
            return;
        }

        clientWrapper.getConferenceWrapper().leave(room, allClient, callback);
    }

    @ReactMethod
    public void sendMessage(final String instanceId, final String roomId, final String msg, final Callback callback) {
        ClientWrapper clientWrapper = stringeeManager.getClientWrapperMap().get(instanceId);
        if (clientWrapper == null) {
            callback.invoke(false, -1, "StringeeClient is not initialized.");
            return;
        }

        if (!clientWrapper.isConnected()) {
            callback.invoke(false, -1, "StringeeClient is not connected.");
            return;
        }

        if (roomId == null || roomId.length() == 0) {
            callback.invoke(false, -2, "The room id is invalid.");
            return;
        }

        StringeeRoom room = stringeeManager.getRoomMap().get(roomId);
        if (room == null) {
            callback.invoke(false, -3, "The room is not found.");
            return;
        }

        try {
            JSONObject jsonObject = new JSONObject(msg);
            clientWrapper.getConferenceWrapper().sendMessage(room, jsonObject, callback);
        } catch (JSONException e) {
            e.printStackTrace();
            callback.invoke(false, -2, "Message is not not in JSON format");
        }
    }

    @ReactMethod
    public void mute(final String instanceId, final String trackId, final boolean mute, final Callback callback) {
        ClientWrapper clientWrapper = stringeeManager.getClientWrapperMap().get(instanceId);
        if (clientWrapper == null) {
            callback.invoke(false, -1, "StringeeClient is not initialized.");
            return;
        }

        if (!clientWrapper.isConnected()) {
            callback.invoke(false, -1, "StringeeClient is not connected.");
            return;
        }

        if (trackId == null || trackId.length() == 0) {
            callback.invoke(false, -2, "The track id is invalid.");
            return;
        }

        VideoTrackManager trackManager = stringeeManager.getTrackMap().get(trackId);
        if (trackManager == null) {
            callback.invoke(false, -3, "The video track is not found.");
            return;
        }

        clientWrapper.getConferenceWrapper().mute(trackManager, mute, callback);
    }

    @ReactMethod
    public void enableVideo(final String instanceId, final String trackId, final boolean enable, final Callback callback) {
        ClientWrapper clientWrapper = stringeeManager.getClientWrapperMap().get(instanceId);
        if (clientWrapper == null) {
            callback.invoke(false, -1, "StringeeClient is not initialized.");
            return;
        }

        if (!clientWrapper.isConnected()) {
            callback.invoke(false, -1, "StringeeClient is not connected.");
            return;
        }

        if (trackId == null || trackId.length() == 0) {
            callback.invoke(false, -2, "The track id is invalid.");
            return;
        }

        VideoTrackManager trackManager = stringeeManager.getTrackMap().get(trackId);
        if (trackManager == null) {
            callback.invoke(false, -3, "The video track is not found.");
            return;
        }

        clientWrapper.getConferenceWrapper().enableVideo(trackManager, enable, callback);
    }

    @ReactMethod
    public void switchCamera(final String instanceId, final String trackId, final Callback callback) {
        ClientWrapper clientWrapper = stringeeManager.getClientWrapperMap().get(instanceId);
        if (clientWrapper == null) {
            callback.invoke(false, -1, "StringeeClient is not initialized.");
            return;
        }

        if (!clientWrapper.isConnected()) {
            callback.invoke(false, -1, "StringeeClient is not connected.");
            return;
        }

        if (trackId == null || trackId.length() == 0) {
            callback.invoke(false, -2, "The track id is invalid.");
            return;
        }

        VideoTrackManager trackManager = stringeeManager.getTrackMap().get(trackId);
        if (trackManager == null) {
            callback.invoke(false, -3, "The video track is not found.");
            return;
        }

        clientWrapper.getConferenceWrapper().switchCamera(trackManager, callback);
    }

    @ReactMethod
    public void switchCameraWithId(final String instanceId, final String trackId, final int cameraId, final Callback callback) {
        ClientWrapper clientWrapper = stringeeManager.getClientWrapperMap().get(instanceId);
        if (clientWrapper == null) {
            callback.invoke(false, -1, "StringeeClient is not initialized.");
            return;
        }

        if (!clientWrapper.isConnected()) {
            callback.invoke(false, -1, "StringeeClient is not connected.");
            return;
        }

        if (trackId == null || trackId.length() == 0) {
            callback.invoke(false, -2, "The track id is invalid.");
            return;
        }

        VideoTrackManager trackManager = stringeeManager.getTrackMap().get(trackId);
        if (trackManager == null) {
            callback.invoke(false, -3, "The video track is not found.");
            return;
        }

        clientWrapper.getConferenceWrapper().switchCameraWithId(trackManager, cameraId, callback);
    }

    @ReactMethod
    public void setNativeEvent(String event) {
        stringeeManager.getRoomEvents().add(event);
    }

    @ReactMethod
    public void removeNativeEvent(String event) {
        stringeeManager.getRoomEvents().remove(event);
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
