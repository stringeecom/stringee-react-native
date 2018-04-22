
#import "RNStringeeRemoteVideoViewManager.h"

#if __has_include(<React/RCTBridge.h>)
#import <React/RCTBridge.h>
#elif __has_include("RCTBridge.h")
#import "RCTBridge.h"
#else
#import "React/RCTBridge.h"
#endif

#import <React/RCTLog.h>

#import "RNStringeeClient.h"

@implementation RNStringeeRemoteVideoViewManager

@synthesize bridge = _bridge;

RCT_EXPORT_MODULE();

RCT_EXPORT_VIEW_PROPERTY(streamId, NSString)
RCT_EXPORT_VIEW_PROPERTY(callId, NSString)

- (UIView *)view {
  RNStringeeRemoteVideoView *remoteView = [[RNStringeeRemoteVideoView alloc] init];
  return remoteView;
}

@end
