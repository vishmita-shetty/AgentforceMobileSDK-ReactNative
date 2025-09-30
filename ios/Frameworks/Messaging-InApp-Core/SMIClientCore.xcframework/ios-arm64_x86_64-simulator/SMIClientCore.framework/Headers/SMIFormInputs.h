//
//  SMIForm.h
//  SMIClientCore
//
//  Created by Jeremy Wright on 2023-07-28.
//

#import <Foundation/Foundation.h>

#import <SMIClientCore/SMIEntryTypeFormMessage.h>
#import <SMIClientCore/SMIFormTitleItem.h>
#import <SMIClientCore/SMIFormInputSection.h>
#import <SMIClientCore/SMIFormResponseInputs.h>
#import <SMIClientCore/SMIFormResponseResult.h>

NS_ASSUME_NONNULL_BEGIN

/// Represents a form.
NS_SWIFT_NAME(FormInputs)
@protocol SMIFormInputs <SMIEntryTypeFormMessage>

/// The title of the form.
/// @see `SMIFormTitleItem`
@property (nonatomic, readonly, strong) id<SMIFormTitleItem> formTitle;

/// A message which optionally accompanies a form.
/// @see `SMIFormTitleItem`
@property (nonatomic, readonly, strong, nullable) id<SMIFormTitleItem> message;

/// The title of the form.
/// @see `SMIFormTitleItem`
@property (nonatomic, readonly, strong, nullable) id<SMIFormTitleItem> reply;

/// The sections which individually represent a single input for each form field.
/// @see `SMIFormInputSection`
@property (nonatomic, readonly, strong) NSArray<id<SMIFormInputSection>> *sections;

/// The response to the form.
/// This value is set once the user has completed and submitted a form.
/// @see `SMIFormResponseInputs`
@property (nonatomic, readonly, strong, nullable) id<SMIFormResponseInputs> response;

/// The result of the form.
/// This value is set once the acknowledgment is received after the form response is sent to the server.
/// @see `SMIFormResponseResult`
@property (nonatomic, readonly, strong, nullable) id<SMIFormResponseResult> result;

@end

NS_ASSUME_NONNULL_END
