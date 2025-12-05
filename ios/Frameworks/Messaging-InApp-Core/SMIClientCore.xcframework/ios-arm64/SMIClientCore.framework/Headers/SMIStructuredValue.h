//
//  SMIStructuredValue.h
//  SMIClientCore
//
//  Created by Aaron Eisses on 2025-09-09.
//  Copyright © 2025 Salesforce. All rights reserved.
//

#import <Foundation/Foundation.h>
#import <SMIClientCore/SMIAbstractValue.h>
#import <SMIClientCore/SMISerializable.h>

NS_ASSUME_NONNULL_BEGIN

/// A structured value that extends AbstractValueType and contains a dictionary of key-value pairs.
///
/// @see `SMIAbstractValue`
///
/// ## Swift
///
/// ```swift
/// protocol StructuredValueProtocol: AbstractValueTypeProtocol
/// ```
NS_SWIFT_NAME(StructuredValueProtocol)
@protocol SMIStructuredValue <SMIAbstractValue>

/// Dictionary containing the structured value data.
@property (nonatomic, readonly, strong) NSDictionary<NSString *, id> *value;

@end

/// Concrete implementation of SMIStructuredValue protocol.
///
/// This class provides a structured value container that holds a dictionary of key-value pairs.
/// It extends SMISerializable to support serialization and implements the SMIStructuredValue protocol.
///
/// @see `SMIStructuredValue` protocol
/// @see `SMISerializable`
NS_SWIFT_NAME(StructuredValue)
@interface SMIStructuredValue : SMISerializable <SMIStructuredValue>

- (instancetype)init NS_UNAVAILABLE;

/// Initializes a new structured value with the provided dictionary.
///
/// @param value A dictionary containing the structured value data. Keys should be NSString objects.
/// @return An initialized SMIStructuredValue instance.
- (instancetype)initWithValue:(NSDictionary<NSString *, id> *)value NS_DESIGNATED_INITIALIZER;

@end

NS_ASSUME_NONNULL_END
