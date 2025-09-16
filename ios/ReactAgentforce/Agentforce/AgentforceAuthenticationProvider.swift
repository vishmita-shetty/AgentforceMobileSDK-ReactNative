//
//  AgentforceAuthenticationProvider.swift
//  ReactAgentforce
//
//  Created by Claude Code
//

import Foundation
import AgentforceService
import SalesforceSDKCore

/// Provides authentication credentials for Agentforce SDK using Salesforce authentication
class SalesforceCredentialProvider: AgentforceAuthCredentialProviding {

    /// Returns OAuth credentials from the currently authenticated Salesforce user
    /// - Returns: AgentforceAuthCredentials containing access token, org ID, and user ID
    /// - Note: Throws a fatal error if no authenticated user is found
    func getAuthCredentials() -> AgentforceService.AgentforceAuthCredentials {
        // Get current user from SalesforceSDK
        if let currentUser = UserAccountManager.shared.currentUserAccount,
           let accessToken = currentUser.credentials.accessToken,
           let orgId = currentUser.credentials.organizationId,
           let userId = currentUser.credentials.userId {
            return .OAuth(authToken: accessToken, orgId: orgId, userId: userId)
        }

        // Fallback - this should not happen in a properly authenticated app
        fatalError("No authenticated user found. Please ensure the user is logged in to Salesforce.")
    }
}