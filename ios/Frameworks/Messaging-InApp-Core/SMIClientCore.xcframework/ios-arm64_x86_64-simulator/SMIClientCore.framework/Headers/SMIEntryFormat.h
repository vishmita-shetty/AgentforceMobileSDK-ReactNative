//
//  SMIEntryFormat.h
//  SMIClientCore
//
//  Created by Jeremy Wright on 2024-11-25.
//

#import <Foundation/Foundation.h>
#import <SMIClientCore/SMIConversationFormatTypes.h>

NS_SWIFT_NAME(EntryFormat)
@protocol SMIEntryFormat

@property (nonatomic, readonly, assign) SMIConversationFormatTypes type;

@end
