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
            
            // Post text with clickable hashtags using WrappedHStack
            WrappedHStack(post.words, horizontalSpacing: 2, verticalSpacing: 2) { word in
                if post.tags.contains(word) {
                    // Clickable hashtag
                    Text(word)
                        .onTapGesture {
                            tagAction(tag: word)
                        }
                        .foregroundStyle(.blue)
                } else {
                    // Regular text
                    Text(word)
                        .foregroundColor(.primary)
                }
            }
            .padding(.vertical, 4)
            
            HStack {
                Text("\(post.commentCount) \(post.commentCount == 1 ? "comment" : "comments")")
                Spacer()
                Text(post.location.distanceString)
            }.font(.caption).foregroundColor(.gray)

        
        }
    }
    
    // Action when a hashtag is tapped
    func tagAction(tag: String) {
        print("tag: \(tag) tapped")
        navigationManager.navigateTo(.tagFilter(String(tag.dropFirst())))
    }
}

#Preview {
    // Example post data
    let samplePost = Post(id: 12, authorId: "Test Author", text: "This room gets #hot as #hell This room gets #hot as #hell", expiryDate: "1-1-2", createdAt: "23:00", updatedAt: "33", location: Location(longitude: 34, latitude: 43, distance: 23), commentCount: 10)
    
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
