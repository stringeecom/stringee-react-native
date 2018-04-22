const clientEvents = {
  ios: {
    onConnect: "didConnect",
    onDisConnect: "didDisConnect",
    onFailWithError: "didFailWithError",
    onRequestAccessToken: "requestAccessToken",
    onIncomingCall: "incomingCall"
  },
  android: {
    onConnect: "onConnectionConnected",
    onDisConnect: "onConnectionDisconnected",
    onFailWithError: "onConnectionError",
    onRequestAccessToken: "onRequestNewToken",
    onIncomingCall: "onIncomingCall"
  }
};

const callEvents = {
  ios: {
    onChangeSignalingState: "didChangeSignalingState",
    onChangeMediaState: "didChangeMediaState",
    onReceiveLocalStream: "didReceiveLocalStream",
    onReceiveRemoteStream: "didReceiveRemoteStream",
    onReceiveDtmfDigit: "didReceiveDtmfDigit",
    onReceiveCallInfo: "didReceiveCallInfo",
    onHandleOnAnotherDevice: "didHandleOnAnotherDevice"
  },
  android: {
    onChangeSignalingState: "onSignalingStateChange",
    onChangeMediaState: "onMediaStateChange",
    onReceiveLocalStream: "onLocalStream",
    onReceiveRemoteStream: "onRemoteStream",
    onReceiveDtmfDigit: "onDTMF",
    onReceiveCallInfo: "onCallInfo",
    onHandleOnAnotherDevice: "onHandledOnAnotherDevice"
  }
};

export type RNStringeeEventCallback = (
  status: boolean,
  code: int,
  message: string
) => void;

export { clientEvents, callEvents };
