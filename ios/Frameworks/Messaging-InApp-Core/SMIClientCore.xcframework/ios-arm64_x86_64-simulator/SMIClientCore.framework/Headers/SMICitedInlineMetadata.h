//
//  SMIInlineMetadata.h
//  SMIClientCore
//
//  Created by Nigel Brown on 2025-03-11.
//

#import <Foundation/Foundation.h>
#import <SMIClientCore/SMICitationClaim.h>

NS_ASSUME_NONNULL_BEGIN

/// Represents the location data of a given citation.
NS_SWIFT_NAME(CitedInlineMetadata)
@protocol SMICitedInlineMetadata <NSObject>

/// Contains the start and end offset for the citation.
@property (nonatomic, nullable, readonly, strong) id<SMICitationClaim> claim;

/// The location of where the citation should end.
@property (nonatomic, readonly, assign) NSUInteger citedLocationOffset;

@end

NS_ASSUME_NONNULL_END
