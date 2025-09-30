//
//  SMICitationContent.h
//  SMIClientCore
//
//  Created by Nigel Brown on 2025-03-17.
//

#import <Foundation/Foundation.h>
#import <SMIClientCore/SMICitation.h>

NS_ASSUME_NONNULL_BEGIN

/// Represents the citations for a given text message entry.
NS_SWIFT_NAME(CitationContent)
@protocol SMICitationContent <NSObject>

/// The list of citations.
@property (nonatomic, readonly, strong) NSArray<id<SMICitation>> *citations;

@end

NS_ASSUME_NONNULL_END
