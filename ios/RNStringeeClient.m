
#import "RNStringeeClient.h"
#import "RNStringeeInstanceManager.h"
#import <React/RCTLog.h>

// Connect
static NSString *didConnect               = @"didConnect";
static NSString *didDisConnect            = @"didDisConnect";
static NSString *didFailWithError         = @"didFailWithError";
static NSString *requestAccessToken       = @"requestAccessToken";

// Call 1-1
static NSString *incomingCall               = @"incomingCall";


@implementation RNStringeeClient {
    NSMutableArray<NSString *> *jsEvents;
}

@synthesize bridge = _bridge;

RCT_EXPORT_MODULE();

- (instancetype)init {
    self = [super init];
    [RNStringeeInstanceManager instance].rnClient = self;
    jsEvents = [[NSMutableArray alloc] init];
    return self;
}

- (NSArray<NSString *> *)supportedEvents {
    return @[didConnect,
             didDisConnect,
             didFailWithError,
             requestAccessToken,
             incomingCall
             ];
}

+ (BOOL)requiresMainQueueSetup {
    return YES;
}

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

RCT_EXPORT_METHOD(connect:(NSString *)accessToken) {
    if (!_client) {
        _client = [[StringeeClient alloc] initWithConnectionDelegate:self];
        _client.incomingCallDelegate = self;
    }
    [_client connectWithAccessToken:accessToken];
}

RCT_EXPORT_METHOD(disconnect) {
    if (_client) {
        [_client disconnect];
    }
}

RCT_EXPORT_METHOD(registerPushForDeviceToken:(NSString *)deviceToken isProduction:(BOOL)isProduction isVoip:(BOOL)isVoip callback:(RCTResponseSenderBlock)callback) {
    if (_client) {
        [_client registerPushForDeviceToken:deviceToken isProduction:isProduction isVoip:isVoip completionHandler:^(BOOL status, int code, NSString *message) {
            callback(@[@(status), @(code), message, _client.userId]);
        }];
    }
}

RCT_EXPORT_METHOD(unregisterPushToken:(NSString *)deviceToken callback:(RCTResponseSenderBlock)callback) {
    if (_client) {
        [_client unregisterPushForDeviceToken:deviceToken completionHandler:^(BOOL status, int code, NSString *message) {
            callback(@[@(status), @(code), message, _client.userId]);
        }];
    }
}

// Connect
- (void)requestAccessToken:(StringeeClient *)stringeeClient {
    [self sendEventWithName:requestAccessToken body:@{ @"userId" : stringeeClient.userId }];
}

- (void)didConnect:(StringeeClient *)stringeeClient isReconnecting:(BOOL)isReconnecting {
    if ([jsEvents containsObject:didConnect]) {
        [self sendEventWithName:didConnect body:@{ @"userId" : stringeeClient.userId, @"isReconnecting" : @(isReconnecting) }];
    }
}

- (void)didDisConnect:(StringeeClient *)stringeeClient isReconnecting:(BOOL)isReconnecting {
    if ([jsEvents containsObject:didDisConnect]) {
        [self sendEventWithName:didDisConnect body:@{ @"userId" : stringeeClient.userId, @"isReconnecting" : @(isReconnecting) }];
    }
}

- (void)didFailWithError:(StringeeClient *)stringeeClient code:(int)code message:(NSString *)message {
    if ([jsEvents containsObject:didFailWithError]) {
        [self sendEventWithName:didFailWithError body:@{ @"userId" : stringeeClient.userId, @"code" : @(code), @"message" : message }];
    }
}

// Call
- (void)incomingCallWithStringeeClient:(StringeeClient *)stringeeClient stringeeCall:(StringeeCall *)stringeeCall {
    [[RNStringeeInstanceManager instance].calls setObject:stringeeCall forKey:stringeeCall.callId];

    if ([jsEvents containsObject:incomingCall]) {

        int index = 0;

        if (stringeeCall.callType == CallTypeCallIn) {
            // Phone-to-app
            index = 3;
        } else if (stringeeCall.callType == CallTypeCallOut) {
            // App-to-phone
            index = 2;
        } else if (stringeeCall.callType == CallTypeInternalIncomingCall) {
            // App-to-app-incoming-call
            index = 1;
        } else {
            // App-to-app-outgoing-call
            index = 0;
        }

        [self sendEventWithName:incomingCall body:@{ @"userId" : stringeeClient.userId, @"callId" : stringeeCall.callId, @"from" : stringeeCall.from, @"to" : stringeeCall.to, @"fromAlias" : stringeeCall.fromAlias, @"toAlias" : stringeeCall.toAlias, @"callType" : @(index), @"isVideoCall" : @(stringeeCall.isVideoCall) }];
    }
    
}



@end
