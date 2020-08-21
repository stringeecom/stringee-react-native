
#import "RNStringeeVideoView.h"
#import "RNStringeeInstanceManager.h"
#import <React/RCTLog.h>

@implementation RNStringeeVideoView {
    BOOL hasDisplayed;
}

- (instancetype)init
{
    self = [super init];
    if (self) {
        [self addObserver:self forKeyPath:@"bounds" options:0 context:nil];
        [self addObserver:self forKeyPath:@"frame" options:0 context:nil];
        self.videoSize = CGSizeZero;
    }
    return self;
}

- (void)dealloc
{
    [self removeObserver:self forKeyPath:@"bounds"];
    [self removeObserver:self forKeyPath:@"frame"];
}

- (void)layoutSubviews {
    [super layoutSubviews];

    if (!hasDisplayed) {
        if (_callId.length) {
            [[RNStringeeInstanceManager instance].rnCall addRenderToView:self callId:_callId isLocal:_local];
            hasDisplayed = YES;
        } else {
            if (_local) {
                [[RNStringeeInstanceManager instance].rnRoom addRenderToView:self streamId:_streamId isLocal:_local];
                hasDisplayed = YES;
            } else {
                if (_streamId.length) {
                    [[RNStringeeInstanceManager instance].rnRoom addRenderToView:self streamId:_streamId isLocal:_local];
                    hasDisplayed = YES;
                }
            }
        }
    }
}

- (void)observeValueForKeyPath:(NSString *)keyPath ofObject:(id)object change:(NSDictionary *)change context:(void *)context
{
    if (object == self) {
        if (([keyPath isEqualToString:@"bounds"] || [keyPath isEqualToString:@"frame"])) {
            if (!CGSizeEqualToSize(self.videoSize, CGSizeZero)) {
                if (self.subviews != nil && self.subviews.count > 0 && self.subviews[0] != nil) {
                    UIView *subView = (UIView *)self.subviews[0];
                    [self updateFrameToFitVideoSize:self.videoSize subView:subView superView:self];
                }
            }
        }
    }
}

- (void)videoView:(StringeeRemoteVideoView *)videoView didChangeVideoSize:(CGSize)size {
    // Thay đổi frame của StringeeRemoteVideoView khi kích thước video thay đổi
    self.videoSize = size;
    [self updateFrameToFitVideoSize:size subView:videoView superView:self];
    
//    CGFloat superWidth = self.bounds.size.width;
//    CGFloat superHeight = self.bounds.size.height;
//
//    CGFloat newWidth;
//    CGFloat newHeight;
//
//    if (size.width > size.height) {
//        newWidth = superWidth;
//        newHeight = newWidth * size.height / size.width;
//
//        [videoView setFrame:CGRectMake(0, (superHeight - newHeight) / 2, newWidth, newHeight)];
//
//    } else {
//        newHeight = superHeight;
//        newWidth = newHeight * size.width / size.height;
//
//        [videoView setFrame:CGRectMake((superWidth - newWidth) / 2, 0, newWidth, newHeight)];
//    }
}

- (void)updateFrameToFitVideoSize:(CGSize)size subView:(UIView *)subView superView:(UIView *)superView {
    dispatch_async(dispatch_get_main_queue(), ^{
        CGFloat superWidth = superView.frame.size.width;
        CGFloat superHeight = superView.frame.size.height;

        CGFloat newWidth, newHeight;
        
        if (size.width > size.height) {
            newWidth = superWidth;
            newHeight = newWidth * size.height / size.width;
            subView.frame = CGRectMake(0, (superHeight - newHeight) * 0.5, newWidth, newHeight);
        } else {
            newHeight = superHeight;
            newWidth = newHeight * size.width / size.height;
            subView.frame = CGRectMake((superWidth - newWidth) * 0.5, 0, newWidth, newHeight);
        }
    });
}

@end
