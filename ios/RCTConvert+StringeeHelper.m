//
//  RCTConvert+StringeeHelper.m
//  RNStringee
//
//  Created by HoangDuoc on 11/16/18.
//  Copyright © 2018 Facebook. All rights reserved.
//

#import "RCTConvert+StringeeHelper.h"
#import <React/RCTUtils.h>

@implementation RCTConvert (StringeeHelper)

//RCT_ENUM_CONVERTER(StringeeMessageType,(@{
//                                          @"Text" : @(StringeeMessageTypeText),
//                                          @"Photo" : @(StringeeMessageTypePhoto),
//                                          @"Video" : @(StringeeMessageTypeVideo),
//                                          @"Audio" : @(StringeeMessageTypeAudio),
//                                          @"File" : @(StringeeMessageTypeFile),
//                                          @"CreateGroup" : @(StringeeMessageTypeCreateGroup),
//                                          @"RenameGroup" : @(StringeeMessageTypeRenameGroup),
//                                          @"Location" : @(StringeeMessageTypeLocation),
//                                          @"Contact" : @(StringeeMessageTypeContact),
//                                          @"Notify" : @(StringeeMessageTypeNotify)
//                                        }), StringeeMessageTypeText, integerValue)
//
//RCT_ENUM_CONVERTER(StringeeMessageStatus,(@{
//                                            @"Pending" : @(StringeeMessageStatusPending),
//                                            @"Sending" : @(StringeeMessageStatusSending),
//                                            @"Sent" : @(StringeeMessageStatusSent),
//                                            @"Delivered" : @(StringeeMessageStatusDelivered),
//                                            @"Read" : @(StringeeMessageStatusRead)
//                                          }), StringeeMessageStatusPending, integerValue)

+ (NSDictionary *)StringeeIdentity:(StringeeIdentity *)identity {
    if (!identity) return RCTNullIfNil(nil);
    
    NSString *userId = identity.userId.length ? identity.userId : @"";
    NSString *name = identity.displayName.length ? identity.displayName : @"";
    NSString *avatar = identity.avatarUrl.length ? identity.avatarUrl : @"";

    return @{
             @"userId": userId,
             @"name": name,
             @"avatar": avatar
             };
}

+ (NSArray *)StringeeIdentities:(NSArray<StringeeIdentity *> *)identities {
    if (!identities) {
        return RCTNullIfNil(nil);
    }
    NSMutableArray *response = [NSMutableArray array];
    for (StringeeIdentity *identity in identities) {
        [response addObject:[self StringeeIdentity:identity]];
    }
    return response;
}

+ (NSDictionary *)StringeeConversation:(StringeeConversation *)conversation {
    if (!conversation) return RCTNullIfNil(nil);

    NSString *identifier = conversation.identifier ? conversation.identifier : @"";
    NSString *name = conversation.name ? conversation.name : @"";
    NSString *lastMsgId = conversation.lastMsg.identifier ? conversation.lastMsg.identifier : @"";

    NSMutableArray *participants = [[NSMutableArray alloc] init];
    for (StringeeIdentity *identity in conversation.participants) {
        [participants addObject:[self StringeeIdentity:identity]];
    }
    NSString *lastMsgSender = conversation.lastMsg.sender ? conversation.lastMsg.sender : @"";
    NSString *text = conversation.lastMsg.content ? conversation.lastMsg.content : @"";
    id lastMsgContent = [self StringToDictionary:text];
    NSString *creator = conversation.creator ? conversation.creator : @"";
    StringeeMessageStatus lastMsgState = conversation.lastMsgSeqReceived > conversation.lastMsgSeqSeen ? StringeeMessageStatusDelivered : StringeeMessageStatusRead;

    return @{
             @"id": identifier,
             @"name": name,
             @"participants": participants,
             @"isGroup": @(conversation.isGroup),
             @"updatedAt" : @(conversation.lastUpdate),
             @"lastMsgSender" : lastMsgSender,
             @"text": lastMsgContent,
             @"lastMsgType": @(conversation.lastMsg.type),
             @"unreadCount": @(conversation.unread),
             @"lastMsgId": lastMsgId,
             @"creator": creator,
             @"created" : @(conversation.created),
             @"lastMsgSeq": @(conversation.lastMsgSeqReceived),
             @"lastMsgCreatedAt": @(conversation.lastTimeNewMsg),
             @"lastMsgState": @(lastMsgState)
             };
}

