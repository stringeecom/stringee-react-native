//
//  RNClientWrapper.h
//  RNStringee
//
//  Created by HoangDuoc on 5/20/20.
//

#import <Foundation/Foundation.h>
#import <Stringee/Stringee.h>

@interface RNClientWrapper : NSObject <StringeeConnectionDelegate, StringeeIncomingCallDelegate>

@property (nonatomic) NSString *identifier;
@property (nonatomic) StringeeClient *client;
@property (assign, nonatomic) BOOL isConnecting; // client dang thuc hien connect toi Stringee Server
@property (nonatomic) NSMutableDictionary *messages;

- (instancetype)initWithIdentifier:(NSString *)identifier;

- (void)setNativeEvent:(NSString *)event;

- (void)removeNativeEvent:(NSString *)event;

- (void)createClientIfNeed;

@end

