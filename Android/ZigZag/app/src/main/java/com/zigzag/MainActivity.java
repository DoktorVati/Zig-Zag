package com.zigzag;

import android.Manifest;
import android.content.pm.PackageManager;
import android.graphics.drawable.Drawable;
import android.location.Location;
import android.os.Bundle;
import android.view.Gravity;
import android.view.View;
import android.widget.EditText;
import android.widget.GridLayout;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;

import com.bumptech.glide.Glide;
import com.bumptech.glide.request.target.CustomTarget;
import com.bumptech.glide.request.transition.Transition;
import com.google.android.gms.location.FusedLocationProviderClient;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.tasks.OnSuccessListener;

import org.jetbrains.annotations.Nullable;

public class MainActivity extends AppCompatActivity {
    private TextView headerTextView;
    private static final int LOCATION_REQUEST_CODE = 101;
    private FusedLocationProviderClient fusedLocationClient;
    private LinearLayout messageContainer; // Container for the message groups
    private ImageButton button;

    private String key = "AIzaSyAvNciAUallXrKrOjyS_8YZUVF5hxRLTk0"; // Use your API key

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main); // Link to activity_main.xml layout

        // Initialize the message container and button
        messageContainer = findViewById(R.id.messageContainer);
        button = findViewById(R.id.button);
        headerTextView = findViewById(R.id.headerTextView); // Update with your TextView ID

        fusedLocationClient = LocationServices.getFusedLocationProviderClient(this);
        getUserLocation();

        // Set a click listener for the button
        button.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                showInputDialog();
            }
        });
    }

    private void getUserLocation() {
        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED) {
            fusedLocationClient.getLastLocation()
                    .addOnSuccessListener(this, new OnSuccessListener<Location>() {
                        @Override
                        public void onSuccess(Location location) {
                            if (location != null) {
                                double latitude = location.getLatitude();
                                double longitude = location.getLongitude();
                                String apiKey = key;
                                String url = "https://maps.googleapis.com/maps/api/staticmap?center="
                                        + latitude + "," + longitude + "&zoom=12&size=400x400&key="
                                        + apiKey;
                                loadImageAsBackground(url);
                            }
                        }
                    });
        } else {
            requestPermissions(new String[]{Manifest.permission.ACCESS_FINE_LOCATION}, LOCATION_REQUEST_CODE);
        }
    }

    private void loadImageAsBackground(String url) {
        Glide.with(this)
                .load(url)
                .into(new CustomTarget<Drawable>() {
                    @Override
                    public void onResourceReady(@NonNull Drawable resource, @Nullable Transition<? super Drawable> transition) {
                        headerTextView.setBackground(resource);
                    }

                    @Override
                    public void onLoadCleared(@Nullable Drawable placeholder) {
                        // Handle placeholder if needed
                    }
                });
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        if (requestCode == LOCATION_REQUEST_CODE) {
            if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                getUserLocation();
            } else {
                Toast.makeText(this, "Location permission denied", Toast.LENGTH_SHORT).show();
            }
        }
    }

    private void showInputDialog() {
        final EditText input = new EditText(this);
        new AlertDialog.Builder(this)
                .setTitle("New Post")
                .setMessage("Enter your post:")
                .setView(input)
                .setPositiveButton("OK", (dialog, which) -> {
                    String userInput = input.getText().toString();
                    addNewPost(userInput);
                })
                .setNegativeButton("Cancel", null)
                .show();
    }

    private void addNewPost(String text) {
        // Create a new message group layout
        LinearLayout newMessageGroup = new LinearLayout(this);
        LinearLayout.LayoutParams messageGroupLayoutParams = new LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.MATCH_PARENT,
                LinearLayout.LayoutParams.WRAP_CONTENT);
        messageGroupLayoutParams.setMargins(10, 0, 10, 30); // Set margins
        newMessageGroup.setLayoutParams(messageGroupLayoutParams);
        newMessageGroup.setBackgroundResource(R.drawable.rounded_posts_shape);

        // Create a GridLayout for the message content
        GridLayout gridLayout = new GridLayout(this);
        gridLayout.setLayoutParams(new LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.MATCH_PARENT,
                LinearLayout.LayoutParams.WRAP_CONTENT)); // Match parent height for content
        gridLayout.setColumnCount(3); // Set to 3 columns
        gridLayout.setRowCount(2); // Set to 2 rows
        gridLayout.setPadding(0, 0, 0, 0);

        // Create TextView for the time ("1 hour ago")
        TextView timeTextView = new TextView(this);
        timeTextView.setTextColor(getResources().getColor(R.color.timeText));
        timeTextView.setText("1 hour ago");
        timeTextView.setLayoutParams(new GridLayout.LayoutParams(
                GridLayout.spec(0), GridLayout.spec(0))); // 1st row, 1st column

        // Create TextView for the duration ("21 hours")
        TextView durationTextView = new TextView(this);
        durationTextView.setTextColor(getResources().getColor(R.color.timeText));
        durationTextView.setText("21 hours");
        durationTextView.setLayoutParams(new GridLayout.LayoutParams(
                GridLayout.spec(0), GridLayout.spec(2))); // 1st row, 2nd column

        // Create ImageView for the backward time icon
        ImageView backwardTimeImageView = new ImageView(this);
        backwardTimeImageView.setImageResource(R.drawable.backward_time);
        backwardTimeImageView.setScaleType(ImageView.ScaleType.CENTER_INSIDE); // Adjust scale type
        GridLayout.LayoutParams imageParams = new GridLayout.LayoutParams(
                GridLayout.spec(0), GridLayout.spec(1)); // 1st row, 1st column
        imageParams.width = 48; // Set a specific width (adjust as needed)
        imageParams.height = 48; // Set a specific height (adjust as needed)
        imageParams.setMargins(60, 0, 0, 0); // Adjust margins to position correctly
        backwardTimeImageView.setLayoutParams(imageParams);

        // Create TextView for the post content
        TextView postTextView = new TextView(this);
        postTextView.setText(text);
        postTextView.setTextSize(20);
        GridLayout.LayoutParams postParams = new GridLayout.LayoutParams(
                GridLayout.spec(1), GridLayout.spec(0, 3)); // 2nd row, spanning 3 columns
        postParams.width = GridLayout.LayoutParams.MATCH_PARENT;
        postParams.height = GridLayout.LayoutParams.WRAP_CONTENT;
        postTextView.setLayoutParams(postParams);
        postTextView.setGravity(Gravity.START); // Align to start

        // Add views to the GridLayout
        gridLayout.addView(timeTextView); // First row, first column
        gridLayout.addView(backwardTimeImageView); // First row, second column
        gridLayout.addView(durationTextView); // First row, third column
        gridLayout.addView(postTextView); // Second row, spanning 3 columns

        // Add the GridLayout to the new message group
        newMessageGroup.addView(gridLayout);

        // Finally, add the new message group to the container
        messageContainer.addView(newMessageGroup);
    }





}
