//
//  SMIFormExperienceType.h
//  SMIClientCore
//
//  Created by Jeremy Wright on 2025-09-11.
//

#import <Foundation/Foundation.h>

NS_ASSUME_NONNULL_BEGIN

/// Experimental Support for Forms for LightningType interactions with Agentforce.
/// NOTE: As an experimental feature all APIs are subject to change, and this feature may be removed at anytime without warning.
NS_SWIFT_NAME(ConfirmExperienceType)
@protocol SMIConfirmExperienceType <NSObject>

@property (nullable, nonatomic, readonly, strong) NSString *message;

@end

NS_ASSUME_NONNULL_END
