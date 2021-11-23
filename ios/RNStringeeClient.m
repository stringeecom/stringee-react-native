
#import "RNStringeeClient.h"
#import "RNStringeeInstanceManager.h"
#import <React/RCTLog.h>
#import <React/RCTUtils.h>
#import "RCTConvert+StringeeHelper.h"

@implementation RNStringeeClient {
//    NSMutableArray<NSString *> *jsEvents;
//    BOOL isConnecting;
}

@synthesize bridge = _bridge;

RCT_EXPORT_MODULE();

- (instancetype)init {
    self = [super init];
    [RNStringeeInstanceManager instance].rnClient = self;
    return self;
}

- (NSArray<NSString *> *)supportedEvents {
    return @[didConnect,
             didDisConnect,
             didFailWithError,
             requestAccessToken,
             incomingCall,
             incomingCall2,
             didReceiveCustomMessage,
             objectChangeNotification,
             didReceiveChatRequest,
             didReceiveTransferChatRequest,
             timeoutAnswerChat,
             timeoutInQueue,
             conversationEnded,
             userBeginTyping,
             userEndTyping
             ];
}

+ (BOOL)requiresMainQueueSetup {
    return YES;
}

RCT_EXPORT_METHOD(setNativeEvent:(NSString *)uuid event:(NSString *)event) {
    RNClientWrapper *clientWrapper = [RNStringeeInstanceManager.instance.clientWrappers objectForKey:uuid];
    [clientWrapper setNativeEvent:event];
}

RCT_EXPORT_METHOD(removeNativeEvent:(NSString *)uuid event:(NSString *)event) {
    RNClientWrapper *clientWrapper = [RNStringeeInstanceManager.instance.clientWrappers objectForKey:uuid];
    [clientWrapper removeNativeEvent:event];
}

RCT_EXPORT_METHOD(createClientWrapper:(NSString *)uuid baseUrl:(NSString *)baseUrl addresses:(NSArray *)addresses stringeeXBaseUrl:(NSString *)stringeeXBaseUrl) {
    RNClientWrapper *clientWrapper = [RNStringeeInstanceManager.instance.clientWrappers objectForKey:uuid];
    if (clientWrapper != nil) {
        return;
    }

    NSMutableArray<StringeeServerAddress *> *addrs = [NSMutableArray new];
    if (addresses != nil && addresses.count > 0) {
        for (NSDictionary *dic in addresses) {
            NSString *host = dic[@"host"];
            int port = [(NSNumber *)dic[@"port"] intValue];
            StringeeServerAddress *addr = [[StringeeServerAddress alloc] initWithHost:host port:port];
            [addrs addObject:addr];
        }
    }

    RNClientWrapper *newClientWrapper = [[RNClientWrapper alloc] initWithIdentifier:uuid baseUrl:baseUrl serverAddresses:addrs stringeeXBaseUrl:stringeeXBaseUrl];
    [RNStringeeInstanceManager.instance.clientWrappers setObject:newClientWrapper forKey:uuid];
}

RCT_EXPORT_METHOD(connect:(NSString *)uuid token:(NSString *)token) {
    // Lay ve wrapper
    RNClientWrapper *wrapper = [RNStringeeInstanceManager.instance.clientWrappers objectForKey:uuid];
    if (wrapper == nil) {
        RCTLog(@"Wrapper is not found");
        return;
    }

    if (wrapper.isConnecting) {
        return;
    }
    wrapper.isConnecting = YES;
    [wrapper createClientIfNeed];

    [wrapper.client connectWithAccessToken:token];
}

RCT_EXPORT_METHOD(disconnect:(NSString *)uuid) {
    RNClientWrapper *wrapper = [RNStringeeInstanceManager.instance.clientWrappers objectForKey:uuid];
    if (wrapper == nil) {
        RCTLog(@"Wrapper is not found");
        return;
    }

    if (wrapper.client) {
        [wrapper.client disconnect];
    }

    wrapper.isConnecting = NO;
}

RCT_EXPORT_METHOD(registerPushForDeviceToken:(NSString *)uuid deviceToken:(NSString *)deviceToken isProduction:(BOOL)isProduction isVoip:(BOOL)isVoip callback:(RCTResponseSenderBlock)callback) {
    RNClientWrapper *wrapper = [RNStringeeInstanceManager.instance.clientWrappers objectForKey:uuid];
    if (wrapper == nil) {
        callback(@[@(NO), @(-1), @"Wrapper is not found"]);
        return;
    }

    if (!wrapper.client || !wrapper.client.hasConnected) {
        callback(@[@(NO), @(-1), @"StringeeClient is not initialized or connected."]);
        return;
    }

    [wrapper.client registerPushForDeviceToken:deviceToken isProduction:isProduction isVoip:isVoip completionHandler:^(BOOL status, int code, NSString *message) {
        callback(@[@(status), @(code), message]);
    }];

}

RCT_EXPORT_METHOD(unregisterPushToken:(NSString *)uuid deviceToken:(NSString *)deviceToken callback:(RCTResponseSenderBlock)callback) {
    RNClientWrapper *wrapper = [RNStringeeInstanceManager.instance.clientWrappers objectForKey:uuid];
    if (wrapper == nil) {
        callback(@[@(NO), @(-1), @"Wrapper is not found"]);
        return;
    }

    if (!wrapper.client || !wrapper.client.hasConnected) {
        callback(@[@(NO), @(-1), @"StringeeClient is not initialized or connected."]);
        return;
    }

    [wrapper.client unregisterPushForDeviceToken:deviceToken completionHandler:^(BOOL status, int code, NSString *message) {
        callback(@[@(status), @(code), message]);
    }];

}

RCT_EXPORT_METHOD(sendCustomMessage:(NSString *)uuid userId:(NSString *)userId message:(NSString *)message callback:(RCTResponseSenderBlock)callback) {

    RNClientWrapper *wrapper = [RNStringeeInstanceManager.instance.clientWrappers objectForKey:uuid];
    if (wrapper == nil) {
        callback(@[@(NO), @(-1), @"Wrapper is not found"]);
        return;
    }

    if (!wrapper.client || !wrapper.client.hasConnected) {
        callback(@[@(NO), @(-1), @"StringeeClient is not initialized or connected."]);
        return;
    }

    if (!message) {
        callback(@[@(NO), @(-3), @"Message can not be nil."]);
        return;
    }

    NSError *jsonError;
    NSData *objectData = [message dataUsingEncoding:NSUTF8StringEncoding];
    NSDictionary *data = [NSJSONSerialization JSONObjectWithData:objectData
                                                         options:NSJSONReadingMutableContainers
                                                           error:&jsonError];

    if (jsonError) {
        callback(@[@(NO), @(-4), @"Message format is invalid."]);
        return;
    }

    [wrapper.client sendCustomMessage:data toUserId:userId completionHandler:^(BOOL status, int code, NSString *message) {
        callback(@[@(status), @(code), message]);
    }];
}

