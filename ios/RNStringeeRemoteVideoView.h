
#import <UIKit/UIKit.h>
#import <Stringee/Stringee.h>

@interface RNStringeeRemoteVideoView : UIView <StringeeRemoteViewDelegate>

@property (strong, nonatomic) NSString *callId;
@property (strong, nonatomic) NSString *streamId;

@end
