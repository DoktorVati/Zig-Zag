import SwiftUI
import Combine

class CommentViewModel: ObservableObject {
    @Published var replyText = ""
    @Published var isSending = false
    @Published var errorMessage: String?

    private var cancellables = Set<AnyCancellable>()
    
    // Replace with your actual API endpoint
    let apiURL = "https://example.com/api/reply"

    func sendReply(to postId: Int) {
        guard !replyText.isEmpty else {
            errorMessage = "Reply cannot be empty."
            return
        }

        isSending = true
        errorMessage = nil
        
        // Create the URL request with URLSession (replace with your API endpoint and request details)
        guard let url = URL(string: "\(apiURL)/\(postId)") else {
            errorMessage = "Invalid URL."
            isSending = false
            return
        }

        var request = URLRequest(url: url)
        request.httpMethod = "POST"
        request.setValue("application/json", forHTTPHeaderField: "Content-Type")

        let body: [String: Any] = ["reply": replyText]
        request.httpBody = try? JSONSerialization.data(withJSONObject: body)

        URLSession.shared.dataTaskPublisher(for: request)
            .receive(on: DispatchQueue.main)
            .sink(receiveCompletion: { completion in
                switch completion {
                case .failure(let error):
                    self.errorMessage = "Failed to send reply: \(error.localizedDescription)"
                case .finished:
                    break
                }
                self.isSending = false
            }, receiveValue: { _ in
                self.replyText = "" // Clear the reply text after sending
            })
            .store(in: &cancellables)
    }
}
