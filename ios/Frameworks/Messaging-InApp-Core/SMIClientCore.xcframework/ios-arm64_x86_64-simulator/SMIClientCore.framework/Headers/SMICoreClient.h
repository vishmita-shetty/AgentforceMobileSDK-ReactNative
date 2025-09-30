//
//  SMICoreClient.h
//  SMIClientCore
//
//  Created by Jeremy Wright on 3/5/19.
//  Copyright © 2019 Salesforce. All rights reserved.
//

#import <Foundation/Foundation.h>

#import <SMIClientCore/SMICoreDelegate.h>
#import <SMIClientCore/SMIRemoteConfiguration.h>
#import <SMIClientCore/SMIBusinessHoursInfo.h>
#import <SMIClientCore/SMIDataTypes.h>
#import <SMIClientCore/SMIHiddenPreChatDelegate.h>
#import <SMIClientCore/SMIUserVerificationDelegate.h>
#import <SMIClientCore/SMITemplatedUrlDelegate.h>
#import <SMIClientCore/SMIAPICompletions.h>
#import <SMIClientCore/SMIListAPIDirections.h>
#import <SMIClientCore/SMIRealtimeConnectionState.h>

@protocol SMIConversationClient;
@class SMICoreConfiguration;

NS_ASSUME_NONNULL_BEGIN

/// Main entry point for the In-App Messaging core API. To create an instance of this class, use the `SMICoreFactory` class and provide an `SMICoreConfiguration ` object.
NS_SWIFT_NAME(CoreClient)
@protocol SMICoreClient

/// Configuration used to determine where to send and receive events for the In-App Messaging service.
@property (nonatomic, readonly, copy) SMICoreConfiguration *configuration;

/// Current network connectivity state.
@property (nonatomic, readonly, strong) SMINetworkConnectivityState state;

/// Current realtime connection state.
@property (nonatomic, readonly, strong) SMIRealtimeConnectionState realtimeConnectionState;

/// The delegate that provides hidden pre-chat field values.
@property (nonatomic, readwrite, weak) id<SMIHiddenPreChatDelegate> preChatDelegate;

/// The delegate that provides the auto-response messaging component parameter values.
@property (nonatomic, readwrite, weak) id<SMITemplatedUrlDelegate> templatedUrlDelegate;

/// The delegate that provides the challenge for the identity token for user verified connections.
@property (nonatomic, readwrite, weak) id<SMIUserVerificationDelegate> userVerificationDelegate;

/// Generates an instance of a `SMIConversationClient` object which is used to manage all conversation data.
/// @param conversationId The conversation ID associated with this `SMIConversationClient` instance. If `nil`, a UUID is used.
- (id<SMIConversationClient>)conversationClientWithId:(nullable NSUUID *)conversationId;

/// Initiates the connection to listen to realtime remote events.
/// Once executed the `SMICoreClient` will aggressively attempt to maintain this connection at all times.
/// @see `-realtimeConnectionState`
- (void)start;

/// Terminates the connection listening for realtime remote events.
/// @see `-realtimeConnectionState`
- (void)stop;

/// Retrieves the current remote configuration if available. Defaults to offline backups if the current configuration cannot be retrieved.
/// @param completion Contains a reference to an `SMIRemoteConfiguration` object if successful, or an `NSError` describing the failure.
- (void)retrieveRemoteConfigurationWithCompletion:(SMIRemoteConfigurationCompletion)completion;

/// Retrieves the business hour info for the current deployment.
/// @param completion Contains a reference to an `SMIBusinessHoursInfoCompletion` object if successful, or an `NSError` describing the failure.
- (void)retrieveBusinessHoursWithCompletion:(SMIBusinessHoursInfoCompletion)completion;

/// Retrieves conversations available to the local participant.
/// @param limit The maximum number of results to expect.
/// @param olderThanConversation The comparison conversation which serves as a base for returning older conversations. If `nil`, the query starts from the newest known conversation.
/// @param completion The completion that contains the result of the search query.
- (void)conversationsWithLimit:(NSUInteger)limit
         olderThanConversation:(nullable id<SMIConversation>)olderThanConversation
                    completion:(SMIConversationQueryCompletion)completion;

