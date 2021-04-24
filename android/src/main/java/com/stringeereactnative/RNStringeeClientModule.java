package com.stringeereactnative;

import android.content.Context;
import android.os.Bundle;

import androidx.annotation.Nullable;

import com.facebook.react.bridge.Arguments;
import com.facebook.react.bridge.Callback;
import com.facebook.react.bridge.ReactApplicationContext;
import com.facebook.react.bridge.ReactContext;
import com.facebook.react.bridge.ReactContextBaseJavaModule;
import com.facebook.react.bridge.ReactMethod;
import com.facebook.react.bridge.ReadableArray;
import com.facebook.react.bridge.ReadableMap;
import com.facebook.react.bridge.WritableArray;
import com.facebook.react.bridge.WritableMap;
import com.facebook.react.modules.core.DeviceEventManagerModule;
import com.stringee.StringeeClient;
import com.stringee.call.StringeeCall;
import com.stringee.call.StringeeCall2;
import com.stringee.exception.StringeeError;
import com.stringee.listener.StatusListener;
import com.stringee.listener.StringeeConnectionListener;
import com.stringee.messaging.Conversation;
import com.stringee.messaging.ConversationOptions;
import com.stringee.messaging.Message;
import com.stringee.messaging.StringeeChange;
import com.stringee.messaging.StringeeObject;
import com.stringee.messaging.User;
import com.stringee.messaging.listeners.CallbackListener;
import com.stringee.messaging.listeners.ChangeEventListenter;

import org.json.JSONException;
import org.json.JSONArray;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

public class RNStringeeClientModule extends ReactContextBaseJavaModule {

    private StringeeManager mStringeeManager;
    private Map<String, ArrayList<String>> eventsMap = new HashMap<>();
    private Context mContext;

    public RNStringeeClientModule(ReactApplicationContext context) {
        super(context);
        mContext = context;
        mStringeeManager = StringeeManager.getInstance();
    }

    @Override
    public String getName() {
        return "RNStringeeClient";
    }

    @ReactMethod
    public void createClientWrapper(String instanceId) {
        StringeeClient mClient = mStringeeManager.getClientsMap().get(instanceId);
        if (mClient == null) {
            mClient = new StringeeClient(getReactApplicationContext());
            StringeeClient finalClient = mClient;
            mClient.setConnectionListener(new StringeeConnectionListener() {
                @Override
                public void onConnectionConnected(StringeeClient stringeeClient, boolean b) {
                    ArrayList<String> jsEvents = eventsMap.get(instanceId);
                    if (jsEvents != null && contains(jsEvents, "onConnectionConnected")) {
                        WritableMap data = Arguments.createMap();
                        data.putString("userId", stringeeClient.getUserId());
                        data.putInt("projectId", stringeeClient.getProjectId());
                        data.putBoolean("isReconnecting", b);
                        WritableMap params = Arguments.createMap();
                        params.putString("uuid", instanceId);
                        params.putMap("data", data);
                        sendEvent(getReactApplicationContext(), "onConnectionConnected", params);
                    }
                }

                @Override
                public void onConnectionDisconnected(StringeeClient stringeeClient, boolean b) {
                    ArrayList<String> jsEvents = eventsMap.get(instanceId);
                    if (jsEvents != null && contains(jsEvents, "onConnectionDisconnected")) {
                        WritableMap data = Arguments.createMap();
                        data.putString("userId", stringeeClient.getUserId());
                        data.putInt("projectId", stringeeClient.getProjectId());
                        data.putBoolean("isReconnecting", b);
                        WritableMap params = Arguments.createMap();
                        params.putString("uuid", instanceId);
                        params.putMap("data", data);
                        sendEvent(getReactApplicationContext(), "onConnectionDisconnected", params);
                    }
                }

                @Override
                public void onIncomingCall(StringeeCall stringeeCall) {
                    ArrayList<String> jsEvents = eventsMap.get(instanceId);
                    if (jsEvents != null && contains(jsEvents, "onIncomingCall")) {
                        StringeeManager.getInstance().getCallsMap().put(stringeeCall.getCallId(), stringeeCall);
                        WritableMap data = Arguments.createMap();
                        data.putString("userId", finalClient.getUserId());
                        data.putString("callId", stringeeCall.getCallId());
                        data.putString("from", stringeeCall.getFrom());
                        data.putString("to", stringeeCall.getTo());
                        data.putString("fromAlias", stringeeCall.getFromAlias());
                        data.putString("toAlias", stringeeCall.getToAlias());
                        int callType = 1;
                        if (stringeeCall.isPhoneToAppCall()) {
                            callType = 3;
                        }
                        data.putInt("callType", callType);
                        data.putBoolean("isVideoCall", stringeeCall.isVideoCall());
                        data.putString("customDataFromYourServer", stringeeCall.getCustomDataFromYourServer());

                        WritableMap params = Arguments.createMap();
                        params.putString("uuid", instanceId);
                        params.putMap("data", data);
                        sendEvent(getReactApplicationContext(), "onIncomingCall", params);
                    }
                }

                @Override
                public void onIncomingCall2(StringeeCall2 stringeeCall2) {
                    ArrayList<String> jsEvents = eventsMap.get(instanceId);
                    if (jsEvents != null && contains(jsEvents, "onIncomingCall2")) {
                        StringeeManager.getInstance().getCalls2Map().put(stringeeCall2.getCallId(), stringeeCall2);
                        WritableMap data = Arguments.createMap();
                        data.putString("userId", finalClient.getUserId());
                        data.putString("callId", stringeeCall2.getCallId());
                        data.putString("from", stringeeCall2.getFrom());
                        data.putString("to", stringeeCall2.getTo());
                        data.putString("fromAlias", stringeeCall2.getFromAlias());
                        data.putString("toAlias", stringeeCall2.getToAlias());
                        int callType = 2;
                        data.putInt("callType", callType);
                        data.putBoolean("isVideoCall", stringeeCall2.isVideoCall());
                        data.putString("customDataFromYourServer", stringeeCall2.getCustomDataFromYourServer());

                        WritableMap params = Arguments.createMap();
                        params.putString("uuid", instanceId);
                        params.putMap("data", data);
                        sendEvent(getReactApplicationContext(), "onIncomingCall2", params);
                    }
                }

                @Override
                public void onConnectionError(StringeeClient stringeeClient, StringeeError stringeeError) {
                    ArrayList<String> jsEvents = eventsMap.get(instanceId);
                    if (jsEvents != null && contains(jsEvents, "onConnectionError")) {
                        WritableMap data = Arguments.createMap();
                        data.putInt("code", stringeeError.getCode());
                        data.putString("message", stringeeError.getMessage());

                        WritableMap params = Arguments.createMap();
                        params.putString("uuid", instanceId);
                        params.putMap("data", data);
                        sendEvent(getReactApplicationContext(), "onConnectionError", params);
                    }
                }

                @Override
                public void onRequestNewToken(StringeeClient stringeeClient) {
                    ArrayList<String> jsEvents = eventsMap.get(instanceId);
                    if (jsEvents != null && contains(jsEvents, "onRequestNewToken")) {
                        sendEvent(getReactApplicationContext(), "onRequestNewToken", null);
                    }
                }

                @Override
                public void onCustomMessage(String s, JSONObject jsonObject) {
                    ArrayList<String> jsEvents = eventsMap.get(instanceId);
                    if (jsEvents != null && contains(jsEvents, "onCustomMessage")) {
                        WritableMap data = Arguments.createMap();
                        data.putString("from", s);
                        data.putString("data", jsonObject.toString());

                        WritableMap params = Arguments.createMap();
                        params.putString("uuid", instanceId);
                        params.putMap("data", data);
                        sendEvent(getReactApplicationContext(), "onCustomMessage", params);
                    }
                }

                @Override
                public void onTopicMessage(String s, JSONObject jsonObject) {
                    ArrayList<String> jsEvents = eventsMap.get(instanceId);
                    if (jsEvents != null && contains(jsEvents, "onTopicMessage")) {
                        WritableMap data = Arguments.createMap();
                        data.putString("from", s);
                        data.putString("data", jsonObject.toString());

                        WritableMap params = Arguments.createMap();
                        params.putString("uuid", instanceId);
                        params.putMap("data", data);
                        sendEvent(getReactApplicationContext(), "onTopicMessage", params);
                    }
                }
            });
            mClient.setChangeEventListenter(new ChangeEventListenter() {
                @Override
                public void onChangeEvent(StringeeChange stringeeChange) {
                    ArrayList<String> jsEvents = eventsMap.get(instanceId);
                    if (jsEvents != null && contains(jsEvents, "onChangeEvent")) {
                        WritableMap data = Arguments.createMap();
                        StringeeObject.Type objectType = stringeeChange.getObjectType();
                        data.putInt("objectType", objectType.getValue());
                        data.putInt("changeType", stringeeChange.getChangeType().getValue());
                        WritableArray objects = Arguments.createArray();
                        WritableMap object = Arguments.createMap();
                        if (objectType == StringeeObject.Type.CONVERSATION) {
                            Conversation conversation = (Conversation) stringeeChange.getObject();
                            object.putString("id", conversation.getId());
                            object.putString("localId", conversation.getLocalId());
                            object.putString("name", conversation.getName());
                            object.putBoolean("isGroup", conversation.isGroup());
                            object.putDouble("updatedAt", conversation.getUpdateAt());
                            object.putString("lastMsgSender", conversation.getLastMsgSender());
                            object.putInt("lastMsgType", conversation.getLastMsgType());
                            object.putInt("unreadCount", conversation.getTotalUnread());
                            object.putString("lastMsgId", conversation.getLastMsgId());
                            object.putString("creator", conversation.getCreator());
                            object.putDouble("created", conversation.getCreateAt());
                            object.putDouble("lastMsgSeq", conversation.getLastMsgSeqReceived());
                            object.putDouble("lastMsgCreatedAt", conversation.getLastTimeNewMsg());
                            object.putInt("lastMsgState", conversation.getLastMsgState());
                            if (conversation.getLastMsg() != null) {
                                try {
                                    Bundle bundle = jsonToBundle(conversation.getLastMsg());
                                    WritableMap lastMsgMap = Arguments.fromBundle(bundle);
                                    object.putMap("text", lastMsgMap);
                                } catch (JSONException e) {
                                    e.printStackTrace();
                                }
                            }

                            List<User> participants = conversation.getParticipants();
                            WritableArray participantsMap = Arguments.createArray();
                            for (int i = 0; i < participants.size(); i++) {
                                User user = participants.get(i);
                                WritableMap userMap = Arguments.createMap();
                                userMap.putString("userId", user.getUserId());
                                userMap.putString("name", user.getName());
                                userMap.putString("avatar", user.getAvatarUrl());
                                participantsMap.pushMap(userMap);
                            }
                            object.putArray("participants", participantsMap);
                        } else if (objectType == StringeeObject.Type.MESSAGE) {
                            Message message = (Message) stringeeChange.getObject();
                            object.putString("id", message.getId());
                            object.putString("localId", message.getLocalId());
                            object.putString("conversationId", message.getConversationId());
                            object.putDouble("createdAt", message.getCreatedAt());
                            object.putInt("state", message.getState().getValue());
                            object.putDouble("sequence", message.getSequence());
                            object.putInt("type", message.getType());
                            WritableMap contentMap = Arguments.createMap();
                            switch (message.getType()) {
                                case 1:
                                    contentMap.putString("content", message.getText());
                                    break;
                                case 2:
                                    WritableMap photoMap = Arguments.createMap();
                                    photoMap.putString("filePath", message.getFileUrl());
                                    photoMap.putString("thumbnail", message.getThumbnailUrl());
                                    photoMap.putDouble("ratio", message.getImageRatio());
                                    contentMap.putMap("photo", photoMap);
                                    break;
                                case 3:
                                    WritableMap videoMap = Arguments.createMap();
                                    videoMap.putString("filePath", message.getFileUrl());
                                    videoMap.putString("thumbnail", message.getThumbnailUrl());
                                    videoMap.putDouble("ratio", message.getImageRatio());
                                    videoMap.putInt("duration", message.getDuration());
                                    contentMap.putMap("video", videoMap);
                                    break;
                                case 4:
                                    WritableMap audioMap = Arguments.createMap();
                                    audioMap.putString("filePath", message.getFileUrl());
                                    audioMap.putInt("duration", message.getDuration());
                                    contentMap.putMap("audio", audioMap);
                                    break;
                                case 5:
                                    WritableMap fileMap = Arguments.createMap();
                                    fileMap.putString("filePath", message.getFileUrl());
                                    fileMap.putString("filename", message.getFileName());
                                    fileMap.putDouble("length", message.getFileLength());
                                    contentMap.putMap("file", fileMap);
                                    break;
                                case 7:
                                    try {
                                        contentMap = Arguments.fromBundle(jsonToBundle(message.getText()));
                                    } catch (JSONException e) {
                                        e.printStackTrace();
                                    }
                                    break;
                                case 9:
                                    WritableMap locationMap = Arguments.createMap();
                                    locationMap.putDouble("lat", message.getLatitude());
                                    locationMap.putDouble("lon", message.getLongitude());
                                    contentMap.putMap("location", locationMap);
                                    break;
                                case 10:
                                    WritableMap contactMap = Arguments.createMap();
                                    contactMap.putString("vcard", message.getContact());
                                    contentMap.putMap("contact", contactMap);
                                    break;
                                case 11:
                                    WritableMap stickerMap = Arguments.createMap();
                                    stickerMap.putString("name", message.getStickerName());
                                    stickerMap.putString("category", message.getStickerCategory());
                                    contentMap.putMap("sticker", stickerMap);
                                    break;
                                case 100:
                                    try {
                                        contentMap = Arguments.fromBundle(jsonToBundle(message.getText()));
                                    } catch (JSONException e) {
                                        e.printStackTrace();
                                    }
                                    break;
                            }
                            object.putMap("content", contentMap);
                            String senderId = message.getSenderId();
                            User user = finalClient.getUser(senderId);
                            String name = "";
                            if (user != null) {
                                name = user.getName();
                                if (name == null || name.length() == 0) {
                                    name = user.getUserId();
                                }
                            }
                            object.putString("sender", name);
                        }
                        objects.pushMap(object);
                        data.putArray("objects", objects);

                        WritableMap params = Arguments.createMap();
                        params.putString("uuid", instanceId);
                        params.putMap("data", data);
                        sendEvent(getReactApplicationContext(), "onChangeEvent", params);
                    }
                }
            });
            mStringeeManager.getClientsMap().put(instanceId, mClient);
        }
    }

