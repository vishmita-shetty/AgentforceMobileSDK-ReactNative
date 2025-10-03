//
//  SMIChoicesExperienceTypeResponse.h
//  SMIClientCore
//
//  Created by Jeremy Wright on 2025-09-06.
//

#import <Foundation/Foundation.h>
#import <SMIClientCore/SMIEntryPayload.h>
#import <SMIClientCore/SMIEntryTypeMessage.h>

NS_ASSUME_NONNULL_BEGIN

/// Experimental Support for Choices for LightningType interactions with Agentforce.
/// NOTE: As an experimental feature all APIs are subject to change, and this feature may be removed at anytime without warning.
NS_SWIFT_NAME(ChoicesExperienceTypeResponse)
@protocol SMIChoicesExperienceTypeResponse <SMIEntryPayload, SMIEntryTypeMessage>

@property (nonatomic, readonly, strong) NSArray<NSDictionary<NSString *, id> *> *selectedValues;

@end

NS_ASSUME_NONNULL_END
