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
    private let baseURL = "https://api.zigzag.madebysaul.com"
    
    // URLSession configuration (optional)
    private let session: URLSession
    
    private init() {
        session = URLSession.shared
    }
    
    // MARK: - Create Post
    struct CreatedPost: Codable {
        let id: Int
        let authorId: String
        let text: String
        let expiryDate: String?
        let createdAt: String
        let updatedAt: String
        let location: CreatedLocation
    }
    
    struct CreatedLocation: Codable {
        let latitude: Double
        let longitude: Double
        let distance: Double
    }
    
    func createPost(lat: Double, long: Double, text: String, author: String, postLat: Double? = nil, postLong: Double? = nil, expiryDate: String? = nil, completion: @escaping (Result<CreatedPost, Error>) -> Void) {
        
        // Prepare the URL
        guard let url = URL(string: "https://api.zigzag.madebysaul.com/posts/?latitude=\(lat)&longitude=\(long)") else {
            print("Invalid URL")
            return
        }
        
        // Create the Post object
        let post:  [String: Any] =
        [
            "text": text,
            "author": author,
            "postLatitude": lat,
            "postLongitude": long
        ]
        
        // Encode the Post object to JSON using JSONSerialization
        guard let jsonData = try? JSONSerialization.data(withJSONObject: post, options: []) else {
            print("Error encoding post data")
            return
        }
        
        // Configure the request
        var request = URLRequest(url: url)
        request.httpMethod = "POST"
        request.setValue("application/json", forHTTPHeaderField: "Content-Type")
        request.httpBody = jsonData
        
        // Send the request
        let task = URLSession.shared.dataTask(with: request) { data, response, error in
            if let error = error {
                completion(.failure(error))
                return
            }
            
            guard let data = data else {
                print("No data in response")
                return
            }
            
            do {
                let createdPost = try JSONDecoder().decode(CreatedPost.self, from: data)
                completion(.success(createdPost))
            } catch let decodingError {
                completion(.failure(decodingError))
            }
        }
        
        task.resume()
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
    
    // Function to get posts by hashtag
    func getTaggedPosts(latitude: Double, longitude: Double, hashtag: String, completion: @escaping (Result<[Post], Error>) -> Void) {
        // Corrected Base URL for the API
        let baseUrl = "https://api.zigzag.madebysaul.com/posts?"
        
        // Construct the query parameters
        var urlComponents = URLComponents(string: baseUrl)!
        urlComponents.queryItems = [
            URLQueryItem(name: "latitude", value: "\(latitude)"),
            URLQueryItem(name: "longitude", value: "\(longitude)"),
            URLQueryItem(name: "hashtag", value: hashtag)
        ]
        
        // Ensure the URL is valid
        guard let url = urlComponents.url else {
            completion(.failure(NSError(domain: "", code: -1, userInfo: [NSLocalizedDescriptionKey: "Invalid URL"])))
            return
        }
        
        // Create the URL request
        var request = URLRequest(url: url)
        request.httpMethod = "GET"
        
        // Start the data task
        URLSession.shared.dataTask(with: request) { data, response, error in
            if let error = error {
                completion(.failure(error))
                return
            }
            
            guard let data = data else {
                completion(.failure(NSError(domain: "", code: -1, userInfo: [NSLocalizedDescriptionKey: "No data received"])))
                return
            }
            
            do {
                // Decode the response into an array of Post objects
                let posts = try JSONDecoder().decode([Post].self, from: data)
                completion(.success(posts))
            } catch {
                completion(.failure(error))
            }
        }.resume()
    }
}
