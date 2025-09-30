//
//  SMICitedReference.h
//  SMIClientCore
//
//  Created by Nigel Brown on 2025-03-11.
//

#import <Foundation/Foundation.h>
#import <SMIClientCore/SMICitedReferenceType.h>

NS_ASSUME_NONNULL_BEGIN

/// Represents the link information of citation.
NS_SWIFT_NAME(CitedReference)
@protocol SMICitedReference <NSObject>

/// The type for the  citation.
@property (nonatomic, readonly, strong) SMICitedReferenceType citedReferenceType;

@end

NS_ASSUME_NONNULL_END
