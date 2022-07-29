
#import "RNStringeeCall2.h"
#import "RNStringeeInstanceManager.h"
#import <React/RCTLog.h>

static NSString *didChangeSignalingState    = @"didChangeSignalingState";
static NSString *didChangeMediaState        = @"didChangeMediaState";
static NSString *didReceiveLocalStream      = @"didReceiveLocalStream";
static NSString *didReceiveRemoteStream     = @"didReceiveRemoteStream";

static NSString *didReceiveDtmfDigit        = @"didReceiveDtmfDigit";
static NSString *didReceiveCallInfo         = @"didReceiveCallInfo";

static NSString *didHandleOnAnotherDevice   = @"didHandleOnAnotherDevice";
static NSString *trackMediaStateChange      = @"trackMediaStateChange";


@implementation RNStringeeCall2 {
    NSMutableArray<NSString *> *jsEvents;
}

@synthesize bridge = _bridge;

RCT_EXPORT_MODULE();

- (instancetype)init {
    self = [super init];
    if (self) {
        [RNStringeeInstanceManager instance].rnCall2 = self;
        jsEvents = [[NSMutableArray alloc] init];
    }
    return self;
}

- (NSArray<NSString *> *)supportedEvents {
    return @[didChangeSignalingState,
             didChangeMediaState,
             didReceiveLocalStream,
             didReceiveRemoteStream,
             didReceiveDtmfDigit,
             didReceiveCallInfo,
             didHandleOnAnotherDevice,
             trackMediaStateChange
             ];
}

+ (BOOL)requiresMainQueueSetup {
    return YES;
}

// TODO: - Publish Functions

RCT_EXPORT_METHOD(setNativeEvent:(NSString *)event) {
    [jsEvents addObject:event];
}

RCT_EXPORT_METHOD(removeNativeEvent:(NSString *)event) {
    int index = -1;
    index = (int)[jsEvents indexOfObject:event];
    if (index >= 0) {
        [jsEvents removeObjectAtIndex:index];
    }
}

RCT_EXPORT_METHOD(makeCall:(NSString *)uuid parameters:(NSString *)parameters callback:(RCTResponseSenderBlock)callback) {

    NSError *jsonError;
    NSData *objectData = [parameters dataUsingEncoding:NSUTF8StringEncoding];
    NSDictionary *data = [NSJSONSerialization JSONObjectWithData:objectData
                                      options:NSJSONReadingMutableContainers 
                                        error:&jsonError];
    if (jsonError) {
        callback(@[@(NO), @(-4), @"The parameters format is invalid.", [NSNull null], [NSNull null]]);
    } else {
        NSString *from = data[@"from"];
        NSString *to = data[@"to"];
        NSNumber *isVideoCall = data[@"isVideoCall"];
        NSString *customData = data[@"customData"];
        NSString *videoResolution = data[@"videoResolution"];
        
        RNClientWrapper *wrapper = [RNStringeeInstanceManager.instance.clientWrappers objectForKey:uuid];
        if (wrapper == nil) {
            callback(@[@(NO), @(-1), @"Wrapper is not found", [NSNull null], [NSNull null]]);
            return;
        }
        
        if (!wrapper.client) {
            callback(@[@(NO), @(-1), @"StringeeClient is not initialized", [NSNull null], [NSNull null]]);
            return;
        }

        StringeeCall2 *outgoingCall = [[StringeeCall2 alloc] initWithStringeeClient:wrapper.client from:from to:to];
        outgoingCall.delegate = self;
        outgoingCall.isVideoCall = [isVideoCall boolValue];

        if (customData.length) {
            outgoingCall.customData = customData;
        }

        if ([videoResolution isEqualToString:@"NORMAL"]) {
            outgoingCall.videoResolution = VideoResolution_Normal;
        } else if ([videoResolution isEqualToString:@"HD"]) {
            outgoingCall.videoResolution = VideoResolution_HD;
        }

        __weak StringeeCall2 *weakCall = outgoingCall;
        __weak NSMutableDictionary *weakCalls = [RNStringeeInstanceManager instance].call2s;

        [outgoingCall makeCallWithCompletionHandler:^(BOOL status, int code, NSString *message, NSString *data) {
            StringeeCall2 *strongCall = weakCall;
            NSMutableDictionary *strongCalls = weakCalls;
            if (status) {
                [strongCalls setObject:strongCall forKey:strongCall.callId]; 
            } 
            id returnCallId = strongCall.callId ? strongCall.callId : [NSNull null];
            id returnData = data ? data : [NSNull null];
            callback(@[@(status), @(code), message, returnCallId, returnData]);
        }];
    }
}