//// Connect
//- (void)requestAccessToken:(StringeeClient *)stringeeClient {
//    isConnecting = NO;
//    [self sendEventWithName:requestAccessToken body:@{ @"userId" : stringeeClient.userId }];
//}
//
//- (void)didConnect:(StringeeClient *)stringeeClient isReconnecting:(BOOL)isReconnecting {
//    if ([jsEvents containsObject:didConnect]) {
//        [self sendEventWithName:didConnect body:@{ @"userId" : stringeeClient.userId, @"projectId" : stringeeClient.projectId, @"isReconnecting" : @(isReconnecting) }];
//    }
//}
//
//- (void)didDisConnect:(StringeeClient *)stringeeClient isReconnecting:(BOOL)isReconnecting {
//    if ([jsEvents containsObject:didDisConnect]) {
//        [self sendEventWithName:didDisConnect body:@{ @"userId" : stringeeClient.userId, @"projectId" : stringeeClient.projectId, @"isReconnecting" : @(isReconnecting) }];
//    }
//}
//
//- (void)didFailWithError:(StringeeClient *)stringeeClient code:(int)code message:(NSString *)message {
//    if ([jsEvents containsObject:didFailWithError]) {
//        [self sendEventWithName:didFailWithError body:@{ @"userId" : stringeeClient.userId, @"code" : @(code), @"message" : message }];
//    }
//}
//
//- (void)didReceiveCustomMessage:(StringeeClient *)stringeeClient message:(NSDictionary *)message fromUserId:(NSString *)userId {
//    if ([jsEvents containsObject:didReceiveCustomMessage]) {
//        NSString *data;
//        if (message) {
//            NSError *err;
//            NSData *jsonData = [NSJSONSerialization dataWithJSONObject:message options:0 error:&err];
//            data = [[NSString alloc] initWithData:jsonData encoding:NSUTF8StringEncoding];
//        }
//
//        data = (data != nil) ? data : @"";
//
//        [self sendEventWithName:didReceiveCustomMessage body:@{ @"from" : userId, @"data" : data }];
//    }
//}
//
//// Call
//- (void)incomingCallWithStringeeClient:(StringeeClient *)stringeeClient stringeeCall:(StringeeCall *)stringeeCall {
//    [[RNStringeeInstanceManager instance].calls setObject:stringeeCall forKey:stringeeCall.callId];
//
//    if ([jsEvents containsObject:incomingCall]) {
//
//        int index = 0;
//
//        if (stringeeCall.callType == CallTypeCallIn) {
//            // Phone-to-app
//            index = 3;
//        } else if (stringeeCall.callType == CallTypeCallOut) {
//            // App-to-phone
//            index = 2;
//        } else if (stringeeCall.callType == CallTypeInternalIncomingCall) {
//            // App-to-app-incoming-call
//            index = 1;
//        } else {
//            // App-to-app-outgoing-call
//            index = 0;
//        }
//
//        id returnUserId = stringeeClient.userId ? stringeeClient.userId : [NSNull null];
//        id returnCallId = stringeeCall.callId ? stringeeCall.callId : [NSNull null];
//        id returnFrom = stringeeCall.from ? stringeeCall.from : [NSNull null];
//        id returnTo = stringeeCall.to ? stringeeCall.to : [NSNull null];
//        id returnFromAlias = stringeeCall.fromAlias ? stringeeCall.fromAlias : [NSNull null];
//        id returnToAlias = stringeeCall.toAlias ? stringeeCall.toAlias : [NSNull null];
//        id returnCustomData = stringeeCall.customDataFromYourServer ? stringeeCall.customDataFromYourServer : [NSNull null];
//
//        [self sendEventWithName:incomingCall body:@{ @"userId" : returnUserId, @"callId" : returnCallId, @"from" : returnFrom, @"to" : returnTo, @"fromAlias" : returnFromAlias, @"toAlias" : returnToAlias, @"callType" : @(index), @"isVideoCall" : @(stringeeCall.isVideoCall), @"customDataFromYourServer" : returnCustomData}];
//    }
//
//}

#pragma mark - Conversation

RCT_EXPORT_METHOD(createConversation:(NSString *)uuid userIds:(NSArray *)userIds options:(NSDictionary *)options callback:(RCTResponseSenderBlock)callback) {

    RNClientWrapper *wrapper = [RNStringeeInstanceManager.instance.clientWrappers objectForKey:uuid];
    if (wrapper == nil) {
        callback(@[@(NO), @(-1), @"Wrapper is not found", [NSNull null]]);
        return;
    }

    if (!wrapper.client || !wrapper.client.hasConnected) {
        callback(@[@(NO), @(-1), @"StringeeClient is not initialized or connected.", [NSNull null]]);
        return;
    }

    if (!userIds.count) {
        callback(@[@(NO), @(-3), @"UserIds is invalid.", [NSNull null]]);
        return;
    }

    NSMutableSet *users = [[NSMutableSet alloc] init];
    for (NSString *userId in userIds) {
        if (![userId isKindOfClass:[NSString class]]) {
            callback(@[@(NO), @(-3), @"UserIds is invalid.", [NSNull null]]);
            return;
        }

        StringeeIdentity *iden = [StringeeIdentity new];
        iden.userId = userId;
        [users addObject:iden];
    }


    if (![options isKindOfClass:[NSDictionary class]]) {
        callback(@[@(NO), @(-4), @"Options is invalid."]);
        return;
    }

    NSString *name = options[@"name"];
    NSNumber *distinctByParticipants = options[@"isDistinct"];
    NSNumber *isGroup = options[@"isGroup"];

    StringeeConversationOption *convOptions = [StringeeConversationOption new];
    convOptions.isGroup = [isGroup boolValue] ? [isGroup boolValue] : NO;
    convOptions.distinctByParticipants = distinctByParticipants ? [distinctByParticipants boolValue] : YES;

    [wrapper.client createConversationWithName:name participants:users options:convOptions completionHandler:^(BOOL status, int code, NSString *message, StringeeConversation *conversation) {
        callback(@[@(status), @(code), message, [RCTConvert StringeeConversation:conversation]]);
    }];
}

RCT_EXPORT_METHOD(getConversationById:(NSString *)uuid conversationId:(NSString *)conversationId callback:(RCTResponseSenderBlock)callback) {

    RNClientWrapper *wrapper = [RNStringeeInstanceManager.instance.clientWrappers objectForKey:uuid];
    if (wrapper == nil) {
        callback(@[@(NO), @(-1), @"Wrapper is not found", [NSNull null]]);
        return;
    }

    if (!wrapper.client || !wrapper.client.hasConnected) {
        callback(@[@(NO), @(-1), @"StringeeClient is not initialized or connected.", [NSNull null]]);
        return;
    }

    if (![conversationId isKindOfClass:[NSString class]]) {
        callback(@[@(NO), @(-2), @"ConversationId is invalid.", [NSNull null]]);
        return;
    }

    [wrapper.client getConversationWithConversationId:conversationId completionHandler:^(BOOL status, int code, NSString *message, StringeeConversation *conversation) {
        callback(@[@(status), @(code), message, [RCTConvert StringeeConversation:conversation]]);
    }];
}

RCT_EXPORT_METHOD(getLocalConversations:(NSString *)uuid count:(NSUInteger)count userId:(NSString *)userId callback:(RCTResponseSenderBlock)callback) {

    RNClientWrapper *wrapper = [RNStringeeInstanceManager.instance.clientWrappers objectForKey:uuid];
    if (wrapper == nil) {
        callback(@[@(NO), @(-1), @"Wrapper is not found", [NSNull null]]);
        return;
    }

    if (!wrapper.client || !wrapper.client.hasConnected) {
        callback(@[@(NO), @(-1), @"StringeeClient is not initialized or connected.", [NSNull null]]);
        return;
    }

    if (![RCTConvert isValid:userId]) {
        callback(@[@(NO), @(-3), @"UserId is invalid", [NSNull null]]);
        return;
    }

    [wrapper.client getLocalConversationsWithCount:count userId:userId completionHandler:^(BOOL status, int code, NSString *message, NSArray<StringeeConversation *> *conversations) {
        callback(@[@(status), @(code), message, [RCTConvert StringeeConversations:conversations]]);
    }];
}

RCT_EXPORT_METHOD(getLastConversations:(NSString *)uuid count:(NSUInteger)count callback:(RCTResponseSenderBlock)callback) {

    RNClientWrapper *wrapper = [RNStringeeInstanceManager.instance.clientWrappers objectForKey:uuid];
    if (wrapper == nil) {
        callback(@[@(NO), @(-1), @"Wrapper is not found", [NSNull null]]);
        return;
    }

    if (!wrapper.client || !wrapper.client.hasConnected) {
        callback(@[@(NO), @(-1), @"StringeeClient is not initialized or connected.", [NSNull null]]);
        return;
    }

    [wrapper.client getLastConversationsWithCount:count completionHandler:^(BOOL status, int code, NSString *message, NSArray<StringeeConversation *> *conversations) {
        callback(@[@(status), @(code), message, [RCTConvert StringeeConversations:conversations]]);
    }];
}

RCT_EXPORT_METHOD(getAllLastConversations:(NSString *)uuid count:(NSUInteger)count callback:(RCTResponseSenderBlock)callback) {

    RNClientWrapper *wrapper = [RNStringeeInstanceManager.instance.clientWrappers objectForKey:uuid];
    if (wrapper == nil) {
        callback(@[@(NO), @(-1), @"Wrapper is not found", [NSNull null]]);
        return;
    }

    if (!wrapper.client || !wrapper.client.hasConnected) {
        callback(@[@(NO), @(-1), @"StringeeClient is not initialized or connected.", [NSNull null]]);
        return;
    }

    [wrapper.client getLastConversationsWithCount:count loadAllConversations:true completionHandler:^(BOOL status, int code, NSString *message, NSArray<StringeeConversation *> *conversations) {
        callback(@[@(status), @(code), message, [RCTConvert StringeeConversations:conversations]]);
    }];
}

