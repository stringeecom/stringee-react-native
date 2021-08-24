import PropTypes from "prop-types";
import {Component} from "react";
import {NativeEventEmitter, NativeModules, Platform} from "react-native";
import {each} from "underscore";
import Conversation from "./chat/Conversation";
import Message from "./chat/Message";
import User from "./chat/User";
import {clientEvents} from "./helpers/StringeeHelper";

const RNStringeeClient = NativeModules.RNStringeeClient;

const iOS = Platform.OS === "ios" ? true : false;

export default class extends Component {
    static propTypes = {
        eventHandlers: PropTypes.object,
        baseUrl: PropTypes.string,
        serverAddresses: PropTypes.array
    };

    constructor(props) {
        super(props);
        this._events = [];
        this._subscriptions = [];
        this._eventEmitter = new NativeEventEmitter(RNStringeeClient);

        // Sinh uuid va tao wrapper object trong native
        this.uuid = Math.random().toString(36).substring(2, 15) + Math.random().toString(36).substring(2, 15) + Math.random().toString(36).substring(2, 15) + Math.random().toString(36).substring(2, 15);
        RNStringeeClient.createClientWrapper(this.uuid, this.props.baseUrl, this.props.serverAddresses);

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
    }

    componentWillMount() {
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
                    if (type == "onObjectChange") {
                        this._subscriptions.push(
                            this._eventEmitter.addListener(eventName, ({uuid, data}) => {
                                // Event cua thang khac
                                if (this.uuid != uuid) {
                                    return;
                                }

                                var objectType = data["objectType"];
                                var objects = data["objects"];
                                var changeType = data["changeType"];

                                var objectChanges = [];
                                if (objectType == 0) {
                                    objects.map((object) => {
                                        objectChanges.push(new Conversation(object));
                                    });
                                } else if (objectType == 1) {
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
                            if (this.uuid == uuid) {
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
                        if (type == "onObjectChange") {
                            this._subscriptions.push(
                                this._eventEmitter.addListener(eventName, ({uuid, data}) => {
                                    if (this.uuid != uuid) {
                                        return;
                                    }
                                    var objectType = data["objectType"];
                                    var objects = data["objects"];
                                    var changeType = data["changeType"];

                                    var objectChanges = [];
                                    if (objectType == 0) {
                                        objects.map((object) => {
                                            objectChanges.push(new Conversation(object));
                                        });
                                    } else if (objectType == 1) {
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
                                if (this.uuid == uuid) {
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

    createConversation(userIds, options, callback) {
        RNStringeeClient.createConversation(this.uuid, userIds, options, (status, code, message, conversation) => {
            var returnConversation;
            if (status) {
                returnConversation = new Conversation(conversation);
            }
            return callback(status, code, message, returnConversation);
        });
    }

    getConversationById(conversationId, callback) {
        RNStringeeClient.getConversationById(this.uuid, conversationId, (status, code, message, conversation) => {
            var returnConversation;
            if (status) {
                returnConversation = new Conversation(conversation);
            }
            return callback(status, code, message, returnConversation);
        });
    }

    getLocalConversations(userId: string, count, isAscending, callback) {
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

    getLastConversations(count, isAscending, callback) {
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

    getAllLastConversations(count, isAscending, callback) {
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

    getConversationsAfter(datetime, count, isAscending, callback) {
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

    getAllConversationsAfter(datetime, count, isAscending, callback) {
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

    getConversationsBefore(datetime, count, isAscending, callback) {
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

    getAllConversationsBefore(datetime, count, isAscending, callback) {
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

    getLastUnreadConversations(count, isAscending, callback) {
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

    getUnreadConversationsAfter(datetime, count, isAscending, callback) {
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

    getUnreadConversationsBefore(datetime, count, isAscending, callback) {
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

    deleteConversation(conversationId, callback) {
        RNStringeeClient.deleteConversation(this.uuid, conversationId, callback);
    }

    addParticipants(conversationId, userIds, callback) {
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

    removeParticipants(conversationId, userIds, callback) {
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

    updateConversation(conversationId, params, callback) {
        RNStringeeClient.updateConversation(this.uuid, conversationId, params, callback);
    }

    markConversationAsRead(conversationId, callback) {
        RNStringeeClient.markConversationAsRead(this.uuid, conversationId, callback);
    }

    getConversationWithUser(userId, callback) {
        RNStringeeClient.getConversationWithUser(this.uuid, userId, (status, code, message, conversation) => {
            var returnConversation;
            if (status) {
                returnConversation = new Conversation(conversation);
            }
            return callback(status, code, message, returnConversation);
        });
    }

    getUnreadConversationCount(callback) {
        RNStringeeClient.getUnreadConversationCount(this.uuid, callback);
    }

    sendMessage(message, callback) {
        RNStringeeClient.sendMessage(this.uuid, message, callback);
    }

    deleteMessage(conversationId, messageId, callback) {
        RNStringeeClient.deleteMessage(this.uuid, conversationId, messageId, callback);
    }

    getLocalMessages(conversationId, count, isAscending, callback) {
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

    getLastMessages(conversationId, count, isAscending, loadDeletedMessage, loadDeletedMessageContent, callback) {
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

    getAllLastMessages(conversationId, count, isAscending, loadDeletedMessage, loadDeletedMessageContent, callback) {
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

    getMessagesAfter(conversationId, sequence, count, isAscending, loadDeletedMessage, loadDeletedMessageContent, callback) {
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

    getAllMessagesAfter(conversationId, sequence, count, isAscending, loadDeletedMessage, loadDeletedMessageContent, callback) {
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

    getMessagesBefore(conversationId, sequence, count, isAscending, loadDeletedMessage, loadDeletedMessageContent, callback) {
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

    getAllMessagesBefore(conversationId, sequence, count, isAscending, loadDeletedMessage, loadDeletedMessageContent, callback) {
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

    clearDb(callback) {
        RNStringeeClient.clearDb(this.uuid, callback);
    }
}
