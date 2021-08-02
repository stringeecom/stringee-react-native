package com.stringeereactnative;

import android.os.Bundle;

import com.facebook.react.bridge.Arguments;
import com.facebook.react.bridge.WritableArray;
import com.facebook.react.bridge.WritableMap;
import com.stringee.messaging.Conversation;
import com.stringee.messaging.User;

import org.json.JSONException;
import org.json.JSONObject;

import java.util.Iterator;
import java.util.List;

public class Utils {
    private static Bundle jsonToBundle(String text) throws JSONException {
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
        conversationMap.putInt("lastMsgType", conversation.getLastMsgType());
        conversationMap.putInt("unreadCount", conversation.getTotalUnread());
        conversationMap.putString("lastMsgId", conversation.getLastMsgId());
        conversationMap.putString("creator", conversation.getCreator());
        conversationMap.putDouble("created", conversation.getCreateAt());
        conversationMap.putDouble("lastMsgSeq", conversation.getLastMsgSeqReceived());
        conversationMap.putDouble("lastMsgCreatedAt", conversation.getLastTimeNewMsg());
        conversationMap.putInt("lastMsgState", conversation.getLastMsgState());
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

    public static WritableMap getUserMap(User user) {
        WritableMap userMap = Arguments.createMap();
        userMap.putString("userId", user.getUserId());
        userMap.putString("name", user.getName());
        userMap.putString("avatar", user.getAvatarUrl());
        return userMap;
    }
}
