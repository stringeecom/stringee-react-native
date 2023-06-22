import PropTypes from 'prop-types';
import {Component} from 'react';
import {NativeEventEmitter, NativeModules, Platform, View} from 'react-native';
import {each} from 'underscore';
import {clientEvents} from './helpers/StringeeHelper';
import type {RNStringeeEventCallback} from './helpers/StringeeHelper';
import {
  ChatRequest,
  Conversation,
  LiveChatTicketParam,
  Message,
  StringeeCall,
  StringeeCall2,
  StringeeServerAddress,
  User,
  UserInfoParam,
} from '../index';

const RNStringeeClient = NativeModules.RNStringeeClient;

const iOS = Platform.OS === 'ios';

interface StringeeClientProps {
  baseUrl: string;
  stringeeXBaseUrl: string;
  serverAddresses: Array<StringeeServerAddress>;
}

class StringeeClient extends Component {
  userId: string;
  uuid: string;
  baseUrl: string;
  stringeeXBaseUrl: string;
  serverAddresses: Array<StringeeServerAddress>;
  isConnected: boolean;

  constructor(props: StringeeClientProps) {
    super(props);
    if (this.props === undefined) {
      this.props = {};
    }
    this.baseUrl = this.props.baseUrl;
    this.stringeeXBaseUrl = this.props.stringeeXBaseUrl;
    this.serverAddresses = this.props.serverAddresses;
    this.events = [];
    this.subscriptions = [];
    this.eventEmitter = new NativeEventEmitter(RNStringeeClient);

    // Sinh uuid va tao wrapper object trong native
    this.uuid =
      Math.random().toString(36).substring(2, 15) +
      Math.random().toString(36).substring(2, 15) +
      Math.random().toString(36).substring(2, 15) +
      Math.random().toString(36).substring(2, 15);

    RNStringeeClient.createClientWrapper(
      this.uuid,
      this.baseUrl,
      this.serverAddresses,
      this.stringeeXBaseUrl,
    );

    this.registerEvents = this.registerEvents.bind(this);
    this.unregisterEvents = this.unregisterEvents.bind(this);
    this.getId = this.getId.bind(this);
    this.connect = this.connect.bind(this);
    this.disconnect = this.disconnect.bind(this);
    this.registerPush = this.registerPush.bind(this);
    this.registerPushAndDeleteOthers =
      this.registerPushAndDeleteOthers.bind(this);
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
    this.getUnreadConversationCount =
      this.getUnreadConversationCount.bind(this);
    this.sendBeginTyping = this.sendBeginTyping.bind(this);
    this.sendEndTyping = this.sendEndTyping.bind(this);
    this.sendMessage = this.sendMessage.bind(this);
    this.deleteMessage = this.deleteMessage.bind(this);
    this.getLocalMessages = this.getLocalMessages.bind(this);
    this.getLastMessages = this.getLastMessages.bind(this);
    this.getMessagesAfter = this.getMessagesAfter.bind(this);
    this.getMessagesBefore = this.getMessagesBefore.bind(this);
    this.getLastUnreadConversations =
      this.getLastUnreadConversations.bind(this);
    this.getUnreadConversationsBefore =
      this.getUnreadConversationsBefore.bind(this);
    this.getUnreadConversationsAfter =
      this.getUnreadConversationsAfter.bind(this);
    this.getAllLastMessages = this.getAllLastMessages.bind(this);
    this.getAllMessagesAfter = this.getAllMessagesAfter.bind(this);
    this.getAllMessagesBefore = this.getAllMessagesBefore.bind(this);
    this.clearDb = this.clearDb.bind(this);
    this.getUserInfo = this.getUserInfo.bind(this);

    this.pinMessage = this.pinMessage.bind(this);
    this.editMessage = this.editMessage.bind(this);
    this.revokeMessage = this.revokeMessage.bind(this);
    this.getMessageById = this.getMessageById.bind(this);

    // live-chat
    this.getChatProfile = this.getChatProfile.bind(this);
    this.getLiveChatToken = this.getLiveChatToken.bind(this);
    this.updateUserInfo = this.updateUserInfo.bind(this);
    this.updateUserInfoWithParam = this.updateUserInfoWithParam.bind(this);
    this.createLiveChatConversation =
      this.createLiveChatConversation.bind(this);
    this.createLiveChatTicket = this.createLiveChatTicket.bind(this);
    this.createLiveChatTicketWithParam =
      this.createLiveChatTicketWithParam.bind(this);
    this.sendChatTranscript = this.sendChatTranscript.bind(this);
    this.endChat = this.endChat.bind(this);
    this.acceptChatRequest = this.acceptChatRequest.bind(this);
    this.rejectChatRequest = this.rejectChatRequest.bind(this);
  }

