import {StringeeVideoDimensions} from './StringeeHelper';

export default class StringeeVideoTrackOption {
  audio: boolean;
  video: boolean;
  screen: boolean;
  videoDimension: StringeeVideoDimensions;

  constructor(
    audio: boolean,
    video: boolean,
    screen: boolean,
    videoDimension: StringeeVideoDimensions,
  ) {
    this.audio = audio;
    this.video = video;
    this.screen = screen;
    this.videoDimension = videoDimension;
  }
}
