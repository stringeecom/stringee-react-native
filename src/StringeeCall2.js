import {Component} from 'react';
import PropTypes from 'prop-types';
import {NativeModules, NativeEventEmitter, Platform, View} from 'react-native';
import {each} from 'underscore';
import type {RNStringeeEventCallback} from './helpers/StringeeHelper';
import {
  StringeeClient,
  StringeeCall2Listener,
  CallType,
  VideoResolution,
} from '../index';
import {
  callEvents,
  getAudioDevice,
  getListAudioDevice,
  getMediaState,
  getMediaType,
  getSignalingState,
  stringeeCall2Events,
} from './helpers/StringeeHelper';

const RNStringeeCall2 = NativeModules.RNStringeeCall2;

class StringeeCall2Props {
  stringeeClient: StringeeClient;
  from: string;
  to: string;
}

class StringeeCall2 extends Component {
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

  constructor(props: StringeeCall2Props) {
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
    this.eventEmitter = new NativeEventEmitter(RNStringeeCall2);

    this.registerEvents = this.registerEvents.bind(this);
    this.unregisterEvents = this.unregisterEvents.bind(this);
    this.makeCall = this.makeCall.bind(this);
    this.initAnswer = this.initAnswer.bind(this);
    this.answer = this.answer.bind(this);
    this.hangup = this.hangup.bind(this);
    this.reject = this.reject.bind(this);
    this.sendDTMF = this.sendDTMF.bind(this);
    this.getCallStats = this.getCallStats.bind(this);
    this.switchCamera = this.switchCamera.bind(this);
    this.enableVideo = this.enableVideo.bind(this);
    this.mute = this.mute.bind(this);
    this.setSpeakerphoneOn = this.setSpeakerphoneOn.bind(this);
    this.resumeVideo = this.resumeVideo.bind(this);
    this.sendCallInfo = this.sendCallInfo.bind(this);
    this.setAutoSendTrackMediaStateChangeEvent =
      this.setAutoSendTrackMediaStateChangeEvent.bind(this);
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

  registerEvents(stringeeCall2Listener: StringeeCall2Listener) {
    if (this.events.length !== 0 && this.subscriptions.length !== 0) {
      return;
    }
    if (stringeeCall2Listener) {
      stringeeCall2Events.forEach(event => {
        if (stringeeCall2Listener[event]) {
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
                    stringeeCall2Listener.onChangeSignalingState(
                      this,
                      getSignalingState(data.code),
                      data.reason,
                      data.sipCode,
                      data.sipReason,
                    );
                    break;
                  case 'onChangeMediaState':
                    stringeeCall2Listener.onChangeMediaState(
                      this,
                      getMediaState(data.code),
                      data.description,
                    );
                    break;
                  case 'onReceiveLocalStream':
                    stringeeCall2Listener.onReceiveLocalStream(this);
                    break;
                  case 'onReceiveRemoteStream':
                    stringeeCall2Listener.onReceiveRemoteStream(this);
                    break;
                  case 'onReceiveDtmfDigit':
                    stringeeCall2Listener.onReceiveDtmfDigit(this, data.dtmf);
                    break;
                  case 'onReceiveCallInfo':
                    stringeeCall2Listener.onReceiveCallInfo(this, data.data);
                    break;
                  case 'onHandleOnAnotherDevice':
                    stringeeCall2Listener.onHandleOnAnotherDevice(
                      this,
                      getSignalingState(data.code),
                      data.description,
                    );
                    break;
                  case 'onAudioDeviceChange':
                    stringeeCall2Listener.onAudioDeviceChange(
                      getAudioDevice(data.selectedAudioDevice),
                      getListAudioDevice(data.availableAudioDevices),
                    );
                    break;
                  case 'onTrackMediaStateChange':
                    stringeeCall2Listener.onTrackMediaStateChange(
                      data.from,
                      getMediaType(data.mediaType),
                      data.enable,
                    );
                    break;
                }
              },
            ),
          );
          this.events.push(callEvents[Platform.OS][event]);
          RNStringeeCall2.setNativeEvent(callEvents[Platform.OS][event]);
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
            console.log('');
            if (type === 'onTrackMediaStateChange') {
              if (eventHandlers[type]) {
                eventHandlers[type]({
                  from: data.from,
                  mediaType: getMediaType(data.mediaType),
                  enable: data.enable,
                });
              }
            } else {
              if (eventHandlers[type]) {
                eventHandlers[type](data);
              }
            }
          }),
        );
        this.events.push(eventName);
        RNStringeeCall2.setNativeEvent(eventName);
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

    this.events.forEach(e => RNStringeeCall2.removeNativeEvent(e));
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
    RNStringeeCall2.makeCall(
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
    RNStringeeCall2.initAnswer(this.clientId, this.callId, callback);
  }

  answer(callId: string, callback: RNStringeeEventCallback) {
    if (!callback) {
      callback = () => {};
    }
    RNStringeeCall2.answer(this.callId, callback);
  }

  hangup(callId: string, callback: RNStringeeEventCallback) {
    if (!callback) {
      callback = () => {};
    }
    RNStringeeCall2.hangup(this.callId, callback);
  }

  reject(callId: string, callback: RNStringeeEventCallback) {
    if (!callback) {
      callback = () => {};
    }
    RNStringeeCall2.reject(this.callId, callback);
  }

  sendDTMF(callId: string, dtmf: string, callback: RNStringeeEventCallback) {
    if (!callback) {
      callback = () => {};
    }
    RNStringeeCall2.sendDTMF(this.callId, dtmf, callback);
  }

  getCallStats(callId: string, callback: RNStringeeEventCallback) {
    if (!callback) {
      callback = () => {};
    }
    RNStringeeCall2.getCallStats(this.clientId, this.callId, callback);
  }

  switchCamera(callId: string, callback: RNStringeeEventCallback) {
    if (!callback) {
      callback = () => {};
    }
    RNStringeeCall2.switchCamera(this.callId, callback);
  }

  enableVideo(
    callId: string,
    enabled: boolean,
    callback: RNStringeeEventCallback,
  ) {
    if (!callback) {
      callback = () => {};
    }
    RNStringeeCall2.enableVideo(this.callId, enabled, callback);
  }

  mute(callId: string, mute: boolean, callback: RNStringeeEventCallback) {
    if (!callback) {
      callback = () => {};
    }
    RNStringeeCall2.mute(this.callId, mute, callback);
  }

  setSpeakerphoneOn(
    callId: string,
    on: boolean,
    callback: RNStringeeEventCallback,
  ) {
    if (!callback) {
      callback = () => {};
    }
    RNStringeeCall2.setSpeakerphoneOn(this.callId, on, callback);
  }

  resumeVideo(callId: string, callback: RNStringeeEventCallback) {
    const platform = Platform.OS;
    if (platform === 'ios') {
      console.warn('this function only for android');
    } else {
      if (!callback) {
        callback = () => {};
      }
      RNStringeeCall2.resumeVideo(this.callId, callback);
    }
  }

  sendCallInfo(
    callId: string,
    callInfo: string,
    callback: RNStringeeEventCallback,
  ) {
    if (!callback) {
      callback = () => {};
    }
    RNStringeeCall2.sendCallInfo(this.callId, callInfo, callback);
  }

  setAutoSendTrackMediaStateChangeEvent(
    callId: string,
    autoSendTrackMediaStateChangeEvent: boolean,
    callback: RNStringeeEventCallback,
  ) {
    if (!callback) {
      callback = () => {};
    }
    RNStringeeCall2.setAutoSendTrackMediaStateChangeEvent(
      this.callId,
      autoSendTrackMediaStateChangeEvent,
      callback,
    );
  }
}

StringeeCall2.propTypes = {
  clientId: PropTypes.string,
  eventHandlers: PropTypes.object,
  ...View.propTypes,
};

export {StringeeCall2};
