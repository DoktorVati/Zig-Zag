package com.zigzag;


import static com.zigzag.ProfileCreation.KEY_PHONE_NUMBER;
import static com.zigzag.ProfileLogin.KEY_EMAIL;

import android.Manifest;
import android.app.AlarmManager;
import android.app.AlertDialog;
import android.app.Dialog;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.content.res.Configuration;
import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.graphics.drawable.Drawable;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.text.Spannable;
import android.text.SpannableString;
import android.text.method.LinkMovementMethod;
import android.text.style.ClickableSpan;
import android.text.style.ForegroundColorSpan;
import android.text.style.UnderlineSpan;
import android.util.Log;
import android.view.KeyEvent;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.view.Window;
import android.view.inputmethod.EditorInfo;
import android.view.inputmethod.InputMethodManager;
import android.widget.ArrayAdapter;
import android.widget.BaseAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.PopupMenu;
import android.widget.RelativeLayout;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;

import com.bumptech.glide.Glide;
import com.bumptech.glide.request.target.CustomTarget;
import com.bumptech.glide.request.transition.Transition;
import com.google.android.gms.location.FusedLocationProviderClient;
import com.google.android.gms.location.LocationServices;
import com.google.firebase.auth.AuthCredential;
import com.google.firebase.auth.EmailAuthProvider;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;

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
import java.util.Calendar;
import java.util.Date;
import java.util.List;
import java.util.Locale;
import java.util.Objects;
import java.util.TimeZone;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.Response;


public class MainActivity extends AppCompatActivity {
    private RelativeLayout mapImage;
    private EditText headerTextView;
    private static final String BASE_URL = "https://api.zigzag.madebysaul.com/posts?";
    private String orderBy = "&orderBy=";
    private static final String CHANNEL_ID = "user_channel_id";
    private static final CharSequence CHANNEL_NAME = "My Notifications";
    private static final int LOCATION_REQUEST_CODE = 101;
    private FusedLocationProviderClient fusedLocationClient;
    private LinearLayout messageContainer; // Container for the post message groups
    private ImageButton button;
    private ImageButton profileButton;
    private ImageButton sortingButton;
    private ImageButton close;
    private ImageButton nearby;
    private ImageButton userArea;
    private ImageButton global;
    private LinearLayout OPContainer;
    private LinearLayout CommentsContainer;
    private static final String DEFAULT_TAG = "Zig Zag"; // Default tag
    private String UserId;


    // These variables are for caching the previous locations so that if the user spams
    // the buttons it wont call the api a million times.
    private double lastLatitude;
    private double lastLongitude;
    private int lastZoomLevel = 1;
    private int zoomLevel = 12; // This is the zoom modifier for the map
    private String key = "AIzaSyAvNciAUallXrKrOjyS_8YZUVF5hxRLTk0"; //  our map api key
    //private String key = "AIzaSyCo18BB_aVNvFECgWGoXqEMS9Odqw1vgX4";
    private Handler handler = new Handler();


    //Their user ID
    private String my_user_id;
    TextView clearTagTextView;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        // Schedule the daily notification
        scheduleDailyNotification();

        // Retrieve the UUID from the intent
        Intent intent = getIntent();
        UserId = intent.getStringExtra("USER_ID");
        my_user_id = UserId;


        messageContainer = findViewById(R.id.messageContainer);
        button = findViewById(R.id.button);
        profileButton = findViewById(R.id.profileButton);

        sortingButton = findViewById(R.id.sortingButton);

        close = findViewById(R.id.close);
        nearby = findViewById(R.id.nearby);
        userArea = findViewById(R.id.userArea);
        global = findViewById(R.id.global);

        headerTextView = findViewById(R.id.headerTextView);
        headerTextView.setText("");
        TextView clearTagTextView = findViewById(R.id.clearTag);


        mapImage = findViewById(R.id.mapImage);

