//
//  SMICitation.h
//  SMIClientCore
//
//  Created by Nigel Brown on 2025-03-11.
//

#import <Foundation/Foundation.h>
#import <SMIClientCore/SMICitedReference.h>
#import <SMIClientCore/SMICitedDetails.h>

NS_ASSUME_NONNULL_BEGIN

/// Represents a citation.
NS_SWIFT_NAME(Citation)
@protocol SMICitation <NSObject>

/// Contains the link information realted to the ciation.
@property (nonatomic, readonly, strong) id<SMICitedReference> citedReference;

/// Offset details for the citation.
@property (nonatomic, readonly, nullable, strong) id<SMICitedDetails> citedDetails;

@end

NS_ASSUME_NONNULL_END
