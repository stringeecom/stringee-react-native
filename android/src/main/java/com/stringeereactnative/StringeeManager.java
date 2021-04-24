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
    private StringeeAudioManager audioManager;
    private Map<String, StringeeClient> clientsMap = new HashMap<>();
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

    public Map<String, StringeeClient> getClientsMap() {
        return clientsMap;
    }

    public Map<String, StringeeCall> getCallsMap() {
        return callsMap;
    }

    public Map<String, StringeeCall2> getCalls2Map() {
        return calls2Map;
    }

    public Map<String, StringeeStream> getStreamsMap() {
        return streamsMap;
    }

    public Map<Integer, StringeeRoom> getRoomsMap() {
        return roomsMap;
    }

    public StringeeAudioManager getAudioManager() {
        return audioManager;
    }

    public void setAudioManager(StringeeAudioManager audioManager) {
        this.audioManager = audioManager;
    }
}
