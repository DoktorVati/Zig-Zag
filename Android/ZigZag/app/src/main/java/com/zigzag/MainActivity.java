package com.zigzag;

import android.Manifest;
import android.content.Context;
import android.content.pm.PackageManager;
import android.graphics.Color;
import android.graphics.drawable.Drawable;
import android.os.Bundle;
import android.os.Handler;
import android.util.Log;
import android.view.Gravity;
import android.view.KeyEvent;
import android.view.View;
import android.view.inputmethod.EditorInfo;
import android.view.inputmethod.InputMethodManager;
import android.widget.EditText;
import android.widget.GridLayout;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
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

import org.jetbrains.annotations.Nullable;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.OutputStream;
import java.net.HttpURLConnection;
import java.net.URL;
import java.nio.charset.StandardCharsets;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;

import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.Response;

public class MainActivity extends AppCompatActivity {
    private RelativeLayout mapImage;
    private EditText headerTextView;
    private static final String BASE_URL = "https://api.zigzag.madebysaul.com/posts?";
    private static final int LOCATION_REQUEST_CODE = 101;
    private FusedLocationProviderClient fusedLocationClient;
    private LinearLayout messageContainer; // Container for the message groups
    private ImageButton button;
    private static final String DEFAULT_TAG = "Zig Zag"; // Default tag


    // These variables are for caching the previous locations so that if the user spams
    // the buttons it wont call the api a million times.
    private double lastLatitude;
    private double lastLongitude;
    private int lastZoomLevel = -1;
    private int zoomLevel = 12; // This is the zoom modifier for the map
    private String key = "AIzaSyAvNciAUallXrKrOjyS_8YZUVF5hxRLTk0"; // Use your API key
    private Handler handler = new Handler(); // Handler for delays

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main); // Link to activity_main.xml layout

        // Initialize the message container and button
        messageContainer = findViewById(R.id.messageContainer);
        button = findViewById(R.id.button);
        headerTextView = findViewById(R.id.headerTextView); // Update with your TextView ID
        headerTextView.setText(DEFAULT_TAG); // Set default text
        mapImage = findViewById(R.id.mapImage);

        fusedLocationClient = LocationServices.getFusedLocationProviderClient(this);
        getUserLocation();
// Create a clickable TextView for the tag input
        TextView clickableText = new TextView(this);
        clickableText.setText(headerTextView.getText());
        clickableText.setTextSize(20);
        clickableText.setTextColor(Color.BLACK);
        clickableText.setPadding(20, 20, 20, 20);
        // Click listener for the tag input
        clickableText.setOnClickListener(v -> {
            closeKeyboard();
            checkAndFetchPosts(lastLatitude, lastLongitude, 40000);
        });

        // Key listener for headerTextView
        headerTextView.setOnEditorActionListener((v, actionId, event) -> {
            if (actionId == EditorInfo.IME_ACTION_DONE || (event != null && event.getKeyCode() == KeyEvent.KEYCODE_ENTER)) {
                closeKeyboard();
                checkAndFetchPosts(lastLatitude, lastLongitude, 800000);
                return true;
            }
            return false;
        });

        findViewById(R.id.close).setOnClickListener(v -> {
            resetButtonScales();
            scaleButton(v);
            zoomIn();
        });

        findViewById(R.id.nearby).setOnClickListener(v -> {
            resetButtonScales();
            scaleButton(v);
            showNearby();
        });

        findViewById(R.id.userArea).setOnClickListener(v -> {
            resetButtonScales();
            scaleButton(v);
            zoomToUserArea();
        });

        findViewById(R.id.global).setOnClickListener(v -> {
            resetButtonScales();
            scaleButton(v);
            zoomOut();
        });

        scaleButton(findViewById(R.id.close));
        handler.postDelayed(this::zoomIn, 2000);

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
                    .addOnSuccessListener(this, location -> {
                        if (location != null) {
                            double latitude = location.getLatitude();
                            double longitude = location.getLongitude();
                            updateMap(latitude, longitude);
                        }
                    });
        } else {
            requestPermissions(new String[]{Manifest.permission.ACCESS_FINE_LOCATION}, LOCATION_REQUEST_CODE);
        }


    }


    private void updateMap(double latitude, double longitude) {
        // Check if the location or zoom level has changed
        if (latitude == lastLatitude && longitude == lastLongitude && zoomLevel == lastZoomLevel) {
            return; // Skip API call
        }

        lastLatitude = latitude;
        lastLongitude = longitude;
        lastZoomLevel = zoomLevel;

        String apiKey = /*null*/key;
        String url = "https://maps.googleapis.com/maps/api/staticmap?center="
                + latitude + "," + longitude + "&zoom=" + zoomLevel + "&size=200x200&key=" + apiKey;
        loadImageAsBackground(url);

    }