RCT_EXPORT_METHOD(getConversationsAfter:(NSString *)uuid datetime:(NSUInteger)datetime count:(NSUInteger)count callback:(RCTResponseSenderBlock)callback) {

    RNClientWrapper *wrapper = [RNStringeeInstanceManager.instance.clientWrappers objectForKey:uuid];
    if (wrapper == nil) {
        callback(@[@(NO), @(-1), @"Wrapper is not found", [NSNull null]]);
        return;
    }

    if (!wrapper.client || !wrapper.client.hasConnected) {
        callback(@[@(NO), @(-1), @"StringeeClient is not initialized or connected.", [NSNull null]]);
        return;
    }

    [wrapper.client getConversationsAfter:datetime withCount:count completionHandler:^(BOOL status, int code, NSString *message, NSArray<StringeeConversation *> *conversations) {
        callback(@[@(status), @(code), message, [RCTConvert StringeeConversations:conversations]]);
    }];
}

RCT_EXPORT_METHOD(getAllConversationsAfter:(NSString *)uuid datetime:(NSUInteger)datetime count:(NSUInteger)count callback:(RCTResponseSenderBlock)callback) {

    RNClientWrapper *wrapper = [RNStringeeInstanceManager.instance.clientWrappers objectForKey:uuid];
    if (wrapper == nil) {
        callback(@[@(NO), @(-1), @"Wrapper is not found", [NSNull null]]);
        return;
    }

    if (!wrapper.client || !wrapper.client.hasConnected) {
        callback(@[@(NO), @(-1), @"StringeeClient is not initialized or connected.", [NSNull null]]);
        return;
    }

    [wrapper.client getConversationsAfter:datetime withCount:count loadAllConversations:true completionHandler:^(BOOL status, int code, NSString *message, NSArray<StringeeConversation *> *conversations) {
        callback(@[@(status), @(code), message, [RCTConvert StringeeConversations:conversations]]);
    }];
}

RCT_EXPORT_METHOD(getConversationsBefore:(NSString *)uuid datetime:(NSUInteger)datetime count:(NSUInteger)count callback:(RCTResponseSenderBlock)callback) {

    RNClientWrapper *wrapper = [RNStringeeInstanceManager.instance.clientWrappers objectForKey:uuid];
    if (wrapper == nil) {
        callback(@[@(NO), @(-1), @"Wrapper is not found", [NSNull null]]);
        return;
    }

    if (!wrapper.client || !wrapper.client.hasConnected) {
        callback(@[@(NO), @(-1), @"StringeeClient is not initialized or connected.", [NSNull null]]);
        return;
    }

    [wrapper.client getConversationsBefore:datetime withCount:count completionHandler:^(BOOL status, int code, NSString *message, NSArray<StringeeConversation *> *conversations) {
        callback(@[@(status), @(code), message, [RCTConvert StringeeConversations:conversations]]);
    }];
}

RCT_EXPORT_METHOD(getAllConversationsBefore:(NSString *)uuid datetime:(NSUInteger)datetime count:(NSUInteger)count callback:(RCTResponseSenderBlock)callback) {

    RNClientWrapper *wrapper = [RNStringeeInstanceManager.instance.clientWrappers objectForKey:uuid];
    if (wrapper == nil) {
        callback(@[@(NO), @(-1), @"Wrapper is not found", [NSNull null]]);
        return;
    }

    if (!wrapper.client || !wrapper.client.hasConnected) {
        callback(@[@(NO), @(-1), @"StringeeClient is not initialized or connected.", [NSNull null]]);
        return;
    }

    [wrapper.client getConversationsBefore:datetime withCount:count loadAllConversations:true completionHandler:^(BOOL status, int code, NSString *message, NSArray<StringeeConversation *> *conversations) {
        callback(@[@(status), @(code), message, [RCTConvert StringeeConversations:conversations]]);
    }];
}

RCT_EXPORT_METHOD(deleteConversation:(NSString *)uuid conversationId:(NSString *)conversationId callback:(RCTResponseSenderBlock)callback) {

    RNClientWrapper *wrapper = [RNStringeeInstanceManager.instance.clientWrappers objectForKey:uuid];
    if (wrapper == nil) {
        callback(@[@(NO), @(-1), @"Wrapper is not found"]);
        return;
    }

    if (!wrapper.client || !wrapper.client.hasConnected) {
        callback(@[@(NO), @(-1), @"StringeeClient is not initialized or connected."]);
        return;
    }

    if (![conversationId isKindOfClass:[NSString class]] || !conversationId.length) {
        callback(@[@(NO), @(-2), @"Conversation not found."]);
        return;
    }

    [wrapper.client getConversationWithConversationId:conversationId completionHandler:^(BOOL status, int code, NSString *message, StringeeConversation *conversation) {
        if (!conversation) {
            callback(@[@(NO), @(-2), @"Conversation not found."]);
            return;
        }
        [conversation deleteWithCompletionHandler:^(BOOL status, int code, NSString *message) {
            callback(@[@(status), @(code), message]);
        }];
    }];
}

RCT_EXPORT_METHOD(addParticipants:(NSString *)uuid conversationId:(NSString *)conversationId userIds:(NSArray *)userIds callback:(RCTResponseSenderBlock)callback) {

    RNClientWrapper *wrapper = [RNStringeeInstanceManager.instance.clientWrappers objectForKey:uuid];
    if (wrapper == nil) {
        callback(@[@(NO), @(-1), @"Wrapper is not found", [NSNull null]]);
        return;
    }

    if (!wrapper.client || !wrapper.client.hasConnected) {
        callback(@[@(NO), @(-1), @"StringeeClient is not initialized or connected.", [NSNull null]]);
        return;
    }

    if (![userIds isKindOfClass:[NSArray class]] || !userIds.count) {
        callback(@[@(NO), @(-2), @"The participants is invalid.", [NSNull null]]);
        return;
    }

    NSMutableSet *users = [[NSMutableSet alloc] init];
    for (NSString *userId in userIds) {
        if (![userId isKindOfClass:[NSString class]]) {
            callback(@[@(NO), @(-2), @"The participants is invalid.", [NSNull null]]);
            return;
        }

        StringeeIdentity *iden = [StringeeIdentity new];
        iden.userId = userId;
        [users addObject:iden];
    }

    if (![conversationId isKindOfClass:[NSString class]] || !conversationId.length) {
        callback(@[@(NO), @(-3), @"Conversation not found.", [NSNull null]]);
        return;
    }

    [wrapper.client getConversationWithConversationId:conversationId completionHandler:^(BOOL status, int code, NSString *message, StringeeConversation *conversation) {
        if (!conversation) {
            callback(@[@(NO), @(-3), @"Conversation not found.", [NSNull null]]);
            return;
        }

        [conversation addParticipants:users completionHandler:^(BOOL status, int code, NSString *message, NSArray<StringeeIdentity *> *addedUsers) {
            callback(@[@(status), @(code), message, [RCTConvert StringeeIdentities:addedUsers]]);
        }];
    }];
}

RCT_EXPORT_METHOD(removeParticipants:(NSString *)uuid conversationId:(NSString *)conversationId userIds:(NSArray *)userIds callback:(RCTResponseSenderBlock)callback) {

    RNClientWrapper *wrapper = [RNStringeeInstanceManager.instance.clientWrappers objectForKey:uuid];
    if (wrapper == nil) {
        callback(@[@(NO), @(-1), @"Wrapper is not found", [NSNull null]]);
        return;
    }

    if (!wrapper.client || !wrapper.client.hasConnected) {
        callback(@[@(NO), @(-1), @"StringeeClient is not initialized or connected.", [NSNull null]]);
        return;
    }

    if (![userIds isKindOfClass:[NSArray class]] || !userIds.count) {
        callback(@[@(NO), @(-2), @"The participants is invalid.", [NSNull null]]);
        return;
    }

    NSMutableSet *users = [[NSMutableSet alloc] init];
    for (NSString *userId in userIds) {
        if (![userId isKindOfClass:[NSString class]]) {
            callback(@[@(NO), @(-2), @"The participants is invalid.", [NSNull null]]);
            return;
        }

        StringeeIdentity *iden = [StringeeIdentity new];
        iden.userId = userId;
        [users addObject:iden];
    }

    if (![conversationId isKindOfClass:[NSString class]] || !conversationId.length) {
        callback(@[@(NO), @(-3), @"Conversation not found.", [NSNull null]]);
        return;
    }

    [wrapper.client getConversationWithConversationId:conversationId completionHandler:^(BOOL status, int code, NSString *message, StringeeConversation *conversation) {
        if (!conversation) {
            callback(@[@(NO), @(-3), @"Conversation not found.", [NSNull null]]);
            return;
        }

        [conversation removeParticipants:users completionHandler:^(BOOL status, int code, NSString *message, NSArray<StringeeIdentity *> *removedUsers) {
            callback(@[@(status), @(code), message, [RCTConvert StringeeIdentities:removedUsers]]);
        }];
    }];
}