RCT_EXPORT_METHOD(initAnswer:(NSString *)uuid callId:(NSString *)callId callback:(RCTResponseSenderBlock)callback) {
    RNClientWrapper *wrapper = [RNStringeeInstanceManager.instance.clientWrappers objectForKey:uuid];
    if (wrapper == nil) {
        callback(@[@(NO), @(-1), @"Wrapper is not found", [NSNull null], [NSNull null]]);
        return;
    }
    
    if (wrapper.client && wrapper.client.hasConnected) {
        if (callId.length) {
            StringeeCall2 *call = [[RNStringeeInstanceManager instance].call2s objectForKey:callId];
            if (call) {
                call.delegate = self;
                [call initAnswerCall];
                callback(@[@(YES), @(0), @"Init answer call successfully."]);
            } else {
                callback(@[@(NO), @(-3), @"Init answer call failed. The call is not found."]);
            }
        } else {
            callback(@[@(NO), @(-2), @"Init answer call failed. The callId is invalid."]);
        }
    } else {
        callback(@[@(NO), @(-1), @"StringeeClient is not initialzied or connected."]);
    }
}

RCT_EXPORT_METHOD(answer:(NSString *)callId callback:(RCTResponseSenderBlock)callback) {
    if (callId.length) {
        StringeeCall2 *call = [[RNStringeeInstanceManager instance].call2s objectForKey:callId];
        if (call) {
            [call answerCallWithCompletionHandler:^(BOOL status, int code, NSString *message) {
                callback(@[@(status), @(code), message]);
            }];
        } else {
            callback(@[@(NO), @(-3), @"Answer call failed. The call is not found."]);
        }
    } else {
        callback(@[@(NO), @(-2), @"Answer call failed. The callId is invalid."]);
    }
}

RCT_EXPORT_METHOD(hangup:(NSString *)callId callback:(RCTResponseSenderBlock)callback) {
    if (callId.length) {
        StringeeCall2 *call = [[RNStringeeInstanceManager instance].call2s objectForKey:callId];
        if (call) {
            [call hangupWithCompletionHandler:^(BOOL status, int code, NSString *message) {
                callback(@[@(status), @(code), message]);
            }];
        } else {
            callback(@[@(NO), @(-3), @"Hangup call failed. The call is not found."]);
        }
        
    } else {
        callback(@[@(NO), @(-2), @"Hangup call failed. The callId is invalid."]);
    }
}

RCT_EXPORT_METHOD(reject:(NSString *)callId callback:(RCTResponseSenderBlock)callback) {
    if (callId.length) {
        StringeeCall2 *call = [[RNStringeeInstanceManager instance].call2s objectForKey:callId];
        if (call) {
            [call rejectWithCompletionHandler:^(BOOL status, int code, NSString *message) {
                callback(@[@(status), @(code), message]);
            }];
        } else {
            callback(@[@(NO), @(-3), @"Reject call failed. The call is not found."]);
        }
    } else {
        callback(@[@(NO), @(-2), @"Reject call failed. The callId is invalid."]);
    }
}

RCT_EXPORT_METHOD(mute:(NSString *)callId mute:(BOOL)mute callback:(RCTResponseSenderBlock)callback) {

    if (!callId.length) {
        callback(@[@(NO), @(-2), @"The call id is invalid."]);
        return;
    }

    StringeeCall2 *call = [[RNStringeeInstanceManager instance].call2s objectForKey:callId];

    if (!call) {
        callback(@[@(NO), @(-3), @"The call is not found."]);
        return;
    }

    [call mute:mute];
    callback(@[@(YES), @(0), @"Success"]);

}

