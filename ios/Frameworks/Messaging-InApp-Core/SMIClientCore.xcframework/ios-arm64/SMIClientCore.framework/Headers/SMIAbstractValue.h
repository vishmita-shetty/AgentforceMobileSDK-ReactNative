//
//  SMIAbstractValue.h
//  SMIClientCore
//
//  Created by Aaron Eisses on 2025-09-09.
//  Copyright © 2025 Salesforce. All rights reserved.
//

#import <Foundation/Foundation.h>
#import <SMIClientCore/SMISerializable.h>

NS_ASSUME_NONNULL_BEGIN

/// Protocol that defines the base type for session context values.
///
/// ## Swift
///
/// ```swift
/// protocol SMIAbstractValueProtocol
/// ```
NS_SWIFT_NAME(AbstractValueProtocol)
@protocol SMIAbstractValue
@end

/// Concrete implementation of SMIAbstractValue protocol.
///
/// This class serves as the base implementation for all session context value types.
/// It extends SMISerializable to support serialization and implements the SMIAbstractValue protocol.
///
/// @see `SMIAbstractValue` protocol
/// @see `SMISerializable`
NS_SWIFT_NAME(AbstractValue)
@interface SMIAbstractValue : SMISerializable <SMIAbstractValue>
@end

NS_ASSUME_NONNULL_END
