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
    
    var tags: [String] {
        return extractTags(from: text)
    }
    
    var words: [String] {
        return extractWords()
    }
    
    // Function to extract hashtags
    private func extractTags(from text: String) -> [String] {
        // Regular expression pattern for matching hashtags with two or more characters
        let regexPattern = "#(\\w{2,})"
        
        do {
            // Create the regular expression object
            let regex = try NSRegularExpression(pattern: regexPattern, options: [])
            let range = NSRange(location: 0, length: text.utf16.count)
            
            // Get matches from the text
            let matches = regex.matches(in: text, options: [], range: range)
            
            // Extract hashtags (including the # symbol)
            let tags = matches.compactMap { match -> String? in
                guard let range = Range(match.range(at: 0), in: text) else {  // Use match.range(at: 0) to include the whole match (including the #)
                    return nil
                }
                return String(text[range])  // This now includes the full hashtag
            }
            
            return tags
        } catch {
            print("Invalid regex pattern")
            return []
        }
    }
    
    private func extractWords() -> [String] {
        return text.split(separator: " ").map { String($0) }
    }
}

struct Location: Codable {
    let longitude: Double
    let latitude: Double
    let distance: Double
    
    var distanceInMiles: Double {
        distance / 1609.344
    }
    
    var distanceInFeet: Double {
        distance / 0.3048
    }
    
    var useFeetValue: Bool {
        distanceInFeet < 528
    }
    
    var distanceString: String {
        return returnDistanceString()
    }
    
    private func returnDistanceString() -> String {
        useFeetValue ? "\(Int(distanceInFeet)) feet away" : "\(Int(distanceInMiles)) miles away"
    }
}
