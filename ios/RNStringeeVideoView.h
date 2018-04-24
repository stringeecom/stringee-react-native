
#import <UIKit/UIKit.h>
#import <Stringee/Stringee.h>

@interface RNStringeeVideoView : UIView<StringeeRemoteViewDelegate>

@property (assign, nonatomic) BOOL local;
@property (strong, nonatomic) NSString *callId;
@property (strong, nonatomic) NSString *streamId;

@end
