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

@objc public class AgentforceClientManager: NSObject {
    private var agentforceClient: AgentforceClient?
    private var currentChatViewController: UIViewController?
    private var agentforceDelegate: AgentforceDelegate?

    @objc public override init() {
        super.init()
        // Force protocol witness table generation at initialization
        _ = LoggerProtocolWitnessForcer.sharedInstance
    }

    @objc public func initialize(agentId: String, orgId: String, endpoint: String, completion: @escaping (Error?) -> Void) {
      // Create protocol implementations
      let credentialProvider = SalesforceCredentialProvider()
      let networkProvider = SalesforceNetworkProvider()
      let navigationService = SalesforceNavigationService()
      let logger = SalesforceLoggerService()

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
          dataProvider: nil,
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

            // Create and retain delegate
            self.agentforceDelegate = AgentforceDelegate()

            // Create chat view
            let chatView = try agentforceClient.createAgentforceChatView(
                conversation: conversation,
                delegate: self.agentforceDelegate!,
                onContainerClose: { [weak self] in
                    DispatchQueue.main.async {
                        self?.dismissChatView { _ in }
                    }
                }
            )

            // Present the chat view
            DispatchQueue.main.async {
                if let windowScene = UIApplication.shared.connectedScenes.first as? UIWindowScene,
                   let window = windowScene.windows.first,
                   let rootViewController = window.rootViewController {

                    // Present directly on root view controller (no more React Native modal)
                    let presentingViewController = rootViewController

                    let hostingController = UIHostingController(rootView: chatView)
                    hostingController.modalPresentationStyle = .pageSheet

                    self.currentChatViewController = hostingController
                    presentingViewController.present(hostingController, animated: true) {
                        completion(nil)
                    }
                } else {
                    completion(NSError(domain: "AgentforceManager", code: 2, userInfo: [NSLocalizedDescriptionKey: "No root view controller found"]))
                }
            }
        } catch {
            completion(error)
        }
    }

    @objc public func dismissChatView(completion: @escaping (Error?) -> Void) {
        DispatchQueue.main.async {
            if let chatViewController = self.currentChatViewController {
                chatViewController.dismiss(animated: true) {
                    self.currentChatViewController = nil
                    self.agentforceDelegate = nil
                    completion(nil)
                }
            } else {
                completion(nil)
            }
        }
    }
}

// MARK: - Protocol Implementations

class SalesforceCredentialProvider: AgentforceAuthCredentialProviding {
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

public enum NetworkErrors: Error {
    case noData
}

private extension URLRequest {
    var restRequestMethod: RestRequest.Method {
        switch httpMethod {
        case "DELETE":
            return RestRequest.Method.DELETE
        case "GET":
            return RestRequest.Method.GET
        case "POST":
            return RestRequest.Method.POST
        case "PUT":
            return RestRequest.Method.PUT
        case "PATCH":
            return RestRequest.Method.PATCH
        case "HEAD":
            return RestRequest.Method.HEAD
        default:
            fatalError("Unknown method")
        }
    }
}

class SalesforceNetworkProvider: SalesforceNetwork.Network {
    func data(for request: SalesforceNetwork.NetworkRequest) async throws -> (Data, URLResponse) {
      let restRequest = restRequest(request: request)

      return try await withCheckedThrowingContinuation { continuation in
          restAPI.send(request: restRequest) { result in
              switch result {
              case let .success(response):
                  if let data = try? result.get().asData() {
                      continuation.resume(returning: (data, response.urlResponse))
                  } else {
                      continuation.resume(throwing: NetworkErrors.noData)
                  }
              case let .failure(error):
                  continuation.resume(throwing: error)
              }
          }
      }
    }

  var restAPI: RestClient

  init(restClient: RestClient = RestClient.shared) {
      self.restAPI = restClient
  }

  func restRequest(request: NetworkRequest) -> RestRequest {
    let urlString = request.baseRequest.url!.absoluteString
    let restReq = RestRequest(method: request.baseRequest.restRequestMethod, path: urlString, queryParams: [:])
    restReq.requiresAuthentication = request.requiresAuthentication ?? true
    let contentType = request.baseRequest.value(forHTTPHeaderField: "Content-Type") ?? "application/json; charset=utf-8"
    if let body = request.baseRequest.httpBody {
        restReq.setCustomRequestBodyData(body, contentType: contentType)
    }
    if urlString.starts(with: "/") {
        restReq.endpoint = kSFDefaultRestEndpoint
    } else {
        restReq.endpoint = ""
    }
    if let headerFields = request.baseRequest.allHTTPHeaderFields {
        for (key, value) in headerFields {
            restReq.setHeaderValue(value, forHeaderName: key)
        }
    }
    return restReq
  }

}

class SalesforceNavigationService: SalesforceNavigation.Navigation {
    func go(to destination: any SalesforceNavigation.Destination) {
        // Default implementation - call the replace version with false
        go(to: destination, replace: false)
    }

    func go(to destination: any SalesforceNavigation.Destination, replace: Bool) {
        // Handle navigation to Salesforce records
        // This would typically open the record in the Salesforce app or web view
        print("Navigation to: \(destination), replace: \(replace)")

        // For now, just log the navigation request
        // In a real implementation, you might:
        // - Open Salesforce mobile app
        // - Open web view with the record
        // - Navigate within your app to show the record
    }
}

class SalesforceLoggerService: SalesforceLogging.Logger {
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

// IMPORTANT: Force protocol witness table generation by creating a static instance
// This ensures the protocol conformance is compiled into the app binary
@objc public class LoggerProtocolWitnessForcer: NSObject {
    @objc public static let sharedInstance = LoggerProtocolWitnessForcer()

    private override init() {
        super.init()
        // Force the protocol witness table to be generated
        let logger: SalesforceLogging.Logger = SalesforceLoggerService()
        logger.log("Protocol witness table forced", level: .debug)
    }
}

class AgentforceDelegate: AgentforceUIDelegate {
    // Implement required delegate methods
    func agentforceUIDidLoad() {
        // UI loaded successfully
    }

    func agentforceUIDidEncounterError(_ error: Error) {
        // Handle UI errors if needed
    }
}