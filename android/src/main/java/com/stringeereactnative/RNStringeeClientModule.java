package com.stringeereactnative;

import android.content.Context;
import android.os.Bundle;
import android.text.TextUtils;

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
import com.stringee.common.SocketAddress;
import com.stringee.exception.StringeeError;
import com.stringee.listener.StatusListener;
import com.stringee.listener.StringeeConnectionListener;
import com.stringee.messaging.ChatProfile;
import com.stringee.messaging.ChatRequest;
import com.stringee.messaging.Conversation;
import com.stringee.messaging.Conversation.State;
import com.stringee.messaging.ConversationOptions;
import com.stringee.messaging.Message;
import com.stringee.messaging.Message.Type;
import com.stringee.messaging.StringeeChange;
import com.stringee.messaging.StringeeObject;
import com.stringee.messaging.User;
import com.stringee.messaging.listeners.CallbackListener;
import com.stringee.messaging.listeners.ChangeEventListenter;
import com.stringee.messaging.listeners.LiveChatEventListerner;
import com.stringee.messaging.listeners.UserTypingEventListener;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.HashMap;
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
    public void createClientWrapper(String instanceId, String baseUrl, ReadableArray addressArray) {
        StringeeClient mClient = mStringeeManager.getClientsMap().get(instanceId);
        if (mClient == null) {
            mClient = new StringeeClient(getReactApplicationContext());

            if (baseUrl != null) {
                mClient.setBaseAPIUrl(baseUrl);
            }
            if (addressArray != null) {
                List<SocketAddress> socketAddresses = new ArrayList<>();
                if (addressArray.size() > 0) {
                    for (int i = 0; i < addressArray.size(); i++) {
                        ReadableMap addressMap = addressArray.getMap(i);
                        SocketAddress socketAddress = new SocketAddress(addressMap.getString("host"), addressMap.getInt("port"));
                        socketAddresses.add(socketAddress);
                    }
                    mClient.setHost(socketAddresses);
                }
            }
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
                        data.putString("userId", stringeeClient.getUserId() != null ? stringeeClient.getUserId() : "");
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
                    StringeeClient mClient = mStringeeManager.getClientsMap().get(instanceId);
                    ArrayList<String> jsEvents = eventsMap.get(instanceId);
                    StringeeObject.Type objectType = stringeeChange.getObjectType();
                    StringeeChange.Type changeType = stringeeChange.getChangeType();
                    WritableMap data = Arguments.createMap();
                    WritableMap object = Arguments.createMap();
                    WritableMap params = Arguments.createMap();

                    params.putString("uuid", instanceId);

                    switch (objectType) {
                        case MESSAGE:
                            Message message = (Message) stringeeChange.getObject();
                            if (message.getType() == com.stringee.messaging.Message.Type.NOTIFICATION) {
                                try {
                                    Bundle msg = Utils.jsonToBundle(message.getText());
                                    if (msg.getInt("type") == 4) {
                                        if (jsEvents != null && contains(jsEvents, "onEndChatSupport")) {
                                            WritableMap msgMap = Arguments.fromBundle(msg);
                                            data.putMap("info", msgMap);
                                            params.putMap("data", data);
                                            sendEvent(getReactApplicationContext(), "onEndChatSupport", params);
                                            break;
                                        }
                                    }
                                } catch (JSONException e) {
                                    e.printStackTrace();
                                }
                            }
                            if (jsEvents != null && contains(jsEvents, "onChangeEvent")) {
                                data.putInt("objectType", objectType.getValue());
                                data.putInt("changeType", changeType.getValue());
                                WritableArray objects = Arguments.createArray();

                                object = Utils.getMessageMap(mClient, message);
                                objects.pushMap(object);
                                data.putArray("objects", objects);

                                params.putMap("data", data);
                                sendEvent(getReactApplicationContext(), "onChangeEvent", params);
                                break;
                            }
                            break;
                        case CONVERSATION:
                            if (jsEvents != null && contains(jsEvents, "onChangeEvent")) {
                                data.putInt("objectType", objectType.getValue());
                                data.putInt("changeType", changeType.getValue());
                                WritableArray objects = Arguments.createArray();

                                object = Utils.getConversationMap((Conversation) stringeeChange.getObject());
                                objects.pushMap(object);
                                data.putArray("objects", objects);

                                params.putMap("data", data);
                                sendEvent(getReactApplicationContext(), "onChangeEvent", params);
                                break;
                            }
                            break;
                    }
                }
            });
            mClient.setLiveChatEventListerner(new LiveChatEventListerner() {
                @Override
                public void onReceiveChatRequest(ChatRequest chatRequest) {
                    WritableMap params = Arguments.createMap();
                    WritableMap data = Arguments.createMap();
                    data.putMap("request", Utils.getChatRequestMap(chatRequest));

                    params.putString("uuid", instanceId);
                    params.putMap("data", data);

                    ArrayList<String> jsEvents = eventsMap.get(instanceId);
                    if (jsEvents != null && contains(jsEvents, "onReceiveChatRequest")) {
                        sendEvent(getReactApplicationContext(), "onReceiveChatRequest", params);
                    }
                }

                @Override
                public void onReceiveTransferChatRequest(ChatRequest chatRequest) {
                    WritableMap params = Arguments.createMap();
                    WritableMap data = Arguments.createMap();
                    data.putMap("request", Utils.getChatRequestMap(chatRequest));

                    params.putString("uuid", instanceId);
                    params.putMap("data", data);

                    ArrayList<String> jsEvents = eventsMap.get(instanceId);
                    if (jsEvents != null && contains(jsEvents, "onReceiveTransferChatRequest")) {
                        sendEvent(getReactApplicationContext(), "onReceiveTransferChatRequest", params);
                    }
                }

                @Override
                public void onTimeoutAnswerChat(ChatRequest chatRequest) {
                    WritableMap params = Arguments.createMap();
                    WritableMap data = Arguments.createMap();
                    data.putMap("request", Utils.getChatRequestMap(chatRequest));

                    params.putString("uuid", instanceId);
                    params.putMap("data", data);

                    ArrayList<String> jsEvents = eventsMap.get(instanceId);
                    if (jsEvents != null && contains(jsEvents, "onTimeoutAnswerChat")) {
                        sendEvent(getReactApplicationContext(), "onTimeoutAnswerChat", params);
                    }
                }

                @Override
                public void onTimeoutInQueue(Conversation conversation) {
                    StringeeClient mClient = mStringeeManager.getClientsMap().get(instanceId);
                    WritableMap params = Arguments.createMap();
                    WritableMap data = Arguments.createMap();

                    data.putString("convId", conversation.getId());
                    User user = mClient.getUser(mClient.getUserId());
                    data.putString("customerId", user.getUserId());
                    data.putString("customerName", user.getName());

                    params.putString("uuid", instanceId);
                    params.putMap("data", data);

                    ArrayList<String> jsEvents = eventsMap.get(instanceId);
                    if (jsEvents != null && contains(jsEvents, "onTimeoutInQueue")) {
                        sendEvent(getReactApplicationContext(), "onTimeoutInQueue", params);
                    }
                }

                @Override
                public void onConversationEnded(Conversation conversation, User user) {
                    WritableMap params = Arguments.createMap();
                    WritableMap data = Arguments.createMap();

                    data.putString("convId", conversation.getId());
                    data.putString("endedby", user.getUserId());

                    params.putString("uuid", instanceId);
                    params.putMap("data", data);

                    ArrayList<String> jsEvents = eventsMap.get(instanceId);
                    if (jsEvents != null && contains(jsEvents, "onConversationEnded")) {
                        sendEvent(getReactApplicationContext(), "onConversationEnded", params);
                    }
                }
            });
            mClient.setUserTypingEventListener(new UserTypingEventListener() {
                @Override
                public void onTyping(Conversation conversation, User user) {
                    WritableMap params = Arguments.createMap();
                    WritableMap data = Arguments.createMap();

                    data.putString("convId", conversation.getId());
                    data.putString("userId", user.getUserId());
                    data.putString("displayName", user.getUserId());
                    String userName = user.getName();
                    if (userName != null) {
                        if (!TextUtils.isEmpty(userName.trim())) {
                            data.putString("displayName", userName);
                        }
                    }

                    params.putString("uuid", instanceId);
                    params.putMap("data", data);

                    ArrayList<String> jsEvents = eventsMap.get(instanceId);
                    if (jsEvents != null && contains(jsEvents, "onTyping")) {
                        sendEvent(getReactApplicationContext(), "onTyping", params);
                    }
                }

                @Override
                public void onEndTyping(Conversation conversation, User user) {
                    WritableMap params = Arguments.createMap();
                    WritableMap data = Arguments.createMap();

                    data.putString("convId", conversation.getId());
                    data.putString("userId", user.getUserId());
                    data.putString("displayName", user.getUserId());
                    String userName = user.getName();
                    if (userName != null) {
                        if (!TextUtils.isEmpty(userName.trim())) {
                            data.putString("displayName", userName);
                        }
                    }

                    params.putString("uuid", instanceId);
                    params.putMap("data", data);

                    ArrayList<String> jsEvents = eventsMap.get(instanceId);
                    if (jsEvents != null && contains(jsEvents, "onEndTyping")) {
                        sendEvent(getReactApplicationContext(), "onEndTyping", params);
                    }
                }
            });
            mStringeeManager.getClientsMap().put(instanceId, mClient);
        }
    }

    @ReactMethod
    public void connect(final String instanceId, final String accessToken) {
        StringeeClient mClient = mStringeeManager.getClientsMap().get(instanceId);
        if (mClient != null) {
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
    }

    @ReactMethod
    public void disconnect(final String instanceId) {
        StringeeClient mClient = mStringeeManager.getClientsMap().get(instanceId);
        if (mClient != null) {
            mClient.disconnect();
        }
    }

    @ReactMethod
    public void registerPushToken(final String instanceId, final String token, final Callback callback) {
        StringeeClient mClient = mStringeeManager.getClientsMap().get(instanceId);
        if (mClient == null) {
            callback.invoke(false, -1, "StringeeClient is not initialized");
            return;
        }

        if (token == null) {
            callback.invoke(false, -2, "token can not be null");
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
    public void unregisterPushToken(final String instanceId, final String token, final Callback callback) {
        StringeeClient mClient = mStringeeManager.getClientsMap().get(instanceId);
        if (mClient == null) {
            callback.invoke(false, -1, "StringeeClient is not initialized");
            return;
        }

        if (token == null) {
            callback.invoke(false, -2, "token can not be null");
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
    public void sendCustomMessage(final String instanceId, final String toUser, final String msg, final Callback callback) {
        StringeeClient mClient = mStringeeManager.getClientsMap().get(instanceId);
        if (mClient == null) {
            callback.invoke(false, -1, "StringeeClient is not initialized");
            return;
        }

        if (toUser == null) {
            callback.invoke(false, -2, "toUserId can not be null");
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
    public void createConversation(final String instanceId, final ReadableArray usersArray, final ReadableMap optionsMap, final Callback callback) {
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
                WritableMap params = Utils.getConversationMap(conversation);
                callback.invoke(true, 0, "Success", params);
            }

            @Override
            public void onError(StringeeError error) {
                callback.invoke(false, error.getCode(), error.getMessage());
            }
        });
    }

    @ReactMethod
    public void getConversationById(final String instanceId, final String convId, final Callback callback) {
        StringeeClient mClient = StringeeManager.getInstance().getClientsMap().get(instanceId);
        if (mClient == null) {
            callback.invoke(false, -1, "StringeeClient is not initialized");
            return;
        }

        if (convId == null) {
            callback.invoke(false, -2, "Conversation id can not be null");
            return;
        }

        mClient.getConversationFromServer(convId, new CallbackListener<Conversation>() {
            @Override
            public void onSuccess(Conversation conversation) {
                WritableMap params = Utils.getConversationMap(conversation);
                callback.invoke(true, 0, "Success", params);
            }

            @Override
            public void onError(StringeeError error) {
                callback.invoke(false, error.getCode(), error.getMessage());
            }
        });
    }

    @ReactMethod
    public void getLocalConversations(final String instanceId, final String userId, final Callback callback) {
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
                    WritableMap param = Utils.getConversationMap(conversations.get(i));
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
    public void getLastConversations(final String instanceId, final int count, final Callback callback) {
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
                    WritableMap param = Utils.getConversationMap(conversations.get(i));
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
    public void getConversationsBefore(final String instanceId, final double datetime, final int count, final Callback callback) {
        StringeeClient mClient = StringeeManager.getInstance().getClientsMap().get(instanceId);
        if (mClient == null) {
            callback.invoke(false, -1, "StringeeClient is not initialized");
            return;
        }

        mClient.getConversationsBefore((long) datetime, count, new CallbackListener<List<Conversation>>() {
            @Override
            public void onSuccess(List<Conversation> conversations) {
                WritableArray params = Arguments.createArray();
                for (int i = 0; i < conversations.size(); i++) {
                    WritableMap param = Utils.getConversationMap(conversations.get(i));
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
    public void getConversationsAfter(final String instanceId, final double datetime, final int count, final Callback callback) {
        StringeeClient mClient = StringeeManager.getInstance().getClientsMap().get(instanceId);
        if (mClient == null) {
            callback.invoke(false, -1, "StringeeClient is not initialized");
            return;
        }

        mClient.getConversationsAfter((long) datetime, count, new CallbackListener<List<Conversation>>() {
            @Override
            public void onSuccess(List<Conversation> conversations) {
                WritableArray params = Arguments.createArray();
                for (int i = 0; i < conversations.size(); i++) {
                    WritableMap param = Utils.getConversationMap(conversations.get(i));
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
    public void deleteConversation(final String instanceId, final String convId, final Callback callback) {
        StringeeClient mClient = StringeeManager.getInstance().getClientsMap().get(instanceId);
        if (mClient == null) {
            callback.invoke(false, -1, "StringeeClient is not initialized");
            return;
        }

        if (convId == null) {
            callback.invoke(false, -2, "Conversation id can not be null");
            return;
        }

        mClient.getConversationFromServer(convId, new CallbackListener<Conversation>() {
            @Override
            public void onSuccess(final Conversation conversation) {
                if (conversation.isGroup()) {
                    if (conversation.getState() != State.LEFT) {
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
    public void addParticipants(final String instanceId, final String convId, final ReadableArray usersArray, final Callback callback) {
        StringeeClient mClient = StringeeManager.getInstance().getClientsMap().get(instanceId);
        if (mClient == null) {
            callback.invoke(false, -1, "StringeeClient is not initialized");
            return;
        }

        if (convId == null) {
            callback.invoke(false, -2, "Conversation id can not be null");
            return;
        }

        mClient.getConversationFromServer(convId, new CallbackListener<Conversation>() {
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
                            WritableMap param = Utils.getUserMap(users.get(i));
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
    public void removeParticipants(final String instanceId, final String convId, final ReadableArray usersArray, final Callback callback) {
        StringeeClient mClient = StringeeManager.getInstance().getClientsMap().get(instanceId);
        if (mClient == null) {
            callback.invoke(false, -1, "StringeeClient is not initialized");
            return;
        }

        if (convId == null) {
            callback.invoke(false, -2, "Conversation id can not be null");
            return;
        }

        mClient.getConversationFromServer(convId, new CallbackListener<Conversation>() {
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
                            WritableMap param = Utils.getUserMap(users.get(i));
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
    public void sendMessage(final String instanceId, final ReadableMap messageMap, final Callback callback) {
        StringeeClient mClient = StringeeManager.getInstance().getClientsMap().get(instanceId);
        if (mClient == null) {
            callback.invoke(false, -1, "StringeeClient is not initialized");
            return;
        }

        String convId = messageMap.getString("convId");
        final Type type = Type.getType(messageMap.getInt("type"));
        final ReadableMap msgMap = messageMap.getMap("message");

        mClient.getConversationFromServer(convId, new CallbackListener<Conversation>() {
            @Override
            public void onSuccess(Conversation conversation) {
                Message message = new Message(type);
                switch (type) {
                    case TEXT:
                    case LINK:
                        message = new Message(msgMap.getString("content"));
                        break;
                    case PHOTO:
                        ReadableMap photoMap = msgMap.getMap("photo");
                        message.setFileUrl(photoMap.getString("filePath"));
                        message.setThumbnailUrl(photoMap.getString("thumbnail"));
                        message.setImageRatio((float) photoMap.getDouble("ratio"));
                        break;
                    case VIDEO:
                        ReadableMap videoMap = msgMap.getMap("video");
                        message.setFileUrl(videoMap.getString("filePath"));
                        message.setThumbnailUrl(videoMap.getString("thumbnail"));
                        message.setImageRatio((float) videoMap.getDouble("ratio"));
                        message.setDuration(videoMap.getInt("duration"));
                        break;
                    case AUDIO:
                        ReadableMap audioMap = msgMap.getMap("audio");
                        message.setFileUrl(audioMap.getString("filePath"));
                        message.setDuration(audioMap.getInt("duration"));
                        break;
                    case FILE:
                        ReadableMap fileMap = msgMap.getMap("file");
                        message.setFileUrl(fileMap.getString("filePath"));
                        message.setFileName(fileMap.getString("filename"));
                        message.setFileLength(fileMap.getInt("length"));
                        break;
                    case LOCATION:
                        ReadableMap locationMap = msgMap.getMap("location");
                        message.setLatitude(locationMap.getDouble("lat"));
                        message.setLongitude(locationMap.getDouble("lon"));
                        break;
                    case CONTACT:
                        ReadableMap contactMap = msgMap.getMap("contact");
                        message.setContact(contactMap.getString("vcard"));
                        break;
                    case STICKER:
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
    public void getLocalMessages(final String instanceId, final String convId, final int count, final Callback callback) {
        StringeeClient mClient = StringeeManager.getInstance().getClientsMap().get(instanceId);
        if (mClient == null) {
            callback.invoke(false, -1, "StringeeClient is not initialized");
            return;
        }

        if (convId == null) {
            callback.invoke(false, -2, "Conversation id can not be null");
            return;
        }

        mClient.getConversationFromServer(convId, new CallbackListener<Conversation>() {
            @Override
            public void onSuccess(Conversation conversation) {
                conversation.getLocalMessages(mClient, count, new CallbackListener<List<Message>>() {
                    @Override
                    public void onSuccess(List<Message> messages) {
                        WritableArray params = Arguments.createArray();
                        for (int i = 0; i < messages.size(); i++) {
                            WritableMap param = Utils.getMessageMap(mClient, messages.get(i));
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
    public void getLastMessages(final String instanceId, final String convId, final int count, final boolean loadDeletedMsg, final boolean loadDeletedMsgContent, final Callback callback) {
        StringeeClient mClient = StringeeManager.getInstance().getClientsMap().get(instanceId);
        if (mClient == null) {
            callback.invoke(false, -1, "StringeeClient is not initialized");
            return;
        }

        if (convId == null) {
            callback.invoke(false, -2, "Conversation id can not be null");
            return;
        }

        mClient.getConversationFromServer(convId, new CallbackListener<Conversation>() {
            @Override
            public void onSuccess(Conversation conversation) {
                conversation.getLastMessages(mClient, count, loadDeletedMsg, loadDeletedMsgContent, false, new CallbackListener<List<Message>>() {
                    @Override
                    public void onSuccess(List<Message> messages) {
                        WritableArray params = Arguments.createArray();
                        for (int i = 0; i < messages.size(); i++) {
                            WritableMap param = Utils.getMessageMap(mClient, messages.get(i));
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
    public void getMessagesAfter(final String instanceId, final String convId, final int sequence, final int count, final boolean loadDeletedMsg, final boolean loadDeletedMsgContent, final Callback callback) {
        StringeeClient mClient = StringeeManager.getInstance().getClientsMap().get(instanceId);
        if (mClient == null) {
            callback.invoke(false, -1, "StringeeClient is not initialized");
            return;
        }

        if (convId == null) {
            callback.invoke(false, -2, "Conversation id can not be null");
            return;
        }

        mClient.getConversationFromServer(convId, new CallbackListener<Conversation>() {
            @Override
            public void onSuccess(Conversation conversation) {
                conversation.getMessagesAfter(mClient, sequence, count, loadDeletedMsg, loadDeletedMsgContent, false, new CallbackListener<List<Message>>() {
                    @Override
                    public void onSuccess(List<Message> messages) {
                        WritableArray params = Arguments.createArray();
                        for (int i = 0; i < messages.size(); i++) {
                            WritableMap param = Utils.getMessageMap(mClient, messages.get(i));
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
    public void getMessagesBefore(final String instanceId, final String convId, final int sequence, final int count, final boolean loadDeletedMsg, final boolean loadDeletedMsgContent, final Callback callback) {
        StringeeClient mClient = StringeeManager.getInstance().getClientsMap().get(instanceId);
        if (mClient == null) {
            callback.invoke(false, -1, "StringeeClient is not initialized");
            return;
        }

        if (convId == null) {
            callback.invoke(false, -2, "Conversation id can not be null");
            return;
        }

        mClient.getConversationFromServer(convId, new CallbackListener<Conversation>() {
            @Override
            public void onSuccess(Conversation conversation) {
                conversation.getMessagesBefore(mClient, sequence, count, loadDeletedMsg, loadDeletedMsgContent, false, new CallbackListener<List<Message>>() {
                    @Override
                    public void onSuccess(List<Message> messages) {
                        WritableArray params = Arguments.createArray();
                        for (int i = 0; i < messages.size(); i++) {
                            WritableMap param = Utils.getMessageMap(mClient, messages.get(i));
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
    public void deleteMessage(final String instanceId, final String convId, final String msgId, final Callback callback) {
        StringeeClient mClient = StringeeManager.getInstance().getClientsMap().get(instanceId);
        if (mClient == null) {
            callback.invoke(false, -1, "StringeeClient is not initialized");
            return;
        }

        if (convId == null) {
            callback.invoke(false, -2, "Conversation id can not be null");
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
    public void markConversationAsRead(final String instanceId, final String convId, final Callback callback) {
        StringeeClient mClient = StringeeManager.getInstance().getClientsMap().get(instanceId);
        if (mClient == null) {
            callback.invoke(false, -1, "StringeeClient is not initialized");
            return;
        }

        if (convId == null) {
            callback.invoke(false, -2, "Conversation id can not be null");
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
    public void getUser(final String instanceId, final String userId, final Callback callback) {
        StringeeClient mClient = StringeeManager.getInstance().getClientsMap().get(instanceId);
        if (mClient == null) {
            callback.invoke(false, -1, "StringeeClient is not initialized");
            return;
        }

        if (userId == null) {
            callback.invoke(false, -2, "User id can not be null");
            return;
        }

        User user = mClient.getUser(userId);
        if (user != null) {
            WritableMap param = Utils.getUserMap(user);
            callback.invoke(true, 0, "Success", param);
        } else {
            callback.invoke(false, -1, "User does not exist.");
        }
    }

    @ReactMethod
    public void clearDb(final String instanceId, final Callback callback) {
        StringeeClient mClient = StringeeManager.getInstance().getClientsMap().get(instanceId);
        if (mClient == null) {
            callback.invoke(false, -1, "StringeeClient is not initialized");
            return;
        }
        mClient.clearDb();
        callback.invoke(true, 0, "Success");
    }

    @ReactMethod
    public void updateConversation(final String instanceId, final String convId, final ReadableMap convMap, final Callback callback) {
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
        mClient.getConversationFromServer(convId, new CallbackListener<Conversation>() {
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
    public void getConversationWithUser(final String instanceId, final String userId, final Callback callback) {
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
                WritableMap params = Utils.getConversationMap(conversation);
                callback.invoke(true, 0, "Success", params);
            }

            @Override
            public void onError(StringeeError error) {
                callback.invoke(false, error.getCode(), error.getMessage());
            }
        });
    }

    @ReactMethod
    public void getUnreadConversationCount(final String instanceId, final Callback callback) {
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
    public void getLastUnreadConversations(final String instanceId, final int count, final Callback callback) {
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
                    WritableMap param = Utils.getConversationMap(conversations.get(i));
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
    public void getUnreadConversationsBefore(final String instanceId, final double datetime, final int count, final Callback callback) {
        StringeeClient mClient = StringeeManager.getInstance().getClientsMap().get(instanceId);
        if (mClient == null) {
            callback.invoke(false, -1, "StringeeClient is not initialized");
            return;
        }

        mClient.getUnreadConversationsBefore((long) datetime, count, new CallbackListener<List<Conversation>>() {
            @Override
            public void onSuccess(List<Conversation> conversations) {
                WritableArray params = Arguments.createArray();
                for (int i = 0; i < conversations.size(); i++) {
                    WritableMap param = Utils.getConversationMap(conversations.get(i));
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
    public void getUnreadConversationsAfter(final String instanceId, final double datetime, final int count, final Callback callback) {
        StringeeClient mClient = StringeeManager.getInstance().getClientsMap().get(instanceId);
        if (mClient == null) {
            callback.invoke(false, -1, "StringeeClient is not initialized");
            return;
        }

        mClient.getUnreadConversationsAfter((long) datetime, count, new CallbackListener<List<Conversation>>() {
            @Override
            public void onSuccess(List<Conversation> conversations) {
                WritableArray params = Arguments.createArray();
                for (int i = 0; i < conversations.size(); i++) {
                    WritableMap param = Utils.getConversationMap(conversations.get(i));
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
    public void getAllLastConversations(final String instanceId, final int count, final Callback callback) {
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
                    WritableMap param = Utils.getConversationMap(conversations.get(i));
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
    public void getAllConversationsBefore(final String instanceId, final double datetime, final int count, final Callback callback) {
        StringeeClient mClient = StringeeManager.getInstance().getClientsMap().get(instanceId);
        if (mClient == null) {
            callback.invoke(false, -1, "StringeeClient is not initialized");
            return;
        }

        mClient.getConversationsBefore((long) datetime, count, true, new CallbackListener<List<Conversation>>() {
            @Override
            public void onSuccess(List<Conversation> conversations) {
                WritableArray params = Arguments.createArray();
                for (int i = 0; i < conversations.size(); i++) {
                    WritableMap param = Utils.getConversationMap(conversations.get(i));
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
    public void getAllConversationsAfter(final String instanceId, final double datetime, final int count, final Callback callback) {
        StringeeClient mClient = StringeeManager.getInstance().getClientsMap().get(instanceId);
        if (mClient == null) {
            callback.invoke(false, -1, "StringeeClient is not initialized");
            return;
        }

        mClient.getConversationsAfter((long) datetime, count, true, new CallbackListener<List<Conversation>>() {
            @Override
            public void onSuccess(List<Conversation> conversations) {
                WritableArray params = Arguments.createArray();
                for (int i = 0; i < conversations.size(); i++) {
                    WritableMap param = Utils.getConversationMap(conversations.get(i));
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
    public void getAllLastMessages(final String instanceId, final String convId, final int count, final boolean loadDeletedMsg, final boolean loadDeletedMsgContent, final Callback callback) {
        StringeeClient mClient = StringeeManager.getInstance().getClientsMap().get(instanceId);
        if (mClient == null) {
            callback.invoke(false, -1, "StringeeClient is not initialized");
            return;
        }

        if (convId == null) {
            callback.invoke(false, -2, "Conversation id can not be null");
            return;
        }

        mClient.getConversationFromServer(convId, new CallbackListener<Conversation>() {
            @Override
            public void onSuccess(Conversation conversation) {
                conversation.getLastMessages(mClient, count, loadDeletedMsg, loadDeletedMsgContent, true, new CallbackListener<List<Message>>() {
                    @Override
                    public void onSuccess(List<Message> messages) {
                        WritableArray params = Arguments.createArray();
                        for (int i = 0; i < messages.size(); i++) {
                            WritableMap param = Utils.getMessageMap(mClient, messages.get(i));
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
    public void getAllMessagesAfter(final String instanceId, final String convId, final int sequence, final int count, final boolean loadDeletedMsg, final boolean loadDeletedMsgContent, final Callback callback) {
        StringeeClient mClient = StringeeManager.getInstance().getClientsMap().get(instanceId);
        if (mClient == null) {
            callback.invoke(false, -1, "StringeeClient is not initialized");
            return;
        }

        if (convId == null) {
            callback.invoke(false, -2, "Conversation id can not be null");
            return;
        }

        mClient.getConversationFromServer(convId, new CallbackListener<Conversation>() {
            @Override
            public void onSuccess(Conversation conversation) {
                conversation.getMessagesAfter(mClient, sequence, count, loadDeletedMsg, loadDeletedMsgContent, true, new CallbackListener<List<Message>>() {
                    @Override
                    public void onSuccess(List<Message> messages) {
                        WritableArray params = Arguments.createArray();
                        for (int i = 0; i < messages.size(); i++) {
                            WritableMap param = Utils.getMessageMap(mClient, messages.get(i));
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
    public void getAllMessagesBefore(final String instanceId, final String convId, final int sequence, final int count, final boolean loadDeletedMsg, final boolean loadDeletedMsgContent, final Callback callback) {
        StringeeClient mClient = StringeeManager.getInstance().getClientsMap().get(instanceId);
        if (mClient == null) {
            callback.invoke(false, -1, "StringeeClient is not initialized");
            return;
        }

        if (convId == null) {
            callback.invoke(false, -2, "Conversation id can not be null");
            return;
        }

        mClient.getConversationFromServer(convId, new CallbackListener<Conversation>() {
            @Override
            public void onSuccess(Conversation conversation) {
                conversation.getMessagesBefore(mClient, sequence, count, loadDeletedMsg, loadDeletedMsgContent, true, new CallbackListener<List<Message>>() {
                    @Override
                    public void onSuccess(List<Message> messages) {
                        WritableArray params = Arguments.createArray();
                        for (int i = 0; i < messages.size(); i++) {
                            WritableMap param = Utils.getMessageMap(mClient, messages.get(i));
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
    public void getChatProfile(final String instanceId, final String widgetKey, final Callback callback) {
        StringeeClient mClient = StringeeManager.getInstance().getClientsMap().get(instanceId);
        if (mClient == null) {
            callback.invoke(false, -1, "StringeeClient is not initialized");
            return;
        }

        if (widgetKey == null) {
            callback.invoke(false, -2, "Widget key can not be null");
            return;
        }

        mClient.getChatProfile(widgetKey, new CallbackListener<ChatProfile>() {
            @Override
            public void onSuccess(ChatProfile chatProfile) {
                WritableMap params = Utils.getChatProfileMap(chatProfile);
                callback.invoke(true, 0, "Success", params);
            }

            @Override
            public void onError(StringeeError stringeeError) {
                super.onError(stringeeError);
                callback.invoke(false, stringeeError.getCode(), stringeeError.getMessage());
            }
        });
    }

    @ReactMethod
    public void getLiveChatToken(final String instanceId, final String widgetKey, final String name, final String email, final Callback callback) {
        StringeeClient mClient = StringeeManager.getInstance().getClientsMap().get(instanceId);
        if (mClient == null) {
            callback.invoke(false, -1, "StringeeClient is not initialized");
            return;
        }

        if (widgetKey == null) {
            callback.invoke(false, -2, "Widget key can not be null");
            return;
        }

        mClient.getLiveChatToken(widgetKey, name, email, new CallbackListener<String>() {
            @Override
            public void onSuccess(String token) {
                callback.invoke(true, 0, "Success", token);
            }

            @Override
            public void onError(StringeeError stringeeError) {
                super.onError(stringeeError);
                callback.invoke(false, stringeeError.getCode(), stringeeError.getMessage());
            }
        });
    }

    @ReactMethod
    public void updateUserInfo(final String instanceId, final String name, final String email, final String avatar, final Callback callback) {
        StringeeClient mClient = StringeeManager.getInstance().getClientsMap().get(instanceId);
        if (mClient == null) {
            callback.invoke(false, -1, "StringeeClient is not initialized");
            return;
        }

        mClient.updateUser(name, email, avatar, new StatusListener() {
            @Override
            public void onSuccess() {
                callback.invoke(true, 0, "Success");
            }

            @Override
            public void onError(StringeeError stringeeError) {
                super.onError(stringeeError);
                callback.invoke(false, stringeeError.getCode(), stringeeError.getMessage());
            }
        });
    }

    @ReactMethod
    public void createLiveChatConversation(final String instanceId, final String queueId, final Callback callback) {
        StringeeClient mClient = StringeeManager.getInstance().getClientsMap().get(instanceId);
        if (mClient == null) {
            callback.invoke(false, -1, "StringeeClient is not initialized");
            return;
        }

        if (queueId == null) {
            callback.invoke(false, -2, "Queue id can not be null");
            return;
        }

        mClient.createLiveChat(queueId, new CallbackListener<Conversation>() {
            @Override
            public void onSuccess(Conversation conversation) {
                WritableMap params = Utils.getConversationMap(conversation);
                callback.invoke(true, 0, "Success", params);
            }

            @Override
            public void onError(StringeeError stringeeError) {
                super.onError(stringeeError);
                callback.invoke(false, stringeeError.getCode(), stringeeError.getMessage());
            }
        });
    }

    @ReactMethod
    public void acceptChatRequest(final String instanceId, final String convId, final Callback callback) {
        StringeeClient mClient = StringeeManager.getInstance().getClientsMap().get(instanceId);
        if (mClient == null) {
            callback.invoke(false, -1, "StringeeClient is not initialized");
            return;
        }

        if (convId == null) {
            callback.invoke(false, -2, "Conversation id can not be null");
            return;
        }

        mClient.getChatRequests(new CallbackListener<List<ChatRequest>>() {
            @Override
            public void onSuccess(List<ChatRequest> chatRequestList) {
                for (int i = 0; i < chatRequestList.size(); i++) {
                    ChatRequest chatRequest = chatRequestList.get(i);
                    if (convId.equals(chatRequest.getConvId())) {
                        chatRequest.accept(mClient, new CallbackListener<Conversation>() {
                            @Override
                            public void onSuccess(Conversation conversation) {
                                callback.invoke(true, 0, "Success");
                            }

                            @Override
                            public void onError(StringeeError stringeeError) {
                                super.onError(stringeeError);
                                callback.invoke(false, stringeeError.getCode(), stringeeError.getMessage());
                            }
                        });
                        break;
                    }
                }
            }

            @Override
            public void onError(StringeeError stringeeError) {
                super.onError(stringeeError);
                callback.invoke(false, stringeeError.getCode(), stringeeError.getMessage());
            }
        });
    }

    @ReactMethod
    public void rejectChatRequest(final String instanceId, final String convId, final Callback callback) {
        StringeeClient mClient = StringeeManager.getInstance().getClientsMap().get(instanceId);
        if (mClient == null) {
            callback.invoke(false, -1, "StringeeClient is not initialized");
            return;
        }

        if (convId == null) {
            callback.invoke(false, -2, "Conversation id can not be null");
            return;
        }

        mClient.getChatRequests(new CallbackListener<List<ChatRequest>>() {
            @Override
            public void onSuccess(List<ChatRequest> chatRequestList) {
                for (int i = 0; i < chatRequestList.size(); i++) {
                    ChatRequest chatRequest = chatRequestList.get(i);
                    if (convId.equals(chatRequest.getConvId())) {
                        chatRequest.reject(mClient, new StatusListener() {
                            @Override
                            public void onSuccess() {
                                callback.invoke(true, 0, "Success");
                            }

                            @Override
                            public void onError(StringeeError stringeeError) {
                                super.onError(stringeeError);
                                callback.invoke(false, stringeeError.getCode(), stringeeError.getMessage());
                            }
                        });
                        break;
                    }
                }
            }

            @Override
            public void onError(StringeeError stringeeError) {
                super.onError(stringeeError);
                callback.invoke(false, stringeeError.getCode(), stringeeError.getMessage());
            }
        });
    }

    @ReactMethod
    public void createLiveChatTicket(final String instanceId, final String widgetKey, final String name, final String email, final String note, final Callback callback) {
        StringeeClient mClient = StringeeManager.getInstance().getClientsMap().get(instanceId);
        if (mClient == null) {
            callback.invoke(false, -1, "StringeeClient is not initialized");
            return;
        }

        if (widgetKey == null) {
            callback.invoke(false, -2, "Widget key is not initialized");
            return;
        }

        mClient.createLiveChatTicket(widgetKey, name, email, note, new StatusListener() {
            @Override
            public void onSuccess() {
                callback.invoke(true, 0, "Success");
            }

            @Override
            public void onError(StringeeError stringeeError) {
                super.onError(stringeeError);
                callback.invoke(false, stringeeError.getCode(), stringeeError.getMessage());
            }
        });
    }

    @ReactMethod
    public void sendChatTranscript(final String instanceId, final String email, final String convId, final String domain, final Callback callback) {
        StringeeClient mClient = StringeeManager.getInstance().getClientsMap().get(instanceId);
        if (mClient == null) {
            callback.invoke(false, -1, "StringeeClient is not initialized");
            return;
        }

        if (convId == null) {
            callback.invoke(false, -2, "Conversation id is not initialized");
            return;
        }

        mClient.getConversationFromServer(convId, new CallbackListener<Conversation>() {
            @Override
            public void onSuccess(Conversation conversation) {
                conversation.sendChatTranscriptTo(mClient, email, domain, new StatusListener() {
                    @Override
                    public void onSuccess() {
                        callback.invoke(true, 0, "Success");
                    }

                    @Override
                    public void onError(StringeeError stringeeError) {
                        super.onError(stringeeError);
                        callback.invoke(false, stringeeError.getCode(), stringeeError.getMessage());
                    }
                });
            }

            @Override
            public void onError(StringeeError stringeeError) {
                super.onError(stringeeError);
                callback.invoke(false, stringeeError.getCode(), stringeeError.getMessage());
            }
        });
    }

    @ReactMethod
    public void endChat(final String instanceId, final String convId, final Callback callback) {
        StringeeClient mClient = StringeeManager.getInstance().getClientsMap().get(instanceId);
        if (mClient == null) {
            callback.invoke(false, -1, "StringeeClient is not initialized");
            return;
        }

        if (convId == null) {
            callback.invoke(false, -2, "Conversation id is not initialized");
            return;
        }

        mClient.getConversationFromServer(convId, new CallbackListener<Conversation>() {
            @Override
            public void onSuccess(Conversation conversation) {
                conversation.endChat(mClient, new StatusListener() {
                    @Override
                    public void onSuccess() {
                        callback.invoke(true, 0, "Success");
                    }

                    @Override
                    public void onError(StringeeError stringeeError) {
                        super.onError(stringeeError);
                        callback.invoke(false, stringeeError.getCode(), stringeeError.getMessage());
                    }
                });
            }

            @Override
            public void onError(StringeeError stringeeError) {
                super.onError(stringeeError);
                callback.invoke(false, stringeeError.getCode(), stringeeError.getMessage());
            }
        });
    }

    @ReactMethod
    public void sendBeginTyping(final String instanceId, final String convId, final Callback callback) {
        StringeeClient mClient = StringeeManager.getInstance().getClientsMap().get(instanceId);
        if (mClient == null) {
            callback.invoke(false, -1, "StringeeClient is not initialized");
            return;
        }

        if (convId == null) {
            callback.invoke(false, -2, "Conversation id is not initialized");
            return;
        }

        mClient.getConversationFromServer(convId, new CallbackListener<Conversation>() {
            @Override
            public void onSuccess(Conversation conversation) {
                conversation.beginTyping(mClient, new StatusListener() {
                    @Override
                    public void onSuccess() {
                        callback.invoke(true, 0, "Success");
                    }

                    @Override
                    public void onError(StringeeError stringeeError) {
                        super.onError(stringeeError);
                        callback.invoke(false, stringeeError.getCode(), stringeeError.getMessage());
                    }
                });
            }

            @Override
            public void onError(StringeeError stringeeError) {
                super.onError(stringeeError);
                callback.invoke(false, stringeeError.getCode(), stringeeError.getMessage());
            }
        });
    }

    @ReactMethod
    public void sendEndTyping(final String instanceId, final String convId, final Callback callback) {
        StringeeClient mClient = StringeeManager.getInstance().getClientsMap().get(instanceId);
        if (mClient == null) {
            callback.invoke(false, -1, "StringeeClient is not initialized");
            return;
        }

        if (convId == null) {
            callback.invoke(false, -2, "Conversation id is not initialized");
            return;
        }

        mClient.getConversationFromServer(convId, new CallbackListener<Conversation>() {
            @Override
            public void onSuccess(Conversation conversation) {
                conversation.endTyping(mClient, new StatusListener() {
                    @Override
                    public void onSuccess() {
                        callback.invoke(true, 0, "Success");
                    }

                    @Override
                    public void onError(StringeeError stringeeError) {
                        super.onError(stringeeError);
                        callback.invoke(false, stringeeError.getCode(), stringeeError.getMessage());
                    }
                });
            }

            @Override
            public void onError(StringeeError stringeeError) {
                super.onError(stringeeError);
                callback.invoke(false, stringeeError.getCode(), stringeeError.getMessage());
            }
        });
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
