package com.stringeereactnative;

import com.facebook.react.bridge.Arguments;
import com.facebook.react.bridge.Callback;
import com.facebook.react.bridge.ReactApplicationContext;
import com.facebook.react.bridge.WritableArray;
import com.facebook.react.bridge.WritableMap;
import com.stringee.StringeeClient;
import com.stringee.call.StringeeCall;
import com.stringee.call.StringeeCall2;
import com.stringee.common.SocketAddress;
import com.stringee.exception.StringeeError;
import com.stringee.listener.StatusListener;
import com.stringee.listener.StringeeConnectionListener;
import com.stringee.messaging.ChatRequest;
import com.stringee.messaging.ChatRequest.State;
import com.stringee.messaging.Conversation;
import com.stringee.messaging.Message;
import com.stringee.messaging.StringeeChange;
import com.stringee.messaging.StringeeObject.Type;
import com.stringee.messaging.User;
import com.stringee.messaging.listeners.ChangeEventListener;
import com.stringee.messaging.listeners.LiveChatEventListener;
import com.stringee.messaging.listeners.UserTypingEventListener;
import com.stringeereactnative.call.Call2Wrapper;
import com.stringeereactnative.call.CallWrapper;
import com.stringeereactnative.chat.ChatWrapper;
import com.stringeereactnative.common.StringeeManager;
import com.stringeereactnative.common.Utils;
import com.stringeereactnative.conference.ConferenceWrapper;
import com.stringeereactnative.conference.ScreenCaptureManager;

import org.json.JSONObject;

import java.util.ArrayList;
import java.util.List;

public class ClientWrapper implements StringeeConnectionListener, ChangeEventListener, LiveChatEventListener, UserTypingEventListener {
    private StringeeClient client;
    private ArrayList<String> jsEvents;
    private ReactApplicationContext context;
    private String instanceId;
    private StringeeManager stringeeManager;
    private ConferenceWrapper conferenceWrapper;
    private ChatWrapper chatWrapper;

    public ClientWrapper(String instanceId, ReactApplicationContext context) {
        this.context = context;
        this.instanceId = instanceId;
        this.client = new StringeeClient(context);
        this.client.setConnectionListener(this);
        this.client.setChangeEventListener(this);
        this.client.setLiveChatEventListener(this);
        this.client.setUserTypingEventListener(this);
        this.jsEvents = new ArrayList<>();
        this.stringeeManager = StringeeManager.getInstance();
        this.conferenceWrapper = new ConferenceWrapper(this);
        this.chatWrapper = new ChatWrapper(this);
        this.stringeeManager.setCaptureManager(ScreenCaptureManager.getInstance(context));
    }

    public StringeeClient getClient() {
        return client;
    }

    public String getClientId() {
        return instanceId;
    }

    public boolean isConnected() {
        return client.isConnected();
    }

    public String getUserId(){
        return client.getUserId();
    }

    public ConferenceWrapper getConferenceWrapper() {
        return conferenceWrapper;
    }

    public ChatWrapper getChatWrapper() {
        return chatWrapper;
    }

    public ReactApplicationContext getContext() {
        return context;
    }

    public void setBaseAPIUrl(String baseUrl) {
        client.setBaseAPIUrl(baseUrl);
    }

    public void setStringeeXBaseUrl(String stringeeXBaseUrl) {
        client.setStringeeXBaseUrl(stringeeXBaseUrl);
    }

    public void setHost(List<SocketAddress> socketAddresses) {
        client.setHost(socketAddresses);
    }

    public void connect(String accessToken) {
        if (client.isConnected()) {
            if (jsEvents != null && Utils.contains(jsEvents, "onConnectionConnected")) {
                WritableMap data = Arguments.createMap();
                data.putString("userId", client.getUserId());
                data.putInt("projectId", client.getProjectId());
                data.putBoolean("isReconnecting", false);

                WritableMap params = Arguments.createMap();
                params.putString("uuid", instanceId);
                params.putMap("data", data);
                Utils.sendEvent(context, "onConnectionConnected", params);
            }
        } else {
            client.connect(accessToken);
        }
    }

    public void disconnect() {
        client.disconnect();
    }

