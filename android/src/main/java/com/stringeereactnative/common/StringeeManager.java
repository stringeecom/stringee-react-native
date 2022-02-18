package com.stringeereactnative.common;

import com.stringee.common.StringeeAudioManager;
import com.stringee.video.StringeeRoom;
import com.stringeereactnative.ClientWrapper;
import com.stringeereactnative.call.Call2Wrapper;
import com.stringeereactnative.call.CallWrapper;
import com.stringeereactnative.conference.ScreenCaptureManager;
import com.stringeereactnative.conference.VideoTrackManager;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

public class StringeeManager {
    private static StringeeManager instance;
    private StringeeAudioManager audioManager;
    private ScreenCaptureManager captureManager;
    private Map<String, ClientWrapper> clientWrapperMap;
    private Map<String, CallWrapper> callWrapperMap;
    private Map<String, Call2Wrapper> call2WrapperMap;
    private Map<String, VideoTrackManager> trackMap;
    private Map<String, Map<String, Object>> localViewOption;
    private Map<String, Map<String, Object>> remoteViewOption;
    private Map<String, StringeeRoom> roomMap;
    private ArrayList<String> callEvents;
    private ArrayList<String> call2Events;
    private ArrayList<String> roomEvents;

    public static synchronized StringeeManager getInstance() {
        if (instance == null) {
            instance = new StringeeManager();
        }
        return instance;
    }

    public StringeeManager() {
        this.clientWrapperMap = new HashMap<>();
        this.callWrapperMap = new HashMap<>();
        this.call2WrapperMap = new HashMap<>();
        this.trackMap = new HashMap<>();
        this.localViewOption = new HashMap<>();
        this.remoteViewOption = new HashMap<>();
        this.roomMap = new HashMap<>();

        this.callEvents = new ArrayList<>();
        this.call2Events = new ArrayList<>();
        this.roomEvents = new ArrayList<>();
    }

    public Map<String, ClientWrapper> getClientWrapperMap() {
        return clientWrapperMap;
    }

    public Map<String, CallWrapper> getCallWrapperMap() {
        return callWrapperMap;
    }

    public Map<String, Call2Wrapper> getCall2WrapperMap() {
        return call2WrapperMap;
    }

    public Map<String, VideoTrackManager> getTrackMap() {
        return trackMap;
    }

    public Map<String, Map<String, Object>> getLocalViewOption() {
        return localViewOption;
    }

    public Map<String, Map<String, Object>> getRemoteViewOption() {
        return remoteViewOption;
    }

    public Map<String, StringeeRoom> getRoomMap() {
        return roomMap;
    }

    public ArrayList<String> getCallEvents() {
        return callEvents;
    }

    public ArrayList<String> getCall2Events() {
        return call2Events;
    }

    public ArrayList<String> getRoomEvents() {
        return roomEvents;
    }

    public StringeeAudioManager getAudioManager() {
        return audioManager;
    }

    public void setAudioManager(StringeeAudioManager audioManager) {
        this.audioManager = audioManager;
    }

    public ScreenCaptureManager getCaptureManager() {
        return captureManager;
    }

    public void setCaptureManager(ScreenCaptureManager captureManager) {
        this.captureManager = captureManager;
    }

}
