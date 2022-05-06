import {Component} from 'react';
import PropTypes from 'prop-types';
import {NativeModules, NativeEventEmitter, Platform} from 'react-native';
import {callEvents, MediaType} from './helpers/StringeeHelper';
import {each} from 'underscore';
import type {RNStringeeEventCallback} from './helpers/StringeeHelper';

const RNStringeeCall2 = NativeModules.RNStringeeCall2;

export default class extends Component {
  static propTypes = {
    eventHandlers: PropTypes.object,
    clientId: PropTypes.string,
  };

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
        if (type === 'onTrackMediaStateChange') {
          this._subscriptions.push(
            this._eventEmitter.addListener(
              eventName,
              ({from, mediaType, enable}) => {
                if (handler !== undefined) {
                  if (mediaType === 1) {
                    mediaType = MediaType.AUDIO;
                  } else if (mediaType === 2) {
                    mediaType = MediaType.VIDEO;
                  }
                  console.log('mediaType - ' + mediaType);
                  handler({from, mediaType, enable});
                }
              },
            ),
          );
        } else {
          this._subscriptions.push(
            this._eventEmitter.addListener(eventName, data => {
              if (handler !== undefined) {
                handler(data);
              }
            }),
          );
        }
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
    const platform = Platform.OS;
    if (platform === 'ios') {
      console.log('this function only for android');
    } else {
      RNStringeeCall2.resumeVideo(callId, callback);
    }
  }

  sendCallInfo(
    callId: string,
    callInfo: string,
    callback: RNStringeeEventCallback,
  ) {
    RNStringeeCall2.sendCallInfo(callId, callInfo, callback);
  }

  setAutoSendTrackMediaStateChangeEvent(
    callId: string,
    autoSendTrackMediaStateChangeEvent: boolean,
    callback: RNStringeeEventCallback,
  ) {
    RNStringeeCall2.setAutoSendTrackMediaStateChangeEvent(
      callId,
      autoSendTrackMediaStateChangeEvent,
      callback,
    );
  }
}
