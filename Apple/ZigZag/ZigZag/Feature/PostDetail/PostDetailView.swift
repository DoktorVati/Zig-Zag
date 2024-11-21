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
    
    @State private var manualCount: Int? = 0
    
    var refreshAction: (() -> Void)?
    
    let post: Post;
    
    var body: some View {
        VStack {
            VStack {
                PostView(post: post, manualComment: $manualCount).padding(8)
                Divider()
                ScrollView {
                    VStack {
                        ForEach(viewModel.comments) { comment in
                            CommentView(comment: comment)
                                .padding()
                            Divider()
                        }
                        
                    }
                }
                .padding(.horizontal)
                .refreshable {
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
                        APIManager.shared.createComment(postId: post.id, text: filterProfanity(in: replyText), author: "Saul") {
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
                    manualCount = (manualCount ?? 0) + 1
                    
                    
                }) {
                    Image(systemName: "paperplane.fill").foregroundColor(.blue)
                }
                
            }.padding()
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
    let samplePost = Post(id: 12, authorId: "Test Author", text: "This room gets #hot as #hell This room gets #hot as #hell", expiryDate: "1-1-2", createdAt: "23:00", updatedAt: "33", location: Location(longitude: 34, latitude: 43, distance: 23), commentCount: 10)
    PostDetailView(post: samplePost)
}
