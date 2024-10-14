//
//  PostView.swift
//  ZigZag
//
//  Created by Daniel W on 10/9/24.
//

import SwiftUI

struct PostView: View {
    var body: some View {
        HStack {
            VStack(alignment: .leading) {
                HStack(spacing: 4) {
                    Text("1 HOUR AGO ⏲️")
                        .font(.caption)
                        .foregroundColor(.gray)
                    Text("21 HOURS")
                        .font(.caption)
                        .foregroundColor(.gray)
                    Spacer()
                    Image(systemName: "ellipsis")
                        .foregroundColor(.gray)
                }
                Text("This room gets hot as hell")
                    .font(.body)
                    .padding(.vertical, 4)
                
                HStack {
                    Text("32😭")
                    Text("16🔥")
                    Spacer()
                    Text("11 M")
                        .font(.caption)
                        .foregroundColor(.gray)
                }
            }
        }
    }
}

#Preview {
    List {
        Section {
            PostView()
        }
        Section {
            PostView()
        }
        Section {
            PostView()
        }
        Section {
            PostView()
        }
    }
}
