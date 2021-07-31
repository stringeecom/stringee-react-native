const clientEvents = {
    ios: {
        onConnect: "didConnect",
        onDisConnect: "didDisConnect",
        onFailWithError: "didFailWithError",
        onRequestAccessToken: "requestAccessToken",
        onIncomingCall: "incomingCall",
        onIncomingCall2: "incomingCall2",
        onCustomMessage: "didReceiveCustomMessage",
        onObjectChange: "objectChangeNotification"
    },
    android: {
        onConnect: "onConnectionConnected",
        onDisConnect: "onConnectionDisconnected",
        onFailWithError: "onConnectionError",
        onRequestAccessToken: "onRequestNewToken",
        onIncomingCall: "onIncomingCall",
        onIncomingCall2: "onIncomingCall2",
        onCustomMessage: "onCustomMessage",
        onObjectChange: "onChangeEvent"
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
        onHandleOnAnotherDevice: "onHandledOnAnotherDevice",
        onAudioDeviceChange: "onAudioDeviceChange" ///only for android
    }
};

const roomEvents = {
    ios: {
        onRoomConnect: "didRoomConnect",
        onRoomDisConnect: "didRoomDisConnect",
        onRoomError: "didRoomError",
        onStreamAdd: "didStreamAdd",
        onStreamRemove: "didStreamRemove"
    },
    android: {
        onRoomConnect: "onRoomConnected",
        onRoomDisConnect: "onRoomDisconnected",
        onRoomError: "onRoomError",
        onStreamAdd: "onStreamAdded",
        onStreamRemove: "onStreamRemoved"
    }
};

const channelType = {
    liveChat: "liveChat",
    facebook: "facebook",
    zalo: "zalo",
};

export type RNStringeeEventCallback = (
    status: boolean,
    code: number,
    message: string
) => void;

export {clientEvents, callEvents, channelType};
