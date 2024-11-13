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
    static final String PREFS_NAME = "MyPrefs";
    private static final String KEY_FIRST_LAUNCH = "first_launch";
    static final String KEY_EMAIL = "key_email"; // Add key for email
    static final String KEY_PASSWORD = "key_password"; // Add key for password

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.login_profile);

        // Initialize Firebase Auth
        mAuth = FirebaseAuth.getInstance();

        // Check if this is the first launch after install
        SharedPreferences prefs = getSharedPreferences(PREFS_NAME, MODE_PRIVATE);
        boolean isFirstLaunch = prefs.getBoolean(KEY_FIRST_LAUNCH, true);

        if (isFirstLaunch) {
            mAuth.signOut();
            prefs.edit().putBoolean(KEY_FIRST_LAUNCH, false).apply();
        }

        // Check if user is already logged in
        FirebaseUser currentUser = mAuth.getCurrentUser();
        if (currentUser != null) {
            switchToMainActivity(currentUser.getUid());
            return;
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
        intent.putExtra("EMAIL", email);
        intent.putExtra("PASSWORD", password);
        startActivity(intent);
        finish();
    }

    private void performLogin(String email, String password) {
        if (!isValidEmail(email)) {
            Toast.makeText(this, "Please enter a valid email address.", Toast.LENGTH_SHORT).show();
            return;
        }

        mAuth.signInWithEmailAndPassword(email, password)
                .addOnCompleteListener(task -> {
                    if (task.isSuccessful()) {
                        FirebaseUser user = mAuth.getCurrentUser();
                        Log.d("ProfileLogin", "signInWithEmail:success");

                        // Save email and password to SharedPreferences
                        saveInputValues(email, password);

                        switchToMainActivity(user.getUid());
                    } else {
                        Log.w("ProfileLogin", "signInWithEmail:failure", task.getException());
                        Toast.makeText(ProfileLogin.this, "Incorrect Username or Password: " + task.getException().getMessage(), Toast.LENGTH_SHORT).show();
                    }
                });
    }

    private void saveInputValues(String email, String password) {
        SharedPreferences prefs = getSharedPreferences(PREFS_NAME, MODE_PRIVATE);
        SharedPreferences.Editor editor = prefs.edit();
        editor.putString(KEY_EMAIL, email);
        editor.putString(KEY_PASSWORD, password);
        editor.apply();

        Log.d("PrefsDebug", "Saved Email: " + email);
        Log.d("PrefsDebug", "Saved Password: " + password); // Be cautious with logging passwords
    }

    private void togglePasswordVisibility() {
        if (passwordEditText.getTransformationMethod() != null) {
            passwordEditText.setTransformationMethod(null);
            showPasswordButton.setText("Hide");
        } else {
            passwordEditText.setTransformationMethod(new android.text.method.PasswordTransformationMethod());
            showPasswordButton.setText("Show");
        }
        passwordEditText.setSelection(passwordEditText.getText().length());
    }

    private void switchToMainActivity(String userId) {
        Intent intent = new Intent(ProfileLogin.this, MainActivity.class);
        intent.putExtra("USER_ID", userId);
        startActivity(intent);
        finish();
    }

    private boolean isValidEmail(String email) {
        String emailPattern = "[a-zA-Z0-9._%+-]+@[a-zA-Z0-9.-]+\\.[a-zA-Z]{2,}";
        return email.matches(emailPattern);
    }
}
