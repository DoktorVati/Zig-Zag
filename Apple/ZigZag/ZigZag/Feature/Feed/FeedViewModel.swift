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
    
    @Published var isLoading: Bool = false
    
    @Published var posts: [Post] = []
    
    let distancesArray: [Double] = [0.001, 0.01, 0.1, 10]
    let distanceIcons: [String] = ["figure.walk.circle", "house", "building.2.crop.circle", "globe.americas"]
    
    var needsLocationPermission: Bool {
        LocationManager.shared.authorizationStatus == .notDetermined
    }
    
    // This function controls both zoom logic and button selection
    func mapZoom(index: Int) {
        // Update selected button index
        selectedRadiusIndex = index
        
        withAnimation {
            region.span.latitudeDelta = distancesArray[index]
            region.span.longitudeDelta = distancesArray[index]
        }
        
    }
    
    func setUserLoaction() {
        guard let userLocation = LocationManager.shared.location else { return }
        region = MKCoordinateRegion(center: userLocation.coordinate, span: MKCoordinateSpan(latitudeDelta: 0.01, longitudeDelta: 0.01))
    }
    
    func fetchPosts() {
        isLoading = true
        guard let location = LocationManager.shared.location else { return }
        APIManager.shared.fetchPosts(latitude: location.coordinate.latitude, longitude: location.coordinate.longitude) { result in
            DispatchQueue.main.async {
                self.isLoading = false
                switch result {
                case .success(let fetchedPosts):
                    self.posts = fetchedPosts
                case .failure(let error):
                    print("Error fetching posts: \(error.localizedDescription)")
                }
            }
        }
    }
}
