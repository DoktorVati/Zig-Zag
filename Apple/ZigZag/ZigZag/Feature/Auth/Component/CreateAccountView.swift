//
//  CreateAccountView.swift
//  ZigZag
//
//  Created by Daniel W on 11/4/24.
//

import SwiftUI

struct CreateAccountView: View {
    @EnvironmentObject var viewModel: AuthenticationViewModel
    @EnvironmentObject var navigationManager: AuthNavigationManager
    
    
    @FocusState private var isTextFieldFocused: Bool
    
    @State private var dateOfBirth = Date() // Default to current date
    
    var body: some View {
        ScrollView {
            VStack(alignment: .leading, spacing: 20) {
                Text("Sign Up")
                    .font(.largeTitle)
                    .fontWeight(.bold)
                    .padding(.bottom, 20)
                
                // Email Field with Validation
                Group {
                    Text("Email")
                        .font(.headline)
                    TextField("Enter your Email", text: $viewModel.email)
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
                        .onChange(of: viewModel.email, perform: { _ in viewModel.validateEmail() })
                    if let error = viewModel.emailError {
                        Text(error)
                            .font(.caption)
                            .foregroundColor(.red)
                    }
                }
                
                // Password Field with Validation
                Group {
                    Text("Password")
                        .font(.headline)
                    HStack {
                        if viewModel.isPasswordVisible {
                            TextField("Enter your password", text: $viewModel.password)
                                .focused($isTextFieldFocused)
                        } else {
                            SecureField("Enter your password", text: $viewModel.password).focused($isTextFieldFocused)
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
                    .onChange(of: viewModel.password, perform: { _ in viewModel.validatePassword() })
                    if let error = viewModel.passwordError {
                        Text(error)
                            .font(.caption)
                            .foregroundColor(.red)
                    }
                }
                
                // Phone Number Field with Validation
                Group {
                    Text("Phone Number")
                        .font(.headline)
                    TextField("Enter your phone number", text: $viewModel.phoneNumber)
                        .padding()
                        .background(Color(.systemGray6))
                        .cornerRadius(8)
                        .overlay(
                            RoundedRectangle(cornerRadius: 8)
                                .stroke(Color.gray.opacity(0.5), lineWidth: 1)
                        )
                        .keyboardType(.phonePad)
                        .focused($isTextFieldFocused)
                        .onChange(of: viewModel.phoneNumber, perform: { _ in viewModel.validatePhoneNumber() })
                    if let error = viewModel.phoneNumberError {
                        Text(error)
                            .font(.caption)
                            .foregroundColor(.red)
                    }
                }
                
                // Date of Birth Picker with Age Verification
                Group {
                    Text("Date of Birth")
                        .font(.headline)
                        .frame(maxWidth: .infinity, alignment: .leading) // Ensures "Date of Birth" aligns left

                    HStack {
                        DatePicker("Select your date of birth", selection: $dateOfBirth, displayedComponents: .date)
                            .labelsHidden() // Hide the default label text if it's unnecessary
                            .datePickerStyle(CompactDatePickerStyle())
                            .onChange(of: dateOfBirth) { newDate in
                                viewModel.checkAge(for: newDate)
                            }
                    }
                    .frame(maxWidth: .infinity, alignment: .leading) // Aligns HStack and DatePicker left

                    if viewModel.isAgeVerified {
                        Text("You are eligible to create an account.")
                            .foregroundColor(.green)
                            .frame(maxWidth: .infinity, alignment: .leading) // Aligns message to the left
                    } else {
                        Text("You must be 18 or older to sign up.")
                            .foregroundColor(.red)
                            .frame(maxWidth: .infinity, alignment: .leading) // Aligns message to the left
                    }
                }
                
                // Sign Up Button, disabled if not age-verified or if any validation failed
                Button(action: {
                    viewModel.startPhoneVerification {
                        navigationManager.navigateTo(.SMSVerification)
                    }
                }) {
                    Text("Sign Up")
                        .font(.headline)
                        .foregroundColor(.white)
                        .frame(maxWidth: .infinity)
                        .padding()
                        .background(viewModel.canSignUp() ? Color.blue : Color.gray)
                        .cornerRadius(8)
                }
                .padding(.top, 20)
              //  .disabled(!viewModel.canSignUp()) // Disable button if validation fails
                
                // Sign-Up Prompt
                HStack {
                    Text("Already have an account?")
                        .foregroundColor(.gray)
                    Button(action: {
                        navigationManager.navigateBack()
                    }) {
                        Text("Log In")
                            .foregroundColor(.blue)
                            .fontWeight(.semibold)
                    }
                }
                .padding(.top, 10)
                
                Spacer()
            }
            
            .padding()
            .navigationBarBackButtonHidden(true)
            .environmentObject(navigationManager)
        }.onTapGesture {
            isTextFieldFocused = false;
        }
    }
}

#Preview {
    CreateAccountView()
}
