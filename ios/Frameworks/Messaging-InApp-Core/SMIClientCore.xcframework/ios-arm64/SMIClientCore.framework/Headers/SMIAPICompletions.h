//
//  SMIEndSessionCompletion.h
//  SMIClientCore
//
//  Created by Aaron Eisses on 10/29/24.
//

#import <Foundation/Foundation.h>
#import <SMIClientCore/SMIQueryCompletions.h>

NS_ASSUME_NONNULL_BEGIN

/// The completion block for returning the error result from the endSession call.
/// @param error if the endSesion call results in an error, it is returned here. An successful endSession call returns an nil for the error.
typedef void (^SMIEndSessionCompletion)(NSError * _Nullable error)
NS_SWIFT_NAME(EndSessionCompletion);

NS_ASSUME_NONNULL_END
