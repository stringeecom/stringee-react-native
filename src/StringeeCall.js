import {Component} from 'react';
import PropTypes from 'prop-types';
import {NativeModules, NativeEventEmitter, Platform, View} from 'react-native';
import {each} from 'underscore';
import type {RNStringeeEventCallback} from './helpers/StringeeHelper';
import {
  StringeeClient,
  StringeeCallListener,
  CallType,
  VideoResolution,
} from '../index';
import {
  callEvents,
  getAudioDevice,
  getListAudioDevice,
  getMediaState,
  getSignalingState,
  stringeeCallEvents,
} from './helpers/StringeeHelper';

const RNStringeeCall = NativeModules.RNStringeeCall;

class StringeeCallProps {
  stringeeClient: StringeeClient;
  from: string;
  to: string;
}

class StringeeCall extends Component {
  clientId: string;
  callId: string;
  customData: string;
  from: string;
  fromAlias: string;
  to: string;
  toAlias: string;
  callType: CallType;
  isVideoCall: boolean;
  videoResolution: VideoResolution = VideoResolution.normal;

  constructor(props: StringeeCallProps) {
    super(props);
    if (this.props === undefined) {
      this.props = {};
    }
    if (this.props.stringeeClient) {
      this.clientId = this.props.stringeeClient.uuid;
    }
    if (this.props.clientId) {
      this.clientId = this.props.clientId;
    }
    this.from = this.props.from;
    this.to = this.props.to;
    this.events = [];
    this.subscriptions = [];
    this.eventEmitter = new NativeEventEmitter(RNStringeeCall);

    this.registerEvents = this.registerEvents.bind(this);
    this.unregisterEvents = this.unregisterEvents.bind(this);
    this.makeCall = this.makeCall.bind(this);
    this.initAnswer = this.initAnswer.bind(this);
    this.answer = this.answer.bind(this);
    this.hangup = this.hangup.bind(this);
    this.reject = this.reject.bind(this);
    this.sendDTMF = this.sendDTMF.bind(this);
    this.sendCallInfo = this.sendCallInfo.bind(this);
    this.getCallStats = this.getCallStats.bind(this);
    this.switchCamera = this.switchCamera.bind(this);
    this.enableVideo = this.enableVideo.bind(this);
    this.mute = this.mute.bind(this);
    this.setSpeakerphoneOn = this.setSpeakerphoneOn.bind(this);
    this.resumeVideo = this.resumeVideo.bind(this);
  }

  componentDidMount() {
    this.sanitizeCallEvents(this.props.eventHandlers);
  }

  componentWillUnmount() {
    this.unregisterEvents();
  }

  render() {
    return null;
  }

  registerEvents(stringeeCallListener: StringeeCallListener) {
    if (this.events.length !== 0 && this.subscriptions.length !== 0) {
      return;
    }
    if (stringeeCallListener) {
      stringeeCallEvents.forEach(event => {
        if (stringeeCallListener[event]) {
          this.subscriptions.push(
            this.eventEmitter.addListener(
              callEvents[Platform.OS][event],
              data => {
                if (data !== undefined) {
                  if (data.callId !== undefined) {
                    this.callId = data.callId;
                  }
                }
                switch (event) {
                  case 'onChangeSignalingState':
                    stringeeCallListener.onChangeSignalingState(
                      this,
                      getSignalingState(data.code),
                      data.reason,
                      data.sipCode,
                      data.sipReason,
                    );
                    break;
                  case 'onChangeMediaState':
                    stringeeCallListener.onChangeMediaState(
                      this,
                      getMediaState(data.code),
                      data.description,
                    );
                    break;
                  case 'onReceiveLocalStream':
                    stringeeCallListener.onReceiveLocalStream(this);
                    break;
                  case 'onReceiveRemoteStream':
                    stringeeCallListener.onReceiveRemoteStream(this);
                    break;
                  case 'onReceiveDtmfDigit':
                    stringeeCallListener.onReceiveDtmfDigit(this, data.dtmf);
                    break;
                  case 'onReceiveCallInfo':
                    stringeeCallListener.onReceiveCallInfo(this, data.data);
                    break;
                  case 'onHandleOnAnotherDevice':
                    stringeeCallListener.onHandleOnAnotherDevice(
                      this,
                      getSignalingState(data.code),
                      data.description,
                    );
                    break;
                  case 'onAudioDeviceChange':
                    stringeeCallListener.onAudioDeviceChange(
                      getAudioDevice(data.selectedAudioDevice),
                      getListAudioDevice(data.availableAudioDevices),
                    );
                    break;
                }
              },
            ),
          );
          this.events.push(callEvents[Platform.OS][event]);
          RNStringeeCall.setNativeEvent(callEvents[Platform.OS][event]);
        }
      });
    }
  }

