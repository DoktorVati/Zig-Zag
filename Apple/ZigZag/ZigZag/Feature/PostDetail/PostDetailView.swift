//
//  CommentView.swift
//  ZigZag
//
//  Created by Saul Almanzar on 11/1/24.
//


import SwiftUI

struct PostDetailView: View {
    @StateObject var viewModel = PostDetailViewModel()
    @State private var replyText = ""
    @FocusState private var isTextFieldFocused: Bool
    
    let post: Post;
    
    var body: some View {
        VStack {
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
            }.onTapGesture {
                isTextFieldFocused = false
            }
            
            Spacer()
            HStack{
                TextField("Reply...", text: $replyText).padding(10).background(Color(.systemGray6)).cornerRadius(8).focused($isTextFieldFocused)
                Button(action: {
                    if !replyText.isEmpty {
                        APIManager.shared.createComment(postId: post.id, text: replyText, author: "Saul") {
                            result in
                            switch result {
                            case .success(let newComment):
                                viewModel.comments.append(newComment)
                                replyText = ""
                                print("Comment created and added.")
                                isTextFieldFocused = false
                            case .failure(let error):
                                print("There was an error creating comment: \(error.localizedDescription)")
                            }
                         
                        }
                    }
                    replyText = ""
                }) {
                    Image(systemName: "paperplane.fill").foregroundColor(.blue)
                }
                
            }.padding()
        }
    }
}

#Preview {
    let samplePost = Post(id: 12, authorId: "Test Author", text: "This room gets #hot as #hell This room gets #hot as #hell", expiryDate: "1-1-2", createdAt: "23:00", updatedAt: "33", location: Location(longitude: 34, latitude: 43, distance: 23), commentCount: 10)
    PostDetailView(post: samplePost)
}
