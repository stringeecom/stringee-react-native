import PropTypes from 'prop-types';
import {requireNativeComponent, Platform, View} from 'react-native';
import React from 'react';
// import {ScalingType} from './helpers/StringeeHelper';

const StringeeVideoView = props => (
  <RCTStringeeVideoView
    {...props}
    style={props.style}
    callId={props.callId}
    trackId={props.trackId}
    local={props.local}
    overlay={props.overlay}
    isMirror={props.isMirror}
    // scalingType={props.scalingType}
  />
);

StringeeVideoView.propTypes = {
  callId: PropTypes.string,
  trackId: PropTypes.string,
  local: PropTypes.bool,
  overlay: PropTypes.bool,
  isMirror: PropTypes.bool,
  // scalingType: PropTypes.string,
  ...View.propTypes,
};
const RCTStringeeVideoView =
  Platform.OS === 'android'
    ? requireNativeComponent('RNStringeeVideoViewManager')
    : requireNativeComponent('RNStringeeVideoView');

export default StringeeVideoView;
