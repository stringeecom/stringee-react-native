import PropTypes from "prop-types";
import {Component} from "react";
import {NativeEventEmitter, NativeModules, Platform} from "react-native";
import {each} from "underscore";
import Conversation from "./chat/Conversation";
import Message from "./chat/Message";
import User from "./chat/User";
import {channelType, clientEvents} from "./helpers/StringeeHelper";
import ChatRequest from "./chat/ChatRequest";
import type {RNStringeeEventCallback} from "./helpers/StringeeHelper";

const RNStringeeClient = NativeModules.RNStringeeClient;

const iOS = (Platform.OS === "ios");

export default class extends Component {
    static propTypes = {
        eventHandlers: PropTypes.object
    };

    constructor(props) {
        super(props);
        this._events = [];
        this._subscriptions = [];
        this._eventEmitter = new NativeEventEmitter(RNStringeeClient);

        // Sinh uuid va tao wrapper object trong native
        this.uuid = Math.random().toString(36).substring(2, 15) + Math.random().toString(36).substring(2, 15) + Math.random().toString(36).substring(2, 15) + Math.random().toString(36).substring(2, 15);
        RNStringeeClient.createClientWrapper(this.uuid);

        this.getId = this.getId.bind(this);
        this.connect = this.connect.bind(this);
        this.disconnect = this.disconnect.bind(this);
        this.registerPush = this.registerPush.bind(this);
        this.unregisterPush = this.unregisterPush.bind(this);
        this.sendCustomMessage = this.sendCustomMessage.bind(this);
        this.createConversation = this.createConversation.bind(this);
        this.getConversationById = this.getConversationById.bind(this);
        this.getLocalConversations = this.getLocalConversations.bind(this);
        this.getLastConversations = this.getLastConversations.bind(this);
        this.getAllLastConversations = this.getAllLastConversations.bind(this);
        this.getConversationsAfter = this.getConversationsAfter.bind(this);
        this.getAllConversationsAfter = this.getAllConversationsAfter.bind(this);
        this.getConversationsBefore = this.getConversationsBefore.bind(this);
        this.getAllConversationsBefore = this.getAllConversationsBefore.bind(this);
        this.deleteConversation = this.deleteConversation.bind(this);
        this.addParticipants = this.addParticipants.bind(this);
        this.removeParticipants = this.removeParticipants.bind(this);
        this.updateConversation = this.updateConversation.bind(this);
        this.markConversationAsRead = this.markConversationAsRead.bind(this);
        this.getConversationWithUser = this.getConversationWithUser.bind(this);
        this.getUnreadConversationCount = this.getUnreadConversationCount.bind(this);
        this.sendMessage = this.sendMessage.bind(this);
        this.deleteMessage = this.deleteMessage.bind(this);
        this.getLocalMessages = this.getLocalMessages.bind(this);
        this.getLastMessages = this.getLastMessages.bind(this);
        this.getMessagesAfter = this.getMessagesAfter.bind(this);
        this.getMessagesBefore = this.getMessagesBefore.bind(this);
        this.getLastUnreadConversations = this.getLastUnreadConversations.bind(this);
        this.getUnreadConversationsBefore = this.getUnreadConversationsBefore.bind(this);
        this.getUnreadConversationsAfter = this.getUnreadConversationsAfter.bind(this);
        this.getAllLastMessages = this.getAllLastMessages.bind(this);
        this.getAllMessagesAfter = this.getAllMessagesAfter.bind(this);
        this.getAllMessagesBefore = this.getAllMessagesBefore.bind(this);
        this.clearDb = this.clearDb.bind(this);

        // live-chat
        this.getChatProfile = this.getChatProfile.bind(this);
        this.getLiveChatToken = this.getLiveChatToken.bind(this);
        this.updateUserInfo = this.updateUserInfo.bind(this);
        this.startLiveChat = this.startLiveChat.bind(this);
        this.sendChatTranscript = this.sendChatTranscript.bind(this);
        this.endChat = this.endChat.bind(this);
        this.createTicketForMissedChat = this.createTicketForMissedChat.bind(this);
        this.acceptChatRequest = this.acceptChatRequest.bind(this);
        this.rejectChatRequest = this.rejectChatRequest.bind(this);

        // this.getChatRequests = this.getChatRequests.bind(this);
        // this.acceptChatRequest = this.acceptChatRequest.bind(this);
        // this.rejectChatRequest = this.rejectChatRequest.bind(this);
        // this.endChat = this.endChat.bind(this);
        // this.blockUser = this.blockUser.bind(this);
        // this.preventAddingToGroup = this.preventAddingToGroup.bind(this);
        // this.getChatProfile = this.getChatProfile.bind(this);
        // this.getLiveChatToken = this.getLiveChatToken.bind(this);
        // this.startLiveChat = this.startLiveChat.bind(this);
        // this.createLiveChatTicket = this.createLiveChatTicket.bind(this);
        // this.updateUser = this.updateUser.bind(this);
        // this.revokeMessages = this.revokeMessages.bind(this);
        // this.getLiveChat = this.getLiveChat.bind(this);
    }

