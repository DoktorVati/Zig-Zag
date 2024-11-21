//
//  SwiftUIView.swift
//  ZigZagApp
//
//  Created by Daniel W on 10/9/24.
//

import SwiftUI
import MapKit

struct RadiusButtonsView: View {
    @EnvironmentObject var viewModel: FeedViewModel
    
    //move these to the viewModel
    var distancesArray: [Double] = [0.01, 0.05, 0.1, 1]
    var distanceIcons: [String] = ["figure.walk.circle", "house.circle", "building.2.crop.circle", "globe.americas"]
    
    var body: some View {
        HStack(spacing: 6) {
            RadiusButton(icon: distanceIcons[0], myIndex: 0) {
                viewModel.mapZoom(index: 0)
                viewModel.fetchPosts()
            }

            RadiusButton(icon: distanceIcons[1], myIndex: 1) {
                viewModel.mapZoom(index: 1)
                viewModel.fetchPosts()
            }
            
            RadiusButton(icon: distanceIcons[2], myIndex: 2) {
                viewModel.mapZoom(index: 2)
                viewModel.fetchPosts()
            }
            
            RadiusButton(icon: distanceIcons[3], myIndex: 3) {
                viewModel.mapZoom(index: 3)
                viewModel.fetchPosts()
            }
        }
    }
}

#Preview {
    ZStack {
        Rectangle()
        RadiusButtonsView()
    }
    .environmentObject(FeedViewModel())
}
