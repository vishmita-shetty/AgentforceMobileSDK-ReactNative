//
//  SMIFormExperienceType.h
//  SMIClientCore
//
//  Created by Jeremy Wright on 2025-09-11.
//

#import <Foundation/Foundation.h>
#import <SMIClientCore/SMIEntryTypeFormResponseMessage.h>

NS_ASSUME_NONNULL_BEGIN

/// Represents a form response.
NS_SWIFT_NAME(FormExperienceTypeResponse)
@protocol SMIFormExperienceTypeResponse <SMIEntryTypeFormResponseMessage>

/// An array containing dictionaries representing form responses.
/// The intention is to return a value for each entry in the `inputs` property.
@property (nonatomic, readonly, strong) NSArray<NSDictionary<NSString *, id> *> *inputValues;

@end

NS_ASSUME_NONNULL_END
