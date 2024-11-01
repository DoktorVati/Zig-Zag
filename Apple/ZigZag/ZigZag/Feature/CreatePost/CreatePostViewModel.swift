//
//  CreatePostViewModel.swift
//  ZigZag
//
//  Created by Daniel W on 10/14/24.
//

import Foundation
import SwiftUI
import Combine
import MapKit

class CreatePostViewModel: ObservableObject {
    @Published var region = MKCoordinateRegion(
        center: CLLocationCoordinate2D(latitude: 34.528675, longitude: -83.987841),
        span: MKCoordinateSpan(latitudeDelta: 0.01, longitudeDelta: 0.01))
    @Published var selectedRadiusIndex: Int? = 0  // Track which button is selected
    @Published var selectedExpiryDate: String? = ExpiryDate.thirtyMinutes.formattedDate
    
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
    
    func createPost(text: String) {
            // Ensure location is available from LocationManager
            guard let location = LocationManager.shared.location else {
                print("Location not available.")
                return
            }
            
            // Ensure post text is not empty
            guard !text.isEmpty else {
                print("Post text is empty.")
                return
            }
            
            // Call APIManager to create the post
            APIManager.shared.createPost(
                lat: location.coordinate.latitude,
                long: location.coordinate.longitude,
                text: text,
                author: "Daniel",
                expiryDate: selectedExpiryDate
            ) {post in
                print("Post Created: \(post)")
            }
        }
}
