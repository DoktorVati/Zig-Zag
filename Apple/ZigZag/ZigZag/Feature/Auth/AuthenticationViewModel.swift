//
//  AuthenticationViewModel.swift
//  ZigZag
//
//  Created by Daniel W on 11/4/24.
//

import Foundation
import SwiftUI
import Combine
import FirebaseAuth

class AuthenticationViewModel: ObservableObject {
    // User credentials and other state variables
    @Published var email: String = ""
    @Published var password: String = ""
    @Published var phoneNumber: String = ""
    @Published var isPasswordVisible: Bool = false
    @Published var verificationCode: String = ""
    @Published var verificationID: String? = nil
    @Published var isAuthenticated: Bool = false
    @Published var errorMessage: String? = nil
    
    // Validation errors
    @Published var usernameError: String? = nil
    @Published var emailError: String? = nil
    @Published var passwordError: String? = nil
    @Published var phoneNumberError: String? = nil
    @Published var isAgeVerified: Bool = false
    
    private var dateOfBirth: Date = Date() // Store the date of birth for age verification

    // Toggle password visibility
    func togglePasswordVisibility() {
        isPasswordVisible.toggle()
    }
    
    // MARK: - Validation Functions
    
    func validateEmail() {
        if email.isEmpty {
            emailError = "Email cannot be empty."
        } else if !email.contains("@") || !email.contains(".") {
            emailError = "Invalid email format."
        } else {
            emailError = nil
        }
    }
    
    func validatePassword() {
        passwordError = password.count < 6 ? "Password must be at least 6 characters." : nil
    }
    
    func validatePhoneNumber() {
        let phoneNumberPattern = "^[0-9]{10}$"
        let phonePredicate = NSPredicate(format: "SELF MATCHES %@", phoneNumberPattern)
        
        if phoneNumber.isEmpty {
            phoneNumberError = "Phone number cannot be empty."
        } else if !phonePredicate.evaluate(with: phoneNumber) {
            phoneNumberError = "Phone number must be 10 digits."
        } else {
            phoneNumberError = nil
        }
    }
    
    func checkAge(for dateOfBirth: Date) {
        self.dateOfBirth = dateOfBirth
        let calendar = Calendar.current
        let ageComponents = calendar.dateComponents([.year], from: dateOfBirth, to: Date())
        isAgeVerified = (ageComponents.year ?? 0) >= 18
    }
    
    func canSignUp() -> Bool {
        return usernameError == nil &&
               emailError == nil &&
               passwordError == nil &&
               phoneNumberError == nil &&
               isAgeVerified
    }
    
    // Start SMS verification
    func startPhoneVerification(completion: @escaping () -> Void) {
        let formattedPhoneNumber = "+1\(self.phoneNumber)"
        
        PhoneAuthProvider.provider().verifyPhoneNumber(formattedPhoneNumber, uiDelegate: nil) { [weak self] verificationID, error in
            DispatchQueue.main.async {
                if let error = error {
                    self?.errorMessage = error.localizedDescription
                    return
                }
                
                self?.verificationID = verificationID
                completion()
            }
        }
    }
    
    // Verify the SMS code and handle account linking/creation
    func verifyCodeAndAuthenticate(completion: @escaping () -> Void) {
        guard let verificationID = verificationID else {
            errorMessage = "Verification ID not found. Please request a new code."
            return
        }
        
        let phoneCredential = PhoneAuthProvider.provider().credential(withVerificationID: verificationID, verificationCode: verificationCode)
        
        // Sign in or link with phone credential
        Auth.auth().signIn(with: phoneCredential) { [weak self] (authResult, error) in
            DispatchQueue.main.async {
                if let error = error {
                    self?.errorMessage = error.localizedDescription
                    return
                }
                
                // Successfully signed in with phone credential; now link email/password
                if let currentUser = authResult?.user {
                    self?.linkEmailPasswordAccount(for: currentUser, completion: completion)
                } else {
                    self?.errorMessage = "Unexpected error: No user authenticated."
                }
            }
        }
    }
    
    // Link email and password to the authenticated phone user
    private func linkEmailPasswordAccount(for user: User, completion: @escaping () -> Void) {
        let emailCredential = EmailAuthProvider.credential(withEmail: email, password: password)
        
        user.link(with: emailCredential) { [weak self] authResult, error in
            DispatchQueue.main.async {
                if let error = error {
                    // Handle case where linking fails due to existing email account
                    if (error as NSError).code == AuthErrorCode.credentialAlreadyInUse.rawValue {
                        self?.handleCredentialAlreadyInUse(emailCredential, completion: completion)
                    } else {
                        self?.errorMessage = error.localizedDescription
                    }
                    return
                }
                
                self?.isAuthenticated = true
                self?.errorMessage = nil
                completion()
            }
        }
    }
    
    // Handle case where the email is already linked to an existing account
    private func handleCredentialAlreadyInUse(_ emailCredential: AuthCredential, completion: @escaping () -> Void) {
        Auth.auth().signIn(with: emailCredential) { [weak self] authResult, error in
            DispatchQueue.main.async {
                if let error = error {
                    self?.errorMessage = "Failed to sign in with existing email account: \(error.localizedDescription)"
                    return
                }
                
                // Re-authenticate with phone credential to link accounts
                if let currentUser = authResult?.user {
                    self?.linkPhoneToExistingEmailUser(currentUser, completion: completion)
                }
            }
        }
    }
    
    // Link phone credential to the existing email account
    private func linkPhoneToExistingEmailUser(_ user: User, completion: @escaping () -> Void) {
        guard let verificationID = verificationID else { return }
        let phoneCredential = PhoneAuthProvider.provider().credential(withVerificationID: verificationID, verificationCode: verificationCode)
        
        user.link(with: phoneCredential) { [weak self] authResult, error in
            DispatchQueue.main.async {
                if let error = error {
                    self?.errorMessage = "Failed to link phone number: \(error.localizedDescription)"
                    return
                }
                
                self?.isAuthenticated = true
                self?.errorMessage = nil
                completion()
            }
        }
    }
    
    func login(completion: @escaping () -> Void) {
        FirebaseManager.shared.login(email: email, password: password) { _ in
        }
    }
}
