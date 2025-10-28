/*
 Copyright (c) 2020-present, salesforce.com, inc. All rights reserved.

 Redistribution and use of this software in source and binary forms, with or without modification,
 are permitted provided that the following conditions are met:
 * Redistributions of source code must retain the above copyright notice, this list of conditions
 and the following disclaimer.
 * Redistributions in binary form must reproduce the above copyright notice, this list of
 conditions and the following disclaimer in the documentation and/or other materials provided
 with the distribution.
 * Neither the name of salesforce.com, inc. nor the names of its contributors may be used to
 endorse or promote products derived from this software without specific prior written
 permission of salesforce.com, inc.

 THIS SOFTWARE IS PROVIDED BY THE COPYRIGHT HOLDERS AND CONTRIBUTORS "AS IS" AND ANY EXPRESS OR
 IMPLIED WARRANTIES, INCLUDING, BUT NOT LIMITED TO, THE IMPLIED WARRANTIES OF MERCHANTABILITY AND
 FITNESS FOR A PARTICULAR PURPOSE ARE DISCLAIMED. IN NO EVENT SHALL THE COPYRIGHT OWNER OR
 CONTRIBUTORS BE LIABLE FOR ANY DIRECT, INDIRECT, INCIDENTAL, SPECIAL, EXEMPLARY, OR CONSEQUENTIAL
 DAMAGES (INCLUDING, BUT NOT LIMITED TO, PROCUREMENT OF SUBSTITUTE GOODS OR SERVICES; LOSS OF USE,
 DATA, OR PROFITS; OR BUSINESS INTERRUPTION) HOWEVER CAUSED AND ON ANY THEORY OF LIABILITY,
 WHETHER IN CONTRACT, STRICT LIABILITY, OR TORT (INCLUDING NEGLIGENCE OR OTHERWISE) ARISING IN ANY
 WAY OUT OF THE USE OF THIS SOFTWARE, EVEN IF ADVISED OF THE POSSIBILITY OF SUCH DAMAGE.
 */

import Foundation
import UIKit
import SwiftUI
import AgentforceSDK
import AgentforceService
import SalesforceSDKCore
import SalesforceReact
import SalesforceLogging
import SalesforceNetwork
import SalesforceNavigation
import SalesforceUser
import SalesforceCache

@objc public class AgentforceClientManager: NSObject {
    private var agentforceClient: AgentforceClient?
    private var uiCoordinator: AgentforceUICoordinator

    @objc public override init() {
        self.uiCoordinator = AgentforceUICoordinator()
        super.init()
    }

    @objc public func initialize(agentId: String, orgId: String, endpoint: String, completion: @escaping (Error?) -> Void) {
      // Create protocol implementations
      let credentialProvider = SalesforceCredentialProvider()
      let networkProvider = SalesforceNetworkProvider()
      let navigationService = SalesforceNavigationService()
      let logger = SalesforceLoggerService()

      // Create cache for data provider
      let cache = AgentforceCacheProvider()

      // Create our custom data provider
      let dataProvider = AgentforceDataProvider(
          cache: cache,
          network: networkProvider
      )

      // Create basic feature flag settings
      let featureFlagSettings = AgentforceFeatureFlagSettings()

      // Create configuration with all required parameters
      // Get the current Salesforce instance URL
      let instanceURL = UserAccountManager.shared.currentUserAccount?.credentials.instanceUrl?.absoluteString

      // Get current user
      guard let currentUser = UserAccountManager.shared.currentUserAccount else {
          fatalError("No authenticated user found")
      }

      let config = AgentforceConfiguration(
        user: User(
            userId: currentUser.credentials.userId ?? "",
            org: Org(id: currentUser.credentials.organizationId ?? ""),
            username: currentUser.credentials.userId ?? "",
            displayName: currentUser.idData.displayName ?? "",
            firstName: currentUser.idData.firstName,
            avatarURL: currentUser.idData.pictureUrl
        ),
          agentforceCopier: nil,
          enableDebugSettings: true,
          ignoreWelcomeMessage: false,
          forceConfigEndpoint: instanceURL!,
          dataProvider: dataProvider, // Now using our custom data provider!
          imageProvider: nil,
          instrumentationHandler: nil,
          agentforceFeatureFlagSettings: featureFlagSettings,
          salesforceNetwork: networkProvider,
          salesforceNavigation: navigationService,
          salesforceLogger: logger,
          ttsVoiceProvider: nil,
          speechRecognizer: nil,
          themeManager: AgentforceDefaultThemeManager()
      )
      
      // Initialize AgentforceClient
      self.agentforceClient = AgentforceClient(
          credentialProvider: credentialProvider,
          mode: .fullConfig(config)
      )

      completion(nil)
    }

    @MainActor @objc public func presentChatView(agentId: String, userContext: String?, completion: @escaping (Error?) -> Void) {
        guard let agentforceClient = self.agentforceClient else {
            completion(NSError(domain: "AgentforceManager", code: 1, userInfo: [NSLocalizedDescriptionKey: "AgentforceClient not initialized"]))
            return
        }

        do {
            // Start conversation
            let conversation = agentforceClient.startAgentforceConversation(forAgentId: agentId)

            // Set additional context if provided
            if let userContext = userContext, !userContext.isEmpty {
                Task {
                    do {
                        let recordIdContext = RecordIdContextVariable(userContext)
                        if let variable = recordIdContext.variable {
                            try await conversation.setAdditionalContext(context: [variable])
                            print("Successfully set record ID context: \(userContext)")
                        }
                    } catch {
                        print("Warning: Failed to set record ID context: \(error)")
                        // Continue even if setting context fails
                    }
                }
            }

            // Create delegate through UI coordinator
            let delegate = uiCoordinator.createDelegate()

            // Create chat view
            let chatView = try agentforceClient.createAgentforceChatView(
                conversation: conversation,
                delegate: delegate,
                onContainerClose: { [weak self] in
                    DispatchQueue.main.async {
                        self?.dismissChatView { _ in }
                    }
                }
            )

            // Present the chat view through UI coordinator
            uiCoordinator.presentChatView(chatView, completion: completion)
        } catch {
            completion(error)
        }
    }

    @objc public func dismissChatView(completion: @escaping (Error?) -> Void) {
        uiCoordinator.dismissChatView(completion: completion)
    }
}
