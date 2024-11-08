//
//  FirebaseManager.swift
//  ZigZag
//
//  Created by Daniel W on 11/4/24.
//

import Foundation
import FirebaseAuth
import Combine

class FirebaseManager: ObservableObject {
    // Singleton instance
    static let shared = FirebaseManager()
    
    // Observable variables for authentication status and user details
    @Published var isAuthenticated: Bool = false
    
    // Firebase user fields
    @Published var email: String?
    @Published var phoneNumber: String?
    @Published var displayName: String?
    @Published var uid: String?
    @Published var photoURL: URL?
    
    private var authStateListener: AuthStateDidChangeListenerHandle?
    
    // Private initializer to enforce singleton pattern
    private init() {
        // Initialize Firebase authentication listener
        authStateListener = Auth.auth().addStateDidChangeListener { [weak self] _, user in
            self?.updateUserProperties(with: user)
            self?.isAuthenticated = (user != nil)
        }
    }
    
    // Method to handle sign-out
    func signOut() {
        do {
            try Auth.auth().signOut()
            clearUserProperties()
        } catch {
            print("Error signing out: \(error.localizedDescription)")
        }
    }
    
    deinit {
        if let authStateListener = authStateListener {
            Auth.auth().removeStateDidChangeListener(authStateListener)
        }
    }

    // MARK: - Update and Clear User Properties
    
    private func updateUserProperties(with user: User?) {
        if let user = user {
            self.email = user.email
            self.phoneNumber = user.phoneNumber
            self.displayName = user.displayName
            self.uid = user.uid
            self.photoURL = user.photoURL
        } else {
            clearUserProperties()
        }
    }
    
    private func clearUserProperties() {
        self.email = nil
        self.phoneNumber = nil
        self.displayName = nil
        self.uid = nil
        self.photoURL = nil
    }

    // MARK: - Phone Authentication

    // Function to send verification code to phone number
    func sendVerificationCode(to phoneNumber: String, completion: @escaping (Result<String, Error>) -> Void) {
        PhoneAuthProvider.provider().verifyPhoneNumber(phoneNumber, uiDelegate: nil) { verificationID, error in
            if let error = error {
                completion(.failure(error))
                return
            }
            if let verificationID = verificationID {
                completion(.success(verificationID))
            } else {
                completion(.failure(NSError(domain: "FirebaseManager", code: -1, userInfo: [NSLocalizedDescriptionKey: "Failed to retrieve verification ID."])))
            }
        }
    }
    
    // Function to verify code with verification ID
    func verifyCode(verificationID: String, verificationCode: String, completion: @escaping (Result<AuthDataResult, Error>) -> Void) {
        let credential = PhoneAuthProvider.provider().credential(withVerificationID: verificationID, verificationCode: verificationCode)
        Auth.auth().signIn(with: credential) { authResult, error in
            if let error = error {
                completion(.failure(error))
                return
            }
            if let authResult = authResult {
                self.updateUserProperties(with: authResult.user)
                completion(.success(authResult))
            } else {
                completion(.failure(NSError(domain: "FirebaseManager", code: -1, userInfo: [NSLocalizedDescriptionKey: "Authentication failed."])))
            }
        }
    }

    // MARK: - Email/Password Authentication

    // Function to create a new account with email and password
    func createAccount(email: String, password: String, completion: @escaping (Result<AuthDataResult, Error>) -> Void) {
        Auth.auth().createUser(withEmail: email, password: password) { authResult, error in
            if let error = error {
                completion(.failure(error))
                return
            }
            if let authResult = authResult {
                self.updateUserProperties(with: authResult.user)
                completion(.success(authResult))
            } else {
                completion(.failure(NSError(domain: "FirebaseManager", code: -1, userInfo: [NSLocalizedDescriptionKey: "Account creation failed."])))
            }
        }
    }

    // Function to log in with email and password
    func login(email: String, password: String, completion: @escaping (Result<AuthDataResult, Error>) -> Void) {
        Auth.auth().signIn(withEmail: email, password: password) { authResult, error in
            if let error = error {
                completion(.failure(error))
                return
            }
            if let authResult = authResult {
                self.updateUserProperties(with: authResult.user)
                completion(.success(authResult))
            } else {
                completion(.failure(NSError(domain: "FirebaseManager", code: -1, userInfo: [NSLocalizedDescriptionKey: "Login failed."])))
            }
        }
    }
}
