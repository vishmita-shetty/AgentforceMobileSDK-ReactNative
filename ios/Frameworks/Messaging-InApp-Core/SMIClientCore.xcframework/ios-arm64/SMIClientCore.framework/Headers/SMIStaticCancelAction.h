//
//  Header.h
//  SMIClientCore
//
//  Created by Jeremy Wright on 2025-09-15.
//

#import <Foundation/Foundation.h>
#import <SMIClientCore/SMIEntryPayload.h>
#import <SMIClientCore/SMIEntryTypeMessage.h>

NS_ASSUME_NONNULL_BEGIN

/// Experimental Support for Static LightningType messages with Agentforce.
/// NOTE: As an experimental feature all APIs are subject to change, and this feature may be removed at anytime without warning.
NS_SWIFT_NAME(StaticCancelAction)
@protocol SMIStaticCancelAction <SMIEntryPayload, SMIEntryTypeMessage>
@property (nonatomic, readonly, strong) NSDate *timestamp;
@end

NS_ASSUME_NONNULL_END
