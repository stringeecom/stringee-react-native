package com.stringeereactnative.conference;

import android.app.Activity;
import android.content.Intent;
import android.os.Build.VERSION;
import android.os.Build.VERSION_CODES;
import android.os.Handler;
import android.os.Looper;

import com.facebook.react.bridge.Arguments;
import com.facebook.react.bridge.BaseActivityEventListener;
import com.facebook.react.bridge.Callback;
import com.facebook.react.bridge.ReactApplicationContext;
import com.facebook.react.bridge.WritableArray;
import com.facebook.react.bridge.WritableMap;
import com.stringee.common.StringeeAudioManager;
import com.stringee.common.StringeeAudioManager.AudioDevice;
import com.stringee.common.StringeeAudioManager.AudioManagerEvents;
import com.stringee.exception.StringeeError;
import com.stringee.listener.StatusListener;
import com.stringee.listener.StringeeRoomListener;
import com.stringee.messaging.listeners.CallbackListener;
import com.stringee.video.RemoteParticipant;
import com.stringee.video.StringeeRoom;
import com.stringee.video.StringeeVideo;
import com.stringee.video.StringeeVideoTrack;
import com.stringee.video.StringeeVideoTrack.Options;
import com.stringeereactnative.ClientWrapper;
import com.stringeereactnative.common.StringeeManager;
import com.stringeereactnative.common.Utils;

import org.json.JSONObject;

import java.util.List;
import java.util.Random;
import java.util.Set;

public class ConferenceWrapper implements StringeeRoomListener {
    private ClientWrapper clientWrapper;
    private StringeeManager stringeeManager;
    private ReactApplicationContext context;
    private Callback joinRoomCallBack;

    public ConferenceWrapper(ClientWrapper clientWrapper) {
        this.clientWrapper = clientWrapper;
        this.context = clientWrapper.getContext();
        this.stringeeManager = StringeeManager.getInstance();
    }

    public void joinRoom(String roomToken, Callback callback) {
        this.joinRoomCallBack = callback;
        StringeeVideo.connect(clientWrapper.getClient(), roomToken, this);
    }

    public void createLocalVideoTrack(Options options, Callback callback) {
        String localId = Utils.createLocalId();
        StringeeVideoTrack videoTrack = StringeeVideo.createLocalVideoTrack(context, options);
        VideoTrackManager trackManager = new VideoTrackManager(clientWrapper, videoTrack, localId, false);
        stringeeManager.getTrackMap().put(localId, trackManager);
        callback.invoke(true, 0, "success", Utils.getVideoTrackMap(trackManager, clientWrapper.getClientId()));
    }

    public void createCaptureScreenTrack(Callback callback) {
        if (VERSION.SDK_INT >= VERSION_CODES.LOLLIPOP) {
            String localId = Utils.createLocalId();
            final int REQUEST_CODE = new Random().nextInt(65536);

            stringeeManager.getCaptureManager().getActivityResult(new BaseActivityEventListener() {
                @Override
                public void onActivityResult(Activity activity, int requestCode, int resultCode, Intent data) {
                    if (requestCode == REQUEST_CODE && resultCode == Activity.RESULT_OK) {
                        stringeeManager.getCaptureManager().getScreenCapture().createCapture(data);
                    }
                }
            });

            stringeeManager.getCaptureManager().getScreenCapture().startCapture(REQUEST_CODE, new CallbackListener<StringeeVideoTrack>() {
                @Override
                public void onSuccess(StringeeVideoTrack videoTrack) {
                    VideoTrackManager trackManager = new VideoTrackManager(clientWrapper, videoTrack, localId, false);
                    stringeeManager.getTrackMap().put(localId, trackManager);
                    callback.invoke(true, 0, "success", Utils.getVideoTrackMap(trackManager, clientWrapper.getClientId()));
                }

                @Override
                public void onError(StringeeError stringeeError) {
                    super.onError(stringeeError);
                    callback.invoke(false, stringeeError.getCode(), stringeeError.getMessage());
                }
            });
        } else {
            callback.invoke(false, -5, "This feature requires android api level >= 21");
        }
    }

    public void publish(StringeeRoom room, VideoTrackManager trackManager, String localId, Callback callback) {
        room.publish(trackManager.getVideoTrack(), new StatusListener() {
            @Override
            public void onSuccess() {
                stringeeManager.getTrackMap().put(localId, trackManager);
                callback.invoke(true, 0, "success", Utils.getVideoTrackMap(trackManager, clientWrapper.getClientId()));
            }

            @Override
            public void onError(StringeeError stringeeError) {
                super.onError(stringeeError);
                callback.invoke(false, stringeeError.getCode(), stringeeError.getMessage());
            }
        });
    }