RCT_EXPORT_METHOD(setSpeakerphoneOn:(NSString *)callId speaker:(BOOL)speaker callback:(RCTResponseSenderBlock)callback) {

    if (!callId.length) {
        callback(@[@(NO), @(-2), @"The call id is invalid."]);
        return;
    }

    StringeeCall2 *call = [[RNStringeeInstanceManager instance].call2s objectForKey:callId];

    if (!call) {
        callback(@[@(NO), @(-3), @"The call is not found."]);
        return;
    }

    [[StringeeAudioManager instance] setLoudspeaker:speaker];
    callback(@[@(YES), @(0), @"Success"]);
}

RCT_EXPORT_METHOD(switchCamera:(NSString *)callId callback:(RCTResponseSenderBlock)callback) {

    if (!callId.length) {
        callback(@[@(NO), @(-2), @"The call id is invalid."]);
        return;
    }

    StringeeCall2 *call = [[RNStringeeInstanceManager instance].call2s objectForKey:callId];

    if (!call) {
        callback(@[@(NO), @(-3), @"The call is not found."]);
        return;
    }

    dispatch_async(dispatch_get_main_queue(), ^{
        [call switchCamera];
    });
    callback(@[@(YES), @(0), @"Success"]);
}

RCT_EXPORT_METHOD(enableVideo:(NSString *)callId enableVideo:(BOOL)enableVideo callback:(RCTResponseSenderBlock)callback) {

    if (!callId.length) {
        callback(@[@(NO), @(-2), @"The call id is invalid."]);
        return;
    }

    StringeeCall2 *call = [[RNStringeeInstanceManager instance].call2s objectForKey:callId];

    if (!call) {
        callback(@[@(NO), @(-3), @"The call is not found."]);
        return;
    }
    
    dispatch_async(dispatch_get_main_queue(), ^{
        [call enableLocalVideo:enableVideo];
    });
    callback(@[@(YES), @(0), @"Success"]);
}

RCT_EXPORT_METHOD(sendCallInfo:(NSString *)callId callInfo:(NSString *)callInfo callback:(RCTResponseSenderBlock)callback) {
    if (callId.length) {
        StringeeCall2 *call = [[RNStringeeInstanceManager instance].call2s objectForKey:callId];
        if (call) {

            NSError *jsonError;
            NSData *objectData = [callInfo dataUsingEncoding:NSUTF8StringEncoding];
            NSDictionary *data = [NSJSONSerialization JSONObjectWithData:objectData
                                                        options:NSJSONReadingMutableContainers 
                                                        error:&jsonError];

            if (jsonError) {
                callback(@[@(NO), @(-4), @"The call info format is invalid."]);
            } else {
                [call sendCallInfo:data completionHandler:^(BOOL status, int code, NSString *message) {
                    if (status) {
                        callback(@[@(YES), @(0), @"Sends successfully"]);
                    } else {
                        callback(@[@(NO), @(-1), @"Failed to send. The client is not connected to Stringee Server."]);
                    }
                }];
            }

        } else {
            callback(@[@(NO), @(-3), @"Failed to send. The call is not found"]);
        }
    } else {
        callback(@[@(NO), @(-2), @"Failed to send. The callId is invalid"]);
    }
}

RCT_EXPORT_METHOD(setAutoSendTrackMediaStateChangeEvent:(NSString *)callId autoSendTrackMediaStateChangeEvent:(BOOL)autoSendTrackMediaStateChangeEvent callback:(RCTResponseSenderBlock)callback) {

    if (!callId.length) {
        callback(@[@(NO), @(-2), @"The call id is invalid."]);
        return;
    }

    StringeeCall2 *call = [[RNStringeeInstanceManager instance].call2s objectForKey:callId];

    if (!call) {
        callback(@[@(NO), @(-3), @"The call is not found."]);
        return;
    }
    
    call.autoSendTrackMediaState = autoSendTrackMediaStateChangeEvent;
    callback(@[@(YES), @(0), @"Success"]);
}