/// Retrieves conversations available to the local participant.
/// @param limit The maximum number of results to expect.
/// @param olderThanConversation The comparison conversation which serves as a base for returning older conversations. If `nil`, the query starts from the newest known conversation.
/// @param forceRefresh forces the conversation list to be updated from the network bypassing the local cache.
/// @param completion The completion that contains the result of the search query.
- (void)conversationsWithLimit:(NSUInteger)limit
         olderThanConversation:(nullable id<SMIConversation>)olderThanConversation
                  forceRefresh:(BOOL)forceRefresh
                    completion:(SMIConversationQueryCompletion)completion;

/// Retrieves n number of conversations from offline cached data sorted by latest activity. This only returns an order based on offline cached data, and
/// may not reflect the real-time state of conversations if the client has not retrieved that activity.
/// @param limit The maximum number of conversations to return in the query.
/// @param direction The direction based on last activity. Default value is `SMIListAPIDirectionsDescending`.
/// @param completion The completion that contains the result of the search query.
- (void)conversationsWithLimit:(NSUInteger)limit
     sortedByActivityDirection:(nullable SMIListAPIDirections)direction
                    completion:(SMIConversationQueryCompletion)completion;

/// Marks a conversation entry as read.
/// @note Only messages from remote participants should be provided.
/// If the participant on the entry is local, this results in a no-op.
/// @param entry The entry to mark as read.
- (void)markAsRead:(id<SMIConversationEntry>)entry NS_SWIFT_NAME( markAsRead(entry:) );

/// Attempts to retry a message delivery. If the message status is not `SMIConversationEntryStatusError`, the entry is ignored.
/// @param entry The message to attempt to retry.
- (void)retryEntry:(id<SMIConversationEntry>)entry;

/// Attempts to retry a message delivery and submit pre-chat fields with the entry. If the message status is not `SMIConversationEntryStatusError`, the entry is ignored.
/// @param entry The message to attempt to retry.
/// @param remoteConfig The remote configuration fields.
- (void)retryEntry:(id<SMIConversationEntry>)entry remoteConfiguration:(id<SMIRemoteConfiguration> _Nullable)remoteConfig NS_SWIFT_NAME( retry(entry:remoteConfiguration:) );

/// Attempts to retry a message delivery and submit pre-chat fields with the entry. If the message status is not `SMIConversationEntryStatusError`, the entry is ignored.
/// @param entry The message to attempt to retry.
/// @param preChatFields The visible pre-chat fields to be resubmitted.
/// @param hiddenPreChatFields The hidden fields to be resubmitted.
- (void)retryEntry:(id<SMIConversationEntry>)entry preChatFields:(nullable NSArray<id<SMIPreChatField>> *)preChatFields hiddenPreChatFields:(nullable NSArray<id<SMIHiddenPreChatField>> *)hiddenPreChatFields
__deprecated_msg("Will be removed in a future version. Use `retryEntry:remoteConfiguration:`");

/// Adds a delegate to listen to `SMICoreDelegate` events.
/// @param delegate The delegate to add.
- (void)addDelegate:(id<SMICoreDelegate>)delegate NS_SWIFT_NAME( addDelegate(delegate:) );

/// Adds a delegate to listen to `SMICoreDelegate` events.
/// It is recommended that the main queue is passed to this method if you plan to handle events directly with changes to the UI.
/// @param delegate The delegate to add.
/// @param queue The dispatch queue to listen for events.
- (void)addDelegate:(id<SMICoreDelegate>)delegate queue:(dispatch_queue_t)queue NS_SWIFT_NAME( addDelegate(delegate:queue:) );

/// Sets the templatedUrlDelegate while also defining the dispatch queue that events fired to this delegate will be emitted to.
/// It is recommended that the main queue is passed to this method if you plan to handle events directly with changes to the UI.
/// @param templatedUrlDelegate The delegate to add.
/// @param queue The dispatch queue to listen for events.
- (void)setTemplatedUrlDelegate:(id<SMITemplatedUrlDelegate>)templatedUrlDelegate
                          queue:(dispatch_queue_t)queue NS_SWIFT_NAME (setTemplatedUrlDelegate(delegate:queue:));

