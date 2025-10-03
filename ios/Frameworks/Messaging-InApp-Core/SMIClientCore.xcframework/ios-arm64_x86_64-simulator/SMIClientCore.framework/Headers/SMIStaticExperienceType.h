//
//  StaticExperienceType.h
//  SMIClientCore
//
//  Created by Jeremy Wright on 2025-08-27.
//
#import <Foundation/Foundation.h>
#import <SMIClientCore/SMIEntryPayload.h>
#import <SMIClientCore/SMIEntryTypeMessage.h>
#import <SMIClientCore/SMICitationContent.h>

NS_ASSUME_NONNULL_BEGIN

/// Experimental Support for Static LightningType messages with Agentforce.
/// NOTE: As an experimental feature all APIs are subject to change, and this feature may be removed at anytime without warning.
NS_SWIFT_NAME(StaticExperienceType)
@protocol SMIStaticExperienceType <SMIEntryPayload, SMIEntryTypeMessage>

@property (nonatomic, readonly, strong) NSString *message;

@property (nonatomic, readonly, strong) NSArray<NSDictionary<NSString *, id> *> *values;

@end

NS_ASSUME_NONNULL_END