RCT_EXPORT_METHOD(updateConversation:(NSString *)uuid conversationId:(NSString *)conversationId params:(NSDictionary *)params callback:(RCTResponseSenderBlock)callback) {

    RNClientWrapper *wrapper = [RNStringeeInstanceManager.instance.clientWrappers objectForKey:uuid];
    if (wrapper == nil) {
        callback(@[@(NO), @(-1), @"Wrapper is not found", [NSNull null]]);
        return;
    }

    if (!wrapper.client || !wrapper.client.hasConnected) {
        callback(@[@(NO), @(-1), @"StringeeClient is not initialized or connected.", [NSNull null]]);
        return;
    }

    if (!params || ![params isKindOfClass:[NSDictionary class]]) {
        callback(@[@(NO), @(-2), @"Params are invalid."]);
        return;
    }

    NSString *name = params[@"name"];
    NSString *avatar = params[@"avatar"];

    if (![conversationId isKindOfClass:[NSString class]] || !conversationId.length || (![name isKindOfClass:[NSString class]] && ![avatar isKindOfClass:[NSString class]])) {
        callback(@[@(NO), @(-2), @"Params are invalid."]);
        return;
    }

    NSString *safeName = [name isKindOfClass:[NSString class]] ? name : nil;
    NSString *safeAvatar = [avatar isKindOfClass:[NSString class]] ? avatar : nil;

    [wrapper.client getConversationWithConversationId:conversationId completionHandler:^(BOOL status, int code, NSString *message, StringeeConversation *conversation) {
        if (!conversation) {
            callback(@[@(NO), @(-3), @"Conversation not found.", [NSNull null]]);
            return;
        }

        [conversation updateWithName:safeName strAvatarUrl:safeAvatar completionHandler:^(BOOL status, int code, NSString *message) {
            callback(@[@(status), @(code), message]);
        }];
    }];
}

RCT_EXPORT_METHOD(getConversationWithUser:(NSString *)uuid userId:(NSString *)userId callback:(RCTResponseSenderBlock)callback) {

    RNClientWrapper *wrapper = [RNStringeeInstanceManager.instance.clientWrappers objectForKey:uuid];
    if (wrapper == nil) {
        callback(@[@(NO), @(-1), @"Wrapper is not found", [NSNull null]]);
        return;
    }

    if (!wrapper.client || !wrapper.client.hasConnected) {
        callback(@[@(NO), @(-1), @"StringeeClient is not initialized or connected.", [NSNull null]]);
        return;
    }

    if (!userId || ![userId isKindOfClass:[NSString class]] || !userId.length || [userId isEqualToString:wrapper.client.userId]) {
        callback(@[@(NO), @(-2), @"UserId is invalid."]);
        return;
    }

    NSMutableSet *users = [[NSMutableSet alloc] init];
    StringeeIdentity *iden = [StringeeIdentity new];
    iden.userId = userId;
    [users addObject:iden];

    StringeeIdentity *meUser = [StringeeIdentity new];
    meUser.userId = wrapper.client.userId;
    [users addObject:meUser];

    [wrapper.client getConversationForUsers:users completionHandler:^(BOOL status, int code, NSString *message, NSArray<StringeeConversation *> *conversations) {
        if (!conversations) {
            callback(@[@(NO), @(-4), @"Conversation is not found.", [NSNull null]]);
            return;
        }

        if (conversations.count == 0) {
            callback(@[@(NO), @(-4), @"Conversation is not found.", [NSNull null]]);
            return;
        }

        for (StringeeConversation *conversation in conversations) {
            if (conversation.isGroup == false) {
                callback(@[@(status), @(code), message, [RCTConvert StringeeConversation:conversation]]);
                return;
            }
        }

    }];
}

RCT_EXPORT_METHOD(getUnreadConversationCount:(NSString *)uuid callback:(RCTResponseSenderBlock)callback) {
    RNClientWrapper *wrapper = [RNStringeeInstanceManager.instance.clientWrappers objectForKey:uuid];
    if (wrapper == nil) {
        callback(@[@(NO), @(-1), @"Wrapper is not found", @(0)]);
        return;
    }

    if (!wrapper.client || !wrapper.client.hasConnected) {
        callback(@[@(NO), @(-1), @"StringeeClient is not initialized or connected.", @(0)]);
        return;
    }

    [wrapper.client getUnreadConversationCountWithCompletionHandler:^(BOOL status, int code, NSString *message, int count) {
        callback(@[@(status), @(code), message, @(count)]);
    }];
}

RCT_EXPORT_METHOD(getLastUnreadConversations:(NSString *)uuid count:(NSUInteger)count callback:(RCTResponseSenderBlock)callback) {

    RNClientWrapper *wrapper = [RNStringeeInstanceManager.instance.clientWrappers objectForKey:uuid];
    if (wrapper == nil) {
        callback(@[@(NO), @(-1), @"Wrapper is not found", [NSNull null]]);
        return;
    }

    if (!wrapper.client || !wrapper.client.hasConnected) {
        callback(@[@(NO), @(-1), @"StringeeClient is not initialized or connected.", [NSNull null]]);
        return;
    }

    [wrapper.client getLastUnreadConversationsWithCount:count completion:^(BOOL status, int code, NSString *message, NSArray<StringeeConversation *> *conversations) {
        callback(@[@(status), @(code), message, [RCTConvert StringeeConversations:conversations]]);
    }];
}

RCT_EXPORT_METHOD(getUnreadConversationsAfter:(NSString *)uuid datetime:(NSUInteger)datetime count:(NSUInteger)count callback:(RCTResponseSenderBlock)callback) {

    RNClientWrapper *wrapper = [RNStringeeInstanceManager.instance.clientWrappers objectForKey:uuid];
    if (wrapper == nil) {
        callback(@[@(NO), @(-1), @"Wrapper is not found", [NSNull null]]);
        return;
    }

    if (!wrapper.client || !wrapper.client.hasConnected) {
        callback(@[@(NO), @(-1), @"StringeeClient is not initialized or connected.", [NSNull null]]);
        return;
    }

    [wrapper.client getUnreadConversationsAfter:datetime withCount:count completion:^(BOOL status, int code, NSString *message, NSArray<StringeeConversation *> *conversations) {
        callback(@[@(status), @(code), message, [RCTConvert StringeeConversations:conversations]]);
    }];
}

RCT_EXPORT_METHOD(getUnreadConversationsBefore:(NSString *)uuid datetime:(NSUInteger)datetime count:(NSUInteger)count callback:(RCTResponseSenderBlock)callback) {

    RNClientWrapper *wrapper = [RNStringeeInstanceManager.instance.clientWrappers objectForKey:uuid];
    if (wrapper == nil) {
        callback(@[@(NO), @(-1), @"Wrapper is not found", [NSNull null]]);
        return;
    }

    if (!wrapper.client || !wrapper.client.hasConnected) {
        callback(@[@(NO), @(-1), @"StringeeClient is not initialized or connected.", [NSNull null]]);
        return;
    }

    [wrapper.client getUnreadConversationsBefore:datetime withCount:count completion:^(BOOL status, int code, NSString *message, NSArray<StringeeConversation *> *conversations) {
        callback(@[@(status), @(code), message, [RCTConvert StringeeConversations:conversations]]);
    }];
}

#pragma mark - Message

