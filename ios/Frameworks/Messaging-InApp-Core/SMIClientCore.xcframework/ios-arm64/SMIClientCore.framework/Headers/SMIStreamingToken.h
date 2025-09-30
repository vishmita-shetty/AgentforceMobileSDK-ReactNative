//
//  SMIStreamingToken.h
//  SMIClientCore
//
//  Created by Jeremy Wright on 2024-12-09.
//

#import <SMIClientCore/SMIEntryPayload.h>
#import <SMIClientCore/SMIConversationEntry.h>
#import <SMIClientCore/SMIStreamingTokenTypes.h>

NS_ASSUME_NONNULL_BEGIN

/// Represents an individual streamed token which is part of a series of tokens each of which contain a fragment of a complete entry payload.
NS_SWIFT_NAME(StreamingToken)
@protocol SMIStreamingToken <SMIEntryPayload>

/// The sequence order in which this token lies in the overall message.
@property (nonatomic, readonly, assign) NSUInteger sequenceNumber;

/// The timestamp of this specific token.
/// Note: This may not correspond directly to the conversation entry, as the token is generated before
/// the entry itself.
@property (nonatomic, readonly, strong) NSDate *timestamp;

/// The type of this streaming token.
@property (nonatomic, readonly, strong) SMIStreamingTokenTypes tokenType;

@end

NS_ASSUME_NONNULL_END
