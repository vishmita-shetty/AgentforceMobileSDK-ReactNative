//
//  SMIAbstractChoice.h
//  SMIClientCore
//
//  Created by Jeremy Wright on 2025-09-02.
//

#import <Foundation/Foundation.h>

NS_ASSUME_NONNULL_BEGIN

NS_SWIFT_NAME(AbstractChoice)
@protocol SMIAbstractChoice <NSObject>

/// The ID of this option.
@property (nonatomic, readonly, strong) NSString *optionId;

/// The ID of the parent entry for this choice.
@property (nonatomic, readonly, nullable, strong) NSString *parentEntryId;
@end

NS_ASSUME_NONNULL_END