RCT_EXPORT_METHOD(sendMessage:(NSString *)uuid message:(NSDictionary *)message callback:(RCTResponseSenderBlock)callback) {
    if (!message || ![message isKindOfClass:[NSDictionary class]]) {
        callback(@[@(NO), @(-2), @"Message is invalid."]);
        return;
    }

    id msg = message[@"message"];
    NSNumber *type = message[@"type"];
    id convId = message[@"convId"];

    if (![msg isKindOfClass:[NSDictionary class]] || ![convId isKindOfClass:[NSString class]] || ![type isKindOfClass:[NSNumber class]]) {
        callback(@[@(NO), @(-2), @"Message is invalid."]);
        return;
    }


    RNClientWrapper *wrapper = [RNStringeeInstanceManager.instance.clientWrappers objectForKey:uuid];
    if (wrapper == nil) {
        callback(@[@(NO), @(-1), @"Wrapper is not found"]);
        return;
    }

    if (!wrapper.client || !wrapper.client.hasConnected) {
        callback(@[@(NO), @(-1), @"StringeeClient is not initialized or connected."]);
        return;
    }

    __weak RNClientWrapper *weakWrapper = wrapper;

    // Lấy về conversation
    [wrapper.client getConversationWithConversationId:convId completionHandler:^(BOOL status, int code, NSString *message, StringeeConversation *conversation) {

        RNClientWrapper *strongWrapper = weakWrapper;

        if (!strongWrapper) {
            callback(@[@(NO), @(-3), @"Conversation not found."]);
            return;
        }

        if (!conversation) {
            callback(@[@(NO), @(-3), @"Conversation not found."]);
            return;
        }

        if (!strongWrapper.client || !strongWrapper.client.hasConnected) {
            callback(@[@(NO), @(-1), @"StringeeClient is not initialized or connected."]);
            return;
        }

        // Lay data tu message -> khoi tao msg tuong ung trong native
        StringeeMessage *msgToSend;
        NSDictionary *dicMsg = (NSDictionary *)msg;

        switch (type.intValue) {
            case StringeeMessageTypeText:
            {
                NSString *text = dicMsg[@"content"];
                if (![text isKindOfClass:[NSString class]] || !text.length) {
                    callback(@[@(NO), @(-2), @"Message is invalid."]);
                    return;
                }
                msgToSend = [[StringeeTextMessage alloc] initWithText:text metadata:nil];
            }
                break;

            case StringeeMessageTypePhoto:
            {
                NSDictionary *photoDic = dicMsg[@"photo"];

                NSString *filePath = photoDic[@"filePath"];
                NSString *thumbnail = photoDic[@"thumbnail"] != nil ? photoDic[@"thumbnail"] : @"";
                NSNumber *ratio = photoDic[@"ratio"] != nil ? photoDic[@"ratio"] : @(1);

                if (![filePath isKindOfClass:[NSString class]] || !filePath.length) {
                    callback(@[@(NO), @(-2), @"Message is invalid."]);
                    return;
                }

                msgToSend = [[StringeePhotoMessage alloc] initWithFileUrl:filePath thumbnailUrl:thumbnail ratio:ratio.floatValue metadata:nil];
            }
                break;

            case StringeeMessageTypeVideo:
            {
                NSDictionary *videoDic = dicMsg[@"video"];

                NSString *filePath = videoDic[@"filePath"];
                NSString *thumbnail = videoDic[@"thumbnail"] != nil ? videoDic[@"thumbnail"] : @"";
                NSNumber *ratio = videoDic[@"ratio"] != nil ? videoDic[@"ratio"] : @(1);
                NSNumber *duration = videoDic[@"duration"] != nil ? videoDic[@"duration"] : @(0);

                if (![filePath isKindOfClass:[NSString class]] || !filePath.length) {
                    callback(@[@(NO), @(-2), @"Message is invalid."]);
                    return;
                }
                msgToSend = [[StringeeVideoMessage alloc] initWithFileUrl:filePath thumbnailUrl:thumbnail ratio:ratio.floatValue duration:duration.doubleValue metadata:nil];
            }
                break;

            case StringeeMessageTypeAudio:
            {
                NSDictionary *audioDic = dicMsg[@"audio"];

                NSString *filePath = audioDic[@"filePath"];
                NSNumber *duration = audioDic[@"duration"] != nil ? audioDic[@"duration"] : @(0);

                if (![filePath isKindOfClass:[NSString class]] || !filePath.length) {
                    callback(@[@(NO), @(-2), @"Message is invalid."]);
                    return;
                }
                msgToSend = [[StringeeAudioMessage alloc] initWithFileUrl:filePath duration:duration.doubleValue metadata:nil];
            }
                break;

            case StringeeMessageTypeFile:
            {
                NSDictionary *fileDic = dicMsg[@"file"];

                NSString *filePath = fileDic[@"filePath"];
                NSString *filename = fileDic[@"filename"] != nil ? fileDic[@"filename"] : @"";
                NSNumber *length = fileDic[@"length"] != nil ? fileDic[@"length"] : @(0);

                if (![filePath isKindOfClass:[NSString class]] || !filePath.length) {
                    callback(@[@(NO), @(-2), @"Message is invalid."]);
                    return;
                }
                msgToSend = [[StringeeFileMessage alloc] initWithFileUrl:filePath fileName:filename length:length.longLongValue metadata:nil];
            }
                break;

            case StringeeMessageTypeLink:
            {
                NSString *text = dicMsg[@"content"];
                if (![text isKindOfClass:[NSString class]] || !text.length) {
                    callback(@[@(NO), @(-2), @"Message is invalid."]);
                    return;
                }
                msgToSend = [[StringeeTextMessage alloc] initWithLink:text metadata:nil];
            }
                break;

            case StringeeMessageTypeLocation:
            {
                NSDictionary *locationDic = dicMsg[@"location"];

                NSNumber *lat = locationDic[@"lat"];
                NSNumber *lon = locationDic[@"lon"];

                if (!lat || !lon) {
                    callback(@[@(NO), @(-2), @"Message is invalid."]);
                    return;
                }
                msgToSend = [[StringeeLocationMessage alloc] initWithlatitude:lat.doubleValue longitude:lon.doubleValue metadata:nil];
            }
                break;

            case StringeeMessageTypeContact:
            {
                NSString *vcard = dicMsg[@"contact"][@"vcard"];
                if (![vcard isKindOfClass:[NSString class]] || !vcard.length) {
                    callback(@[@(NO), @(-2), @"Message is invalid."]);
                    return;
                }
                msgToSend = [[StringeeContactMessage alloc] initWithVcard:vcard metadata:nil];
            }
                break;

            case StringeeMessageTypeSticker:
            {
                NSDictionary *stickerDic = dicMsg[@"sticker"];

                NSString *category = stickerDic[@"category"];
                NSString *name = stickerDic[@"name"];

                if (![category isKindOfClass:[NSString class]] || !category.length || ![name isKindOfClass:[NSString class]] || !name.length) {
                    callback(@[@(NO), @(-2), @"Message is invalid."]);
                    return;
                }
                msgToSend = [[StringeeStickerMessage alloc] initWithCategory:category name:name metadata:nil];
            }
                break;

            default:
                callback(@[@(NO), @(-2), @"Message is invalid."]);
                return;
        }

        NSError *error;
        [strongWrapper.messages setObject:msgToSend forKey:msgToSend.localIdentifier];

        [conversation sendMessageWithoutPretreatment:msgToSend error:&error];
        if (error) {
            callback(@[@(NO), @(1), @"Fail."]);
        } else {
            callback(@[@(YES), @(0), @"Success."]);
        }
    }];
}

RCT_EXPORT_METHOD(deleteMessage:(NSString *)uuid conversationId:(NSString *)conversationId msgId:(NSString *)msgId callback:(RCTResponseSenderBlock)callback) {

    RNClientWrapper *wrapper = [RNStringeeInstanceManager.instance.clientWrappers objectForKey:uuid];
    if (wrapper == nil) {
        callback(@[@(NO), @(-1), @"Wrapper is not found"]);
        return;
    }

    if (!wrapper.client || !wrapper.client.hasConnected) {
        callback(@[@(NO), @(-1), @"StringeeClient is not initialized or connected."]);
        return;
    }

    if (!msgId.length) {
        callback(@[@(NO), @(-2), @"Message's id is invalid."]);
        return;
    }

    // Lấy về conversation
    [wrapper.client getConversationWithConversationId:conversationId completionHandler:^(BOOL status, int code, NSString *message, StringeeConversation *conversation) {
        if (!conversation) {
            callback(@[@(NO), @(-3), @"Conversation not found."]);
            return;
        }

        [conversation deleteMessageWithMessageIds:@[msgId] withCompletionHandler:^(BOOL status, int code, NSString *message) {
            callback(@[@(status), @(code), message]);
        }];
    }];
}