RCT_EXPORT_METHOD(sendDTMF:(NSString *)callId dtmf:(NSString *)dtmf callback:(RCTResponseSenderBlock)callback) {
    if (callId.length) {
        StringeeCall *call = [[RNStringeeInstanceManager instance].call2s objectForKey:callId];
        if (call) {
            NSArray *DTMF = @[@"0", @"1", @"2", @"3", @"4", @"5", @"6", @"7", @"8", @"9", @"*", @"#"];
            if ([DTMF containsObject:dtmf]) {

                CallDTMF dtmfParam;
        
                if ([dtmf isEqualToString:@"0"]) {
                    dtmfParam = CallDTMFZero;
                }
                else if ([dtmf isEqualToString:@"1"]) {
                    dtmfParam = CallDTMFOne;
                }
                else if ([dtmf isEqualToString:@"2"]) {
                    dtmfParam = CallDTMFTwo;
                }
                else if ([dtmf isEqualToString:@"3"]) {
                    dtmfParam = CallDTMFThree;
                }
                else if ([dtmf isEqualToString:@"4"]) {
                    dtmfParam = CallDTMFFour;
                }
                else if ([dtmf isEqualToString:@"5"]) {
                    dtmfParam = CallDTMFFive;
                }
                else if ([dtmf isEqualToString:@"6"]) {
                    dtmfParam = CallDTMFSix;
                }
                else if ([dtmf isEqualToString:@"7"]) {
                    dtmfParam = CallDTMFSeven;
                }
                else if ([dtmf isEqualToString:@"8"]) {
                    dtmfParam = CallDTMFEight;
                }
                else if ([dtmf isEqualToString:@"9"]) {
                    dtmfParam = CallDTMFNine;
                }
                else if ([dtmf isEqualToString:@"*"]) {
                    dtmfParam = CallDTMFStar;
                }
                else {
                    dtmfParam = CallDTMFPound;
                }

                [call sendDTMF:dtmfParam completionHandler:^(BOOL status, int code, NSString *message) {
                    if (status) {
                        callback(@[@(YES), @(0), @"Sends successfully"]);
                    } else {
                        callback(@[@(NO), @(-1), @"Failed to send. The client is not connected to Stringee Server."]);
                    }
                }];
            } else {
                callback(@[@(NO), @(-4), @"Failed to send. The dtmf is invalid."]);
            }
        } else {
            callback(@[@(NO), @(-3), @"Failed to send. The call is not found."]);
        }
    } else {
        callback(@[@(NO), @(-2), @"Failed to send. The callId is invalid."]);
    }
}

- (void)didChangeSignalingState2:(StringeeCall2 *)stringeeCall2 signalingState:(SignalingState)signalingState reason:(NSString *)reason sipCode:(int)sipCode sipReason:(NSString *)sipReason {
    if ([jsEvents containsObject:didChangeSignalingState]) {
        [self sendEventWithName:didChangeSignalingState body:@{ @"callId" : stringeeCall2.callId, @"code" : @(signalingState), @"reason" : reason, @"sipCode" : @(sipCode), @"sipReason" : sipReason,  @"serial": @(stringeeCall2.serial) }];
    }
    
    // Xoá videoTrack
    if (signalingState == SignalingStateBusy || signalingState == SignalingStateEnded) {
        [[RNStringeeInstanceManager instance].call2VideoTracks removeObjectForKey:stringeeCall2.callId];
    }
}

- (void)didChangeMediaState2:(StringeeCall2 *)stringeeCall2 mediaState:(MediaState)mediaState {

    if ([jsEvents containsObject:didChangeMediaState]) {
        switch (mediaState) {
            case MediaStateConnected:
                [self sendEventWithName:didChangeMediaState body:@{ @"callId" : stringeeCall2.callId, @"code" : @(0), @"description" : @"Connected" }];
                break;
            case MediaStateDisconnected:
                [self sendEventWithName:didChangeMediaState body:@{ @"callId" : stringeeCall2.callId, @"code" : @(1), @"description" : @"Disconnected" }];
                break;
            default:
                break;
        }
    }

}

