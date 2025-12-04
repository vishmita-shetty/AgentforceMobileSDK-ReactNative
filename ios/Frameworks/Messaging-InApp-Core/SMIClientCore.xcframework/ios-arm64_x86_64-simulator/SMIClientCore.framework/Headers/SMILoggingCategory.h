//
//  SMILoggingCategory.h
//  SMIClientCore
//
//  Created by Nigel Brown on 2022-08-23.
//

#import <Foundation/Foundation.h>

NS_ASSUME_NONNULL_BEGIN

/// Logging category information.
typedef NSString *SMILoggingCategory NS_TYPED_ENUM
NS_SWIFT_NAME(LoggingCategory);

/// Logging message associated with general network operations.
FOUNDATION_EXPORT SMILoggingCategory const SMILoggingCategoryNetworking;

/// Logging message associated with core API operations.
FOUNDATION_EXPORT SMILoggingCategory const SMILoggingCategoryAPI;

/// Logging message associated with local database operations.
FOUNDATION_EXPORT SMILoggingCategory const SMILoggingCategoryDatabase;

/// Logging message associated with emitting delegate events.
FOUNDATION_EXPORT SMILoggingCategory const SMILoggingCategoryEvent;

/// Logging message associated with memory management.
FOUNDATION_EXPORT SMILoggingCategory const SMILoggingCategoryMemory;

/// Logging message associated with errors which are emitted to the consuming client.
FOUNDATION_EXPORT SMILoggingCategory const SMILoggingCategoryError;

/// Logging message associated with the realtime event listener connection.
FOUNDATION_EXPORT SMILoggingCategory const SMILoggingCategoryRealtimeConnection;

NS_ASSUME_NONNULL_END
