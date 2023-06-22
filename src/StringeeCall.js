import {Component} from 'react';
import {NativeModules, NativeEventEmitter, Platform, View} from 'react-native';
import {callEvents} from './helpers/StringeeHelper';
import {each} from 'underscore';
import type {RNStringeeEventCallback} from './helpers/StringeeHelper';
import PropTypes from 'prop-types';

const RNStringeeCall = NativeModules.RNStringeeCall;

interface StringeeCallProps {
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

class StringeeCall extends Component {
  clientId: string;
  callId: string;
  customData: string;
  from: string;
  fromAlias: string;
  to: string;
  toAlias: string;
  isPhoneToApp: boolean;
  isVideoCall: boolean;

  constructor(props: StringeeCallProps) {
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
    this.isPhoneToApp = this.props.isPhoneToApp;
    this.isVideoCall = this.props.isVideoCall;
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
            if (handler !== undefined) {
              handler(data);
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
    const params = JSON.parse(parameters);
    this.from = params.from;
    this.to = params.to;
    this.isVideoCall = params.isVideoCall;
    this.customData = params.customData;
    RNStringeeCall.makeCall(
      this.clientId,
      parameters,
      (status, code, message, callId, customData) => {
        this.callId = callId;
        return callback(status, code, message, callId, customData);
      },
    );
  }

  initAnswer(callId: string, callback: RNStringeeEventCallback) {
    RNStringeeCall.initAnswer(this.clientId, this.callId, callback);
  }

  answer(callId: string, callback: RNStringeeEventCallback) {
    RNStringeeCall.answer(this.callId, callback);
  }

  hangup(callId: string, callback: RNStringeeEventCallback) {
    RNStringeeCall.hangup(this.callId, callback);
  }

  reject(callId: string, callback: RNStringeeEventCallback) {
    RNStringeeCall.reject(this.callId, callback);
  }

  sendDTMF(callId: string, dtmf: string, callback: RNStringeeEventCallback) {
    RNStringeeCall.sendDTMF(this.callId, dtmf, callback);
  }

  sendCallInfo(
    callId: string,
    callInfo: string,
    callback: RNStringeeEventCallback,
  ) {
    RNStringeeCall.sendCallInfo(this.callId, callInfo, callback);
  }

  getCallStats(callId: string, callback: RNStringeeEventCallback) {
    RNStringeeCall.getCallStats(this.clientId, this.callId, callback);
  }

  switchCamera(callId: string, callback: RNStringeeEventCallback) {
    RNStringeeCall.switchCamera(this.callId, callback);
  }

  enableVideo(
    callId: string,
    enabled: boolean,
    callback: RNStringeeEventCallback,
  ) {
    RNStringeeCall.enableVideo(this.callId, enabled, callback);
  }

  mute(callId: string, mute: boolean, callback: RNStringeeEventCallback) {
    RNStringeeCall.mute(this.callId, mute, callback);
  }

  setSpeakerphoneOn(
    callId: string,
    on: boolean,
    callback: RNStringeeEventCallback,
  ) {
    RNStringeeCall.setSpeakerphoneOn(this.callId, on, callback);
  }

  resumeVideo(callId: string, callback: RNStringeeEventCallback) {
    const platform = Platform.OS;
    if (platform === 'ios') {
      console.warn('this function only for android');
    } else {
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
