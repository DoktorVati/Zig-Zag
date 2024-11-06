//
//  CommentView.swift
//  ZigZag
//
//  Created by Saul Almanzar on 11/1/24.
//


import SwiftUI

struct PostDetailView: View {
    @StateObject var viewModel = PostDetailViewModel()
    let post: Post;
    
    var body: some View {
        VStack {
            PostView(post: post).padding(8)
            List {
                ForEach(viewModel.comments) {
                        comment in CommentView(comment: comment);
                }.listStyle(PlainListStyle())
                
                }.refreshable {
                    await viewModel.fetchPostComments(postId: post.id);
                
            }.onAppear {
                if viewModel.comments.isEmpty {
                    Task {
                        await viewModel.fetchPostComments(postId: post.id);
                    }
                    
                }
            }
            
        }
    }
}

#Preview {
    let samplePost = Post(id: 12, authorId: "Test Author", text: "This room gets #hot as #hell This room gets #hot as #hell", expiryDate: "1-1-2", createdAt: "23:00", updatedAt: "33", location: Location(longitude: 34, latitude: 43, distance: 23))
    PostDetailView(post: samplePost)
}
