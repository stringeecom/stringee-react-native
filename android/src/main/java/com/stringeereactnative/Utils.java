package com.stringeereactnative;

import android.os.Bundle;

import com.facebook.react.bridge.Arguments;
import com.facebook.react.bridge.WritableArray;
import com.facebook.react.bridge.WritableMap;
import com.stringee.StringeeClient;
import com.stringee.messaging.ChatProfile;
import com.stringee.messaging.ChatRequest;
import com.stringee.messaging.Conversation;
import com.stringee.messaging.Message;
import com.stringee.messaging.Queue;
import com.stringee.messaging.User;

import org.json.JSONException;
import org.json.JSONObject;

import java.util.Iterator;
import java.util.List;

public class Utils {
    public static Bundle jsonToBundle(String text) throws JSONException {
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

    public static WritableMap getConversationMap(Conversation conversation) {
        WritableMap conversationMap = Arguments.createMap();
        conversationMap.putString("id", conversation.getId());
        conversationMap.putString("localId", conversation.getLocalId());
        conversationMap.putString("name", conversation.getName());
        conversationMap.putBoolean("isDistinct", conversation.isDistinct());
        conversationMap.putBoolean("isGroup", conversation.isGroup());
        conversationMap.putDouble("updatedAt", conversation.getUpdateAt());
        conversationMap.putString("lastMsgSender", conversation.getLastMsgSender());
        conversationMap.putString("text", conversation.getText());
        conversationMap.putInt("lastMsgType", conversation.getLastMsgType().getValue());
        conversationMap.putInt("unreadCount", conversation.getTotalUnread());
        conversationMap.putString("lastMsgId", conversation.getLastMsgId());
        conversationMap.putString("creator", conversation.getCreator());
        conversationMap.putDouble("created", conversation.getCreateAt());
        conversationMap.putDouble("lastMsgSeq", conversation.getLastMsgSeqReceived());
        conversationMap.putDouble("lastMsgCreatedAt", conversation.getLastTimeNewMsg());
        conversationMap.putInt("lastMsgState", conversation.getLastMsgState().getValue());
        if (conversation.getLastMsg() != null) {
            try {
                Bundle bundle = jsonToBundle(conversation.getLastMsg());
                WritableMap lastMsgMap = Arguments.fromBundle(bundle);
                conversationMap.putMap("text", lastMsgMap);
            } catch (JSONException e) {
                e.printStackTrace();
            }
        }
        List<User> participants = conversation.getParticipants();
        WritableArray participantsMap = Arguments.createArray();
        for (int j = 0; j < participants.size(); j++) {
            WritableMap userMap = getUserMap(participants.get(j));
            participantsMap.pushMap(userMap);
        }
        conversationMap.putArray("participants", participantsMap);
        return conversationMap;
    }

    public static WritableMap getMessageMap(StringeeClient client, Message message) {
        WritableMap messageMap = Arguments.createMap();
        messageMap.putString("id", message.getId());
        messageMap.putString("localId", message.getLocalId());
        messageMap.putString("conversationId", message.getConversationId());
        messageMap.putDouble("createdAt", message.getCreatedAt());
        messageMap.putInt("state", message.getState().getValue());
        messageMap.putDouble("sequence", message.getSequence());
        messageMap.putInt("type", message.getType().getValue());
        WritableMap contentMap = Arguments.createMap();
        switch (message.getType()) {
            case TEXT:
            case LINK:
                contentMap.putString("content", message.getText());
                break;
            case PHOTO:
                WritableMap photoMap = Arguments.createMap();
                photoMap.putString("filePath", message.getFileUrl());
                photoMap.putString("thumbnail", message.getThumbnailUrl());
                photoMap.putDouble("ratio", message.getImageRatio());
                contentMap.putMap("photo", photoMap);
                break;
            case VIDEO:
                WritableMap videoMap = Arguments.createMap();
                videoMap.putString("filePath", message.getFileUrl());
                videoMap.putString("thumbnail", message.getThumbnailUrl());
                videoMap.putDouble("ratio", message.getImageRatio());
                videoMap.putInt("duration", message.getDuration());
                contentMap.putMap("video", videoMap);
                break;
            case AUDIO:
                WritableMap audioMap = Arguments.createMap();
                audioMap.putString("filePath", message.getFileUrl());
                audioMap.putInt("duration", message.getDuration());
                contentMap.putMap("audio", audioMap);
                break;
            case FILE:
                WritableMap fileMap = Arguments.createMap();
                fileMap.putString("filePath", message.getFileUrl());
                fileMap.putString("filename", message.getFileName());
                fileMap.putDouble("length", message.getFileLength());
                contentMap.putMap("file", fileMap);
                break;
            case CREATE_CONVERSATION:
            case RENAME_CONVERSATION:
            case RATING:
            case NOTIFICATION:
                try {
                    contentMap = Arguments.fromBundle(jsonToBundle(message.getText()));
                } catch (JSONException e) {
                    e.printStackTrace();
                }
                break;

            case LOCATION:
                WritableMap locationMap = Arguments.createMap();
                locationMap.putDouble("lat", message.getLatitude());
                locationMap.putDouble("lon", message.getLongitude());
                contentMap.putMap("location", locationMap);
                break;
            case CONTACT:
                WritableMap contactMap = Arguments.createMap();
                contactMap.putString("vcard", message.getContact());
                contentMap.putMap("contact", contactMap);
                break;
            case STICKER:
                WritableMap stickerMap = Arguments.createMap();
                stickerMap.putString("name", message.getStickerName());
                stickerMap.putString("category", message.getStickerCategory());
                contentMap.putMap("sticker", stickerMap);
                break;
        }
        messageMap.putMap("content", contentMap);
        String senderId = message.getSenderId();
        User user = client.getUser(senderId);
        String name = "";
        if (user != null) {
            name = user.getName();
            if (name == null || name.length() == 0) {
                name = user.getUserId();
            }
        }
        messageMap.putString("sender", name);
        return messageMap;
    }

    public static WritableMap getUserMap(User user) {
        WritableMap userMap = Arguments.createMap();
        userMap.putString("userId", user.getUserId());
        userMap.putString("name", user.getName());
        userMap.putString("avatar", user.getAvatarUrl());
        return userMap;
    }

    public static WritableMap getChatRequestMap(ChatRequest chatRequest) {
        WritableMap chatRequestMap = Arguments.createMap();
        chatRequestMap.putString("convId", chatRequest.getConvId());
        chatRequestMap.putInt("channelType", chatRequest.getChannelType().getValue());
        chatRequestMap.putInt("type", chatRequest.getRequestType().getValue());
        chatRequestMap.putString("customerId", chatRequest.getCustomerId());
        chatRequestMap.putString("customerName", chatRequest.getName());
        return chatRequestMap;
    }

    public static WritableMap getChatProfileMap(ChatProfile chatProfile) {
        WritableMap conversationMap = Arguments.createMap();
        conversationMap.putString("id", chatProfile.getId());
        conversationMap.putString("background", chatProfile.getBackground());
        conversationMap.putString("hour", chatProfile.getBusinessHour());
        conversationMap.putString("language", chatProfile.getLanguage());
        conversationMap.putString("logoUrl", chatProfile.getLogoUrl());
        conversationMap.putString("popupAnswerUrl", chatProfile.getPopupAnswerUrl());
        conversationMap.putString("portal", chatProfile.getPortalId());
        conversationMap.putBoolean("autoCreateTicket", chatProfile.isAutoCreateTicket());
        conversationMap.putBoolean("enabled", chatProfile.isEnabledBusinessHour());
        conversationMap.putBoolean("facebookAsLivechat", chatProfile.isFacebookAsLivechat());
        conversationMap.putInt("projectId", chatProfile.getProjectId());
        conversationMap.putBoolean("zaloAsLivechat", chatProfile.isZaloAsLivechat());

        List<Queue> queues = chatProfile.getQueues();
        WritableArray queuesMap = Arguments.createArray();
        for (int j = 0; j < queues.size(); j++) {
            WritableMap queueMap = getQueueMap(queues.get(j));
            queuesMap.pushMap(queueMap);
        }
        conversationMap.putArray("queues", queuesMap);
        return conversationMap;
    }

    public static WritableMap getQueueMap(Queue queue) {
        WritableMap queueMap = Arguments.createMap();
        queueMap.putString("userId", queue.getId());
        queueMap.putString("name", queue.getName());
        return queueMap;
    }
}
