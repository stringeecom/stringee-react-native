import {Component} from "react";
import PropTypes from "prop-types";
import {NativeModules, NativeEventEmitter, Platform} from "react-native";
import {clientEvents} from "./helpers/StringeeHelper";
import {each} from "underscore";

const RNStringeeClient = NativeModules.RNStringeeClient;

const iOS = Platform.OS === "ios" ? true : false;

export default class extends Component {
    static propTypes = {
        eventHandlers: PropTypes.object
    };

    constructor(props) {
        super(props);
        this._events = [];
        this._subscriptions = [];
        this._eventEmitter = new NativeEventEmitter(RNStringeeClient);
        if (!iOS) {
            RNStringeeClient.init();
        }
        this.sanitizeClientEvents(this.props.eventHandlers);
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

        this._events.forEach(e => RNStringeeClient.removeNativeEvent(e));
        this._events = [];
    }

    sanitizeClientEvents(events) {
        if (typeof events !== "object") {
            return;
        }
        const platform = Platform.OS;

        each(events, (handler, type) => {
            const eventName = clientEvents[platform][type];
            if (eventName !== undefined) {
                this._subscriptions.push(
                    this._eventEmitter.addListener(eventName, data => {
                        handler(data);
                    })
                );

                this._events.push(eventName);
                RNStringeeClient.setNativeEvent(eventName);
            } else {
                console.log(`${type} is not a supported event`);
            }
        });
    }

    connect(token: string) {
        RNStringeeClient.connect(token);
    }

    disconnect() {
        RNStringeeClient.disconnect();
    }

    registerPush(
        deviceToken: string,
        isProduction: boolean,
        isVoip: boolean,
        callback: RNStringeeEventCallback
    ) {
        if (iOS) {
            RNStringeeClient.registerPushForDeviceToken(
                deviceToken,
                isProduction,
                isVoip,
                callback
            );
        } else {
            RNStringeeClient.registerPushToken(deviceToken, callback);
        }
    }

    unregisterPush(deviceToken: string, callback: RNStringeeEventCallback) {
        RNStringeeClient.unregisterPushToken(deviceToken, callback);
    }

    sendCustomMessage(
        toUserId: string,
        message: string,
        callback: RNStringeeEventCallback
    ) {
        RNStringeeClient.sendCustomMessage(toUserId, message, callback);
    }
}
