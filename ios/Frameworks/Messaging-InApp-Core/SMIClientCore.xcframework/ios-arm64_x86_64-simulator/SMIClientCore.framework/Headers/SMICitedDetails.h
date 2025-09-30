//
//  SMICitedDetails.h
//  SMIClientCore
//
//  Created by Nigel Brown on 2025-03-11.
//

#import <Foundation/Foundation.h>
#import <SMIClientCore/SMICitedDetailsType.h>
#import <SMIClientCore/SMICitedInlineMetadata.h>

NS_ASSUME_NONNULL_BEGIN

/// Represents the location data of a given citation.
NS_SWIFT_NAME(CitedDetails)
@protocol SMICitedDetails <NSObject>

/// The type for the cited details.
@property (nonatomic, readonly, strong) SMICitedDetailsType citedDetailsType;

@end

NS_ASSUME_NONNULL_END
