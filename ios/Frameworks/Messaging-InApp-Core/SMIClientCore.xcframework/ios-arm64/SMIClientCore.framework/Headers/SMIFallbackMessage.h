//
//  SMIFallbackMessage.h
//  SMIClientCore
//
//  Created by Aaron Eisses on 2024-11-13.
//

#import <Foundation/Foundation.h>

NS_ASSUME_NONNULL_BEGIN

/// The Terms and Condtions for prechat.
NS_SWIFT_NAME(FallbackMessage)
@protocol SMIFallbackMessage <NSObject>

/// The functional fallback message that will be displayed to the user
@property (nullable, nonatomic, readonly, strong) NSAttributedString *content;

/// Determines if functional fallback message is enabled
@property (nonatomic, readonly, assign) BOOL isEnabled;

@end

NS_ASSUME_NONNULL_END
