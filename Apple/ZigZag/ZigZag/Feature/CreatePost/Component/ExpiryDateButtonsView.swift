//
//  ExpiryDateButtonsView.swift
//  ZigZag
//
//  Created by Daniel W on 10/30/24.
//

import SwiftUI

enum ExpiryDate: String, CaseIterable, Identifiable {
    case thirtyMinutes = "30 min"
    case oneHour = "1 hrs"
    case oneDay = "1 day"
    
    var id: String { self.rawValue } // Conform to Identifiable using rawValue as id
    
    var formattedDate: String {
        let currentDate = Date()
        switch self {
        case .thirtyMinutes:
            return currentDate.addingTimeInterval(1800).ISO8601Format()  // 30 minutes
        case .oneHour:
            return currentDate.addingTimeInterval(3600).ISO8601Format()  // 1 hour
        case .oneDay:
            return currentDate.addingTimeInterval(86400).ISO8601Format()  // 1 day
        }
    }
}

struct ExpiryDateButtonsView: View {
    @EnvironmentObject var viewModel: CreatePostViewModel
    
    var body: some View {
        HStack(spacing: 32) {
            ForEach(ExpiryDate.allCases) { expiryDate in
                Button {
                    viewModel.selectedExpiryDate = expiryDate.formattedDate
                } label: {
                    Text(expiryDate.rawValue)
                        .padding()
                        .foregroundStyle(viewModel.selectedExpiryDate == expiryDate.formattedDate ? Color.white : Color(UIColor.label))
                        .background(
                            RoundedRectangle(cornerRadius: CGFloat(20))
                                .foregroundStyle(viewModel.selectedExpiryDate == expiryDate.formattedDate ? Color.blue : Color(UIColor.secondarySystemBackground))
                        )
                }
            }
        }
    }
}

#Preview {
    ExpiryDateButtonsView()
        .environmentObject(CreatePostViewModel())
}
