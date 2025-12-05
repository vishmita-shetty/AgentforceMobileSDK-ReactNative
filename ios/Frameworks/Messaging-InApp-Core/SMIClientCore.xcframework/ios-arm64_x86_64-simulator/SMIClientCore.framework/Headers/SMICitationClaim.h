//
//  SMICitationClaim.h
//  SMIClientCore
//
//  Created by Nigel Brown on 2025-03-11.
//

#import <Foundation/Foundation.h>

NS_ASSUME_NONNULL_BEGIN

/// Represents the start and end locations of a given citation.
NS_SWIFT_NAME(CitationClaim)
@protocol SMICitationClaim <NSObject>

/// The start offset for where the citation is located in the text message.
@property (nonatomic, readonly, assign) NSUInteger claimStartOffset;

/// The end offset for where the citation is located in the text message.
@property (nonatomic, readonly, assign) NSUInteger claimEndOffset;

@end

NS_ASSUME_NONNULL_END
