//
//  StaticExperienceType.h
//  SMIClientCore
//
//  Created by Jeremy Wright on 2025-08-27.
//
#import <Foundation/Foundation.h>

NS_ASSUME_NONNULL_BEGIN

/// A protocol that represents a an Error Message within a ``SMIConversationEntry``.
///
/// ## Swift
///
/// ```swift
/// protocol StaticErrorDetails
/// ```
///
/// Details of an error message returned from an agent.
///
NS_SWIFT_NAME(StaticErrorDetails)
@protocol SMIStaticErrorDetails <NSObject>

/// The error code.
@property (nonatomic, readonly, strong) NSString *errorCode;

/// A list of error messages.
@property (nonatomic, readonly, strong) NSArray<NSString *> *errorMessages;

@end

NS_ASSUME_NONNULL_END