    @ReactMethod
    public void connect(String instanceId, String accessToken) {
        StringeeClient mClient = mStringeeManager.getClientsMap().get(instanceId);
        if (mClient.isConnected()) {
            ArrayList<String> jsEvents = eventsMap.get(instanceId);
            if (jsEvents != null && contains(jsEvents, "onConnectionConnected")) {
                WritableMap data = Arguments.createMap();
                data.putString("userId", mClient.getUserId());
                data.putInt("projectId", mClient.getProjectId());
                data.putBoolean("isReconnecting", false);

                WritableMap params = Arguments.createMap();
                params.putString("uuid", instanceId);
                params.putMap("data", data);
                sendEvent(getReactApplicationContext(), "onConnectionConnected", params);
            }
        } else {
            mClient.connect(accessToken);
        }
    }

    @ReactMethod
    public void disconnect(String instanceId) {
        StringeeClient mClient = mStringeeManager.getClientsMap().get(instanceId);
        if (mClient != null) {
            mClient.disconnect();
        }
    }

    @ReactMethod
    public void registerPushToken(String instanceId, String token, final Callback callback) {
        StringeeClient mClient = mStringeeManager.getClientsMap().get(instanceId);
        if (mClient == null) {
            callback.invoke(false, -1, "StringeeClient is not initialized or connected");
            return;
        }

        mClient.registerPushToken(token, new StatusListener() {
            @Override
            public void onSuccess() {
                callback.invoke(true, 0, "Success");
            }

            @Override
            public void onError(StringeeError error) {
                callback.invoke(false, error.getCode(), error.getMessage());
            }
        });
    }

    @ReactMethod
    public void unregisterPushToken(String instanceId, final String token, final Callback callback) {
        StringeeClient mClient = mStringeeManager.getClientsMap().get(instanceId);
        if (mClient == null) {
            callback.invoke(false, -1, "StringeeClient is not initialized or connected");
            return;
        }

        mClient.unregisterPushToken(token, new StatusListener() {
            @Override
            public void onSuccess() {
                callback.invoke(true, 0, "Success");
            }

            @Override
            public void onError(StringeeError error) {
                callback.invoke(false, error.getCode(), error.getMessage());
            }
        });
    }

    @ReactMethod
    public void sendCustomMessage(String instanceId, String toUser, String msg, final Callback callback) {
        StringeeClient mClient = mStringeeManager.getClientsMap().get(instanceId);
        if (mClient == null) {
            callback.invoke(false, -1, "StringeeClient is not initialized or connected");
            return;
        }

        try {
            JSONObject jsonObject = new JSONObject(msg);
            mClient.sendCustomMessage(toUser, jsonObject, new StatusListener() {
                @Override
                public void onSuccess() {
                    callback.invoke(true, 0, "Success");
                }

                @Override
                public void onError(StringeeError error) {
                    callback.invoke(false, error.getCode(), error.getMessage());
                }
            });
        } catch (JSONException e) {
            e.printStackTrace();
            callback.invoke(false, -2, "Message is not not in JSON format");
        }
    }

    @ReactMethod
    public void createConversation(String instanceId, ReadableArray usersArray, ReadableMap optionsMap, final Callback callback) {
        StringeeClient mClient = StringeeManager.getInstance().getClientsMap().get(instanceId);
        if (mClient == null) {
            callback.invoke(false, -1, "StringeeClient is not initialized");
            return;
        }

        List<User> participants = new ArrayList<>();
        for (int i = 0; i < usersArray.size(); i++) {
            User user = new User(usersArray.getString(i));
            participants.add(user);
        }

        ConversationOptions convOptions = null;
        if (optionsMap != null) {
            convOptions = new ConversationOptions();
            if (optionsMap.hasKey("name")) {
                convOptions.setName(optionsMap.getString("name"));
            }
            if (optionsMap.hasKey("isGroup")) {
                convOptions.setGroup(optionsMap.getBoolean("isGroup"));
            }
            if (optionsMap.hasKey("isDistinct")) {
                convOptions.setDistinct(optionsMap.getBoolean("isDistinct"));
            }
        }

        mClient.createConversation(participants, convOptions, new CallbackListener<Conversation>() {
            @Override
            public void onSuccess(Conversation conversation) {
                WritableMap params = Arguments.createMap();
                params.putString("id", conversation.getId());
                params.putString("localId", conversation.getLocalId());
                params.putString("name", conversation.getName());
                params.putBoolean("isDistinct", conversation.isDistinct());
                params.putBoolean("isGroup", conversation.isGroup());
                params.putDouble("updatedAt", conversation.getUpdateAt());
                params.putString("lastMsgSender", conversation.getLastMsgSender());
                params.putInt("lastMsgType", conversation.getLastMsgType());
                params.putInt("unreadCount", conversation.getTotalUnread());
                params.putString("lastMsgId", conversation.getLastMsgId());
                params.putString("creator", conversation.getCreator());
                params.putDouble("created", conversation.getCreateAt());
                params.putDouble("lastMsgSeq", conversation.getLastMsgSeqReceived());
                params.putDouble("lastMsgCreatedAt", conversation.getLastTimeNewMsg());
                params.putInt("lastMsgState", conversation.getLastMsgState());
                if (conversation.getLastMsg() != null) {
                    try {
                        Bundle bundle = jsonToBundle(conversation.getLastMsg());
                        WritableMap lastMsgMap = Arguments.fromBundle(bundle);
                        params.putMap("text", lastMsgMap);
                    } catch (JSONException e) {
                        e.printStackTrace();
                    }
                }
                List<User> participants = conversation.getParticipants();
                WritableArray participantsMap = Arguments.createArray();
                for (int i = 0; i < participants.size(); i++) {
                    User user = participants.get(i);
                    WritableMap userMap = Arguments.createMap();
                    userMap.putString("userId", user.getUserId());
                    userMap.putString("name", user.getName());
                    userMap.putString("avatar", user.getAvatarUrl());
                    participantsMap.pushMap(userMap);
                }
                params.putArray("participants", participantsMap);
                callback.invoke(true, 0, "Success", params);
            }

            @Override
            public void onError(StringeeError error) {
                callback.invoke(false, error.getCode(), error.getMessage());
            }
        });
    }

    @ReactMethod
    public void getConversationById(String instanceId, String id, final Callback callback) {
        StringeeClient mClient = StringeeManager.getInstance().getClientsMap().get(instanceId);
        if (mClient == null) {
            callback.invoke(false, -1, "StringeeClient is not initialized");
            return;
        }

        if (id == null) {
            callback.invoke(false, -2, "Conversation id can not be null");
            return;
        }

        mClient.getConversation(id, new CallbackListener<Conversation>() {
            @Override
            public void onSuccess(Conversation conversation) {
                WritableMap params = Arguments.createMap();
                params.putString("id", conversation.getId());
                params.putString("localId", conversation.getLocalId());
                params.putString("name", conversation.getName());
                params.putBoolean("isDistinct", conversation.isDistinct());
                params.putBoolean("isGroup", conversation.isGroup());
                params.putDouble("updatedAt", conversation.getUpdateAt());
                params.putString("lastMsgSender", conversation.getLastMsgSender());
                params.putString("text", conversation.getText());
                params.putInt("lastMsgType", conversation.getLastMsgType());
                params.putInt("unreadCount", conversation.getTotalUnread());
                params.putString("lastMsgId", conversation.getLastMsgId());
                params.putString("creator", conversation.getCreator());
                params.putDouble("created", conversation.getCreateAt());
                params.putDouble("lastMsgSeq", conversation.getLastMsgSeqReceived());
                params.putDouble("lastMsgCreatedAt", conversation.getLastTimeNewMsg());
                params.putInt("lastMsgState", conversation.getLastMsgState());
                if (conversation.getLastMsg() != null) {
                    try {
                        Bundle bundle = jsonToBundle(conversation.getLastMsg());
                        WritableMap lastMsgMap = Arguments.fromBundle(bundle);
                        params.putMap("text", lastMsgMap);
                    } catch (JSONException e) {
                        e.printStackTrace();
                    }
                }
                List<User> participants = conversation.getParticipants();
                WritableArray participantsMap = Arguments.createArray();
                for (int i = 0; i < participants.size(); i++) {
                    User user = participants.get(i);
                    WritableMap userMap = Arguments.createMap();
                    userMap.putString("userId", user.getUserId());
                    userMap.putString("name", user.getName());
                    userMap.putString("avatar", user.getAvatarUrl());
                    participantsMap.pushMap(userMap);
                }
                params.putArray("participants", participantsMap);
                callback.invoke(true, 0, "Success", params);
            }

            @Override
            public void onError(StringeeError error) {
                callback.invoke(false, error.getCode(), error.getMessage());
            }
        });
    }