/// Sets the preChatDelegate while also defining the dispatch queue that events fired to this delegate will be emitted to.
/// It is recommended that the main queue is passed to this method if you plan to handle events directly with changes to the UI.
/// @param preChatDelegate The delegate to add.
/// @param queue The dispatch queue to listen for events.
- (void)setPreChatDelegate:(id<SMIHiddenPreChatDelegate> _Nullable)preChatDelegate
                     queue:(dispatch_queue_t)queue NS_SWIFT_NAME (setPreChatDelegate(delegate:queue:));

/// Sets the userVerificationDelegate while also defining the dispatch queue that events fired to this delegate will be emitted to.
/// It is recommended that the main queue is passed to this method if you plan to handle events directly with changes to the UI.
/// @param userVerificationDelegate The delegate to add.
/// @param queue The dispatch queue to listen for events.
- (void)setUserVerificationDelegate:(id<SMIUserVerificationDelegate>)userVerificationDelegate
                              queue:(dispatch_queue_t)queue NS_SWIFT_NAME (setUserVerificationDelegate(delegate:queue:));

/// Removes the delegate to stop receiving events.
/// @param delegate The delegate to remove.
- (void)removeDelegate:(id<SMICoreDelegate>)delegate NS_SWIFT_NAME( removeDelegate(delegate:) );

/// Deletes the In-App Message store.
/// This will reset all conversation as well as authorization credentials.
/// By default this will also clear all auth credentials.
/// @param completion Completion block that returns with the success or failure of the operation.
- (void)destroyStorage:(SMIDataOperationCompletion)completion
__deprecated_msg("Will be removed in a future version. Use `destroyStorageAndAuthorization:completion:`");

/// Deletes the In-App Message store.
/// This will reset all conversation as well as authorization credentials.
/// By default this will also clear all auth credentials.
/// @param destroyAuthorization determines whether the authorization store will be cleared with this operation.
/// @param completion completion block that returns with the success or failure of the operation.
- (void)destroyStorageAndAuthorization:(BOOL)destroyAuthorization
                            completion:(SMIDataOperationCompletion)completion NS_SWIFT_NAME( destroyStorage(andAuthorization:completion:) );

/// De-registers the device from receiving push notifications.
/// After this command has been executed, to receive push notifications again, the device will need to be registered again by calling
/// `-provideDeviceToken:` on the `CoreFactory`.
/// @param completion  Returns the result of the operation. If error is nil, the request is successful.
- (void)deregisterDeviceWithCompletion:(nullable SMIDataOperationCompletion)completion;

/// Registers the device to receive push notifcations.
/// @param deviceToken The token used to register the device.
/// @param completion Returns the result of the operation. If error is nil, the request is successful.
- (void)registerDevice:(NSString *)deviceToken completion:(nullable SMIDataOperationCompletion)completion NS_SWIFT_NAME( register(deviceToken:completion:) );

/// Revokes the authorization credentials associated with this configuration and optionally de-registers the device from receiving push notifications.
/// @param deregisterDevice Whether or not to also de-register device from push notifications.
/// @param completion Returns the result of the operation. If error is nil, the request is successful.
- (void)revokeTokenAndDeregisterDevice:(BOOL)deregisterDevice completion:(nullable SMIDataOperationCompletion)completion;

/// Revokes the authorization credentials associated with this configuration and de-registers the device from receiving push notifications.
/// @note This method automatically de-registers the device from receiving push notifications. If you don't want to de-register the device,
/// use `-revokeTokenAndDeregisterDevice:completion:` and set `deregisterDevice` to `false`.
/// @param completion Returns the result of the operation. If error is nil, the request is successful.
- (void)revokeTokenWithCompletion:(nullable SMIDataOperationCompletion)completion;

@end

NS_ASSUME_NONNULL_END
