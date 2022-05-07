const clientEvents = {
  ios: {
    onConnect: 'didConnect',
    onDisConnect: 'didDisConnect',
    onFailWithError: 'didFailWithError',
    onRequestAccessToken: 'requestAccessToken',
    onIncomingCall: 'incomingCall',
    onIncomingCall2: 'incomingCall2',
    onCustomMessage: 'didReceiveCustomMessage',
    onObjectChange: 'objectChangeNotification',
    onReceiveChatRequest: 'didReceiveChatRequest',
    onReceiveTransferChatRequest: 'didReceiveTransferChatRequest',
    onTimeoutAnswerChat: 'timeoutAnswerChat',
    onTimeoutInQueue: 'timeoutInQueue',
    onConversationEnded: 'conversationEnded',
    onUserBeginTyping: 'userBeginTyping',
    onUserEndTyping: 'userEndTyping',
  },
  android: {
    onConnect: 'onConnectionConnected',
    onDisConnect: 'onConnectionDisconnected',
    onFailWithError: 'onConnectionError',
    onRequestAccessToken: 'onRequestNewToken',
    onIncomingCall: 'onIncomingCall',
    onIncomingCall2: 'onIncomingCall2',
    onCustomMessage: 'onCustomMessage',
    onObjectChange: 'onChangeEvent',
    onReceiveChatRequest: 'onReceiveChatRequest',
    onReceiveTransferChatRequest: 'onReceiveTransferChatRequest',
    onTimeoutAnswerChat: 'onTimeoutAnswerChat',
    onTimeoutInQueue: 'onTimeoutInQueue',
    onConversationEnded: 'onConversationEnded',
    onUserBeginTyping: 'onTyping',
    onUserEndTyping: 'onEndTyping',
  },
};

const callEvents = {
  ios: {
    onChangeSignalingState: 'didChangeSignalingState',
    onChangeMediaState: 'didChangeMediaState',
    onReceiveLocalStream: 'didReceiveLocalStream',
    onReceiveRemoteStream: 'didReceiveRemoteStream',
    onReceiveDtmfDigit: 'didReceiveDtmfDigit',
    onReceiveCallInfo: 'didReceiveCallInfo',
    onHandleOnAnotherDevice: 'didHandleOnAnotherDevice',
    onTrackMediaStateChange: 'trackMediaStateChange',
  },
  android: {
    onChangeSignalingState: 'onSignalingStateChange',
    onChangeMediaState: 'onMediaStateChange',
    onReceiveLocalStream: 'onLocalStream',
    onReceiveRemoteStream: 'onRemoteStream',
    onReceiveDtmfDigit: 'onDTMF',
    onReceiveCallInfo: 'onCallInfo',
    onHandleOnAnotherDevice: 'onHandledOnAnotherDevice',
    onAudioDeviceChange: 'onAudioDeviceChange', ///only for android
    onTrackMediaStateChange: 'onTrackMediaStateChange',
  },
};

export type UserInfoParam = (
  name: string,
  email: string,
  avatar: string,
  phone: string,
) => void;

export type LiveChatTicketParam = (
  name: string,
  email: string,
  phone: string,
  note: string,
) => void;

const MediaType = {
  AUDIO: 'AUDIO',
  VIDEO: 'VIDEO',
};

export type RNStringeeEventCallback = (
  status: boolean,
  code: number,
  message: string,
) => void;

export {clientEvents, callEvents, MediaType};