    @ReactMethod
    public void getLocalConversations(String instanceId, String userId, final Callback callback) {
        StringeeClient mClient = StringeeManager.getInstance().getClientsMap().get(instanceId);
        if (mClient == null) {
            callback.invoke(false, -1, "StringeeClient is not initialized");
            return;
        }

        if (userId == null) {
            callback.invoke(false, -2, "User id can not be null");
            return;
        }

        mClient.getLocalConversations(userId, new CallbackListener<List<Conversation>>() {
            @Override
            public void onSuccess(List<Conversation> conversations) {
                WritableArray params = Arguments.createArray();
                for (int i = 0; i < conversations.size(); i++) {
                    Conversation conversation = conversations.get(i);
                    WritableMap param = Arguments.createMap();
                    param.putString("id", conversation.getId());
                    param.putString("localId", conversation.getLocalId());
                    param.putString("name", conversation.getName());
                    param.putBoolean("isDistinct", conversation.isDistinct());
                    param.putBoolean("isGroup", conversation.isGroup());
                    param.putDouble("updatedAt", conversation.getUpdateAt());
                    param.putString("lastMsgSender", conversation.getLastMsgSender());
                    param.putString("text", conversation.getText());
                    param.putInt("lastMsgType", conversation.getLastMsgType());
                    param.putInt("unreadCount", conversation.getTotalUnread());
                    param.putString("lastMsgId", conversation.getLastMsgId());
                    param.putString("creator", conversation.getCreator());
                    param.putDouble("created", conversation.getCreateAt());
                    param.putDouble("lastMsgSeq", conversation.getLastMsgSeqReceived());
                    param.putDouble("lastMsgCreatedAt", conversation.getLastTimeNewMsg());
                    param.putInt("lastMsgState", conversation.getLastMsgState());
                    if (conversation.getLastMsg() != null) {
                        try {
                            Bundle bundle = jsonToBundle(conversation.getLastMsg());
                            WritableMap lastMsgMap = Arguments.fromBundle(bundle);
                            param.putMap("text", lastMsgMap);
                        } catch (JSONException e) {
                            e.printStackTrace();
                        }
                    }
                    List<User> participants = conversation.getParticipants();
                    WritableArray participantsMap = Arguments.createArray();
                    for (int j = 0; j < participants.size(); j++) {
                        User user = participants.get(j);
                        WritableMap userMap = Arguments.createMap();
                        userMap.putString("userId", user.getUserId());
                        userMap.putString("name", user.getName());
                        userMap.putString("avatar", user.getAvatarUrl());
                        participantsMap.pushMap(userMap);
                    }
                    param.putArray("participants", participantsMap);

                    params.pushMap(param);
                }
                callback.invoke(true, 0, "Success", params);
            }

            @Override
            public void onError(StringeeError error) {
                callback.invoke(false, error.getCode(), error.getMessage());
            }
        });
    }

    @ReactMethod
    public void getLastConversations(String instanceId, int count, final Callback callback) {
        StringeeClient mClient = StringeeManager.getInstance().getClientsMap().get(instanceId);
        if (mClient == null) {
            callback.invoke(false, -1, "StringeeClient is not initialized");
            return;
        }

        mClient.getLastConversations(count, new CallbackListener<List<Conversation>>() {
            @Override
            public void onSuccess(List<Conversation> conversations) {
                WritableArray params = Arguments.createArray();
                for (int i = 0; i < conversations.size(); i++) {
                    Conversation conversation = conversations.get(i);
                    WritableMap param = Arguments.createMap();
                    param.putString("id", conversation.getId());
                    param.putString("localId", conversation.getLocalId());
                    param.putString("name", conversation.getName());
                    param.putBoolean("isDistinct", conversation.isDistinct());
                    param.putBoolean("isGroup", conversation.isGroup());
                    param.putDouble("updatedAt", conversation.getUpdateAt());
                    param.putString("lastMsgSender", conversation.getLastMsgSender());
                    param.putString("text", conversation.getText());
                    param.putInt("lastMsgType", conversation.getLastMsgType());
                    param.putInt("unreadCount", conversation.getTotalUnread());
                    param.putString("lastMsgId", conversation.getLastMsgId());
                    param.putString("creator", conversation.getCreator());
                    param.putDouble("created", conversation.getCreateAt());
                    param.putDouble("lastMsgSeq", conversation.getLastMsgSeqReceived());
                    param.putDouble("lastMsgCreatedAt", conversation.getLastTimeNewMsg());
                    param.putInt("lastMsgState", conversation.getLastMsgState());
                    if (conversation.getLastMsg() != null) {
                        try {
                            Bundle bundle = jsonToBundle(conversation.getLastMsg());
                            WritableMap lastMsgMap = Arguments.fromBundle(bundle);
                            param.putMap("text", lastMsgMap);
                        } catch (JSONException e) {
                            e.printStackTrace();
                        }
                    }
                    List<User> participants = conversation.getParticipants();
                    WritableArray participantsMap = Arguments.createArray();
                    for (int j = 0; j < participants.size(); j++) {
                        User user = participants.get(j);
                        WritableMap userMap = Arguments.createMap();
                        userMap.putString("userId", user.getUserId());
                        userMap.putString("name", user.getName());
                        userMap.putString("avatar", user.getAvatarUrl());
                        participantsMap.pushMap(userMap);
                    }
                    param.putArray("participants", participantsMap);

                    params.pushMap(param);
                }
                callback.invoke(true, 0, "Success", params);
            }

            @Override
            public void onError(StringeeError error) {
                callback.invoke(false, error.getCode(), error.getMessage());
            }
        });
    }

    @ReactMethod
    public void getConversationsBefore(String instanceId, double datetime, int count, final Callback callback) {
        StringeeClient mClient = StringeeManager.getInstance().getClientsMap().get(instanceId);
        if (mClient == null) {
            callback.invoke(false, -1, "StringeeClient is not initialized or connected");
            return;
        }

        mClient.getConversationsBefore((long) datetime, count, new CallbackListener<List<Conversation>>() {
            @Override
            public void onSuccess(List<Conversation> conversations) {
                WritableArray params = Arguments.createArray();
                for (int i = 0; i < conversations.size(); i++) {
                    Conversation conversation = conversations.get(i);
                    WritableMap param = Arguments.createMap();
                    param.putString("id", conversation.getId());
                    param.putString("localId", conversation.getLocalId());
                    param.putString("name", conversation.getName());
                    param.putBoolean("isDistinct", conversation.isDistinct());
                    param.putBoolean("isGroup", conversation.isGroup());
                    param.putDouble("updatedAt", conversation.getUpdateAt());
                    param.putString("lastMsgSender", conversation.getLastMsgSender());
                    param.putString("text", conversation.getText());
                    param.putInt("lastMsgType", conversation.getLastMsgType());
                    param.putInt("unreadCount", conversation.getTotalUnread());
                    param.putString("lastMsgId", conversation.getLastMsgId());
                    param.putString("creator", conversation.getCreator());
                    param.putDouble("created", conversation.getCreateAt());
                    param.putDouble("lastMsgSeq", conversation.getLastMsgSeqReceived());
                    param.putDouble("lastMsgCreatedAt", conversation.getLastTimeNewMsg());
                    param.putInt("lastMsgState", conversation.getLastMsgState());
                    if (conversation.getLastMsg() != null) {
                        try {
                            Bundle bundle = jsonToBundle(conversation.getLastMsg());
                            WritableMap lastMsgMap = Arguments.fromBundle(bundle);
                            param.putMap("text", lastMsgMap);
                        } catch (JSONException e) {
                            e.printStackTrace();
                        }
                    }
                    List<User> participants = conversation.getParticipants();
                    WritableArray participantsMap = Arguments.createArray();
                    for (int j = 0; j < participants.size(); j++) {
                        User user = participants.get(j);
                        WritableMap userMap = Arguments.createMap();
                        userMap.putString("userId", user.getUserId());
                        userMap.putString("name", user.getName());
                        userMap.putString("avatar", user.getAvatarUrl());
                        participantsMap.pushMap(userMap);
                    }
                    param.putArray("participants", participantsMap);

                    params.pushMap(param);
                }
                callback.invoke(true, 0, "Success", params);
            }

            @Override
            public void onError(StringeeError error) {
                callback.invoke(false, error.getCode(), error.getMessage());
            }
        });
    }

    @ReactMethod
    public void getConversationsAfter(String instanceId, double datetime, int count, final Callback callback) {
        StringeeClient mClient = StringeeManager.getInstance().getClientsMap().get(instanceId);
        if (mClient == null) {
            callback.invoke(false, -1, "StringeeClient is not initialized or connected");
            return;
        }

        mClient.getConversationsAfter((long) datetime, count, new CallbackListener<List<Conversation>>() {
            @Override
            public void onSuccess(List<Conversation> conversations) {
                WritableArray params = Arguments.createArray();
                for (int i = 0; i < conversations.size(); i++) {
                    Conversation conversation = conversations.get(i);
                    WritableMap param = Arguments.createMap();
                    param.putString("id", conversation.getId());
                    param.putString("localId", conversation.getLocalId());
                    param.putString("name", conversation.getName());
                    param.putBoolean("isDistinct", conversation.isDistinct());
                    param.putBoolean("isGroup", conversation.isGroup());
                    param.putDouble("updatedAt", conversation.getUpdateAt());
                    param.putString("lastMsgSender", conversation.getLastMsgSender());
                    param.putString("text", conversation.getText());
                    param.putInt("lastMsgType", conversation.getLastMsgType());
                    param.putInt("unreadCount", conversation.getTotalUnread());
                    param.putString("lastMsgId", conversation.getLastMsgId());
                    param.putString("creator", conversation.getCreator());
                    param.putDouble("created", conversation.getCreateAt());
                    param.putDouble("lastMsgSeq", conversation.getLastMsgSeqReceived());
                    param.putDouble("lastMsgCreatedAt", conversation.getLastTimeNewMsg());
                    param.putInt("lastMsgState", conversation.getLastMsgState());
                    if (conversation.getLastMsg() != null) {
                        try {
                            Bundle bundle = jsonToBundle(conversation.getLastMsg());
                            WritableMap lastMsgMap = Arguments.fromBundle(bundle);
                            param.putMap("text", lastMsgMap);
                        } catch (JSONException e) {
                            e.printStackTrace();
                        }
                    }
                    List<User> participants = conversation.getParticipants();
                    WritableArray participantsMap = Arguments.createArray();
                    for (int j = 0; j < participants.size(); j++) {
                        User user = participants.get(j);
                        WritableMap userMap = Arguments.createMap();
                        userMap.putString("userId", user.getUserId());
                        userMap.putString("name", user.getName());
                        userMap.putString("avatar", user.getAvatarUrl());
                        participantsMap.pushMap(userMap);
                    }
                    param.putArray("participants", participantsMap);

                    params.pushMap(param);
                }
                callback.invoke(true, 0, "Success", params);
            }

            @Override
            public void onError(StringeeError error) {
                callback.invoke(false, error.getCode(), error.getMessage());
            }
        });
    }

    @ReactMethod
    public void deleteConversation(String instanceId, String convId, final Callback callback) {
        StringeeClient mClient = StringeeManager.getInstance().getClientsMap().get(instanceId);
        if (mClient == null) {
            callback.invoke(false, -1, "StringeeClient is not initialized or connected");
            return;
        }

        mClient.getConversation(convId, new CallbackListener<Conversation>() {
            @Override
            public void onSuccess(final Conversation conversation) {
                if (conversation.isGroup()) {
                    if (conversation.getState() != Conversation.STATE_LEFT) {
                        callback.invoke(false, -2, "You must leave this group before deleting");
                        return;
                    }
                }
                conversation.delete(mClient, new StatusListener() {
                    @Override
                    public void onSuccess() {
                        callback.invoke(true, 0, "Success");
                    }

                    @Override
                    public void onError(StringeeError error) {
                        callback.invoke(false, error.getCode(), error.getMessage());
                    }
                });
            }

            @Override
            public void onError(StringeeError error) {
                callback.invoke(false, error.getCode(), error.getMessage());
            }
        });
    }

    @ReactMethod
    public void addParticipants(String instanceId, String convId, final ReadableArray usersArray, final Callback callback) {
        StringeeClient mClient = StringeeManager.getInstance().getClientsMap().get(instanceId);
        if (mClient == null) {
            callback.invoke(false, -1, "StringeeClient is not initialized or connected");
            return;
        }

        mClient.getConversation(convId, new CallbackListener<Conversation>() {
            @Override
            public void onSuccess(Conversation conversation) {
                List<User> users = new ArrayList<>();
                for (int i = 0; i < usersArray.size(); i++) {
                    User user = new User(usersArray.getString(i));
                    users.add(user);
                }
                conversation.addParticipants(mClient, users, new CallbackListener<List<User>>() {
                    @Override
                    public void onSuccess(List<User> users) {
                        WritableArray params = Arguments.createArray();
                        for (int i = 0; i < users.size(); i++) {
                            User user = users.get(i);
                            WritableMap param = Arguments.createMap();
                            param.putString("userId", user.getUserId());
                            param.putString("name", user.getName());
                            param.putString("avatar", user.getAvatarUrl());
                            params.pushMap(param);
                        }

                        callback.invoke(true, 0, "Success", params);
                    }

                    @Override
                    public void onError(StringeeError error) {
                        callback.invoke(false, error.getCode(), error.getMessage());
                    }
                });
            }

            @Override
            public void onError(StringeeError error) {
                callback.invoke(false, error.getCode(), error.getMessage());
            }
        });
    }

