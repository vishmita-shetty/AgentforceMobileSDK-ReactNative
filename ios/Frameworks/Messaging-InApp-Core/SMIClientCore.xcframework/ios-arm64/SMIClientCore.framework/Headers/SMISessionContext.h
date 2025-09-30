//
//  SMISessionContext.h
//  SMIClientCore
//
//  Created by Aaron Eisses on 2025-09-09.
//  Copyright © 2025 Salesforce. All rights reserved.
//

#import <Foundation/Foundation.h>
#import <SMIClientCore/SMIEntryPayload.h>
#import <SMIClientCore/SMIAbstractSessionContext.h>
#import <SMIClientCore/SMIConversation.h>
#import <SMIClientCore/SMIParticipant.h>

NS_ASSUME_NONNULL_BEGIN

/// Represents a session context item that contains an abstractSessionContext.
///
/// @see `SMIEntryPayload`
/// @see `SMIAbstractSessionContext`
///
/// ## Swift
///
/// ```swift
/// protocol SessionContextProtocol
/// ```
NS_SWIFT_NAME(SessionContextProtocol)
@protocol SMISessionContext

/// Unique identifier for this session context item.
///
/// This identifier is used to distinguish between different session context instances
/// and is typically generated automatically when the context is created.
@property (nonatomic, readonly, strong) NSString *identifier;

/// The abstract session context data associated with this session context item.
///
/// This property contains the actual context data wrapped by this session context.
/// The context data can be any type that conforms to the SMIAbstractSessionContext protocol.
@property (nonatomic, readonly, strong) id<SMIAbstractSessionContext> sessionContext;

@end

/// Concrete implementation of SMISessionContext protocol.
///
/// This class represents a session context item that wraps an abstract session context
/// with a unique identifier. It provides a container for session-specific context data.
///
/// @see `SMISessionContext` protocol
/// @see `SMIAbstractSessionContext`
NS_SWIFT_NAME(SessionContext)
@interface SMISessionContext: NSObject <SMISessionContext>

/// Initializes a new session context with the provided abstract session context.
///
/// @param sessionContext The abstract session context to wrap. Must not be nil.
/// @return An initialized SMISessionContext instance with a generated identifier.
- (instancetype)initWithSessionContext:(id<SMIAbstractSessionContext>)sessionContext;

@end

NS_ASSUME_NONNULL_END