    componentDidMount() {
        this.sanitizeClientEvents(this.props.eventHandlers);
    }

    componentWillUnmount() {
        // Keep events for android
        if (!iOS) {
            return;
        }
        this._unregisterEvents();
    }

    render() {
        return null;
    }

    _unregisterEvents() {
        this._subscriptions.forEach(e => e.remove());
        this._subscriptions = [];

        this._events.forEach(e => RNStringeeClient.removeNativeEvent(this.uuid, e));
        this._events = [];
    }

    sanitizeClientEvents(events) {
        if (typeof events !== "object") {
            return;
        }
        const platform = Platform.OS;

        if (iOS) {
            each(events, (handler, type) => {
                const eventName = clientEvents[platform][type];
                if (eventName !== undefined) {
                    // Voi phan chat can format du lieu
                    if (type === "onObjectChange") {
                        this._subscriptions.push(
                            this._eventEmitter.addListener(eventName, ({uuid, data}) => {
                                // Event cua thang khac
                                if (this.uuid !== uuid) {
                                    return;
                                }

                                var objectType = data["objectType"];
                                var objects = data["objects"];
                                var changeType = data["changeType"];

                                var objectChanges = [];
                                if (objectType === 0) {
                                    objects.map((object) => {
                                        objectChanges.push(new Conversation(object));
                                    });
                                } else if (objectType === 1) {
                                    objects.map((object) => {
                                        objectChanges.push(new Message(object));
                                    });
                                }
                                if (handler !== undefined) {
                                    handler({objectType, objectChanges, changeType});
                                }
                            })
                        );
                    } else {
                        this._subscriptions.push(this._eventEmitter.addListener(eventName, ({uuid, data}) => {
                            if (this.uuid === uuid) {
                                if (handler !== undefined) {
                                    handler(data);
                                }
                            }
                        }));
                    }

                    this._events.push(eventName);
                    RNStringeeClient.setNativeEvent(this.uuid, eventName);
                } else {
                    console.log(`${type} is not a supported event`);
                }
            });
        } else {
            each(events, (handler, type) => {
                const eventName = clientEvents[platform][type];
                if (eventName !== undefined) {
                    if (!this._events.includes(eventName)) {
                        // Voi phan chat can format du lieu
                        if (type === "onObjectChange") {
                            this._subscriptions.push(
                                this._eventEmitter.addListener(eventName, ({uuid, data}) => {
                                    if (this.uuid !== uuid) {
                                        return;
                                    }
                                    var objectType = data["objectType"];
                                    var objects = data["objects"];
                                    var changeType = data["changeType"];

                                    var objectChanges = [];
                                    if (objectType === 0) {
                                        objects.map((object) => {
                                            objectChanges.push(new Conversation(object));
                                        });
                                    } else if (objectType === 1) {
                                        objects.map((object) => {
                                            objectChanges.push(new Message(object));
                                        });
                                    }
                                    if (handler !== undefined) {
                                        handler({objectType, objectChanges, changeType});
                                    }
                                })
                            );
                        } else {
                            this._subscriptions.push(this._eventEmitter.addListener(eventName, ({uuid, data}) => {
                                if (this.uuid === uuid) {
                                    if (handler !== undefined) {
                                        handler(data);
                                    }
                                }
                            }));
                        }

                        this._events.push(eventName);
                        RNStringeeClient.setNativeEvent(this.uuid, eventName);
                    }
                } else {
                    console.log(`${type} is not a supported event`);
                }
            });
        }
    }

