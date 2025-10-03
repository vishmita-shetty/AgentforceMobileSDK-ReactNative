//
//  StaticExperienceType.h
//  SMIClientCore
//
//  Created by Jeremy Wright on 2025-08-27.
//
#import <Foundation/Foundation.h>

NS_ASSUME_NONNULL_BEGIN

/// Experimental Support for Static LightningType messages with Agentforce.
/// NOTE: As an experimental feature all APIs are subject to change, and this feature may be removed at anytime without warning.
NS_SWIFT_NAME(StaticErrorDetails)
@protocol SMIStaticErrorDetails <NSObject>

@property (nonatomic, readonly, strong) NSString *errorCode;

@property (nonatomic, readonly, strong) NSArray<NSString *> *errorMessages;

@end

NS_ASSUME_NONNULL_END
