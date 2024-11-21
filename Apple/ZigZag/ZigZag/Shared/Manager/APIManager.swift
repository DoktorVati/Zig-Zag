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
//    
//    struct CreatedComment: Codable {
//        let id: Int
//        let postId: Int
//        let authorId: String
//        let updatedAt: String
//        let createdAt: String
//    }
//    
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
        var post:  [String: Any] =
        [
            "text": text,
            "author": author,
            "postLatitude": lat,
            "postLongitude": long,
        ]
        
        if let expiryDate = expiryDate {
            post["expiryDate"] = expiryDate
        }
        
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
    func fetchPosts(option: String?, latitude: Double, longitude: Double, distance: String, completion: @escaping (Result<[Post], Error>) -> Void) {
        var orderBy: String = ""
        if let option {
            orderBy = "&orderBy=\(option)"
        }
        guard let url = URL(string: "\(baseURL)/posts?latitude=\(latitude)&longitude=\(longitude)&distance=\(distance)\(orderBy)") else {
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
    
    func fetchPostComments(postId: Int, completion: @escaping (Result<[Comment], Error>) -> Void) {
        guard let url = URL(string: "\(baseURL)/posts/\(postId)/comments") else {
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
                let comments = try JSONDecoder().decode([Comment].self, from: data)
                completion(.success(comments))
            } catch {
                completion(.failure(error))
            }
        }.resume()
    }
    
    func reportPost(postId: Int, reportingUserId: String, completion: @escaping (Result<Void, Error>) -> Void) {
        // Construct the URL with the post ID
        guard let url = URL(string: "\(baseURL)/posts/\(postId)/reports") else {
            print("Invalid URL")
            return
        }
        
        // Create a URLRequest and set it to POST
        var request = URLRequest(url: url)
        request.httpMethod = "POST"
        
        // Set the request body with the "snitch" field containing the reporting user ID
        let body: [String: Any] = ["snitch": reportingUserId]
        do {
            request.httpBody = try JSONSerialization.data(withJSONObject: body, options: [])
        } catch {
            completion(.failure(error))
            return
        }
        
        // Set headers, if necessary (e.g., for JSON content type)
        request.setValue("application/json", forHTTPHeaderField: "Content-Type")
        
        // Perform the network request
        session.dataTask(with: request) { data, response, error in
            if let error = error {
                completion(.failure(error))
                return
            }
            
            // Check for successful HTTP response status code (e.g., 200...299)
            if let httpResponse = response as? HTTPURLResponse, (200...299).contains(httpResponse.statusCode) {
                completion(.success(()))
            } else {
                let error = NSError(domain: "", code: (response as? HTTPURLResponse)?.statusCode ?? 500, userInfo: [NSLocalizedDescriptionKey: "Failed to report post"])
                completion(.failure(error))
            }
        }.resume()
    }
    
    func createComment(postId: Int, text: String, author: String, completion: @escaping (Result<Comment, Error>) -> Void) {
        
        // Prepare the URL
        guard let url = URL(string: "https://api.zigzag.madebysaul.com/posts/\(postId)/comments") else {
            print("Invalid URL")
            return
        }
        
        // Create the Comment object
        let comment:  [String: Any] =
        [
            "text": text,
            "author": author,
        ]
        
        // Encode the Comment object to JSON using JSONSerialization
        guard let jsonData = try? JSONSerialization.data(withJSONObject: comment, options: []) else {
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
                let createdComment = try JSONDecoder().decode(Comment.self, from: data)
                completion(.success(createdComment))
            } catch let decodingError {
                completion(.failure(decodingError))
            }
        }
        
        task.resume()
    }
}


enum Distance: String {
    case local = "80"
    case building = "820"
    case neighborhood = "40000"
    case global = "800000"
}
