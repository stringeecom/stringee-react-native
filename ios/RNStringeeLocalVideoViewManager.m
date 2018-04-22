
#import "RNStringeeLocalVideoViewManager.h"

#if __has_include(<React/RCTBridge.h>)
#import <React/RCTBridge.h>
#elif __has_include("RCTBridge.h")
#import "RCTBridge.h"
#else
#import "React/RCTBridge.h"
#endif

#import <React/RCTLog.h>

@implementation RNStringeeLocalVideoViewManager

@synthesize bridge = _bridge;

RCT_EXPORT_MODULE();

- (UIView *)view {
  RNStringeeLocalVideoView *localView = [[RNStringeeLocalVideoView alloc] init];
  return localView;
}

RCT_EXPORT_VIEW_PROPERTY(streamId, NSString)

RCT_EXPORT_VIEW_PROPERTY(callId, NSString)

@end
