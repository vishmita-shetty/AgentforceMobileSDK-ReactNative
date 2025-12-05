//
//  SMITranscriptConfiguration.h
//  SMIClientCore
//
//  Created by Jeremy Wright on 2025-09-17.
//

#import <Foundation/Foundation.h>

NS_ASSUME_NONNULL_BEGIN

/// Protocol for defining configuration properties related to Transcription.
NS_SWIFT_NAME(TranscriptConfiguration)
@protocol SMITranscriptConfiguration <NSObject>

/// Determines whether transcription is currently enabled.
@property (nonatomic, readonly, assign) BOOL allowTranscriptDownload;

@end

NS_ASSUME_NONNULL_END