    getId() {
        return this.uuid;
    }

    connect(token: string) {
        RNStringeeClient.connect(this.uuid, token);
    }

    disconnect() {
        RNStringeeClient.disconnect(this.uuid);
    }

    registerPush(
        deviceToken: string,
        isProduction: boolean,
        isVoip: boolean,
        callback: RNStringeeEventCallback
    ) {
        if (iOS) {
            RNStringeeClient.registerPushForDeviceToken(
                this.uuid,
                deviceToken,
                isProduction,
                isVoip,
                callback
            );
        } else {
            RNStringeeClient.registerPushToken(this.uuid, deviceToken, callback);
        }
    }

    unregisterPush(deviceToken: string, callback: RNStringeeEventCallback) {
        RNStringeeClient.unregisterPushToken(this.uuid, deviceToken, callback);
    }

    sendCustomMessage(
        toUserId: string,
        message: string,
        callback: RNStringeeEventCallback
    ) {
        RNStringeeClient.sendCustomMessage(this.uuid, toUserId, message, callback);
    }

    createConversation(userIds: string, options, callback: RNStringeeEventCallback) {
        RNStringeeClient.createConversation(this.uuid, userIds, options, (status, code, message, conversation) => {
            var returnConversation;
            if (status) {
                returnConversation = new Conversation(conversation);
            }
            return callback(status, code, message, returnConversation);
        });
    }

    getConversationById(conversationId: string, callback: RNStringeeEventCallback) {
        RNStringeeClient.getConversationById(this.uuid, conversationId, (status, code, message, conversation) => {
            var returnConversation;
            if (status) {
                returnConversation = new Conversation(conversation);
            }
            return callback(status, code, message, returnConversation);
        });
    }

    getLocalConversations(userId: string, count: number, isAscending: boolean, callback: RNStringeeEventCallback) {
        var param = iOS ? count : userId;

        if (iOS) {
            // iOS su dung ca 2 tham so
            RNStringeeClient.getLocalConversations(this.uuid, count, userId, (status, code, message, conversations) => {
                var returnConversations = [];
                if (status) {
                    if (isAscending) {
                        conversations.reverse().map((conversation) => {
                            returnConversations.push(new Conversation(conversation));
                        });
                    } else {
                        conversations.map((conversation) => {
                            returnConversations.push(new Conversation(conversation));
                        });
                    }
                }
                return callback(status, code, message, returnConversations);
            });
        } else {
            // Android chi su dung userId
            RNStringeeClient.getLocalConversations(this.uuid, userId, (status, code, message, conversations) => {
                var returnConversations = [];
                if (status) {
                    if (isAscending) {
                        conversations.reverse().map((conversation) => {
                            returnConversations.push(new Conversation(conversation));
                        });
                    } else {
                        conversations.map((conversation) => {
                            returnConversations.push(new Conversation(conversation));
                        });
                    }
                }
                return callback(status, code, message, returnConversations);
            });
        }
    }

    getLastConversations(count: number, isAscending: boolean, callback: RNStringeeEventCallback) {
        RNStringeeClient.getLastConversations(this.uuid, count, (status, code, message, conversations) => {
            var returnConversations = [];
            if (status) {
                if (isAscending) {
                    // Tăng dần -> Cần đảo mảng
                    conversations.reverse().map((conversation) => {
                        returnConversations.push(new Conversation(conversation));
                    });
                } else {
                    conversations.map((conversation) => {
                        returnConversations.push(new Conversation(conversation));
                    });
                }
            }
            return callback(status, code, message, returnConversations);
        });
    }

    getAllLastConversations(count: number, isAscending: boolean, callback: RNStringeeEventCallback) {
        RNStringeeClient.getAllLastConversations(this.uuid, count, (status, code, message, conversations) => {
            var returnConversations = [];
            if (status) {
                if (isAscending) {
                    // Tăng dần -> Cần đảo mảng
                    conversations.reverse().map((conversation) => {
                        returnConversations.push(new Conversation(conversation));
                    });
                } else {
                    conversations.map((conversation) => {
                        returnConversations.push(new Conversation(conversation));
                    });
                }
            }
            return callback(status, code, message, returnConversations);
        });
    }

