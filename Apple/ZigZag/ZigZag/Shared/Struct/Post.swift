//
//  Post.swift
//  ZigZag
//
//  Created by Daniel W on 10/17/24.
//


import Foundation

struct Post: Codable, Identifiable {
    let id: Int
    let authorId: String
    let text: String
    let expiryDate: String?
    let createdAt: String
    let updatedAt: String
    let location: Location
}

struct Location: Codable {
    let longitude: Double
    let latitude: Double
    let distance: Double
}
