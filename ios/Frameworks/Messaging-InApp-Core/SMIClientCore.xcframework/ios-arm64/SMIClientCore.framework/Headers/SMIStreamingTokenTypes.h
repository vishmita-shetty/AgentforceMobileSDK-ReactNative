//
//  SMIStreamingTokenTypes.h
//  SMIClientCore
//
//  Created by Jeremy Wright on 2024-12-09.
//

#import <Foundation/Foundation.h>

NS_ASSUME_NONNULL_BEGIN

/// An enum which describes a set of typing indicator types
/// Possible values:
/// * `SMIStreamingTokenTypes`
typedef NSString *SMIStreamingTokenTypes NS_TYPED_ENUM
NS_SWIFT_NAME(StreamingTokenTypes);

/// Describes a streamed token which contains part of a message.
FOUNDATION_EXPORT SMIStreamingTokenTypes const SMIStreamingTokenTypesMessage;

/// Describes a streamed token which serves as a validation of all tokens in a batch.
FOUNDATION_EXPORT SMIStreamingTokenTypes const SMIStreamingTokenTypesValidation;

NS_ASSUME_NONNULL_END
