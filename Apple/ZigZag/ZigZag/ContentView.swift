//
//  ContentView.swift
//  ZigZag
//
//  Created by Daniel W on 8/26/24.
//

import SwiftUI
import SwiftData
import FirebaseAuth

struct ContentView: View {
    //@AppStorage("isAuthenticated") private var isAuthenticated: Bool = false
    
    @Environment(\.modelContext) private var modelContext
    @Query private var items: [Item]
    
    @EnvironmentObject private var authManager: FirebaseManager
    
    var body: some View {
        if authManager.isAuthenticated {
            FeedView()
        } else {
            SignInView()
        }
        
    }
}

#Preview {
    ContentView()
        .environmentObject(FirebaseManager.shared)
        .modelContainer(for: Item.self, inMemory: true)
}
