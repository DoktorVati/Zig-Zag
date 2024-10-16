//
//  NavigationManager.swift
//  ZigZag
//
//  Created by Daniel W on 10/16/24.
//

import Foundation
import SwiftUI

enum ZigZagDestination: Hashable {
    case createPost
}

class NavigationManager: ObservableObject {
    @Published var path = NavigationPath()

    // Method to navigate to different destinations
    func navigateTo(_ destination: ZigZagDestination) {
        path.append(destination)
    }

    // Method to pop back to the root (Home)
    func navigateBackToRoot() {
        path.removeLast(path.count)
    }

    // Method to pop back one step
    func navigateBack() {
        path.removeLast()
    }
}