- (void)didReceiveLocalStream2:(StringeeCall2 *)stringeeCall2 {
    if ([jsEvents containsObject:didReceiveLocalStream]) {
        [self sendEventWithName:didReceiveLocalStream body:@{ @"callId" : stringeeCall2.callId }];
    }    
}

- (void)didReceiveRemoteStream2:(StringeeCall2 *)stringeeCall2 {
    if ([jsEvents containsObject:didReceiveRemoteStream]) {
        [self sendEventWithName:didReceiveRemoteStream body:@{ @"callId" : stringeeCall2.callId }];
    }
}

- (void)didAddTrack2:(StringeeCall2 *)stringeeCall2 track:(StringeeVideoTrack *)track {
    [[RNStringeeInstanceManager instance].call2VideoTracks setObject:track forKey:stringeeCall2.callId];
    if ([jsEvents containsObject:didReceiveRemoteStream]) {
        [self sendEventWithName:didReceiveRemoteStream body:@{ @"callId" : stringeeCall2.callId }];
    }
}

- (void)didHandleOnAnotherDevice2:(StringeeCall2 *)stringeeCall2 signalingState:(SignalingState)signalingState reason:(NSString *)reason sipCode:(int)sipCode sipReason:(NSString *)sipReason {
    if ([jsEvents containsObject:didHandleOnAnotherDevice]) {
        [self sendEventWithName:didHandleOnAnotherDevice body:@{ @"callId" : stringeeCall2.callId, @"code" : @(signalingState), @"description" : reason }];
    }
}

- (void)didReceiveCallInfo2:(StringeeCall2 *)stringeeCall2 info:(NSDictionary *)info {
    if ([jsEvents containsObject:didReceiveCallInfo]) {
        NSData *jsonData = [NSJSONSerialization dataWithJSONObject:info
                                            options:NSJSONWritingPrettyPrinted
                                            error:nil];
        NSString *jsonString = [[[NSString alloc] initWithData:jsonData encoding:NSUTF8StringEncoding] stringByReplacingOccurrencesOfString:@" " withString:@""];
        [self sendEventWithName:didReceiveCallInfo body:@{ @"callId" : stringeeCall2.callId, @"data" : jsonString }];
    }
}

- (void)trackMediaStateChange:(StringeeCall2 *)stringeeCall2 mediaType:(StringeeTrackMediaType)mediaType enable:(BOOL)enable from:(NSString *)from {
    if ([jsEvents containsObject:trackMediaStateChange]) {
        [self sendEventWithName:trackMediaStateChange body:@{ @"from" : from, @"mediaType" : @(mediaType), @"enable" : @(enable) }];
    }
}

- (void)addRenderToView:(UIView *)view callId:(NSString *)callId isLocal:(BOOL)isLocal {
    if (callId.length) {
        StringeeCall2 *call = [[RNStringeeInstanceManager instance].call2s objectForKey:callId];
        if (call) {
            if (isLocal) {
                call.localVideoView.frame = CGRectMake(0, 0, view.bounds.size.width, view.bounds.size.height);
                [view addSubview:call.localVideoView];
            } else {
                StringeeVideoTrack *track = [[RNStringeeInstanceManager instance].call2VideoTracks objectForKey:callId];
                if (track != nil) {
                    StringeeVideoView *videoView = [track attachWithVideoContentMode:StringeeVideoContentModeScaleAspectFill];
                    if (videoView != nil) {
                        videoView.frame = CGRectMake(0, 0, view.bounds.size.width, view.bounds.size.height);
                        [view addSubview:videoView];
                    }
                    
                    [[RNStringeeInstanceManager instance].call2VideoTracks removeObjectForKey:callId];
                } else {
                    call.remoteVideoView.frame = CGRectMake(0, 0, view.bounds.size.width, view.bounds.size.height);
//                    call.remoteVideoView.delegate = view;
                    [view addSubview:call.remoteVideoView];
                }
            }
        }
    }
}


@end
