import PropTypes from "prop-types";
import { requireNativeComponent, ViewPropTypes, Platform } from "react-native";

var iface = {
  name: "StringeeVideoView",
  propTypes: {
    callId: PropTypes.string.isRequired,
    local: PropTypes.bool.isRequired,
    streamId: PropTypes.string.isRequired,
    ...ViewPropTypes
  }
};
module.exports =
  Platform.OS === "android"
    ? requireNativeComponent("RNStringeeVideoViewManager", iface)
    : null;
