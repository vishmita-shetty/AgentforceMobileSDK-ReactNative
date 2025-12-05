//
//  SMIExperimentalConfiguration.h
//  SMIClientCore
//
//  Created by Jeremy Wright on 2025-09-25.
//

#import <Foundation/Foundation.h>

NS_ASSUME_NONNULL_BEGIN

/// This class stores configuration for experimental or beta features. It is important to note that all properties and features which are exposed in this class are subject to change.
/// Experimental features/APIs may be removed without warning at any time.
///
/// ## Swift
///
/// ```swift
/// protocol ExperimentalConfiguration
/// ```
NS_SWIFT_NAME(ExperimentalConfiguration)
@protocol SMIExperimentalConfiguration

// This property will enable Lightning Type experiences with Agentforce Agents.
// Note: This is only available with un authenticated deployments.
// Toggling this flag will not impact existing users or conversations and would require destroying the database cache for it to take effect.
@property (nonatomic, readwrite, assign) BOOL enableLightningTypes;

@end

NS_ASSUME_NONNULL_END
