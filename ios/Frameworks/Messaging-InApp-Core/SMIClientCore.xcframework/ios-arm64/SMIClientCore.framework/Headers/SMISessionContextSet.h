//
//  SMISessionContextSet.h
//  SMIClientCore
//
//  Created by Aaron Eisses on 2025-09-09.
//  Copyright © 2025 Salesforce. All rights reserved.
//

#import <Foundation/Foundation.h>
#import <SMIClientCore/SMINamedValue.h>
#import <SMIClientCore/SMIAbstractSessionContext.h>

NS_ASSUME_NONNULL_BEGIN

/// Represents a session context item with a name and value.
///
/// @see `AbstractSessionContext`
/// @see `SMINamedValue`
///
/// ## Swift
///
/// ```swift
/// protocol SessionContextSetProtocol: AbstractSessionContextProtocol
/// ```
NS_SWIFT_NAME(SessionContextSetProtocol)
@protocol SMISessionContextSet <SMIAbstractSessionContext>

/// Array of named context variables that make up this session context set.
///
/// This property contains a collection of named value pairs that represent the context
/// variables for this session. Each element in the array conforms to the SMINamedValue
/// protocol and contains a name-value pair for a specific context variable.
@property (nonatomic, readonly, strong) NSArray<id<SMINamedValue>> *contextVariables;

@end

/// Concrete implementation of SMISessionContextSet protocol.
///
/// This class represents a collection of named context variables that form a session context set.
/// It extends SMIAbstractSessionContext and implements the SMISessionContextSet protocol.
///
/// @see `SMISessionContextSet` protocol
/// @see `SMIAbstractSessionContext`
/// @see `SMINamedValue`
NS_SWIFT_NAME(SessionContextSet)
@interface SMISessionContextSet : SMIAbstractSessionContext <SMISessionContextSet>
- (instancetype)init NS_UNAVAILABLE;

/// Initializes a new session context set with the provided context variables.
///
/// @param contextVariables An array of named values representing the context variables. Must not be nil.
/// @return An initialized SMISessionContextSet instance.
- (instancetype)initWithContextVariables:(NSArray<id<SMINamedValue>> *)contextVariables NS_DESIGNATED_INITIALIZER;

@end

NS_ASSUME_NONNULL_END
