package com.zigzag;

import android.Manifest;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.os.Build;
import android.os.Bundle;
import android.text.InputFilter;
import android.text.InputType;
import android.view.View;
import android.view.inputmethod.EditorInfo;
import android.view.inputmethod.InputMethodManager;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.FirebaseException;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.auth.PhoneAuthCredential;
import com.google.firebase.auth.PhoneAuthOptions;
import com.google.firebase.auth.PhoneAuthProvider;

import java.util.concurrent.TimeUnit;

public class ProfileCreation extends AppCompatActivity {
    private EditText userNameText;
    private EditText phoneNumberText;
    private TextView incorrectCodeText;
    private TextView formatText;
    private EditText codeInput;
    private TextView codeInputLabel;
    private Button createProfileButton;
    private FirebaseAuth mAuth;
    private String verificationId;
    private static final int REQUEST_CODE_POST_NOTIFICATIONS = 100;
    private static final int REQUEST_CODE_LOCATION = 101;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.create_profile);

        mAuth = FirebaseAuth.getInstance();

        // Check if the user is already signed in
        FirebaseUser currentUser = mAuth.getCurrentUser();
        if (currentUser != null) {
            switchToMainActivity(currentUser.getPhoneNumber());
            return;
        }

        incorrectCodeText = findViewById(R.id.incorrectCodeText);
        userNameText = findViewById(R.id.userName);
        phoneNumberText = findViewById(R.id.phoneNumber);
        codeInput = findViewById(R.id.CodeInput);
        codeInputLabel = findViewById(R.id.codeInputText);
        createProfileButton = findViewById(R.id.create_profile_button);
        formatText = findViewById(R.id.formatNumber);

        // Set input types and filters
        userNameText.setInputType(InputType.TYPE_CLASS_TEXT);
        userNameText.setImeOptions(EditorInfo.IME_ACTION_DONE);
        userNameText.setFilters(new InputFilter[]{new InputFilter.LengthFilter(11)});

        phoneNumberText.setInputType(InputType.TYPE_CLASS_PHONE);
        phoneNumberText.setImeOptions(EditorInfo.IME_ACTION_DONE);
        phoneNumberText.setFilters(new InputFilter[]{new InputFilter.LengthFilter(16)});

        codeInput.setInputType(InputType.TYPE_CLASS_TEXT);
        codeInput.setImeOptions(EditorInfo.IME_ACTION_DONE);

        clearInputs();
        hideCodeInput();

        createProfileButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                String phoneNumber = phoneNumberText.getText().toString().trim();
                if (!phoneNumber.isEmpty()) {
                    checkIfUserExists(phoneNumber);
                } else {
                    Toast.makeText(ProfileCreation.this, "Please enter a valid phone number.", Toast.LENGTH_SHORT).show();
                }
            }
        });

        requestLocationPermissions();
        requestNotificationPermissions();

        codeInput.setOnKeyListener((v, keyCode, event) -> {
            if (event.getAction() == android.view.KeyEvent.ACTION_DOWN && keyCode == android.view.KeyEvent.KEYCODE_ENTER) {
                String code = codeInput.getText().toString().trim();
                if (!code.isEmpty()) {
                    verifyCode(code);
                } else {
                    Toast.makeText(ProfileCreation.this, "Please enter the verification code.", Toast.LENGTH_SHORT).show();
                }
                return true;
            }
            return false;
        });
    }

    private void requestLocationPermissions() {
        if (ContextCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(this,
                    new String[]{Manifest.permission.ACCESS_FINE_LOCATION, Manifest.permission.ACCESS_COARSE_LOCATION},
                    REQUEST_CODE_LOCATION);
        }
    }

    private void requestNotificationPermissions() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            if (ContextCompat.checkSelfPermission(this, Manifest.permission.POST_NOTIFICATIONS) != PackageManager.PERMISSION_GRANTED) {
                ActivityCompat.requestPermissions(this,
                        new String[]{Manifest.permission.POST_NOTIFICATIONS},
                        REQUEST_CODE_POST_NOTIFICATIONS);
            }
        }
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions,
                                           @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        switch (requestCode) {
            case REQUEST_CODE_LOCATION:
                if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                    // Location permission granted
                }
                break;

            case REQUEST_CODE_POST_NOTIFICATIONS:
                if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                    // Notification permission granted
                }
                break;
        }
    }

    private void clearInputs() {
        userNameText.setText("");
        phoneNumberText.setText("");
        codeInput.setText("");
    }

    private void hideCodeInput() {
        codeInput.setVisibility(View.GONE);
        codeInputLabel.setVisibility(View.GONE);
    }

    private void showCodeInput() {
        codeInput.setVisibility(View.VISIBLE);
        codeInputLabel.setVisibility(View.VISIBLE);
    }

    private void checkIfUserExists(String phoneNumber) {
        // Here, replace with your method to check if the user exists
        // For demonstration, we assume the user does not exist
        sendVerificationCode(phoneNumber);
        hideKeyboard(createProfileButton);
        showCodeInput();
        formatText.setVisibility(View.GONE);
        userNameText.setVisibility(View.GONE);
        phoneNumberText.setVisibility(View.GONE);
        createProfileButton.setVisibility(View.GONE);
    }

    private void sendVerificationCode(String phoneNumber) {
        PhoneAuthOptions options = PhoneAuthOptions.newBuilder(mAuth)
                .setPhoneNumber(phoneNumber)
                .setTimeout(60L, TimeUnit.SECONDS)
                .setActivity(this)
                .setCallbacks(new PhoneAuthProvider.OnVerificationStateChangedCallbacks() {
                    @Override
                    public void onVerificationCompleted(PhoneAuthCredential credential) {
                        signInWithPhoneAuthCredential(credential);
                    }

                    @Override
                    public void onVerificationFailed(@NonNull FirebaseException e) {
                        Toast.makeText(ProfileCreation.this, "Verification failed: " + e.getMessage(), Toast.LENGTH_SHORT).show();
                    }

                    @Override
                    public void onCodeSent(String vId, PhoneAuthProvider.ForceResendingToken token) {
                        verificationId = vId;
                        Toast.makeText(ProfileCreation.this, "Code sent to " + phoneNumber, Toast.LENGTH_SHORT).show();
                    }
                })
                .build();
        PhoneAuthProvider.verifyPhoneNumber(options);
    }

    private void verifyCode(String code) {
        PhoneAuthCredential credential = PhoneAuthProvider.getCredential(verificationId, code);
        signInWithPhoneAuthCredential(credential);
    }

    private void signInWithPhoneAuthCredential(PhoneAuthCredential credential) {
        mAuth.signInWithCredential(credential)
                .addOnCompleteListener(this, new OnCompleteListener<AuthResult>() {
                    @Override
                    public void onComplete(@NonNull Task<AuthResult> task) {
                        if (task.isSuccessful()) {
                            FirebaseUser user = task.getResult().getUser();
                            updateUI(user);
                            incorrectCodeText.setVisibility(View.GONE);
                        } else {
                            Toast.makeText(ProfileCreation.this, "Verification failed.", Toast.LENGTH_SHORT).show();
                            incorrectCodeText.setVisibility(View.VISIBLE);
                            updateUI(null);
                        }
                    }
                });
    }

    private void updateUI(FirebaseUser user) {
        if (user != null) {
            switchToMainActivity(user.getPhoneNumber());
        }
    }

    private void switchToMainActivity(String phoneNumber) {
        Intent intent = new Intent(ProfileCreation.this, MainActivity.class);
        intent.putExtra("USER_PHONE", phoneNumber);
        startActivity(intent);
        finish();
    }

    private void hideKeyboard(View view) {
        InputMethodManager imm = (InputMethodManager) getSystemService(INPUT_METHOD_SERVICE);
        if (imm != null) {
            imm.hideSoftInputFromWindow(view.getWindowToken(), 0);
        }
    }
}