  componentDidMount() {
    this.registerEvents(this.props.eventHandlers);
  }

  componentWillUnmount() {
    // Keep events for android
    if (!iOS) {
      return;
    }
    this.unregisterEvents();
  }

  render() {
    return null;
  }

  registerEvents(eventHandlers) {
    if (
      eventHandlers === undefined ||
      typeof eventHandlers !== 'object' ||
      (this.events.length !== 0 && this.subscriptions.length !== 0)
    ) {
      return;
    }
    const platform = Platform.OS;

    each(eventHandlers, (handler, type) => {
      const eventName = clientEvents[platform][type];
      if (eventName !== undefined) {
        if (type === 'onIncomingCall' || type === 'onIncomingCall2') {
          this.subscriptions.push(
            this.eventEmitter.addListener(eventName, ({uuid, data}) => {
              if (this.uuid !== uuid) {
                return;
              }
              if (type === 'onIncomingCall') {
                const stringeeCall = new StringeeCall(data);
                if (handler !== undefined) {
                  handler({stringeeCall});
                }
              } else {
                const stringeeCall2 = new StringeeCall2(data);
                if (handler !== undefined) {
                  handler({stringeeCall2});
                }
              }
            }),
          );
          if (type === 'onIncomingCall') {
            this.events.push(eventName);
            this.events.push('onIncomingCallObject');
            RNStringeeClient.setNativeEvent(this.uuid, eventName);
            RNStringeeClient.setNativeEvent(this.uuid, 'onIncomingCallObject');
          } else {
            this.events.push(eventName);
            this.events.push('onIncomingCall2Object');
            RNStringeeClient.setNativeEvent(this.uuid, eventName);
            RNStringeeClient.setNativeEvent(this.uuid, 'onIncomingCall2Object');
          }
        } else {
          // Voi phan chat can format du lieu
          if (type === 'onObjectChange') {
            this.subscriptions.push(
              this.eventEmitter.addListener(eventName, ({uuid, data}) => {
                // Event cua thang khac
                if (this.uuid !== uuid) {
                  return;
                }

                const objectType = data.objectType;
                const objects = data.objects;
                const changeType = data.changeType;

                let objectChanges = [];
                if (objectType === 0) {
                  objects.map(object => {
                    objectChanges.push(new Conversation(object));
                  });
                } else if (objectType === 1) {
                  objects.map(object => {
                    objectChanges.push(new Message(object));
                  });
                }
                if (handler !== undefined) {
                  handler({objectType, objectChanges, changeType});
                }
              }),
            );
          } else if (
            type === 'onReceiveChatRequest' ||
            type === 'onReceiveTransferChatRequest' ||
            type === 'onTimeoutAnswerChat'
          ) {
            this.subscriptions.push(
              this.eventEmitter.addListener(eventName, ({uuid, data}) => {
                if (this.uuid !== uuid) {
                  return;
                }

                const requestData = data.request;
                const request = new ChatRequest(requestData);

                if (handler !== undefined) {
                  handler({request});
                }
              }),
            );
          } else {
            this.subscriptions.push(
              this.eventEmitter.addListener(eventName, ({uuid, data}) => {
                if (this.uuid === uuid) {
                  if (handler !== undefined) {
                    if (type === 'onConnect') {
                      this.isConnected = true;
                      this.userId = data.userId;
                    } else if (
                      type === 'onDisConnect' ||
                      type === 'onFailWithError'
                    ) {
                      this.isConnected = false;
                    }
                    handler(data);
                  }
                }
              }),
            );
          }
          this.events.push(eventName);
          RNStringeeClient.setNativeEvent(this.uuid, eventName);
        }
      } else {
        console.warn(`${type} is not a supported event`);
      }
    });
  }

