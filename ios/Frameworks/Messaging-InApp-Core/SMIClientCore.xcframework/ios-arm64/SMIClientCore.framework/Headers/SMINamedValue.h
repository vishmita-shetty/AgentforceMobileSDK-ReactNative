//
//  SMINamedValue.h
//  SMIClientCore
//
//  Created by Aaron Eisses on 2025-09-09.
//  Copyright © 2025 Salesforce. All rights reserved.
//

#import <Foundation/Foundation.h>
#import <SMIClientCore/SMIAbstractValue.h>
#import <SMIClientCore/SMISerializable.h>

NS_ASSUME_NONNULL_BEGIN

/// Represents a named value variable item with a name and value.
///
/// @see `SMIValue`
///
/// ## Swift
///
/// ```swift
/// protocol NamedValueProtocol
/// ```
NS_SWIFT_NAME(NamedValueProtocol)
@protocol SMINamedValue

/// The name of the context variable.
@property (nonatomic, readonly, strong) NSString *name;

/// The value of the context variable.
@property (nonatomic, readonly, strong) id<SMIAbstractValue> value;

@end

/// Concrete implementation of SMINamedValue protocol.
///
/// This class represents a named value pair containing a string name and an abstract value.
/// It extends SMISerializable to support serialization and implements the SMINamedValue protocol.
///
/// @see `SMINamedValue` protocol
/// @see `SMIAbstractValue`
/// @see `SMISerializable`
NS_SWIFT_NAME(NamedValue)
@interface SMINamedValue : SMISerializable <SMINamedValue>
- (instancetype)init NS_UNAVAILABLE;

/// Initializes a new named value with the provided name and value.
///
/// @param name The name of the context variable. Must not be nil.
/// @param value The abstract value associated with the name. Must not be nil.
/// @return An initialized SMINamedValue instance.
- (instancetype)initWithName:(NSString *)name value:(id<SMIAbstractValue>)value NS_DESIGNATED_INITIALIZER;

@end

NS_ASSUME_NONNULL_END
