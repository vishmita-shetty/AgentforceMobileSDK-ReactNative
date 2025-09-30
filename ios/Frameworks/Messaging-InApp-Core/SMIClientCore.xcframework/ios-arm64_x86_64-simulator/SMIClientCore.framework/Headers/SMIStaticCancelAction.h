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

NS_SWIFT_NAME(StaticCancelAction)
@protocol SMIStaticCancelAction <SMIEntryPayload, SMIEntryTypeMessage>
@property (nonatomic, readonly, strong) NSDate *timestamp;
@end

NS_ASSUME_NONNULL_END