    getConversationsAfter(datetime: number, count: number, isAscending: boolean, callback: RNStringeeEventCallback) {
        RNStringeeClient.getConversationsAfter(this.uuid, datetime, count, (status, code, message, conversations) => {
            var returnConversations = [];
            if (status) {
                if (isAscending) {
                    conversations.reverse().map((conversation) => {
                        returnConversations.push(new Conversation(conversation));
                    });
                } else {
                    conversations.map((conversation) => {
                        returnConversations.push(new Conversation(conversation));
                    });
                }
            }
            return callback(status, code, message, returnConversations);
        });
    }

    getAllConversationsAfter(datetime: number, count: number, isAscending: boolean, callback: RNStringeeEventCallback) {
        RNStringeeClient.getAllConversationsAfter(this.uuid, datetime, count, (status, code, message, conversations) => {
            var returnConversations = [];
            if (status) {
                if (isAscending) {
                    conversations.reverse().map((conversation) => {
                        returnConversations.push(new Conversation(conversation));
                    });
                } else {
                    conversations.map((conversation) => {
                        returnConversations.push(new Conversation(conversation));
                    });
                }
            }
            return callback(status, code, message, returnConversations);
        });
    }

    getConversationsBefore(datetime: number, count: number, isAscending: boolean, callback: RNStringeeEventCallback) {
        RNStringeeClient.getConversationsBefore(this.uuid, datetime, count, (status, code, message, conversations) => {
            var returnConversations = [];
            if (status) {
                if (isAscending) {
                    conversations.reverse().map((conversation) => {
                        returnConversations.push(new Conversation(conversation));
                    });
                } else {
                    conversations.map((conversation) => {
                        returnConversations.push(new Conversation(conversation));
                    });
                }
            }
            return callback(status, code, message, returnConversations);
        });
    }

    getAllConversationsBefore(datetime: number, count: number, isAscending: boolean, callback: RNStringeeEventCallback) {
        RNStringeeClient.getAllConversationsBefore(this.uuid, datetime, count, (status, code, message, conversations) => {
            var returnConversations = [];
            if (status) {
                if (isAscending) {
                    conversations.reverse().map((conversation) => {
                        returnConversations.push(new Conversation(conversation));
                    });
                } else {
                    conversations.map((conversation) => {
                        returnConversations.push(new Conversation(conversation));
                    });
                }
            }
            return callback(status, code, message, returnConversations);
        });
    }

    getLastUnreadConversations(count: number, isAscending: boolean, callback: RNStringeeEventCallback) {
        RNStringeeClient.getLastUnreadConversations(this.uuid, count, (status, code, message, conversations) => {
            var returnConversations = [];
            if (status) {
                if (isAscending) {
                    // Tăng dần -> Cần đảo mảng
                    conversations.reverse().map((conversation) => {
                        returnConversations.push(new Conversation(conversation));
                    });
                } else {
                    conversations.map((conversation) => {
                        returnConversations.push(new Conversation(conversation));
                    });
                }
            }
            return callback(status, code, message, returnConversations);
        });
    }

    getUnreadConversationsAfter(datetime: number, count: number, isAscending: boolean, callback: RNStringeeEventCallback) {
        RNStringeeClient.getUnreadConversationsAfter(this.uuid, datetime, count, (status, code, message, conversations) => {
            var returnConversations = [];
            if (status) {
                if (isAscending) {
                    conversations.reverse().map((conversation) => {
                        returnConversations.push(new Conversation(conversation));
                    });
                } else {
                    conversations.map((conversation) => {
                        returnConversations.push(new Conversation(conversation));
                    });
                }
            }
            return callback(status, code, message, returnConversations);
        });
    }

    getUnreadConversationsBefore(datetime: number, count: number, isAscending: boolean, callback: RNStringeeEventCallback) {
        RNStringeeClient.getUnreadConversationsBefore(this.uuid, datetime, count, (status, code, message, conversations) => {
            var returnConversations = [];
            if (status) {
                if (isAscending) {
                    conversations.reverse().map((conversation) => {
                        returnConversations.push(new Conversation(conversation));
                    });
                } else {
                    conversations.map((conversation) => {
                        returnConversations.push(new Conversation(conversation));
                    });
                }
            }
            return callback(status, code, message, returnConversations);
        });
    }

