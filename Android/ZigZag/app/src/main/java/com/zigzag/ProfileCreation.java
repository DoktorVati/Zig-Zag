package com.zigzag;


import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.text.InputType;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;


import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;


import com.google.firebase.FirebaseException;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.auth.PhoneAuthCredential;
import com.google.firebase.auth.PhoneAuthOptions;
import com.google.firebase.auth.PhoneAuthProvider;


import java.util.concurrent.TimeUnit;


public class ProfileCreation extends AppCompatActivity {
    private EditText userNameText;
    private EditText emailText;
    private EditText phoneNumberText;
    private EditText codeInput;
    private TextView codeInputLabel;
    private Button createProfileButton;
    private Button sendCodeButton;
    private TextView incorrectCodeText;


    private FirebaseAuth mAuth;
    private String verificationId;


    private static final String PREFS_NAME = "ProfileCreationPrefs";
    private static final String KEY_CODE_INPUT_VISIBLE = "isCodeInputVisible";
    private static final String KEY_INCORRECT_CODE_VISIBLE = "isIncorrectCodeVisible";
    private static final String KEY_EMAIL = "email";
    private static final String KEY_PHONE_NUMBER = "phoneNumber";
    private static final String KEY_VERIFICATION_ID = "verificationId";


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.create_profile);


        mAuth = FirebaseAuth.getInstance();


        userNameText = findViewById(R.id.userName);
        emailText = findViewById(R.id.email);
        phoneNumberText = findViewById(R.id.phoneNumber);
        codeInput = findViewById(R.id.CodeInput);
        codeInputLabel = findViewById(R.id.codeInputText);
        createProfileButton = findViewById(R.id.create_profile_button);
        sendCodeButton = findViewById(R.id.sendCode);
        incorrectCodeText = findViewById(R.id.incorrectCodeText);


        setInputFilters();


        createProfileButton.setOnClickListener(v -> sendVerificationCode());
        sendCodeButton.setOnClickListener(v -> verifyCode());


        // Restore visibility states and input values
        restoreVisibilityStates();
        restoreInputValues();
    }


    @Override
    protected void onPause() {
        super.onPause();
        // Save visibility states and input values
        saveVisibilityStates();
        saveInputValues();
    }


    private void setInputFilters() {
        userNameText.setInputType(InputType.TYPE_CLASS_TEXT);
        emailText.setInputType(InputType.TYPE_TEXT_VARIATION_EMAIL_ADDRESS);
        phoneNumberText.setInputType(InputType.TYPE_CLASS_PHONE);
        codeInput.setInputType(InputType.TYPE_CLASS_TEXT);
    }


    private void saveVisibilityStates() {
        SharedPreferences prefs = getSharedPreferences(PREFS_NAME, MODE_PRIVATE);
        SharedPreferences.Editor editor = prefs.edit();
        editor.putBoolean(KEY_CODE_INPUT_VISIBLE, codeInput.getVisibility() == View.VISIBLE);
        editor.putBoolean(KEY_INCORRECT_CODE_VISIBLE, incorrectCodeText.getVisibility() == View.VISIBLE);
        editor.apply();
    }


    private void restoreVisibilityStates() {
        SharedPreferences prefs = getSharedPreferences(PREFS_NAME, MODE_PRIVATE);
        boolean isCodeInputVisible = prefs.getBoolean(KEY_CODE_INPUT_VISIBLE, false);
        boolean isIncorrectCodeVisible = prefs.getBoolean(KEY_INCORRECT_CODE_VISIBLE, false);


        if (isCodeInputVisible) {
            showCodeInput(prefs.getString(KEY_PHONE_NUMBER, ""));
        } else {
            hideCodeInput();
        }


        incorrectCodeText.setVisibility(isIncorrectCodeVisible ? View.VISIBLE : View.GONE);
    }


    private void saveInputValues() {
        SharedPreferences prefs = getSharedPreferences(PREFS_NAME, MODE_PRIVATE);
        SharedPreferences.Editor editor = prefs.edit();
        editor.putString(KEY_EMAIL, emailText.getText().toString().trim());
        editor.putString(KEY_PHONE_NUMBER, phoneNumberText.getText().toString().trim());
        editor.putString(KEY_VERIFICATION_ID, verificationId); // Save verification ID
        editor.apply();
    }


    private void restoreInputValues() {
        SharedPreferences prefs = getSharedPreferences(PREFS_NAME, MODE_PRIVATE);
        String email = prefs.getString(KEY_EMAIL, "");
        String phoneNumber = prefs.getString(KEY_PHONE_NUMBER, "");
        verificationId = prefs.getString(KEY_VERIFICATION_ID, null); // Restore verification ID


        emailText.setText(email);
        phoneNumberText.setText(phoneNumber);


        // If we have a verification ID, show the code input
        if (verificationId != null) {
            showCodeInput(phoneNumber);
        }
    }


    private void sendVerificationCode() {
        String email = emailText.getText().toString().trim();
        String userName = userNameText.getText().toString().trim();
        String phoneNumber = phoneNumberText.getText().toString().trim();


        if (email.isEmpty() || userName.isEmpty() || phoneNumber.isEmpty()) {
            Toast.makeText(this, "Please fill in all fields.", Toast.LENGTH_SHORT).show();
            return;
        }


        // Save the email and phone number before sending the verification code
        saveInputValues();


        PhoneAuthOptions options = PhoneAuthOptions.newBuilder(mAuth)
                .setPhoneNumber(phoneNumber)
                .setTimeout(60L, TimeUnit.SECONDS)
                .setActivity(this)
                .setCallbacks(new PhoneAuthProvider.OnVerificationStateChangedCallbacks() {
                    @Override
                    public void onVerificationCompleted(PhoneAuthCredential credential) {
                        linkPhoneNumber(credential, email);  // Pass email to link
                    }


                    @Override
                    public void onVerificationFailed(@NonNull FirebaseException e) {
                        Toast.makeText(ProfileCreation.this, "Verification failed: " + e.getMessage(), Toast.LENGTH_LONG).show();
                    }


                    @Override
                    public void onCodeSent(@NonNull String verificationId, @NonNull PhoneAuthProvider.ForceResendingToken token) {
                        ProfileCreation.this.verificationId = verificationId;
                        showCodeInput(phoneNumber);
                        saveInputValues(); // Save verification ID after sending
                    }
                })
                .build();
        PhoneAuthProvider.verifyPhoneNumber(options);
    }


    private void showCodeInput(String phoneNumber) {
        codeInput.setVisibility(View.VISIBLE);
        codeInputLabel.setVisibility(View.VISIBLE);
        sendCodeButton.setVisibility(View.VISIBLE);
        codeInputLabel.setText("Enter the code sent to " + phoneNumber);


        userNameText.setVisibility(View.GONE);
        emailText.setVisibility(View.GONE);
        phoneNumberText.setVisibility(View.GONE);
        createProfileButton.setVisibility(View.GONE);
    }


    private void verifyCode() {
        String code = codeInput.getText().toString().trim();
        if (code.isEmpty()) {
            Toast.makeText(this, "Please enter the verification code.", Toast.LENGTH_SHORT).show();
            return;
        }


        PhoneAuthCredential credential = PhoneAuthProvider.getCredential(verificationId, code);
        linkPhoneNumber(credential, emailText.getText().toString().trim());
    }


    private void linkPhoneNumber(PhoneAuthCredential credential, String email) {
        FirebaseUser user = mAuth.getCurrentUser();
        if (user != null) {
            user.linkWithCredential(credential)
                    .addOnCompleteListener(this, task -> {
                        if (task.isSuccessful()) {
                            // Phone number linked successfully
                            Toast.makeText(ProfileCreation.this, "Phone number linked successfully!", Toast.LENGTH_SHORT).show();
                            switchToMainActivity(user.getUid());
                        } else {
                            Toast.makeText(ProfileCreation.this, "Linking phone number failed: " + task.getException().getMessage(), Toast.LENGTH_SHORT).show();
                            incorrectCodeText.setVisibility(View.VISIBLE);
                        }
                    });
        } else {
            // No user signed in, create a new account with the email
            createUserAccount(email, credential);
        }
    }


    private void createUserAccount(String email, PhoneAuthCredential credential) {
        // Use a secure password in real applications
        String password = "defaultPassword"; // Replace with a secure password handling mechanism
        mAuth.createUserWithEmailAndPassword(email, password)
                .addOnCompleteListener(this, task -> {
                    if (task.isSuccessful()) {
                        // Account created successfully, now link the phone number
                        FirebaseUser newUser = mAuth.getCurrentUser();
                        if (newUser != null) {
                            newUser.linkWithCredential(credential)
                                    .addOnCompleteListener(this, linkTask -> {
                                        if (linkTask.isSuccessful()) {
                                            Toast.makeText(ProfileCreation.this, "Phone number linked successfully!", Toast.LENGTH_SHORT).show();
                                            switchToMainActivity(newUser.getUid());
                                        } else {
                                            Toast.makeText(ProfileCreation.this, "Linking phone number failed: " + linkTask.getException().getMessage(), Toast.LENGTH_SHORT).show();
                                        }
                                    });
                        }
                    } else {
                        Toast.makeText(ProfileCreation.this, "Failed to create user: " + task.getException().getMessage(), Toast.LENGTH_SHORT).show();
                    }
                });
    }




    private void createUserAccount(String email) {
        // Use a secure password in real applications
        String password = "defaultPassword";
        mAuth.createUserWithEmailAndPassword(email, password)
                .addOnCompleteListener(this, task -> {
                    if (task.isSuccessful()) {
                        Toast.makeText(ProfileCreation.this, "User account created successfully!", Toast.LENGTH_SHORT).show();
                        switchToMainActivity(mAuth.getCurrentUser().getUid());
                    } else {
                        Toast.makeText(ProfileCreation.this, "Failed to create user: " + task.getException().getMessage(), Toast.LENGTH_SHORT).show();
                    }
                });
    }


    private void switchToMainActivity(String userId) {
        Intent intent = new Intent(ProfileCreation.this, MainActivity.class);
        intent.putExtra("USER_ID", userId);
        startActivity(intent);
        finish();
    }


    private void hideCodeInput() {
        codeInput.setVisibility(View.GONE);
        codeInputLabel.setVisibility(View.GONE);
        sendCodeButton.setVisibility(View.GONE);
    }


    @Override
    protected void onDestroy() {
        super.onDestroy();
    }
}
