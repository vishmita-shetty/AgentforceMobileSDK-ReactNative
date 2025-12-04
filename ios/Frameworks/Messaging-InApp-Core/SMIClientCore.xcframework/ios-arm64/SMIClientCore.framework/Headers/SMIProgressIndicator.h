//
//  SMIProgressIndicator.h
//  SMIClientCore
//
//  Created by Jeremy Wright on 2024-11-22.
//

#import <Foundation/Foundation.h>
#import <SMIClientCore/SMIEntryPayload.h>
#import <SMIClientCore/SMIConversationEntry.h>
#import <SMIClientCore/SMIProgressIndicatorTypes.h>
#import <SMIClientCore/SMIEntryFormat.h>

NS_ASSUME_NONNULL_BEGIN

/// Represents a typing indicator within an `SMIConversationEntry`.
NS_SWIFT_NAME(ProgressIndicator)
@protocol SMIProgressIndicator <SMIEntryPayload>

/// The type of this typing indicator.
@property (nonatomic, readonly, strong) SMIProgressIndicatorTypes indicatorType;

/// The contents of the progress indicator.
@property (nonatomic, readonly, strong) id<SMIEntryFormat> progressMessage;

@end

NS_ASSUME_NONNULL_END
