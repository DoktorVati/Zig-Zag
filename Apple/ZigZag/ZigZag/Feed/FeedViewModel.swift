//
//  FeedViewModel.swift
//  ZigZag
//
//  Created by Daniel W on 10/9/24.
//

import Foundation
import SwiftUI
import Combine
import MapKit

class FeedViewModel: ObservableObject {
    @Published var region = MKCoordinateRegion(
        center: CLLocationCoordinate2D(latitude: 34.528675, longitude: -83.987841),
        span: MKCoordinateSpan(latitudeDelta: 0.01, longitudeDelta: 0.01))
    @Published var selectedRadiusIndex: Int? = 0  // Track which button is selected
    
    let distancesArray: [Double] = [0.01, 0.05, 0.1, 50]
    let distanceIcons: [String] = ["figure.walk.circle", "house", "building.2.crop.circle", "globe.americas"]
    
    // This function controls both zoom logic and button selection
    func mapZoom(index: Int) {
        // Update selected button index
        selectedRadiusIndex = index
        
        withAnimation {
            region.span.latitudeDelta = distancesArray[index]
            region.span.longitudeDelta = distancesArray[index]
        }
        
    }
}
