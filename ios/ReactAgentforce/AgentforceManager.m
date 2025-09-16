//
//  AgentforceManager.m
//  ReactAgentforce
//
//  Created by Claude Code
//

#import "AgentforceManager.h"
#import "ReactAgentforce-Swift.h"
#import <React/RCTLog.h>

@interface AgentforceManager()
@property (nonatomic, strong) AgentforceClientManager *agentforceClient;
@end

@implementation AgentforceManager

RCT_EXPORT_MODULE();

+ (BOOL)requiresMainQueueSetup
{
    return YES;
}

RCT_EXPORT_METHOD(initializeAgentforce:(NSDictionary *)config
                  resolver:(RCTPromiseResolveBlock)resolve
                  rejecter:(RCTPromiseRejectBlock)reject)
{
    dispatch_async(dispatch_get_main_queue(), ^{
        @try {
            NSString *agentId = config[@"agentId"];
            NSString *orgId = config[@"orgId"];
            NSString *endpoint = config[@"endpoint"];

            if (!agentId || !orgId || !endpoint) {
                reject(@"INVALID_CONFIG", @"Missing required configuration parameters", nil);
                return;
            }

            self.agentforceClient = [[AgentforceClientManager alloc] init];
            [self.agentforceClient initializeWithAgentId:agentId
                                                   orgId:orgId
                                                endpoint:endpoint
                                              completion:^(NSError * _Nullable error) {
                if (error) {
                    reject(@"INIT_ERROR", error.localizedDescription, error);
                } else {
                    resolve(@{@"success": @YES});
                }
            }];
        } @catch (NSException *exception) {
            reject(@"INIT_EXCEPTION", exception.reason, nil);
        }
    });
}

RCT_EXPORT_METHOD(presentAgentforceChatView:(NSString *)agentId
                  resolver:(RCTPromiseResolveBlock)resolve
                  rejecter:(RCTPromiseRejectBlock)reject)
{
    dispatch_async(dispatch_get_main_queue(), ^{
        @try {
            if (!self.agentforceClient) {
                reject(@"NOT_INITIALIZED", @"AgentforceClient not initialized. Call initializeAgentforce first.", nil);
                return;
            }

            [self.agentforceClient presentChatViewWithAgentId:agentId
                                                   completion:^(NSError * _Nullable error) {
                if (error) {
                    reject(@"PRESENT_ERROR", error.localizedDescription, error);
                } else {
                    resolve(@{@"success": @YES});
                }
            }];
        } @catch (NSException *exception) {
            reject(@"PRESENT_EXCEPTION", exception.reason, nil);
        }
    });
}

RCT_EXPORT_METHOD(dismissAgentforceChatView:(RCTPromiseResolveBlock)resolve
                  rejecter:(RCTPromiseRejectBlock)reject)
{
    dispatch_async(dispatch_get_main_queue(), ^{
        @try {
            if (!self.agentforceClient) {
                reject(@"NOT_INITIALIZED", @"AgentforceClient not initialized.", nil);
                return;
            }

            [self.agentforceClient dismissChatViewWithCompletion:^(NSError * _Nullable error) {
                if (error) {
                    reject(@"DISMISS_ERROR", error.localizedDescription, error);
                } else {
                    resolve(@{@"success": @YES});
                }
            }];
        } @catch (NSException *exception) {
            reject(@"DISMISS_EXCEPTION", exception.reason, nil);
        }
    });
}

@end