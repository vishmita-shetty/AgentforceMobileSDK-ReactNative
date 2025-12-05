//
//  SMIConversationClient.h
//  SMIClientCore
//
//  Created by Jeremy Wright on 2021-05-26.
//

#import <Foundation/Foundation.h>
#import <UIKit/UIKit.h>
#import <PDFKit/PDFKit.h>

#import <SMIClientCore/SMIConversationClientDelegate.h>
#import <SMIClientCore/SMIAPICompletions.h>
#import <SMIClientCore/SMIDataTypes.h>
#import <SMIClientCore/SMINetworkMimeTypes.h>
#import <SMIClientCore/SMIEntryTypeFormMessage.h>
#import <SMIClientCore/SMIListAPIDirections.h>
#import <SMIClientCore/SMIQueryBehaviour.h>
#import <SMIClientCore/SMIRemoteConfiguration.h>
#import <SMIClientCore/SMIConversationRequestCompletion.h>
#import <SMIClientCore/SMISessionContext.h>

@protocol SMIPreChatField;
@protocol SMIHiddenPreChatField;
@protocol SMICoreClient;
@protocol SMIAbstractChoice;

NS_ASSUME_NONNULL_BEGIN

/// Class that allows you to perform actions related to a conversation and to listen
/// for events using the associated delegate.
///
/// @see `SMIConversationClientDelegate`
NS_SWIFT_NAME(ConversationClient)
@protocol SMIConversationClient

/// The identifier for the conversation managed by this client.
@property (nonatomic, readonly, strong) NSUUID *identifier;

/// Reference to the core object which manages this client.
@property (nonatomic, readonly, weak) id<SMICoreClient> core;

/// Sends a message with the contents of the message parameter.
///
/// @param message The message to send.
- (void)sendMessage:(NSString *)message NS_SWIFT_NAME( send(message:) );

/// Sends an image with the contents of the image parameter.
///
/// @param image The image to send.
/// @see `-sendImage:fileName:`
- (void)sendImage:(UIImage *)image NS_SWIFT_NAME( send(image:) ) __deprecated_msg("Will be removed in a future version. Use `sendImage:fileName:message`");

/// Sends an image with the contents of the data parameter.
///
/// @param data The data representation of an image.
/// @param fileName The file name, if one exists, for the image that is being sent.
- (void)sendImage:(NSData *)data fileName:(NSString * _Nullable)fileName NS_SWIFT_NAME( send(image:fileName:))
__deprecated_msg("Will be removed in a future version. Use `sendImage:fileName:message:`");

/// Sends an PDF attachment.
///
/// @param pdf The PDF document to send.
- (void)sendPDF:(PDFDocument *)pdf NS_SWIFT_NAME( send(pdf:) ) __deprecated_msg("Will be removed in a future version. Use `sendImage:fileName:message:`");

/// Sends a file attachment
///
/// @param data The file document to send.
/// @param fileName the of the document to send.
- (void)sendFile:(NSData *)data fileName:(NSString * _Nullable)fileName NS_SWIFT_NAME( send(file:fileName:) )
__deprecated_msg("Will be removed in a future version. Use `sendImage:fileName:message:`");

/// Sends a file attachment
///
/// @param data the file document to send.
/// @param fileName the fileName of the document to send.
/// @param message the message to be sent with the file.
- (void)sendFile:(NSData *)data fileName:(NSString * _Nullable)fileName message:(NSString * _Nullable)message NS_SWIFT_NAME( send(file:fileName:message:) );

/// Sends a reply to a structured content message.
///
/// @param reply The selected option for the structured content.
- (void)sendReply:(id<SMIAbstractChoice>)reply NS_SWIFT_NAME( send(reply:) );

/// Sends a cancel action referencing a conversation entry.
///
/// @param entry The conversation entry which is used to provide the unique identifier for the cancel action.
- (void)sendCancelForEntry:(id<SMIConversationEntry>)entry NS_SWIFT_NAME( sendCancel(entry:) );

/// Sets the session context that will be sent with the next message.
///
/// @param sessionContext An array of session context items to be sent with the next message.
/// @note The context is stored in memory only and will be cleared when the message is successfully sent.
- (void)setSessionContext:(nullable id<SMISessionContext>)sessionContext NS_SWIFT_NAME( set(sessionContext:) );

/// Attempts to send a typing indicator.
///
/// @note There's a predefined threshold for how often typing indicators are sent. It's safe to call this method
/// as often as needed. Attempts to send an indicator too frequently are ignored.
- (void)sendTypingEvent;

