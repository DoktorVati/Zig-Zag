//
//  APIManager.swift
//  ZigZag
//
//  Created by Daniel W on 10/17/24.
//


import Foundation
import SwiftUI

class APIManager {
    // Singleton instance for easy access
    static let shared = APIManager()
    
    // Base URL for the API
    private let baseURL = "http://api.zigzag.madebysaul.com"
    
    // URLSession configuration (optional)
    private let session: URLSession
    
    private init() {
        session = URLSession.shared
    }
    
    // MARK: - Create Post
    func createPost(text: String, latitude: Double, longitude: Double, authorId: String, completion: @escaping (Result<Void, Error>) -> Void) {
        guard let url = URL(string: "\(baseURL)/posts") else {
            print("Invalid URL")
            return
        }
        
        var request = URLRequest(url: url)
        request.httpMethod = "POST"
        request.setValue("application/json", forHTTPHeaderField: "Content-Type")
        
        // Request body
        let body: [String: Any] = [
            "text": text,
            "latitude": latitude,
            "longitude": longitude,
            "author": authorId,
            "postLatitude": latitude,
            "postLongitude": longitude
        ]
        
        do {
            request.httpBody = try JSONSerialization.data(withJSONObject: body, options: [])
        } catch {
            completion(.failure(error))
            return
        }
        
        // Make the request
        session.dataTask(with: request) { data, response, error in
            if let error = error {
                completion(.failure(error))
                return
            }
            
            if let httpResponse = response as? HTTPURLResponse, httpResponse.statusCode == 200 {
                completion(.success(()))
            } else {
//                let error = NSError(domain: "", code: httpResponse?.statusCode ?? 500, userInfo: [NSLocalizedDescriptionKey: "Failed to create post"])
//                completion(.failure(error))
            }
        }.resume()
    }
    
    // MARK: - Fetch Posts
    func fetchPosts(latitude: Double, longitude: Double, completion: @escaping (Result<[Post], Error>) -> Void) {
        guard let url = URL(string: "\(baseURL)/posts?latitude=\(latitude)&longitude=\(longitude)") else {
            print("Invalid URL")
            return
        }
        
        session.dataTask(with: url) { data, response, error in
            if let error = error {
                completion(.failure(error))
                return
            }
            
            guard let data = data else {
                let error = NSError(domain: "", code: 500, userInfo: [NSLocalizedDescriptionKey: "No data received"])
                completion(.failure(error))
                return
            }
            
            do {
                let posts = try JSONDecoder().decode([Post].self, from: data)
                completion(.success(posts))
            } catch {
                completion(.failure(error))
            }
        }.resume()
    }
    
    // MARK: - Delete Post
    func deletePost(postId: Int, completion: @escaping (Result<Void, Error>) -> Void) {
        guard let url = URL(string: "\(baseURL)/posts/\(postId)") else {
            print("Invalid URL")
            return
        }
        
        var request = URLRequest(url: url)
        request.httpMethod = "DELETE"
        
        session.dataTask(with: request) { data, response, error in
            if let error = error {
                completion(.failure(error))
                return
            }
            
            if let httpResponse = response as? HTTPURLResponse, httpResponse.statusCode == 200 {
                completion(.success(()))
            } else {
//                let error = NSError(domain: "", code: httpResponse?.statusCode ?? 500, userInfo: [NSLocalizedDescriptionKey: "Failed to delete post"])
//                completion(.failure(error))
            }
        }.resume()
    }
}
