import {
  AudioDevice,
  MediaState,
  SignalingState,
  StringeeCall,
} from '../../index';

class StringeeCallListener {
  onChangeSignalingState: (
    stringeeCall: StringeeCall,
    signalingState: SignalingState,
    reason: string,
    sipCode: number,
    sipReason: string,
  ) => void;
  onChangeMediaState: (
    stringeeCall: StringeeCall,
    mediaState: MediaState,
    description: string,
  ) => void;
  onReceiveLocalStream: (stringeeCall: StringeeCall) => void;
  onReceiveRemoteStream: (stringeeCall: StringeeCall) => void;
  onReceiveDtmfDigit: (stringeeCall: StringeeCall, dtmf: string) => void;
  onReceiveCallInfo: (stringeeCall: StringeeCall, callInfo: string) => void;
  onHandleOnAnotherDevice: (
    stringeeCall: StringeeCall,
    signalingState: SignalingState,
    description: string,
  ) => void;
  onAudioDeviceChange: (
    selectedAudioDevice: AudioDevice,
    availableAudioDevices: Array<AudioDevice>,
  ) => void;
}

export {StringeeCallListener};
