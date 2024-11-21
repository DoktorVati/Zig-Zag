//
//  SignInView.swift
//  ZigZag
//
//  Created by Daniel W on 11/4/24.
//

import SwiftUI

struct SignInView: View {
    @StateObject var viewModel = AuthenticationViewModel()
    @StateObject var navigationManager = AuthNavigationManager()
    
    @FocusState private var isTextFieldFocused: Bool

    var body: some View {
        NavigationStack(path: $navigationManager.path) {
            VStack(alignment: .leading, spacing: 20) {
                Text("Login")
                    .font(.largeTitle)
                    .fontWeight(.bold)
                    .padding(.bottom, 20)
                
                // Email Field
                Text("Email")
                    .font(.headline)
                TextField("Enter your email", text: $viewModel.email)
                    .padding()
                    .background(Color(.systemGray6))
                    .cornerRadius(8)
                    .overlay(
                        RoundedRectangle(cornerRadius: 8)
                            .stroke(Color.gray.opacity(0.5), lineWidth: 1)
                    )
                    .keyboardType(.emailAddress)
                    .autocapitalization(.none)
                    .focused($isTextFieldFocused)
                
                // Password Field with Show Toggle
                Text("Password")
                    .font(.headline)
                HStack {
                    if viewModel.isPasswordVisible {
                        TextField("Enter your password", text: $viewModel.password)
                            .focused($isTextFieldFocused)
                    } else {
                        SecureField("Enter your password", text: $viewModel.password)
                            .focused($isTextFieldFocused)
                    }
                    Button(action: {
                        viewModel.togglePasswordVisibility()
                    }) {
                        Text(viewModel.isPasswordVisible ? "Hide" : "Show")
                            .foregroundColor(.blue)
                    }
                }
                .padding()
                .background(Color(.systemGray6))
                .cornerRadius(8)
                .overlay(
                    RoundedRectangle(cornerRadius: 8)
                        .stroke(Color.gray.opacity(0.5), lineWidth: 1)
                )
                
                // Login Button
                Button(action: {
                    viewModel.login() {
                    }
                }) {
                    Text("Login")
                        .font(.headline)
                        .foregroundColor(.white)
                        .frame(maxWidth: .infinity)
                        .padding()
                        .background(Color.blue)
                        .cornerRadius(8)
                }
                .padding(.top, 20)
                
                // Sign-Up Prompt
                HStack {
                    Text("Don’t have an account?")
                        .foregroundColor(.gray)
                    Button(action: {
                        navigationManager.navigateTo(.SignUp)
                    }) {
                        Text("Sign Up")
                            .foregroundColor(.blue)
                            .fontWeight(.semibold)
                    }
                }
                .padding(.top, 10)
                
                Spacer()
            }
            .padding()
            .navigationBarBackButtonHidden(true)
            .navigationDestination(for: AuthDestination.self) { destination in
                switch destination {
                case .Login:
                    SignInView(viewModel: viewModel)
                case .SignUp:
                    CreateAccountView()
                case .SMSVerification:
                    SMSVerificationView()
                }
            }
        
            }
        .onTapGesture {
            isTextFieldFocused = false;
        }
        .environmentObject(navigationManager)
        .environmentObject(viewModel)
        }
    }


#Preview {
    SignInView(viewModel: AuthenticationViewModel())
}
