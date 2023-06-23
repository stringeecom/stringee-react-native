import {Component} from 'react';
import PropTypes from 'prop-types';
import {NativeModules, NativeEventEmitter, Platform, View} from 'react-native';
import {callEvents, MediaType} from './helpers/StringeeHelper';
import {each} from 'underscore';
import type {RNStringeeEventCallback} from './helpers/StringeeHelper';
import {StringeeCall} from './StringeeCall';

const RNStringeeCall2 = NativeModules.RNStringeeCall2;

interface StringeeCall2Props {
  clientId: string;
  callId: string;
  customData: string;
  from: string;
  fromAlias: string;
  to: string;
  toAlias: string;
  isPhoneToApp: boolean;
  isVideoCall: boolean;
}

class StringeeCall2 extends Component {
  clientId: string;
  callId: string;
  customData: string;
  from: string;
  fromAlias: string;
  to: string;
  toAlias: string;
  isVideoCall: boolean;

  constructor(props: StringeeCall2Props) {
    super(props);
    if (this.props === undefined) {
      this.props = {};
    }
    this.clientId = this.props.clientId;
    this.callId = this.props.callId;
    this.customData = this.props.customData;
    this.from = this.props.from;
    this.fromAlias = this.props.fromAlias;
    this.to = this.props.to;
    this.toAlias = this.props.toAlias;
    this.isVideoCall = this.props.isVideoCall;
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
    this.registerEvents(this.props.eventHandlers);
  }

  componentWillUnmount() {
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
      const eventName = callEvents[platform][type];
      if (eventName !== undefined) {
        this.subscriptions.push(
          this.eventEmitter.addListener(eventName, data => {
            console.log('');
            if (type === 'onTrackMediaStateChange') {
              if (data.mediaType === 1) {
                data.mediaType = MediaType.AUDIO;
              } else if (data.mediaType === 2) {
                data.mediaType = MediaType.VIDEO;
              }
              if (eventHandlers[type]) {
                eventHandlers[type]({
                  from: data.from,
                  mediaType: data.mediaType,
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
    const params = JSON.parse(parameters);
    this.from = params.from;
    this.to = params.to;
    this.isVideoCall = params.isVideoCall;
    this.customData = params.customData;
    RNStringeeCall2.makeCall(
      this.clientId,
      parameters,
      (status, code, message, callId, customData) => {
        this.callId = callId;
        return callback(status, code, message, callId, customData);
      },
    );
  }

  initAnswer(callId: string, callback: RNStringeeEventCallback) {
    RNStringeeCall2.initAnswer(this.clientId, this.callId, callback);
  }

  answer(callId: string, callback: RNStringeeEventCallback) {
    RNStringeeCall2.answer(this.callId, callback);
  }

  hangup(callId: string, callback: RNStringeeEventCallback) {
    RNStringeeCall2.hangup(this.callId, callback);
  }

  reject(callId: string, callback: RNStringeeEventCallback) {
    RNStringeeCall2.reject(this.callId, callback);
  }

  sendDTMF(callId: string, dtmf: string, callback: RNStringeeEventCallback) {
    RNStringeeCall2.sendDTMF(this.callId, dtmf, callback);
  }

  getCallStats(callId: string, callback: RNStringeeEventCallback) {
    RNStringeeCall2.getCallStats(this.clientId, this.callId, callback);
  }

  switchCamera(callId: string, callback: RNStringeeEventCallback) {
    RNStringeeCall2.switchCamera(this.callId, callback);
  }

  enableVideo(
    callId: string,
    enabled: boolean,
    callback: RNStringeeEventCallback,
  ) {
    RNStringeeCall2.enableVideo(this.callId, enabled, callback);
  }

  mute(callId: string, mute: boolean, callback: RNStringeeEventCallback) {
    RNStringeeCall2.mute(this.callId, mute, callback);
  }

  setSpeakerphoneOn(
    callId: string,
    on: boolean,
    callback: RNStringeeEventCallback,
  ) {
    RNStringeeCall2.setSpeakerphoneOn(this.callId, on, callback);
  }

  resumeVideo(callId: string, callback: RNStringeeEventCallback) {
    const platform = Platform.OS;
    if (platform === 'ios') {
      console.warn('this function only for android');
    } else {
      RNStringeeCall2.resumeVideo(this.callId, callback);
    }
  }

  sendCallInfo(
    callId: string,
    callInfo: string,
    callback: RNStringeeEventCallback,
  ) {
    RNStringeeCall2.sendCallInfo(this.callId, callInfo, callback);
  }

  setAutoSendTrackMediaStateChangeEvent(
    callId: string,
    autoSendTrackMediaStateChangeEvent: boolean,
    callback: RNStringeeEventCallback,
  ) {
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
