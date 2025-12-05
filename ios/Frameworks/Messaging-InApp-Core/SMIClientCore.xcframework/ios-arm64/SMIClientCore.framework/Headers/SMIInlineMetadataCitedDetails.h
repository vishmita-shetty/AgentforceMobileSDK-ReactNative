//
//  SMIInlineMetadataCitedDetails.h
//  SMIClientCore
//
//  Created by Jeremy Wright on 2025-04-03.
//

#import <Foundation/Foundation.h>
#import <SMIClientCore/SMICitedDetails.h>
#import <SMIClientCore/SMICitedInlineMetadata.h>

NS_ASSUME_NONNULL_BEGIN

/// Represents the location data of a given citation.
NS_SWIFT_NAME(InlineMetadataCitedDetails)
@protocol SMIInlineMetadataCitedDetails <SMICitedDetails>

/// Contains the information of where the citation is located.
@property (nonatomic, readonly, strong) NSArray<id<SMICitedInlineMetadata>> *inlineMetadata;

@end

NS_ASSUME_NONNULL_END
