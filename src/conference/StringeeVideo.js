import {Platform} from 'react-native';
import {RNStringeeVideo} from '../StringeeClient';
import {
  StringeeVideoRoom,
  StringeeVideoTrackInfo,
  StringeeVideoTrackOption,
  StringeeRoomUser,
  StringeeVideoTrack,
} from '../../index';
import type {RNStringeeEventCallback} from '../helpers/StringeeHelper';

export default class StringeeVideo {
  clientId: string;

  constructor(clientId) {
    this.clientId = clientId;
  }

  joinRoom(roomToken: string, callback: RNStringeeEventCallback) {
    RNStringeeVideo.joinRoom(
      this.clientId,
      roomToken,
      (status, code, message, value) => {
        if (status) {
          let room = new StringeeVideoRoom(value.room);

          let videoTrackInfos: StringeeVideoTrackInfo[] = [];
          value.videoTrackInfos.map(data => {
            videoTrackInfos.push(new StringeeVideoTrackInfo(data));
          });

          let users: StringeeRoomUser[] = [];
          value.users.map(data => {
            users.push(new StringeeRoomUser(data));
          });

          let data = {
            room: room,
            videoTrackInfos: videoTrackInfos,
            users: users,
          };
          return callback(status, code, message, data);
        } else {
          return callback(status, code, message);
        }
      },
    );
  }

  createLocalVideoTrack(
    options: StringeeVideoTrackOption,
    callback: RNStringeeEventCallback,
  ) {
    RNStringeeVideo.createLocalVideoTrack(
      this.clientId,
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

  createCaptureScreenTrack(callback: RNStringeeEventCallback) {
    if (Platform.OS === 'android') {
      RNStringeeVideo.createCaptureScreenTrack(
        this.clientId,
        (status, code, message, data) => {
          if (status) {
            return callback(
              status,
              code,
              message,
              new StringeeVideoTrack(data),
            );
          } else {
            return callback(status, code, message);
          }
        },
      );
    } else {
      return callback(false, -1, 'This function is only available in Android');
    }
  }
}
