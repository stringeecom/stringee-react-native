import PropTypes from 'prop-types';
import {requireNativeComponent, Platform} from 'react-native';
import {ViewPropTypes} from 'deprecated-react-native-prop-types';
var iface = {
  name: 'StringeeVideoView',
  propTypes: {
    callId: PropTypes.string.isRequired,
    local: PropTypes.bool.isRequired,
    overlay: PropTypes.bool.isRequired,
    ...ViewPropTypes,
  },
};
module.exports =
  Platform.OS === 'android'
    ? requireNativeComponent('RNStringeeVideoViewManager', iface)
    : requireNativeComponent('RNStringeeVideoView', iface);
