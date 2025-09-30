//
//  SMIChoicesExperienceType.h
//  SMIClientCore
//
//  Created by Jeremy Wright on 2025-09-02.
//

#import <Foundation/Foundation.h>
#import <SMIClientCore/SMIEntryPayload.h>
#import <SMIClientCore/SMIEntryTypeMessage.h>
#import <SMIClientCore/SMIOptionExperienceType.h>

@protocol SMIChoicesExperienceTypeResponse;

NS_ASSUME_NONNULL_BEGIN

/// A protocol that represents an option message built with an experienceType format. ``SMIConversationEntry``.
///
/// ## Swift
///
/// ```swift
/// protocol ChoiceExperienceType
/// ```
///
NS_SWIFT_NAME(ChoiceExperienceType)
@protocol SMIChoicesExperienceType <SMIEntryPayload, SMIEntryTypeMessage>

@property (nonatomic, readonly, strong) NSString *targetType;
@property (nonatomic, readonly, strong) NSString *sourceType;
@property (nullable, nonatomic, readonly, strong) NSString *optionType;
@property (nullable, nonatomic, readonly, strong) NSString *message;
@property (nullable, nonatomic, readonly, strong) NSString *targetProperty;

@property (nonatomic, readonly, strong) NSArray<id<SMIOptionExperienceType>> *choices;
@property (nullable, nonatomic, readonly, strong) id<SMIChoicesExperienceTypeResponse> response;

@end

NS_ASSUME_NONNULL_END
