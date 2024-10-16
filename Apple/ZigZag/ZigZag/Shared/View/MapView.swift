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
    @Binding var mapSize: CGFloat
    
    @Binding var overlayText: String
    
    var body: some View {
        ZStack {
            Map(coordinateRegion: $region)
                .frame(height: mapSize)
                .cornerRadius(15)
                .overlay {
                    RoundedRectangle(cornerSize: CGSize(width: 15, height: 15))
                        .opacity(0.2)
                }
            
            
            // Text overlay
            Text(overlayText)
                .font(.largeTitle)
                .foregroundColor(.white)
                .bold()
                .padding()
        }
    }
}


#Preview {
    VStack{
        MapView(region: .constant(MKCoordinateRegion(
            center: CLLocationCoordinate2D(latitude: 34.528675, longitude: -83.987841),
            span: MKCoordinateSpan(latitudeDelta: 0.01, longitudeDelta: 0.01))), mapSize: .constant(250), overlayText: .constant("ZigZag"))
        Rectangle().foregroundStyle(.black).ignoresSafeArea(.all)
    }
}
