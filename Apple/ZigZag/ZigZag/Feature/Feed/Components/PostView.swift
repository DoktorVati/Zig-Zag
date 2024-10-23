//
//  PostView.swift
//  ZigZag
//
//  Created by Daniel W on 10/9/24.
//

import SwiftUI

struct PostView: View {
    @EnvironmentObject var navigationManager: NavigationManager

    let post: Post  // Accept a Post object
    
    var body: some View {
        VStack(alignment: .leading) {
            HStack(spacing: 4) {
                // Simulate time since post creation
                Text("1 HOUR AGO ⏲️")  // You can add a real-time formatter here later
                    .font(.caption)
                    .foregroundColor(.gray)
                Text("21 HOURS")
                    .font(.caption)
                    .foregroundColor(.gray)
                Spacer()
                Image(systemName: "ellipsis")
                    .foregroundColor(.gray)
            }
            
            // Post text with clickable hashtags
            taggableText()
                .font(.body)
                .padding(.vertical, 4)
            
            HStack {
                Text("32😭") // Placeholder for reactions, could be dynamic later
                Text("16🔥") // Placeholder for reactions, could be dynamic later
                Spacer()
                // Display post location or some other data
                Text(post.location.distanceString)
                    .font(.caption)
                    .foregroundColor(.gray)
            }
        }
    }
    
    // Function to build a view with clickable hashtags and normal text
    @ViewBuilder
    func taggableText() -> some View {
        // Using VStack to allow text flow
        HStack(spacing: 0) {
            // Start by displaying each word, and handling hashtags as clickable
            let words = post.words
            
            ForEach(words.indices, id: \.self) { index in
                let word = words[index]
                
                // Handle hashtags (make them clickable)
                if post.tags.contains(word) {
                    Button(action: {
                        tagAction(String: word)
                        
                    }) {
                        Text(word)
                            .foregroundColor(.blue)
                            .underline()
                    }
                } else {
                    // Display normal words
                    Text(word)
                        .foregroundColor(.primary)
                }
                
                // Add space after each word except the last
                if index < words.count - 1 {
                    Text(" ")
                        .foregroundColor(.primary)
                }
            }
        }
    }
    
    func tagAction(String tag: String) {
        navigationManager.navigateTo(.tagFilter(String(tag.dropFirst())))
    }
}

#Preview {
    // Example post data
    let samplePost = Post(id: 12, authorId: "Test Author", text: "This room gets #hot as #hell This room gets #hot as #hell", expiryDate: "1-1-2", createdAt: "23:00", updatedAt: "33", location: Location(longitude: 34, latitude: 43, distance: 23))
    
    List {
        Section {
            PostView(post: samplePost)
        }
        Section {
            PostView(post: samplePost)
        }
        Section {
            PostView(post: samplePost)
        }
        Section {
            PostView(post: samplePost)
        }
    }
}
