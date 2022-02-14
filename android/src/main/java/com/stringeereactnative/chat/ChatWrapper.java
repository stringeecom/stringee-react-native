package com.stringeereactnative.chat;

import com.facebook.react.bridge.Arguments;
import com.facebook.react.bridge.Callback;
import com.facebook.react.bridge.ReactApplicationContext;
import com.facebook.react.bridge.WritableArray;
import com.facebook.react.bridge.WritableMap;
import com.stringee.exception.StringeeError;
import com.stringee.listener.StatusListener;
import com.stringee.messaging.ChatProfile;
import com.stringee.messaging.ChatRequest;
import com.stringee.messaging.Conversation;
import com.stringee.messaging.ConversationOptions;
import com.stringee.messaging.Message;
import com.stringee.messaging.User;
import com.stringee.messaging.listeners.CallbackListener;
import com.stringeereactnative.ClientWrapper;
import com.stringeereactnative.common.StringeeManager;
import com.stringeereactnative.common.Utils;

import org.json.JSONArray;

import java.util.List;

public class ChatWrapper {
    private ClientWrapper clientWrapper;
    private StringeeManager stringeeManager;
    private ReactApplicationContext context;

    public ChatWrapper(ClientWrapper clientWrapper) {
        this.clientWrapper = clientWrapper;
        this.context = clientWrapper.getContext();
        this.stringeeManager = StringeeManager.getInstance();
    }

