//
//  ProfileIcon.swift
//  ZigZag
//
//  Created by Daniel W on 11/8/24.
//

import SwiftUI

struct ProfileIcon: View {
    
    var body: some View {
        Circle()
            .fill(
                LinearGradient(
                    gradient: Gradient(colors: [.cyan, .blue]),
                    startPoint: .topLeading,
                    endPoint: .bottomTrailing
                )
            )
            .frame(width: 45, height: 45)
            .overlay {
                Image(systemName: "person.fill")
                    .foregroundStyle(.white)
                    .frame(width: 50, height: 50)
            }
    }
}
