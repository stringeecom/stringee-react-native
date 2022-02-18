import {Component} from 'react';
import PropTypes from 'prop-types';
import {NativeModules, NativeEventEmitter, Platform} from 'react-native';
import {callEvents} from '../helpers/StringeeHelper';
import {each} from 'underscore';
import type {RNStringeeEventCallback} from '../helpers/StringeeHelper';

const RNStringeeCall2 = NativeModules.RNStringeeCall2;

class StringeeCall2 extends Component {
  constructor(props) {
    super(props);
    this._events = [];
    this._subscriptions = [];
    this._eventEmitter = new NativeEventEmitter(RNStringeeCall2);

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

    this._events.forEach(e => RNStringeeCall2.removeNativeEvent(e));
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
        RNStringeeCall2.setNativeEvent(eventName);
      } else {
        console.log(`${type} is not a supported event`);
      }
    });
  }

  makeCall(parameters: string, callback: RNStringeeEventCallback) {
    RNStringeeCall2.makeCall(this.props.clientId, parameters, callback);
  }

  initAnswer(callId: string, callback: RNStringeeEventCallback) {
    RNStringeeCall2.initAnswer(this.props.clientId, callId, callback);
  }

  answer(callId: string, callback: RNStringeeEventCallback) {
    RNStringeeCall2.answer(callId, callback);
  }

  hangup(callId: string, callback: RNStringeeEventCallback) {
    RNStringeeCall2.hangup(callId, callback);
  }

  reject(callId: string, callback: RNStringeeEventCallback) {
    RNStringeeCall2.reject(callId, callback);
  }

  sendDTMF(callId: string, dtmf: string, callback: RNStringeeEventCallback) {
    RNStringeeCall2.sendDTMF(callId, dtmf, callback);
  }

  getCallStats(callId: string, callback: RNStringeeEventCallback) {
    RNStringeeCall2.getCallStats(this.props.clientId, callId, callback);
  }

  switchCamera(callId: string, callback: RNStringeeEventCallback) {
    RNStringeeCall2.switchCamera(callId, callback);
  }

  switchCameraWithId(
    callId: string,
    cameraId: number,
    callback: RNStringeeEventCallback,
  ) {
    if (Platform.OS === 'android') {
      RNStringeeCall2.switchCameraWithId(callId, cameraId, callback);
    } else {
      this.switchCamera(callId, callback);
    }
  }

  enableVideo(
    callId: string,
    enabled: boolean,
    callback: RNStringeeEventCallback,
  ) {
    RNStringeeCall2.enableVideo(callId, enabled, callback);
  }

  mute(callId: string, mute: boolean, callback: RNStringeeEventCallback) {
    RNStringeeCall2.mute(callId, mute, callback);
  }

  setSpeakerphoneOn(
    callId: string,
    on: boolean,
    callback: RNStringeeEventCallback,
  ) {
    RNStringeeCall2.setSpeakerphoneOn(callId, on, callback);
  }

  resumeVideo(callId: string, callback: RNStringeeEventCallback) {
    if (Platform.OS === 'android') {
      RNStringeeCall2.resumeVideo(callId, callback);
    } else {
      return callback(false, -4, 'this function only for android');
    }
  }

  sendCallInfo(
    callId: string,
    callInfo: string,
    callback: RNStringeeEventCallback,
  ) {
    RNStringeeCall2.sendCallInfo(callId, callInfo, callback);
  }

  setMirror(
    callId: string,
    isLocal: boolean,
    isMirror: boolean,
    callback: RNStringeeEventCallback,
  ) {
    if (Platform.OS === 'android') {
      RNStringeeCall2.setMirror(callId, isLocal, isMirror, callback);
    } else {
      return callback(false, -4, 'this function only for android');
    }
  }

  // startCapture(callId: string, callback: RNStringeeEventCallback) {
  //   if (Platform.OS === 'android') {
  //     RNStringeeCall2.startCapture(callId, callback);
  //   } else {
  //     return callback(false, -4, 'this function only for android');
  //   }
  // }
  //
  // stopCapture(callId: string, callback: RNStringeeEventCallback) {
  //   if (Platform.OS === 'android') {
  //     RNStringeeCall2.stopCapture(callId, callback);
  //   } else {
  //     return callback(false, -4, 'this function only for android');
  //   }
  // }
}

StringeeCall2.propTypes = {
  eventHandlers: PropTypes.object,
  clientId: PropTypes.string,
};

export {StringeeCall2};
