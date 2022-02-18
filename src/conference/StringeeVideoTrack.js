import {Platform, StyleProp, ViewStyle} from 'react-native';
import {StringeeRoomUser, StringeeVideoView, ScalingType} from '../../index';
import React from 'react';
import {RNStringeeVideo} from '../StringeeClient';
import type {RNStringeeEventCallback} from '../helpers/StringeeHelper';

export default class StringeeVideoTrack {
  clientId: string;
  id: string;
  localId: string;
  publisher: StringeeRoomUser;
  audioEnable: boolean;
  videoEnable: boolean;
  isScreenCapture: boolean;
  isLocal: boolean;

  constructor(props) {
    this.clientId = props.clientId;
    this.id = props.id;
    this.localId = props.localId;
    this.publisher = new StringeeRoomUser(props.publisher);
    this.audioEnable = props.audioEnable;
    this.videoEnable = props.videoEnable;
    this.isScreenCapture = props.isScreenCapture;
    this.isLocal = props.isLocal;
  }

  getTrackId() {
    if (this.isLocal) {
      return this.localId;
    } else {
      return this.id;
    }
  }

  mute(mute: boolean, callback: RNStringeeEventCallback) {
    RNStringeeVideo.mute(this.clientId, this.localId, mute, callback);
  }

  enableVideo(enable: boolean, callback: RNStringeeEventCallback) {
    RNStringeeVideo.enableVideo(this.clientId, this.localId, enable, callback);
  }

  switchCamera(callback: RNStringeeEventCallback) {
    RNStringeeVideo.switchCamera(this.clientId, this.localId, callback);
  }

  switchCameraWithId(cameraId: number, callback: RNStringeeEventCallback) {
    if (Platform.OS === 'android') {
      RNStringeeVideo.switchCameraWithId(
        this.clientId,
        this.localId,
        cameraId,
        callback,
      );
    } else {
      this.switchCamera(callback);
    }
  }
}
