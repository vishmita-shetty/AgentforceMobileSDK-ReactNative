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

/// Experimental Support for Static LightningType messages with Agentforce.
/// NOTE: As an experimental feature all APIs are subject to change, and this feature may be removed at anytime without warning.
NS_SWIFT_NAME(StaticErrorMessage)
@protocol SMIStaticErrorMessage <SMIEntryPayload, SMIEntryTypeMessage>

/// The title of the error response.
@property (nonatomic, readonly, strong) NSString *title;

/// Specific details regarding the error which triggered this message.
@property (nonatomic, readonly, strong) id<SMIStaticErrorDetails> errorDetails;

@end

NS_ASSUME_NONNULL_END
