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
    
    // Time until the post expires
    var timeUntilExpires: String {
        guard let expiryDate = expiryDate else {
            return "No expiry date"
        }
        
        // Create an ISO8601 Date Formatter
        let formatter = ISO8601DateFormatter()
        
        // Convert the expiryDate string to a Date object
        guard let expiryDateAsDate = formatter.date(from: expiryDate) else {
            return "Invalid expiry date"
        }
        
        // Get the current date
        let currentDate = Date()
        
        // Calculate the time interval between the current date and the expiry date
        let timeInterval = expiryDateAsDate.timeIntervalSince(currentDate)
        
        if timeInterval <= 0 {
            return "Expired"
        }
        
        // Convert the time interval to hours, minutes, or days
        let hours = timeInterval / 3600
        let minutes = (timeInterval.truncatingRemainder(dividingBy: 3600)) / 60
        let days = timeInterval / (3600 * 24)
        
        // Return a human-readable string based on the time left
        if days >= 1 {
            return "\(Int(days)) day(s) left"
        } else if hours >= 1 {
            return "\(Int(hours)) hour(s) left"
        } else {
            return "\(Int(minutes)) minute(s) left"
        }
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
