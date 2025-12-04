//
//  SMIFormExperienceType.h
//  SMIClientCore
//
//  Created by Jeremy Wright on 2025-09-11.
//

#import <Foundation/Foundation.h>
#import <SMIClientCore/SMIEntryTypeFormMessage.h>
#import <SMIClientCore/SMIFormExperienceTypeResponse.h>
#import <SMIClientCore/SMIConfirmExperienceType.h>

NS_ASSUME_NONNULL_BEGIN

/// Experimental Support for Forms for LightningType interactions with Agentforce.
/// NOTE: As an experimental feature all APIs are subject to change, and this feature may be removed at anytime without warning.
NS_SWIFT_NAME(FormExperienceType)
@protocol SMIFormExperienceType <SMIEntryTypeFormMessage>

@property (nonatomic, readonly, strong) NSString *message;

@property (nullable, nonatomic, readonly, strong) id<SMIConfirmExperienceType> confirm;

@property (nonatomic, readonly, strong) NSArray<NSDictionary<NSString *, id> *> *inputs;

/// An array containing dictionaries representing form responses.
/// The intention is to return a value for each entry in the `inputs` property.
/// This array is only intended for collection of data. It is not persisted on this entry.
/// However upon sending this form entry, a `SMIFormExperienceTypeResponse` will be created and persisted with the collected data.
@property (null_resettable, nonatomic, readwrite, strong) NSArray<NSDictionary<NSString *, id> *> *collectValues;

/// nil until the form has been sent. The `inputValues` array of the response will be populated with the content of the `collectValues` array.
@property (nullable, nonatomic, readonly, strong) id<SMIFormExperienceTypeResponse> response;

@end

NS_ASSUME_NONNULL_END
