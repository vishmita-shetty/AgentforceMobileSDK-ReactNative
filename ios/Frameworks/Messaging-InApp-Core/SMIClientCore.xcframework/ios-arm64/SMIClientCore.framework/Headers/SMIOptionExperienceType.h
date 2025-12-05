//
//  SMIOptionExperienceType.h
//  SMIClientCore
//
//  Created by Jeremy Wright on 2025-09-10.
//

#import <Foundation/Foundation.h>
#import <SMIClientCore/SMIChoice.h>
#import <SMIClientCore/SMIAbstractChoice.h>

NS_ASSUME_NONNULL_BEGIN

/// Experimental Support for Choices for LightningType interactions with Agentforce.
/// NOTE: As an experimental feature all APIs are subject to change, and this feature may be removed at anytime without warning.
NS_SWIFT_NAME(OptionExperienceType)
@protocol SMIOptionExperienceType <SMIAbstractChoice>

@property(nonatomic, readonly, strong) NSDictionary<NSString *, id> *content;

@end

NS_ASSUME_NONNULL_END
