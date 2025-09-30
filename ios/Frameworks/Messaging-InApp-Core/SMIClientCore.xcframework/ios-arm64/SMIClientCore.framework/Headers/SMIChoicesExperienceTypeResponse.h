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

/// Represents a list of choices within an `SMIConversationEntry`.
NS_SWIFT_NAME(ChoicesExperienceTypeResponse)
@protocol SMIChoicesExperienceTypeResponse <SMIEntryPayload, SMIEntryTypeMessage>

/// The list of choices.
@property (nonatomic, readonly, strong) NSArray<NSDictionary<NSString *, id> *> *selectedValues;

@end

NS_ASSUME_NONNULL_END
