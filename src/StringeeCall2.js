import {Component} from "react";
import PropTypes from "prop-types";
import {NativeModules, NativeEventEmitter, Platform} from "react-native";
import {callEvents} from "./helpers/StringeeHelper";
import {each} from "underscore";

const RNStringeeCall2 = NativeModules.RNStringeeCall2;

const iOS = Platform.OS === "ios" ? true : false;

export default class extends Component {
    static propTypes = {
        eventHandlers: PropTypes.object,
        clientId: PropTypes.string
    };

    constructor(props) {
        super(props);
        this._events = [];
        this._subscriptions = [];
        this._eventEmitter = new NativeEventEmitter(RNStringeeCall2);

        this.makeCall = this.makeCall.bind(this);
        this.initAnswer = this.initAnswer.bind(this);
    }

    componentWillMount() {
        this.sanitizeCallEvents(this.props.eventHandlers);
    }

    componentWillUnmount() {
        this._unregisterEvents();
    }

    render() {
        return null;
    }

    _unregisterEvents() {
        this._subscriptions.forEach(e => e.remove());
        this._subscriptions = [];

        this._events.forEach(e => RNStringeeCall2.removeNativeEvent(e));
        this._events = [];
    }

    sanitizeCallEvents(events) {
        if (typeof events !== "object") {
            return;
        }
        const platform = Platform.OS;

        each(events, (handler, type) => {
            const eventName = callEvents[platform][type];
            if (eventName !== undefined) {
                this._subscriptions.push(
                    this._eventEmitter.addListener(eventName, data => {
                        handler(data);
                    })
                );

                this._events.push(eventName);
                RNStringeeCall2.setNativeEvent(eventName);
            } else {
                console.log(`${type} is not a supported event`);
            }
        });
    }

    makeCall(parameters: string, callback: RNStringeeEventCallback) {
        RNStringeeCall2.makeCall(this.props.clientId, parameters, callback);
    }

    initAnswer(callId: string, callback: RNStringeeEventCallback) {
        RNStringeeCall2.initAnswer(this.props.clientId, callId, callback);
    }

    answer(callId: string, callback: RNStringeeEventCallback) {
        RNStringeeCall2.answer(callId, callback);
    }

    hangup(callId: string, callback: RNStringeeEventCallback) {
        RNStringeeCall2.hangup(callId, callback);
    }

    reject(callId: string, callback: RNStringeeEventCallback) {
        RNStringeeCall2.reject(callId, callback);
    }

    getCallStats(callId: string, callback: RNStringeeEventCallback) {
        RNStringeeCall2.getCallStats(this.props.clientId, callId, callback);
    }

    switchCamera(callId: string, callback: RNStringeeEventCallback) {
        RNStringeeCall2.switchCamera(callId, callback);
    }

    enableVideo(
        callId: string,
        enabled: boolean,
        callback: RNStringeeEventCallback
    ) {
        RNStringeeCall2.enableVideo(callId, enabled, callback);
    }

    mute(callId: string, mute: boolean, callback: RNStringeeEventCallback) {
        RNStringeeCall2.mute(callId, mute, callback);
    }

    setSpeakerphoneOn(
        callId: string,
        on: boolean,
        callback: RNStringeeEventCallback
    ) {
        RNStringeeCall2.setSpeakerphoneOn(callId, on, callback);
    }

    resumeVideo(
        callId: string,
        callback: RNStringeeEventCallback
    ) {
        if (iOS) {
            console.log('this function only for android');
        } else {
            RNStringeeCall2.resumeVideo(callId, callback);
        }
    }
}