  unregisterEvents() {
    if (this.events.length === 0 && this.subscriptions.length === 0) {
      return;
    }
    this.subscriptions.forEach(e => e.remove());
    this.subscriptions = [];

    this.events.forEach(e => RNStringeeClient.removeNativeEvent(this.uuid, e));
    this.events = [];
  }

  getId(): string {
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
    callback: RNStringeeEventCallback,
  ) {
    if (iOS) {
      RNStringeeClient.registerPushForDeviceToken(
        this.uuid,
        deviceToken,
        isProduction,
        isVoip,
        callback,
      );
    } else {
      RNStringeeClient.registerPushToken(this.uuid, deviceToken, callback);
    }
  }

  registerPushAndDeleteOthers(
    deviceToken: string,
    isProduction: boolean,
    isVoip: boolean,
    packageNames: Array<string>,
    callback: RNStringeeEventCallback,
  ) {
    if (iOS) {
      RNStringeeClient.registerPushAndDeleteOthers(
        this.uuid,
        deviceToken,
        isProduction,
        isVoip,
        packageNames,
        callback,
      );
    } else {
      RNStringeeClient.registerPushAndDeleteOthers(
        this.uuid,
        deviceToken,
        packageNames,
        callback,
      );
    }
  }

  unregisterPush(deviceToken: string, callback: RNStringeeEventCallback) {
    RNStringeeClient.unregisterPushToken(this.uuid, deviceToken, callback);
  }

  sendCustomMessage(
    toUserId: string,
    message: string,
    callback: RNStringeeEventCallback,
  ) {
    RNStringeeClient.sendCustomMessage(this.uuid, toUserId, message, callback);
  }

  createConversation(
    userIds: string,
    options,
    callback: RNStringeeEventCallback,
  ) {
    RNStringeeClient.createConversation(
      this.uuid,
      userIds,
      options,
      (status, code, message, conversation) => {
        let returnConversation;
        if (status) {
          returnConversation = new Conversation(conversation);
        }
        return callback(status, code, message, returnConversation);
      },
    );
  }

  getConversationById(convId: string, callback: RNStringeeEventCallback) {
    RNStringeeClient.getConversationById(
      this.uuid,
      convId,
      (status, code, message, conversation) => {
        let returnConversation;
        if (status) {
          returnConversation = new Conversation(conversation);
        }
        return callback(status, code, message, returnConversation);
      },
    );
  }

  getLocalConversations(
    userId: string,
    count: number,
    isAscending: boolean,
    callback: RNStringeeEventCallback,
  ) {
    if (iOS) {
      // iOS su dung ca 2 tham so
      RNStringeeClient.getLocalConversations(
        this.uuid,
        count,
        userId,
        (status, code, message, conversations) => {
          let returnConversations = [];
          if (status) {
            if (isAscending) {
              conversations.reverse().map(conversation => {
                returnConversations.push(new Conversation(conversation));
              });
            } else {
              conversations.map(conversation => {
                returnConversations.push(new Conversation(conversation));
              });
            }
          }
          return callback(status, code, message, returnConversations);
        },
      );
    } else {
      // Android chi su dung userId
      RNStringeeClient.getLocalConversations(
        this.uuid,
        userId,
        (status, code, message, conversations) => {
          let returnConversations = [];
          if (status) {
            if (isAscending) {
              conversations.reverse().map(conversation => {
                returnConversations.push(new Conversation(conversation));
              });
            } else {
              conversations.map(conversation => {
                returnConversations.push(new Conversation(conversation));
              });
            }
          }
          return callback(status, code, message, returnConversations);
        },
      );
    }
  }

  getLastConversations(
    count: number,
    isAscending: boolean,
    callback: RNStringeeEventCallback,
  ) {
    RNStringeeClient.getLastConversations(
      this.uuid,
      count,
      (status, code, message, conversations) => {
        let returnConversations = [];
        if (status) {
          if (isAscending) {
            // Tăng dần -> Cần đảo mảng
            conversations.reverse().map(conversation => {
              returnConversations.push(new Conversation(conversation));
            });
          } else {
            conversations.map(conversation => {
              returnConversations.push(new Conversation(conversation));
            });
          }
        }
        return callback(status, code, message, returnConversations);
      },
    );
  }

