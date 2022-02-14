package com.stringeereactnative.conference;

import com.facebook.react.bridge.Arguments;
import com.facebook.react.bridge.WritableMap;
import com.stringee.video.StringeeVideoTrack;
import com.stringee.video.StringeeVideoTrack.Listener;
import com.stringeereactnative.ClientWrapper;
import com.stringeereactnative.common.StringeeManager;
import com.stringeereactnative.common.Utils;

public class VideoTrackManager implements Listener {
    private ClientWrapper clientWrapper;
    private String localId;
    private StringeeVideoTrack videoTrack;
    private boolean mediaAvailable = false;
    private boolean forCall = false;
    private Listener listener;

    public VideoTrackManager(ClientWrapper clientWrapper, StringeeVideoTrack videoTrack, String localId, boolean forCall) {
        this.clientWrapper = clientWrapper;
        this.videoTrack = videoTrack;
        this.localId = localId;
        this.forCall = forCall;
        videoTrack.setListener(this);
        if (forCall) {
            mediaAvailable = true;
        }
    }

    public void setListener(Listener listener) {
        this.listener = listener;
        if (mediaAvailable) {
            if (listener != null) {
                listener.onMediaAvailable();
            }
        }
    }

    public StringeeVideoTrack getVideoTrack() {
        return videoTrack;
    }

    public String getLocalId() {
        return localId;
    }

    public String getUserId() {
        return videoTrack.isLocal() ? clientWrapper.getUserId() : videoTrack.getUserId();
    }

    public VideoTrackManager getThis() {
        return this;
    }

    @Override
    public void onMediaAvailable() {
        mediaAvailable = true;
        if (listener != null) {
            listener.onMediaAvailable();
        }
        if (Utils.contains(StringeeManager.getInstance().getRoomEvents(), "trackReadyToPlay") && !forCall) {
            Utils.sendEvent(clientWrapper.getContext(), "trackReadyToPlay", Utils.getVideoTrackMap(getThis(), clientWrapper.getClientId()));
        }
    }

    @Override
    public void onMediaStateChange(StringeeVideoTrack.MediaState mediaState) {

    }
}