    deleteConversation(conversationId: string, callback: RNStringeeEventCallback) {
        RNStringeeClient.deleteConversation(this.uuid, conversationId, callback);
    }

    addParticipants(conversationId: string, userIds, callback: RNStringeeEventCallback) {
        RNStringeeClient.addParticipants(this.uuid, conversationId, userIds, (status, code, message, users) => {
            var returnUsers = [];
            if (status) {
                users.map((user) => {
                    returnUsers.push(new User(user));
                });
            }
            return callback(status, code, message, returnUsers);
        });
    }

    removeParticipants(conversationId: string, userIds, callback: RNStringeeEventCallback) {
        RNStringeeClient.removeParticipants(this.uuid, conversationId, userIds, (status, code, message, users) => {
            var returnUsers = [];
            if (status) {
                users.map((user) => {
                    returnUsers.push(new User(user));
                });
            }
            return callback(status, code, message, returnUsers);
        });
    }

    updateConversation(conversationId: string, params, callback: RNStringeeEventCallback) {
        RNStringeeClient.updateConversation(this.uuid, conversationId, params, callback);
    }

    markConversationAsRead(conversationId: string, callback: RNStringeeEventCallback) {
        RNStringeeClient.markConversationAsRead(this.uuid, conversationId, callback);
    }

    getConversationWithUser(userId: string, callback: RNStringeeEventCallback) {
        RNStringeeClient.getConversationWithUser(this.uuid, userId, (status, code, message, conversation) => {
            var returnConversation;
            if (status) {
                returnConversation = new Conversation(conversation);
            }
            return callback(status, code, message, returnConversation);
        });
    }

    getUnreadConversationCount(callback: RNStringeeEventCallback) {
        RNStringeeClient.getUnreadConversationCount(this.uuid, callback);
    }

    sendMessage(message, callback: RNStringeeEventCallback) {
        RNStringeeClient.sendMessage(this.uuid, message, callback);
    }

    deleteMessage(conversationId: string, messageId: string, callback: RNStringeeEventCallback) {
        RNStringeeClient.deleteMessage(this.uuid, conversationId, messageId, callback);
    }

    getLocalMessages(conversationId: string, count: number, isAscending: boolean, callback: RNStringeeEventCallback) {
        RNStringeeClient.getLocalMessages(this.uuid, conversationId, count, (status, code, message, messages) => {
            var returnMessages = [];
            if (status) {
                if (isAscending) {
                    messages.map((msg) => {
                        returnMessages.push(new Message(msg));
                    });
                } else {
                    messages.reverse().map((msg) => {
                        returnMessages.push(new Message(msg));
                    });
                }
            }
            return callback(status, code, message, returnMessages);
        });
    }

    getLastMessages(conversationId: string, count: number, isAscending: boolean, loadDeletedMessage: boolean, loadDeletedMessageContent: boolean, callback: RNStringeeEventCallback) {
        RNStringeeClient.getLastMessages(this.uuid, conversationId, count, loadDeletedMessage, loadDeletedMessageContent, (status, code, message, messages) => {
            var returnMessages = [];
            if (status) {
                if (isAscending) {
                    messages.map((msg) => {
                        returnMessages.push(new Message(msg));
                    });
                } else {
                    messages.reverse().map((msg) => {
                        returnMessages.push(new Message(msg));
                    });
                }
            }
            return callback(status, code, message, returnMessages);
        });
    }

    getAllLastMessages(conversationId: string, count: number, isAscending: boolean, loadDeletedMessage: boolean, loadDeletedMessageContent: boolean, callback: RNStringeeEventCallback) {
        RNStringeeClient.getAllLastMessages(this.uuid, conversationId, count, loadDeletedMessage, loadDeletedMessageContent, (status, code, message, messages) => {
            var returnMessages = [];
            if (status) {
                if (isAscending) {
                    messages.map((msg) => {
                        returnMessages.push(new Message(msg));
                    });
                } else {
                    messages.reverse().map((msg) => {
                        returnMessages.push(new Message(msg));
                    });
                }
            }
            return callback(status, code, message, returnMessages);
        });
    }

