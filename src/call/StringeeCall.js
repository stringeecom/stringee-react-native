import {Component} from 'react';
import PropTypes from 'prop-types';
import {NativeModules, NativeEventEmitter, Platform} from 'react-native';
import {callEvents} from '../helpers/StringeeHelper';
import {each} from 'underscore';
import type {RNStringeeEventCallback} from '../helpers/StringeeHelper';

const RNStringeeCall = NativeModules.RNStringeeCall;

class StringeeCall extends Component {
  constructor(props) {
    super(props);
    this._events = [];
    this._subscriptions = [];
    this._eventEmitter = new NativeEventEmitter(RNStringeeCall);

    this.makeCall = this.makeCall.bind(this);
    this.initAnswer = this.initAnswer.bind(this);
    this.answer = this.answer.bind(this);
    this.hangup = this.hangup.bind(this);
    this.reject = this.reject.bind(this);
    this.sendDTMF = this.sendDTMF.bind(this);
    this.sendCallInfo = this.sendCallInfo.bind(this);
    this.getCallStats = this.getCallStats.bind(this);
    this.switchCamera = this.switchCamera.bind(this);
    this.switchCameraWithId = this.switchCameraWithId.bind(this);
    this.enableVideo = this.enableVideo.bind(this);
    this.mute = this.mute.bind(this);
    this.setSpeakerphoneOn = this.setSpeakerphoneOn.bind(this);
    this.resumeVideo = this.resumeVideo.bind(this);
    this.setMirror = this.setMirror.bind(this);
  }

  componentDidMount() {
    this.sanitizeCallEvents(this.props.eventHandlers);
  }

  componentWillUnmount() {
    this._unregisterEvents();
  }

  render() {
    return null;
  }

  _unregisterEvents() {
    this._subscriptions.forEach(e => e.remove());
    this._subscriptions = [];

    this._events.forEach(e => RNStringeeCall.removeNativeEvent(e));
    this._events = [];
  }

  sanitizeCallEvents(events) {
    if (typeof events !== 'object') {
      return;
    }
    const platform = Platform.OS;

    each(events, (handler, type) => {
      const eventName = callEvents[platform][type];
      if (eventName !== undefined) {
        this._subscriptions.push(
          this._eventEmitter.addListener(eventName, data => {
            if (handler !== undefined) {
              handler(data);
            }
          }),
        );

        this._events.push(eventName);
        RNStringeeCall.setNativeEvent(eventName);
      } else {
        console.log(`${type} is not a supported event`);
      }
    });
  }

  makeCall(parameters: string, callback: RNStringeeEventCallback) {
    RNStringeeCall.makeCall(this.props.clientId, parameters, callback);
  }

  initAnswer(callId: string, callback: RNStringeeEventCallback) {
    RNStringeeCall.initAnswer(this.props.clientId, callId, callback);
  }

  answer(callId: string, callback: RNStringeeEventCallback) {
    RNStringeeCall.answer(callId, callback);
  }

  hangup(callId: string, callback: RNStringeeEventCallback) {
    RNStringeeCall.hangup(callId, callback);
  }

  reject(callId: string, callback: RNStringeeEventCallback) {
    RNStringeeCall.reject(callId, callback);
  }

  sendDTMF(callId: string, dtmf: string, callback: RNStringeeEventCallback) {
    RNStringeeCall.sendDTMF(callId, dtmf, callback);
  }

  sendCallInfo(
    callId: string,
    callInfo: string,
    callback: RNStringeeEventCallback,
  ) {
    RNStringeeCall.sendCallInfo(callId, callInfo, callback);
  }

  getCallStats(callId: string, callback: RNStringeeEventCallback) {
    RNStringeeCall.getCallStats(this.props.clientId, callId, callback);
  }

  switchCamera(callId: string, callback: RNStringeeEventCallback) {
    RNStringeeCall.switchCamera(callId, callback);
  }

  switchCameraWithId(
    callId: string,
    cameraId: number,
    callback: RNStringeeEventCallback,
  ) {
    if (Platform.OS === 'android') {
      RNStringeeCall.switchCameraWithId(callId, cameraId, callback);
    } else {
      this.switchCamera(callId, callback);
    }
  }

  enableVideo(
    callId: string,
    enabled: boolean,
    callback: RNStringeeEventCallback,
  ) {
    RNStringeeCall.enableVideo(callId, enabled, callback);
  }

  mute(callId: string, mute: boolean, callback: RNStringeeEventCallback) {
    RNStringeeCall.mute(callId, mute, callback);
  }

  setSpeakerphoneOn(
    callId: string,
    on: boolean,
    callback: RNStringeeEventCallback,
  ) {
    RNStringeeCall.setSpeakerphoneOn(callId, on, callback);
  }

  resumeVideo(callId: string, callback: RNStringeeEventCallback) {
    if (Platform.OS === 'android') {
      RNStringeeCall.resumeVideo(callId, callback);
    } else {
      return callback(false, -4, 'this function only for android');
    }
  }

  setMirror(
    callId: string,
    isLocal: boolean,
    isMirror: boolean,
    callback: RNStringeeEventCallback,
  ) {
    if (Platform.OS === 'android') {
      RNStringeeCall.setMirror(callId, isLocal, isMirror, callback);
    } else {
      return callback(false, -4, 'this function only for android');
    }
  }
}

StringeeCall.propTypes = {
  eventHandlers: PropTypes.object,
  clientId: PropTypes.string,
};

export {StringeeCall};
