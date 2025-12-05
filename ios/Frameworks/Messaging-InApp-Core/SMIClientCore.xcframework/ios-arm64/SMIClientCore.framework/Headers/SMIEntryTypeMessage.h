//
//  SMIEntryTypeMessage.h
//  SMIClientCore
//
//  Created by Nigel Brown on 2023-02-01.
//

#import <Foundation/Foundation.h>
#import <SMIClientCore/SMIMessageReason.h>
#import <SMIClientCore/SMIStreamingTokenValidation.h>

NS_ASSUME_NONNULL_BEGIN

/// This class provides additional details for the conversation entry when the entry type is `SMIConversationEntryTypesMessage`.
NS_SWIFT_NAME(EntryTypeMessage)
@protocol SMIEntryTypeMessage <NSObject>

/// The message reason for automated responses.
@property (nonatomic, readonly, strong) SMIMessageReason messageReason;

/// If this message was generated as part of a streamed response, this property can be used to determine if this final static response has been modified in any way from the original set of streamed tokens.
@property (nonatomic, readonly, assign) BOOL wasRevised;

@end

NS_ASSUME_NONNULL_END