//california is 3801 km away
    private void zoomIn() {
        zoomLevel = 18; // Closer zoom
        //fetchPosts(lastLatitude, lastLongitude, 100); // Distance is in meters
        checkAndFetchPosts(lastLatitude, lastLongitude, 100); // Distance is in meters

        getUserLocation(); // Refresh location to update the map
    }

    private void showNearby() {
        zoomLevel = 15; // Default nearby zoom
        //fetchPosts(lastLatitude, lastLongitude, 820); // Distance is in meters
        checkAndFetchPosts(lastLatitude, lastLongitude, 820); // Distance is in meters
        getUserLocation(); // Refresh location to update the map
    }

    private void zoomToUserArea() {
        zoomLevel = 12; // User area zoom
        //fetchPosts(lastLatitude, lastLongitude, 40000); // Distance is in meters
        checkAndFetchPosts(lastLatitude, lastLongitude, 40000); // Distance is in meters

        getUserLocation(); // Refresh location to update the map
    }

    private void zoomOut() {
        zoomLevel = 8; // Further zoom out
        //fetchPosts(lastLatitude, lastLongitude, 800000); // Distance is in meters
        checkAndFetchPosts(lastLatitude, lastLongitude, 800000); // Distance is in meters

        getUserLocation(); // Refresh location to update the map
    }
    // Increase scale of the button to show that it is selected.
    private void scaleButton(View button) {
        button.setScaleX(1.2f); // Scale up X
        button.setScaleY(1.2f); // Scale up Y
    }
