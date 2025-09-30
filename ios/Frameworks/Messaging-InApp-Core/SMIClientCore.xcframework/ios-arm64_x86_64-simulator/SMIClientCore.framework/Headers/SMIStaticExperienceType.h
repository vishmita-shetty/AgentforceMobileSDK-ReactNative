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

/// A protocol that represents a an experience type message within a ``SMIConversationEntry``.
///
/// ## Swift
///
/// ```swift
/// protocol StaticExperienceType
/// ```
///
/// Use this protocol to access the content of a text message displayed in a conversation.
/// A text message can optionally include citation content that provides additional context.
///
NS_SWIFT_NAME(StaticExperienceType)
@protocol SMIStaticExperienceType <SMIEntryPayload, SMIEntryTypeMessage>

@property (nonatomic, readonly, strong) NSString *message;

@property (nonatomic, readonly, strong) NSArray<NSDictionary<NSString *, id> *> *values;

@end

NS_ASSUME_NONNULL_END
