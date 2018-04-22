
import React, { Component } from 'react'
import PropTypes from 'prop-types'
import { requireNativeComponent } from 'react-native'

const RNStringeeLocalVideoView = requireNativeComponent('RNStringeeLocalVideoView', StringeeLocalVideoView)

export default class StringeeLocalVideoView extends Component {

  static propTypes = {
    callId: PropTypes.string.isRequired,
    streamId: PropTypes.string.isRequired
  }

  static defaultProps = {
    callId:'',
    streamId:''
  };

  render() {
    return <RNStringeeLocalVideoView {...this.props}>{this.props.children}</RNStringeeLocalVideoView>
  }
}