    @ReactMethod
    public void removeParticipants(String instanceId, String convId, final ReadableArray usersArray, final Callback callback) {
        StringeeClient mClient = StringeeManager.getInstance().getClientsMap().get(instanceId);
        if (mClient == null) {
            callback.invoke(false, -1, "StringeeClient is not initialized or connected");
            return;
        }

        mClient.getConversation(convId, new CallbackListener<Conversation>() {
            @Override
            public void onSuccess(Conversation conversation) {
                List<User> users = new ArrayList<>();
                for (int i = 0; i < usersArray.size(); i++) {
                    User user = new User(usersArray.getString(i));
                    users.add(user);
                }
                conversation.removeParticipants(mClient, users, new CallbackListener<List<User>>() {
                    @Override
                    public void onSuccess(List<User> users) {
                        WritableArray params = Arguments.createArray();
                        for (int i = 0; i < users.size(); i++) {
                            User user = users.get(i);
                            WritableMap param = Arguments.createMap();
                            param.putString("userId", user.getUserId());
                            param.putString("name", user.getName());
                            param.putString("avatar", user.getAvatarUrl());
                            params.pushMap(param);
                        }

                        callback.invoke(true, 0, "Success", params);
                    }

                    @Override
                    public void onError(StringeeError error) {
                        callback.invoke(false, error.getCode(), error.getMessage());
                    }
                });
            }

            @Override
            public void onError(StringeeError error) {
                callback.invoke(false, error.getCode(), error.getMessage());
            }
        });
    }

    @ReactMethod
    public void sendMessage(String instanceId, ReadableMap messageMap, final Callback callback) {
        StringeeClient mClient = StringeeManager.getInstance().getClientsMap().get(instanceId);
        if (mClient == null) {
            callback.invoke(false, -1, "StringeeClient is not initialized or connected");
            return;
        }

        String convId = messageMap.getString("convId");
        final int type = messageMap.getInt("type");
        final ReadableMap msgMap = messageMap.getMap("message");

        mClient.getConversation(convId, new CallbackListener<Conversation>() {
            @Override
            public void onSuccess(Conversation conversation) {
                Message message = new Message(type);
                switch (type) {
                    case 1:
                        message = new Message(msgMap.getString("content"));
                        break;
                    case 2:
                        ReadableMap photoMap = msgMap.getMap("photo");
                        message.setFileUrl(photoMap.getString("filePath"));
                        message.setThumbnailUrl(photoMap.getString("thumbnail"));
                        message.setImageRatio((float) photoMap.getDouble("ratio"));
                        break;
                    case 3:
                        ReadableMap videoMap = msgMap.getMap("video");
                        message.setFileUrl(videoMap.getString("filePath"));
                        message.setThumbnailUrl(videoMap.getString("thumbnail"));
                        message.setImageRatio((float) videoMap.getDouble("ratio"));
                        message.setDuration(videoMap.getInt("duration"));
                        break;
                    case 4:
                        ReadableMap audioMap = msgMap.getMap("audio");
                        message.setFileUrl(audioMap.getString("filePath"));
                        message.setDuration(audioMap.getInt("duration"));
                        break;
                    case 5:
                        ReadableMap fileMap = msgMap.getMap("file");
                        message.setFileUrl(fileMap.getString("filePath"));
                        message.setFileName(fileMap.getString("filename"));
                        message.setFileLength(fileMap.getInt("length"));
                        break;
                    case 9:
                        ReadableMap locationMap = msgMap.getMap("location");
                        message.setLatitude(locationMap.getDouble("lat"));
                        message.setLongitude(locationMap.getDouble("lon"));
                        break;
                    case 10:
                        ReadableMap contactMap = msgMap.getMap("contact");
                        message.setContact(contactMap.getString("vcard"));
                        break;
                    case 11:
                        ReadableMap stickerMap = msgMap.getMap("sticker");
                        message.setStickerCategory(stickerMap.getString("category"));
                        message.setStickerName(stickerMap.getString("name"));
                        break;
                    default:
                        break;
                }

                conversation.sendMessage(mClient, message, new StatusListener() {
                    @Override
                    public void onSuccess() {
                        callback.invoke(true, 0, "Success");
                    }

                    @Override
                    public void onError(StringeeError error) {
                        callback.invoke(false, error.getCode(), error.getMessage());
                    }
                });
            }

            @Override
            public void onError(StringeeError error) {
                callback.invoke(false, error.getCode(), error.getMessage());
            }
        });
    }

    @ReactMethod
    public void getLocalMessages(String instanceId, String convId, final int count, final Callback callback) {
        StringeeClient mClient = StringeeManager.getInstance().getClientsMap().get(instanceId);
        if (mClient == null) {
            callback.invoke(false, -1, "StringeeClient is not initialized or connected");
            return;
        }

        mClient.getConversation(convId, new CallbackListener<Conversation>() {
            @Override
            public void onSuccess(Conversation conversation) {
                conversation.getLocalMessages(mClient, count, new CallbackListener<List<Message>>() {
                    @Override
                    public void onSuccess(List<Message> messages) {
                        WritableArray params = Arguments.createArray();
                        for (int i = 0; i < messages.size(); i++) {
                            Message message = messages.get(i);
                            WritableMap param = Arguments.createMap();
                            param.putString("id", message.getId());
                            param.putString("localId", message.getLocalId());
                            param.putString("conversationId", message.getConversationId());
                            param.putDouble("createdAt", message.getCreatedAt());
                            param.putInt("state", message.getState().getValue());
                            param.putDouble("sequence", message.getSequence());
                            param.putInt("type", message.getType());
                            WritableMap contentMap = Arguments.createMap();
                            switch (message.getType()) {
                                case 1:
                                    contentMap.putString("content", message.getText());
                                    break;
                                case 2:
                                    WritableMap photoMap = Arguments.createMap();
                                    photoMap.putString("filePath", message.getFileUrl());
                                    photoMap.putString("thumbnail", message.getThumbnailUrl());
                                    photoMap.putDouble("ratio", message.getImageRatio());
                                    contentMap.putMap("photo", photoMap);
                                    break;
                                case 3:
                                    WritableMap videoMap = Arguments.createMap();
                                    videoMap.putString("filePath", message.getFileUrl());
                                    videoMap.putString("thumbnail", message.getThumbnailUrl());
                                    videoMap.putDouble("ratio", message.getImageRatio());
                                    videoMap.putInt("duration", message.getDuration());
                                    contentMap.putMap("video", videoMap);
                                    break;
                                case 4:
                                    WritableMap audioMap = Arguments.createMap();
                                    audioMap.putString("filePath", message.getFileUrl());
                                    audioMap.putInt("duration", message.getDuration());
                                    contentMap.putMap("audio", audioMap);
                                    break;
                                case 5:
                                    WritableMap fileMap = Arguments.createMap();
                                    fileMap.putString("filePath", message.getFileUrl());
                                    fileMap.putString("filename", message.getFileName());
                                    fileMap.putDouble("length", message.getFileLength());
                                    contentMap.putMap("file", fileMap);
                                    break;
                                case 7:
                                    try {
                                        contentMap = Arguments.fromBundle(jsonToBundle(message.getText()));
                                    } catch (JSONException e) {
                                        e.printStackTrace();
                                    }
                                    break;
                                case 9:
                                    WritableMap locationMap = Arguments.createMap();
                                    locationMap.putDouble("lat", message.getLatitude());
                                    locationMap.putDouble("lon", message.getLongitude());
                                    contentMap.putMap("location", locationMap);
                                    break;
                                case 10:
                                    WritableMap contactMap = Arguments.createMap();
                                    contactMap.putString("vcard", message.getContact());
                                    contentMap.putMap("contact", contactMap);
                                    break;
                                case 11:
                                    WritableMap stickerMap = Arguments.createMap();
                                    stickerMap.putString("name", message.getStickerName());
                                    stickerMap.putString("category", message.getStickerCategory());
                                    contentMap.putMap("sticker", stickerMap);
                                    break;
                                case 100:
                                    try {
                                        contentMap = Arguments.fromBundle(jsonToBundle(message.getText()));
                                    } catch (JSONException e) {
                                        e.printStackTrace();
                                    }
                                    break;
                            }
                            param.putMap("content", contentMap);
                            String senderId = message.getSenderId();
                            User user = mClient.getUser(senderId);
                            String name = "";
                            if (user != null) {
                                name = user.getName();
                                if (name == null || name.length() == 0) {
                                    name = user.getUserId();
                                }
                            }
                            param.putString("sender", name);
                            params.pushMap(param);
                        }
                        callback.invoke(true, 0, "Success", params);
                    }

                    @Override
                    public void onError(StringeeError error) {
                        callback.invoke(false, error.getCode(), error.getMessage());
                    }
                });
            }

            @Override
            public void onError(StringeeError error) {
                callback.invoke(false, error.getCode(), error.getMessage());
            }
        });
    }

    @ReactMethod
    public void getLastMessages(String instanceId, String convId, final int count, boolean loadDeletedMsg, boolean loadDeletedMsgContent, final Callback callback) {
        StringeeClient mClient = StringeeManager.getInstance().getClientsMap().get(instanceId);
        if (mClient == null) {
            callback.invoke(false, -1, "StringeeClient is not initialized or connected");
            return;
        }

        mClient.getConversation(convId, new CallbackListener<Conversation>() {
            @Override
            public void onSuccess(Conversation conversation) {
                conversation.getLastMessages(mClient, count, loadDeletedMsg, loadDeletedMsgContent, false, new CallbackListener<List<Message>>() {
                    @Override
                    public void onSuccess(List<Message> messages) {
                        WritableArray params = Arguments.createArray();
                        for (int i = 0; i < messages.size(); i++) {
                            Message message = messages.get(i);
                            WritableMap param = Arguments.createMap();
                            param.putString("id", message.getId());
                            param.putString("localId", message.getLocalId());
                            param.putString("conversationId", message.getConversationId());
                            param.putDouble("createdAt", message.getCreatedAt());
                            param.putInt("state", message.getState().getValue());
                            param.putDouble("sequence", message.getSequence());
                            param.putInt("type", message.getType());
                            WritableMap contentMap = Arguments.createMap();
                            switch (message.getType()) {
                                case 1:
                                    contentMap.putString("content", message.getText());
                                    break;
                                case 2:
                                    WritableMap photoMap = Arguments.createMap();
                                    photoMap.putString("filePath", message.getFileUrl());
                                    photoMap.putString("thumbnail", message.getThumbnailUrl());
                                    photoMap.putDouble("ratio", message.getImageRatio());
                                    contentMap.putMap("photo", photoMap);
                                    break;
                                case 3:
                                    WritableMap videoMap = Arguments.createMap();
                                    videoMap.putString("filePath", message.getFileUrl());
                                    videoMap.putString("thumbnail", message.getThumbnailUrl());
                                    videoMap.putDouble("ratio", message.getImageRatio());
                                    videoMap.putInt("duration", message.getDuration());
                                    contentMap.putMap("video", videoMap);
                                    break;
                                case 4:
                                    WritableMap audioMap = Arguments.createMap();
                                    audioMap.putString("filePath", message.getFileUrl());
                                    audioMap.putInt("duration", message.getDuration());
                                    contentMap.putMap("audio", audioMap);
                                    break;
                                case 5:
                                    WritableMap fileMap = Arguments.createMap();
                                    fileMap.putString("filePath", message.getFileUrl());
                                    fileMap.putString("filename", message.getFileName());
                                    fileMap.putDouble("length", message.getFileLength());
                                    contentMap.putMap("file", fileMap);
                                    break;
                                case 7:
                                    try {
                                        contentMap = Arguments.fromBundle(jsonToBundle(message.getText()));
                                    } catch (JSONException e) {
                                        e.printStackTrace();
                                    }
                                    break;
                                case 9:
                                    WritableMap locationMap = Arguments.createMap();
                                    locationMap.putDouble("lat", message.getLatitude());
                                    locationMap.putDouble("lon", message.getLongitude());
                                    contentMap.putMap("location", locationMap);
                                    break;
                                case 10:
                                    WritableMap contactMap = Arguments.createMap();
                                    contactMap.putString("vcard", message.getContact());
                                    contentMap.putMap("contact", contactMap);
                                    break;
                                case 11:
                                    WritableMap stickerMap = Arguments.createMap();
                                    stickerMap.putString("name", message.getStickerName());
                                    stickerMap.putString("category", message.getStickerCategory());
                                    contentMap.putMap("sticker", stickerMap);
                                    break;
                                case 100:
                                    try {
                                        contentMap = Arguments.fromBundle(jsonToBundle(message.getText()));
                                    } catch (JSONException e) {
                                        e.printStackTrace();
                                    }
                                    break;
                            }
                            param.putMap("content", contentMap);
                            String senderId = message.getSenderId();
                            User user = mClient.getUser(senderId);
                            String name = "";
                            if (user != null) {
                                name = user.getName();
                                if (name == null || name.length() == 0) {
                                    name = user.getUserId();
                                }
                            }
                            param.putString("sender", name);
                            params.pushMap(param);
                        }
                        callback.invoke(true, 0, "Success", params);
                    }

                    @Override
                    public void onError(StringeeError error) {
                        callback.invoke(false, error.getCode(), error.getMessage());
                    }
                });
            }

            @Override
            public void onError(StringeeError error) {
                callback.invoke(false, error.getCode(), error.getMessage());
            }
        });
    }


