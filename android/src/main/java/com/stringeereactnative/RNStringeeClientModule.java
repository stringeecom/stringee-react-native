package com.stringeereactnative;

import com.facebook.react.bridge.Callback;
import com.facebook.react.bridge.ReactApplicationContext;
import com.facebook.react.bridge.ReactContextBaseJavaModule;
import com.facebook.react.bridge.ReactMethod;
import com.facebook.react.bridge.ReadableArray;
import com.facebook.react.bridge.ReadableMap;
import com.stringee.common.SocketAddress;
import com.stringee.messaging.ConversationOptions;
import com.stringee.messaging.Message;
import com.stringee.messaging.Message.Type;
import com.stringee.messaging.User;
import com.stringeereactnative.common.StringeeManager;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.List;

public class RNStringeeClientModule extends ReactContextBaseJavaModule {
    private StringeeManager stringeeManager;
    private ReactApplicationContext mContext;

    public RNStringeeClientModule(ReactApplicationContext context) {
        super(context);
        mContext = context;
        stringeeManager = StringeeManager.getInstance();
    }

    @Override
    public String getName() {
        return "RNStringeeClient";
    }

    @ReactMethod
    public void createClientWrapper(String instanceId, String baseUrl, ReadableArray addressArray, String stringeeXBaseUrl) {
        ClientWrapper clientWrapper = stringeeManager.getClientWrapperMap().get(instanceId);
        if (clientWrapper == null) {
            clientWrapper = new ClientWrapper(instanceId, mContext);

            if (baseUrl != null) {
                clientWrapper.setBaseAPIUrl(baseUrl);
            }
            if (stringeeXBaseUrl != null) {
                clientWrapper.setStringeeXBaseUrl(stringeeXBaseUrl);
            }
            if (addressArray != null) {
                List<SocketAddress> socketAddresses = new ArrayList<>();
                if (addressArray.size() > 0) {
                    for (int i = 0; i < addressArray.size(); i++) {
                        ReadableMap addressMap = addressArray.getMap(i);
                        SocketAddress socketAddress = new SocketAddress(addressMap.getString("host"), addressMap.getInt("port"));
                        socketAddresses.add(socketAddress);
                    }
                    clientWrapper.setHost(socketAddresses);
                }
            }
            stringeeManager.getClientWrapperMap().put(instanceId, clientWrapper);
        }
    }

    @ReactMethod
    public void connect(final String instanceId, final String accessToken) {
        ClientWrapper clientWrapper = stringeeManager.getClientWrapperMap().get(instanceId);
        if (clientWrapper != null) {
            clientWrapper.connect(accessToken);
        }
    }

    @ReactMethod
    public void disconnect(final String instanceId) {
        ClientWrapper clientWrapper = stringeeManager.getClientWrapperMap().get(instanceId);
        if (clientWrapper != null) {
            clientWrapper.disconnect();
        }
    }

    @ReactMethod
    public void registerPushToken(final String instanceId, final String token, final Callback callback) {
        ClientWrapper clientWrapper = stringeeManager.getClientWrapperMap().get(instanceId);
        if (clientWrapper == null) {
            callback.invoke(false, -1, "StringeeClient is not initialized");
            return;
        }

        if (token == null) {
            callback.invoke(false, -2, "token can not be null");
            return;
        }

        clientWrapper.registerPushToken(token, callback);
    }

    @ReactMethod
    public void registerPushAndDeleteOthers(final String instanceId, final String token, final ReadableArray packagesArray, final Callback callback) {
        ClientWrapper clientWrapper = stringeeManager.getClientWrapperMap().get(instanceId);
        if (clientWrapper == null) {
            callback.invoke(false, -1, "StringeeClient is not initialized");
            return;
        }

        if (token == null) {
            callback.invoke(false, -2, "token can not be null");
            return;
        }

        List<String> packages = null;
        if (packagesArray != null) {
            if (packagesArray.size() > 0) {
                packages = new ArrayList<>();
                for (int i = 0; i < packagesArray.size(); i++) {
                    packages.add(packagesArray.getString(i));
                }
            }
        }

        clientWrapper.registerPushTokenAndDeleteOthers(token, packages, callback);
    }