RCT_EXPORT_METHOD(getLocalMessages:(NSString *)uuid conversationId:(NSString *)conversationId count:(NSUInteger)count callback:(RCTResponseSenderBlock)callback) {

    RNClientWrapper *wrapper = [RNStringeeInstanceManager.instance.clientWrappers objectForKey:uuid];
    if (wrapper == nil) {
        callback(@[@(NO), @(-1), @"Wrapper is not found"]);
        return;
    }

    if (!wrapper.client || !wrapper.client.hasConnected) {
        callback(@[@(NO), @(-1), @"StringeeClient is not initialized or connected."]);
        return;
    }

    // Lấy về conversation
    [wrapper.client getConversationWithConversationId:conversationId completionHandler:^(BOOL status, int code, NSString *message, StringeeConversation *conversation) {
        if (!conversation) {
            callback(@[@(NO), @(-3), @"Conversation not found.", [NSNull null]]);
            return;
        }

        [conversation getLocalMessagesWithCount:count completionHandler:^(BOOL status, int code, NSString *message, NSArray<StringeeMessage *> *messages) {
            callback(@[@(status), @(code), message, [RCTConvert StringeeMessages:[[messages reverseObjectEnumerator] allObjects]]]);
        }];
    }];
}

RCT_EXPORT_METHOD(getLastMessages:(NSString *)uuid conversationId:(NSString *)conversationId count:(NSUInteger)count loadDeletedMessage:(BOOL)loadDeletedMessage loadDeletedMessageContent:(BOOL)loadDeletedMessageContent callback:(RCTResponseSenderBlock)callback) {

    RNClientWrapper *wrapper = [RNStringeeInstanceManager.instance.clientWrappers objectForKey:uuid];
    if (wrapper == nil) {
        callback(@[@(NO), @(-1), @"Wrapper is not found"]);
        return;
    }

    if (!wrapper.client || !wrapper.client.hasConnected) {
        callback(@[@(NO), @(-1), @"StringeeClient is not initialized or connected."]);
        return;
    }

    // Lấy về conversation
    [wrapper.client getConversationWithConversationId:conversationId completionHandler:^(BOOL status, int code, NSString *message, StringeeConversation *conversation) {
        if (!conversation) {
            callback(@[@(NO), @(-3), @"Conversation not found.", [NSNull null]]);
            return;
        }

        [conversation getLastMessagesWithCount:count loadDeletedMessage:loadDeletedMessage loadDeletedMessageContent:loadDeletedMessageContent completionHandler:^(BOOL status, int code, NSString *message, NSArray<StringeeMessage *> *messages) {
            callback(@[@(status), @(code), message, [RCTConvert StringeeMessages:[[messages reverseObjectEnumerator] allObjects]]]);
        }];
    }];
}

RCT_EXPORT_METHOD(getAllLastMessages:(NSString *)uuid conversationId:(NSString *)conversationId count:(NSUInteger)count loadDeletedMessage:(BOOL)loadDeletedMessage loadDeletedMessageContent:(BOOL)loadDeletedMessageContent callback:(RCTResponseSenderBlock)callback) {

    RNClientWrapper *wrapper = [RNStringeeInstanceManager.instance.clientWrappers objectForKey:uuid];
    if (wrapper == nil) {
        callback(@[@(NO), @(-1), @"Wrapper is not found"]);
        return;
    }

    if (!wrapper.client || !wrapper.client.hasConnected) {
        callback(@[@(NO), @(-1), @"StringeeClient is not initialized or connected."]);
        return;
    }

    // Lấy về conversation
    [wrapper.client getConversationWithConversationId:conversationId completionHandler:^(BOOL status, int code, NSString *message, StringeeConversation *conversation) {
        if (!conversation) {
            callback(@[@(NO), @(-3), @"Conversation not found.", [NSNull null]]);
            return;
        }

        [conversation getLastMessagesWithCount:count loadDeletedMessage:loadDeletedMessage loadDeletedMessageContent:loadDeletedMessageContent loadHistory:true completionHandler:^(BOOL status, int code, NSString *message, NSArray<StringeeMessage *> *messages) {
            callback(@[@(status), @(code), message, [RCTConvert StringeeMessages:[[messages reverseObjectEnumerator] allObjects]]]);
        }];
    }];
}

RCT_EXPORT_METHOD(getMessagesAfter:(NSString *)uuid conversationId:(NSString *)conversationId sequence:(NSUInteger)sequence count:(NSUInteger)count loadDeletedMessage:(BOOL)loadDeletedMessage loadDeletedMessageContent:(BOOL)loadDeletedMessageContent callback:(RCTResponseSenderBlock)callback) {

    RNClientWrapper *wrapper = [RNStringeeInstanceManager.instance.clientWrappers objectForKey:uuid];
    if (wrapper == nil) {
        callback(@[@(NO), @(-1), @"Wrapper is not found"]);
        return;
    }

    if (!wrapper.client || !wrapper.client.hasConnected) {
        callback(@[@(NO), @(-1), @"StringeeClient is not initialized or connected."]);
        return;
    }

    // Lấy về conversation
    [wrapper.client getConversationWithConversationId:conversationId completionHandler:^(BOOL status, int code, NSString *message, StringeeConversation *conversation) {
        if (!conversation) {
            callback(@[@(NO), @(-3), @"Conversation not found.", [NSNull null]]);
            return;
        }

        [conversation getMessagesAfter:sequence withCount:count loadDeletedMessage:loadDeletedMessage loadDeletedMessageContent:loadDeletedMessageContent completionHandler:^(BOOL status, int code, NSString *message, NSArray<StringeeMessage *> *messages) {
            callback(@[@(status), @(code), message, [RCTConvert StringeeMessages:messages]]);
        }];
    }];
}

RCT_EXPORT_METHOD(getAllMessagesAfter:(NSString *)uuid conversationId:(NSString *)conversationId sequence:(NSUInteger)sequence count:(NSUInteger)count loadDeletedMessage:(BOOL)loadDeletedMessage loadDeletedMessageContent:(BOOL)loadDeletedMessageContent callback:(RCTResponseSenderBlock)callback) {

    RNClientWrapper *wrapper = [RNStringeeInstanceManager.instance.clientWrappers objectForKey:uuid];
    if (wrapper == nil) {
        callback(@[@(NO), @(-1), @"Wrapper is not found"]);
        return;
    }

    if (!wrapper.client || !wrapper.client.hasConnected) {
        callback(@[@(NO), @(-1), @"StringeeClient is not initialized or connected."]);
        return;
    }

    // Lấy về conversation
    [wrapper.client getConversationWithConversationId:conversationId completionHandler:^(BOOL status, int code, NSString *message, StringeeConversation *conversation) {
        if (!conversation) {
            callback(@[@(NO), @(-3), @"Conversation not found.", [NSNull null]]);
            return;
        }

        [conversation getMessagesAfter:sequence withCount:count loadDeletedMessage:loadDeletedMessage loadDeletedMessageContent:loadDeletedMessageContent loadHistory:true completionHandler:^(BOOL status, int code, NSString *message, NSArray<StringeeMessage *> *messages) {
            callback(@[@(status), @(code), message, [RCTConvert StringeeMessages:messages]]);
        }];
    }];
}

RCT_EXPORT_METHOD(getMessagesBefore:(NSString *)uuid conversationId:(NSString *)conversationId sequence:(NSUInteger)sequence count:(NSUInteger)count loadDeletedMessage:(BOOL)loadDeletedMessage loadDeletedMessageContent:(BOOL)loadDeletedMessageContent callback:(RCTResponseSenderBlock)callback) {

    RNClientWrapper *wrapper = [RNStringeeInstanceManager.instance.clientWrappers objectForKey:uuid];
    if (wrapper == nil) {
        callback(@[@(NO), @(-1), @"Wrapper is not found"]);
        return;
    }

    if (!wrapper.client || !wrapper.client.hasConnected) {
        callback(@[@(NO), @(-1), @"StringeeClient is not initialized or connected."]);
        return;
    }

    // Lấy về conversation
    [wrapper.client getConversationWithConversationId:conversationId completionHandler:^(BOOL status, int code, NSString *message, StringeeConversation *conversation) {
        if (!conversation) {
            callback(@[@(NO), @(-3), @"Conversation not found.", [NSNull null]]);
            return;
        }

        [conversation getMessagesBefore:sequence withCount:count loadDeletedMessage:loadDeletedMessage loadDeletedMessageContent:loadDeletedMessageContent completionHandler:^(BOOL status, int code, NSString *message, NSArray<StringeeMessage *> *messages) {
            callback(@[@(status), @(code), message, [RCTConvert StringeeMessages:[[messages reverseObjectEnumerator] allObjects]]]);
        }];
    }];
}

