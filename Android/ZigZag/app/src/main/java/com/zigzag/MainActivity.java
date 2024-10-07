package com.zigzag;

import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.TextView;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;

public class MainActivity extends AppCompatActivity {

    private LinearLayout messageContainer; // Container for the message groups
    private Button button;
    private String AIzaSyAvNciAUallXrKrOjyS_8YZUVF5hxRLTk0;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main); // Link to activity_main.xml layout

        // Initialize the message container and button
        messageContainer = findViewById(R.id.messageContainer);
        button = findViewById(R.id.button);

        // Set a click listener for the button
        button.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                showInputDialog();
            }
        });
    }

    private void showInputDialog() {
        // Create an EditText for user input
        final EditText input = new EditText(this);

        // Build the dialog
        new AlertDialog.Builder(this)
                .setTitle("New Post")
                .setMessage("Enter your post:")
                .setView(input)
                .setPositiveButton("OK", (dialog, which) -> {
                    String userInput = input.getText().toString();
                    addNewPost(userInput); // Add the new post to the layout
                })
                .setNegativeButton("Cancel", null)
                .show();
    }

    private void addNewPost(String text) {
        // Create a new TextView for the post
        TextView newPost = new TextView(this);
        newPost.setText(text);
        newPost.setTextSize(20); // Set text size

        // Set layout parameters
        LinearLayout.LayoutParams params = new LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.MATCH_PARENT,
                LinearLayout.LayoutParams.WRAP_CONTENT);

        newPost.setLayoutParams(params);
        newPost.setGravity(View.TEXT_ALIGNMENT_CENTER); // Center the text within the TextView

        // Center the entire TextView within the parent LinearLayout
        newPost.setGravity(android.view.Gravity.CENTER); // Set gravity to center

        // Add the new TextView to the message container
        messageContainer.addView(newPost);
    }
}
