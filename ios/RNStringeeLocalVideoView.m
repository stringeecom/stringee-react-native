
#import "RNStringeeLocalVideoView.h"
#import "RNStringeeInstanceManager.h"
#import <React/RCTLog.h>

@implementation RNStringeeLocalVideoView

- (void)didMoveToWindow {
    [super didMoveToSuperview];
    [[RNStringeeInstanceManager instance].rnCall addLocalView:self callId:_callId];
}

@end
