package com.stringeereactnative;

import com.stringee.StringeeClient;
import com.stringee.call.StringeeCall;
import com.stringee.call.StringeeCall2;
import com.stringee.video.StringeeRoom;
import com.stringee.video.StringeeStream;

import java.util.HashMap;
import java.util.Map;

public class StringeeManager {

    private static StringeeManager stringeeManager;
    private StringeeClient mClient;
    private StringeeAudioManager audioManager;
    private Map<String, StringeeCall> callsMap = new HashMap<>();
    private Map<String, StringeeCall2> calls2Map = new HashMap<>();
    private Map<String, StringeeStream> streamsMap = new HashMap<>();
    private Map<Integer, StringeeRoom> roomsMap = new HashMap<>();

    public static synchronized StringeeManager getInstance() {
        if (stringeeManager == null) {
            stringeeManager = new StringeeManager();
        }

        return stringeeManager;
    }

    public StringeeClient getClient() {
        return mClient;
    }

    public void setClient(StringeeClient mClient) {
        this.mClient = mClient;
    }

    public Map<String, StringeeCall> getCallsMap() {
        return callsMap;
    }

    public void setCallsMap(Map<String, StringeeCall> callsMap) {
        this.callsMap = callsMap;
    }

    public Map<String, StringeeCall2> getCalls2Map() {
        return calls2Map;
    }

    public void setCalls2Map(Map<String, StringeeCall2> calls2Map) {
        this.calls2Map = calls2Map;
    }

    public Map<String, StringeeStream> getStreamsMap() {
        return streamsMap;
    }

    public void setStreamsMap(Map<String, StringeeStream> streamsMap) {
        this.streamsMap = streamsMap;
    }

    public Map<Integer, StringeeRoom> getRoomsMap() {
        return roomsMap;
    }

    public void setRoomsMap(Map<Integer, StringeeRoom> roomsMap) {
        this.roomsMap = roomsMap;
    }

    public StringeeAudioManager getAudioManager() {
        return audioManager;
    }

    public void setAudioManager(StringeeAudioManager audioManager) {
        this.audioManager = audioManager;
    }
}