    getMessagesAfter(conversationId: string, sequence: number, count: number, isAscending: boolean, loadDeletedMessage: boolean, loadDeletedMessageContent: boolean, callback: RNStringeeEventCallback) {
        RNStringeeClient.getMessagesAfter(this.uuid, conversationId, sequence, count, loadDeletedMessage, loadDeletedMessageContent, (status, code, message, messages) => {
            var returnMessages = [];
            if (status) {
                if (isAscending) {
                    messages.map((msg) => {
                        returnMessages.push(new Message(msg));
                    });
                } else {
                    messages.reverse().map((msg) => {
                        returnMessages.push(new Message(msg));
                    });
                }
            }
            return callback(status, code, message, returnMessages);
        });
    }

    getAllMessagesAfter(conversationId: string, sequence: number, count: number, isAscending: boolean, loadDeletedMessage: boolean, loadDeletedMessageContent: boolean, callback: RNStringeeEventCallback) {
        RNStringeeClient.getAllMessagesAfter(this.uuid, conversationId, sequence, count, loadDeletedMessage, loadDeletedMessageContent, (status, code, message, messages) => {
            var returnMessages = [];
            if (status) {
                if (isAscending) {
                    messages.map((msg) => {
                        returnMessages.push(new Message(msg));
                    });
                } else {
                    messages.reverse().map((msg) => {
                        returnMessages.push(new Message(msg));
                    });
                }
            }
            return callback(status, code, message, returnMessages);
        });
    }

    getMessagesBefore(conversationId: string, sequence: number, count: number, isAscending: boolean, loadDeletedMessage: boolean, loadDeletedMessageContent: boolean, callback: RNStringeeEventCallback) {
        RNStringeeClient.getMessagesBefore(this.uuid, conversationId, sequence, count, loadDeletedMessage, loadDeletedMessageContent, (status, code, message, messages) => {
            var returnMessages = [];
            if (status) {
                if (isAscending) {
                    messages.map((msg) => {
                        returnMessages.push(new Message(msg));
                    });
                } else {
                    messages.reverse().map((msg) => {
                        returnMessages.push(new Message(msg));
                    });
                }
            }
            return callback(status, code, message, returnMessages);
        });
    }

    getAllMessagesBefore(conversationId: string, sequence: number, count: number, isAscending: boolean, loadDeletedMessage: boolean, loadDeletedMessageContent: boolean, callback: RNStringeeEventCallback) {
        RNStringeeClient.getAllMessagesBefore(this.uuid, conversationId, sequence, count, loadDeletedMessage, loadDeletedMessageContent, (status, code, message, messages) => {
            var returnMessages = [];
            if (status) {
                if (isAscending) {
                    messages.map((msg) => {
                        returnMessages.push(new Message(msg));
                    });
                } else {
                    messages.reverse().map((msg) => {
                        returnMessages.push(new Message(msg));
                    });
                }
            }
            return callback(status, code, message, returnMessages);
        });
    }

    clearDb(callback: RNStringeeEventCallback) {
        RNStringeeClient.clearDb(this.uuid, callback);
    }

    // ============================== LIVE-CHAT ================================

    // ===== CUSTOMER-SIDE =====

    // 1
    getChatProfile(widgetKey: string, callback: RNStringeeEventCallback) {
        RNStringeeClient.getChatProfile(this.uuid, widgetKey, callback);
    }

    // 2
    getLiveChatToken(widgetKey: string, name: string, email: string, callback: RNStringeeEventCallback) {
        RNStringeeClient.getLiveChatToken(this.uuid, widgetKey, name, email, callback);
    }

    // 3
    updateUserInfo(name: string, email: string, avatar: string, callback: RNStringeeEventCallback) {
        RNStringeeClient.updateUserInfo(this.uuid, name, email, avatar, callback);
    }

    // 4
    startLiveChat(queueId: string, callback: RNStringeeEventCallback) {
        RNStringeeClient.startLiveChat(this.uuid, queueId, callback);
    }