    public void registerPushToken(String token, Callback callback) {
        client.registerPushToken(token, new StatusListener() {
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

    public void registerPushTokenAndDeleteOthers(String token, List<String> packages, Callback callback) {
        client.registerPushTokenAndDeleteOthers(token, packages, new StatusListener() {
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

    public void unregisterPushToken(String token, Callback callback) {
        client.unregisterPushToken(token, new StatusListener() {
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

    public void sendCustomMessage(String toUser, JSONObject msg, Callback callback) {
        client.sendCustomMessage(toUser, msg, new StatusListener() {
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

    public void clearDb(final Callback callback) {
        client.clearDb();
        callback.invoke(true, 0, "Success");
    }

    public void getUser(String userId, Callback callback) {
        User user = client.getUser(userId);
        if (user != null) {
            WritableMap param = Utils.getUserMap(user);
            callback.invoke(true, 0, "Success", param);
        } else {
            callback.invoke(false, -1, "User does not exist.");
        }
    }

    public void setNativeEvent(String event) {
        if (jsEvents == null) {
            jsEvents = new ArrayList<>();
            jsEvents.add(event);
        } else {
            jsEvents.add(event);
        }
    }

    public void removeNativeEvent(String event) {
        if (jsEvents != null) {
            jsEvents.remove(event);
        }
    }

    @Override
    public void onConnectionConnected(StringeeClient stringeeClient, boolean b) {
        if (jsEvents != null && Utils.contains(jsEvents, "onConnectionConnected")) {
            WritableMap data = Arguments.createMap();
            data.putString("userId", stringeeClient.getUserId());
            data.putInt("projectId", stringeeClient.getProjectId());
            data.putBoolean("isReconnecting", b);
            WritableMap params = Arguments.createMap();
            params.putString("uuid", instanceId);
            params.putMap("data", data);
            Utils.sendEvent(context, "onConnectionConnected", params);
        }
    }

    @Override
    public void onConnectionDisconnected(StringeeClient stringeeClient, boolean b) {
        if (jsEvents != null && Utils.contains(jsEvents, "onConnectionDisconnected")) {
            WritableMap data = Arguments.createMap();
            data.putString("userId", stringeeClient.getUserId() != null ? stringeeClient.getUserId() : "");
            data.putInt("projectId", stringeeClient.getProjectId());
            data.putBoolean("isReconnecting", b);
            WritableMap params = Arguments.createMap();
            params.putString("uuid", instanceId);
            params.putMap("data", data);
            Utils.sendEvent(context, "onConnectionDisconnected", params);
        }
    }

    @Override
    public void onIncomingCall(StringeeCall stringeeCall) {
        if (jsEvents != null && Utils.contains(jsEvents, "onIncomingCall")) {
            stringeeManager.getCallWrapperMap().put(stringeeCall.getCallId(), new CallWrapper(context, stringeeCall));
            WritableMap data = Arguments.createMap();
            data.putString("userId", client.getUserId());
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
            Utils.sendEvent(context, "onIncomingCall", params);
        }
    }

    @Override
    public void onIncomingCall2(StringeeCall2 stringeeCall2) {
        if (jsEvents != null && Utils.contains(jsEvents, "onIncomingCall2")) {
            stringeeManager.getCall2WrapperMap().put(stringeeCall2.getCallId(), new Call2Wrapper(context, stringeeCall2));
            WritableMap data = Arguments.createMap();
            data.putString("userId", client.getUserId());
            data.putString("callId", stringeeCall2.getCallId());
            data.putString("from", stringeeCall2.getFrom());
            data.putString("to", stringeeCall2.getTo());
            data.putString("fromAlias", stringeeCall2.getFromAlias());
            data.putString("toAlias", stringeeCall2.getToAlias());
            int callType = 1;
            data.putInt("callType", callType);
            data.putBoolean("isVideoCall", stringeeCall2.isVideoCall());
            data.putString("customDataFromYourServer", stringeeCall2.getCustomDataFromYourServer());

            WritableMap params = Arguments.createMap();
            params.putString("uuid", instanceId);
            params.putMap("data", data);
            Utils.sendEvent(context, "onIncomingCall2", params);
        }
    }

    @Override
    public void onConnectionError(StringeeClient stringeeClient, StringeeError stringeeError) {
        if (jsEvents != null && Utils.contains(jsEvents, "onConnectionError")) {
            WritableMap data = Arguments.createMap();
            data.putInt("code", stringeeError.getCode());
            data.putString("message", stringeeError.getMessage());

            WritableMap params = Arguments.createMap();
            params.putString("uuid", instanceId);
            params.putMap("data", data);
            Utils.sendEvent(context, "onConnectionError", params);
        }
    }

    @Override
    public void onRequestNewToken(StringeeClient stringeeClient) {
        if (jsEvents != null && Utils.contains(jsEvents, "onRequestNewToken")) {
            WritableMap params = Arguments.createMap();
            params.putString("uuid", instanceId);
            Utils.sendEvent(context, "onRequestNewToken", params);
        }
    }

    @Override
    public void onCustomMessage(String from, JSONObject jsonObject) {
        if (jsEvents != null && Utils.contains(jsEvents, "onCustomMessage")) {
            WritableMap data = Arguments.createMap();
            data.putString("from", from);
            data.putString("data", jsonObject.toString());

            WritableMap params = Arguments.createMap();
            params.putString("uuid", instanceId);
            params.putMap("data", data);
            Utils.sendEvent(context, "onCustomMessage", params);
        }
    }

    @Override
    public void onTopicMessage(String from, JSONObject jsonObject) {
        if (jsEvents != null && Utils.contains(jsEvents, "onCustomMessage")) {
            WritableMap data = Arguments.createMap();
            data.putString("from", from);
            data.putString("data", jsonObject.toString());

            WritableMap params = Arguments.createMap();
            params.putString("uuid", instanceId);
            params.putMap("data", data);
            Utils.sendEvent(context, "onTopicMessage", params);
        }
    }

    @Override
    public void onChangeEvent(StringeeChange stringeeChange) {
        Type objectType = stringeeChange.getObjectType();
        StringeeChange.Type changeType = stringeeChange.getChangeType();
        WritableMap data = Arguments.createMap();
        WritableMap object = Arguments.createMap();
        WritableMap params = Arguments.createMap();

        params.putString("uuid", instanceId);

        switch (objectType) {
            case MESSAGE:
                if (jsEvents != null && Utils.contains(jsEvents, "onChangeEvent")) {
                    data.putInt("objectType", objectType.getValue());
                    data.putInt("changeType", changeType.getValue());
                    WritableArray objects = Arguments.createArray();

                    object = Utils.getMessageMap((Message) stringeeChange.getObject());
                    objects.pushMap(object);
                    data.putArray("objects", objects);

                    params.putMap("data", data);
                    Utils.sendEvent(context, "onChangeEvent", params);
                    break;
                }
                break;
            case CONVERSATION:
                if (jsEvents != null && Utils.contains(jsEvents, "onChangeEvent")) {
                    data.putInt("objectType", objectType.getValue());
                    data.putInt("changeType", changeType.getValue());
                    WritableArray objects = Arguments.createArray();

                    object = Utils.getConversationMap((Conversation) stringeeChange.getObject());
                    objects.pushMap(object);
                    data.putArray("objects", objects);

                    params.putMap("data", data);
                    Utils.sendEvent(context, "onChangeEvent", params);
                    break;
                }
                break;
        }
    }

    @Override
    public void onReceiveChatRequest(ChatRequest chatRequest) {
        WritableMap params = Arguments.createMap();
        WritableMap data = Arguments.createMap();
        data.putMap("request", Utils.getChatRequestMap(chatRequest));

        params.putString("uuid", instanceId);
        params.putMap("data", data);

        if (jsEvents != null && Utils.contains(jsEvents, "onReceiveChatRequest")) {
            Utils.sendEvent(context, "onReceiveChatRequest", params);
        }
    }

    @Override
    public void onReceiveTransferChatRequest(ChatRequest chatRequest) {
        WritableMap params = Arguments.createMap();
        WritableMap data = Arguments.createMap();
        data.putMap("request", Utils.getChatRequestMap(chatRequest));

        params.putString("uuid", instanceId);
        params.putMap("data", data);

        if (jsEvents != null && Utils.contains(jsEvents, "onReceiveTransferChatRequest")) {
            Utils.sendEvent(context, "onReceiveTransferChatRequest", params);
        }
    }

    @Override
    public void onHandleOnAnotherDevice(ChatRequest chatRequest, State state) {

    }

    @Override
    public void onTimeoutAnswerChat(ChatRequest chatRequest) {
        WritableMap params = Arguments.createMap();
        WritableMap data = Arguments.createMap();
        data.putMap("request", Utils.getChatRequestMap(chatRequest));

        params.putString("uuid", instanceId);
        params.putMap("data", data);

        if (jsEvents != null && Utils.contains(jsEvents, "onTimeoutAnswerChat")) {
            Utils.sendEvent(context, "onTimeoutAnswerChat", params);
        }
    }

    @Override
    public void onTimeoutInQueue(Conversation conversation) {
        WritableMap params = Arguments.createMap();
        WritableMap data = Arguments.createMap();

        data.putString("convId", conversation.getId());
        User user = client.getUser(client.getUserId());
        data.putString("customerId", user.getUserId());
        data.putString("customerName", user.getName());

        params.putString("uuid", instanceId);
        params.putMap("data", data);

        if (jsEvents != null && Utils.contains(jsEvents, "onTimeoutInQueue")) {
            Utils.sendEvent(context, "onTimeoutInQueue", params);
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

        if (jsEvents != null && Utils.contains(jsEvents, "onConversationEnded")) {
            Utils.sendEvent(context, "onConversationEnded", params);
        }
    }

    @Override
    public void onTyping(Conversation conversation, User user) {
        WritableMap params = Arguments.createMap();
        WritableMap data = Arguments.createMap();

        data.putString("convId", conversation.getId());
        data.putString("userId", user.getUserId());
        data.putString("displayName", user.getUserId());
        String userName = user.getName();
        if (userName != null) {
            if (!android.text.TextUtils.isEmpty(userName.trim())) {
                data.putString("displayName", userName);
            }
        }

        params.putString("uuid", instanceId);
        params.putMap("data", data);

        if (jsEvents != null && Utils.contains(jsEvents, "onTyping")) {
            Utils.sendEvent(context, "onTyping", params);
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
            if (!android.text.TextUtils.isEmpty(userName.trim())) {
                data.putString("displayName", userName);
            }
        }

        params.putString("uuid", instanceId);
        params.putMap("data", data);

        if (jsEvents != null && Utils.contains(jsEvents, "onEndTyping")) {
            Utils.sendEvent(context, "onEndTyping", params);
        }
    }
}