        // Create notification channel for Android 8.0 and above
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            NotificationChannel channel = new NotificationChannel(
                    CHANNEL_ID,
                    CHANNEL_NAME,
                    NotificationManager.IMPORTANCE_DEFAULT
            );
            NotificationManager notificationManager = getSystemService(NotificationManager.class);
            if (notificationManager != null) {
                notificationManager.createNotificationChannel(channel);
            }
        }

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


            int distance = getDistanceBasedOnZoom();
            checkAndFetchPosts(lastLatitude, lastLongitude, distance);
        });


        // Key listener for headerTextView
        headerTextView.setOnEditorActionListener((v, actionId, event) -> {
            if (actionId == EditorInfo.IME_ACTION_DONE || (event != null && event.getKeyCode() == KeyEvent.KEYCODE_ENTER)) {
                closeKeyboard();
                int distance = getDistanceBasedOnZoom();
                checkAndFetchPosts(lastLatitude, lastLongitude, distance);
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


        // Profile button
        profileButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                showProfileDialog();
            }
        });

        // Sorting button
        sortingButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v){showSortingDialog();}
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


        zoomLevel = 15; // Closer zoom
        //fetchPosts(lastLatitude, lastLongitude, 100);
        checkAndFetchPosts(lastLatitude, lastLongitude, 800); // Distance is in meters
        close.setColorFilter(Color.parseColor("#23C6F4"));

        getUserLocation(); // Refresh location to update the map
    }


    private void showNearby() {
        zoomLevel = 13; // Default nearby zoom
        //fetchPosts(lastLatitude, lastLongitude, 820);
        nearby.setColorFilter(Color.parseColor("#23C6F4"));
        checkAndFetchPosts(lastLatitude, lastLongitude, 5000); // Distance is in meters
        getUserLocation(); // Refresh location to update the map
    }


    private void zoomToUserArea() {
        zoomLevel = 11; // User area zoom
        userArea.setColorFilter(Color.parseColor("#23C6F4"));
        //fetchPosts(lastLatitude, lastLongitude, 40000);
        checkAndFetchPosts(lastLatitude, lastLongitude, 40000); // Distance is in meters
        getUserLocation(); // Refresh location to update the map
    }


    private void zoomOut() {
        zoomLevel = 7; // Further zoom out
        //fetchPosts(lastLatitude, lastLongitude, 800000);
        checkAndFetchPosts(lastLatitude, lastLongitude, 800000); // Distance is in meters
        global.setColorFilter(Color.parseColor("#23C6F4"));

        getUserLocation(); // Refresh location to update the map
    }


    // Increase scale of the button to show that it is selected.
    private void scaleButton(View button) {
        button.setScaleX(1.2f);
        button.setScaleY(1.2f);
    }


    //this will set the buttons back to normal scale
    private void resetButtonScales() {

        // Reset the colors for each icon
        close.setColorFilter(Color.parseColor("#000000"));
        nearby.setColorFilter(Color.parseColor("#000000"));
        userArea.setColorFilter(Color.parseColor("#000000"));
        global.setColorFilter(Color.parseColor("#000000"));

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
            headerTextView.setText(""); // Reset headerTextView text to DEFAULT_TAG
            clearTagTextView.setVisibility(View.GONE); // Hide clear tag after resetting
            fetchPosts(latitude, longitude, distance);
        });
    }


    // Fetch posts from backend
    private void fetchPostsWithHashtag(double latitude, double longitude, int distance, String hashtag) {
        String url = BASE_URL + "latitude=" + latitude + "&longitude=" + longitude + "&distance=" + distance + "&hashtag=" + hashtag + orderBy;


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
                    handlePostsResponse(jsonResponse, messageContainer, false);
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


    private void showInputDialog() {
        // Create a full-screen dialog
        Dialog dialog = new Dialog(this);
        dialog.requestWindowFeature(Window.FEATURE_NO_TITLE);
        dialog.setContentView(R.layout.dialog_create_post);

        // Set the dialog to full-screen
        dialog.getWindow().setLayout(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.MATCH_PARENT);
        dialog.getWindow().setBackgroundDrawable(new ColorDrawable(Color.TRANSPARENT));

        // Initialize views
        EditText inputPost = dialog.findViewById(R.id.inputPost);
        Spinner typeDropdown = dialog.findViewById(R.id.typeDropdown);
        EditText durationStr = dialog.findViewById(R.id.durationStr);
        Button postButton = dialog.findViewById(R.id.postButton);
        ImageButton cancelButton = dialog.findViewById(R.id.cancelButton);

        // Create a CurseWordFilter instance with the Activity context
        CurseWordFilter filter = new CurseWordFilter(this);
        List<String> curseWords = filter.loadCurseWords();  // Load curse words from the CSV

        // Set up the dropdown menu
        String[] types = new String[]{"days", "hours", "minutes"};
        ArrayAdapter<String> adapter = new ArrayAdapter<>(this, android.R.layout.simple_spinner_dropdown_item, types);
        typeDropdown.setAdapter(adapter);

        // Set button listeners
        postButton.setOnClickListener(v -> {
            String userInput = inputPost.getText().toString().trim();
            Integer duration = null;  // Default to null for "forever"

            // If the user input is empty after cleaning, show a toast message
            if (userInput.isEmpty()) {
                Toast.makeText(this, "Please enter a message before posting.", Toast.LENGTH_SHORT).show();
                return;
            }
            if (userInput.length() >= 225)
            {
                Toast.makeText(this, "Message must be below 225 characters!", Toast.LENGTH_SHORT).show();
                return;
            }
            // Clean the input text by replacing any swear words
            userInput = filter.cleanInput(userInput, curseWords);


            // Check if the duration input is empty
            String durationText = durationStr.getText().toString().trim();
            if (!durationText.isEmpty()) {
                try {
                    // If the input is not empty, parse the duration
                    duration = Integer.parseInt(durationText);
                } catch (NumberFormatException e) {
                    // Handle invalid duration input
                    Toast.makeText(this, "Please enter a valid duration.", Toast.LENGTH_SHORT).show();
                    return;
                }
            }

            String expiryDate;

            // Format the expiration date (if duration is null, it means "forever")
            if(duration!=null) {
                expiryDate = formatExpiration(duration, typeDropdown.getSelectedItem().toString());
            }else {
                expiryDate = "";
            }
            // Proceed with the post if input is valid
            // If the user input is empty after cleaning, show a toast message
            if (userInput.isEmpty()) {
                Toast.makeText(this, "Please enter a message before posting.", Toast.LENGTH_SHORT).show();
                return;
            }
            if (userInput.length() >= 225)
            {
                Toast.makeText(this, "Message must be below 225 characters!", Toast.LENGTH_SHORT).show();
                return;
            }
            addNewPost(userInput, expiryDate);
            dialog.dismiss();

            // Update the UI with the new post
            updateUIWithPost(userInput, "Just now", "0 ft", expiryDate, 0, UserId, messageContainer, "0", false);
        });

        cancelButton.setOnClickListener(v -> {
            dialog.dismiss();
        });

        // Show the dialog
        try {
            dialog.show();
        } catch (Exception e) {
            e.printStackTrace();  // Log the exception for debugging
            Toast.makeText(this, "Error showing dialog: " + e.getMessage(), Toast.LENGTH_SHORT).show();
        }
    }





    private void showCommentsDialog(int id) {
        // Create a full-screen dialog
        Dialog dialog = new Dialog(this);
        dialog.requestWindowFeature(Window.FEATURE_NO_TITLE);
        dialog.setContentView(R.layout.dialog_comments);

        // Set the dialog to full-screen
        dialog.getWindow().setLayout(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.MATCH_PARENT);
        dialog.getWindow().setBackgroundDrawable(new ColorDrawable(Color.TRANSPARENT));

        OPContainer = dialog.findViewById((R.id.OPContainer));
        CommentsContainer = dialog.findViewById((R.id.Comments));
        ImageButton cancelButton = dialog.findViewById(R.id.cancelButton);
        EditText inputReply = dialog.findViewById(R.id.CommentText);
        Button replyButton = dialog.findViewById((R.id.PostComment));


        // Create a CurseWordFilter instance with the Activity context
        CurseWordFilter filter = new CurseWordFilter(this);
        List<String> curseWords = filter.loadCurseWords();  // Load curse words from the CSV

        cancelButton.setOnClickListener(v -> {
            dialog.dismiss();
        });

        replyButton.setOnClickListener(v -> {
            String userInput = inputReply.getText().toString().trim();

            // Clean the input text by replacing any swear words
            userInput = filter.cleanInput(userInput, curseWords);
            if (userInput.isEmpty()) {
                Toast.makeText(this, "Please enter a message before posting.", Toast.LENGTH_SHORT).show();
                return;
            }
            if (userInput.length() >= 225)
            {
                Toast.makeText(this, "Message must be below 225 characters!", Toast.LENGTH_SHORT).show();
                return;
            }
            if (!userInput.isEmpty() && userInput.length() <= 225) {
                addNewComment(userInput, id);
                inputReply.setText("");
            }
        });

        // Show the dialog
        try {
            dialog.show();
        } catch (Exception e) {
            e.printStackTrace(); // Log the exception for debugging
            Toast.makeText(this, "Error showing dialog: " + e.getMessage(), Toast.LENGTH_SHORT).show();
        }
    }


    // This adds the post to the API
    private void addNewPost(String text, String expiryDate) {
        // Get the current time in the required format
        String currentTime = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss.SSS'Z'", Locale.getDefault()).format(new Date());

        // Check if expiryDate is null, and if so, omit it from the JSON body
        String jsonBody;
        if (expiryDate == null || expiryDate.equals("")) {
            jsonBody = String.format("{\"text\":\"%s\", \"author\":\"%s\", \"postLatitude\":%f, \"postLongitude\":%f}",
                    text, UserId, lastLatitude, lastLongitude);
        } else {
            jsonBody = String.format("{\"text\":\"%s\", \"author\":\"%s\", \"expiryDate\":\"%s\", \"postLatitude\":%f, \"postLongitude\":%f}",
                    text, UserId, expiryDate, lastLatitude, lastLongitude);
        }

        // Log the JSON body to ensure it's being generated correctly
        Log.d("Post JSON", jsonBody);

        new Thread(() -> {
            try {
                // Construct the URL for the API
                URL url = new URL("https://api.zigzag.madebysaul.com/posts/?latitude=" + lastLatitude + "&longitude=" + lastLongitude);
                HttpURLConnection connection = (HttpURLConnection) url.openConnection();
                connection.setRequestMethod("POST");
                connection.setRequestProperty("Content-Type", "application/json");
                connection.setDoOutput(true);

                // Write the JSON body to the request
                try (OutputStream os = connection.getOutputStream()) {
                    byte[] input = jsonBody.getBytes(StandardCharsets.UTF_8);
                    os.write(input, 0, input.length);
                }

                // Get the response code
                int responseCode = connection.getResponseCode();
                Log.d("Post Response", "Response code: " + responseCode);

                if (responseCode == HttpURLConnection.HTTP_OK) {
                    runOnUiThread(() -> {
                        Toast.makeText(MainActivity.this, "Post created successfully!", Toast.LENGTH_SHORT).show();
                    });
                } else {
                    runOnUiThread(() -> {
                        //Toast.makeText(MainActivity.this, "Failed to create post. Response code: " + responseCode, Toast.LENGTH_SHORT).show();
                    });
                }
            } catch (Exception e) {
                e.printStackTrace();
                runOnUiThread(() -> Toast.makeText(MainActivity.this, "Error posting!", Toast.LENGTH_SHORT).show());
            }
        }).start();

        // Refresh posts
        int distance = getDistanceBasedOnZoom();
        checkAndFetchPosts(lastLatitude, lastLongitude, distance);
    }


    private void addNewComment(String text, int id) {
        String jsonBody = String.format("{\"text\":\"%s\", \"author\":\"%s\"}", text, UserId);


        // This line below shows the posted zig immediately
        //updateUIWithPost(text, "Just now", "0 ft", expiryDate, 0);


        new Thread(() -> {
            try {
                URL url = new URL("https://api.zigzag.madebysaul.com/posts/" + id + "/comments");
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
                    });
                } else {
                    //runOnUiThread(() -> Toast.makeText(MainActivity.this, "Failed to create post", Toast.LENGTH_SHORT).show());
                }
            } catch (Exception e) {
                e.printStackTrace();
                runOnUiThread(() -> Toast.makeText(MainActivity.this, "", Toast.LENGTH_SHORT).show());
            }
        }).start();


        //Refresh posts
        fetchComments(id, lastLatitude, lastLongitude);
    }

    private void updateUIWithPost(String text, String currentTime, String distance, String expiryDate, int id, String authorID, LinearLayout layout, String commentAmount, boolean isComments) {
        // Create a new message group layout
        LinearLayout newMessageGroup = new LinearLayout(this);
        LinearLayout.LayoutParams messageGroupLayoutParams = new LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.MATCH_PARENT,
                LinearLayout.LayoutParams.WRAP_CONTENT);
        messageGroupLayoutParams.setMargins(10, 0, 10, 30);
        newMessageGroup.setLayoutParams(messageGroupLayoutParams);
        newMessageGroup.setBackgroundResource(R.drawable.rounded_posts_shape);

        if(!isComments)
        {
            newMessageGroup.setOnClickListener(v -> {
                Log.d("Comments", "Comments Clicked");
                showCommentsDialog(id);
                fetchComments(id, lastLatitude, lastLongitude);
            });
        }


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
        imageParams.setMargins(275, 0, 0, 0);
        backwardTimeImageView.setLayoutParams(imageParams);


        // Create TextView for the duration
        TextView durationTextView = new TextView(this);
        durationTextView.setTextColor(getResources().getColor(R.color.timeText));
        durationTextView.setText(formatDuration(expiryDate)); // Modify as needed
        durationTextView.setId(View.generateViewId()); // Generate unique ID
        RelativeLayout.LayoutParams durationParams = new RelativeLayout.LayoutParams(
                RelativeLayout.LayoutParams.WRAP_CONTENT,
                RelativeLayout.LayoutParams.WRAP_CONTENT);
        durationParams.addRule(RelativeLayout.CENTER_HORIZONTAL);
        durationParams.addRule(RelativeLayout.RIGHT_OF, backwardTimeImageView.getId());
        durationParams.setMargins(0, 5, 0, 0);
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
        moreButtonParams.addRule(RelativeLayout.ALIGN_PARENT_TOP);
        moreButtonParams.addRule(RelativeLayout.ALIGN_PARENT_END);
        moreButtonParams.setMargins(0, -60, 0, -30);
        moreButton.setLayoutParams(moreButtonParams);


        // Set click listener for the more button
        moreButton.setOnClickListener(v -> {
            PopupMenu postOptions = new PopupMenu(this, v);
            if(my_user_id.equals(authorID))
                postOptions.getMenuInflater().inflate(R.menu.menu_post_options_author, postOptions.getMenu());
            else
                postOptions.getMenuInflater().inflate(R.menu.menu_post_options, postOptions.getMenu());
            postOptions.setOnMenuItemClickListener(new PopupMenu.OnMenuItemClickListener() {
                @Override
                public boolean onMenuItemClick(MenuItem item) {
                    if(my_user_id.equals(authorID)) {
                        if (item.getItemId() == R.id.item_delete) {
                            try {
                                deletePost(id);
                            } catch (IOException e) {
                                Log.e("Delete", "Failed to delete");
                            }
                            return true;
                        }
                        return false;
                    } else {
                        if(item.getItemId() == R.id.item_report){
                            reportPost(text, id, authorID);
                            return true;
                        }
                        return false;
                    }
                }
            });
            postOptions.show();
        });


        // Create TextView for the post content
        TextView postTextView = new TextView(this);
        SpannableString spannableString = new SpannableString(text);
        Pattern pattern = Pattern.compile("#\\w+");
        Matcher matcher = pattern.matcher(text);
        while (matcher.find()) {
            int start = matcher.start();
            int end = matcher.end();
            spannableString.setSpan(new ForegroundColorSpan(Color.parseColor("#23C6F4")), start, end, Spannable.SPAN_EXCLUSIVE_EXCLUSIVE);
            spannableString.setSpan(new UnderlineSpan(), start, end, Spannable.SPAN_EXCLUSIVE_EXCLUSIVE);


            // Create a ClickableSpan for the hashtag
            final String hashtag = text.substring(start, end);
            spannableString.setSpan(new ClickableSpan() {
                @Override
                public void onClick(View widget) {
                    // Replace the headerTextView text with the selected hashtag
                    headerTextView.setText(hashtag);


                    int distance = getDistanceBasedOnZoom();
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
        postParams.setMargins(0, 25, 0, 0);
        postDistanceView.setLayoutParams(distanceParams);


        // Create an imageview for the comment icon
        ImageView commentIcon = new ImageView(this);
        commentIcon.setImageResource(R.drawable.baseline_comment_24);
        commentIcon.setScaleType(ImageView.ScaleType.CENTER_INSIDE);
        commentIcon.setId(View.generateViewId()); // Generate unique ID
        RelativeLayout.LayoutParams imageParamsComment = new RelativeLayout.LayoutParams(
                58, 58);
        imageParamsComment.addRule(RelativeLayout.ALIGN_PARENT_START);
        imageParamsComment.addRule(RelativeLayout.BELOW, postTextView.getId());
        imageParamsComment.addRule(RelativeLayout.ALIGN_PARENT_BOTTOM);
        // every +40 for bottom = 2 line
        imageParamsComment.setMargins(0, 180, 0, 40);
        commentIcon.setLayoutParams(imageParamsComment);

        // Create TextView for the post's comments
        TextView commentTextView = new TextView(this);
        commentTextView.setTextColor(getResources().getColor(R.color.timeText));
        commentTextView.setText(commentAmount);
        commentTextView.setTextSize(14);
        commentTextView.setId(View.generateViewId()); // Generate unique ID
        RelativeLayout.LayoutParams commentTextParams = new RelativeLayout.LayoutParams(
                RelativeLayout.LayoutParams.WRAP_CONTENT,
                RelativeLayout.LayoutParams.WRAP_CONTENT);
        commentTextParams.addRule(RelativeLayout.ALIGN_PARENT_BOTTOM);
        commentTextParams.addRule(RelativeLayout.BELOW, postTextView.getId());
        commentTextParams.addRule(RelativeLayout.ALIGN_RIGHT, commentIcon.getId());
        commentTextParams.setMargins(0, 25, -35, 0);
        commentTextView.setLayoutParams(commentTextParams);

        // If the user is the author, show the profile icon to differentiate posts
        if(Objects.equals(my_user_id, authorID)) {
            ImageView profileIcon = new ImageView(this);
            profileIcon.setColorFilter(Color.parseColor("#23C6F4"));  // Example tint for dark mode (red)

            profileIcon.setImageResource(R.drawable.baseline_person_24);
            profileIcon.setScaleType(ImageView.ScaleType.CENTER_INSIDE);
            profileIcon.setId(View.generateViewId()); // Generate unique ID
            RelativeLayout.LayoutParams imageParamsProfile = new RelativeLayout.LayoutParams(
                    54, 54);
            imageParamsProfile.addRule(RelativeLayout.ABOVE, postTextView.getId());
            imageParamsProfile.addRule(RelativeLayout.LEFT_OF, moreButton.getId());

            profileIcon.setLayoutParams(imageParamsProfile);
            relativeLayout.addView(profileIcon);
        }

        // Add views to the RelativeLayout
        relativeLayout.addView(timeTextView);
        relativeLayout.addView(moreButton);
        relativeLayout.addView(durationTextView);
        relativeLayout.addView(backwardTimeImageView);
        relativeLayout.addView(postTextView);

        relativeLayout.addView(postDistanceView);
        relativeLayout.addView(commentIcon);
        relativeLayout.addView(commentTextView);

        // Measure the height of the postTextView
        postTextView.post(() -> {
            // Get the actual height of the postTextView after rendering
            int postTextHeight = postTextView.getHeight();

            // Adjust the margins dynamically based on the height of the postTextView
            int additionalMargin = (postTextHeight / postTextView.getLineHeight()) * 40;

            // Update comment icon margin dynamically
            RelativeLayout.LayoutParams updatedIconParams = (RelativeLayout.LayoutParams) commentIcon.getLayoutParams();
            updatedIconParams.setMargins(0, 140, 0, additionalMargin); // Add space based on text height
            commentIcon.setLayoutParams(updatedIconParams);

            // Update comment text margin dynamically
            RelativeLayout.LayoutParams updatedTextParams = (RelativeLayout.LayoutParams) commentTextView.getLayoutParams();
            updatedTextParams.setMargins(0, 25, -35, additionalMargin);
            commentTextView.setLayoutParams(updatedTextParams);
        });
        // Add the RelativeLayout to the new message group
        newMessageGroup.addView(relativeLayout);


        // and add the new message group to the container
        layout.addView(newMessageGroup);
    }

    private void reportPost(String text, int postID, String authorID) {
        // Create the JSON body with the snitch (reporting user)
        String jsonBody = String.format(
                "{\"text\":\"%s\", \"author\":\"%s\", \"snitch\":\"%s\"}",
                text, authorID, UserId  // UserId is the ID of the reporting user
        );

        // This line below shows the posted zig immediately
        // updateUIWithPost(text, "Just now", "0 ft", expiryDate, 0);

        new Thread(() -> {
            try {
                // Construct the URL for the report API
                URL url = new URL("https://api.zigzag.madebysaul.com/posts/" + postID + "/reports");
                HttpURLConnection connection = (HttpURLConnection) url.openConnection();
                connection.setRequestMethod("POST");
                connection.setRequestProperty("Content-Type", "application/json");
                connection.setDoOutput(true);

                // Write the JSON body to the request output stream
                try (OutputStream os = connection.getOutputStream()) {
                    byte[] input = jsonBody.getBytes(StandardCharsets.UTF_8);
                    os.write(input, 0, input.length);
                }

                // Get the response code
                int responseCode = connection.getResponseCode();
                if (responseCode == HttpURLConnection.HTTP_OK) {
                    // Report successful, show a success toast
                    runOnUiThread(() -> {
                        Toast.makeText(MainActivity.this, "Post Reported! We will investigate this at our earliest convenience.", Toast.LENGTH_SHORT).show();
                    });
                } else {
                    // Report failed, show a failure toast
                    runOnUiThread(() -> {
                        Toast.makeText(MainActivity.this, "Post Reported! We will investigate this at our earliest convenience.", Toast.LENGTH_SHORT).show();
                    });
                }
            } catch (Exception e) {
                e.printStackTrace();
                runOnUiThread(() -> {
                    Toast.makeText(MainActivity.this, "Report Failed!!", Toast.LENGTH_SHORT).show();
                });
            }
        }).start();
    }


    private void updateUIWithComments(String text, String currentTime, String authorID){
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

        // Create TextView for the post content
        TextView postTextView = new TextView(this);
        SpannableString spannableString = new SpannableString(text);
        postTextView.setText(spannableString);
        postTextView.setTextSize(20);
        postTextView.setMovementMethod(LinkMovementMethod.getInstance()); // Enable links
        RelativeLayout.LayoutParams postParams = new RelativeLayout.LayoutParams(
                RelativeLayout.LayoutParams.WRAP_CONTENT,
                RelativeLayout.LayoutParams.WRAP_CONTENT);
        postParams.setMargins(0, 40, 33, 0);
        postTextView.setLayoutParams(postParams);


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

        relativeLayout.addView(postTextView);
        relativeLayout.addView(timeTextView);
        if(Objects.equals(my_user_id, authorID)) {
            ImageView profileIcon = new ImageView(this);

            profileIcon.setImageResource(R.drawable.baseline_person_24);
            profileIcon.setScaleType(ImageView.ScaleType.CENTER_INSIDE);
            profileIcon.setId(View.generateViewId()); // Generate unique ID
            RelativeLayout.LayoutParams imageParamsProfile = new RelativeLayout.LayoutParams(
                    44, 44);
            imageParamsProfile.addRule(RelativeLayout.ABOVE, postTextView.getId());
            imageParamsProfile.addRule(RelativeLayout.RIGHT_OF, timeTextView.getId());
            profileIcon.setLayoutParams(imageParamsProfile);
            //imageParamsProfile.setMargins(0, 0, 0, 0);
            relativeLayout.addView(profileIcon);
        }
        // Add the RelativeLayout to the new message group
        newMessageGroup.addView(relativeLayout);


        // and add the new message group to the container
        CommentsContainer.addView(newMessageGroup);
    }


    // Fetches posts based on distance
    private void fetchPosts(double latitude, double longitude, int distance) {
        String url = BASE_URL + "latitude=" + latitude + "&longitude=" + longitude + "&distance=" + distance + orderBy;
        Log.d("FetchPosts", "Request URL: " + url + " latitude: " + latitude + " longitude: " + longitude + " distance: " + distance);


        new Thread(() -> {
            OkHttpClient client = new OkHttpClient();
            Request request = new Request.Builder()
                    .url(url)
                    .build();


            try (Response response = client.newCall(request).execute()) {
                if (response.isSuccessful() && response.body() != null) {
                    String jsonResponse = response.body().string();
                    handlePostsResponse(jsonResponse, messageContainer, false);
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

    private void fetchComments(int postId, double latitude, double longitude){

        String opUrl = "https://api.zigzag.madebysaul.com/posts/" + postId + "?" + "latitude=" + latitude + "&longitude=" + longitude;
        String url = "https://api.zigzag.madebysaul.com/posts/" + postId + "/comments";
        Log.d("FetchComments", "Request opURL: " + opUrl);
        Log.d("FetchComments", "Request URL: " + url);
        OPContainer.removeAllViews();
        CommentsContainer.removeAllViews();

        //Fetch original post
        new Thread(() -> {
            OkHttpClient client = new OkHttpClient();
            Request request = new Request.Builder()
                    .url(opUrl)
                    .build();


            try (Response response = client.newCall(request).execute()) {
                if (response.isSuccessful() && response.body() != null) {
                    String jsonResponse = "[" + response.body().string() + "]";
                    handlePostsResponse(jsonResponse, OPContainer, true);
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

        //Fetch comments
        new Thread(() -> {
            OkHttpClient client = new OkHttpClient();
            Request request = new Request.Builder()
                    .url(url)
                    .build();


            try (Response response = client.newCall(request).execute()) {
                if (response.isSuccessful() && response.body() != null) {
                    String jsonResponse = response.body().string();
                    handleCommentsResponse(jsonResponse);
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


    private void handlePostsResponse(String jsonResponse, LinearLayout layout, boolean isCommentss) {
        runOnUiThread(() -> {
            try {
                Log.d("handlePostsResponse", "Response received");
                JSONArray postsArray = new JSONArray(jsonResponse);
                Log.d("This is the", jsonResponse);
                layout.removeAllViews(); // Clear previous posts


                for (int i = 0; i < postsArray.length(); i++) {
                    JSONObject post = postsArray.getJSONObject(i);


                    int id = post.getInt("id");
                    String authorID = post.getString("authorId");
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


                    String expiryDate = post.getString("expiryDate");
                    String commentCount = post.getString("commentCount");

                    // Update the UI with the post, formatted time, and distance
                    updateUIWithPost(text, formattedTime, distanceString, expiryDate, id, authorID, layout, commentCount, isCommentss);
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

    private void handleCommentsResponse(String jsonResponse){
        runOnUiThread(() -> {
            try {
                Log.d("handleCommentsResponse", "Comments received");
                JSONArray postsArray = new JSONArray(jsonResponse);
                Log.d("This is the", jsonResponse);

                for (int i = 0; i < postsArray.length(); i++) {
                    JSONObject post = postsArray.getJSONObject(i);

                    String text = post.getString("text"); // Get the post text
                    String createdAt = post.getString("createdAt"); // Get the createdAt time

                    String formattedTime = formatTime(createdAt);

                    String authorId = post.getString("authorId");

                    // Update the UI with the post, formatted time, and distance
                    updateUIWithComments(text, formattedTime, authorId);
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

    private void deletePost(int id) throws IOException{


        Thread thread = new Thread(new Runnable() {


            @Override
            public void run() {
                try {


                    //Delete the specified post
                    URL url = new URL("https://api.zigzag.madebysaul.com/posts/" + id);
                    HttpURLConnection connection = (HttpURLConnection) url.openConnection();
                    connection.setRequestMethod("DELETE");


                    //Check response from api
                    int responseCode = connection.getResponseCode();
                    if (responseCode == HttpURLConnection.HTTP_NO_CONTENT) {
                        Log.d("Delete", "Ladies and Gentlemen, we got 'em");
                    } else if (responseCode == HttpURLConnection.HTTP_NOT_FOUND) {
                        Log.e("Delete", "Where is it?");
                    } else {
                        Log.e("Delete", "I don't even know, man");
                    }
                    connection.disconnect();
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
        });
        thread.start();


        //Refresh the posts
        int distance = getDistanceBasedOnZoom();
        checkAndFetchPosts(lastLatitude, lastLongitude, distance);
    }


    private String formatDistance(double meters) {
        // Convert meters to miles
        double miles = meters * 0.000621371;


        // Log the distance in meters and calculated miles
        Log.d("DistanceLogger", "Distance in meters: " + meters + ", calculated miles: " + miles);


        if (miles < 0.1 ) {
            double feet = meters * 3.28084; // Convert meters to feet
            if(feet < 250)
            {
                return String.format("<250 ft", feet);
            }
            else {
                Log.d("DistanceLogger", "Distance in feet: " + feet); // Log the feet distance
                return String.format("%.0f ft", feet);
            }
        } else {
            return String.format("%.2f mi", miles);
        }
    }




    // This helps format the time that is received from the Backend API to show simple results
    private String formatTime(String createdAt) {
        try {
            SimpleDateFormat inputFormat = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss.SSS'Z'", Locale.getDefault());
            inputFormat.setTimeZone(TimeZone.getTimeZone("UTC"));
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
                return diffInMinutes + " min ago";
            } else if (diffInHours < 24) {
                return diffInHours + " hour" + (diffInHours > 1 ? "s" : "") + " ago";
            } else {
                return diffInDays + " day" + (diffInDays > 1 ? "s" : "") + " ago";
            }
        } catch (Exception e) {
            Log.e("MainActivity", "Date parsing error: ", e);
            return "";
        }
    }


    private String formatExpiration(Integer duration, String type){
        if (duration == null) {
            // If duration is null, return "Forever" or some default message.
            return null;
        }
        if(type.equals("days"))
            duration *= (24 * 60);
        else if(type.equals("hours"))
            duration *= 60;
        else
            ;


        try {
            Date currentDate = new Date();
            Calendar cal = Calendar.getInstance();
            cal.setTimeZone(TimeZone.getTimeZone("UTC"));
            cal.setTime(currentDate);


            int year = cal.get(Calendar.YEAR);
            int month = cal.get(Calendar.MONTH) + 1;
            int day = cal.get(Calendar.DATE);
            int hour = cal.get(Calendar.HOUR_OF_DAY);
            int minute = cal.get(Calendar.MINUTE);
            int monthLength = monthLength(month, year);


            minute = minute + duration;


            Log.d("Time", minute + "minutes");
            //Cascade the overflow
            while(minute >= 60){
                hour++;
                minute -= 60;
            }
            Log.d("Time", hour + "hours");


            while(hour >= 24){
                day++;
                hour -= 24;
            }
            Log.d("Time", day + "days");


            while (day > monthLength) {
                month++;
                day -= monthLength;
                monthLength = monthLength(month, year);
            }
            Log.d("Time", month + "months");


            if (month > 12) {
                year++;
                month -= 12;
            }
            Log.d("Time", year + "years");


            String monthStr = month + "";
            String dateStr = day + "";
            String hourStr = hour + "";
            String minuteStr = minute + "";


            //Add 0s to one digit numbers
            if((month + "").length() == 1)
                monthStr = "0" + month;
            if((day + "").length() == 1)
                dateStr = "0" + day;
            if((hour + "").length() == 1)
                hourStr = "0" + hour;
            if((minute + "").length() == 1)
                minuteStr = "0" + minute;


            return year + "-" + monthStr + "-" + dateStr + "T" + hourStr + ":" + minuteStr + ":00.000Z";
        } catch (Exception e) {
            Log.e("MainActivity", "Error");
            return null;
        }
    }


    private String formatDuration(String expiryDate){
        if (expiryDate.equals("")) {
            return "Forever";
        }
        try {
            SimpleDateFormat inputFormat = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss.SSS'Z'", Locale.getDefault());
            inputFormat.setTimeZone(TimeZone.getTimeZone("UTC"));
            Date date = inputFormat.parse(expiryDate);


            // Calculate the time difference
            long diffInMillis = date.getTime() - new Date().getTime();
            long diffInMinutes = diffInMillis / (1000 * 60);
            long diffInHours = diffInMinutes / 60;
            long diffInDays = diffInHours / 24;


            // Generate the appropriate time left string
            if (diffInMinutes < 1) {
                return "About to go";
            } else if (diffInMinutes < 60) {
                return diffInMinutes + " min left";
            } else if (diffInHours < 24) {
                return diffInHours + " hour" + (diffInHours > 1 ? "s" : "") + " left";
            } else {
                return diffInDays + " day" + (diffInDays > 1 ? "s" : "") + " left";
            }
        } catch (Exception e) {
            Log.e("MainActivity", "Date parsing error: ", e);
            return "Forever";
        }
    }


    private int monthLength(int month, int year) {


        int febLength;
        if(year % 4 == 0)
            febLength = 29;
        else
            febLength = 28;

        int[] monthLengths = new int[]{31, febLength, 31, 30, 31, 30, 31, 31, 30, 31, 30, 31};

        return monthLengths[month-1];
    }

    private void showProfileDialog() {
        // Create a full-screen dialog
        Dialog dialog = new Dialog(this);
        dialog.requestWindowFeature(Window.FEATURE_NO_TITLE);
        dialog.setContentView(R.layout.dialog_show_profile);


        // Set the dialog to full-screen
        dialog.getWindow().setLayout(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.MATCH_PARENT);
        dialog.getWindow().setBackgroundDrawable(new ColorDrawable(Color.TRANSPARENT));


        // Initialize views
        TextView emailTextView = dialog.findViewById(R.id.emailTextView);
        TextView phoneTextView = dialog.findViewById(R.id.phoneTextView);
        LinearLayout signOut = dialog.findViewById(R.id.SignOutLayout);
        LinearLayout deleteAccountLayout = dialog.findViewById(R.id.DeleteAccountLayout);
        ImageButton closeButton = dialog.findViewById(R.id.closeButton);


        // Load email and phone number from SharedPreferences
        loadInputValues(emailTextView, phoneTextView);


        // Set button listener to close the dialog
        closeButton.setOnClickListener(v -> dialog.dismiss());

        if(Objects.equals(UserId, "TesterAccount"))
        {
            String currentUser = "TesterAccount";
        }
        else {
            FirebaseUser currentUser = FirebaseAuth.getInstance().getCurrentUser();
            if (currentUser == null) {
                Toast.makeText(this, "User not authenticated", Toast.LENGTH_SHORT).show();
                return;
            }


            // Handle click for deleting account
            deleteAccountLayout.setOnClickListener(v -> {
                // Show confirmation dialog for account deletion
                showDeleteConfirmationDialog(currentUser);
            });

        }
        // Sign out functionality
        signOut.setOnClickListener(v -> {
            FirebaseAuth.getInstance().signOut();
            SharedPreferences preferences = getSharedPreferences(PREFS_NAME, MODE_PRIVATE);
            SharedPreferences.Editor editor = preferences.edit();
            editor.clear();
            editor.apply();
            startActivity(new Intent(this, ProfileLogin.class));
            finish();
        });


        // Show the dialog
        try {
            dialog.show();
        } catch (Exception e) {
            e.printStackTrace();
            Toast.makeText(this, "Error showing dialog: " + e.getMessage(), Toast.LENGTH_SHORT).show();
        }
    }



    private void showDeleteConfirmationDialog(FirebaseUser currentUser) {
        new AlertDialog.Builder(this)
                .setTitle("Delete Account")
                .setMessage("Are you sure you want to delete your account?")
                .setPositiveButton("Yes", (dialogInterface, i) -> {
                    // Proceed with account deletion
                    deleteAccount(currentUser);
                })
                .setNegativeButton("No", null)
                .show();
    }


    private void deleteAccount(FirebaseUser currentUser) {
        // Re-authenticate the user before deleting the account
        reAuthenticateUser(currentUser);
    }


    private void reAuthenticateUser(FirebaseUser currentUser) {
        // Get email from the current user
        String email = currentUser.getEmail();


        // Retrieve the password from SharedPreferences (replace "user_prefs" with your SharedPreferences file name)
        SharedPreferences sharedPreferences = getSharedPreferences("ProfileCreationPrefs", MODE_PRIVATE);
        String storedPassword = sharedPreferences.getString("password", null);


        // Check if email and password exist
        if (email != null && storedPassword != null && !storedPassword.isEmpty()) {
            // Create the AuthCredential using the email and password
            AuthCredential credential = EmailAuthProvider.getCredential(email, storedPassword);


            // Re-authenticate the user with the email and password
            currentUser.reauthenticate(credential).addOnCompleteListener(reAuthTask -> {
                if (reAuthTask.isSuccessful()) {
                    // Now that the user is re-authenticated, proceed to delete the account
                    deleteUserFromAuth(currentUser);
                } else {
                    // If re-authentication fails, notify the user
                    Toast.makeText(this, "Re-authentication failed. Please log in again.", Toast.LENGTH_SHORT).show();
                }
            });
        } else {
            // If no email or password is found, prompt the user to log in again
            Toast.makeText(this, "Error: Missing credentials. Please log in again.", Toast.LENGTH_SHORT).show();
        }
    }




    private void deleteUserFromAuth(FirebaseUser currentUser) {
        //clear all saved info about user
        SharedPreferences sharedPreferences = getSharedPreferences("user_prefs", MODE_PRIVATE);
        SharedPreferences.Editor editor = sharedPreferences.edit();
        editor.clear();  // Clear all preferences
        editor.apply();  // Apply changes to SharedPreferences


        SharedPreferences sharedPreferences2 = getSharedPreferences("ProfileCreationPrefs", MODE_PRIVATE);
        SharedPreferences.Editor editor2 = sharedPreferences2.edit();
        editor2.clear();  // Clear all preferences
        editor2.apply();  // Apply changes to SharedPreferences
        // Step 3: Delete the Firebase Authentication user (email, password, phone number, etc.)
        currentUser.delete().addOnCompleteListener(deleteTask -> {
            if (deleteTask.isSuccessful()) {
                // If the deletion is successful, show a success message
                Toast.makeText(this, "Account deleted successfully", Toast.LENGTH_SHORT).show();


                // Sign the user out after successful deletion
                FirebaseAuth.getInstance().signOut();


                // Navigate to the login screen or exit the app
                startActivity(new Intent(this, ProfileLogin.class));
                finish();
            } else {
                // If deleting the Firebase Authentication user fails
                Toast.makeText(this, "Failed to delete account from Authentication. Please try again later.", Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void loadInputValues(TextView emailTextView, TextView phoneTextView) {
        // First, check ProfileCreationPrefs for the email and phone number
        SharedPreferences prefs = getSharedPreferences("ProfileCreationPrefs", MODE_PRIVATE);
        String email = prefs.getString(KEY_EMAIL, null);
        String phoneNumber = prefs.getString(KEY_PHONE_NUMBER, null);

        // If email is not found, check MyPrefs for key_email
        if (email == null || email.equals("Email not available")) {
            SharedPreferences myPrefs = getSharedPreferences("MyPrefs", MODE_PRIVATE);
            email = myPrefs.getString("key_email", null);  // Check MyPrefs for key_email
        }

        // If phone number is not found, check ProfileCreationPrefs again
        if (phoneNumber == null || phoneNumber.equals("Phone number not available")) {
            SharedPreferences prefs2 = getSharedPreferences(PREFS_NAME, MODE_PRIVATE);
            phoneNumber = prefs2.getString(KEY_PHONE_NUMBER, "Phone number not available");
        }

        // Set the email value in the TextView
        if (email != null && !email.equals("Email not available")) {
            emailTextView.setText("Email:       " + email);
        } else {
            emailTextView.setText("Email not available");
        }

        // Set the phone number value in the TextView
        if (phoneNumber != null && !phoneNumber.equals("Phone number not available")) {
            phoneTextView.setText("Phone Number:       " + phoneNumber);
        } else {
            phoneTextView.setText("Phone number not available");
        }
    }





    private static final String PREFS_NAME = "SortPrefs";  // SharedPreferences name
    private static final String KEY_LAST_SORTED = "last_sorted";  // Key to store the last selected option

    // Method to show the sorting dialog
    private void showSortingDialog() {
        // Define the options and their associated icons (drawables)
        final String[] options = {"Recent", "Close", "Hot"};
        final int[] icons = {
                R.drawable.baseline_calendar_month_24,  // Icon for "Recent"
                R.drawable.baseline_public_24,          // Icon for "Close"
                R.drawable.baseline_local_fire_department_24 // Icon for "Hot"
        };

        // Get the last sorted option from SharedPreferences, default to "Recent"
        SharedPreferences prefs = getSharedPreferences(PREFS_NAME, MODE_PRIVATE);
        String lastSorted = prefs.getString(KEY_LAST_SORTED, "Recent");

        // Find the index of the last selected option
        int selectedOptionIndex = getOptionIndex(lastSorted, options);

        // Create a custom adapter for the dialog, passing the context to the adapter
        CustomAdapter adapter = new CustomAdapter(this, options, icons, selectedOptionIndex); // Pass the context here

        // Create an AlertDialog.Builder
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setTitle("Sort Posts") // Title of the dialog
                .setSingleChoiceItems(adapter, selectedOptionIndex, (dialog, which) -> {
                    // Handle the selection
                    switch (which) {
                        case 0:
                            showRecent();
                            break;
                        case 1:
                            showClose();
                            break;
                        case 2:
                            showHot();
                            break;
                        default:
                            break;
                    }

                    // Save the selected option in SharedPreferences
                    SharedPreferences.Editor editor = prefs.edit();
                    editor.putString(KEY_LAST_SORTED, options[which]);
                    editor.apply();

                    // Update the selected index and notify the adapter to refresh the list
                    adapter.setSelectedIndex(which);
                    adapter.notifyDataSetChanged();

                    // Dismiss the dialog after selection
                    dialog.dismiss();
                })
                .setCancelable(true) // Allows the dialog to be canceled by clicking outside
                .create() // Create the dialog
                .show(); // Show the dialog
    }

    // Custom Adapter
    private class CustomAdapter extends BaseAdapter {
        private final Context context;
        private final String[] options;
        private final int[] icons;
        private int selectedIndex;

        // Constructor for the custom adapter
        public CustomAdapter(Context context, String[] options, int[] icons, int selectedIndex) {
            this.context = context;  // Save the context
            this.options = options;
            this.icons = icons;
            this.selectedIndex = selectedIndex;
        }

        @Override
        public int getCount() {
            return options.length;
        }

        @Override
        public Object getItem(int position) {
            return options[position];
        }

        @Override
        public long getItemId(int position) {
            return position;
        }

        @Override
        public View getView(int position, View convertView, ViewGroup parent) {
            // If convertView is null, create a new layout
            if (convertView == null) {
                convertView = new LinearLayout(context);  // Use the context passed in constructor
                LinearLayout.LayoutParams layoutParams = new LinearLayout.LayoutParams(
                        LinearLayout.LayoutParams.MATCH_PARENT, LinearLayout.LayoutParams.WRAP_CONTENT);
                convertView.setLayoutParams(layoutParams);
                ((LinearLayout) convertView).setOrientation(LinearLayout.HORIZONTAL);

                // Set padding to create space between items
                convertView.setPadding(16, 16, 16, 16);

                // TextView for option text
                TextView optionText = new TextView(context);  // Use the context here too
                optionText.setTextSize(18);  // Increase text size
                optionText.setLayoutParams(new LinearLayout.LayoutParams(0, LinearLayout.LayoutParams.WRAP_CONTENT, 1f)); // Text takes the available space

                // ImageView for the icon on the right
                ImageView iconImage = new ImageView(context);  // Use the context here too
                LinearLayout.LayoutParams iconParams = new LinearLayout.LayoutParams(80, 80);  // Increase icon size
                iconParams.setMargins(16, 0, 0, 0); // margin between text and icon
                iconImage.setLayoutParams(iconParams);

                // Add both views (TextView and ImageView) to the LinearLayout
                ((LinearLayout) convertView).addView(optionText);
                ((LinearLayout) convertView).addView(iconImage);
            }

            // Get the TextView and ImageView from the converted view
            LinearLayout linearLayout = (LinearLayout) convertView;
            TextView optionText = (TextView) linearLayout.getChildAt(0);
            ImageView iconImage = (ImageView) linearLayout.getChildAt(1);

            // Set text and image
            optionText.setText(options[position]);
            iconImage.setImageResource(icons[position]);

            // Highlight the selected item by changing the background
            if (position == selectedIndex) {
                convertView.setBackgroundColor(context.getResources().getColor(android.R.color.darker_gray)); // Selected item background
            } else {
                convertView.setBackgroundColor(context.getResources().getColor(android.R.color.transparent)); // Reset background for non-selected items
            }

            return convertView;
        }

        // Method to set the selected index
        public void setSelectedIndex(int index) {
            this.selectedIndex = index;
        }
    }




    // Helper method to get the index of the selected option
    private int getOptionIndex(String lastSorted, String[] options) {
        for (int i = 0; i < options.length; i++) {
            if (options[i].equals(lastSorted)) {
                return i;  // Return the index of the selected option
            }
        }
        return 0;  // Default to "Recent" if no match or there is an error
    }

    // Method for "Show Recent"
    private void showRecent() {
        int distance = getDistanceBasedOnZoom();
        orderBy = "&orderBy=";

        // Set the "Recent" icon
        sortingButton.setImageResource(R.drawable.baseline_calendar_month_24);

        // Set the color filter based on the mode (dark or light)
        if (isInDarkMode()) {
            sortingButton.setColorFilter(Color.parseColor("#1EB4DF"));
            //sortingButton.setColorFilter(Color.parseColor("#B3B3B3"));  // Example tint for dark mode (red)
        } else {
            sortingButton.setColorFilter(Color.parseColor("#1EB4DF"));  // Default gray tint for light mode
        }

        checkAndFetchPosts(lastLatitude, lastLongitude, distance);
    }

    // Method for "Show Close"
    private void showClose() {
        int distance = getDistanceBasedOnZoom();
        orderBy = "&orderBy=CLOSEST";
        // Set the "Close" icon
        sortingButton.setImageResource(R.drawable.baseline_public_24);
        // Set the color filter based on the mode (dark or light)
        if (isInDarkMode()) {
            sortingButton.setColorFilter(Color.parseColor("#1EB4DF"));
            //sortingButton.setColorFilter(Color.parseColor("#B3B3B3"));  // Example tint for dark mode (red)
        } else {
            sortingButton.setColorFilter(Color.parseColor("#1EB4DF"));  // Default gray tint for light mode
        }

        checkAndFetchPosts(lastLatitude, lastLongitude, distance);
    }

    // Method for "Show Hot"
    private void showHot() {
        int distance = getDistanceBasedOnZoom();
        orderBy = "&orderBy=HOT";
        // Set the "Hot" icon
        sortingButton.setImageResource(R.drawable.baseline_local_fire_department_24);
        // Set the color filter based on the mode (dark or light)
        if (isInDarkMode()) {
            sortingButton.setColorFilter(Color.parseColor("#1EB4DF"));
            //sortingButton.setColorFilter(Color.parseColor("#B3B3B3"));  // Example tint for dark mode (red)
        } else {
            sortingButton.setColorFilter(Color.parseColor("#1EB4DF"));  // Default gray tint for light mode
        }

        checkAndFetchPosts(lastLatitude, lastLongitude, distance);
    }


    // Helper method to calculate distance based on zoom level
    private int getDistanceBasedOnZoom() {
        int distance = 5000;  // Default distance

        if (lastZoomLevel == 15) {
            distance = 800;
        } else if (lastZoomLevel == 11) {
            distance = 40000;
        } else if (lastZoomLevel == 7) {
            distance = 800000;
        }

        return distance;
    }


    private void scheduleDailyNotification() {
        // Get the AlarmManager system service
        AlarmManager alarmManager = (AlarmManager) getSystemService(Context.ALARM_SERVICE);

        // Set up the time for 12 PM
        Calendar calendar = Calendar.getInstance();
        calendar.set(Calendar.HOUR_OF_DAY, 12); // Set the hour to 12 PM
        calendar.set(Calendar.MINUTE, 16);
        calendar.set(Calendar.SECOND, 0);
        calendar.set(Calendar.MILLISECOND, 0);

        // If the time has already passed for today, set it for tomorrow
        if (calendar.getTimeInMillis() <= System.currentTimeMillis()) {
            calendar.add(Calendar.DAY_OF_YEAR, 1);  // Move to the next day if 12 PM has passed
        }

        // Create an intent to trigger the BroadcastReceiver
        Intent intent = new Intent(this, DailyNotificationReceiver.class);
        PendingIntent pendingIntent = PendingIntent.getBroadcast(this, 0, intent, PendingIntent.FLAG_IMMUTABLE);

        // Set the alarm to trigger daily at 12 PM
        if (alarmManager != null) {
            alarmManager.setRepeating(
                    AlarmManager.RTC_WAKEUP,  // Wake up the device if it's asleep
                    calendar.getTimeInMillis(), // Start time (12 PM today or tomorrow)
                    AlarmManager.INTERVAL_DAY, // Repeat every 24 hours
                    pendingIntent // PendingIntent to trigger the receiver
            );
        }
    }
    private boolean isInDarkMode() {
        int nightModeFlags = getResources().getConfiguration().uiMode & Configuration.UI_MODE_NIGHT_MASK;
        return nightModeFlags == Configuration.UI_MODE_NIGHT_YES;
    }
}
