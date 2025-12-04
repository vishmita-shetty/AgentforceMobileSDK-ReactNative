//
//  SMIDataAsset.h
//  SMIClientCore
//
//  Created by Aaron Eisses on 2024-11-27.
//

#import <Foundation/Foundation.h>
#import <UIKit/UIKit.h>
#import <SMIClientCore/SMIFileAsset.h>

NS_ASSUME_NONNULL_BEGIN

/// A Data asset.
NS_SWIFT_NAME(DataAsset)
@protocol SMIDataAsset <SMIFileAsset>

/// NSData representing this document for this asset.
@property (nullable, nonatomic, readonly, strong) NSData *data;

@end

NS_ASSUME_NONNULL_END
