//
//  FeedView.swift
//  ZigZag
//
//  Created by Daniel W on 10/9/24.
//

import SwiftUI
import CoreLocation
import FirebaseAuth

struct FeedView: View {
    @EnvironmentObject var auth: FirebaseManager
    
    @StateObject var viewModel = FeedViewModel()
    @StateObject var navigationManager = NavigationManager()
    
    @State private var showProfileSheet = false // State to control sheet display
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
                                PostView(post: post) {
                                    viewModel.fetchPosts()
                                }
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
                        case .tagFilter(let tag):
                            TagsView(selectedTag: tag)
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
                    .task {
                        viewModel.fetchPosts()
                    }
                }
            }
            .environmentObject(navigationManager)
            
            MapView(region: $viewModel.region, mapSize: $mapsize, overlayText: $mapTitle)
            
            // Position ProfileIcon at the top right
            VStack {
                HStack {
                    Spacer()
                    Button {
                        showProfileSheet = true // Trigger sheet display
                    } label: {
                        ProfileIcon()
                    }
                }
                Spacer()
            }
            .padding(.horizontal,20)
            .padding(.vertical, 38)
            .sheet(isPresented: $showProfileSheet) {
                NavigationStack {
                    ScrollView {
                        ProfileSheetView()
                            .environmentObject(auth)
                    }
                        //.navigationBarTitle("Profile", displayMode: .inline) // Title for the profile sheet
                        .toolbar {
                            ToolbarItem(placement: .navigationBarLeading) {
                                Button("Close") {
                                    showProfileSheet = false // Dismiss the sheet
                                }
                            }
                        }
                }
            }
            
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