    @ReactMethod
    public void getMessagesAfter(String instanceId, String convId, final int sequence, final int count, boolean loadDeletedMsg, boolean loadDeletedMsgContent, final Callback callback) {
        StringeeClient mClient = StringeeManager.getInstance().getClientsMap().get(instanceId);
        if (mClient == null) {
            callback.invoke(false, -1, "StringeeClient is not initialized or connected");
            return;
        }

        mClient.getConversation(convId, new CallbackListener<Conversation>() {
            @Override
            public void onSuccess(Conversation conversation) {
                conversation.getMessagesAfter(mClient, sequence, count, loadDeletedMsg, loadDeletedMsgContent, false, new CallbackListener<List<Message>>() {
                    @Override
                    public void onSuccess(List<Message> messages) {
                        WritableArray params = Arguments.createArray();
                        for (int i = 0; i < messages.size(); i++) {
                            Message message = messages.get(i);
                            WritableMap param = Arguments.createMap();
                            param.putString("id", message.getId());
                            param.putString("localId", message.getLocalId());
                            param.putString("conversationId", message.getConversationId());
                            param.putDouble("createdAt", message.getCreatedAt());
                            param.putInt("state", message.getState().getValue());
                            param.putDouble("sequence", message.getSequence());
                            param.putInt("type", message.getType());
                            WritableMap contentMap = Arguments.createMap();
                            switch (message.getType()) {
                                case 1:
                                    contentMap.putString("content", message.getText());
                                    break;
                                case 2:
                                    WritableMap photoMap = Arguments.createMap();
                                    photoMap.putString("filePath", message.getFileUrl());
                                    photoMap.putString("thumbnail", message.getThumbnailUrl());
                                    photoMap.putDouble("ratio", message.getImageRatio());
                                    contentMap.putMap("photo", photoMap);
                                    break;
                                case 3:
                                    WritableMap videoMap = Arguments.createMap();
                                    videoMap.putString("filePath", message.getFileUrl());
                                    videoMap.putString("thumbnail", message.getThumbnailUrl());
                                    videoMap.putDouble("ratio", message.getImageRatio());
                                    videoMap.putInt("duration", message.getDuration());
                                    contentMap.putMap("video", videoMap);
                                    break;
                                case 4:
                                    WritableMap audioMap = Arguments.createMap();
                                    audioMap.putString("filePath", message.getFileUrl());
                                    audioMap.putInt("duration", message.getDuration());
                                    contentMap.putMap("audio", audioMap);
                                    break;
                                case 5:
                                    WritableMap fileMap = Arguments.createMap();
                                    fileMap.putString("filePath", message.getFileUrl());
                                    fileMap.putString("filename", message.getFileName());
                                    fileMap.putDouble("length", message.getFileLength());
                                    contentMap.putMap("file", fileMap);
                                    break;
                                case 7:
                                    try {
                                        contentMap = Arguments.fromBundle(jsonToBundle(message.getText()));
                                    } catch (JSONException e) {
                                        e.printStackTrace();
                                    }
                                    break;
                                case 9:
                                    WritableMap locationMap = Arguments.createMap();
                                    locationMap.putDouble("lat", message.getLatitude());
                                    locationMap.putDouble("lon", message.getLongitude());
                                    contentMap.putMap("location", locationMap);
                                    break;
                                case 10:
                                    WritableMap contactMap = Arguments.createMap();
                                    contactMap.putString("vcard", message.getContact());
                                    contentMap.putMap("contact", contactMap);
                                    break;
                                case 11:
                                    WritableMap stickerMap = Arguments.createMap();
                                    stickerMap.putString("name", message.getStickerName());
                                    stickerMap.putString("category", message.getStickerCategory());
                                    contentMap.putMap("sticker", stickerMap);
                                    break;
                                case 100:
                                    try {
                                        contentMap = Arguments.fromBundle(jsonToBundle(message.getText()));
                                    } catch (JSONException e) {
                                        e.printStackTrace();
                                    }
                                    break;
                            }
                            param.putMap("content", contentMap);
                            String senderId = message.getSenderId();
                            User user = mClient.getUser(senderId);
                            String name = "";
                            if (user != null) {
                                name = user.getName();
                                if (name == null || name.length() == 0) {
                                    name = user.getUserId();
                                }
                            }
                            param.putString("sender", name);
                            params.pushMap(param);
                        }
                        callback.invoke(true, 0, "Success", params);
                    }

                    @Override
                    public void onError(StringeeError error) {
                        callback.invoke(false, error.getCode(), error.getMessage());
                    }
                });
            }

            @Override
            public void onError(StringeeError error) {
                callback.invoke(false, error.getCode(), error.getMessage());
            }
        });
    }

    @ReactMethod
    public void getMessagesBefore(String instanceId, String convId, final int sequence, final int count, boolean loadDeletedMsg, boolean loadDeletedMsgContent, final Callback callback) {
        StringeeClient mClient = StringeeManager.getInstance().getClientsMap().get(instanceId);
        if (mClient == null) {
            callback.invoke(false, -1, "StringeeClient is not initialized or connected");
            return;
        }

        mClient.getConversation(convId, new CallbackListener<Conversation>() {
            @Override
            public void onSuccess(Conversation conversation) {
                conversation.getMessagesBefore(mClient, sequence, count, loadDeletedMsg, loadDeletedMsgContent, false, new CallbackListener<List<Message>>() {
                    @Override
                    public void onSuccess(List<Message> messages) {
                        WritableArray params = Arguments.createArray();
                        for (int i = 0; i < messages.size(); i++) {
                            Message message = messages.get(i);
                            WritableMap param = Arguments.createMap();
                            param.putString("id", message.getId());
                            param.putString("localId", message.getLocalId());
                            param.putString("conversationId", message.getConversationId());
                            param.putDouble("createdAt", message.getCreatedAt());
                            param.putInt("state", message.getState().getValue());
                            param.putDouble("sequence", message.getSequence());
                            param.putInt("type", message.getType());
                            WritableMap contentMap = Arguments.createMap();
                            switch (message.getType()) {
                                case 1:
                                    contentMap.putString("content", message.getText());
                                    break;
                                case 2:
                                    WritableMap photoMap = Arguments.createMap();
                                    photoMap.putString("filePath", message.getFileUrl());
                                    photoMap.putString("thumbnail", message.getThumbnailUrl());
                                    photoMap.putDouble("ratio", message.getImageRatio());
                                    contentMap.putMap("photo", photoMap);
                                    break;
                                case 3:
                                    WritableMap videoMap = Arguments.createMap();
                                    videoMap.putString("filePath", message.getFileUrl());
                                    videoMap.putString("thumbnail", message.getThumbnailUrl());
                                    videoMap.putDouble("ratio", message.getImageRatio());
                                    videoMap.putInt("duration", message.getDuration());
                                    contentMap.putMap("video", videoMap);
                                    break;
                                case 4:
                                    WritableMap audioMap = Arguments.createMap();
                                    audioMap.putString("filePath", message.getFileUrl());
                                    audioMap.putInt("duration", message.getDuration());
                                    contentMap.putMap("audio", audioMap);
                                    break;
                                case 5:
                                    WritableMap fileMap = Arguments.createMap();
                                    fileMap.putString("filePath", message.getFileUrl());
                                    fileMap.putString("filename", message.getFileName());
                                    fileMap.putDouble("length", message.getFileLength());
                                    contentMap.putMap("file", fileMap);
                                    break;
                                case 7:
                                    try {
                                        contentMap = Arguments.fromBundle(jsonToBundle(message.getText()));
                                    } catch (JSONException e) {
                                        e.printStackTrace();
                                    }
                                    break;

                                case 9:
                                    WritableMap locationMap = Arguments.createMap();
                                    locationMap.putDouble("lat", message.getLatitude());
                                    locationMap.putDouble("lon", message.getLongitude());
                                    contentMap.putMap("location", locationMap);
                                    break;
                                case 10:
                                    WritableMap contactMap = Arguments.createMap();
                                    contactMap.putString("vcard", message.getContact());
                                    contentMap.putMap("contact", contactMap);
                                    break;
                                case 11:
                                    WritableMap stickerMap = Arguments.createMap();
                                    stickerMap.putString("name", message.getStickerName());
                                    stickerMap.putString("category", message.getStickerCategory());
                                    contentMap.putMap("sticker", stickerMap);
                                    break;
                                case 100:
                                    try {
                                        contentMap = Arguments.fromBundle(jsonToBundle(message.getText()));
                                    } catch (JSONException e) {
                                        e.printStackTrace();
                                    }
                                    break;
                            }
                            param.putMap("content", contentMap);
                            String senderId = message.getSenderId();
                            User user = mClient.getUser(senderId);
                            String name = "";
                            if (user != null) {
                                name = user.getName();
                                if (name == null || name.length() == 0) {
                                    name = user.getUserId();
                                }
                            }
                            param.putString("sender", name);
                            params.pushMap(param);
                        }
                        callback.invoke(true, 0, "Success", params);
                    }

                    @Override
                    public void onError(StringeeError error) {
                        callback.invoke(false, error.getCode(), error.getMessage());
                    }
                });
            }

            @Override
            public void onError(StringeeError error) {
                callback.invoke(false, error.getCode(), error.getMessage());
            }
        });
    }

    @ReactMethod
    public void deleteMessage(String instanceId, String convId, final String msgId, final Callback callback) {
        StringeeClient mClient = StringeeManager.getInstance().getClientsMap().get(instanceId);
        if (mClient == null) {
            callback.invoke(false, -1, "StringeeClient is not initialized or connected");
            return;
        }

        JSONArray messageIds = new JSONArray();
        messageIds.put(msgId);
        mClient.deleteMessages(convId, messageIds, new StatusListener() {
            @Override
            public void onSuccess() {
                callback.invoke(true, 0, "Success");
            }

            @Override
            public void onError(StringeeError error) {
                callback.invoke(false, error.getCode(), error.getMessage());
            }
        });
    }

    @ReactMethod
    public void markConversationAsRead(String instanceId, String convId, final Callback callback) {
        StringeeClient mClient = StringeeManager.getInstance().getClientsMap().get(instanceId);
        if (mClient == null) {
            callback.invoke(false, -1, "StringeeClient is not initialized or connected");
            return;
        }

        mClient.getConversationFromServer(convId, new CallbackListener<Conversation>() {
            @Override
            public void onSuccess(Conversation conversation) {
                conversation.getLastMessages(mClient, 1, true, true, false, new CallbackListener<List<Message>>() {
                    @Override
                    public void onSuccess(List<Message> messages) {
                        if (messages != null && messages.size() > 0) {
                            Message message = messages.get(0);
                            if (message != null) {
                                message.markAsRead(mClient, new StatusListener() {
                                    @Override
                                    public void onSuccess() {
                                        callback.invoke(true, 0, "Success");
                                    }

                                    @Override
                                    public void onError(StringeeError error) {
                                        callback.invoke(false, error.getCode(), error.getMessage());
                                    }
                                });
                            }
                        }
                    }
                });
            }
        });
    }

