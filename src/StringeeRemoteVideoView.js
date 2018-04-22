import React, { Component } from 'react'
import PropTypes from 'prop-types'
import { requireNativeComponent } from 'react-native'

const RNStringeeRemoteVideoView = requireNativeComponent('RNStringeeRemoteVideoView', StringeeRemoteVideoView)

export default class StringeeRemoteVideoView extends Component {

  static propTypes = {
    callId: PropTypes.string.isRequired,
    streamId: PropTypes.string.isRequired
  }

  render() {
    return <RNStringeeRemoteVideoView {...this.props}>{this.props.children}</RNStringeeRemoteVideoView>
  }
}