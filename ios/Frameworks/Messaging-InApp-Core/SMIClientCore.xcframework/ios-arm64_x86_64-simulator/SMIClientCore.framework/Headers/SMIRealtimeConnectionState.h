//
//  SMIRealtimeConnectionState.h
//  SMIClientCore
//
//  Created by Jeremy Wright on 2024-08-09.
//

#import <Foundation/Foundation.h>

NS_ASSUME_NONNULL_BEGIN

/// The possible connection states for listening to realtime events.
/// * `SMIRealtimeConnectionStateClosed`
/// * `SMIRealtimeConnectionStateOpen`
/// * `SMIRealtimeConnectionStateConnecting`
NS_SWIFT_NAME(RealtimeConnectionState)
typedef NSString *SMIRealtimeConnectionState NS_TYPED_ENUM;

/// The realtime event router is not connected and will not receive events.
FOUNDATION_EXPORT SMIRealtimeConnectionState const SMIRealtimeConnectionStateClosed;


/// The realtime event router is fully connected and will receive remote events.
FOUNDATION_EXPORT SMIRealtimeConnectionState const SMIRealtimeConnectionStateOpen;

/// The realtime event router is currently attempting to connect.
/// events will not be received in this state.
FOUNDATION_EXPORT SMIRealtimeConnectionState const SMIRealtimeConnectionStateConnecting;

NS_ASSUME_NONNULL_END