/// End the current session.
///
/// @note sending this command will end the session on the current conversation.
/// @param completion The completion that will contain an error if an error is return from the endSession call..
- (void)endSessionWithCompletion:(SMIEndSessionCompletion)completion;

/// Sends a filled out Form.
///
/// @param form The Form to send.
/// @see SMIEntryTypeFormMessage
- (void)sendForm:(id<SMIEntryTypeFormMessage>)form NS_SWIFT_NAME( send(form:) );

/// Retrieves the conversation represented by the client.
/// @param completion The completion that contains the stored conversation record.
- (void)conversationWithCompletion:(SMISingleConversationQueryCompletion)completion;

/// Submits collected pre-chat fields and creates a conversation if required.
///
/// @param preChatFields An array of `SMIPreChatField` objects representing collected pre-chat data.
- (void)submitPreChatFields:(NSArray<id<SMIPreChatField>> *)preChatFields NS_SWIFT_NAME( submit(preChatFields:) )
__deprecated_msg("Will be removed in a future version. Use `submitRemoteConfiguration:`");

/// Submits collected pre-chat fields and creates a conversation if required.
///
/// @param preChatFields An array of `SMIPreChatField` objects representing collected pre-chat data.
/// @param hiddenPreChatFields An array of `SMIHiddenPreChatField` objects representing hidden prechat data.
- (void)submitPreChatFields:(NSArray<id<SMIPreChatField>> *)preChatFields
        hiddenPreChatFields:(NSArray<id<SMIHiddenPreChatField>> *)hiddenPreChatFields NS_SWIFT_NAME( submit(preChatFields:hiddenPreChatFields:) )
__deprecated_msg("Will be removed in a future version. Use `submitRemoteConfiguration:`");

/// Submits collected pre-chat fields and starts the conversation if `createConversationOnSubmit` is true.
/// Otherwise, the SDK stores the pre-chat data until the conversation is created.
///
/// @param preChatFields An array of `SMIPreChatField` objects representing collected pre-chat data.
/// @param hiddenPreChatFields An array of `SMIHiddenPreChatField` objects representing any hidden pre-chat data.
/// @param createConversationOnSubmit Determines if the conversation starts when the pre-chat data is submitted.
- (void)submitPreChatFields:(NSArray<id<SMIPreChatField>> *)preChatFields
        hiddenPreChatFields:(NSArray<id<SMIHiddenPreChatField>> *)hiddenPreChatFields
 createConversationOnSubmit:(BOOL)createConversationOnSubmit NS_SWIFT_NAME( submit(preChatFields:hiddenPreChatFields:createConversationOnSubmit:) )
__deprecated_msg("Will be removed in a future version. Use `submitRemoteConfiguration:createConversationOnSubmit:`");

/// Submits the remote configuration values.
///
/// @param remoteConfig The remote configuration fields.
- (void)submitRemoteConfiguration:(id<SMIRemoteConfiguration>)remoteConfig NS_SWIFT_NAME( submit(remoteConfig:) );

/// Submits the remote configuration values and starts the conversation if `createConversationOnSubmit` is true.
/// Otherwise, the SDK stores the pre-chat data until the conversation is created.
///
/// @param remoteConfig The remote configuration fields.
/// @param createConversationOnSubmit Determines if the conversation starts when the pre-chat data is submitted.
- (void)submitRemoteConfiguration:(id<SMIRemoteConfiguration>)remoteConfig
       createConversationOnSubmit:(BOOL)createConversationOnSubmit NS_SWIFT_NAME( submit(remoteConfig:createConversationOnSubmit:) );

/// Creates a conversation with the remote configuration.
///
/// @param remoteConfig A `RemoteConfiguration` object for routing data.
- (void)createConversation:(id<SMIRemoteConfiguration>)remoteConfig NS_SWIFT_NAME( create(remoteConfig:) );

/// Creates a conversation.
- (void)createConversation NS_SWIFT_NAME( create() );

/// Retrieves messages for this conversation. Only returns the first subset of messages.
/// This is a convenience method to make direct database calls for entries associated with this conversation.
/// @param completion The completion that contains the result of the search query.
- (void)entriesWithCompletion:(SMIFullQueryCompletion)completion
__deprecated_msg("Will be removed in a future version. Use `entriesWithLimit:fromTimestamp:direction:completion:`");

