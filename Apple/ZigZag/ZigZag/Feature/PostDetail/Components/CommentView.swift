//
//  CommentView.swift
//  ZigZag
//
//  Created by Saul Almanzar on 11/1/24.
//

import SwiftUI

struct CommentView: View {
    let comment: Comment;
    @State var text: String = "This is a really comment."
    var body: some View {
        HStack(alignment: .center) {
                Text(comment.text).padding(4)
                Spacer();
            Text("1 HOUR AGO")
                .font(.caption)
                .textCase(.uppercase)
                .foregroundColor(.gray)
            }
    }
}

#Preview {
    
    let samplePost = Post(id: 12, authorId: "Test Author", text: "This room gets #hot as #hell This room gets #hot as #hell", expiryDate: "1-1-2", createdAt: "23:00", updatedAt: "33", location: Location(longitude: 34, latitude: 43, distance: 23), commentCount: 10)
    
    let sampleComment = Comment(id: 1, authorId: "Saul", postId: 1, text: "This is a really long comment because I am insufferable", createdAt: "2024-03-10:20:00:30Z")
    
    List {
        Section {
            PostView(post: samplePost);
        }
       
            
            Section {
                CommentView(comment: sampleComment);
            }
            
            Section {
                CommentView(comment: sampleComment);
            }
            
            Section {
                CommentView(comment: sampleComment);
            }
        
    }
}

