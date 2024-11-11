//
//  ZigZagApp.swift
//  ZigZag
//
//  Created by Daniel W on 8/26/24.
//

import SwiftUI
import SwiftData
import FirebaseCore
import FirebaseAuth


class AppDelegate: NSObject, UIApplicationDelegate {
    func application(_ application: UIApplication,
                     didFinishLaunchingWithOptions launchOptions: [UIApplication.LaunchOptionsKey : Any]? = nil) -> Bool {
        FirebaseApp.configure()
        
        // Set up Firebase authentication state listener
        Auth.auth().addStateDidChangeListener { _, user in
            NotificationCenter.default.post(name: .authStateDidChange, object: nil)
        }
        
        return true
    }
    // Register for remote notifications
    func application(_ application: UIApplication, didRegisterForRemoteNotificationsWithDeviceToken deviceToken: Data) {
        Auth.auth().setAPNSToken(deviceToken, type: .prod) // Use .prod for production
    }
    
    func application(_ application: UIApplication, didReceiveRemoteNotification userInfo: [AnyHashable: Any], fetchCompletionHandler completionHandler: @escaping (UIBackgroundFetchResult) -> Void) {
        if Auth.auth().canHandleNotification(userInfo) {
            completionHandler(.noData)
            return
        }
        // Handle other notifications if necessary
        completionHandler(.newData)
    }
}

@main
struct ZigZagApp: App {
    //Firebase setup
    @UIApplicationDelegateAdaptor(AppDelegate.self) var delegate
        
    var sharedModelContainer: ModelContainer = {
        let schema = Schema([
            Item.self,
        ])
        let modelConfiguration = ModelConfiguration(schema: schema, isStoredInMemoryOnly: false)
        
        do {
            return try ModelContainer(for: schema, configurations: [modelConfiguration])
        } catch {
            fatalError("Could not create ModelContainer: \(error)")
        }
    }()
    
    var body: some Scene {
        WindowGroup {
            ContentView()
                .environmentObject(FirebaseManager.shared) // Inject AuthManager as an environment object
        }
        .modelContainer(sharedModelContainer)
    }
}

import Foundation

extension Notification.Name {
    static let authStateDidChange = Notification.Name("authStateDidChange")
}
