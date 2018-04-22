import PropTypes from "prop-types";
import { View, requireNativeComponent } from "react-native";

const viewPropTypes = View.propTypes;
var iface = {
  name: "StringeeVideoView",
  PropTypes: {
    callId: PropTypes.string,
    local: PropTypes.boolean,
    ...viewPropTypes
  }
};
module.exports = requireNativeComponent("RNStringeeVideoViewManager", iface);
