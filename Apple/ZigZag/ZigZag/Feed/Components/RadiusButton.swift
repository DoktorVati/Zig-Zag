//
//  RadiusButton.swift
//  ZigZag
//
//  Created by Daniel W on 10/9/24.
//

import SwiftUI

struct RadiusButton: View {
    @EnvironmentObject var viewModel: FeedViewModel
    
    var icon: String
    var myIndex: Int
    var action: () -> Void
    
    var isPressed: Bool {
        if viewModel.selectedRadiusIndex == myIndex {
            return true
        } else {
            return false
        }
    }
    var body: some View {
        Button {
            action()
        } label: {
            Image(systemName: icon)
                .foregroundStyle(isPressed ? Color(.white) : Color(UIColor.label))
                .padding()
//                .background(
//                    RoundedRectangle(cornerRadius: CGFloat(20))
//                        .foregroundStyle(isPressed ? Color.blue : Color(UIColor.systemBackground))
//                )
        }

    }
}

#Preview {
    RadiusButton(icon: "globe.americas", myIndex: 0) { }
        .environmentObject(FeedViewModel())
}
