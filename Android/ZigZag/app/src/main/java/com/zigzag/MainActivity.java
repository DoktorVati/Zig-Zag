package com.zigzag;

import android.Manifest;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.Color;
import android.graphics.drawable.Drawable;
import android.os.Bundle;
import android.os.Handler;
import android.text.InputType;
import android.text.Spannable;
import android.text.SpannableString;
import android.text.method.LinkMovementMethod;
import android.text.style.ClickableSpan;
import android.text.style.ForegroundColorSpan;
import android.text.style.UnderlineSpan;
import android.util.Log;
import android.view.KeyEvent;
import android.view.View;
import android.view.inputmethod.EditorInfo;
import android.view.inputmethod.InputMethodManager;
import android.widget.EditText;
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

import java.io.IOException;
import java.io.OutputStream;
import java.net.HttpURLConnection;
import java.net.URL;
import java.nio.charset.StandardCharsets;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.Response;

public class MainActivity extends AppCompatActivity {
    private RelativeLayout mapImage;
    private EditText headerTextView;
    private static final String BASE_URL = "https://api.zigzag.madebysaul.com/posts?";

    private static final int LOCATION_REQUEST_CODE = 101;
    private FusedLocationProviderClient fusedLocationClient;
    private LinearLayout messageContainer; // Container for the post message groups
    private ImageButton button;
    private static final String DEFAULT_TAG = "Zig Zag"; // Default tag


