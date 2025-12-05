//
//  SMILinkCitedReference.h
//  SMIClientCore
//
//  Created by Jeremy Wright on 2025-04-02.
//

#import <Foundation/Foundation.h>
#import <SMIClientCore/SMICitedReference.h>

NS_ASSUME_NONNULL_BEGIN

/// Represents the link information of citation.
NS_SWIFT_NAME(LinkCitedReference)
@protocol SMILinkCitedReference <SMICitedReference>

/// The URL for the citation.
@property (nonatomic, readonly, strong) NSURL *url;

/// The Record ID for the citation.
@property (nonatomic, readonly, nullable, strong) NSString *recordId;

// A label which may be provided to be used in place of excessively long domains.
@property (nonatomic, readonly, nullable, strong) NSString *label;

@end

NS_ASSUME_NONNULL_END
