//
//  ProfileSheetView.swift
//  ZigZag
//
//  Created by Daniel W on 11/8/24.
//

import SwiftUI

struct ProfileSheetView: View {
    @EnvironmentObject var auth: FirebaseManager
    
    var body: some View {
        VStack(spacing: 20) {
            Text("Profile")
                .font(.largeTitle)
                .fontWeight(.bold)
            
            if let email = auth.email {
                HStack {
                    Text("Email:")
                        .fontWeight(.semibold)
                    Spacer()
                    Text(email)
                }
            }
            
            if let phoneNumber = auth.phoneNumber {
                HStack {
                    Text("Phone Number:")
                        .fontWeight(.semibold)
                    Spacer()
                    Text(phoneNumber)
                }
            }
            
            Spacer()
            
            Button(action: {
                auth.signOut()
            }) {
                Text("Sign Out")
                    .font(.headline)
                    .foregroundColor(.white)
                    .frame(maxWidth: .infinity)
                    .padding()
                    .background(Color.red)
                    .cornerRadius(8)
            }
            
            Button(action: {
                //TODO: Delete account
            }) {
                Label("Delete Account", systemImage: "trash.fill")
                    .foregroundStyle(.red)
            }
        }
        .padding()
    }
}
