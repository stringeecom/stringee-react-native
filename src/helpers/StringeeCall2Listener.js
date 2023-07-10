import {
  AudioDevice,
  MediaState,
  SignalingState,
  StringeeCall2,
  MediaType,
} from '../../index';

class StringeeCall2Listener {
  onChangeSignalingState: (
    stringeeCall: StringeeCall2,
    signalingState: SignalingState,
    reason: string,
    sipCode: number,
    sipReason: string,
  ) => void;
  onChangeMediaState: (
    stringeeCall: StringeeCall2,
    mediaState: MediaState,
    description: string,
  ) => void;
  onReceiveLocalStream: (stringeeCall: StringeeCall2) => void;
  onReceiveRemoteStream: (stringeeCall: StringeeCall2) => void;
  onReceiveDtmfDigit: (stringeeCall: StringeeCall2, dtmf: string) => void;
  onReceiveCallInfo: (stringeeCall: StringeeCall2, callInfo: string) => void;
  onHandleOnAnotherDevice: (
    stringeeCall: StringeeCall2,
    signalingState: SignalingState,
    description: string,
  ) => void;
  onAudioDeviceChange: (
    selectedAudioDevice: AudioDevice,
    availableAudioDevices: Array<AudioDevice>,
  ) => void;
  onTrackMediaStateChange: (
    from: string,
    mediaType: MediaType,
    enable: boolean,
  ) => void;
}

export {StringeeCall2Listener};