  getAllLastConversations(
    count: number,
    isAscending: boolean,
    callback: RNStringeeEventCallback,
  ) {
    RNStringeeClient.getAllLastConversations(
      this.uuid,
      count,
      (status, code, message, conversations) => {
        let returnConversations = [];
        if (status) {
          if (isAscending) {
            // Tăng dần -> Cần đảo mảng
            conversations.reverse().map(conversation => {
              returnConversations.push(new Conversation(conversation));
            });
          } else {
            conversations.map(conversation => {
              returnConversations.push(new Conversation(conversation));
            });
          }
        }
        return callback(status, code, message, returnConversations);
      },
    );
  }

  getConversationsAfter(
    datetime: number,
    count: number,
    isAscending: boolean,
    callback: RNStringeeEventCallback,
  ) {
    RNStringeeClient.getConversationsAfter(
      this.uuid,
      datetime,
      count,
      (status, code, message, conversations) => {
        let returnConversations = [];
        if (status) {
          if (isAscending) {
            conversations.reverse().map(conversation => {
              returnConversations.push(new Conversation(conversation));
            });
          } else {
            conversations.map(conversation => {
              returnConversations.push(new Conversation(conversation));
            });
          }
        }
        return callback(status, code, message, returnConversations);
      },
    );
  }

  getAllConversationsAfter(
    datetime: number,
    count: number,
    isAscending: boolean,
    callback: RNStringeeEventCallback,
  ) {
    RNStringeeClient.getAllConversationsAfter(
      this.uuid,
      datetime,
      count,
      (status, code, message, conversations) => {
        let returnConversations = [];
        if (status) {
          if (isAscending) {
            conversations.reverse().map(conversation => {
              returnConversations.push(new Conversation(conversation));
            });
          } else {
            conversations.map(conversation => {
              returnConversations.push(new Conversation(conversation));
            });
          }
        }
        return callback(status, code, message, returnConversations);
      },
    );
  }

  getConversationsBefore(
    datetime: number,
    count: number,
    isAscending: boolean,
    callback: RNStringeeEventCallback,
  ) {
    RNStringeeClient.getConversationsBefore(
      this.uuid,
      datetime,
      count,
      (status, code, message, conversations) => {
        let returnConversations = [];
        if (status) {
          if (isAscending) {
            conversations.reverse().map(conversation => {
              returnConversations.push(new Conversation(conversation));
            });
          } else {
            conversations.map(conversation => {
              returnConversations.push(new Conversation(conversation));
            });
          }
        }
        return callback(status, code, message, returnConversations);
      },
    );
  }

  getAllConversationsBefore(
    datetime: number,
    count: number,
    isAscending: boolean,
    callback: RNStringeeEventCallback,
  ) {
    RNStringeeClient.getAllConversationsBefore(
      this.uuid,
      datetime,
      count,
      (status, code, message, conversations) => {
        let returnConversations = [];
        if (status) {
          if (isAscending) {
            conversations.reverse().map(conversation => {
              returnConversations.push(new Conversation(conversation));
            });
          } else {
            conversations.map(conversation => {
              returnConversations.push(new Conversation(conversation));
            });
          }
        }
        return callback(status, code, message, returnConversations);
      },
    );
  }

  getLastUnreadConversations(
    count: number,
    isAscending: boolean,
    callback: RNStringeeEventCallback,
  ) {
    RNStringeeClient.getLastUnreadConversations(
      this.uuid,
      count,
      (status, code, message, conversations) => {
        let returnConversations = [];
        if (status) {
          if (isAscending) {
            // Tăng dần -> Cần đảo mảng
            conversations.reverse().map(conversation => {
              returnConversations.push(new Conversation(conversation));
            });
          } else {
            conversations.map(conversation => {
              returnConversations.push(new Conversation(conversation));
            });
          }
        }
        return callback(status, code, message, returnConversations);
      },
    );
  }

  getUnreadConversationsAfter(
    datetime: number,
    count: number,
    isAscending: boolean,
    callback: RNStringeeEventCallback,
  ) {
    RNStringeeClient.getUnreadConversationsAfter(
      this.uuid,
      datetime,
      count,
      (status, code, message, conversations) => {
        let returnConversations = [];
        if (status) {
          if (isAscending) {
            conversations.reverse().map(conversation => {
              returnConversations.push(new Conversation(conversation));
            });
          } else {
            conversations.map(conversation => {
              returnConversations.push(new Conversation(conversation));
            });
          }
        }
        return callback(status, code, message, returnConversations);
      },
    );
  }

