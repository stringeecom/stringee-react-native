import {
  ChangeType,
  ChatRequest,
  ObjectType,
  StringeeCall,
  StringeeCall2,
} from '../../index';

class StringeeClientListener {
  onConnect: (userId: string) => void;
  onDisConnect: () => void;
  onFailWithError: (code: number, message: string) => void;
  onRequestAccessToken: () => void;
  onIncomingCall: (stringeeCall: StringeeCall) => void;
  onIncomingCall2: (stringeeCall2: StringeeCall2) => void;
  onCustomMessage: (from: string, data: {}) => void;
  onObjectChange: (
    objectType: ObjectType,
    objectChanges: Array,
    changeType: ChangeType,
  ) => void;
  onReceiveChatRequest: (chatRequest: ChatRequest) => void;
  onReceiveTransferChatRequest: (chatRequest: ChatRequest) => void;
  onTimeoutAnswerChat: (chatRequest: ChatRequest) => void;
  onTimeoutInQueue: (
    convId: string,
    customerId: string,
    customerName: string,
  ) => void;
  onConversationEnded: (convId: string, endedBy: string) => void;
  onUserBeginTyping: (
    convId: string,
    userId: string,
    displayName: string,
  ) => void;
  onUserEndTyping: (
    convId: string,
    userId: string,
    displayName: string,
  ) => void;
}

export {StringeeClientListener};