    @ReactMethod
    public void getUser(String instanceId, String userId, Callback callback) {
        StringeeClient mClient = StringeeManager.getInstance().getClientsMap().get(instanceId);
        if (mClient == null) {
            callback.invoke(false, -1, "StringeeClient is not initialized");
            return;
        }

        User user = mClient.getUser(userId);
        if (user != null) {
            WritableMap param = Arguments.createMap();
            param.putString("userId", user.getUserId());
            param.putString("name", user.getName());
            param.putString("avatar", user.getAvatarUrl());
            callback.invoke(true, 0, "Success", param);
        } else {
            callback.invoke(false, -1, "User does not exist.");
        }
    }

    @ReactMethod
    public void clearDb(String instanceId, Callback callback) {
        StringeeClient mClient = StringeeManager.getInstance().getClientsMap().get(instanceId);
        if (mClient == null) {
            callback.invoke(false, -1, "StringeeClient is not initialized");
            return;
        }
        mClient.clearDb();
        callback.invoke(true, 0, "Success");
    }

    @ReactMethod
    public void updateConversation(String instanceId, String convId, ReadableMap convMap, final Callback callback) {
        StringeeClient mClient = StringeeManager.getInstance().getClientsMap().get(instanceId);
        if (mClient == null) {
            callback.invoke(false, -1, "StringeeClient is not initialized");
            return;
        }

        if (convId == null) {
            callback.invoke(false, -2, "Conversation id can not be null");
            return;
        }

        String name = "";
        if (convMap.hasKey("name")) {
            name = convMap.getString("name");
        }
        String avatar = "";
        if (convMap.hasKey("avatar")) {
            avatar = convMap.getString("avatar");
        }

        final String finalAvatar = avatar;
        final String finalName = name;
        mClient.getConversation(convId, new CallbackListener<Conversation>() {
            @Override
            public void onSuccess(Conversation conversation) {
                conversation.updateConversation(mClient, finalName, finalAvatar, new StatusListener() {
                    @Override
                    public void onSuccess() {
                        callback.invoke(true, 0, "Success");
                    }

                    @Override
                    public void onError(StringeeError error) {
                        callback.invoke(false, error.getCode(), error.getMessage());
                    }
                });
            }

            @Override
            public void onError(StringeeError error) {
                callback.invoke(false, error.getCode(), error.getMessage());
            }
        });
    }

    @ReactMethod
    public void getConversationWithUser(String instanceId, String userId, final Callback callback) {
        StringeeClient mClient = StringeeManager.getInstance().getClientsMap().get(instanceId);
        if (mClient == null) {
            callback.invoke(false, -1, "StringeeClient is not initialized");
            return;
        }

        if (userId == null) {
            callback.invoke(false, -2, "User id can not be null");
            return;
        }

        mClient.getConversationByUserId(userId, new CallbackListener<Conversation>() {
            @Override
            public void onSuccess(Conversation conversation) {
                WritableMap params = Arguments.createMap();
                params.putString("id", conversation.getId());
                params.putString("localId", conversation.getLocalId());
                params.putString("name", conversation.getName());
                params.putBoolean("isDistinct", conversation.isDistinct());
                params.putBoolean("isGroup", conversation.isGroup());
                params.putDouble("updatedAt", conversation.getUpdateAt());
                params.putString("lastMsgSender", conversation.getLastMsgSender());
                params.putString("text", conversation.getText());
                params.putInt("lastMsgType", conversation.getLastMsgType());
                params.putInt("unreadCount", conversation.getTotalUnread());
                params.putString("lastMsgId", conversation.getLastMsgId());
                params.putString("creator", conversation.getCreator());
                params.putDouble("created", conversation.getCreateAt());
                params.putDouble("lastMsgSeq", conversation.getLastMsgSeqReceived());
                params.putDouble("lastMsgCreatedAt", conversation.getLastTimeNewMsg());
                params.putInt("lastMsgState", conversation.getLastMsgState());
                if (conversation.getLastMsg() != null) {
                    try {
                        Bundle bundle = jsonToBundle(conversation.getLastMsg());
                        WritableMap lastMsgMap = Arguments.fromBundle(bundle);
                        params.putMap("text", lastMsgMap);
                    } catch (JSONException e) {
                        e.printStackTrace();
                    }
                }
                List<User> participants = conversation.getParticipants();
                WritableArray participantsMap = Arguments.createArray();
                for (int i = 0; i < participants.size(); i++) {
                    User user = participants.get(i);
                    WritableMap userMap = Arguments.createMap();
                    userMap.putString("userId", user.getUserId());
                    userMap.putString("name", user.getName());
                    userMap.putString("avatar", user.getAvatarUrl());
                    participantsMap.pushMap(userMap);
                }
                params.putArray("participants", participantsMap);
                callback.invoke(true, 0, "Success", params);
            }

            @Override
            public void onError(StringeeError error) {
                callback.invoke(false, error.getCode(), error.getMessage());
            }
        });
    }

    @ReactMethod
    public void getUnreadConversationCount(String instanceId, final Callback callback) {
        StringeeClient mClient = StringeeManager.getInstance().getClientsMap().get(instanceId);
        if (mClient == null) {
            callback.invoke(false, -1, "StringeeClient is not initialized");
            return;
        }

        mClient.getTotalUnread(new CallbackListener<Integer>() {
            @Override
            public void onSuccess(Integer count) {
                callback.invoke(true, 0, "Success", count);
            }

            @Override
            public void onError(StringeeError error) {
                callback.invoke(false, error.getCode(), error.getMessage());
            }
        });
    }

    @ReactMethod
    public void getLastUnreadConversations(String instanceId, int count, final Callback callback) {
        StringeeClient mClient = StringeeManager.getInstance().getClientsMap().get(instanceId);
        if (mClient == null) {
            callback.invoke(false, -1, "StringeeClient is not initialized");
            return;
        }

        mClient.getLastUnreadConversations(count, new CallbackListener<List<Conversation>>() {
            @Override
            public void onSuccess(List<Conversation> conversations) {
                WritableArray params = Arguments.createArray();
                for (int i = 0; i < conversations.size(); i++) {
                    Conversation conversation = conversations.get(i);
                    WritableMap param = Arguments.createMap();
                    param.putString("id", conversation.getId());
                    param.putString("localId", conversation.getLocalId());
                    param.putString("name", conversation.getName());
                    param.putBoolean("isDistinct", conversation.isDistinct());
                    param.putBoolean("isGroup", conversation.isGroup());
                    param.putDouble("updatedAt", conversation.getUpdateAt());
                    param.putString("lastMsgSender", conversation.getLastMsgSender());
                    param.putString("text", conversation.getText());
                    param.putInt("lastMsgType", conversation.getLastMsgType());
                    param.putInt("unreadCount", conversation.getTotalUnread());
                    param.putString("lastMsgId", conversation.getLastMsgId());
                    param.putString("creator", conversation.getCreator());
                    param.putDouble("created", conversation.getCreateAt());
                    param.putDouble("lastMsgSeq", conversation.getLastMsgSeqReceived());
                    param.putDouble("lastMsgCreatedAt", conversation.getLastTimeNewMsg());
                    param.putInt("lastMsgState", conversation.getLastMsgState());
                    if (conversation.getLastMsg() != null) {
                        try {
                            Bundle bundle = jsonToBundle(conversation.getLastMsg());
                            WritableMap lastMsgMap = Arguments.fromBundle(bundle);
                            param.putMap("text", lastMsgMap);
                        } catch (JSONException e) {
                            e.printStackTrace();
                        }
                    }
                    List<User> participants = conversation.getParticipants();
                    WritableArray participantsMap = Arguments.createArray();
                    for (int j = 0; j < participants.size(); j++) {
                        User user = participants.get(j);
                        WritableMap userMap = Arguments.createMap();
                        userMap.putString("userId", user.getUserId());
                        userMap.putString("name", user.getName());
                        userMap.putString("avatar", user.getAvatarUrl());
                        participantsMap.pushMap(userMap);
                    }
                    param.putArray("participants", participantsMap);

                    params.pushMap(param);
                }
                callback.invoke(true, 0, "Success", params);
            }

            @Override
            public void onError(StringeeError error) {
                callback.invoke(false, error.getCode(), error.getMessage());
            }
        });
    }

    @ReactMethod
    public void getUnreadConversationsBefore(String instanceId, double datetime, int count, final Callback callback) {
        StringeeClient mClient = StringeeManager.getInstance().getClientsMap().get(instanceId);
        if (mClient == null) {
            callback.invoke(false, -1, "StringeeClient is not initialized or connected");
            return;
        }

        mClient.getUnreadConversationsBefore((long) datetime, count, new CallbackListener<List<Conversation>>() {
            @Override
            public void onSuccess(List<Conversation> conversations) {
                WritableArray params = Arguments.createArray();
                for (int i = 0; i < conversations.size(); i++) {
                    Conversation conversation = conversations.get(i);
                    WritableMap param = Arguments.createMap();
                    param.putString("id", conversation.getId());
                    param.putString("localId", conversation.getLocalId());
                    param.putString("name", conversation.getName());
                    param.putBoolean("isDistinct", conversation.isDistinct());
                    param.putBoolean("isGroup", conversation.isGroup());
                    param.putDouble("updatedAt", conversation.getUpdateAt());
                    param.putString("lastMsgSender", conversation.getLastMsgSender());
                    param.putString("text", conversation.getText());
                    param.putInt("lastMsgType", conversation.getLastMsgType());
                    param.putInt("unreadCount", conversation.getTotalUnread());
                    param.putString("lastMsgId", conversation.getLastMsgId());
                    param.putString("creator", conversation.getCreator());
                    param.putDouble("created", conversation.getCreateAt());
                    param.putDouble("lastMsgSeq", conversation.getLastMsgSeqReceived());
                    param.putDouble("lastMsgCreatedAt", conversation.getLastTimeNewMsg());
                    param.putInt("lastMsgState", conversation.getLastMsgState());
                    if (conversation.getLastMsg() != null) {
                        try {
                            Bundle bundle = jsonToBundle(conversation.getLastMsg());
                            WritableMap lastMsgMap = Arguments.fromBundle(bundle);
                            param.putMap("text", lastMsgMap);
                        } catch (JSONException e) {
                            e.printStackTrace();
                        }
                    }
                    List<User> participants = conversation.getParticipants();
                    WritableArray participantsMap = Arguments.createArray();
                    for (int j = 0; j < participants.size(); j++) {
                        User user = participants.get(j);
                        WritableMap userMap = Arguments.createMap();
                        userMap.putString("userId", user.getUserId());
                        userMap.putString("name", user.getName());
                        userMap.putString("avatar", user.getAvatarUrl());
                        participantsMap.pushMap(userMap);
                    }
                    param.putArray("participants", participantsMap);

                    params.pushMap(param);
                }
                callback.invoke(true, 0, "Success", params);
            }

            @Override
            public void onError(StringeeError error) {
                callback.invoke(false, error.getCode(), error.getMessage());
            }
        });
    }

    @ReactMethod
    public void getUnreadConversationsAfter(String instanceId, double datetime, int count, final Callback callback) {
        StringeeClient mClient = StringeeManager.getInstance().getClientsMap().get(instanceId);
        if (mClient == null) {
            callback.invoke(false, -1, "StringeeClient is not initialized or connected");
            return;
        }

        mClient.getUnreadConversationsAfter((long) datetime, count, new CallbackListener<List<Conversation>>() {
            @Override
            public void onSuccess(List<Conversation> conversations) {
                WritableArray params = Arguments.createArray();
                for (int i = 0; i < conversations.size(); i++) {
                    Conversation conversation = conversations.get(i);
                    WritableMap param = Arguments.createMap();
                    param.putString("id", conversation.getId());
                    param.putString("localId", conversation.getLocalId());
                    param.putString("name", conversation.getName());
                    param.putBoolean("isDistinct", conversation.isDistinct());
                    param.putBoolean("isGroup", conversation.isGroup());
                    param.putDouble("updatedAt", conversation.getUpdateAt());
                    param.putString("lastMsgSender", conversation.getLastMsgSender());
                    param.putString("text", conversation.getText());
                    param.putInt("lastMsgType", conversation.getLastMsgType());
                    param.putInt("unreadCount", conversation.getTotalUnread());
                    param.putString("lastMsgId", conversation.getLastMsgId());
                    param.putString("creator", conversation.getCreator());
                    param.putDouble("created", conversation.getCreateAt());
                    param.putDouble("lastMsgSeq", conversation.getLastMsgSeqReceived());
                    param.putDouble("lastMsgCreatedAt", conversation.getLastTimeNewMsg());
                    param.putInt("lastMsgState", conversation.getLastMsgState());
                    if (conversation.getLastMsg() != null) {
                        try {
                            Bundle bundle = jsonToBundle(conversation.getLastMsg());
                            WritableMap lastMsgMap = Arguments.fromBundle(bundle);
                            param.putMap("text", lastMsgMap);
                        } catch (JSONException e) {
                            e.printStackTrace();
                        }
                    }
                    List<User> participants = conversation.getParticipants();
                    WritableArray participantsMap = Arguments.createArray();
                    for (int j = 0; j < participants.size(); j++) {
                        User user = participants.get(j);
                        WritableMap userMap = Arguments.createMap();
                        userMap.putString("userId", user.getUserId());
                        userMap.putString("name", user.getName());
                        userMap.putString("avatar", user.getAvatarUrl());
                        participantsMap.pushMap(userMap);
                    }
                    param.putArray("participants", participantsMap);

                    params.pushMap(param);
                }
                callback.invoke(true, 0, "Success", params);
            }

            @Override
            public void onError(StringeeError error) {
                callback.invoke(false, error.getCode(), error.getMessage());
            }
        });
    }