//this will set the buttons back to normal scale
    private void resetButtonScales() {
        // Reset scale for all buttons
        findViewById(R.id.close).setScaleX(0.8f);
        findViewById(R.id.close).setScaleY(0.8f);

        findViewById(R.id.nearby).setScaleX(0.8f);
        findViewById(R.id.nearby).setScaleY(0.8f);

        findViewById(R.id.userArea).setScaleX(0.8f);
        findViewById(R.id.userArea).setScaleY(0.8f);

        findViewById(R.id.global).setScaleX(0.8f);
        findViewById(R.id.global).setScaleY(0.8f);
    }
    private void checkAndFetchPosts(double latitude, double longitude, int distance) {
        String userInput = headerTextView.getText().toString().trim();

        // Check if the user has changed the default tag or input is empty
        if (userInput.isEmpty() || userInput.equals(DEFAULT_TAG)) {
            // Fetch all posts in the radius without hashtags
            fetchPosts(latitude, longitude, distance);
        } else if (userInput.startsWith("#")) {
            String hashtag = userInput.substring(1);
            // Fetch posts based on the hashtag
            fetchPostsWithHashtag(latitude, longitude, distance, hashtag);
        } else {
            String hashtag = userInput;
            // Fetch posts based on the input without hashtags
            fetchPostsWithHashtag(latitude, longitude, distance, hashtag);
        }
    }
    private void fetchPostsWithHashtag(double latitude, double longitude, int distance, String hashtag) {
        String url = BASE_URL + "latitude=" + latitude + "&longitude=" + longitude + "&distance=" + distance + "&hashtag=" + hashtag;

        new Thread(() -> {
            OkHttpClient client = new OkHttpClient();
            Request request = new Request.Builder()
                    .url(url)
                    .build();

            try (Response response = client.newCall(request).execute()) {
                if (response.isSuccessful() && response.body() != null) {
                    String jsonResponse = response.body().string();
                    handlePostsResponse(jsonResponse);
                } else {
                    Log.e("MainActivity", "Error fetching posts: " + response.message());
                }
            } catch (Exception e) {
                Log.e("MainActivity", "Exception: ", e);
            }
        }).start();
    }
    private void closeKeyboard() {
        InputMethodManager imm = (InputMethodManager) getSystemService(Context.INPUT_METHOD_SERVICE);
        if (getCurrentFocus() != null) {
            imm.hideSoftInputFromWindow(getCurrentFocus().getWindowToken(), 0);
        }
    }
    private void loadImageAsBackground(String url) {
        Glide.with(this)
                .load(url)
                .into(new CustomTarget<Drawable>() {
                    @Override
                    public void onResourceReady(@NonNull Drawable resource, @Nullable Transition<? super Drawable> transition) {
                        mapImage.setBackground(resource);
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
        // Create a LinearLayout to hold the EditText for the post
        LinearLayout layout = new LinearLayout(this);
        layout.setOrientation(LinearLayout.VERTICAL);
        layout.setPadding(40, 40, 40, 40); // Add padding for a nicer look

        // Create an EditText for the post message
        final EditText inputPost = new EditText(this);
        inputPost.setHint("What's on your mind?");
        inputPost.setTextSize(18);
        inputPost.setTextColor(getResources().getColor(R.color.black));
        //inputPost.setBackgroundResource(R.drawable.rounded_posts_shape);
        inputPost.setPadding(20, 20, 20, 20); // Padding inside EditText
        layout.addView(inputPost);

        // Create a TextView for additional instructions (optional)
        TextView instructionText = new TextView(this);
        instructionText.setText("You can also add a tag (e.g., #tagname) at the end.");
        instructionText.setTextSize(16);
        instructionText.setPadding(0, 10, 0, 0); // Padding for the TextView
        layout.addView(instructionText);

        // Create the dialog with a professional appearance
        new AlertDialog.Builder(this)
                .setTitle("Create a New Post")
                .setView(layout)
                .setPositiveButton("Post", (dialog, which) -> {
                    String userInput = inputPost.getText().toString().trim();
                    if (!userInput.isEmpty()) {
                        addNewPost(userInput);
                    } else {
                        Toast.makeText(this, "Please enter a message before posting.", Toast.LENGTH_SHORT).show();
                    }
                })
                .setNegativeButton("Cancel", null)
                .show();
    }


    private void addNewPost(String text) {
        String currentTime = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss.SSS'Z'", Locale.getDefault()).format(new Date());

        String jsonBody = String.format("{\"text\":\"%s\", \"author\":\"%s\", \"postLatitude\":%f, \"postLongitude\":%f}",
                text, "your_user_id_here", lastLatitude, lastLongitude);
        // This line below is temporary to show posts, delete on showing
        updateUIWithPost(text, "Just now");

        new Thread(() -> {
            try {
                URL url = new URL("https://api.zigzag.madebysaul.com/posts/?latitude=" + lastLatitude + "&longitude=" + lastLongitude);
                HttpURLConnection connection = (HttpURLConnection) url.openConnection();
                connection.setRequestMethod("POST");
                connection.setRequestProperty("Content-Type", "application/json");
                connection.setDoOutput(true);

                try (OutputStream os = connection.getOutputStream()) {
                    byte[] input = jsonBody.getBytes(StandardCharsets.UTF_8);
                    os.write(input, 0, input.length);
                }

                int responseCode = connection.getResponseCode();
                if (responseCode == HttpURLConnection.HTTP_OK) {
                    runOnUiThread(() -> {
                        Toast.makeText(MainActivity.this, "Post created successfully!", Toast.LENGTH_SHORT).show();
                        // Call the method to update the UI with the new post
                        //updateUIWithPost(text, currentTime);
                    });
                } else {
                    //runOnUiThread(() -> Toast.makeText(MainActivity.this, "Failed to create post", Toast.LENGTH_SHORT).show());
                }
            } catch (Exception e) {
                e.printStackTrace();
                runOnUiThread(() -> Toast.makeText(MainActivity.this, "Error occurred", Toast.LENGTH_SHORT).show());
            }
        }).start();
    }


    private void updateUIWithPost(String text, String currentTime) {
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
                LinearLayout.LayoutParams.WRAP_CONTENT));
        gridLayout.setColumnCount(4);
        gridLayout.setRowCount(2);
        gridLayout.setPadding(0, -5, 0, 0);

        // Create TextView for the time (using current time)
        TextView timeTextView = new TextView(this);
        timeTextView.setTextColor(getResources().getColor(R.color.timeText));
        timeTextView.setText(currentTime); // You may want to format this to show "1 hour ago"
        timeTextView.setLayoutParams(new GridLayout.LayoutParams(
                GridLayout.spec(0), GridLayout.spec(0))); // 1st row, 1st column

        // Create TextView for the duration (you can set this based on your logic)
        TextView durationTextView = new TextView(this);
        durationTextView.setTextColor(getResources().getColor(R.color.timeText));
        durationTextView.setText("10 hours left"); // Modify as needed
        durationTextView.setLayoutParams(new GridLayout.LayoutParams(
                GridLayout.spec(0), GridLayout.spec(2))); // 1st row, 2nd column

        // Create ImageView for the backward time icon
        ImageView backwardTimeImageView = new ImageView(this);
        backwardTimeImageView.setImageResource(R.drawable.backward_time);
        backwardTimeImageView.setScaleType(ImageView.ScaleType.CENTER_INSIDE);
        GridLayout.LayoutParams imageParams = new GridLayout.LayoutParams(
                GridLayout.spec(0), GridLayout.spec(1)); // 1st row, 1st column
        imageParams.width = 48;
        imageParams.height = 48;
        imageParams.setMargins(60, 0, 0, 0);
        backwardTimeImageView.setLayoutParams(imageParams);

        // Create ImageButton for more options
        ImageButton moreButton = new ImageButton(this);
        moreButton.setImageResource(R.drawable.baseline_more_horiz_24);
        moreButton.setScaleX(1f);
        moreButton.setScaleY(1f);
        moreButton.setScaleType(ImageView.ScaleType.FIT_XY);
        moreButton.setBackgroundColor(Color.TRANSPARENT);
            // Set layout parameters to match the XML attributes
        GridLayout.LayoutParams moreButtonParams = new GridLayout.LayoutParams(
                GridLayout.spec(0), GridLayout.spec(3)); // Adjust to be in the correct column
        moreButtonParams.width = 100; // Set appropriate width
        moreButtonParams.height = 160; // Set appropriate height
        moreButtonParams.setMargins(0, -60, 20, -30); // Adjust margins if necessary
        moreButtonParams.setGravity(Gravity.END); // This sets the gravity to end
        //moreButton.setId(View.generateViewId()); // Generate a unique ID if needed
        moreButton.setLayoutParams(moreButtonParams);

        // Set click listener
        moreButton.setOnClickListener(v -> {
            Toast.makeText(this, "More options clicked!", Toast.LENGTH_SHORT).show();
            // Add any additional functionality here
        });


        // Create TextView for the post content
        TextView postTextView = new TextView(this);
        postTextView.setText(text);
        postTextView.setTextSize(20);
        GridLayout.LayoutParams postParams = new GridLayout.LayoutParams(
                GridLayout.spec(1), GridLayout.spec(0, 4)); // 2nd row, spanning 3 columns
        postParams.width = GridLayout.LayoutParams.MATCH_PARENT;
        postParams.height = GridLayout.LayoutParams.WRAP_CONTENT;
        postTextView.setLayoutParams(postParams);
        postTextView.setGravity(Gravity.START);

        // Add views to the GridLayout
        gridLayout.addView(timeTextView);
        gridLayout.addView(backwardTimeImageView);
        gridLayout.addView(durationTextView);
        gridLayout.addView(moreButton);
        gridLayout.addView(postTextView);

        // Add the GridLayout to the new message group
        newMessageGroup.addView(gridLayout);

        // Finally, add the new message group to the container
        messageContainer.addView(newMessageGroup);
    }


    private void fetchPosts(double latitude, double longitude, int distance) {
        String url = BASE_URL + "latitude=" + latitude + "&longitude=" + longitude + "&distance=" + distance;

        new Thread(() -> {
            OkHttpClient client = new OkHttpClient();
            Request request = new Request.Builder()
                    .url(url)
                    .build();

            try (Response response = client.newCall(request).execute()) {
                if (response.isSuccessful() && response.body() != null) {
                    String jsonResponse = response.body().string();
                    handlePostsResponse(jsonResponse);
                } else {
                    Log.e("MainActivity", "Error fetching posts: " + response.message());
                }
            } catch (Exception e) {
                Log.e("MainActivity", "Exception: ", e);
            }
        }).start();
    }

    private void handlePostsResponse(String jsonResponse) {
        runOnUiThread(() -> {
            try {
                JSONArray postsArray = new JSONArray(jsonResponse);
                messageContainer.removeAllViews(); // Clear previous posts

                for (int i = 0; i < postsArray.length(); i++) {
                    JSONObject post = postsArray.getJSONObject(i);

                    String text = post.getString("text"); // Get the post text
                    String createdAt = post.getString("createdAt"); // Get the createdAt time

                    // Format the createdAt time
                    String formattedTime = formatTime(createdAt);

                    // Update the UI with the post and formatted time
                    updateUIWithPost(text, formattedTime);
                }
            } catch (JSONException e) {
                Log.e("MainActivity", "JSON Parsing Error: ", e);
            }
        });
    }

    private String formatTime(String createdAt) {
        // Parse the createdAt time and convert it to a "time ago" format
        try {
            SimpleDateFormat inputFormat = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss.SSS'Z'", Locale.getDefault());
            Date date = inputFormat.parse(createdAt);

            // Calculate the time difference
            long diffInMillis = new Date().getTime() - date.getTime();
            long diffInMinutes = diffInMillis / (1000 * 60);
            long diffInHours = diffInMinutes / 60;
            long diffInDays = diffInHours / 24;

            // Generate the appropriate time ago string
            if (diffInMinutes < 1) {
                return "Just now";
            } else if (diffInMinutes < 60) {
                return diffInMinutes + " minute" + (diffInMinutes > 1 ? "s" : "") + " ago";
            } else if (diffInHours < 24) {
                return diffInHours + " hour" + (diffInHours > 1 ? "s" : "") + " ago";
            } else {
                return diffInDays + " day" + (diffInDays > 1 ? "s" : "") + " ago";
            }
        } catch (Exception e) {
            Log.e("MainActivity", "Date parsing error: ", e);
            return "Unknown time";
        }
    }




}
