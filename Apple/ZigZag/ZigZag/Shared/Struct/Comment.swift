//
//  Comment.swift
//  ZigZag
//
//  Created by Saul Almanzar on 11/1/24.
//

struct Comment: Codable, Identifiable {
    let id: Int;
    let authorId: String;
    let postId: Int;
    let text: String;
    let createdAt: String;
}
