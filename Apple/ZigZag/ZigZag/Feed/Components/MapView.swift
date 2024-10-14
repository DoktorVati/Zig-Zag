//
//  MapView.swift
//  ZigZag
//
//  Created by Daniel W on 10/9/24.
//

import SwiftUI
import MapKit

struct MapView: View {
    @Binding var region: MKCoordinateRegion
    
    let overlayText: String
    
    var body: some View {
        ZStack {
            Map(coordinateRegion: $region)
                .frame(height: 250)
                .cornerRadius(15)
                .overlay {
                    RoundedRectangle(cornerSize: CGSize(width: 15, height: 15))
                        .opacity(0.2)
                }
            
            
            // Text overlay
            Text("ZigZag")
                .font(.largeTitle)
                .foregroundColor(.white)
                .bold()
                .padding()
        }
        .ignoresSafeArea(.all)
    }
}


#Preview {
    VStack{
        MapView(region: .constant(MKCoordinateRegion(
            center: CLLocationCoordinate2D(latitude: 34.528675, longitude: -83.987841),
            span: MKCoordinateSpan(latitudeDelta: 0.01, longitudeDelta: 0.01))), overlayText: "ZigZag")
        Rectangle().foregroundStyle(.black).ignoresSafeArea(.all)
    }
}