    // These variables are for caching the previous locations so that if the user spams
    // the buttons it wont call the api a million times.
    private double lastLatitude;
    private double lastLongitude;
    private int lastZoomLevel = 1;
    private int zoomLevel = 12; // This is the zoom modifier for the map
    private String key = "AIzaSyAvNciAUallXrKrOjyS_8YZUVF5hxRLTk0"; // Use your API key
    //private String key = "AIzaSyCo18BB_aVNvFECgWGoXqEMS9Odqw1vgX4";
    private Handler handler = new Handler(); // Handler for delays
    TextView clearTagTextView;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main); // Link to activity_main.xml layout

        // Retrieve the phone number from the intent
        Intent intent = getIntent();
        String userPhone = intent.getStringExtra("USER_PHONE");

        messageContainer = findViewById(R.id.messageContainer);
        button = findViewById(R.id.button);

        headerTextView = findViewById(R.id.headerTextView);
        headerTextView.setText(DEFAULT_TAG);
        TextView clearTagTextView = findViewById(R.id.clearTag);

        mapImage = findViewById(R.id.mapImage);

        fusedLocationClient = LocationServices.getFusedLocationProviderClient(this);
        getUserLocation();

        // This is the TextView for the tag input
        TextView clickableText = new TextView(this);
        clickableText.setText(headerTextView.getText());
        clickableText.setTextSize(20);
        clickableText.setTextColor(Color.BLACK);
        clickableText.setPadding(20, 20, 20, 20);
        // Click listener for the tag input
        clickableText.setOnClickListener(v -> {
            closeKeyboard();
            int distance = 820;
             if (lastZoomLevel == 18) {
                distance = 100;
            }
            else if (lastZoomLevel == 12) {
                distance = 40000;
            }
            else if (lastZoomLevel == 8) {
                distance = 800000;
            }
            else distance = 820;
            checkAndFetchPosts(lastLatitude, lastLongitude, distance);
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

        scaleButton(findViewById(R.id.nearby));
        handler.postDelayed(this::showNearby, 2000);

        // Post button
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
            return; // Skip API call to prevent useless calls
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
        //fetchPosts(lastLatitude, lastLongitude, 100);
        checkAndFetchPosts(lastLatitude, lastLongitude, 100); // Distance is in meters

        getUserLocation(); // Refresh location to update the map
    }

    private void showNearby() {
        zoomLevel = 15; // Default nearby zoom
        //fetchPosts(lastLatitude, lastLongitude, 820);
        checkAndFetchPosts(lastLatitude, lastLongitude, 820); // Distance is in meters
        getUserLocation(); // Refresh location to update the map
    }

    private void zoomToUserArea() {
        zoomLevel = 12; // User area zoom
        //fetchPosts(lastLatitude, lastLongitude, 40000);
        checkAndFetchPosts(lastLatitude, lastLongitude, 40000); // Distance is in meters

        getUserLocation(); // Refresh location to update the map
    }

    private void zoomOut() {
        zoomLevel = 8; // Further zoom out
        //fetchPosts(lastLatitude, lastLongitude, 800000);
        checkAndFetchPosts(lastLatitude, lastLongitude, 800000); // Distance is in meters

        getUserLocation(); // Refresh location to update the map
    }
    // Increase scale of the button to show that it is selected.
    private void scaleButton(View button) {
        button.setScaleX(1.2f);
        button.setScaleY(1.2f);
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

    // This is called first when a tag is entered or when they change the radius.
    private void checkAndFetchPosts(double latitude, double longitude, int distance) {
        String userInput = headerTextView.getText().toString().trim();
        TextView clearTagTextView = findViewById(R.id.clearTag);

        // Check if the user has changed the default tag or input is empty
        if (userInput.isEmpty() || userInput.equals(DEFAULT_TAG)) {
            // Fetch all posts in the radius without hashtags
            clearTagTextView.setVisibility(View.GONE);
            fetchPosts(latitude, longitude, distance);
        } else if (userInput.startsWith("#")) {
            String hashtag = userInput.substring(1);
            // Fetch posts based on the hashtag
            clearTagTextView.setVisibility(View.VISIBLE);
            fetchPostsWithHashtag(latitude, longitude, distance, hashtag);
        } else {
            String hashtag = userInput;
            clearTagTextView.setVisibility(View.VISIBLE); // Show the clear tag button
            // Fetch posts based on the input without hashtags
            fetchPostsWithHashtag(latitude, longitude, distance, hashtag);
        }
        clearTagTextView.setOnClickListener(v -> {
            headerTextView.setText(DEFAULT_TAG); // Reset headerTextView text to DEFAULT_TAG
            clearTagTextView.setVisibility(View.GONE); // Hide clear tag after resetting
            fetchPosts(latitude, longitude, distance);
        });
    }
    // Fetch posts from backend
    private void fetchPostsWithHashtag(double latitude, double longitude, int distance, String hashtag) {
        String url = BASE_URL + "latitude=" + latitude + "&longitude=" + longitude + "&distance=" + distance + "&hashtag=" + hashtag;

        Log.d("FetchPosts", "Request URL: " + url);

        new Thread(() -> {
            OkHttpClient client = new OkHttpClient();
            Request request = new Request.Builder()
                    .url(url) // Ensure this URL uses https
                    .build();

            try (Response response = client.newCall(request).execute()) {
                if (response.isSuccessful() && response.body() != null) {
                    String jsonResponse = response.body().string();
                    Log.d("FetchPosts", "Received response: " + jsonResponse);
                    handlePostsResponse(jsonResponse);
                } else {
                    Log.e("MainActivity", "Error fetching posts: " + response.code() + " " + response.message());
                }
            } catch (Exception e) {
                Log.e("MainActivity", "Exception: fetch hashtag ", e);
            }
        }).start();
    }


    // This closes the android keyboard after entering / returning
    private void closeKeyboard() {
        InputMethodManager imm = (InputMethodManager) getSystemService(Context.INPUT_METHOD_SERVICE);
        if (getCurrentFocus() != null) {
            imm.hideSoftInputFromWindow(getCurrentFocus().getWindowToken(), 0);
        }
    }

    // Sets mapImage to the Google Maps Image API
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

    // Request perms for location usage
    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        if (requestCode == LOCATION_REQUEST_CODE) {
            if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                getUserLocation();
            } else {
                Toast.makeText(this, "Location permission denied, Please Enable Locations", Toast.LENGTH_SHORT).show();
            }
        }
    }

    // This is what comes up when they click the post button.
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
        inputPost.setPadding(20, 20, 20, 20); // Padding inside EditText

        // Set input type to allow text and emojis
        inputPost.setInputType(InputType.TYPE_CLASS_TEXT | InputType.TYPE_TEXT_FLAG_MULTI_LINE | InputType.TYPE_TEXT_VARIATION_NORMAL);
        inputPost.setMaxLines(3); // Optional: limit number of lines

        layout.addView(inputPost);

        // Additional instructions
        TextView instructionText = new TextView(this);
        instructionText.setText("You can also add a tag (e.g., #tagname) at the end.");
        instructionText.setTextSize(16);
        instructionText.setPadding(0, 10, 0, 0);
        layout.addView(instructionText);

        // Create the dialog
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


    // This adds the post to the API
    private void addNewPost(String text) {
        String currentTime = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss.SSS'Z'", Locale.getDefault()).format(new Date());

        String jsonBody = String.format("{\"text\":\"%s\", \"author\":\"%s\", \"postLatitude\":%f, \"postLongitude\":%f}",
                text, "your_user_id_here", lastLatitude, lastLongitude);
        // This line below shows the posted zig immediately
        updateUIWithPost(text, "Just now", "0 feet");

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
                runOnUiThread(() -> Toast.makeText(MainActivity.this, "", Toast.LENGTH_SHORT).show());
            }
        }).start();
    }

    private void updateUIWithPost(String text, String currentTime, String distance) {
        // Create a new message group layout
        LinearLayout newMessageGroup = new LinearLayout(this);
        LinearLayout.LayoutParams messageGroupLayoutParams = new LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.MATCH_PARENT,
                LinearLayout.LayoutParams.WRAP_CONTENT);
        messageGroupLayoutParams.setMargins(10, 0, 10, 30);
        newMessageGroup.setLayoutParams(messageGroupLayoutParams);
        newMessageGroup.setBackgroundResource(R.drawable.rounded_posts_shape);

        // Create a RelativeLayout for the message content
        RelativeLayout relativeLayout = new RelativeLayout(this);
        relativeLayout.setLayoutParams(new LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.MATCH_PARENT,
                LinearLayout.LayoutParams.WRAP_CONTENT));

        // Create TextView for the current time
        TextView timeTextView = new TextView(this);
        timeTextView.setTextColor(getResources().getColor(R.color.timeText));
        timeTextView.setText(currentTime);
        timeTextView.setId(View.generateViewId()); // Generate unique ID
        RelativeLayout.LayoutParams timeParams = new RelativeLayout.LayoutParams(
                RelativeLayout.LayoutParams.WRAP_CONTENT,
                RelativeLayout.LayoutParams.WRAP_CONTENT);
        timeParams.addRule(RelativeLayout.ALIGN_PARENT_START);
        timeParams.addRule(RelativeLayout.ALIGN_PARENT_TOP);
        timeTextView.setLayoutParams(timeParams);

        // Create ImageView for the time icon
        ImageView backwardTimeImageView = new ImageView(this);
        backwardTimeImageView.setImageResource(R.drawable.backward_time);
        backwardTimeImageView.setScaleType(ImageView.ScaleType.CENTER_INSIDE);
        backwardTimeImageView.setId(View.generateViewId()); // Generate unique ID
        RelativeLayout.LayoutParams imageParams = new RelativeLayout.LayoutParams(
                48, 48);
        //imageParams.addRule(RelativeLayout.RIGHT_OF, timeTextView.getId());
        imageParams.setMargins(275, 0, 0, 0);
        backwardTimeImageView.setLayoutParams(imageParams);

        // Create TextView for the duration
        TextView durationTextView = new TextView(this);
        durationTextView.setTextColor(getResources().getColor(R.color.timeText));
        durationTextView.setText("10 hours left"); // Modify as needed
        durationTextView.setId(View.generateViewId()); // Generate unique ID
        RelativeLayout.LayoutParams durationParams = new RelativeLayout.LayoutParams(
                RelativeLayout.LayoutParams.WRAP_CONTENT,
                RelativeLayout.LayoutParams.WRAP_CONTENT);
        durationParams.addRule(RelativeLayout.CENTER_HORIZONTAL);
        durationParams.addRule(RelativeLayout.RIGHT_OF, backwardTimeImageView.getId());
        durationParams.setMargins(0, 5, 0, 0); // Reduced margin to decrease gap
        durationTextView.setLayoutParams(durationParams);

        // Create ImageButton for more options
        ImageButton moreButton = new ImageButton(this);
        moreButton.setImageResource(R.drawable.baseline_more_horiz_24);
        moreButton.setScaleX(1f);
        moreButton.setScaleY(1f);
        moreButton.setScaleType(ImageView.ScaleType.FIT_CENTER);
        moreButton.setBackgroundColor(Color.TRANSPARENT);

        moreButton.setId(View.generateViewId()); // Generate unique ID
        RelativeLayout.LayoutParams moreButtonParams = new RelativeLayout.LayoutParams(
                100, 160);
        //moreButtonParams.addRule(RelativeLayout.RIGHT_OF, backwardTimeImageView.getId());
        moreButtonParams.addRule(RelativeLayout.ALIGN_PARENT_TOP);
        moreButtonParams.addRule(RelativeLayout.ALIGN_PARENT_END);
        moreButtonParams.setMargins(0,-60,0,-30);
        moreButton.setLayoutParams(moreButtonParams);

        // Set click listener for the more button
        moreButton.setOnClickListener(v -> {
            Toast.makeText(this, "More options clicked!", Toast.LENGTH_SHORT).show();
        });

        // Create TextView for the post content
        TextView postTextView = new TextView(this);
        SpannableString spannableString = new SpannableString(text);
        Pattern pattern = Pattern.compile("#\\w+");
        Matcher matcher = pattern.matcher(text);
        while (matcher.find()) {
            int start = matcher.start();
            int end = matcher.end();
            spannableString.setSpan(new ForegroundColorSpan(Color.BLUE), start, end, Spannable.SPAN_EXCLUSIVE_EXCLUSIVE);
            spannableString.setSpan(new UnderlineSpan(), start, end, Spannable.SPAN_EXCLUSIVE_EXCLUSIVE);

            // Create a ClickableSpan for the hashtag
            final String hashtag = text.substring(start, end);
            spannableString.setSpan(new ClickableSpan() {
                @Override
                public void onClick(View widget) {
                    // Replace the headerTextView text with the selected hashtag
                    headerTextView.setText(hashtag); // Ensure headerTextView is defined

                    // Determine the distance based on the last zoom level
                    int distance;
                    switch (lastZoomLevel) {
                        case 18:
                            distance = 100;
                            break;
                        case 12:
                            distance = 40000;
                            break;
                        case 8:
                            distance = 800000;
                            break;
                        default:
                            distance = 820;
                            break;
                    }
                    Log.d("it has been called", "onClick: fetched hashtag posts");
                    // Fetch posts with the selected hashtag
                    checkAndFetchPosts(lastLatitude, lastLongitude, distance);
                }
            }, start, end, Spannable.SPAN_EXCLUSIVE_EXCLUSIVE);

        }
        postTextView.setText(spannableString);
        postTextView.setTextSize(20);
        postTextView.setMovementMethod(LinkMovementMethod.getInstance()); // Enable links
        RelativeLayout.LayoutParams postParams = new RelativeLayout.LayoutParams(
                RelativeLayout.LayoutParams.WRAP_CONTENT,
                RelativeLayout.LayoutParams.WRAP_CONTENT);
        postParams.addRule(RelativeLayout.BELOW, backwardTimeImageView.getId());
        postParams.setMargins(0, 20, 33, 0);
        postTextView.setLayoutParams(postParams);

        // Create TextView for the post distance
        TextView postDistanceView = new TextView(this);
        postDistanceView.setTextColor(getResources().getColor(R.color.timeText));
        postDistanceView.setText(distance);
        postDistanceView.setTextSize(14);
        postDistanceView.setId(View.generateViewId()); // Generate unique ID
        RelativeLayout.LayoutParams distanceParams = new RelativeLayout.LayoutParams(
                RelativeLayout.LayoutParams.WRAP_CONTENT,
                RelativeLayout.LayoutParams.WRAP_CONTENT);
        distanceParams.addRule(RelativeLayout.ALIGN_PARENT_END);
        distanceParams.addRule(RelativeLayout.ALIGN_PARENT_BOTTOM);
        distanceParams.addRule(RelativeLayout.BELOW, postTextView.getId());
        postParams.setMargins(0,25,0,0);
        postDistanceView.setLayoutParams(distanceParams);

        // Add views to the RelativeLayout
        relativeLayout.addView(timeTextView);
        relativeLayout.addView(moreButton);
        relativeLayout.addView(durationTextView);
        relativeLayout.addView(backwardTimeImageView);
        relativeLayout.addView(postTextView);
        relativeLayout.addView(postDistanceView);

        // Add the RelativeLayout to the new message group
        newMessageGroup.addView(relativeLayout);

        // and add the new message group to the container
        messageContainer.addView(newMessageGroup);
    }




    // Fetches posts based on distance
    private void fetchPosts(double latitude, double longitude, int distance) {
        String url = BASE_URL + "latitude=" + latitude + "&longitude=" + longitude + "&distance=" + distance;
        Log.d("FetchPosts", "Request URL: " + url + " latitude: " + latitude + " longitude: " + longitude + " distance: " + distance);

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
                    String errorBody = response.body() != null ? response.body().string() : "No response body";
                    Log.e("MainActivity", "Error fetching posts: " + response.code() + " " + response.message() + " Response body: " + errorBody);
                }
            } catch (IOException e) {
                Log.e("MainActivity", "Network error fetching posts: " + e.getMessage(), e);
            } catch (Exception e) {
                Log.e("MainActivity", "Exception fetching posts: " + e.getMessage(), e);
            }
        }).start();
    }



    private void handlePostsResponse(String jsonResponse) {
        runOnUiThread(() -> {
            try {
                Log.d("handlePostsResponse", "Response received");
                JSONArray postsArray = new JSONArray(jsonResponse);
                Log.d("This is the",jsonResponse);
                messageContainer.removeAllViews(); // Clear previous posts

                for (int i = 0; i < postsArray.length(); i++) {
                    JSONObject post = postsArray.getJSONObject(i);

                    String text = post.getString("text"); // Get the post text
                    String createdAt = post.getString("createdAt"); // Get the createdAt time

                    // Access the location object and then access the distance
                    JSONObject location = post.getJSONObject("location");
                    String distanceInMetersStr = location.getString("distance"); // Get the distance as a string

                    // Log the distance string
                    Log.d("Distance check", "Distance string: " + distanceInMetersStr);

                    // Convert the distance string to an integer
                    double distanceInMeters = Double.parseDouble(distanceInMetersStr);

                    // Format the createdAt time
                    String formattedTime = formatTime(createdAt);

                    // Convert distance and prepare display string
                    String distanceString = formatDistance(distanceInMeters); // Pass as int

                    // Update the UI with the post, formatted time, and distance
                    updateUIWithPost(text, formattedTime, distanceString);
                }
            } catch (JSONException e) {
                Log.e("MainActivity", "JSON Parsing Error: ", e);
            } catch (NumberFormatException e) {
                Log.e("MainActivity", "Distance conversion error: ", e);
            } catch (Exception e) {
                Log.e("MainActivity", "General error: ", e);
            }
        });
    }


    private String formatDistance(double meters) {
        // Convert meters to miles
        double miles = meters * 0.000621371;

        // Log the distance in meters and calculated miles
        Log.d("DistanceLogger", "Distance in meters: " + meters + ", calculated miles: " + miles);

        if (miles < 0.1) {
            double feet = meters * 3.28084; // Convert meters to feet
            Log.d("DistanceLogger", "Distance in feet: " + feet); // Log the feet distance
            return String.format("%.0f ft", feet);
        } else {
            return String.format("%.2f mi", miles);
        }
    }




    // This helps format the time that is received from the Backend API to show simple results
    private String formatTime(String createdAt) {
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