RCT_EXPORT_METHOD(getAllMessagesBefore:(NSString *)uuid conversationId:(NSString *)conversationId sequence:(NSUInteger)sequence count:(NSUInteger)count loadDeletedMessage:(BOOL)loadDeletedMessage loadDeletedMessageContent:(BOOL)loadDeletedMessageContent callback:(RCTResponseSenderBlock)callback) {

    RNClientWrapper *wrapper = [RNStringeeInstanceManager.instance.clientWrappers objectForKey:uuid];
    if (wrapper == nil) {
        callback(@[@(NO), @(-1), @"Wrapper is not found"]);
        return;
    }

    if (!wrapper.client || !wrapper.client.hasConnected) {
        callback(@[@(NO), @(-1), @"StringeeClient is not initialized or connected."]);
        return;
    }

    // Lấy về conversation
    [wrapper.client getConversationWithConversationId:conversationId completionHandler:^(BOOL status, int code, NSString *message, StringeeConversation *conversation) {
        if (!conversation) {
            callback(@[@(NO), @(-3), @"Conversation not found.", [NSNull null]]);
            return;
        }

        [conversation getMessagesBefore:sequence withCount:count loadDeletedMessage:loadDeletedMessage loadDeletedMessageContent:loadDeletedMessageContent loadHistory:true completionHandler:^(BOOL status, int code, NSString *message, NSArray<StringeeMessage *> *messages) {
            callback(@[@(status), @(code), message, [RCTConvert StringeeMessages:[[messages reverseObjectEnumerator] allObjects]]]);
        }];
    }];
}

RCT_EXPORT_METHOD(markConversationAsRead:(NSString *)uuid conversationId:(NSString *)conversationId callback:(RCTResponseSenderBlock)callback) {

    RNClientWrapper *wrapper = [RNStringeeInstanceManager.instance.clientWrappers objectForKey:uuid];
    if (wrapper == nil) {
        callback(@[@(NO), @(-1), @"Wrapper is not found"]);
        return;
    }

    if (!wrapper.client || !wrapper.client.hasConnected) {
        callback(@[@(NO), @(-1), @"StringeeClient is not initialized or connected."]);
        return;
    }

    // Lấy về conversation
    [wrapper.client getConversationWithConversationId:conversationId completionHandler:^(BOOL status, int code, NSString *message, StringeeConversation *conversation) {
        if (!conversation) {
            callback(@[@(NO), @(-3), @"Conversation not found."]);
            return;
        }

        [conversation markAllMessagesAsSeenWithCompletionHandler:^(BOOL status, int code, NSString *message) {
            callback(@[@(status), @(code), message]);
        }];
    }];
}

#pragma mark - ClearData

RCT_EXPORT_METHOD(clearDb:(NSString *)uuid callback:(RCTResponseSenderBlock)callback) {

    RNClientWrapper *wrapper = [RNStringeeInstanceManager.instance.clientWrappers objectForKey:uuid];
    if (wrapper == nil) {
        callback(@[@(NO), @(-1), @"Wrapper is not found"]);
        return;
    }

    if (!wrapper.client) {
        callback(@[@(NO), @(-1), @"StringeeClient is not initialized"]);
        return;
    }

    [wrapper.client clearData];
    callback(@[@(YES), @(0), @"Success."]);
}

#pragma mark - Live-Chat

RCT_EXPORT_METHOD(getChatProfile:(NSString *)uuid widgetKey:(NSString *)widgetKey callback:(RCTResponseSenderBlock)callback) {
    RNClientWrapper *wrapper = [RNStringeeInstanceManager.instance.clientWrappers objectForKey:uuid];
    if (wrapper == nil) {
        callback(@[@(NO), @(-1), @"Wrapper is not found"]);
        return;
    }

    if (!wrapper.client) {
        callback(@[@(NO), @(-1), @"StringeeClient is not initialized"]);
        return;
    }

    if (![RCTConvert isValid:widgetKey]) {
        callback(@[@(NO), @(-2), @"WidgetKey invalid"]);
        return;
    }

    [wrapper.client getChatProfileWithKey:widgetKey completion:^(BOOL status, int code, NSString *message, StringeeChatProfile *chatProfile) {
        callback(@[@(status), @(code), message, [RCTConvert SXChatProfile:chatProfile]]);
    }];
}

RCT_EXPORT_METHOD(getLiveChatToken:(NSString *)uuid widgetKey:(NSString *)widgetKey name:(NSString *)name email:(NSString *)email callback:(RCTResponseSenderBlock)callback) {
    RNClientWrapper *wrapper = [RNStringeeInstanceManager.instance.clientWrappers objectForKey:uuid];
    if (wrapper == nil) {
        callback(@[@(NO), @(-1), @"Wrapper is not found"]);
        return;
    }

    if (!wrapper.client) {
        callback(@[@(NO), @(-1), @"StringeeClient is not initialized"]);
        return;
    }

    if (![RCTConvert isValid:widgetKey] || ![RCTConvert isValid:name] || ![RCTConvert isValid:email] || ![RCTConvert isValidEmail:email]) {
        callback(@[@(NO), @(-2), @"Parameters invalid"]);
        return;
    }

    [wrapper.client generateTokenForCustomerWithKey:widgetKey username:name email:email completion:^(BOOL status, int code, NSString *message, NSString *token) {
        if (status) {
            callback(@[@(status), @(code), message, token]);
        } else {
            callback(@[@(status), @(code), message, [NSNull null]]);
        }
    }];
}

RCT_EXPORT_METHOD(updateUserInfo:(NSString *)uuid name:(NSString *)name email:(NSString *)email avatar:(NSString *)avatar phone:(NSString *)phone callback:(RCTResponseSenderBlock)callback) {
    RNClientWrapper *wrapper = [RNStringeeInstanceManager.instance.clientWrappers objectForKey:uuid];
    if (wrapper == nil) {
        callback(@[@(NO), @(-1), @"Wrapper is not found"]);
        return;
    }

    if (!wrapper.client) {
        callback(@[@(NO), @(-1), @"StringeeClient is not initialized"]);
        return;
    }

    if (![RCTConvert isValid:name]) {
        callback(@[@(NO), @(-2), @"Parameters invalid"]);
        return;
    }
    
    if (phone != nil && phone.length > 0) {
        [wrapper.client updateUserInfoWithUsername:name email:email avatar:avatar phone:phone completion:^(BOOL status, int code, NSString *message) {
            callback(@[@(status), @(code), message]);
        }];
    } else {
        [wrapper.client updateUserInfoWithUsername:name email:email avatar:avatar completion:^(BOOL status, int code, NSString *message) {
            callback(@[@(status), @(code), message]);
        }];
    }
}

RCT_EXPORT_METHOD(createLiveChatConversation:(NSString *)uuid queueId:(NSString *)queueId callback:(RCTResponseSenderBlock)callback) {
    RNClientWrapper *wrapper = [RNStringeeInstanceManager.instance.clientWrappers objectForKey:uuid];
    if (wrapper == nil) {
        callback(@[@(NO), @(-1), @"Wrapper is not found"]);
        return;
    }

    if (!wrapper.client) {
        callback(@[@(NO), @(-1), @"StringeeClient is not initialized"]);
        return;
    }

    if (![RCTConvert isValid:queueId]) {
        callback(@[@(NO), @(-2), @"QueueId invalid"]);
        return;
    }

    [wrapper.client createLiveChatConversationWithQueueId:queueId completion:^(BOOL status, int code, NSString *message, StringeeConversation *conversation) {
        callback(@[@(status), @(code), message, [RCTConvert StringeeConversation:conversation]]);
    }];
}

