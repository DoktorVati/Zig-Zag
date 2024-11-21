//
//  TagsView.swift
//  ZigZag
//
//  Created by Daniel W on 10/22/24.
//

import SwiftUI

struct TagsView: View {
    @State var posts: [Post] = []
    var selectedTag: String
    
    var body: some View {
        VStack {
                VStack {
                    List(posts) { post in
                        Section {
                            PostView(post: post, manualComment: .constant(nil))
                        }
                        
                    }
                    .refreshable {
                        
                    }
                }
                .toolbar {
                    ToolbarItem {
                        RadiusButtonsView()
                    }
                }
                .task{
                    guard let coordinate = LocationManager.shared.location?.coordinate else { return }
                    APIManager.shared.getTaggedPosts(latitude: coordinate.latitude, longitude: coordinate.longitude, hashtag: selectedTag, completion: { result in
                        switch result {
                        case .success(let fetchedPosts):
                            // Handle the array of posts
                            self.posts = fetchedPosts
                            print("Received \(posts.count) posts")
                        case .failure(let error):
                            // Handle the error
                            print("Error fetching posts: \(error.localizedDescription)")
                        }
                    })
                }
            
            
        }
    }
}

//#Preview {
//    SwiftUIView()
//}
