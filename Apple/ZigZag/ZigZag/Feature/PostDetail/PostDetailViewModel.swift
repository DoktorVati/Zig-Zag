//
//  PostDetailViewModel.swift
//  ZigZag
//
//  Created by Saul Almanzar on 11/1/24.
//
import SwiftUI
import MapKit

class PostDetailViewModel: ObservableObject {
    
    @Published var isLoading: Bool = false;
    
    @Published var comments: [Comment] = [];
    
    func fetchPostComments(postId: Int) async {
        isLoading = true
        APIManager.shared.fetchPostComments(postId: postId) {
            result in DispatchQueue.main.async {
                self.isLoading = false;
                switch result {
                case .success(let fetchedComments):
                    print("Successfully fetched comments for post");
                    print(fetchedComments);
                    self.comments = fetchedComments;
                    
                case .failure (let error):
                    print("Error fetching comments for post: \(error.localizedDescription)")
                }
            }
        }
    }
}
