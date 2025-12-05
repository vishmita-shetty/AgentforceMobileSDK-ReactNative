//
//  SMIItem.h
//  SMIClientCore
//
//  Created by Aaron Eisses on 2021-07-09.
//

#import <Foundation/Foundation.h>
#import <SMIClientCore/SMIAbstractChoice.h>

NS_ASSUME_NONNULL_BEGIN

/// A response type that represents a specific choice for text based choice messages.
NS_SWIFT_NAME(Choice)
@protocol SMIChoice <SMIAbstractChoice>

/// The title of this choice.
@property (nonatomic, readonly, strong) NSString *title;

/// The string for this choice that will be displayed in the chat feed.
@property (nonatomic, readonly, strong) NSString *optionValue;

@end

NS_ASSUME_NONNULL_END
