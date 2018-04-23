import React, { Component } from "react";
import PropTypes from "prop-types";
import { requireNativeComponent, Platform } from "react-native";

const RNStringeeRemoteVideoView =
  Platform.OS === "ios"
    ? requireNativeComponent(
        "RNStringeeRemoteVideoView",
        StringeeRemoteVideoView
      )
    : null;

export default class StringeeRemoteVideoView extends Component {
  static propTypes = {
    callId: PropTypes.string.isRequired,
    streamId: PropTypes.string.isRequired
  };

  render() {
    if (Platform.OS === "ios") {
      return (
        <RNStringeeRemoteVideoView {...this.props}>
          {this.props.children}
        </RNStringeeRemoteVideoView>
      );
    } else {
      return null;
    }
  }
}
