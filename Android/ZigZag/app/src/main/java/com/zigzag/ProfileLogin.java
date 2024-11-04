package com.zigzag;

import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.util.Log;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;


public class ProfileLogin extends AppCompatActivity {
    private EditText emailEditText;
    private EditText passwordEditText;
    private Button loginButton;
    private Button signupButton;
    private Button showPasswordButton;

    private FirebaseAuth mAuth;
    private static final String PREFS_NAME = "MyPrefs";
    private static final String KEY_FIRST_LAUNCH = "first_launch";
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.login_profile); // Ensure this layout includes your login UI

        // Initialize Firebase Auth
        mAuth = FirebaseAuth.getInstance();

        // Check if this is the first launch after install
        SharedPreferences prefs = getSharedPreferences(PREFS_NAME, MODE_PRIVATE);
        boolean isFirstLaunch = prefs.getBoolean(KEY_FIRST_LAUNCH, true);

        if (isFirstLaunch) {
            // If this is the first launch, log out any current user
            mAuth.signOut();
            // Update the preference to false
            prefs.edit().putBoolean(KEY_FIRST_LAUNCH, false).apply();
        }


        // Check if user is already logged in
        FirebaseUser currentUser = mAuth.getCurrentUser();
        if (currentUser != null) {
            // User is signed in, navigate to MainActivity
            switchToMainActivity(currentUser.getUid());
            return; // Exit the onCreate method early
        }

        emailEditText = findViewById(R.id.email);
        passwordEditText = findViewById(R.id.password);
        loginButton = findViewById(R.id.loginButton);
        showPasswordButton = findViewById(R.id.showPasswordButton);
        signupButton = findViewById(R.id.signupButton);

        loginButton.setOnClickListener(v -> {
            String email = emailEditText.getText().toString().trim();
            String password = passwordEditText.getText().toString().trim();
            performLogin(email, password);
        });

        showPasswordButton.setOnClickListener(v -> togglePasswordVisibility());

        signupButton.setOnClickListener(v -> switchToCreateProfile());
    }

    private void switchToCreateProfile() {
        String email = emailEditText.getText().toString().trim();
        String password = passwordEditText.getText().toString().trim();

        Intent intent = new Intent(ProfileLogin.this, ProfileCreation.class);
        intent.putExtra("EMAIL", email); // Pass email to ProfileCreation
        intent.putExtra("PASSWORD", password); // Pass password to ProfileCreation
        startActivity(intent);
        finish();
    }

    // Method to log in a user with email and password
    private void performLogin(String email, String password) {
        if (!isValidEmail(email)) {
            Toast.makeText(this, "Please enter a valid email address.", Toast.LENGTH_SHORT).show();
            return; // Exit if the email is not valid
        }

        mAuth.signInWithEmailAndPassword(email, password)
                .addOnCompleteListener(task -> {
                    if (task.isSuccessful()) {
                        // Sign in success, get the current user
                        FirebaseUser user = mAuth.getCurrentUser();
                        Log.d("ProfileLogin", "signInWithEmail:success");
                        switchToMainActivity(user.getUid());
                    } else {
                        // If sign in fails, log the error
                        Log.w("ProfileLogin", "signInWithEmail:failure", task.getException());
                        Toast.makeText(ProfileLogin.this, "Incorrect Username or Password: " + task.getException().getMessage(), Toast.LENGTH_SHORT).show();
                    }
                });
    }

    private void togglePasswordVisibility() {
        // Toggle the password visibility
        if (passwordEditText.getTransformationMethod() != null) {
            // If the password is currently hidden, show it
            passwordEditText.setTransformationMethod(null);
            showPasswordButton.setText("Hide");
        } else {
            // If the password is currently visible, hide it
            passwordEditText.setTransformationMethod(new android.text.method.PasswordTransformationMethod());
            showPasswordButton.setText("Show");
        }
        // Move the cursor to the end of the text
        passwordEditText.setSelection(passwordEditText.getText().length());
    }

    private void switchToMainActivity(String userId) {
        Intent intent = new Intent(ProfileLogin.this, MainActivity.class);
        intent.putExtra("USER_ID", userId);
        startActivity(intent);
        finish();
    }

    // Method to validate email format
    private boolean isValidEmail(String email) {
        String emailPattern = "[a-zA-Z0-9._%+-]+@[a-zA-Z0-9.-]+\\.[a-zA-Z]{2,}";
        return email.matches(emailPattern);
    }
}
