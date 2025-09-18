//
//  AgentforceClientManager.swift
//  ReactAgentforce
//
//  Created by Claude Code
//

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
          agentforceConfiguration: config
      )

      completion(nil)
    }

    @MainActor @objc public func presentChatView(agentId: String, completion: @escaping (Error?) -> Void) {
        guard let agentforceClient = self.agentforceClient else {
            completion(NSError(domain: "AgentforceManager", code: 1, userInfo: [NSLocalizedDescriptionKey: "AgentforceClient not initialized"]))
            return
        }

        do {
            // Start conversation
            let conversation = agentforceClient.startAgentforceConversation(forAgentId: agentId)

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
