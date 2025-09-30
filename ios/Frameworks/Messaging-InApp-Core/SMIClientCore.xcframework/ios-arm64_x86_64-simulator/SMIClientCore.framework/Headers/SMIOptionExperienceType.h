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

/// A protocol that represents an option message built with an experienceType format. ``SMIConversationEntry``.
///
/// ## Swift
///
/// ```swift
/// protocol OptionExperienceType
/// ```
///
NS_SWIFT_NAME(OptionExperienceType)
@protocol SMIOptionExperienceType <SMIAbstractChoice>

@property(nonatomic, readonly, strong) NSDictionary<NSString *, id> *content;

@end

NS_ASSUME_NONNULL_END
