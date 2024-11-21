//
//  CreatePostView.swift
//  ZigZag
//
//  Created by Daniel W on 10/14/24.
//

import SwiftUI
import MapKit

struct CreatePostView: View {
    @StateObject var viewModel = CreatePostViewModel()
    
    @EnvironmentObject var navigationManager: NavigationManager
    @EnvironmentObject var auth: FirebaseManager
    
    @State private var postText: String = ""
    @State private var timerSet = false
    @State private var location: String = "Current Location"
    
    // Focus state variable for the TextEditor
    @FocusState private var isTextEditorFocused: Bool
    
    var body: some View {
        ScrollView {
            VStack {
                HStack {
                    Image(systemName: "location.circle")
                        .foregroundColor(.gray)
                    Text(location)
                        .foregroundColor(.gray)
                    Spacer()
                }
                .padding(.top)
                
                // Text Editor with placeholder
                ZStack(alignment: .leading) {
                    if postText.isEmpty {
                        VStack {
                            Text("What's happening in your area?")
                                .padding(.top, 10)
                                .padding(.leading, 6)
                                .opacity(0.8)
                            Spacer()
                        }
                    }
                    
                    VStack {
                        TextEditor(text: $postText)
                            .frame(minHeight: 150, maxHeight: 300)
                            .scrollContentBackground(.hidden)
                            .background(Color(UIColor.systemGray6))
                            .cornerRadius(12)
                            .opacity(postText.isEmpty ? 0.85 : 1)
                            .focused($isTextEditorFocused) // Attach focus state
                        Spacer()
                        HStack {
                            Spacer()
                            if postText.count > 250 {
                                withAnimation {
                                    Label("Character Limit", systemImage: "exclamationmark.triangle.fill")
                                        .foregroundStyle(.yellow)
                                }
                            }
                            Text("\(postText.count)/250")
                                .padding(.bottom, 10)
                                .padding(.trailing, 6)
                                .opacity(0.8)
                        }

                    }
                }
                
                // Set Timer
                HStack {
                    Image(systemName: "clock")
                        .foregroundColor(.gray)
                    Text("Set Timer")
                        .foregroundColor(.gray)
                    Spacer()
                }
                
                ExpiryDateButtonsView()
                
                Spacer()
            }
            .environmentObject(viewModel)
            .padding(.horizontal)
            .toolbar {
                ToolbarItem(placement: .topBarTrailing) {
                    Button(action: {
                        viewModel.createPost(text: filterProfanity(in: postText))
                        navigationManager.navigateBackToRoot()
                    }) {
                        HStack {
                            Text("POST")
                            Image(systemName: "location.fill")
                        }
                        .bold()
                        .frame(maxWidth: .infinity)
                        .padding(4)
                        .background(Color.blue)
                        .foregroundColor(.white)
                        .cornerRadius(10)
                    }
                }
            }
        }
        .onTapGesture {
            isTextEditorFocused = false // Dismiss keyboard when tapping outside
        }
    }
    
    // Function to filter profanity
    private func filterProfanity(in text: String) -> String {
        var filteredText = text
        for word in viewModel.profanityList {
            let pattern = "\\b\(word)\\b" // Match whole words only
            if let regex = try? NSRegularExpression(pattern: pattern, options: .caseInsensitive) {
                let range = NSRange(filteredText.startIndex..<filteredText.endIndex, in: filteredText)
                filteredText = regex.stringByReplacingMatches(in: filteredText, options: [], range: range, withTemplate: String(repeating: "*", count: word.count))
            }
        }
        return filteredText
    }
}

#Preview {
    NavigationStack {
        CreatePostView()
            .environmentObject(CreatePostViewModel())
    }
}
