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
    @Published var selectedRadiusIndex: Int? = 1  // Track which button is selected
    
    @Published var isLoading: Bool = false
    
    @Published var posts: [Post] = []
    
    let distancesArray: [Double] = [0.001, 0.01, 0.1, 10]
    let distanceIcons: [String] = ["figure.walk.circle", "house", "building.2.crop.circle", "globe.americas"]
    
    let filterOptions: [String] = ["Now", "Hot", "Near"]
    let filterIcons: [String] = ["alarm.fill", "flame.fill", "map.fill"]
    
    @Published var selectedFilterIndex: Int = 0
    
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
    
    func decodeDistanceIndex() -> Distance {
        switch selectedRadiusIndex {
        case 0: return .local
        case 1: return .building
        case 2: return .neighborhood
        case 3: return .global
        default: return .local
        }
    }
    
    func fetchPosts() {
        isLoading = true
        guard let location = LocationManager.shared.location else { return }
        let distance = decodeDistanceIndex().rawValue
        
        var option = ""
        
        if selectedFilterIndex == 2 {
            option = "CLOSEST"
        } else if selectedFilterIndex == 1 {
            option = "HOT"
        }
        APIManager.shared.fetchPosts(option: option, latitude: location.coordinate.latitude, longitude: location.coordinate.longitude, distance: distance) { result in
            DispatchQueue.main.async {
                self.isLoading = false
                switch result {
                case .success(let fetchedPosts):
                    print("Successfully fetched posts")
                    
                    self.posts = fetchedPosts
                case .failure(let error):
                    print("Error fetching posts: \(error.localizedDescription)")
                }
            }
        }
    }
}
