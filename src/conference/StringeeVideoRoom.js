import {NativeEventEmitter} from 'react-native';
import {each, object} from 'underscore';
import {roomEvents} from '../helpers/StringeeHelper';
import {RNStringeeVideo} from '../StringeeClient';
import {
  StringeeRoomUser,
  StringeeVideoTrack,
  StringeeVideoTrackInfo,
  StringeeVideoTrackOption,
} from '../../index';
import type {RNStringeeEventCallback} from '../helpers/StringeeHelper';

export default class StringeeVideoRoom {
  clientId: string;
  id: string;
  recorded: boolean;

  constructor(props) {
    this.id = props.id;
    this.recorded = props.recorded;
    this.clientId = props.clientId;

    this.events = [];
    this.subscriptions = [];
    this.eventEmitter = new NativeEventEmitter(RNStringeeVideo);
  }

  registerRoomEvent(events: Object) {
    if (typeof events !== 'object') {
      return;
    }

    each(events, (handler, type) => {
      const eventName = roomEvents[type];
      if (eventName !== undefined) {
        this.subscriptions.push(
          this.eventEmitter.addListener(eventName, data => {
            if (handler !== undefined) {
              if (type === 'didJoinRoom' || type === 'didLeaveRoom') {
                handler(new StringeeRoomUser(data));
              } else if (
                type === 'didAddVideoTrack' ||
                type === 'didRemoveVideoTrack'
              ) {
                handler(new StringeeVideoTrackInfo(data));
              } else if (type === 'didReceiveRoomMessage') {
                let msg = JSON.parse(data.msg);
                let from = new StringeeRoomUser(data.from);

                handler({msg, from});
              } else if (type === 'trackReadyToPlay') {
                handler(new StringeeVideoTrack(data));
              }
            }
          }),
        );

        this.events.push(eventName);
        RNStringeeVideo.setNativeEvent(eventName);
      } else {
        console.log(`${type} is not a supported event`);
      }
    });
  }

  close() {
    this.subscriptions.forEach(e => e.remove());
    this.subscriptions = [];

    this.events.forEach(e => RNStringeeVideo.removeNativeEvent(e));
    this.events = [];
  }

  publish(videoTrack: StringeeVideoTrack, callback: RNStringeeEventCallback) {
    RNStringeeVideo.publish(
      this.clientId,
      this.id,
      videoTrack.localId,
      (status, code, message, data) => {
        if (status) {
          return callback(status, code, message, new StringeeVideoTrack(data));
        } else {
          return callback(status, code, message);
        }
      },
    );
  }

  unpublish(videoTrack: StringeeVideoTrack, callback: RNStringeeEventCallback) {
    RNStringeeVideo.unpublish(
      this.clientId,
      this.id,
      videoTrack.localId,
      callback,
    );
  }

  subscribe(
    videoTrackInfo: StringeeVideoTrackInfo,
    options: StringeeVideoTrackOption,
    callback: RNStringeeEventCallback,
  ) {
    RNStringeeVideo.subscribe(
      this.clientId,
      this.id,
      videoTrackInfo.id,
      options,
      (status, code, message, data) => {
        if (status) {
          return callback(status, code, message, new StringeeVideoTrack(data));
        } else {
          return callback(status, code, message);
        }
      },
    );
  }

  unsubscribe(
    videoTrackInfo: StringeeVideoTrackInfo,
    callback: RNStringeeEventCallback,
  ) {
    RNStringeeVideo.unsubscribe(
      this.clientId,
      this.id,
      videoTrackInfo.id,
      callback,
    );
  }

  leave(allClient: boolean, callback: RNStringeeEventCallback) {
    RNStringeeVideo.leave(this.clientId, this.id, allClient, callback);
  }

  sendMessage(msg, callback: RNStringeeEventCallback) {
    RNStringeeVideo.sendMessage(this.clientId, this.id, msg, callback);
  }
}