    public void unpublish(StringeeRoom room, VideoTrackManager trackManager, Callback callback) {
        room.unpublish(trackManager.getVideoTrack(), new StatusListener() {
            @Override
            public void onSuccess() {
                trackManager.getVideoTrack().release();
                callback.invoke(true, 0, "success");
            }

            @Override
            public void onError(StringeeError stringeeError) {
                super.onError(stringeeError);
                callback.invoke(false, stringeeError.getCode(), stringeeError.getMessage());
            }
        });
    }

    public void subscribe(StringeeRoom room, VideoTrackManager trackManager, Options options, Callback callback) {
        room.subscribe(trackManager.getVideoTrack(), options, new StatusListener() {
            @Override
            public void onSuccess() {

            }

            @Override
            public void onError(StringeeError stringeeError) {
                super.onError(stringeeError);
                callback.invoke(false, stringeeError.getCode(), stringeeError.getMessage());
            }
        });
    }

    public void unsubscribe(StringeeRoom room, VideoTrackManager trackManager, Callback callback) {
        room.unsubscribe(trackManager.getVideoTrack(), new StatusListener() {
            @Override
            public void onSuccess() {
                callback.invoke(true, 0, "success");
            }

            @Override
            public void onError(StringeeError stringeeError) {
                super.onError(stringeeError);
                callback.invoke(false, stringeeError.getCode(), stringeeError.getMessage());
            }
        });
    }

    public void leave(StringeeRoom room, boolean allClient, Callback callback) {
        room.leave(allClient, new StatusListener() {
            @Override
            public void onSuccess() {
                StringeeVideo.release(room);
                Handler handler = new Handler(Looper.getMainLooper());
                handler.post(new Runnable() {
                    @Override
                    public void run() {
                        StringeeAudioManager audioManager = stringeeManager.getAudioManager();
                        if (audioManager != null) {
                            audioManager.stop();
                            audioManager = null;
                        }
                    }
                });
                callback.invoke(true, 0, "success");
            }

            @Override
            public void onError(StringeeError stringeeError) {
                super.onError(stringeeError);
                callback.invoke(false, stringeeError.getCode(), stringeeError.getMessage());
            }
        });
    }

    public void sendMessage(StringeeRoom room, JSONObject msg, Callback callback) {
        room.sendMessage(msg, new StatusListener() {
            @Override
            public void onSuccess() {
                callback.invoke(true, 0, "success");
            }

            @Override
            public void onError(StringeeError stringeeError) {
                super.onError(stringeeError);
                callback.invoke(false, stringeeError.getCode(), stringeeError.getMessage());
            }
        });
    }

    public void mute(VideoTrackManager trackManager, boolean mute, Callback callback) {
        trackManager.getVideoTrack().mute(mute);
        callback.invoke(true, 0, "success");
    }

    public void enableVideo(VideoTrackManager trackManager, boolean enable, Callback callback) {
        trackManager.getVideoTrack().enableVideo(enable);
        callback.invoke(true, 0, "success");
    }

    public void switchCamera(VideoTrackManager trackManager, Callback callback) {
        trackManager.getVideoTrack().switchCamera(new StatusListener() {
            @Override
            public void onSuccess() {
                callback.invoke(true, 0, "success");
            }

            @Override
            public void onError(StringeeError stringeeError) {
                super.onError(stringeeError);
                callback.invoke(false, stringeeError.getCode(), stringeeError.getMessage());
            }
        });
    }

    public void switchCameraWithId(VideoTrackManager trackManager, int cameraId, Callback callback) {
        trackManager.getVideoTrack().switchCamera(new StatusListener() {
            @Override
            public void onSuccess() {
                callback.invoke(true, 0, "success");
            }

            @Override
            public void onError(StringeeError stringeeError) {
                super.onError(stringeeError);
                callback.invoke(false, stringeeError.getCode(), stringeeError.getMessage());
            }
        }, cameraId);
    }

