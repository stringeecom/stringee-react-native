import PropTypes from 'prop-types';
import {
  findNodeHandle,
  Platform,
  requireNativeComponent,
  UIManager,
  View,
} from 'react-native';
import React, {Component} from 'react';
import {ScalingType} from './helpers/StringeeHelper';

class StringeeVideoView extends Component {
  callId: string;
  trackId: string;
  local: boolean;
  overlay: boolean;
  isMirror: boolean;
  scalingType: ScalingType;

  constructor(props) {
    super(props);
    this.ref = React.createRef();
    this.callId = props.callId;
    this.trackId = props.trackId;
    this.local = props.local;
    this.overlay = props.overlay;
    this.isMirror = props.isMirror;
    this.scalingType = props.scalingType;
  }

  componentDidMount() {
    this.viewId = findNodeHandle(this.ref.current);
    if (Platform.OS === 'android') {
      this.createNativeView(this.viewId);
    }
  }

  createNativeView = viewId => {
    UIManager.dispatchViewManagerCommand(
      viewId,
      UIManager.RNStringeeVideoView.Commands.create.toString(),
      [],
    );
  };

  render(): React.ReactNode {
    return (
      <RCTStringeeVideoView
        {...this.props}
        style={this.props.style}
        callId={this.callId}
        trackId={this.trackId}
        local={this.local}
        overLay={this.overlay}
        isMirror={this.isMirror}
        scalingType={this.scalingType}
        ref={this.ref}
      />
    );
  }
}

StringeeVideoView.propTypes = {
  callId: PropTypes.string,
  trackId: PropTypes.string,
  local: PropTypes.bool,
  overlay: PropTypes.bool,
  isMirror: PropTypes.bool,
  scalingType: PropTypes.oneOf([ScalingType.FIT, ScalingType.FILL]),
  ...View.propTypes,
};

const RCTStringeeVideoView = requireNativeComponent('RNStringeeVideoView');

export {StringeeVideoView};
