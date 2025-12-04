//
//  SMIProgressIndicatorTypes.h
//  SMIClientCore
//
//  Created by Jeremy Wright on 2024-11-22.
//

#import <Foundation/Foundation.h>

NS_ASSUME_NONNULL_BEGIN

/// An enum which describes a set of typing indicator types
/// Possible values:
/// * `SMIProgressIndicatorTypesUpdate`
typedef NSString *SMIProgressIndicatorTypes NS_TYPED_ENUM
NS_SWIFT_NAME(ProgressIndicatorTypes);

/// Describes a typing started event.
FOUNDATION_EXPORT SMIProgressIndicatorTypes const SMIProgressIndicatorTypesUpdate;

NS_ASSUME_NONNULL_END
