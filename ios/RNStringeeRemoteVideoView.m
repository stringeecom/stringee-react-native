
#import "RNStringeeRemoteVideoView.h"
#import "RNStringeeInstanceManager.h"
#import <React/RCTLog.h>

@implementation RNStringeeRemoteVideoView

- (void)didMoveToWindow {
    [super didMoveToSuperview];
    [[RNStringeeInstanceManager instance].rnCall addRemoteView:self callId:_callId];
}

- (void)videoView:(StringeeRemoteVideoView *)videoView didChangeVideoSize:(CGSize)size {

    // Thay đổi frame của StringeeRemoteVideoView khi kích thước video thay đổi
    CGFloat superWidth = self.bounds.size.width;
    CGFloat superHeight = self.bounds.size.height;
    
    CGFloat newWidth;
    CGFloat newHeight;
    
    if (size.width > size.height) {
        newWidth = superWidth;
        newHeight = newWidth * size.height / size.width;
        
        [videoView setFrame:CGRectMake(0, (superHeight - newHeight) / 2, newWidth, newHeight)];
        
    } else {
        newHeight = superHeight;
        newWidth = newHeight * size.width / size.height;
        
        [videoView setFrame:CGRectMake((superWidth - newWidth) / 2, 0, newWidth, newHeight)];
    }
}

@end