  getUnreadConversationsBefore(
    datetime: number,
    count: number,
    isAscending: boolean,
    callback: RNStringeeEventCallback,
  ) {
    RNStringeeClient.getUnreadConversationsBefore(
      this.uuid,
      datetime,
      count,
      (status, code, message, conversations) => {
        let returnConversations = [];
        if (status) {
          if (isAscending) {
            conversations.reverse().map(conversation => {
              returnConversations.push(new Conversation(conversation));
            });
          } else {
            conversations.map(conversation => {
              returnConversations.push(new Conversation(conversation));
            });
          }
        }
        return callback(status, code, message, returnConversations);
      },
    );
  }

  deleteConversation(convId: string, callback: RNStringeeEventCallback) {
    RNStringeeClient.deleteConversation(this.uuid, convId, callback);
  }

  addParticipants(convId: string, userIds, callback: RNStringeeEventCallback) {
    RNStringeeClient.addParticipants(
      this.uuid,
      convId,
      userIds,
      (status, code, message, users) => {
        let returnUsers = [];
        if (status) {
          users.map(user => {
            returnUsers.push(new User(user));
          });
        }
        return callback(status, code, message, returnUsers);
      },
    );
  }

  removeParticipants(
    convId: string,
    userIds,
    callback: RNStringeeEventCallback,
  ) {
    RNStringeeClient.removeParticipants(
      this.uuid,
      convId,
      userIds,
      (status, code, message, users) => {
        let returnUsers = [];
        if (status) {
          users.map(user => {
            returnUsers.push(new User(user));
          });
        }
        return callback(status, code, message, returnUsers);
      },
    );
  }

  updateConversation(
    convId: string,
    params,
    callback: RNStringeeEventCallback,
  ) {
    RNStringeeClient.updateConversation(this.uuid, convId, params, callback);
  }

  markConversationAsRead(convId: string, callback: RNStringeeEventCallback) {
    RNStringeeClient.markConversationAsRead(this.uuid, convId, callback);
  }

