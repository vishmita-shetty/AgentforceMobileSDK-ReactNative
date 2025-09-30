//
//  SMIEntryFormatText.h
//  SMIClientCore
//
//  Created by Jeremy Wright on 2024-11-25.
//

#import <Foundation/Foundation.h>
#import <SMIClientCore/SMIEntryFormat.h>

NS_SWIFT_NAME(EntryFormatText)
@protocol SMIEntryFormatText <SMIEntryFormat>

@property (nonatomic, readonly, strong) NSString *text;

@end
