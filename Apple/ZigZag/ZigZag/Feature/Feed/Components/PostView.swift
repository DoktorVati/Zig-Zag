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
    
    var refreshAction: (() -> Void)?
    
    
    var body: some View {
        VStack(alignment: .leading) {
            HStack(spacing: 4) {
                // Simulate time since post creation
                Text("\(post.timeSinceCreated) ⏲️")  // You can add a real-time formatter here later
                    .font(.caption)
                    .foregroundColor(.gray)
                Text(post.timeUntilExpires)
                    .font(.caption)
                    .foregroundColor(.gray)
                Spacer()
                
                
                Menu {
                    
                    if let userId = FirebaseManager.shared.uid, userId == post.authorId {
                        Button {
                            APIManager.shared.deletePost(postId: post.id) { _ in
                            }
                            refreshAction?()
                        } label: {
                            Label("Delete", systemImage: "trash.fill")
                                .foregroundStyle(.red)
                            
                        }
                    }
                    
                    Button {
                        //TODO: report posts
                        refreshAction?()
                    } label: {
                        Label("Report", systemImage: "megaphone.fill")
                    }
                    
                } label: {
                    Image(systemName: "ellipsis")
                        .foregroundColor(.gray)
                        .padding(20) // Adds padding to increase tappable area
                        .contentShape(Rectangle()) // Expands the tappable area without resizing the view
                        .frame(width: 20, height: 20, alignment: .center) // Keeps the icon itself small
                }
                
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
                //                Text("32😭") // Placeholder for reactions, could be dynamic later
                //                Text("16🔥") // Placeholder for reactions, could be dynamic later
                Spacer()
                // Display post location or some other data
                Text(post.location.distanceString)
                    .font(.caption)
                    .foregroundColor(.gray)
            }
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
    let samplePost = Post(id: 12, authorId: "Test Author", text: "This room gets #hot as #hell This room gets #hot as #hell", expiryDate: Date().addingTimeInterval(3600).ISO8601Format(), createdAt: Date().addingTimeInterval(-3600).ISO8601Format(), updatedAt: "33", location: Location(longitude: 34, latitude: 43, distance: 23))
    
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