    @ReactMethod
    public void getAllLastConversations(String instanceId, int count, final Callback callback) {
        StringeeClient mClient = StringeeManager.getInstance().getClientsMap().get(instanceId);
        if (mClient == null) {
            callback.invoke(false, -1, "StringeeClient is not initialized");
            return;
        }

        mClient.getLastConversations(count, true, new CallbackListener<List<Conversation>>() {
            @Override
            public void onSuccess(List<Conversation> conversations) {
                WritableArray params = Arguments.createArray();
                for (int i = 0; i < conversations.size(); i++) {
                    Conversation conversation = conversations.get(i);
                    WritableMap param = Arguments.createMap();
                    param.putString("id", conversation.getId());
                    param.putString("localId", conversation.getLocalId());
                    param.putString("name", conversation.getName());
                    param.putBoolean("isDistinct", conversation.isDistinct());
                    param.putBoolean("isGroup", conversation.isGroup());
                    param.putDouble("updatedAt", conversation.getUpdateAt());
                    param.putString("lastMsgSender", conversation.getLastMsgSender());
                    param.putString("text", conversation.getText());
                    param.putInt("lastMsgType", conversation.getLastMsgType());
                    param.putInt("unreadCount", conversation.getTotalUnread());
                    param.putString("lastMsgId", conversation.getLastMsgId());
                    param.putString("creator", conversation.getCreator());
                    param.putDouble("created", conversation.getCreateAt());
                    param.putDouble("lastMsgSeq", conversation.getLastMsgSeqReceived());
                    param.putDouble("lastMsgCreatedAt", conversation.getLastTimeNewMsg());
                    param.putInt("lastMsgState", conversation.getLastMsgState());
                    if (conversation.getLastMsg() != null) {
                        try {
                            Bundle bundle = jsonToBundle(conversation.getLastMsg());
                            WritableMap lastMsgMap = Arguments.fromBundle(bundle);
                            param.putMap("text", lastMsgMap);
                        } catch (JSONException e) {
                            e.printStackTrace();
                        }
                    }
                    List<User> participants = conversation.getParticipants();
                    WritableArray participantsMap = Arguments.createArray();
                    for (int j = 0; j < participants.size(); j++) {
                        User user = participants.get(j);
                        WritableMap userMap = Arguments.createMap();
                        userMap.putString("userId", user.getUserId());
                        userMap.putString("name", user.getName());
                        userMap.putString("avatar", user.getAvatarUrl());
                        participantsMap.pushMap(userMap);
                    }
                    param.putArray("participants", participantsMap);

                    params.pushMap(param);
                }
                callback.invoke(true, 0, "Success", params);
            }

            @Override
            public void onError(StringeeError error) {
                callback.invoke(false, error.getCode(), error.getMessage());
            }
        });
    }

    @ReactMethod
    public void getAllConversationsBefore(String instanceId, double datetime, int count, final Callback callback) {
        StringeeClient mClient = StringeeManager.getInstance().getClientsMap().get(instanceId);
        if (mClient == null) {
            callback.invoke(false, -1, "StringeeClient is not initialized or connected");
            return;
        }

        mClient.getConversationsBefore((long) datetime, count, true, new CallbackListener<List<Conversation>>() {
            @Override
            public void onSuccess(List<Conversation> conversations) {
                WritableArray params = Arguments.createArray();
                for (int i = 0; i < conversations.size(); i++) {
                    Conversation conversation = conversations.get(i);
                    WritableMap param = Arguments.createMap();
                    param.putString("id", conversation.getId());
                    param.putString("localId", conversation.getLocalId());
                    param.putString("name", conversation.getName());
                    param.putBoolean("isDistinct", conversation.isDistinct());
                    param.putBoolean("isGroup", conversation.isGroup());
                    param.putDouble("updatedAt", conversation.getUpdateAt());
                    param.putString("lastMsgSender", conversation.getLastMsgSender());
                    param.putString("text", conversation.getText());
                    param.putInt("lastMsgType", conversation.getLastMsgType());
                    param.putInt("unreadCount", conversation.getTotalUnread());
                    param.putString("lastMsgId", conversation.getLastMsgId());
                    param.putString("creator", conversation.getCreator());
                    param.putDouble("created", conversation.getCreateAt());
                    param.putDouble("lastMsgSeq", conversation.getLastMsgSeqReceived());
                    param.putDouble("lastMsgCreatedAt", conversation.getLastTimeNewMsg());
                    param.putInt("lastMsgState", conversation.getLastMsgState());
                    if (conversation.getLastMsg() != null) {
                        try {
                            Bundle bundle = jsonToBundle(conversation.getLastMsg());
                            WritableMap lastMsgMap = Arguments.fromBundle(bundle);
                            param.putMap("text", lastMsgMap);
                        } catch (JSONException e) {
                            e.printStackTrace();
                        }
                    }
                    List<User> participants = conversation.getParticipants();
                    WritableArray participantsMap = Arguments.createArray();
                    for (int j = 0; j < participants.size(); j++) {
                        User user = participants.get(j);
                        WritableMap userMap = Arguments.createMap();
                        userMap.putString("userId", user.getUserId());
                        userMap.putString("name", user.getName());
                        userMap.putString("avatar", user.getAvatarUrl());
                        participantsMap.pushMap(userMap);
                    }
                    param.putArray("participants", participantsMap);

                    params.pushMap(param);
                }
                callback.invoke(true, 0, "Success", params);
            }

            @Override
            public void onError(StringeeError error) {
                callback.invoke(false, error.getCode(), error.getMessage());
            }
        });
    }

    @ReactMethod
    public void getAllConversationsAfter(String instanceId, double datetime, int count, final Callback callback) {
        StringeeClient mClient = StringeeManager.getInstance().getClientsMap().get(instanceId);
        if (mClient == null) {
            callback.invoke(false, -1, "StringeeClient is not initialized or connected");
            return;
        }

        mClient.getConversationsAfter((long) datetime, count, true, new CallbackListener<List<Conversation>>() {
            @Override
            public void onSuccess(List<Conversation> conversations) {
                WritableArray params = Arguments.createArray();
                for (int i = 0; i < conversations.size(); i++) {
                    Conversation conversation = conversations.get(i);
                    WritableMap param = Arguments.createMap();
                    param.putString("id", conversation.getId());
                    param.putString("localId", conversation.getLocalId());
                    param.putString("name", conversation.getName());
                    param.putBoolean("isDistinct", conversation.isDistinct());
                    param.putBoolean("isGroup", conversation.isGroup());
                    param.putDouble("updatedAt", conversation.getUpdateAt());
                    param.putString("lastMsgSender", conversation.getLastMsgSender());
                    param.putString("text", conversation.getText());
                    param.putInt("lastMsgType", conversation.getLastMsgType());
                    param.putInt("unreadCount", conversation.getTotalUnread());
                    param.putString("lastMsgId", conversation.getLastMsgId());
                    param.putString("creator", conversation.getCreator());
                    param.putDouble("created", conversation.getCreateAt());
                    param.putDouble("lastMsgSeq", conversation.getLastMsgSeqReceived());
                    param.putDouble("lastMsgCreatedAt", conversation.getLastTimeNewMsg());
                    param.putInt("lastMsgState", conversation.getLastMsgState());
                    if (conversation.getLastMsg() != null) {
                        try {
                            Bundle bundle = jsonToBundle(conversation.getLastMsg());
                            WritableMap lastMsgMap = Arguments.fromBundle(bundle);
                            param.putMap("text", lastMsgMap);
                        } catch (JSONException e) {
                            e.printStackTrace();
                        }
                    }
                    List<User> participants = conversation.getParticipants();
                    WritableArray participantsMap = Arguments.createArray();
                    for (int j = 0; j < participants.size(); j++) {
                        User user = participants.get(j);
                        WritableMap userMap = Arguments.createMap();
                        userMap.putString("userId", user.getUserId());
                        userMap.putString("name", user.getName());
                        userMap.putString("avatar", user.getAvatarUrl());
                        participantsMap.pushMap(userMap);
                    }
                    param.putArray("participants", participantsMap);

                    params.pushMap(param);
                }
                callback.invoke(true, 0, "Success", params);
            }

            @Override
            public void onError(StringeeError error) {
                callback.invoke(false, error.getCode(), error.getMessage());
            }
        });
    }

    @ReactMethod
    public void getAllLastMessages(String instanceId, String convId, final int count, boolean loadDeletedMsg, boolean loadDeletedMsgContent, final Callback callback) {
        StringeeClient mClient = StringeeManager.getInstance().getClientsMap().get(instanceId);
        if (mClient == null) {
            callback.invoke(false, -1, "StringeeClient is not initialized or connected");
            return;
        }

        mClient.getConversation(convId, new CallbackListener<Conversation>() {
            @Override
            public void onSuccess(Conversation conversation) {
                conversation.getLastMessages(mClient, count, loadDeletedMsg, loadDeletedMsgContent, true, new CallbackListener<List<Message>>() {
                    @Override
                    public void onSuccess(List<Message> messages) {
                        WritableArray params = Arguments.createArray();
                        for (int i = 0; i < messages.size(); i++) {
                            Message message = messages.get(i);
                            WritableMap param = Arguments.createMap();
                            param.putString("id", message.getId());
                            param.putString("localId", message.getLocalId());
                            param.putString("conversationId", message.getConversationId());
                            param.putDouble("createdAt", message.getCreatedAt());
                            param.putInt("state", message.getState().getValue());
                            param.putDouble("sequence", message.getSequence());
                            param.putInt("type", message.getType());
                            WritableMap contentMap = Arguments.createMap();
                            switch (message.getType()) {
                                case 1:
                                    contentMap.putString("content", message.getText());
                                    break;
                                case 2:
                                    WritableMap photoMap = Arguments.createMap();
                                    photoMap.putString("filePath", message.getFileUrl());
                                    photoMap.putString("thumbnail", message.getThumbnailUrl());
                                    photoMap.putDouble("ratio", message.getImageRatio());
                                    contentMap.putMap("photo", photoMap);
                                    break;
                                case 3:
                                    WritableMap videoMap = Arguments.createMap();
                                    videoMap.putString("filePath", message.getFileUrl());
                                    videoMap.putString("thumbnail", message.getThumbnailUrl());
                                    videoMap.putDouble("ratio", message.getImageRatio());
                                    videoMap.putInt("duration", message.getDuration());
                                    contentMap.putMap("video", videoMap);
                                    break;
                                case 4:
                                    WritableMap audioMap = Arguments.createMap();
                                    audioMap.putString("filePath", message.getFileUrl());
                                    audioMap.putInt("duration", message.getDuration());
                                    contentMap.putMap("audio", audioMap);
                                    break;
                                case 5:
                                    WritableMap fileMap = Arguments.createMap();
                                    fileMap.putString("filePath", message.getFileUrl());
                                    fileMap.putString("filename", message.getFileName());
                                    fileMap.putDouble("length", message.getFileLength());
                                    contentMap.putMap("file", fileMap);
                                    break;
                                case 7:
                                    try {
                                        contentMap = Arguments.fromBundle(jsonToBundle(message.getText()));
                                    } catch (JSONException e) {
                                        e.printStackTrace();
                                    }
                                    break;
                                case 9:
                                    WritableMap locationMap = Arguments.createMap();
                                    locationMap.putDouble("lat", message.getLatitude());
                                    locationMap.putDouble("lon", message.getLongitude());
                                    contentMap.putMap("location", locationMap);
                                    break;
                                case 10:
                                    WritableMap contactMap = Arguments.createMap();
                                    contactMap.putString("vcard", message.getContact());
                                    contentMap.putMap("contact", contactMap);
                                    break;
                                case 11:
                                    WritableMap stickerMap = Arguments.createMap();
                                    stickerMap.putString("name", message.getStickerName());
                                    stickerMap.putString("category", message.getStickerCategory());
                                    contentMap.putMap("sticker", stickerMap);
                                    break;
                                case 100:
                                    try {
                                        contentMap = Arguments.fromBundle(jsonToBundle(message.getText()));
                                    } catch (JSONException e) {
                                        e.printStackTrace();
                                    }
                                    break;
                            }
                            param.putMap("content", contentMap);
                            String senderId = message.getSenderId();
                            User user = mClient.getUser(senderId);
                            String name = "";
                            if (user != null) {
                                name = user.getName();
                                if (name == null || name.length() == 0) {
                                    name = user.getUserId();
                                }
                            }
                            param.putString("sender", name);
                            params.pushMap(param);
                        }
                        callback.invoke(true, 0, "Success", params);
                    }

                    @Override
                    public void onError(StringeeError error) {
                        callback.invoke(false, error.getCode(), error.getMessage());
                    }
                });
            }

            @Override
            public void onError(StringeeError error) {
                callback.invoke(false, error.getCode(), error.getMessage());
            }
        });
    }


