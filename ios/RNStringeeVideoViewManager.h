#if __has_include(<React/RCTViewManager.h>)
#import <React/RCTViewManager.h>
#elif __has_include("RCTViewManager.h")
#import "RCTViewManager.h"
#else
#import "React/RCTViewManager.h"
#endif

#import "RNStringeeVideoView.h"

@interface RNStringeeVideoViewManager : RCTViewManager

@end
