//
//  FeedView.swift
//  ZigZag
//
//  Created by Daniel W on 10/9/24.
//

import SwiftUI

struct FeedView: View {
    @StateObject var viewModel = FeedViewModel()
    
    var distancesArray: [Double] = [0.01, 0.05, 0.1, 1]
    var distanceIcons: [String] = ["figure.walk.circle", "house.circle", "building.2.crop.circle", "globe.americas"]
    
    var body: some View {
        ZStack(alignment: .top) {
            // Background color, dynamic for light/dark mode
            Color(UIColor.systemBackground)
                .edgesIgnoringSafeArea(.all)
            
            VStack {
                Rectangle()
                    .foregroundStyle(Color(UIColor.systemBackground))
                    .frame(height: 180)
                    .ignoresSafeArea(.all)
             // Scrollable Feed
                NavigationStack {
                    VStack {
                        List {
                          //  RadiusButtonsView()
                          //  .listRowBackground(Color.clear) // Set background to clear

                            ForEach(0..<10, id: \.self) { _ in
                                Section {
                                    PostView()  // Custom post view below
                                }
                            }
                        }
                        .refreshable {
                            //Add logic
                        }
                    }
                    .toolbar {
                        ToolbarItem {
                            RadiusButtonsView()
                        }
                    }
                }
               // .border(Color.red)
 
            }
            
            MapView(region: $viewModel.region, overlayText: "ZigZag")

            
            // Floating "+" Button
            VStack {
                Spacer()
                HStack {
                    Spacer()
                    Button(action: {
                        // Action for the floating button
                    }) {
                        Image(systemName: "plus")
                            .font(.system(size: 30))
                            .foregroundColor(.white)
                            .frame(width: 60, height: 60)
                            .background(Color(UIColor.systemBlue))
                            .clipShape(Circle())
                            .shadow(radius: 10)
                    }
                    .padding(.trailing, 30)
                }
            }
        }
        .environmentObject(viewModel)
    }
}

#Preview {
    FeedView()
}
