//
//  StaticExperienceType.h
//  SMIClientCore
//
//  Created by Jeremy Wright on 2025-08-27.
//
#import <Foundation/Foundation.h>
#import <SMIClientCore/SMIEntryPayload.h>
#import <SMIClientCore/SMIEntryTypeMessage.h>
#import <SMIClientCore/SMIStaticErrorDetails.h>

NS_ASSUME_NONNULL_BEGIN

/// A protocol that represents a an Error Message within a ``SMIConversationEntry``.
///
/// ## Swift
///
/// ```swift
/// protocol StaticErrorMessage
/// ```
///
/// Displays an error message from the agent.
///
NS_SWIFT_NAME(StaticErrorMessage)
@protocol SMIStaticErrorMessage <SMIEntryPayload, SMIEntryTypeMessage>

/// The title of the error response.
@property (nonatomic, readonly, strong) NSString *title;

/// Specific details regarding the error which triggered this message.
@property (nonatomic, readonly, strong) id<SMIStaticErrorDetails> errorDetails;

@end

NS_ASSUME_NONNULL_END