    @ReactMethod
    public void unregisterPushToken(final String instanceId, final String token, final Callback callback) {
        ClientWrapper clientWrapper = stringeeManager.getClientWrapperMap().get(instanceId);
        if (clientWrapper == null) {
            callback.invoke(false, -1, "StringeeClient is not initialized");
            return;
        }

        if (token == null) {
            callback.invoke(false, -2, "token can not be null");
            return;
        }

        clientWrapper.unregisterPushToken(token, callback);
    }

    @ReactMethod
    public void sendCustomMessage(final String instanceId, final String toUser, final String msg, final Callback callback) {
        ClientWrapper clientWrapper = stringeeManager.getClientWrapperMap().get(instanceId);
        if (clientWrapper == null) {
            callback.invoke(false, -1, "StringeeClient is not initialized");
            return;
        }

        if (toUser == null) {
            callback.invoke(false, -2, "toUserId can not be null");
            return;
        }

        try {
            JSONObject jsonObject = new JSONObject(msg);
            clientWrapper.sendCustomMessage(toUser, jsonObject, callback);
        } catch (JSONException e) {
            e.printStackTrace();
            callback.invoke(false, -2, "Message is not not in JSON format");
        }
    }

    @ReactMethod
    public void createConversation(final String instanceId, final ReadableArray usersArray, final ReadableMap optionsMap, final Callback callback) {
        ClientWrapper clientWrapper = stringeeManager.getClientWrapperMap().get(instanceId);
        if (clientWrapper == null) {
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

        clientWrapper.getChatWrapper().createConversation(participants, convOptions, callback);
    }

    @ReactMethod
    public void getConversationById(final String instanceId, final String convId, final Callback callback) {
        ClientWrapper clientWrapper = stringeeManager.getClientWrapperMap().get(instanceId);
        if (clientWrapper == null) {
            callback.invoke(false, -1, "StringeeClient is not initialized");
            return;
        }

        if (convId == null) {
            callback.invoke(false, -2, "Conversation id can not be null");
            return;
        }

        clientWrapper.getChatWrapper().getConversationById(convId, callback);
    }

    @ReactMethod
    public void getLocalConversations(final String instanceId, final String userId, final Callback callback) {
        ClientWrapper clientWrapper = stringeeManager.getClientWrapperMap().get(instanceId);
        if (clientWrapper == null) {
            callback.invoke(false, -1, "StringeeClient is not initialized");
            return;
        }

        if (userId == null) {
            callback.invoke(false, -2, "User id can not be null");
            return;
        }

        clientWrapper.getChatWrapper().getLocalConversations(userId, callback);
    }

    @ReactMethod
    public void getLastConversations(final String instanceId, final int count, final Callback callback) {
        ClientWrapper clientWrapper = stringeeManager.getClientWrapperMap().get(instanceId);
        if (clientWrapper == null) {
            callback.invoke(false, -1, "StringeeClient is not initialized");
            return;
        }

        clientWrapper.getChatWrapper().getLastConversations(count, callback);
    }

    @ReactMethod
    public void getConversationsBefore(final String instanceId, final double datetime, final int count, final Callback callback) {
        ClientWrapper clientWrapper = stringeeManager.getClientWrapperMap().get(instanceId);
        if (clientWrapper == null) {
            callback.invoke(false, -1, "StringeeClient is not initialized");
            return;
        }

        clientWrapper.getChatWrapper().getConversationsBefore((long) datetime, count, callback);
    }

    @ReactMethod
    public void getConversationsAfter(final String instanceId, final double datetime, final int count, final Callback callback) {
        ClientWrapper clientWrapper = stringeeManager.getClientWrapperMap().get(instanceId);
        if (clientWrapper == null) {
            callback.invoke(false, -1, "StringeeClient is not initialized");
            return;
        }

        clientWrapper.getChatWrapper().getConversationsAfter((long) datetime, count, callback);
    }

    @ReactMethod
    public void deleteConversation(final String instanceId, final String convId, final Callback callback) {
        ClientWrapper clientWrapper = stringeeManager.getClientWrapperMap().get(instanceId);
        if (clientWrapper == null) {
            callback.invoke(false, -1, "StringeeClient is not initialized");
            return;
        }

        if (convId == null) {
            callback.invoke(false, -2, "Conversation id can not be null");
            return;
        }

        clientWrapper.getChatWrapper().deleteConversation(convId, callback);
    }

    @ReactMethod
    public void addParticipants(final String instanceId, final String convId, final ReadableArray usersArray, final Callback callback) {
        ClientWrapper clientWrapper = stringeeManager.getClientWrapperMap().get(instanceId);
        if (clientWrapper == null) {
            callback.invoke(false, -1, "StringeeClient is not initialized");
            return;
        }

        if (convId == null) {
            callback.invoke(false, -2, "Conversation id can not be null");
            return;
        }

        List<User> users = new ArrayList<>();
        for (int i = 0; i < usersArray.size(); i++) {
            User user = new User(usersArray.getString(i));
            users.add(user);
        }

        clientWrapper.getChatWrapper().addParticipants(convId, users, callback);
    }

    @ReactMethod
    public void removeParticipants(final String instanceId, final String convId, final ReadableArray usersArray, final Callback callback) {
        ClientWrapper clientWrapper = stringeeManager.getClientWrapperMap().get(instanceId);
        if (clientWrapper == null) {
            callback.invoke(false, -1, "StringeeClient is not initialized");
            return;
        }

        if (convId == null) {
            callback.invoke(false, -2, "Conversation id can not be null");
            return;
        }

        List<User> users = new ArrayList<>();
        for (int i = 0; i < usersArray.size(); i++) {
            User user = new User(usersArray.getString(i));
            users.add(user);
        }

        clientWrapper.getChatWrapper().removeParticipants(convId, users, callback);
    }

    @ReactMethod
    public void sendMessage(final String instanceId, final ReadableMap messageMap, final Callback callback) {
        ClientWrapper clientWrapper = stringeeManager.getClientWrapperMap().get(instanceId);
        if (clientWrapper == null) {
            callback.invoke(false, -1, "StringeeClient is not initialized");
            return;
        }

        String convId = messageMap.getString("convId");
        final Type type = Type.getType(messageMap.getInt("type"));
        final ReadableMap msgMap = messageMap.getMap("message");

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

        clientWrapper.getChatWrapper().sendMessage(convId, message, callback);
    }

    @ReactMethod
    public void getLocalMessages(final String instanceId, final String convId, final int count, final Callback callback) {
        ClientWrapper clientWrapper = stringeeManager.getClientWrapperMap().get(instanceId);
        if (clientWrapper == null) {
            callback.invoke(false, -1, "StringeeClient is not initialized");
            return;
        }

        if (convId == null) {
            callback.invoke(false, -2, "Conversation id can not be null");
            return;
        }

        clientWrapper.getChatWrapper().getLocalMessages(convId, count, callback);
    }

    @ReactMethod
    public void getLastMessages(final String instanceId, final String convId, final int count, final boolean loadDeletedMsg, final boolean loadDeletedMsgContent, final Callback callback) {
        ClientWrapper clientWrapper = stringeeManager.getClientWrapperMap().get(instanceId);
        if (clientWrapper == null) {
            callback.invoke(false, -1, "StringeeClient is not initialized");
            return;
        }

        if (convId == null) {
            callback.invoke(false, -2, "Conversation id can not be null");
            return;
        }

        clientWrapper.getChatWrapper().getLastMessages(convId, count, loadDeletedMsg, loadDeletedMsgContent, false, callback);
    }


    @ReactMethod
    public void getMessagesAfter(final String instanceId, final String convId, final int sequence, final int count, final boolean loadDeletedMsg, final boolean loadDeletedMsgContent, final Callback callback) {
        ClientWrapper clientWrapper = stringeeManager.getClientWrapperMap().get(instanceId);
        if (clientWrapper == null) {
            callback.invoke(false, -1, "StringeeClient is not initialized");
            return;
        }

        if (convId == null) {
            callback.invoke(false, -2, "Conversation id can not be null");
            return;
        }

        clientWrapper.getChatWrapper().getMessagesAfter(convId, sequence, count, loadDeletedMsg, loadDeletedMsgContent, false, callback);
    }

    @ReactMethod
    public void getMessagesBefore(final String instanceId, final String convId, final int sequence, final int count, final boolean loadDeletedMsg, final boolean loadDeletedMsgContent, final Callback callback) {
        ClientWrapper clientWrapper = stringeeManager.getClientWrapperMap().get(instanceId);
        if (clientWrapper == null) {
            callback.invoke(false, -1, "StringeeClient is not initialized");
            return;
        }

        if (convId == null) {
            callback.invoke(false, -2, "Conversation id can not be null");
            return;
        }

        clientWrapper.getChatWrapper().getMessagesBefore(convId, sequence, count, loadDeletedMsg, loadDeletedMsgContent, false, callback);
    }

    @ReactMethod
    public void deleteMessage(final String instanceId, final String convId, final String msgId, final Callback callback) {
        ClientWrapper clientWrapper = stringeeManager.getClientWrapperMap().get(instanceId);
        if (clientWrapper == null) {
            callback.invoke(false, -1, "StringeeClient is not initialized");
            return;
        }

        if (convId == null) {
            callback.invoke(false, -2, "Conversation id can not be null");
            return;
        }

        JSONArray messageIds = new JSONArray();
        messageIds.put(msgId);
        clientWrapper.getChatWrapper().deleteMessage(convId, messageIds, callback);
    }

    @ReactMethod
    public void markConversationAsRead(final String instanceId, final String convId, final Callback callback) {
        ClientWrapper clientWrapper = stringeeManager.getClientWrapperMap().get(instanceId);
        if (clientWrapper == null) {
            callback.invoke(false, -1, "StringeeClient is not initialized");
            return;
        }

        if (convId == null) {
            callback.invoke(false, -2, "Conversation id can not be null");
            return;
        }

        clientWrapper.getChatWrapper().markConversationAsRead(convId, callback);
    }

    @ReactMethod
    public void getUser(final String instanceId, final String userId, final Callback callback) {
        ClientWrapper clientWrapper = stringeeManager.getClientWrapperMap().get(instanceId);
        if (clientWrapper == null) {
            callback.invoke(false, -1, "StringeeClient is not initialized");
            return;
        }

        if (userId == null) {
            callback.invoke(false, -2, "User id can not be null");
            return;
        }

        clientWrapper.getUser(userId, callback);
    }

    @ReactMethod
    public void clearDb(final String instanceId, final Callback callback) {
        ClientWrapper clientWrapper = stringeeManager.getClientWrapperMap().get(instanceId);
        if (clientWrapper == null) {
            callback.invoke(false, -1, "StringeeClient is not initialized");
            return;
        }
        clientWrapper.clearDb(callback);
    }

    @ReactMethod
    public void updateConversation(final String instanceId, final String convId, final ReadableMap convMap, final Callback callback) {
        ClientWrapper clientWrapper = stringeeManager.getClientWrapperMap().get(instanceId);
        if (clientWrapper == null) {
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

        clientWrapper.getChatWrapper().updateConversation(convId, name, avatar, callback);
    }

    @ReactMethod
    public void getConversationWithUser(final String instanceId, final String userId, final Callback callback) {
        ClientWrapper clientWrapper = stringeeManager.getClientWrapperMap().get(instanceId);
        if (clientWrapper == null) {
            callback.invoke(false, -1, "StringeeClient is not initialized");
            return;
        }

        if (userId == null) {
            callback.invoke(false, -2, "User id can not be null");
            return;
        }

        clientWrapper.getChatWrapper().getConversationWithUser(userId, callback);
    }

    @ReactMethod
    public void getUnreadConversationCount(final String instanceId, final Callback callback) {
        ClientWrapper clientWrapper = stringeeManager.getClientWrapperMap().get(instanceId);
        if (clientWrapper == null) {
            callback.invoke(false, -1, "StringeeClient is not initialized");
            return;
        }

        clientWrapper.getChatWrapper().getUnreadConversationCount(callback);
    }

    @ReactMethod
    public void getLastUnreadConversations(final String instanceId, final int count, final Callback callback) {
        ClientWrapper clientWrapper = stringeeManager.getClientWrapperMap().get(instanceId);
        if (clientWrapper == null) {
            callback.invoke(false, -1, "StringeeClient is not initialized");
            return;
        }

        clientWrapper.getChatWrapper().getLastUnreadConversations(count, callback);
    }

    @ReactMethod
    public void getUnreadConversationsBefore(final String instanceId, final double datetime, final int count, final Callback callback) {
        ClientWrapper clientWrapper = stringeeManager.getClientWrapperMap().get(instanceId);
        if (clientWrapper == null) {
            callback.invoke(false, -1, "StringeeClient is not initialized");
            return;
        }

        clientWrapper.getChatWrapper().getUnreadConversationsBefore((long) datetime, count, callback);
    }

    @ReactMethod
    public void getUnreadConversationsAfter(final String instanceId, final double datetime, final int count, final Callback callback) {
        ClientWrapper clientWrapper = stringeeManager.getClientWrapperMap().get(instanceId);
        if (clientWrapper == null) {
            callback.invoke(false, -1, "StringeeClient is not initialized");
            return;
        }

        clientWrapper.getChatWrapper().getUnreadConversationsAfter((long) datetime, count, callback);
    }

    @ReactMethod
    public void getAllLastConversations(final String instanceId, final int count, final Callback callback) {
        ClientWrapper clientWrapper = stringeeManager.getClientWrapperMap().get(instanceId);
        if (clientWrapper == null) {
            callback.invoke(false, -1, "StringeeClient is not initialized");
            return;
        }

        clientWrapper.getChatWrapper().getAllLastConversations(count, true, callback);
    }

    @ReactMethod
    public void getAllConversationsBefore(final String instanceId, final double datetime, final int count, final Callback callback) {
        ClientWrapper clientWrapper = stringeeManager.getClientWrapperMap().get(instanceId);
        if (clientWrapper == null) {
            callback.invoke(false, -1, "StringeeClient is not initialized");
            return;
        }

        clientWrapper.getChatWrapper().getAllConversationsBefore((long) datetime, count, true, callback);
    }

    @ReactMethod
    public void getAllConversationsAfter(final String instanceId, final double datetime, final int count, final Callback callback) {
        ClientWrapper clientWrapper = stringeeManager.getClientWrapperMap().get(instanceId);
        if (clientWrapper == null) {
            callback.invoke(false, -1, "StringeeClient is not initialized");
            return;
        }

        clientWrapper.getChatWrapper().getAllConversationsAfter((long) datetime, count, true, callback);
    }

    @ReactMethod
    public void getAllLastMessages(final String instanceId, final String convId, final int count, final boolean loadDeletedMsg, final boolean loadDeletedMsgContent, final Callback callback) {
        ClientWrapper clientWrapper = stringeeManager.getClientWrapperMap().get(instanceId);
        if (clientWrapper == null) {
            callback.invoke(false, -1, "StringeeClient is not initialized");
            return;
        }

        if (convId == null) {
            callback.invoke(false, -2, "Conversation id can not be null");
            return;
        }

        clientWrapper.getChatWrapper().getAllLastMessages(convId, count, loadDeletedMsg, loadDeletedMsgContent, true, callback);
    }


    @ReactMethod
    public void getAllMessagesAfter(final String instanceId, final String convId, final int sequence, final int count, final boolean loadDeletedMsg, final boolean loadDeletedMsgContent, final Callback callback) {
        ClientWrapper clientWrapper = stringeeManager.getClientWrapperMap().get(instanceId);
        if (clientWrapper == null) {
            callback.invoke(false, -1, "StringeeClient is not initialized");
            return;
        }

        if (convId == null) {
            callback.invoke(false, -2, "Conversation id can not be null");
            return;
        }

        clientWrapper.getChatWrapper().getAllMessagesAfter(convId, sequence, count, loadDeletedMsg, loadDeletedMsgContent, true, callback);
    }

    @ReactMethod
    public void getAllMessagesBefore(final String instanceId, final String convId, final int sequence, final int count, final boolean loadDeletedMsg, final boolean loadDeletedMsgContent, final Callback callback) {
        ClientWrapper clientWrapper = stringeeManager.getClientWrapperMap().get(instanceId);
        if (clientWrapper == null) {
            callback.invoke(false, -1, "StringeeClient is not initialized");
            return;
        }

        if (convId == null) {
            callback.invoke(false, -2, "Conversation id can not be null");
            return;
        }

        clientWrapper.getChatWrapper().getAllMessagesBefore(convId, sequence, count, loadDeletedMsg, loadDeletedMsgContent, true, callback);
    }

    @ReactMethod
    public void getChatProfile(final String instanceId, final String widgetKey, final Callback callback) {
        ClientWrapper clientWrapper = stringeeManager.getClientWrapperMap().get(instanceId);
        if (clientWrapper == null) {
            callback.invoke(false, -1, "StringeeClient is not initialized");
            return;
        }

        if (widgetKey == null) {
            callback.invoke(false, -2, "Widget key can not be null");
            return;
        }

        clientWrapper.getChatWrapper().getChatProfile(widgetKey, callback);
    }

    @ReactMethod
    public void getLiveChatToken(final String instanceId, final String widgetKey, final String name, final String email, final Callback callback) {
        ClientWrapper clientWrapper = stringeeManager.getClientWrapperMap().get(instanceId);
        if (clientWrapper == null) {
            callback.invoke(false, -1, "StringeeClient is not initialized");
            return;
        }

        if (widgetKey == null) {
            callback.invoke(false, -2, "Widget key can not be null");
            return;
        }

        clientWrapper.getChatWrapper().getLiveChatToken(widgetKey, name, email, callback);
    }

    @ReactMethod
    public void updateUserInfo(final String instanceId, final String name, final String email, final String avatar, final String phone, final Callback callback) {
        ClientWrapper clientWrapper = stringeeManager.getClientWrapperMap().get(instanceId);
        if (clientWrapper == null) {
            callback.invoke(false, -1, "StringeeClient is not initialized");
            return;
        }

        clientWrapper.getChatWrapper().updateUserInfo(name, email, avatar, phone, callback);
    }

    @ReactMethod
    public void createLiveChatConversation(final String instanceId, final String queueId, final Callback callback) {
        ClientWrapper clientWrapper = stringeeManager.getClientWrapperMap().get(instanceId);
        if (clientWrapper == null) {
            callback.invoke(false, -1, "StringeeClient is not initialized");
            return;
        }

        if (queueId == null) {
            callback.invoke(false, -2, "Queue id can not be null");
            return;
        }

        clientWrapper.getChatWrapper().createLiveChatConversation(queueId, callback);
    }

    @ReactMethod
    public void acceptChatRequest(final String instanceId, final String convId, final Callback callback) {
        ClientWrapper clientWrapper = stringeeManager.getClientWrapperMap().get(instanceId);
        if (clientWrapper == null) {
            callback.invoke(false, -1, "StringeeClient is not initialized");
            return;
        }

        if (convId == null) {
            callback.invoke(false, -2, "Conversation id can not be null");
            return;
        }

        clientWrapper.getChatWrapper().acceptChatRequest(convId, callback);
    }

    @ReactMethod
    public void rejectChatRequest(final String instanceId, final String convId, final Callback callback) {
        ClientWrapper clientWrapper = stringeeManager.getClientWrapperMap().get(instanceId);
        if (clientWrapper == null) {
            callback.invoke(false, -1, "StringeeClient is not initialized");
            return;
        }

        if (convId == null) {
            callback.invoke(false, -2, "Conversation id can not be null");
            return;
        }

        clientWrapper.getChatWrapper().rejectChatRequest(convId, callback);
    }

    @ReactMethod
    public void createLiveChatTicket(final String instanceId, final String widgetKey, final String name, final String email, final String phone, final String note, final Callback callback) {
        ClientWrapper clientWrapper = stringeeManager.getClientWrapperMap().get(instanceId);
        if (clientWrapper == null) {
            callback.invoke(false, -1, "StringeeClient is not initialized");
            return;
        }

        if (widgetKey == null) {
            callback.invoke(false, -2, "Widget key can not be null");
            return;
        }

        clientWrapper.getChatWrapper().createLiveChatTicket(widgetKey, name, email, phone, note, callback);
    }

    @ReactMethod
    public void sendChatTranscript(final String instanceId, final String email, final String convId, final String domain, final Callback callback) {
        ClientWrapper clientWrapper = stringeeManager.getClientWrapperMap().get(instanceId);
        if (clientWrapper == null) {
            callback.invoke(false, -1, "StringeeClient is not initialized");
            return;
        }

        if (convId == null) {
            callback.invoke(false, -2, "Conversation id can not be null");
            return;
        }

        clientWrapper.getChatWrapper().sendChatTranscript(convId, email, domain, callback);
    }

    @ReactMethod
    public void endChat(final String instanceId, final String convId, final Callback callback) {
        ClientWrapper clientWrapper = stringeeManager.getClientWrapperMap().get(instanceId);
        if (clientWrapper == null) {
            callback.invoke(false, -1, "StringeeClient is not initialized");
            return;
        }

        if (convId == null) {
            callback.invoke(false, -2, "Conversation id can not be null");
            return;
        }

        clientWrapper.getChatWrapper().endChat(convId, callback);
    }

    @ReactMethod
    public void sendBeginTyping(final String instanceId, final String convId, final Callback callback) {
        ClientWrapper clientWrapper = stringeeManager.getClientWrapperMap().get(instanceId);
        if (clientWrapper == null) {
            callback.invoke(false, -1, "StringeeClient is not initialized");
            return;
        }

        if (convId == null) {
            callback.invoke(false, -2, "Conversation id can not be null");
            return;
        }

        clientWrapper.getChatWrapper().sendBeginTyping(convId, callback);
    }

    @ReactMethod
    public void sendEndTyping(final String instanceId, final String convId, final Callback callback) {
        ClientWrapper clientWrapper = stringeeManager.getClientWrapperMap().get(instanceId);
        if (clientWrapper == null) {
            callback.invoke(false, -1, "StringeeClient is not initialized");
            return;
        }

        if (convId == null) {
            callback.invoke(false, -2, "Conversation id can not be null");
            return;
        }

        clientWrapper.getChatWrapper().sendEndTyping(convId, callback);
    }

    @ReactMethod
    public void pinMessage(final String instanceId, final String convId, final String msgId, final boolean pinOrUnpin, final Callback callback) {
        ClientWrapper clientWrapper = stringeeManager.getClientWrapperMap().get(instanceId);
        if (clientWrapper == null) {
            callback.invoke(false, -1, "StringeeClient is not initialized");
            return;
        }

        if (convId == null) {
            callback.invoke(false, -2, "Conversation id can not be null");
            return;
        }

        if (msgId == null) {
            callback.invoke(false, -2, "Message id can not be null");
            return;
        }

        String[] msgIds = new String[1];
        msgIds[0] = msgId;

        clientWrapper.getChatWrapper().pinMessage(convId, msgIds, pinOrUnpin, callback);
    }

    @ReactMethod
    public void editMessage(final String instanceId, final String convId, final String msgId, final String newContent, final Callback callback) {
        ClientWrapper clientWrapper = stringeeManager.getClientWrapperMap().get(instanceId);
        if (clientWrapper == null) {
            callback.invoke(false, -1, "StringeeClient is not initialized");
            return;
        }

        if (convId == null) {
            callback.invoke(false, -2, "Conversation id can not be null");
            return;
        }

        if (msgId == null) {
            callback.invoke(false, -2, "Message id can not be null");
            return;
        }

        String[] msgIds = new String[1];
        msgIds[0] = msgId;

        clientWrapper.getChatWrapper().editMessage(convId, msgIds, newContent, callback);
    }

    @ReactMethod
    public void revokeMessage(final String instanceId, final String convId, final String msgId, final Callback callback) {
        ClientWrapper clientWrapper = stringeeManager.getClientWrapperMap().get(instanceId);
        if (clientWrapper == null) {
            callback.invoke(false, -1, "StringeeClient is not initialized");
            return;
        }

        if (convId == null) {
            callback.invoke(false, -2, "Conversation id can not be null");
            return;
        }

        if (msgId == null) {
            callback.invoke(false, -2, "Message id can not be null");
            return;
        }

        JSONArray msgArray = new JSONArray();
        msgArray.put(msgId);

        clientWrapper.getChatWrapper().revokeMessage(convId, msgArray, callback);
    }

    @ReactMethod
    public void getMessageById(final String instanceId, final String convId, final String msgId, final Callback callback) {
        ClientWrapper clientWrapper = stringeeManager.getClientWrapperMap().get(instanceId);
        if (clientWrapper == null) {
            callback.invoke(false, -1, "StringeeClient is not initialized");
            return;
        }

        if (convId == null) {
            callback.invoke(false, -2, "Conversation id can not be null");
            return;
        }

        if (msgId == null) {
            callback.invoke(false, -2, "Message id can not be null");
            return;
        }

        String[] msgIds = new String[1];
        msgIds[0] = msgId;

        clientWrapper.getChatWrapper().getMessageById(convId, msgIds, callback);
    }

    @ReactMethod
    public void setNativeEvent(String instanceId, String event) {
        ClientWrapper clientWrapper = stringeeManager.getClientWrapperMap().get(instanceId);
        if (clientWrapper != null) {
            clientWrapper.setNativeEvent(event);
        }
    }

    @ReactMethod
    public void removeNativeEvent(String instanceId, String event) {
        ClientWrapper clientWrapper = stringeeManager.getClientWrapperMap().get(instanceId);
        if (clientWrapper != null) {
            clientWrapper.removeNativeEvent(event);
        }
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
