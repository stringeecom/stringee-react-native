import {StringeeRoomUser} from '../../index';

export default class StringeeVideoTrackInfo {
  clientId: string;
  id: string;
  audioEnable: boolean;
  videoEnable: boolean;
  isScreenCapture: boolean;
  publisher: StringeeRoomUser;

  constructor(props) {
    this.clientId = props.clientId;
    this.id = props.id;
    this.audioEnable = props.audioEnable;
    this.videoEnable = props.videoEnable;
    this.isScreenCapture = props.isScreenCapture;
    this.publisher = new StringeeRoomUser(props.publisher);
  }
}