  getConversationWithUser(userId: string, callback: RNStringeeEventCallback) {
    RNStringeeClient.getConversationWithUser(
      this.uuid,
      userId,
      (status, code, message, conversation) => {
        let returnConversation;
        if (status) {
          returnConversation = new Conversation(conversation);
        }
        return callback(status, code, message, returnConversation);
      },
    );
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

  deleteMessage(
    convId: string,
    messageId: string,
    callback: RNStringeeEventCallback,
  ) {
    RNStringeeClient.deleteMessage(this.uuid, convId, messageId, callback);
  }

  pinMessage(
    convId: string,
    messageId: string,
    pin: boolean,
    callback: RNStringeeEventCallback,
  ) {
    RNStringeeClient.pinMessage(this.uuid, convId, messageId, pin, callback);
  }

  editMessage(
    convId: string,
    messageId: string,
    newContent: string,
    callback: RNStringeeEventCallback,
  ) {
    RNStringeeClient.editMessage(
      this.uuid,
      convId,
      messageId,
      newContent,
      callback,
    );
  }

  revokeMessage(
    convId: string,
    messageId: string,
    callback: RNStringeeEventCallback,
  ) {
    RNStringeeClient.revokeMessage(this.uuid, convId, messageId, callback);
  }

  getLocalMessages(
    convId: string,
    count: number,
    isAscending: boolean,
    callback: RNStringeeEventCallback,
  ) {
    RNStringeeClient.getLocalMessages(
      this.uuid,
      convId,
      count,
      (status, code, message, messages) => {
        let returnMessages = [];
        if (status) {
          if (isAscending) {
            messages.map(msg => {
              returnMessages.push(new Message(msg));
            });
          } else {
            messages.reverse().map(msg => {
              returnMessages.push(new Message(msg));
            });
          }
        }
        return callback(status, code, message, returnMessages);
      },
    );
  }

  getLastMessages(
    convId: string,
    count: number,
    isAscending: boolean,
    loadDeletedMessage: boolean,
    loadDeletedMessageContent: boolean,
    callback: RNStringeeEventCallback,
  ) {
    RNStringeeClient.getLastMessages(
      this.uuid,
      convId,
      count,
      loadDeletedMessage,
      loadDeletedMessageContent,
      (status, code, message, messages) => {
        let returnMessages = [];
        if (status) {
          if (isAscending) {
            messages.map(msg => {
              returnMessages.push(new Message(msg));
            });
          } else {
            messages.reverse().map(msg => {
              returnMessages.push(new Message(msg));
            });
          }
        }
        return callback(status, code, message, returnMessages);
      },
    );
  }

  getAllLastMessages(
    convId: string,
    count: number,
    isAscending: boolean,
    loadDeletedMessage: boolean,
    loadDeletedMessageContent: boolean,
    callback: RNStringeeEventCallback,
  ) {
    RNStringeeClient.getAllLastMessages(
      this.uuid,
      convId,
      count,
      loadDeletedMessage,
      loadDeletedMessageContent,
      (status, code, message, messages) => {
        let returnMessages = [];
        if (status) {
          if (isAscending) {
            messages.map(msg => {
              returnMessages.push(new Message(msg));
            });
          } else {
            messages.reverse().map(msg => {
              returnMessages.push(new Message(msg));
            });
          }
        }
        return callback(status, code, message, returnMessages);
      },
    );
  }

  getMessagesAfter(
    convId: string,
    sequence: number,
    count: number,
    isAscending: boolean,
    loadDeletedMessage: boolean,
    loadDeletedMessageContent: boolean,
    callback: RNStringeeEventCallback,
  ) {
    RNStringeeClient.getMessagesAfter(
      this.uuid,
      convId,
      sequence,
      count,
      loadDeletedMessage,
      loadDeletedMessageContent,
      (status, code, message, messages) => {
        let returnMessages = [];
        if (status) {
          if (isAscending) {
            messages.map(msg => {
              returnMessages.push(new Message(msg));
            });
          } else {
            messages.reverse().map(msg => {
              returnMessages.push(new Message(msg));
            });
          }
        }
        return callback(status, code, message, returnMessages);
      },
    );
  }

  getAllMessagesAfter(
    convId: string,
    sequence: number,
    count: number,
    isAscending: boolean,
    loadDeletedMessage: boolean,
    loadDeletedMessageContent: boolean,
    callback: RNStringeeEventCallback,
  ) {
    RNStringeeClient.getAllMessagesAfter(
      this.uuid,
      convId,
      sequence,
      count,
      loadDeletedMessage,
      loadDeletedMessageContent,
      (status, code, message, messages) => {
        let returnMessages = [];
        if (status) {
          if (isAscending) {
            messages.map(msg => {
              returnMessages.push(new Message(msg));
            });
          } else {
            messages.reverse().map(msg => {
              returnMessages.push(new Message(msg));
            });
          }
        }
        return callback(status, code, message, returnMessages);
      },
    );
  }

  getMessagesBefore(
    convId: string,
    sequence: number,
    count: number,
    isAscending: boolean,
    loadDeletedMessage: boolean,
    loadDeletedMessageContent: boolean,
    callback: RNStringeeEventCallback,
  ) {
    RNStringeeClient.getMessagesBefore(
      this.uuid,
      convId,
      sequence,
      count,
      loadDeletedMessage,
      loadDeletedMessageContent,
      (status, code, message, messages) => {
        let returnMessages = [];
        if (status) {
          if (isAscending) {
            messages.map(msg => {
              returnMessages.push(new Message(msg));
            });
          } else {
            messages.reverse().map(msg => {
              returnMessages.push(new Message(msg));
            });
          }
        }
        return callback(status, code, message, returnMessages);
      },
    );
  }

  getAllMessagesBefore(
    convId: string,
    sequence: number,
    count: number,
    isAscending: boolean,
    loadDeletedMessage: boolean,
    loadDeletedMessageContent: boolean,
    callback: RNStringeeEventCallback,
  ) {
    RNStringeeClient.getAllMessagesBefore(
      this.uuid,
      convId,
      sequence,
      count,
      loadDeletedMessage,
      loadDeletedMessageContent,
      (status, code, message, messages) => {
        let returnMessages = [];
        if (status) {
          if (isAscending) {
            messages.map(msg => {
              returnMessages.push(new Message(msg));
            });
          } else {
            messages.reverse().map(msg => {
              returnMessages.push(new Message(msg));
            });
          }
        }
        return callback(status, code, message, returnMessages);
      },
    );
  }

  getMessageById(
    convId: string,
    messageId: string,
    callback: RNStringeeEventCallback,
  ) {
    RNStringeeClient.getMessageById(
      this.uuid,
      convId,
      messageId,
      (status, code, message, msg) => {
        return callback(status, code, message, new Message(msg));
      },
    );
  }

  clearDb(callback: RNStringeeEventCallback) {
    RNStringeeClient.clearDb(this.uuid, callback);
  }

  getUserInfo(userIds: Array<string>, callback: RNStringeeEventCallback) {
    RNStringeeClient.getUserInfo(
      this.uuid,
      userIds,
      (status, code, message, users) => {
        let returnUsers = [];
        if (status) {
          users.map(user => {
            returnUsers.push(new User(user));
          });
        }
        return callback(status, code, message, returnUsers);
      },
    );
  }

  // ============================== LIVE-CHAT ================================

  // ===== CUSTOMER-SIDE =====

  getChatProfile(widgetKey: string, callback: RNStringeeEventCallback) {
    RNStringeeClient.getChatProfile(this.uuid, widgetKey, callback);
  }

  getLiveChatToken(
    widgetKey: string,
    name: string,
    email: string,
    callback: RNStringeeEventCallback,
  ) {
    RNStringeeClient.getLiveChatToken(
      this.uuid,
      widgetKey,
      name,
      email,
      callback,
    );
  }

  updateUserInfo(
    name: string,
    email: string,
    avatar: string,
    phone: string,
    callback: RNStringeeEventCallback,
  ) {
    RNStringeeClient.updateUserInfo(
      this.uuid,
      name,
      email,
      avatar,
      phone,
      callback,
    );
  }

  updateUserInfoWithParam(
    param: UserInfoParam,
    callback: RNStringeeEventCallback,
  ) {
    RNStringeeClient.updateUserInfo2(
      this.uuid,
      JSON.stringify(param),
      callback,
    );
  }

  createLiveChatConversation(
    queueId: string,
    callback: RNStringeeEventCallback,
  ) {
    RNStringeeClient.createLiveChatConversation(
      this.uuid,
      queueId,
      (status, code, message, data) => {
        var returnConversation;
        if (status) {
          returnConversation = new Conversation(data);
        }
        return callback(status, code, message, returnConversation);
      },
    );
  }

  // ===== AGENT-SIDE =====

  acceptChatRequest(request: ChatRequest, callback: RNStringeeEventCallback) {
    RNStringeeClient.acceptChatRequest(this.uuid, request.convId, callback);
  }

  rejectChatRequest(request: ChatRequest, callback: RNStringeeEventCallback) {
    RNStringeeClient.rejectChatRequest(this.uuid, request.convId, callback);
  }

  createLiveChatTicket(
    widgetKey: string,
    name: string,
    email: string,
    note: string,
    callback: RNStringeeEventCallback,
  ) {
    RNStringeeClient.createLiveChatTicket(
      this.uuid,
      widgetKey,
      name,
      email,
      '',
      note,
      callback,
    );
  }

  createLiveChatTicketWithParam(
    widgetKey: string,
    param: LiveChatTicketParam,
    callback: RNStringeeEventCallback,
  ) {
    RNStringeeClient.createLiveChatTicket(
      this.uuid,
      widgetKey,
      param.name,
      param.email,
      param.phone,
      param.note,
      callback,
    );
  }

  // ===== ALL-SIDE =====

  sendChatTranscript(
    email: string,
    convId: string,
    domain: string,
    callback: RNStringeeEventCallback,
  ) {
    RNStringeeClient.sendChatTranscript(
      this.uuid,
      email,
      convId,
      domain,
      callback,
    );
  }

  endChat(convId: string, callback: RNStringeeEventCallback) {
    RNStringeeClient.endChat(this.uuid, convId, callback);
  }
}

StringeeClient.propTypes = {
  eventHandlers: PropTypes.object,
  baseUrl: PropTypes.string,
  serverAddresses: PropTypes.array,
  stringeeXBaseUrl: PropTypes.string,
  ...View.propTypes,
};

export {StringeeClient};