+ (NSArray *)StringeeConversations:(NSArray<StringeeConversation *> *)conversations {
    if (!conversations) {
//        return RCTNullIfNil(nil);
        return @[];
    }
    NSMutableArray *response = [NSMutableArray array];
    for (StringeeConversation *conversation in conversations) {
        [response addObject:[self StringeeConversation:conversation]];
    }
    return response;
}

+ (NSDictionary *)StringeeMessage:(StringeeMessage *)message {
    if (!message) return RCTNullIfNil(nil);
    
    NSString *localId = message.localIdentifier.length ? message.localIdentifier : @"";
    NSString *identifier = message.identifier.length ? message.identifier : @"";
    NSString *conversationId = message.convId.length ? message.convId : @"";
    NSString *sender = message.sender.length ? message.sender : @"";
    
    // Cần parse text và type ở đây
//    NSString *text = @"";
//    NSNumber *type = [NSNumber numberWithInt:1];
//    NSString *content = message.content.length ? message.content : @"";
//    if (message.type == StringeeMessageTypeCreateGroup || message.type == StringeeMessageTypeRenameGroup || message.type == StringeeMessageTypeNotify) {
//        text = content;
//        type = [NSNumber numberWithInt:message.type];
//    } else {
//         NSError *jsonError;
//         NSData *msgData = [content dataUsingEncoding:NSUTF8StringEncoding];
//         NSDictionary *dicData = [NSJSONSerialization JSONObjectWithData:msgData
//                                                                 options:NSJSONReadingMutableContainers
//                                                                   error:&jsonError];
//
////         text = dicData[@"text"] != nil && dicData[@"text"] != [NSNull null] ? dicData[@"text"] : @"";
//        text = content;
//        type = dicData[@"type"] != nil && dicData[@"type"] != [NSNull null] ? dicData[@"type"] : [NSNumber numberWithInt:1];
//    }
    
    NSString *thumbnailPath = @"";
    NSString *thumbnailUrl = @"";
    NSString *filePath = @"";
    NSString *fileUrl = @"";
    double longitude = 0;
    double latitude = 0;
    double duration = 0;
    double ratio = 0;
    NSUInteger fileLength = 0;
    NSString *fileName = @"";
    NSString *contact = @"";
    
    NSDictionary *content;
    
    switch (message.type) {
        case StringeeMessageTypeText:
            content = @{@"content": message.content};
            break;
        case StringeeMessageTypeLink:
            content = @{@"content": message.content};
            break;
        case StringeeMessageTypeCreateGroup:
            content = [self StringToDictionary:message.content];
            break;
        case StringeeMessageTypeRenameGroup:
            content = [self StringToDictionary:message.content];
            break;
        case StringeeMessageTypeNotify:
            content = [self StringToDictionary:message.content];
            break;
        case StringeeMessageTypePhoto:
        {
            StringeePhotoMessage *photoMsg = (StringeePhotoMessage *)message;
            thumbnailPath = photoMsg.thumbnailPath.length ? photoMsg.thumbnailPath : @"";
            thumbnailUrl = photoMsg.thumbnailUrl.length ? photoMsg.thumbnailUrl : @"";
            filePath = photoMsg.filePath.length ? photoMsg.filePath : @"";
            fileUrl = photoMsg.fileUrl.length ? photoMsg.fileUrl : @"";
            ratio = photoMsg.ratio;
            
            content = @{
                        @"photo": @{
                                    @"filePath": fileUrl,
                                    @"thumbnail": thumbnailUrl,
                                    @"ratio": @(ratio)
                                }
                        };
        }
            break;
        case StringeeMessageTypeVideo:
        {
            StringeeVideoMessage *videoMsg = (StringeeVideoMessage *)message;
            thumbnailPath = videoMsg.thumbnailPath.length ? videoMsg.thumbnailPath : @"";
            thumbnailUrl = videoMsg.thumbnailUrl.length ? videoMsg.thumbnailUrl : @"";
            filePath = videoMsg.filePath.length ? videoMsg.filePath : @"";
            fileUrl = videoMsg.fileUrl.length ? videoMsg.fileUrl : @"";
            ratio = videoMsg.ratio;
            duration = videoMsg.duration;
            
            content = @{
                        @"video": @{
                                    @"filePath": fileUrl,
                                    @"thumbnail": thumbnailUrl,
                                    @"ratio": @(ratio),
                                    @"duration": @(duration)
                                }
                        };
        }
            break;
        case StringeeMessageTypeAudio:
        {
            StringeeAudioMessage *audioMsg = (StringeeAudioMessage *)message;
            filePath = audioMsg.filePath.length ? audioMsg.filePath : @"";
            fileUrl = audioMsg.fileUrl.length ? audioMsg.fileUrl : @"";
            duration = audioMsg.duration;
            
            content = @{
                        @"audio": @{
                                @"filePath": fileUrl,
                                @"duration": @(duration)
                                }
                        };
        }
            break;
        case StringeeMessageTypeFile:
        {
            StringeeFileMessage *fileMsg = (StringeeFileMessage *)message;
            filePath = fileMsg.filePath.length ? fileMsg.filePath : @"";
            fileUrl = fileMsg.fileUrl.length ? fileMsg.fileUrl : @"";
            fileName = fileMsg.filename.length ? fileMsg.filename : @"";
            fileLength = fileMsg.length;
            
            content = @{
                        @"file": @{
                                @"filePath": fileUrl,
                                @"filename": fileName,
                                @"length": @(fileLength),
                                }
                        };
        }
            break;
        case StringeeMessageTypeLocation:
        {
            StringeeLocationMessage *locationMsg = (StringeeLocationMessage *)message;
            latitude = locationMsg.latitude;
            longitude = locationMsg.longitude;
            
            content = @{
                        @"location": @{
                                @"lat": @(latitude),
                                @"lon": @(longitude)
                                }
                        };
        }
            break;
        case StringeeMessageTypeContact:
        {
            StringeeContactMessage *contactMsg = (StringeeContactMessage *)message;
            NSString *vcard = contactMsg.vcard.length ? contactMsg.vcard : @"";
            
            content = @{
                        @"contact": @{
                                @"vcard": vcard
                                }
                        };
        }
            break;
            
        default:
            content = @{};
            break;
    }
    
    
    return @{
             @"localId": localId,
             @"id": identifier,
             @"conversationId": conversationId,
             @"sender": sender,
             @"createdAt": @(message.created),
             @"state": @(message.status),
             @"sequence": @(message.seq),
             @"type": @(message.type),
             @"content": content,
             @"thumbnailPath": thumbnailPath,
             @"thumbnailUrl": thumbnailUrl,
             @"filePath": filePath,
             @"fileUrl": fileUrl,
             @"latitude": @(latitude),
             @"longitude": @(longitude),
             @"duration": @(duration),
             @"ratio": @(ratio),
             @"fileName": fileName,
             @"fileLength": @(fileLength),
             @"contact": contact
             };
}

+ (NSArray *)StringeeMessages:(NSArray<StringeeMessage *> *)messages {
    if (!messages) {
        return RCTNullIfNil(nil);
    }
    NSMutableArray *response = [NSMutableArray array];
    for (StringeeMessage *message in messages) {
        [response addObject:[self StringeeMessage:message]];
    }
    return response;
}

// MARK: - Utils

+ (id)StringToDictionary:(NSString *)str {
    if (!str || !str.length) {
        return [NSNull null];
    }
    
    NSError *jsonError;
    NSData *objectData = [str dataUsingEncoding:NSUTF8StringEncoding];
    NSDictionary *json = [NSJSONSerialization JSONObjectWithData:objectData
                                                         options:NSJSONReadingMutableContainers
                                                           error:&jsonError];
    
    if (jsonError) {
        return [NSNull null];
    } else {
        return json;
    }
}

+ (BOOL)isValid:(NSString *)value {
    if (value == nil || ![value isKindOfClass:[NSString class]] || value.length == 0) {
        return false;
    }
    
    return true;
}

@end
