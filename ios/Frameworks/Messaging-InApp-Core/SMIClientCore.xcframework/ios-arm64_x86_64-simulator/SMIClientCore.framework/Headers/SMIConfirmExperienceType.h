//
//  SMIFormExperienceType.h
//  SMIClientCore
//
//  Created by Jeremy Wright on 2025-09-11.
//

#import <Foundation/Foundation.h>

NS_ASSUME_NONNULL_BEGIN

/// Represents a form.
NS_SWIFT_NAME(ConfirmExperienceType)
@protocol SMIConfirmExperienceType <NSObject>

@property (nullable, nonatomic, readonly, strong) NSString *message;

@end

NS_ASSUME_NONNULL_END