    @Override
    public void onConnected(StringeeRoom stringeeRoom) {
        stringeeManager.getRoomMap().put(stringeeRoom.getId(), stringeeRoom);
        WritableMap dataMap = Arguments.createMap();
        dataMap.putMap("room", Utils.getRoomMap(clientWrapper.getClientId(), stringeeRoom));

        WritableArray videoTracksArray = Arguments.createArray();
        WritableArray usersArray = Arguments.createArray();
        List<RemoteParticipant> participantList = stringeeRoom.getRemoteParticipants();
        for (int i = 0; i < participantList.size(); i++) {
            RemoteParticipant participant = participantList.get(i);
            usersArray.pushMap(Utils.getRoomUserMap(participant));
            for (int j = 0; j < participant.getVideoTracks().size(); j++) {
                VideoTrackManager trackManager = new VideoTrackManager(clientWrapper, participant.getVideoTracks().get(j), "", false);
                videoTracksArray.pushMap(Utils.getVideoTrackInfoMap(trackManager, clientWrapper.getClientId()));
                stringeeManager.getTrackMap().put(trackManager.getVideoTrack().getId(), trackManager);
            }
        }

        dataMap.putArray("videoTrackInfos", videoTracksArray);
        dataMap.putArray("users", usersArray);
        joinRoomCallBack.invoke(true, 0, "Success", dataMap);

        Handler handler = new Handler(Looper.getMainLooper());
        handler.post(new Runnable() {
            @Override
            public void run() {
                StringeeAudioManager audioManager = StringeeAudioManager.create(context);
                audioManager.start(new AudioManagerEvents() {
                    @Override
                    public void onAudioDeviceChanged(AudioDevice audioDevice, Set<AudioDevice> set) {
                        switch (audioDevice) {
                            case BLUETOOTH:
                            case WIRED_HEADSET:
                                audioManager.setSpeakerphoneOn(false);
                                break;
                            default:
                                audioManager.setSpeakerphoneOn(true);
                                break;
                        }
                    }
                });
                stringeeManager.setAudioManager(audioManager);
            }
        });
    }

    @Override
    public void onDisconnected(StringeeRoom stringeeRoom) {

    }

    @Override
    public void onError(StringeeRoom stringeeRoom, StringeeError stringeeError) {
        joinRoomCallBack.invoke(false, stringeeError.getCode(), stringeeError.getMessage());
    }

    @Override
    public void onParticipantConnected(StringeeRoom stringeeRoom, RemoteParticipant remoteParticipant) {
        if (Utils.contains(stringeeManager.getRoomEvents(), "didJoinRoom")) {
            Utils.sendEvent(context, "didJoinRoom", Utils.getRoomUserMap(remoteParticipant));
        }
    }

    @Override
    public void onParticipantDisconnected(StringeeRoom stringeeRoom, RemoteParticipant remoteParticipant) {
        if (Utils.contains(stringeeManager.getRoomEvents(), "didLeaveRoom")) {
            Utils.sendEvent(context, "didLeaveRoom", Utils.getRoomUserMap(remoteParticipant));
        }
    }

    @Override
    public void onVideoTrackAdded(StringeeRoom stringeeRoom, StringeeVideoTrack stringeeVideoTrack) {
        if (Utils.contains(stringeeManager.getRoomEvents(), "didAddVideoTrack")) {
            VideoTrackManager trackManager = new VideoTrackManager(clientWrapper, stringeeVideoTrack, "", false);
            stringeeManager.getTrackMap().put(stringeeVideoTrack.getId(), trackManager);
            Utils.sendEvent(context, "didAddVideoTrack", Utils.getVideoTrackInfoMap(trackManager, clientWrapper.getClientId()));
        }
    }

    @Override
    public void onVideoTrackRemoved(StringeeRoom stringeeRoom, StringeeVideoTrack stringeeVideoTrack) {
        if (Utils.contains(stringeeManager.getRoomEvents(), "didRemoveVideoTrack")) {
            if (!stringeeVideoTrack.isLocal()) {
                Utils.sendEvent(context, "didRemoveVideoTrack", Utils.getVideoTrackInfoMap(stringeeManager.getTrackMap().get(stringeeVideoTrack.getId()), clientWrapper.getClientId()));
            }
        }
    }

    @Override
    public void onMessage(StringeeRoom stringeeRoom, JSONObject jsonObject, RemoteParticipant remoteParticipant) {
        if (Utils.contains(stringeeManager.getRoomEvents(), "didReceiveRoomMessage")) {
            WritableMap params = Arguments.createMap();
            params.putString("msg", jsonObject.toString());
            params.putMap("from", Utils.getRoomUserMap(remoteParticipant));
            Utils.sendEvent(context, "didReceiveRoomMessage", params);
        }
    }

    @Override
    public void onVideoTrackNotification(RemoteParticipant remoteParticipant, StringeeVideoTrack stringeeVideoTrack) {

    }
}
