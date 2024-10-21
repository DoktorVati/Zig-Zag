//
//  PostView.swift
//  ZigZag
//
//  Created by Daniel W on 10/9/24.
//

import SwiftUI

struct PostView: View {
    let post: Post  // Accept a Post object
    
    var body: some View {
        HStack {
            VStack(alignment: .leading) {
                HStack(spacing: 4) {
                    // Simulate time since post creation
                    Text("1 HOUR AGO ⏲️")  // You can add a real time formatter here later
                        .font(.caption)
                        .foregroundColor(.gray)
                    Text("21 HOURS")
                        .font(.caption)
                        .foregroundColor(.gray)
                    Spacer()
                    Image(systemName: "ellipsis")
                        .foregroundColor(.gray)
                }
                
                // Post text
                Text(post.text)
                    .font(.body)
                    .padding(.vertical, 4)
                
                HStack {
                    Text("32😭") // Placeholder for reactions, could be dynamic later
                    Text("16🔥") // Placeholder for reactions, could be dynamic later
                    Spacer()
                    // Display post location or some other data
                    Text("\(Int(post.location.distance))°")
                        .font(.caption)
                        .foregroundColor(.gray)
                }
            }
        }
    }
}

#Preview {
    // Example post data
    let samplePost = Post(id: 12, authorId: "Test Author", text: "This room gets hot as hell", expiryDate: "1-1-2", createdAt: "23:00", updatedAt: "33", location: Location(longitude: 34, latitude: 43, distance: 23))
    
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
