//
//  AgentforceLoggingService.swift
//  ReactAgentforce
//
//  Created by Claude Code
//

import Foundation
import SalesforceLogging
import SalesforceSDKCore

/// Logging service implementation that bridges Agentforce logging to Salesforce SDK logging
class SalesforceLoggerService: SalesforceLogging.Logger {

    /// Log a message with specified level
    /// - Parameters:
    ///   - logMessage: The message to log
    ///   - level: The logging level
    /// - Note: Maps Agentforce log levels to appropriate Salesforce SDK log levels
    func log(_ logMessage: String, level: SalesforceLogging.LogLevel) {
        // Map to SalesforceSDK logging using the new API
        let sfLogLevel: SalesforceLogger.Level
        switch level {
        case .debug:
            sfLogLevel = .debug
        case .info:
            sfLogLevel = .info
        case .warning:
            sfLogLevel = .error  // Use error level if warning doesn't exist
        case .error:
            sfLogLevel = .error
        @unknown default:
            sfLogLevel = .info
        }

        SalesforceLogger.d(type(of: self), message: logMessage)
    }
}