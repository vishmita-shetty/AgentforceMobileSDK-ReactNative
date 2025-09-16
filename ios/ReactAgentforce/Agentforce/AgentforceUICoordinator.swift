//
//  AgentforceUICoordinator.swift
//  ReactAgentforce
//
//  Created by Claude Code
//

import Foundation
import UIKit
import SwiftUI
import AgentforceSDK

/// Coordinates UI presentation and lifecycle for Agentforce chat views
class AgentforceUICoordinator {

    /// Currently presented chat view controller
    private var currentChatViewController: UIViewController?

    /// Agentforce UI delegate instance
    private var agentforceDelegate: AgentforceDelegate?

    /// Present the Agentforce chat view
    /// - Parameters:
    ///   - chatView: SwiftUI view containing the chat interface
    ///   - completion: Completion handler called after presentation
    func presentChatView<T: View>(_ chatView: T, completion: @escaping (Error?) -> Void) {
        DispatchQueue.main.async { [weak self] in
            if let windowScene = UIApplication.shared.connectedScenes.first as? UIWindowScene,
               let window = windowScene.windows.first,
               let rootViewController = window.rootViewController {

                let hostingController = UIHostingController(rootView: chatView)
                hostingController.modalPresentationStyle = .pageSheet

                self?.currentChatViewController = hostingController
                rootViewController.present(hostingController, animated: true) {
                    completion(nil)
                }
            } else {
                completion(NSError(domain: "AgentforceUICoordinator", code: 2, userInfo: [NSLocalizedDescriptionKey: "No root view controller found"]))
            }
        }
    }

    /// Dismiss the currently presented chat view
    /// - Parameter completion: Completion handler called after dismissal
    func dismissChatView(completion: @escaping (Error?) -> Void) {
        DispatchQueue.main.async { [weak self] in
            if let chatViewController = self?.currentChatViewController {
                chatViewController.dismiss(animated: true) {
                    self?.currentChatViewController = nil
                    self?.agentforceDelegate = nil
                    completion(nil)
                }
            } else {
                completion(nil)
            }
        }
    }

    /// Create and return a new AgentforceDelegate instance
    /// - Returns: Configured AgentforceDelegate
    func createDelegate() -> AgentforceDelegate {
        let delegate = AgentforceDelegate()
        self.agentforceDelegate = delegate
        return delegate
    }
}

/// Delegate implementation for handling Agentforce UI events
class AgentforceDelegate: AgentforceUIDelegate {

    /// Called when the Agentforce UI has loaded successfully
    func agentforceUIDidLoad() {
        // UI loaded successfully
        print("Agentforce UI loaded successfully")
    }

    /// Called when the Agentforce UI encounters an error
    /// - Parameter error: The error that occurred
    func agentforceUIDidEncounterError(_ error: Error) {
        // Handle UI errors if needed
        print("Agentforce UI error: \(error.localizedDescription)")
    }
}