    public void createConversation(List<User> participants, ConversationOptions options, final Callback callback) {
        clientWrapper.getClient().createConversation(participants, options, new CallbackListener<Conversation>() {
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

    public void getConversationById(String convId, Callback callback) {
        clientWrapper.getClient().getConversationFromServer(convId, new CallbackListener<Conversation>() {
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

    public void getLocalConversations(String userId, Callback callback) {
        clientWrapper.getClient().getLocalConversations(userId, new CallbackListener<List<Conversation>>() {
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

    public void getLastConversations(int count, Callback callback) {
        clientWrapper.getClient().getLastConversations(count, new CallbackListener<List<Conversation>>() {
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

    public void getConversationsBefore(double datetime, int count, Callback callback) {
        clientWrapper.getClient().getConversationsBefore((long) datetime, count, new CallbackListener<List<Conversation>>() {
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

    public void getConversationsAfter(double datetime, int count, Callback callback) {
        clientWrapper.getClient().getConversationsAfter((long) datetime, count, new CallbackListener<List<Conversation>>() {
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

    public void deleteConversation(String convId, Callback callback) {
        clientWrapper.getClient().getConversationFromServer(convId, new CallbackListener<Conversation>() {
            @Override
            public void onSuccess(final Conversation conversation) {
                if (conversation.isGroup()) {
                    if (conversation.getState() != Conversation.State.LEFT) {
                        callback.invoke(false, -2, "You must leave this group before deleting");
                        return;
                    }
                }
                conversation.delete(clientWrapper.getClient(), new StatusListener() {
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

    public void addParticipants(String convId, List<User> users, Callback callback) {
        clientWrapper.getClient().getConversationFromServer(convId, new CallbackListener<Conversation>() {
            @Override
            public void onSuccess(Conversation conversation) {
                conversation.addParticipants(clientWrapper.getClient(), users, new CallbackListener<List<User>>() {
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

    public void removeParticipants(String convId, List<User> users, Callback callback) {
        clientWrapper.getClient().getConversationFromServer(convId, new CallbackListener<Conversation>() {
            @Override
            public void onSuccess(Conversation conversation) {
                conversation.removeParticipants(clientWrapper.getClient(), users, new CallbackListener<List<User>>() {
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

    public void sendMessage(String convId, Message message, Callback callback) {
        clientWrapper.getClient().getConversationFromServer(convId, new CallbackListener<Conversation>() {
            @Override
            public void onSuccess(Conversation conversation) {
                conversation.sendMessage(clientWrapper.getClient(), message, new StatusListener() {
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

    public void getLocalMessages(String convId, int count, Callback callback) {
        clientWrapper.getClient().getConversationFromServer(convId, new CallbackListener<Conversation>() {
            @Override
            public void onSuccess(Conversation conversation) {
                conversation.getLocalMessages(clientWrapper.getClient(), count, new CallbackListener<List<Message>>() {
                    @Override
                    public void onSuccess(List<Message> messages) {
                        WritableArray params = Arguments.createArray();
                        for (int i = 0; i < messages.size(); i++) {
                            WritableMap param = Utils.getMessageMap(messages.get(i));
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

    public void getLastMessages(String convId, int count, boolean loadDeletedMsg, boolean loadDeletedMsgContent, boolean loadAll, Callback callback) {
        clientWrapper.getClient().getConversationFromServer(convId, new CallbackListener<Conversation>() {
            @Override
            public void onSuccess(Conversation conversation) {
                conversation.getLastMessages(clientWrapper.getClient(), count, loadDeletedMsg, loadDeletedMsgContent, loadAll, new CallbackListener<List<Message>>() {
                    @Override
                    public void onSuccess(List<Message> messages) {
                        WritableArray params = Arguments.createArray();
                        for (int i = 0; i < messages.size(); i++) {
                            WritableMap param = Utils.getMessageMap(messages.get(i));
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

    public void getMessagesAfter(String convId, int sequence, int count, boolean loadDeletedMsg, boolean loadDeletedMsgContent, boolean loadAll, Callback callback) {
        clientWrapper.getClient().getConversationFromServer(convId, new CallbackListener<Conversation>() {
            @Override
            public void onSuccess(Conversation conversation) {
                conversation.getMessagesAfter(clientWrapper.getClient(), sequence, count, loadDeletedMsg, loadDeletedMsgContent, loadAll, new CallbackListener<List<Message>>() {
                    @Override
                    public void onSuccess(List<Message> messages) {
                        WritableArray params = Arguments.createArray();
                        for (int i = 0; i < messages.size(); i++) {
                            WritableMap param = Utils.getMessageMap(messages.get(i));
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

    public void getMessagesBefore(String convId, int sequence, int count, boolean loadDeletedMsg, boolean loadDeletedMsgContent, boolean loadAll, Callback callback) {
        clientWrapper.getClient().getConversationFromServer(convId, new CallbackListener<Conversation>() {
            @Override
            public void onSuccess(Conversation conversation) {
                conversation.getMessagesBefore(clientWrapper.getClient(), sequence, count, loadDeletedMsg, loadDeletedMsgContent, loadAll, new CallbackListener<List<Message>>() {
                    @Override
                    public void onSuccess(List<Message> messages) {
                        WritableArray params = Arguments.createArray();
                        for (int i = 0; i < messages.size(); i++) {
                            WritableMap param = Utils.getMessageMap(messages.get(i));
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

    public void deleteMessage(String convId, JSONArray messageIds, Callback callback) {
        clientWrapper.getClient().deleteMessages(convId, messageIds, new StatusListener() {
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

    public void markConversationAsRead(String convId, Callback callback) {
        clientWrapper.getClient().getConversationFromServer(convId, new CallbackListener<Conversation>() {
            @Override
            public void onSuccess(Conversation conversation) {
                conversation.getLastMessages(clientWrapper.getClient(), 1, true, true, false, new CallbackListener<List<Message>>() {
                    @Override
                    public void onSuccess(List<Message> messages) {
                        if (messages != null && messages.size() > 0) {
                            Message message = messages.get(0);
                            if (message != null) {
                                message.markAsRead(clientWrapper.getClient(), new StatusListener() {
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

    public void updateConversation(String convId, String name, String avatar, Callback callback) {
        clientWrapper.getClient().getConversationFromServer(convId, new CallbackListener<Conversation>() {
            @Override
            public void onSuccess(Conversation conversation) {
                conversation.updateConversation(clientWrapper.getClient(), name, avatar, new StatusListener() {
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

    public void getConversationWithUser(String userId, Callback callback) {
        clientWrapper.getClient().getConversationByUserId(userId, new CallbackListener<Conversation>() {
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

    public void getUnreadConversationCount(Callback callback) {
        clientWrapper.getClient().getTotalUnread(new CallbackListener<Integer>() {
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

    public void getLastUnreadConversations(int count, Callback callback) {
        clientWrapper.getClient().getLastUnreadConversations(count, new CallbackListener<List<Conversation>>() {
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

    public void getUnreadConversationsBefore(long datetime, int count, Callback callback) {
        clientWrapper.getClient().getUnreadConversationsBefore(datetime, count, new CallbackListener<List<Conversation>>() {
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

    public void getUnreadConversationsAfter(long datetime, int count, Callback callback) {
        clientWrapper.getClient().getUnreadConversationsAfter(datetime, count, new CallbackListener<List<Conversation>>() {
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

    public void getAllLastConversations(int count, boolean loadAll, Callback callback) {
        clientWrapper.getClient().getLastConversations(count, loadAll, new CallbackListener<List<Conversation>>() {
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

    public void getAllConversationsBefore(double datetime, int count, boolean loadAll, Callback callback) {
        clientWrapper.getClient().getConversationsBefore((long) datetime, count, loadAll, new CallbackListener<List<Conversation>>() {
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

    public void getAllConversationsAfter(long datetime, int count, boolean loadAll, Callback callback) {
        clientWrapper.getClient().getConversationsAfter(datetime, count, loadAll, new CallbackListener<List<Conversation>>() {
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

    public void getAllLastMessages(String convId, int count, boolean loadDeletedMsg, boolean loadDeletedMsgContent, boolean loadAll, Callback callback) {
        clientWrapper.getClient().getConversationFromServer(convId, new CallbackListener<Conversation>() {
            @Override
            public void onSuccess(Conversation conversation) {
                conversation.getLastMessages(clientWrapper.getClient(), count, loadDeletedMsg, loadDeletedMsgContent, loadAll, new CallbackListener<List<Message>>() {
                    @Override
                    public void onSuccess(List<Message> messages) {
                        WritableArray params = Arguments.createArray();
                        for (int i = 0; i < messages.size(); i++) {
                            WritableMap param = Utils.getMessageMap(messages.get(i));
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

    public void getAllMessagesAfter(String convId, int sequence, int count, boolean loadDeletedMsg, boolean loadDeletedMsgContent, boolean loadAll, Callback callback) {
        clientWrapper.getClient().getConversationFromServer(convId, new CallbackListener<Conversation>() {
            @Override
            public void onSuccess(Conversation conversation) {
                conversation.getMessagesAfter(clientWrapper.getClient(), sequence, count, loadDeletedMsg, loadDeletedMsgContent, loadAll, new CallbackListener<List<Message>>() {
                    @Override
                    public void onSuccess(List<Message> messages) {
                        WritableArray params = Arguments.createArray();
                        for (int i = 0; i < messages.size(); i++) {
                            WritableMap param = Utils.getMessageMap(messages.get(i));
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

    public void getAllMessagesBefore(String convId, int sequence, int count, boolean loadDeletedMsg, boolean loadDeletedMsgContent, boolean loadAll, Callback callback) {
        clientWrapper.getClient().getConversationFromServer(convId, new CallbackListener<Conversation>() {
            @Override
            public void onSuccess(Conversation conversation) {
                conversation.getMessagesBefore(clientWrapper.getClient(), sequence, count, loadDeletedMsg, loadDeletedMsgContent, loadAll, new CallbackListener<List<Message>>() {
                    @Override
                    public void onSuccess(List<Message> messages) {
                        WritableArray params = Arguments.createArray();
                        for (int i = 0; i < messages.size(); i++) {
                            WritableMap param = Utils.getMessageMap(messages.get(i));
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

    public void getChatProfile(String widgetKey, Callback callback) {
        clientWrapper.getClient().getChatProfile(widgetKey, new CallbackListener<ChatProfile>() {
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

    public void getLiveChatToken(String widgetKey, String name, String email, Callback callback) {
        clientWrapper.getClient().getLiveChatToken(widgetKey, name, email, new CallbackListener<String>() {
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

    public void updateUserInfo(String name, String email, String avatar, Callback callback) {
        clientWrapper.getClient().updateUser(name, email, avatar, new StatusListener() {
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

    public void createLiveChatConversation(String queueId, Callback callback) {
        clientWrapper.getClient().createLiveChat(queueId, new CallbackListener<Conversation>() {
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

    public void acceptChatRequest(String convId, Callback callback) {
        clientWrapper.getClient().getChatRequests(new CallbackListener<List<ChatRequest>>() {
            @Override
            public void onSuccess(List<ChatRequest> chatRequestList) {
                for (int i = 0; i < chatRequestList.size(); i++) {
                    ChatRequest chatRequest = chatRequestList.get(i);
                    if (convId.equals(chatRequest.getConvId())) {
                        chatRequest.accept(clientWrapper.getClient(), new CallbackListener<Conversation>() {
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

    public void rejectChatRequest(String convId, Callback callback) {
        clientWrapper.getClient().getChatRequests(new CallbackListener<List<ChatRequest>>() {
            @Override
            public void onSuccess(List<ChatRequest> chatRequestList) {
                for (int i = 0; i < chatRequestList.size(); i++) {
                    ChatRequest chatRequest = chatRequestList.get(i);
                    if (convId.equals(chatRequest.getConvId())) {
                        chatRequest.reject(clientWrapper.getClient(), new StatusListener() {
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

    public void createLiveChatTicket(String widgetKey, String name, String email, String note, Callback callback) {
        clientWrapper.getClient().createLiveChatTicket(widgetKey, name, email, note, new StatusListener() {
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

    public void sendChatTranscript(String email, String convId, String domain, Callback callback) {
        clientWrapper.getClient().getConversationFromServer(convId, new CallbackListener<Conversation>() {
            @Override
            public void onSuccess(Conversation conversation) {
                conversation.sendChatTranscriptTo(clientWrapper.getClient(), email, domain, new StatusListener() {
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

    public void endChat(String convId, Callback callback) {
        clientWrapper.getClient().getConversationFromServer(convId, new CallbackListener<Conversation>() {
            @Override
            public void onSuccess(Conversation conversation) {
                conversation.endChat(clientWrapper.getClient(), new StatusListener() {
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

    public void sendBeginTyping(String convId, Callback callback) {
        clientWrapper.getClient().getConversationFromServer(convId, new CallbackListener<Conversation>() {
            @Override
            public void onSuccess(Conversation conversation) {
                conversation.beginTyping(clientWrapper.getClient(), new StatusListener() {
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

    public void sendEndTyping(String convId, Callback callback) {
        clientWrapper.getClient().getConversationFromServer(convId, new CallbackListener<Conversation>() {
            @Override
            public void onSuccess(Conversation conversation) {
                conversation.endTyping(clientWrapper.getClient(), new StatusListener() {
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

    public void pinMessage(String convId, String[] msgIds, boolean pinOrUnpin, Callback callback) {
        clientWrapper.getClient().getConversationFromServer(convId, new CallbackListener<Conversation>() {
            @Override
            public void onSuccess(Conversation conversation) {
                conversation.getMessages(clientWrapper.getClient(), msgIds, new CallbackListener<List<Message>>() {
                    @Override
                    public void onSuccess(List<Message> messages) {
                        messages.get(0).pinOrUnpin(clientWrapper.getClient(), pinOrUnpin, new StatusListener() {
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

            @Override
            public void onError(StringeeError stringeeError) {
                super.onError(stringeeError);
                callback.invoke(false, stringeeError.getCode(), stringeeError.getMessage());
            }
        });
    }

    public void editMessage(String convId, String[] msgIds, String newContent, Callback callback) {
        clientWrapper.getClient().getConversationFromServer(convId, new CallbackListener<Conversation>() {
            @Override
            public void onSuccess(Conversation conversation) {
                conversation.getMessages(clientWrapper.getClient(), msgIds, new CallbackListener<List<Message>>() {
                    @Override
                    public void onSuccess(List<Message> messages) {
                        messages.get(0).edit(clientWrapper.getClient(), newContent, new StatusListener() {
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

            @Override
            public void onError(StringeeError stringeeError) {
                super.onError(stringeeError);
                callback.invoke(false, stringeeError.getCode(), stringeeError.getMessage());
            }
        });
    }

    public void revokeMessage(String convId, JSONArray msgArray, Callback callback) {
        clientWrapper.getClient().revokeMessages(convId, msgArray, true, new StatusListener() {
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

    public void getMessageById(String convId, String[] msgIds, Callback callback) {
        clientWrapper.getClient().getConversationFromServer(convId, new CallbackListener<Conversation>() {
            @Override
            public void onSuccess(Conversation conversation) {
                conversation.getMessages(clientWrapper.getClient(), msgIds, new CallbackListener<List<Message>>() {
                    @Override
                    public void onSuccess(List<Message> messages) {
                        if (messages.size() > 0) {
                            WritableMap param = com.stringeereactnative.common.Utils.getMessageMap(messages.get(0));
                            callback.invoke(true, 0, "Success", param);
                        }
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
}
