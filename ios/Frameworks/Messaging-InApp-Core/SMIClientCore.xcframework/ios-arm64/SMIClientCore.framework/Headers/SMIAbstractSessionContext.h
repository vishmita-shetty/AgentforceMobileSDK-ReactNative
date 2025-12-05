//
//  SMIAbstractSessionContext.h
//  SMIClientCore
//
//  Created by Aaron Eisses on 2025-09-11.
//  Copyright © 2025 Salesforce. All rights reserved.
//

#import <Foundation/Foundation.h>
#import <SMIClientCore/SMISerializable.h>

NS_ASSUME_NONNULL_BEGIN

/// Protocol that defines the base type for abstract session context values.
///
/// ## Swift
///
/// ```swift
/// protocol AbstractSessionContextProtocol
/// ```
NS_SWIFT_NAME(AbstractSessionContextProtocol)
@protocol SMIAbstractSessionContext
@end

/// Concrete implementation of SMIAbstractSessionContext protocol.
///
/// This class serves as the base implementation for all session context types.
/// It extends SMISerializable to support serialization and implements the SMIAbstractSessionContext protocol.
///
/// @see `SMIAbstractSessionContext` protocol
/// @see `SMISerializable`
NS_SWIFT_NAME(AbstractSessionContext)
@interface SMIAbstractSessionContext : SMISerializable <SMIAbstractSessionContext>
@end

NS_ASSUME_NONNULL_END
