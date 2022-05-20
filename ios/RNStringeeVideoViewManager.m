#if __has_include(<React/RCTBridge.h>)
#import <React/RCTBridge.h>
#elif __has_include("RCTBridge.h")
#import "RCTBridge.h"
#else
#import "React/RCTBridge.h"
#endif

#import "RNStringeeVideoViewManager.h"

@implementation RNStringeeVideoViewManager

@synthesize bridge = _bridge;

RCT_EXPORT_MODULE();

RCT_EXPORT_VIEW_PROPERTY(local, BOOL)
RCT_EXPORT_VIEW_PROPERTY(streamId, NSString)
RCT_EXPORT_VIEW_PROPERTY(callId, NSString)

- (UIView *)view {
    // Init native View that will be converted to react-native view
    RNStringeeVideoView *videoView = [[RNStringeeVideoView alloc] init];
    return videoView;
}

@end
