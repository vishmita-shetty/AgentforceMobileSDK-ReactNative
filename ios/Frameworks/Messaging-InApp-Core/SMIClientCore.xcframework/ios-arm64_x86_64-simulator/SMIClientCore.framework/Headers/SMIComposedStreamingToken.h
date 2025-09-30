//
//  SMIComposedStreamingToken.h
//  SMIClientCore
//
//  Created by Jeremy Wright on 2025-01-06.
//

#import <SMIClientCore/SMIMessageStreamingToken.h>
#import <SMIClientCore/SMIStreamingTokenValidation.h>
#import <SMIClientCore/SMIEntryFormat.h>

NS_ASSUME_NONNULL_BEGIN

/// Represents a conversation entry composed with a number of `SMIStreamingToken`.
NS_SWIFT_NAME(ComposedStreamingToken)
@protocol SMIComposedStreamingToken <SMIEntryPayload>

/// The identifier which will correspond to the final transcripted entry.
@property (nonatomic, readonly, strong) NSString *targetMessageIdentifier;

/// Corresponds to an identifier applied to the agent's response. This Identifier can be used to correlate all events to a single turn.
@property (nonatomic, readonly, strong) NSString *turnId;

/// Corresponds to an identifier applied to the agent's response. Events which have a strong association will be sent in batches.
/// This identifier can be used to identifier a batch of related events within a single turn.
@property (nonatomic, readonly, assign) NSUInteger batchNumber;

/// The type of this streaming token.
@property (nonatomic, readonly, strong) SMIStreamingTokenTypes tokenType;

/// The compisition of all current `SMIStreamingToken` objects associated with this entry.
@property (nonatomic, readonly, strong) id<SMIEntryFormat> derivedValue;

/// The current list of all related streaming tokens in sequential order.
@property (nonatomic, readonly, strong) NSArray<id<SMIMessageStreamingToken>> *relatedStreamingTokens;

/// The `SMIStreamingTokenValidation` which if present will signal whether the payload contained in this composed token is valid.
/// This property is nil until the validation token is received. It is safe to assume that while this is nil the currently streamed set of tokens are valid.
/// It is possible that there will never be a validation token sent, this is generally true where the content of the stream has been accepted and transcripted as final.
/// If this validation token is present and it's `valid` property is `NO` you should assume that the content of this stream has been discarded and will be replaced.
@property (nullable, nonatomic, readonly, strong) id<SMIStreamingTokenValidation> validation;

@end

NS_ASSUME_NONNULL_END