/// Retrieves entries for this conversation. This is a convenience method to make direct database calls for entries associated with this conversation.
///
/// @param limit The maximum number of results to expect.
/// @param offset The offset to begin searching for results.
/// @param completion The completion that contains the result of the search query.
/// @see `-entriesWithLimit:olderThanEntry:completion:`
- (void)entriesWithLimit:(NSUInteger)limit offset:(NSUInteger)offset completion:(SMIFullQueryCompletion)completion
__deprecated_msg("Will be removed in a future version. Use `entriesWithLimit:fromTimestamp:direction:completion:`");

/// Retrieves entries for this conversation. This is a convenience method to make direct database calls for entries associated with this conversation.
///
/// @param limit The maximum number of results to expect.
/// @param olderThanEntry The comparison entry which serves as a base for returning older entries in the conversation. If `nil`, the query starts from the newest known entry.
/// @param completion The completion that contains the result of the search query.
- (void)entriesWithLimit:(NSUInteger)limit olderThanEntry:(nullable id<SMIConversationEntry>)olderThanEntry completion:(SMIFullQueryCompletion)completion
__deprecated_msg("Will be removed in a future version. Use `entriesWithLimit:fromTimestamp:direction:completion:`");

/// Retrieves entries for this conversation. This is a convenience method to make direct database calls for entries associated with this conversation.
///
/// @param limit The maximum number of results to expect.
/// @param fromTimestamp A timestamp to use as a point of comparison for conversation entry queries.
/// @param direction The direction of the query.
/// @param behaviour Defines how the query will behave. Default: `SMIQueryBehaviourLocalWithNetwork`
/// @param completion The completion that contains the result of the search query.
- (void)entriesWithLimit:(NSUInteger)limit
           fromTimestamp:(nullable NSDate *)fromTimestamp
               direction:(SMIListAPIDirections)direction
               behaviour:(SMIQueryBehaviour)behaviour
              completion:(void (^)(NSArray<id<SMIConversationEntry>> * _Nullable messages,
                           id<SMIConversation> _Nullable conversation,
                           NSError * _Nullable error))completion;

/// Retrieves entries for this conversation. This is a convenience method to make direct database calls for entries associated with this conversation.
///
/// @param limit The maximum number of results to expect.
/// @param fromTimestamp A timestamp to use as a point of comparison for conversation entry queries.
/// @param direction The direction of the query.
/// @param completion The completion that contains the result of the search query.
- (void)entriesWithLimit:(NSUInteger)limit
           fromTimestamp:(nullable NSDate *)fromTimestamp
               direction:(SMIListAPIDirections)direction
              completion:(SMIFullQueryCompletion)completion;

/// Returns the most recent verified entry.
/// An entry is considered verified if the entry in the local DB cache has been verified against the conversation transcript in the network service.
///
/// @param completion The completion that contains the result of the search query.
- (void)latestVerifiedEntryWithCompletion:(SMIFullQueryCompletion)completion
__deprecated_msg("Will be removed in a future version. Use `fetchLatestVerifiedEntryWithCompletion:`");

/// Returns the most recent verified entry.
/// An entry is considered verified if the entry in the local DB cache has been verified against the conversation transcript in the network service.
///
/// @param completion The completion that contains the result of the search query.
- (void)fetchLatestVerifiedEntryWithCompletion:(SMISingleEntryQueryCompletion)completion;

/// Attempt to download a transcript of the converation.
///
/// @param completion The completion of the transcript request with a PDF document of the conversation transcript.
- (void)retrieveTranscript:(SMITranscriptRequestCompletion)completion;

/// Adds a delegate to listen to `SMIConversationClientDelegate` events.
///
/// @param delegate The delegate to add.
- (void)addDelegate:(id<SMIConversationClientDelegate>)delegate NS_SWIFT_NAME( addDelegate(delegate:) );

/// Adds a delegate to listen to `SMIConversationClientDelegate` events.
/// It is recommended that the main queue is passed to this method if you plan to handle events directly with changes to the UI.
///
/// @param delegate The delegate to add.
/// @param queue The dispatch queue to listen for events.
- (void)addDelegate:(id<SMIConversationClientDelegate>)delegate queue:(dispatch_queue_t)queue NS_SWIFT_NAME( addDelegate(delegate:queue:) );

/// Removes the delegate to stop receiving events.
///
/// @param delegate The delegate to remove.
- (void)removeDelegate:(id<SMIConversationClientDelegate>)delegate NS_SWIFT_NAME( removeDelegate(delegate:) );

@end

NS_ASSUME_NONNULL_END
