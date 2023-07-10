const clientEvents = {
  ios: {
    onConnect: 'didConnect',
    onDisConnect: 'didDisConnect',
    onFailWithError: 'didFailWithError',
    onRequestAccessToken: 'requestAccessToken',
    onIncomingCall: 'incomingCall',
    onIncomingCallObject: 'incomingCall',
    onIncomingCall2: 'incomingCall2',
    onIncomingCall2Object: 'incomingCall2',
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
    onIncomingCallObject: 'onIncomingCall',
    onIncomingCall2: 'onIncomingCall2',
    onIncomingCall2Object: 'onIncomingCall2',
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

const stringeeClientEvents = [
  'onConnect',
  'onDisConnect',
  'onFailWithError',
  'onRequestAccessToken',
  'onIncomingCall',
  'onIncomingCall2',
  'onCustomMessage',
  'onObjectChange',
  'onReceiveChatRequest',
  'onReceiveTransferChatRequest',
  'onTimeoutAnswerChat',
  'onTimeoutInQueue',
  'onConversationEnded',
  'onUserBeginTyping',
  'onUserEndTyping',
];

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

const stringeeCallEvents = [
  'onChangeSignalingState',
  'onChangeMediaState',
  'onReceiveLocalStream',
  'onReceiveRemoteStream',
  'onReceiveDtmfDigit',
  'onReceiveCallInfo',
  'onHandleOnAnotherDevice',
  'onAudioDeviceChange',
];

const stringeeCall2Events = [
  'onChangeSignalingState',
  'onChangeMediaState',
  'onReceiveLocalStream',
  'onReceiveRemoteStream',
  'onReceiveDtmfDigit',
  'onReceiveCallInfo',
  'onHandleOnAnotherDevice',
  'onTrackMediaStateChange',
  'onAudioDeviceChange',
];

const StringeeVideoScalingType = {
  fit: 'fit',
  fill: 'fill',
};

const MediaType = {
  audio: 'audio',
  video: 'video',
};

const ObjectType = {
  conversation: 'conversation',
  message: 'message',
};

const ChangeType = {
  insert: 'insert',
  update: 'update',
  delete: 'delete',
};

const SignalingState = {
  calling: 'calling',
  ringing: 'ringing',
  answered: 'answered',
  busy: 'busy',
  ended: 'ended',
};

const MediaState = {
  connected: 'connected',
  disconnected: 'disconnected',
};

const AudioDevice = {
  speakerPhone: 'speakerPhone',
  wiredHeadset: 'wiredHeadset',
  earpiece: 'earpiece',
  bluetooth: 'bluetooth',
  none: 'none',
};

const VideoResolution = {
  normal: 'NORMAL',
  hd: 'HD',
};

const CallType = {
  appToAppOutgoing: 'appToAppOutgoing',
  appToAppIncoming: 'appToAppIncoming',
  appToPhone: 'appToPhone',
  phoneToApp: 'phoneToApp',
};

export type RNStringeeEventCallback = (
  status: boolean,
  code: number,
  message: string,
) => void;

export {
  clientEvents,
  callEvents,
  MediaType,
  StringeeVideoScalingType,
  ObjectType,
  ChangeType,
  stringeeClientEvents,
  stringeeCallEvents,
  stringeeCall2Events,
  SignalingState,
  MediaState,
  AudioDevice,
  VideoResolution,
  CallType,
};