RCT_EXPORT_METHOD(sendChatTranscript:(NSString *)uuid email:(NSString *)email convId:(NSString *)convId domain:(NSString *)domain callback:(RCTResponseSenderBlock)callback) {
    RNClientWrapper *wrapper = [RNStringeeInstanceManager.instance.clientWrappers objectForKey:uuid];
    if (wrapper == nil) {
        callback(@[@(NO), @(-1), @"Wrapper is not found"]);
        return;
    }

    if (!wrapper.client) {
        callback(@[@(NO), @(-1), @"StringeeClient is not initialized"]);
        return;
    }

    if (![RCTConvert isValid:convId] || ![RCTConvert isValid:domain] || ![RCTConvert isValid:email] || ![RCTConvert isValidEmail:email]) {
        callback(@[@(NO), @(-2), @"Parameters invalid"]);
        return;
    }

    [wrapper.client sendChatTranscriptTo:email convId:convId domain:domain completion:^(BOOL status, int code, NSString *message) {
        callback(@[@(status), @(code), message]);
    }];
}

RCT_EXPORT_METHOD(endChat:(NSString *)uuid convId:(NSString *)convId callback:(RCTResponseSenderBlock)callback) {
    RNClientWrapper *wrapper = [RNStringeeInstanceManager.instance.clientWrappers objectForKey:uuid];
    if (wrapper == nil) {
        callback(@[@(NO), @(-1), @"Wrapper is not found"]);
        return;
    }

    if (!wrapper.client) {
        callback(@[@(NO), @(-1), @"StringeeClient is not initialized"]);
        return;
    }

    if (![RCTConvert isValid:convId]) {
        callback(@[@(NO), @(-2), @"convId invalid"]);
        return;
    }

    [wrapper.client endChatSupportWithConvId:convId completion:^(BOOL status, int code, NSString *message) {
        callback(@[@(status), @(code), message]);
    }];
}

RCT_EXPORT_METHOD(createTicketForMissedChat:(NSString *)uuid widgetKey:(NSString *)widgetKey name:(NSString *)name email:(NSString *)email phone:(NSString *)phone note:(NSString *)note callback:(RCTResponseSenderBlock)callback) {
    RNClientWrapper *wrapper = [RNStringeeInstanceManager.instance.clientWrappers objectForKey:uuid];
    if (wrapper == nil) {
        callback(@[@(NO), @(-1), @"Wrapper is not found"]);
        return;
    }

    if (!wrapper.client) {
        callback(@[@(NO), @(-1), @"StringeeClient is not initialized"]);
        return;
    }

    if (![RCTConvert isValid:widgetKey] || ![RCTConvert isValid:name]) {
        callback(@[@(NO), @(-2), @"Parameters invalid"]);
        return;
    }

    if (phone != nil && phone.length > 0) {
        [wrapper.client createTicketForMissChatWithKey:widgetKey username:name email:email phone:phone note:note completion:^(BOOL status, int code, NSString *message) {
            callback(@[@(status), @(code), message]);
        }];
    } else {
        [wrapper.client createTicketForMissChatWithKey:widgetKey username:name email:email note:note completion:^(BOOL status, int code, NSString *message) {
            callback(@[@(status), @(code), message]);
        }];
    }
}

RCT_EXPORT_METHOD(acceptChatRequest:(NSString *)uuid convId:(NSString *)convId callback:(RCTResponseSenderBlock)callback) {
    RNClientWrapper *wrapper = [RNStringeeInstanceManager.instance.clientWrappers objectForKey:uuid];
    if (wrapper == nil) {
        callback(@[@(NO), @(-1), @"Wrapper is not found"]);
        return;
    }

    if (!wrapper.client) {
        callback(@[@(NO), @(-1), @"StringeeClient is not initialized"]);
        return;
    }

    if (![RCTConvert isValid:convId]) {
        callback(@[@(NO), @(-2), @"ConvId invalid"]);
        return;
    }

    StringeeChatRequest *request = [wrapper.client getChatRequestWithConvId:convId];
    if (request == nil) {
        callback(@[@(NO), @(-3), @"Request not found"]);
        return;
    }
    
    [request acceptWithCompletionHandler:^(BOOL status, int code, NSString * _Nonnull message) {
        callback(@[@(status), @(code), message]);
    }];
}

RCT_EXPORT_METHOD(rejectChatRequest:(NSString *)uuid convId:(NSString *)convId callback:(RCTResponseSenderBlock)callback) {
    RNClientWrapper *wrapper = [RNStringeeInstanceManager.instance.clientWrappers objectForKey:uuid];
    if (wrapper == nil) {
        callback(@[@(NO), @(-1), @"Wrapper is not found"]);
        return;
    }

    if (!wrapper.client) {
        callback(@[@(NO), @(-1), @"StringeeClient is not initialized"]);
        return;
    }

    if (![RCTConvert isValid:convId]) {
        callback(@[@(NO), @(-2), @"ConvId invalid"]);
        return;
    }

    StringeeChatRequest *request = [wrapper.client getChatRequestWithConvId:convId];
    if (request == nil) {
        callback(@[@(NO), @(-3), @"Request not found"]);
        return;
    }
    
    [request rejectWithCompletionHandler:^(BOOL status, int code, NSString * _Nonnull message) {
        callback(@[@(status), @(code), message]);
    }];
}



RCT_EXPORT_METHOD(acceptTransferChatRequest:(NSString *)uuid convId:(NSString *)convId callback:(RCTResponseSenderBlock)callback) {
    RNClientWrapper *wrapper = [RNStringeeInstanceManager.instance.clientWrappers objectForKey:uuid];
    if (wrapper == nil) {
        callback(@[@(NO), @(-1), @"Wrapper is not found"]);
        return;
    }

    if (!wrapper.client) {
        callback(@[@(NO), @(-1), @"StringeeClient is not initialized"]);
        return;
    }

    if (![RCTConvert isValid:convId]) {
        callback(@[@(NO), @(-2), @"ConvId invalid"]);
        return;
    }

    StringeeChatRequest *request = [wrapper.client getChatRequestWithConvId:convId];
    if (request == nil) {
        callback(@[@(NO), @(-3), @"Request not found"]);
        return;
    }
    
    [request acceptWithCompletionHandler:^(BOOL status, int code, NSString * _Nonnull message) {
        callback(@[@(status), @(code), message]);
    }];
}

RCT_EXPORT_METHOD(rejectTransferChatRequest:(NSString *)uuid convId:(NSString *)convId callback:(RCTResponseSenderBlock)callback) {
    RNClientWrapper *wrapper = [RNStringeeInstanceManager.instance.clientWrappers objectForKey:uuid];
    if (wrapper == nil) {
        callback(@[@(NO), @(-1), @"Wrapper is not found"]);
        return;
    }

    if (!wrapper.client) {
        callback(@[@(NO), @(-1), @"StringeeClient is not initialized"]);
        return;
    }

    if (![RCTConvert isValid:convId]) {
        callback(@[@(NO), @(-2), @"ConvId invalid"]);
        return;
    }

    StringeeChatRequest *request = [wrapper.client getChatRequestWithConvId:convId];
    if (request == nil) {
        callback(@[@(NO), @(-3), @"Request not found"]);
        return;
    }
    
    [request rejectWithCompletionHandler:^(BOOL status, int code, NSString * _Nonnull message) {
        callback(@[@(status), @(code), message]);
    }];
}

RCT_EXPORT_METHOD(createLiveChatTicket:(NSString *)uuid widgetKey:(NSString *)widgetKey name:(NSString *)name email:(NSString *)email phone:(NSString *)phone note:(NSString *)note callback:(RCTResponseSenderBlock)callback) {
    RNClientWrapper *wrapper = [RNStringeeInstanceManager.instance.clientWrappers objectForKey:uuid];
    if (wrapper == nil) {
        callback(@[@(NO), @(-1), @"Wrapper is not found"]);
        return;
    }

    if (!wrapper.client) {
        callback(@[@(NO), @(-1), @"StringeeClient is not initialized"]);
        return;
    }

    if (![RCTConvert isValid:widgetKey] || ![RCTConvert isValid:name]) {
        callback(@[@(NO), @(-2), @"Parameters invalid"]);
        return;
    }

    NSString *checkedNote = note != nil ? note : @"";
    
    if (phone != nil && phone.length > 0) {
        [wrapper.client createTicketForMissChatWithKey:widgetKey username:name email:email phone:phone note:checkedNote completion:^(BOOL status, int code, NSString *message) {
            callback(@[@(status), @(code), message]);
        }];
    } else {
        [wrapper.client createTicketForMissChatWithKey:widgetKey username:name email:email note:checkedNote completion:^(BOOL status, int code, NSString *message) {
            callback(@[@(status), @(code), message]);
        }];
    }
    

    
}

@end