    @ReactMethod
    public void getAllMessagesAfter(String instanceId, String convId, final int sequence, final int count, boolean loadDeletedMsg, boolean loadDeletedMsgContent, final Callback callback) {
        StringeeClient mClient = StringeeManager.getInstance().getClientsMap().get(instanceId);
        if (mClient == null) {
            callback.invoke(false, -1, "StringeeClient is not initialized or connected");
            return;
        }

        mClient.getConversation(convId, new CallbackListener<Conversation>() {
            @Override
            public void onSuccess(Conversation conversation) {
                conversation.getMessagesAfter(mClient, sequence, count, loadDeletedMsg, loadDeletedMsgContent, true, new CallbackListener<List<Message>>() {
                    @Override
                    public void onSuccess(List<Message> messages) {
                        WritableArray params = Arguments.createArray();
                        for (int i = 0; i < messages.size(); i++) {
                            Message message = messages.get(i);
                            WritableMap param = Arguments.createMap();
                            param.putString("id", message.getId());
                            param.putString("localId", message.getLocalId());
                            param.putString("conversationId", message.getConversationId());
                            param.putDouble("createdAt", message.getCreatedAt());
                            param.putInt("state", message.getState().getValue());
                            param.putDouble("sequence", message.getSequence());
                            param.putInt("type", message.getType());
                            WritableMap contentMap = Arguments.createMap();
                            switch (message.getType()) {
                                case 1:
                                    contentMap.putString("content", message.getText());
                                    break;
                                case 2:
                                    WritableMap photoMap = Arguments.createMap();
                                    photoMap.putString("filePath", message.getFileUrl());
                                    photoMap.putString("thumbnail", message.getThumbnailUrl());
                                    photoMap.putDouble("ratio", message.getImageRatio());
                                    contentMap.putMap("photo", photoMap);
                                    break;
                                case 3:
                                    WritableMap videoMap = Arguments.createMap();
                                    videoMap.putString("filePath", message.getFileUrl());
                                    videoMap.putString("thumbnail", message.getThumbnailUrl());
                                    videoMap.putDouble("ratio", message.getImageRatio());
                                    videoMap.putInt("duration", message.getDuration());
                                    contentMap.putMap("video", videoMap);
                                    break;
                                case 4:
                                    WritableMap audioMap = Arguments.createMap();
                                    audioMap.putString("filePath", message.getFileUrl());
                                    audioMap.putInt("duration", message.getDuration());
                                    contentMap.putMap("audio", audioMap);
                                    break;
                                case 5:
                                    WritableMap fileMap = Arguments.createMap();
                                    fileMap.putString("filePath", message.getFileUrl());
                                    fileMap.putString("filename", message.getFileName());
                                    fileMap.putDouble("length", message.getFileLength());
                                    contentMap.putMap("file", fileMap);
                                    break;
                                case 7:
                                    try {
                                        contentMap = Arguments.fromBundle(jsonToBundle(message.getText()));
                                    } catch (JSONException e) {
                                        e.printStackTrace();
                                    }
                                    break;
                                case 9:
                                    WritableMap locationMap = Arguments.createMap();
                                    locationMap.putDouble("lat", message.getLatitude());
                                    locationMap.putDouble("lon", message.getLongitude());
                                    contentMap.putMap("location", locationMap);
                                    break;
                                case 10:
                                    WritableMap contactMap = Arguments.createMap();
                                    contactMap.putString("vcard", message.getContact());
                                    contentMap.putMap("contact", contactMap);
                                    break;
                                case 11:
                                    WritableMap stickerMap = Arguments.createMap();
                                    stickerMap.putString("name", message.getStickerName());
                                    stickerMap.putString("category", message.getStickerCategory());
                                    contentMap.putMap("sticker", stickerMap);
                                    break;
                                case 100:
                                    try {
                                        contentMap = Arguments.fromBundle(jsonToBundle(message.getText()));
                                    } catch (JSONException e) {
                                        e.printStackTrace();
                                    }
                                    break;
                            }
                            param.putMap("content", contentMap);
                            String senderId = message.getSenderId();
                            User user = mClient.getUser(senderId);
                            String name = "";
                            if (user != null) {
                                name = user.getName();
                                if (name == null || name.length() == 0) {
                                    name = user.getUserId();
                                }
                            }
                            param.putString("sender", name);
                            params.pushMap(param);
                        }
                        callback.invoke(true, 0, "Success", params);
                    }

                    @Override
                    public void onError(StringeeError error) {
                        callback.invoke(false, error.getCode(), error.getMessage());
                    }
                });
            }

            @Override
            public void onError(StringeeError error) {
                callback.invoke(false, error.getCode(), error.getMessage());
            }
        });
    }

    @ReactMethod
    public void getAllMessagesBefore(String instanceId, String convId, final int sequence, final int count, boolean loadDeletedMsg, boolean loadDeletedMsgContent, final Callback callback) {
        StringeeClient mClient = StringeeManager.getInstance().getClientsMap().get(instanceId);
        if (mClient == null) {
            callback.invoke(false, -1, "StringeeClient is not initialized or connected");
            return;
        }

        mClient.getConversation(convId, new CallbackListener<Conversation>() {
            @Override
            public void onSuccess(Conversation conversation) {
                conversation.getMessagesBefore(mClient, sequence, count, loadDeletedMsg, loadDeletedMsgContent, true, new CallbackListener<List<Message>>() {
                    @Override
                    public void onSuccess(List<Message> messages) {
                        WritableArray params = Arguments.createArray();
                        for (int i = 0; i < messages.size(); i++) {
                            Message message = messages.get(i);
                            WritableMap param = Arguments.createMap();
                            param.putString("id", message.getId());
                            param.putString("localId", message.getLocalId());
                            param.putString("conversationId", message.getConversationId());
                            param.putDouble("createdAt", message.getCreatedAt());
                            param.putInt("state", message.getState().getValue());
                            param.putDouble("sequence", message.getSequence());
                            param.putInt("type", message.getType());
                            WritableMap contentMap = Arguments.createMap();
                            switch (message.getType()) {
                                case 1:
                                    contentMap.putString("content", message.getText());
                                    break;
                                case 2:
                                    WritableMap photoMap = Arguments.createMap();
                                    photoMap.putString("filePath", message.getFileUrl());
                                    photoMap.putString("thumbnail", message.getThumbnailUrl());
                                    photoMap.putDouble("ratio", message.getImageRatio());
                                    contentMap.putMap("photo", photoMap);
                                    break;
                                case 3:
                                    WritableMap videoMap = Arguments.createMap();
                                    videoMap.putString("filePath", message.getFileUrl());
                                    videoMap.putString("thumbnail", message.getThumbnailUrl());
                                    videoMap.putDouble("ratio", message.getImageRatio());
                                    videoMap.putInt("duration", message.getDuration());
                                    contentMap.putMap("video", videoMap);
                                    break;
                                case 4:
                                    WritableMap audioMap = Arguments.createMap();
                                    audioMap.putString("filePath", message.getFileUrl());
                                    audioMap.putInt("duration", message.getDuration());
                                    contentMap.putMap("audio", audioMap);
                                    break;
                                case 5:
                                    WritableMap fileMap = Arguments.createMap();
                                    fileMap.putString("filePath", message.getFileUrl());
                                    fileMap.putString("filename", message.getFileName());
                                    fileMap.putDouble("length", message.getFileLength());
                                    contentMap.putMap("file", fileMap);
                                    break;
                                case 7:
                                    try {
                                        contentMap = Arguments.fromBundle(jsonToBundle(message.getText()));
                                    } catch (JSONException e) {
                                        e.printStackTrace();
                                    }
                                    break;

                                case 9:
                                    WritableMap locationMap = Arguments.createMap();
                                    locationMap.putDouble("lat", message.getLatitude());
                                    locationMap.putDouble("lon", message.getLongitude());
                                    contentMap.putMap("location", locationMap);
                                    break;
                                case 10:
                                    WritableMap contactMap = Arguments.createMap();
                                    contactMap.putString("vcard", message.getContact());
                                    contentMap.putMap("contact", contactMap);
                                    break;
                                case 11:
                                    WritableMap stickerMap = Arguments.createMap();
                                    stickerMap.putString("name", message.getStickerName());
                                    stickerMap.putString("category", message.getStickerCategory());
                                    contentMap.putMap("sticker", stickerMap);
                                    break;
                                case 100:
                                    try {
                                        contentMap = Arguments.fromBundle(jsonToBundle(message.getText()));
                                    } catch (JSONException e) {
                                        e.printStackTrace();
                                    }
                                    break;
                            }
                            param.putMap("content", contentMap);
                            String senderId = message.getSenderId();
                            User user = mClient.getUser(senderId);
                            String name = "";
                            if (user != null) {
                                name = user.getName();
                                if (name == null || name.length() == 0) {
                                    name = user.getUserId();
                                }
                            }
                            param.putString("sender", name);
                            params.pushMap(param);
                        }
                        callback.invoke(true, 0, "Success", params);
                    }

                    @Override
                    public void onError(StringeeError error) {
                        callback.invoke(false, error.getCode(), error.getMessage());
                    }
                });
            }

            @Override
            public void onError(StringeeError error) {
                callback.invoke(false, error.getCode(), error.getMessage());
            }
        });
    }

    private Bundle jsonToBundle(String text) throws JSONException {
        JSONObject jsonObject = new JSONObject(text);
        Bundle bundle = new Bundle();
        Iterator iter = jsonObject.keys();
        while (iter.hasNext()) {
            String key = (String) iter.next();
            String value = jsonObject.getString(key);
            bundle.putString(key, value);
        }
        return bundle;
    }

    private void sendEvent(ReactContext reactContext, String eventName, @Nullable WritableMap eventData) {
        reactContext
                .getJSModule(DeviceEventManagerModule.RCTDeviceEventEmitter.class)
                .emit(eventName, eventData);
    }

    @ReactMethod
    public void setNativeEvent(String instanceId, String event) {
        ArrayList<String> jsEvents = eventsMap.get(instanceId);
        if (jsEvents != null) {
            jsEvents.add(event);
        } else {
            jsEvents = new ArrayList<>();
            jsEvents.add(event);
            eventsMap.put(instanceId, jsEvents);
        }
    }

    @ReactMethod
    public void removeNativeEvent(String instanceId, String event) {
        ArrayList<String> jsEvents = eventsMap.get(instanceId);
        if (jsEvents != null) {
            jsEvents.remove(event);
        }
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
