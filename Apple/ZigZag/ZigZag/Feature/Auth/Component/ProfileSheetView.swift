//
//  ProfileSheetView.swift
//  ZigZag
//
//  Created by Daniel W on 11/8/24.
//

import SwiftUI
import FirebaseAuth

struct ProfileSheetView: View {
    @EnvironmentObject var auth: FirebaseManager
    
    // State variables for custom re-authentication alert
    @State private var showReauthenticateAlert = false
    @State private var email = ""
    @State private var password = ""
    @State private var errorMessage: String?
    
    var body: some View {
        ZStack {
            // Main content with a border to help identify its boundaries
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
                    auth.deleteAccount { result in
                        switch result {
                        case .success:
                            print("Account deleted successfully.")
                            
                        case .failure(let error):
                            print("Delete Account Error: \(error.localizedDescription)")
                            
                            if let authError = error as NSError?, authError.code == AuthErrorCode.requiresRecentLogin.rawValue {
                                showReauthenticateAlert = true
                            } else {
                                errorMessage = error.localizedDescription
                            }
                        }
                    }
                }) {
                    Label("Delete Account", systemImage: "trash.fill")
                        .foregroundStyle(.red)
                }
            }
            .padding()
            .frame(maxWidth: .infinity, maxHeight: .infinity) // Expand to fill available space
            .background(Color(UIColor.systemBackground)) // Consistent background color
            .edgesIgnoringSafeArea(.all) // Fill the screen fully
            .disabled(showReauthenticateAlert) // Disables background content when alert is visible
            
            // Dimmed overlay when re-authentication is required
            if showReauthenticateAlert {
                Color.black.opacity(0.4)
                    .edgesIgnoringSafeArea(.all) // Full-screen overlay effect
            }
            
            // Custom alert overlay for re-authentication on top of the dimmed background
            if showReauthenticateAlert {
                VStack(spacing: 15) {
                    Text("Re-authenticate to Delete Account")
                        .font(.headline)
                        .padding(.top)
                    
                    if let errorMessage = errorMessage {
                        Text(errorMessage)
                            .foregroundColor(.red)
                            .font(.caption)
                    }
                    
                    TextField("Email", text: $email)
                        .textFieldStyle(RoundedBorderTextFieldStyle())
                        .autocapitalization(.none)
                        .padding(.horizontal)
                    
                    SecureField("Password", text: $password)
                        .textFieldStyle(RoundedBorderTextFieldStyle())
                        .padding(.horizontal)
                    
                    HStack {
                        Button("Cancel") {
                            // Clear fields and hide the alert without taking any action
                            showReauthenticateAlert = false
                            email = ""
                            password = ""
                            errorMessage = nil
                        }
                        .foregroundColor(.red)
                        
                        Spacer()
                        
                        Button("Confirm") {
                            // Attempt to re-authenticate the user
                            reauthenticateAndDelete()
                        }
                        .foregroundColor(.blue)
                    }
                    .padding()
                }
                .frame(width: 300)
                .background(Color(UIColor.systemBackground))
                .cornerRadius(12)
                .shadow(radius: 10)
                .padding()
            }
        }
    }
    
    // MARK: - Helper Functions
    
    // Re-authenticate and, if successful, proceed to delete the account
    func reauthenticateAndDelete() {
        auth.reauthenticateUser(email: email, password: password) { result in
            switch result {
            case .success:
                print("Re-authentication successful.")
                // After successful re-authentication, delete the account
                auth.deleteAccount { deleteResult in
                    switch deleteResult {
                    case .success:
                        print("Account deleted successfully after re-authentication.")
                        showReauthenticateAlert = false
                        clearAlertFields() // Clear fields after deletion
                    case .failure(let deleteError):
                        print("Failed to delete account after re-authentication: \(deleteError.localizedDescription)")
                        errorMessage = deleteError.localizedDescription
                    }
                }
                
            case .failure(let error):
                print("Re-authentication failed: \(error.localizedDescription)")
                errorMessage = "Re-authentication failed: \(error.localizedDescription)"
            }
        }
    }
    
    // Clears fields when alert is dismissed or canceled
    private func clearAlertFields() {
        email = ""
        password = ""
        errorMessage = nil
    }
}
