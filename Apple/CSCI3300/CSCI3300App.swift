//
//  CSCI3300App.swift
//  CSCI3300
//
//  Created by Daniel W on 8/16/24.
//

import SwiftUI

@main
struct CSCI3300App: App {
    let persistenceController = PersistenceController.shared

    var body: some Scene {
        WindowGroup {
            ContentView()
                .environment(\.managedObjectContext, persistenceController.container.viewContext)
        }
    }
}
