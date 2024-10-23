//
//  FeedView.swift
//  ZigZag
//
//  Created by Daniel W on 10/9/24.
//

import SwiftUI
import CoreLocation

struct FeedView: View {
    @StateObject var viewModel = FeedViewModel()
    @StateObject var navigationManager = NavigationManager()
    
    @State var mapsize: CGFloat = 250
    @State var mapTitle = "ZigZag"
    
    var body: some View {
        ZStack(alignment: .top) {
            // Background color, dynamic for light/dark mode
            Color(UIColor.systemBackground)
                .edgesIgnoringSafeArea(.all)
            
            VStack {
                Rectangle()
                    .foregroundStyle(Color(UIColor.systemBackground))
                    .frame(height: mapsize - 10)
                    .ignoresSafeArea(.all)
                // Scrollable Feed
                NavigationStack(path: $navigationManager.path) {
                    VStack {
                        List(viewModel.posts) { post in
                            Section {
                                PostView(post: post)
                            }
                            
                        }
                        .refreshable {
                            viewModel.fetchPosts()
                        }
                    }
                    .toolbar {
                        ToolbarItem {
                            RadiusButtonsView()
                        }
                    }
                    .navigationDestination(for: ZigZagDestination.self) { destination in
                        switch destination {
                        case .createPost:
                            CreatePostView()
                            //TODO: find a way to make transitionBetter
                        case .tagFilter:
                            //TODO: add tagView
                            EmptyView()
                        }
                    }
                    .onAppear {
                        if viewModel.needsLocationPermission {
                            LocationManager.shared.requestWhenInUseAuthorization()
                        }
                        viewModel.setUserLoaction()
                        withAnimation {
                            mapsize = 250
                            mapTitle = "ZigZag"
                        }
                    }
                    .task{
                        viewModel.fetchPosts()
                    }
                }
                
            }
            .environmentObject(navigationManager)
            
            MapView(region: $viewModel.region, mapSize: $mapsize, overlayText: $mapTitle)
            
            // Floating "+" Button
            VStack {
                Spacer()
                HStack {
                    Spacer()
                    Button(action: {
                        navigationManager.navigateTo(.createPost)
                        withAnimation {
                            mapsize = 180
                            mapTitle = "Create Post"
                        }
                    }) {
                        if mapsize == 250 {
                            Image(systemName: "plus")
                                .font(.system(size: 30))
                                .foregroundColor(.white)
                                .frame(width: 60, height: 60)
                                .background(Color(UIColor.systemBlue))
                                .clipShape(Circle())
                                .shadow(radius: 10)
                        }
                    }
                    .padding(.trailing, 30)
                    .padding(.bottom)
                }
            }
        }
        .ignoresSafeArea(.all)
        .environmentObject(viewModel)
    }
}

#Preview {
    FeedView()
}
