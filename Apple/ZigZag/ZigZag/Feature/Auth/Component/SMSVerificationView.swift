//
//  SMSVerificationView.swift
//  ZigZag
//
//  Created by Daniel W on 11/4/24.
//

import SwiftUI

struct SMSVerificationView: View {
    @EnvironmentObject var viewModel: AuthenticationViewModel
    @EnvironmentObject var navigationManager: AuthNavigationManager
    
    var body: some View {
        VStack(alignment: .center, spacing: 20) {
            // Title
            Text("Sign Up")
                .font(.largeTitle)
                .fontWeight(.bold)
                .padding(.top, 40)
            
            // Instructional Text
            Text("Enter the code sent to \(viewModel.phoneNumber)")
                .foregroundColor(.gray)
                .multilineTextAlignment(.center)
                .padding(.horizontal, 40)
            
            // Code Entry Field
            TextField("Enter code", text: $viewModel.verificationCode)
                .padding()
                .background(Color(.systemGray6))
                .cornerRadius(8)
                .overlay(
                    RoundedRectangle(cornerRadius: 8)
                        .stroke(Color.gray.opacity(0.5), lineWidth: 1)
                )
                .keyboardType(.numberPad)
                .multilineTextAlignment(.center)
            
            // Submit Button
            Button(action: {
                viewModel.verifyCodeAndAuthenticate {
                    navigationManager.navigateBackToRoot()
                }
            }) {
                Text("Submit")
                    .font(.headline)
                    .foregroundColor(.white)
                    .frame(maxWidth: .infinity)
                    .padding()
                    .background(Color.blue)
                    .cornerRadius(8)
            }
            .padding(.horizontal, 40)
            .padding(.top, 20)
            
            // Clickable Footer to Request New Code
            Button(action: {
                // viewModel.requestNewCode()  // This function will handle the code resending logic
            }) {
                Text("Didn't receive a code? Request a new one.")
                    .font(.footnote)
                    .foregroundColor(.blue)
            }
            .padding(.top, 10)
            
            Spacer()
        }
        .padding()
        .navigationBarBackButtonHidden(false)
    }
}

#Preview {
    SMSVerificationView()
}
