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
    
    @State var hidePlus = false
    
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
                                PostView(post: post, manualComment: .constant(nil), changeTagAction: { selectedTag in
                                    self.mapTitle = selectedTag
                                    
                                }, refreshAction:  {
                                    viewModel.fetchPosts()
                                    
                                })
                                .background(
                                    NavigationLink("", destination: PostDetailView(post: post)
                                        .onAppear {
                                            mapTitle = "Comments"
                                            hidePlus = true  // Set hidPlus to true when navigating
                                        }
                                    )
                                    .opacity(0)
                                )
                            }
                        }
                        .refreshable {
                            viewModel.fetchPosts()
                        }
                    }
                    .toolbar {
                        ToolbarItem(placement: .topBarLeading) {
                            Menu {
                                Button(action: {
                                    viewModel.selectedFilterIndex = 0
                                }) {
                                    Label("Now", systemImage: "alarm.fill")
                                        .bold()
                                        .padding(4)
                                        .background(Color.blue)
                                        .foregroundColor(.white)
                                        .cornerRadius(10)
                                }
                                
                                Button(action: {
                                    viewModel.selectedFilterIndex = 1
                                }) {
                                    Label("Hot", systemImage: "flame.fill")
                                        .bold()
                                        .padding(4)
                                        .background(Color.blue)
                                        .foregroundColor(.white)
                                        .cornerRadius(10)
                                }
                                
                                Button(action: {
                                    viewModel.selectedFilterIndex = 2
                                }) {
                                    Label("Near", systemImage: "map.fill")
                                        .bold()
                                        .padding(4)
                                        .background(Color.blue)
                                        .foregroundColor(.white)
                                        .cornerRadius(10)
                                }
                                
                            } label: {
                                HStack {
                                    Text(viewModel.filterOptions[viewModel.selectedFilterIndex])
                                    Image(systemName: viewModel.filterIcons[viewModel.selectedFilterIndex])
                                }
                                    .bold()
                                    .padding(4)
                                    .background(Color.blue)
                                    .foregroundStyle(.white)
                                    .cornerRadius(10)
                            }
                        }
                        ToolbarItem{
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
                            hidePlus = false
                            mapsize = 250
                            mapTitle = "ZigZag"
                            
                        }
                    }
                    .task {
                        viewModel.fetchPosts()
                    }
                    .onChange(of: viewModel.selectedFilterIndex) {
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
                        if !hidePlus {
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
