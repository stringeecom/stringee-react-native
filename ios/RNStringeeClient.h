#if __has_include(<React/RCTBridgeModule.h>)
#import <React/RCTBridgeModule.h>
#elif __has_include("RCTBridgeModule.h")
#import “RCTBridgeModule.h”
#else
#import "React/RCTBridgeModule.h"
#endif

#if __has_include(<React/RCTEventEmitter.h>)
#import <React/RCTEventEmitter.h>
#elif __has_include("RCTEventEmitter.h")
#import "RCTEventEmitter"
#else
#import "React/RCTEventEmitter.h"
#endif

#import <Stringee/Stringee.h>

// Connect
static NSString *didConnect               = @"didConnect";
static NSString *didDisConnect            = @"didDisConnect";
static NSString *didFailWithError         = @"didFailWithError";
static NSString *requestAccessToken       = @"requestAccessToken";

// Call 1-1
static NSString *incomingCall               = @"incomingCall";
static NSString *incomingCall2              = @"incomingCall2";
static NSString *didReceiveCustomMessage    = @"didReceiveCustomMessage";

// Chat
static NSString *objectChangeNotification   = @"objectChangeNotification";

// Live-chat
static NSString *didReceiveChatRequest          = @"didReceiveChatRequest";
static NSString *didReceiveTransferChatRequest  = @"didReceiveTransferChatRequest";
static NSString *chatRequestTimeout             = @"chatRequestTimeout";
static NSString *didEndChatSupport              = @"didEndChatSupport";

@interface RNStringeeClient : RCTEventEmitter <RCTBridgeModule, StringeeConnectionDelegate, StringeeIncomingCallDelegate>

//@property (strong, nonatomic) StringeeClient *client;
//@property (strong, nonatomic) NSMutableDictionary *messages;

@end