    // 5
    sendChatTranscript(email: string, convId: string, domain: string, callback: RNStringeeEventCallback) {
        RNStringeeClient.sendChatTranscript(this.uuid, email, convId, domain, callback);
    }

    // 6
    endChat(convId: string, callback: RNStringeeEventCallback) {
        RNStringeeClient.endChat(this.uuid, convId, callback);
    }

    createTicketForMissedChat(widgetKey: string, name: string, email: string, note: string, callback: RNStringeeEventCallback) {
        RNStringeeClient.createTicketForMissedChat(this.uuid, widgetKey, name, email, note, callback);
    }

    // ===== AGENT-SIDE =====

    acceptChatRequest(request: ChatRequest, callback: RNStringeeEventCallback) {
        RNStringeeClient.acceptChatRequest(this.uuid, request.id, callback);
    }

    rejectChatRequest(request: ChatRequest, callback: RNStringeeEventCallback) {
        RNStringeeClient.rejectChatRequest(this.uuid, request.id, callback);
    }






    // getChatRequests(count: number, isAscending: boolean, callback: RNStringeeEventCallback) {
    //     RNStringeeClient.getChatRequests(this.uuid, (status, code, message, chatRequests) => {
    //         var returnChatRequests = [];
    //         if (status) {
    //             if (isAscending) {
    //                 // Tăng dần -> Cần đảo mảng
    //                 chatRequests.reverse().map((chatRequest) => {
    //                     returnChatRequests.push(new ChatRequest(chatRequest));
    //                 });
    //             } else {
    //                 chatRequests.map((conversation) => {
    //                     returnChatRequests.push(new Conversation(conversation));
    //                 });
    //             }
    //         }
    //         return callback(status, code, message, returnChatRequests);
    //     });
    // }
    //
    // acceptChatRequest(conversationId: string, channelType: channelType, callback: RNStringeeEventCallback) {
    //     RNStringeeClient.acceptChatRequest(this.uuid, conversationId, channelType, callback);
    // }
    //
    // rejectChatRequest(conversationId: string, channelType: channelType, callback: RNStringeeEventCallback) {
    //     RNStringeeClient.rejectChatRequest(this.uuid, conversationId, channelType, callback);
    // }
    //
    // endChat(conversationId: string, callback: RNStringeeEventCallback) {
    //     RNStringeeClient.endChat(this.uuid, conversationId, callback);
    // }
    //
    // blockUser(userId: string, callback: RNStringeeEventCallback) {
    //     RNStringeeClient.blockUser(this.uuid, userId, callback);
    // }
    //
    // preventAddingToGroup(conversationId: string, callback: RNStringeeEventCallback) {
    //     RNStringeeClient.preventAddingToGroup(this.uuid, conversationId, callback);
    // }
    //
    // getChatProfile(widgetKey: string, callback: RNStringeeEventCallback) {
    //     RNStringeeClient.getChatProfile(this.uuid, widgetKey, callback);
    // }
    //
    // getLiveChatToken(widgetKey: string, name: string, email: string, callback: RNStringeeEventCallback) {
    //     RNStringeeClient.getLiveChatToken(this.uuid, widgetKey, name, email, callback);
    // }
    //
    // startLiveChat(queueId: string, callback: RNStringeeEventCallback) {
    //     RNStringeeClient.startLiveChat(this.uuid, queueId, callback);
    // }
    //
    // createLiveChatTicket(widgetKey: string, name: string, email: string, note: string, callback: RNStringeeEventCallback) {
    //     RNStringeeClient.createLiveChatTicket(this.uuid, widgetKey, name, email, note, callback);
    // }
    //
    // updateUser(name: string, email: string, avatar: string, callback: RNStringeeEventCallback) {
    //     RNStringeeClient.updateUser(this.uuid, name, email, avatar, callback);
    // }
    //
    // revokeMessages(conversationId: string, msgIds, deleted: boolean, callback: RNStringeeEventCallback) {
    //     RNStringeeClient.revokeMessages(this.uuid, conversationId, msgIds, deleted, callback);
    // }
    //
    // getLiveChat(ended: boolean, callback: RNStringeeEventCallback) {
    //     RNStringeeClient.getLiveChat(this.uuid, ended, callback);
    // }
}
