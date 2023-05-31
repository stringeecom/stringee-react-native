import PropTypes from 'prop-types';
import {
  findNodeHandle,
  Platform,
  requireNativeComponent,
  UIManager,
  View,
} from 'react-native';
import React, {Component} from 'react';
import {StringeeVideoScalingType} from './helpers/StringeeHelper';

class StringeeVideoView extends Component {
  callId: string;
  local: boolean;
  overlay: boolean;
  scalingType: StringeeVideoScalingType;

  constructor(props) {
    super(props);
    this.ref = React.createRef();
    this.callId = props.callId;
    this.local = props.local !== undefined ? props.local : false;
    this.overlay = props.overlay !== undefined ? props.overlay : false;
    this.scalingType =
      props.scalingType !== undefined
        ? props.scalingType
        : StringeeVideoScalingType.fill;
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
      <View style={this.props.style}>
        <RCTStringeeVideoView
          {...this.props}
          callId={this.callId}
          local={this.local}
          overLay={this.overlay}
          scalingType={this.scalingType}
          ref={this.ref}
        />
      </View>
    );
  }
}

StringeeVideoView.propTypes = {
  callId: PropTypes.string,
  local: PropTypes.bool,
  overlay: PropTypes.bool,
  scalingType: PropTypes.oneOf([
    StringeeVideoScalingType.fit,
    StringeeVideoScalingType.fill,
  ]),
  ...View.propTypes,
};

const RCTStringeeVideoView = requireNativeComponent('RNStringeeVideoView');

export {StringeeVideoView};
