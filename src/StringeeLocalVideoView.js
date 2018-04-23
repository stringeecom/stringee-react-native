import React, { Component } from "react";
import PropTypes from "prop-types";
import { requireNativeComponent, Platform } from "react-native";

const RNStringeeLocalVideoView =
  Platform.OS === "ios"
    ? requireNativeComponent("RNStringeeLocalVideoView", StringeeLocalVideoView)
    : null;

export default class StringeeLocalVideoView extends Component {
  static propTypes = {
    callId: PropTypes.string.isRequired,
    streamId: PropTypes.string.isRequired
  };

  static defaultProps = {
    callId: "",
    streamId: ""
  };

  render() {
    if (Platform.OS === "ios") {
      return (
        <RNStringeeLocalVideoView {...this.props}>
          {this.props.children}
        </RNStringeeLocalVideoView>
      );
    } else {
      return null;
    }
  }
}
