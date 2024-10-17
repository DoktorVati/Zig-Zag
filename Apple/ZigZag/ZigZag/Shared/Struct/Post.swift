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
    let postLatitude: Double
    let postLongitude: Double
}