  sanitizeCallEvents(eventHandlers) {
    if (
      eventHandlers === undefined ||
      typeof eventHandlers !== 'object' ||
      (this.events.length !== 0 && this.subscriptions.length !== 0)
    ) {
      return;
    }

    const platform = Platform.OS;

    each(eventHandlers, (handler, type) => {
      const eventName = callEvents[platform][type];
      if (eventName !== undefined) {
        this.subscriptions.push(
          this.eventEmitter.addListener(eventName, data => {
            if (eventHandlers[type]) {
              eventHandlers[type](data);
            }
          }),
        );
        this.events.push(eventName);
        RNStringeeCall.setNativeEvent(eventName);
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

    this.events.forEach(e => RNStringeeCall.removeNativeEvent(e));
    this.events = [];
  }

  makeCall(parameters: string, callback: RNStringeeEventCallback) {
    if (parameters) {
      const params = JSON.parse(parameters);
      if (!this.from) {
        this.from = params.from;
      }
      if (!this.to) {
        this.to = params.to;
      }
      if (params.isVideoCall) {
        this.isVideoCall = params.isVideoCall;
      }
      if (params.customData) {
        this.customData = params.customData;
      }
      if (params.videoResolution) {
        this.videoResolution = params.videoResolution;
      }
    }
    const makeCallParam = {
      from: this.from,
      to: this.to,
      isVideoCall: this.isVideoCall,
      customData: this.customData,
      videoResolution: this.videoResolution,
    };
    RNStringeeCall.makeCall(
      this.clientId,
      JSON.stringify(makeCallParam),
      (status, code, message, callId, customData) => {
        this.callId = callId;
        if (!callback) {
          callback = () => {};
        }
        return callback(status, code, message, callId, customData);
      },
    );
  }

  initAnswer(callId: string, callback: RNStringeeEventCallback) {
    if (!callback) {
      callback = () => {};
    }
    RNStringeeCall.initAnswer(this.clientId, this.callId, callback);
  }

  answer(callId: string, callback: RNStringeeEventCallback) {
    if (!callback) {
      callback = () => {};
    }
    RNStringeeCall.answer(this.callId, callback);
  }

  hangup(callId: string, callback: RNStringeeEventCallback) {
    if (!callback) {
      callback = () => {};
    }
    RNStringeeCall.hangup(this.callId, callback);
  }

  reject(callId: string, callback: RNStringeeEventCallback) {
    if (!callback) {
      callback = () => {};
    }
    RNStringeeCall.reject(this.callId, callback);
  }

  sendDTMF(callId: string, dtmf: string, callback: RNStringeeEventCallback) {
    if (!callback) {
      callback = () => {};
    }
    RNStringeeCall.sendDTMF(this.callId, dtmf, callback);
  }

  sendCallInfo(
    callId: string,
    callInfo: string,
    callback: RNStringeeEventCallback,
  ) {
    if (!callback) {
      callback = () => {};
    }
    RNStringeeCall.sendCallInfo(this.callId, callInfo, callback);
  }

  getCallStats(callId: string, callback: RNStringeeEventCallback) {
    if (!callback) {
      callback = () => {};
    }
    RNStringeeCall.getCallStats(this.clientId, this.callId, callback);
  }

  switchCamera(callId: string, callback: RNStringeeEventCallback) {
    if (!callback) {
      callback = () => {};
    }
    RNStringeeCall.switchCamera(this.callId, callback);
  }

  enableVideo(
    callId: string,
    enabled: boolean,
    callback: RNStringeeEventCallback,
  ) {
    if (!callback) {
      callback = () => {};
    }
    RNStringeeCall.enableVideo(this.callId, enabled, callback);
  }

  mute(callId: string, mute: boolean, callback: RNStringeeEventCallback) {
    if (!callback) {
      callback = () => {};
    }
    RNStringeeCall.mute(this.callId, mute, callback);
  }

  setSpeakerphoneOn(
    callId: string,
    on: boolean,
    callback: RNStringeeEventCallback,
  ) {
    if (!callback) {
      callback = () => {};
    }
    RNStringeeCall.setSpeakerphoneOn(this.callId, on, callback);
  }

  resumeVideo(callId: string, callback: RNStringeeEventCallback) {
    const platform = Platform.OS;
    if (platform === 'ios') {
      console.warn('this function only for android');
    } else {
      if (!callback) {
        callback = () => {};
      }
      RNStringeeCall.resumeVideo(this.callId, callback);
    }
  }
}

StringeeCall.propTypes = {
  clientId: PropTypes.string,
  eventHandlers: PropTypes.object,
  ...View.propTypes,
};

export {StringeeCall};
