import PropTypes from "prop-types";
import {Component} from "react";
import {NativeEventEmitter, NativeModules, Platform} from "react-native";
import {each} from "underscore";
import Conversation from "./chat/Conversation";
import Message from "./chat/Message";
import User from "./chat/User";
import ChatRequest from './chat/ChatRequest';
import {clientEvents} from "./helpers/StringeeHelper";
import type {LiveChatTicketParam, UserInfoParam, RNStringeeEventCallback} from './helpers/StringeeHelper';

const RNStringeeClient = NativeModules.RNStringeeClient;

const iOS = (Platform.OS === "ios");

export default class extends Component {
  static propTypes = {
    eventHandlers: PropTypes.object,
    baseUrl: PropTypes.string,
    serverAddresses: PropTypes.array,
    stringeeXBaseUrl: PropTypes.string
  };

  constructor(props) {
    super(props);
    this._events = [];
    this._subscriptions = [];
    this._eventEmitter = new NativeEventEmitter(RNStringeeClient);

    // Sinh uuid va tao wrapper object trong native
    this.uuid = Math.random().toString(36).substring(2, 15) + Math.random().toString(36).substring(2, 15) + Math.random().toString(36).substring(2, 15) + Math.random().toString(36).substring(2, 15);
    RNStringeeClient.createClientWrapper(this.uuid, this.props.baseUrl, this.props.serverAddresses, this.props.stringeeXBaseUrl);

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
    this.sendBeginTyping = this.sendBeginTyping.bind(this);
    this.sendEndTyping = this.sendEndTyping.bind(this);
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

    this.pinMessage = this.pinMessage.bind(this);
    this.editMessage = this.editMessage.bind(this);
    this.revokeMessage = this.revokeMessage.bind(this);
    this.getMessageById = this.getMessageById.bind(this);

    // live-chat
    this.getChatProfile = this.getChatProfile.bind(this);
    this.getLiveChatToken = this.getLiveChatToken.bind(this);
    this.updateUserInfo = this.updateUserInfo.bind(this);
    this.updateUserInfoWithParam = this.updateUserInfoWithParam.bind(this);
    this.createLiveChatConversation = this.createLiveChatConversation.bind(this);
    this.createLiveChatTicket = this.createLiveChatTicket.bind(this);
    this.createLiveChatTicketWithParam = this.createLiveChatTicketWithParam.bind(this);
    this.sendChatTranscript = this.sendChatTranscript.bind(this);
    this.endChat = this.endChat.bind(this);
    this.acceptChatRequest = this.acceptChatRequest.bind(this);
    this.rejectChatRequest = this.rejectChatRequest.bind(this);
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
        } else if (type === "onReceiveChatRequest" || type === "onReceiveTransferChatRequest" || type === "onTimeoutAnswerChat") {
          this._subscriptions.push(this._eventEmitter.addListener(eventName, ({uuid, data}) => {
            if (this.uuid !== uuid) {
              return;
            }

            var requestData = data["request"];
            var request = new ChatRequest(requestData);

            if (handler !== undefined) {
              handler({request});
            }
          }));
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

  registerPush(deviceToken: string, isProduction: boolean, isVoip: boolean, callback: RNStringeeEventCallback) {
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

  sendCustomMessage(toUserId: string, message: string, callback: RNStringeeEventCallback) {
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

  getConversationById(convId: string, callback: RNStringeeEventCallback) {
    RNStringeeClient.getConversationById(this.uuid, convId, (status, code, message, conversation) => {
      var returnConversation;
      if (status) {
        returnConversation = new Conversation(conversation);
      }
      return callback(status, code, message, returnConversation);
    });
  }

  getLocalConversations(userId: string, count: number, isAscending: boolean, callback: RNStringeeEventCallback) {
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

  deleteConversation(convId: string, callback: RNStringeeEventCallback) {
    RNStringeeClient.deleteConversation(this.uuid, convId, callback);
  }

  addParticipants(convId: string, userIds, callback: RNStringeeEventCallback) {
    RNStringeeClient.addParticipants(this.uuid, convId, userIds, (status, code, message, users) => {
      var returnUsers = [];
      if (status) {
        users.map((user) => {
          returnUsers.push(new User(user));
        });
      }
      return callback(status, code, message, returnUsers);
    });
  }

  removeParticipants(convId: string, userIds, callback: RNStringeeEventCallback) {
    RNStringeeClient.removeParticipants(this.uuid, convId, userIds, (status, code, message, users) => {
      var returnUsers = [];
      if (status) {
        users.map((user) => {
          returnUsers.push(new User(user));
        });
      }
      return callback(status, code, message, returnUsers);
    });
  }

  updateConversation(convId: string, params, callback: RNStringeeEventCallback) {
    RNStringeeClient.updateConversation(this.uuid, convId, params, callback);
  }

  markConversationAsRead(convId: string, callback: RNStringeeEventCallback) {
    RNStringeeClient.markConversationAsRead(this.uuid, convId, callback);
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

  sendBeginTyping(convId: string, callback: RNStringeeEventCallback) {
    RNStringeeClient.sendBeginTyping(this.uuid, convId, callback);
  }

  sendEndTyping(convId: string, callback: RNStringeeEventCallback) {
    RNStringeeClient.sendEndTyping(this.uuid, convId, callback);
  }

  sendMessage(message, callback: RNStringeeEventCallback) {
    RNStringeeClient.sendMessage(this.uuid, message, callback);
  }

  deleteMessage(convId: string, messageId: string, callback: RNStringeeEventCallback) {
    RNStringeeClient.deleteMessage(this.uuid, convId, messageId, callback);
  }

  pinMessage(convId: string, messageId: string, pin: boolean, callback: RNStringeeEventCallback) {
    RNStringeeClient.pinMessage(this.uuid, convId, messageId, pin, callback);
  }

  editMessage(convId: string, messageId: string, newContent: string, callback: RNStringeeEventCallback) {
    RNStringeeClient.editMessage(this.uuid, convId, messageId, newContent, callback);
  }

  revokeMessage(convId: string, messageId: string, callback: RNStringeeEventCallback) {
    RNStringeeClient.revokeMessage(this.uuid, convId, messageId, callback);
  }

  getLocalMessages(convId: string, count: number, isAscending: boolean, callback: RNStringeeEventCallback) {
    RNStringeeClient.getLocalMessages(this.uuid, convId, count, (status, code, message, messages) => {
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

  getLastMessages(convId: string, count: number, isAscending: boolean, loadDeletedMessage: boolean, loadDeletedMessageContent: boolean, callback: RNStringeeEventCallback) {
    RNStringeeClient.getLastMessages(this.uuid, convId, count, loadDeletedMessage, loadDeletedMessageContent, (status, code, message, messages) => {
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

  getAllLastMessages(convId: string, count: number, isAscending: boolean, loadDeletedMessage: boolean, loadDeletedMessageContent: boolean, callback: RNStringeeEventCallback) {
    RNStringeeClient.getAllLastMessages(this.uuid, convId, count, loadDeletedMessage, loadDeletedMessageContent, (status, code, message, messages) => {
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

  getMessagesAfter(convId: string, sequence: number, count: number, isAscending: boolean, loadDeletedMessage: boolean, loadDeletedMessageContent: boolean, callback: RNStringeeEventCallback) {
    RNStringeeClient.getMessagesAfter(this.uuid, convId, sequence, count, loadDeletedMessage, loadDeletedMessageContent, (status, code, message, messages) => {
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

  getAllMessagesAfter(convId: string, sequence: number, count: number, isAscending: boolean, loadDeletedMessage: boolean, loadDeletedMessageContent: boolean, callback: RNStringeeEventCallback) {
    RNStringeeClient.getAllMessagesAfter(this.uuid, convId, sequence, count, loadDeletedMessage, loadDeletedMessageContent, (status, code, message, messages) => {
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

  getMessagesBefore(convId: string, sequence: number, count: number, isAscending: boolean, loadDeletedMessage: boolean, loadDeletedMessageContent: boolean, callback: RNStringeeEventCallback) {
    RNStringeeClient.getMessagesBefore(this.uuid, convId, sequence, count, loadDeletedMessage, loadDeletedMessageContent, (status, code, message, messages) => {
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

  getAllMessagesBefore(convId: string, sequence: number, count: number, isAscending: boolean, loadDeletedMessage: boolean, loadDeletedMessageContent: boolean, callback: RNStringeeEventCallback) {
    RNStringeeClient.getAllMessagesBefore(this.uuid, convId, sequence, count, loadDeletedMessage, loadDeletedMessageContent, (status, code, message, messages) => {
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

  getMessageById(convId: string, messageId: string, callback: RNStringeeEventCallback) {
    RNStringeeClient.getMessageById(this.uuid, convId, messageId, callback);
  }

  clearDb(callback: RNStringeeEventCallback) {
    RNStringeeClient.clearDb(this.uuid, callback);
  }


  // ============================== LIVE-CHAT ================================

  // ===== CUSTOMER-SIDE =====

  getChatProfile(widgetKey: string, callback: RNStringeeEventCallback) {
    RNStringeeClient.getChatProfile(this.uuid, widgetKey, callback);
  }

  getLiveChatToken(widgetKey: string, name: string, email: string, callback: RNStringeeEventCallback) {
    RNStringeeClient.getLiveChatToken(this.uuid, widgetKey, name, email, callback);
  }

  updateUserInfo(name: string, email: string, avatar: string, callback: RNStringeeEventCallback) {
    RNStringeeClient.updateUserInfo(this.uuid, name, email, avatar, '', callback);
  }

  updateUserInfoWithParam(param: UserInfoParam, callback: RNStringeeEventCallback) {
    RNStringeeClient.updateUserInfo(this.uuid, param.name, param.email, param.avatar, param.phone, callback);
  }

  createLiveChatConversation(queueId: string, callback: RNStringeeEventCallback) {
    RNStringeeClient.createLiveChatConversation(this.uuid, queueId, (status, code, message, data) => {
      var returnConversation;
      if (status) {
        returnConversation = new Conversation(data);
      }
      return callback(status, code, message, returnConversation);
    });
  }

  // ===== AGENT-SIDE =====

  acceptChatRequest(request: ChatRequest, callback: RNStringeeEventCallback) {
    RNStringeeClient.acceptChatRequest(this.uuid, request.convId, callback);
  }

  rejectChatRequest(request: ChatRequest, callback: RNStringeeEventCallback) {
    RNStringeeClient.rejectChatRequest(this.uuid, request.convId, callback);
  }

  createLiveChatTicket(widgetKey: string, name: string, email: string, note: string, callback: RNStringeeEventCallback) {
    RNStringeeClient.createLiveChatTicket(this.uuid, widgetKey, name, email, '', note, callback);
  }

  createLiveChatTicketWithParam(widgetKey: string, param: LiveChatTicketParam, callback: RNStringeeEventCallback) {
    RNStringeeClient.createLiveChatTicket(this.uuid, widgetKey, param.name, param.email, param.phone, param.note, callback);
  }

  // ===== ALL-SIDE =====

  sendChatTranscript(email: string, convId: string, domain: string, callback: RNStringeeEventCallback) {
    RNStringeeClient.sendChatTranscript(this.uuid, email, convId, domain, callback);
  }

  endChat(convId: string, callback: RNStringeeEventCallback) {
    RNStringeeClient.endChat(this.uuid, convId, callback);
  }
}
