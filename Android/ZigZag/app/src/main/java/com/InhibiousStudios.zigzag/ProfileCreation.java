package com.InhibiousStudios.zigzag;


import android.app.DatePickerDialog;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.text.InputType;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.LinearLayout;
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
import java.util.Calendar;
import java.util.concurrent.TimeUnit;
public class ProfileCreation extends AppCompatActivity {
    private TextView textInputError;

    private EditText emailText;
    private EditText phoneNumberText;
    private EditText codeInput;
    private EditText password;
    private PhoneAuthProvider.ForceResendingToken resendToken;
    private TextView codeInputLabel;
    private TextView phoneLabel;
    private TextView passwordLabel;
    private TextView emailLabel;
    private Button createProfileButton;
    private Button sendCodeButton;
    private Button showPasswordButton;
    private LinearLayout phoneLayout;
    private LinearLayout passwordlayout;
    private ImageButton backButton;
    private ImageButton backButton2;
    private TextView incorrectCodeText;


    private FirebaseAuth mAuth;
    private String verificationId;


    static final String PREFS_NAME = "ProfileCreationPrefs";
    private static final String KEY_CODE_INPUT_VISIBLE = "isCodeInputVisible";
    private static final String KEY_INCORRECT_CODE_VISIBLE = "isIncorrectCodeVisible";
    static final String KEY_EMAIL = "email";
    private static final String KEY_PASSWORD = "password";
    static final String KEY_PHONE_NUMBER = "phoneNumber";
    private static final String KEY_VERIFICATION_ID = "verificationId";

    private EditText birthDateInput;
    private TextView birthdayLabel;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.create_profile);


        mAuth = FirebaseAuth.getInstance();

        emailText = findViewById(R.id.email);
        emailLabel = findViewById(R.id.emailLabel);
        password = findViewById(R.id.password);
        passwordlayout = findViewById(R.id.passwordLayout);
        phoneNumberText = findViewById(R.id.phoneNumber);
        phoneLabel = findViewById(R.id.phoneLabel);
        phoneLayout = findViewById(R.id.phoneNumberLayout);

        codeInput = findViewById(R.id.CodeInput);
        codeInputLabel = findViewById(R.id.codeInputText);
        createProfileButton = findViewById(R.id.create_profile_button);
        sendCodeButton = findViewById(R.id.sendCode);
        incorrectCodeText = findViewById(R.id.incorrectCodeText);
        showPasswordButton = findViewById(R.id.showPasswordButton);
        backButton = findViewById(R.id.cancelButton);
        backButton2 = findViewById(R.id.cancelButton2);

        passwordLabel = findViewById(R.id.passwordLabel);
        textInputError = findViewById(R.id.textInputError);


        setInputFilters();


        createProfileButton.setOnClickListener(v -> sendVerificationCode());
        sendCodeButton.setOnClickListener(v -> verifyCode());
        showPasswordButton.setOnClickListener(v -> togglePasswordVisibility());
        backButton.setOnClickListener(v -> backToLogin());
        backButton2.setOnClickListener(v -> hideCodeInput());
        birthDateInput = findViewById(R.id.birthDateInput);
        birthdayLabel = findViewById(R.id.birthDateLabel);

        birthDateInput.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                showDatePickerDialog();
            }
        });
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
        editor.putString(KEY_PASSWORD, password.getText().toString().trim());
        editor.apply();
    }

    private void backToLogin()
    {
        //hideCodeInput();
        Intent intent = new Intent(ProfileCreation.this, ProfileLogin.class);
        startActivity(intent);
        finish();
    }
    private void restoreInputValues() {
        SharedPreferences prefs = getSharedPreferences(PREFS_NAME, MODE_PRIVATE);
        String email = prefs.getString(KEY_EMAIL, "");
        String phoneNumber = prefs.getString(KEY_PHONE_NUMBER, "");
        String passwordText = prefs.getString(KEY_PASSWORD, ""); // Retrieve the password

        emailText.setText(email);
        phoneNumberText.setText(phoneNumber);
        password.setText(passwordText); // Restore the password

        // If we have a verification ID, show the code input
        if (verificationId != null) {
            showCodeInput(phoneNumber);
        }
    }


    private void sendVerificationCode() {
        String email = emailText.getText().toString().trim();
        String phoneNumber = phoneNumberText.getText().toString().trim();
        String birthDate = birthDateInput.getText().toString().trim();

        if (email.isEmpty() || phoneNumber.isEmpty() || birthDate.isEmpty()) {
            // Set the error message in the TextView and make it visible
            textInputError.setText("Please fill in all fields.");
            textInputError.setVisibility(View.VISIBLE);
            return;
        } else {
            // Hide the error message if all fields are filled
            textInputError.setVisibility(View.GONE);
        }

        // Check if the user is at least 13 years old
        if (!isAgeValid(birthDate)) {
            textInputError.setText("You must be at least 13 years old to sign up.");
            textInputError.setVisibility(View.VISIBLE);
            return;
        } else {
            // Hide the error message if all fields are filled
            textInputError.setVisibility(View.GONE);
        }

        // Validate the password
        if (!isPasswordValid(password.getText().toString())) {
            textInputError.setText("Password must be at least 8 characters, contain upper and lower case letters, digits, and special characters.");
            textInputError.setVisibility(View.VISIBLE);
            return;
        } else {
            textInputError.setVisibility(View.GONE);  // Hide the error message
        }
        // Save the email and phone number before sending the verification code
        saveInputValues();

        PhoneAuthOptions options = PhoneAuthOptions.newBuilder(mAuth)
                .setPhoneNumber(phoneNumber)
                .setTimeout(30L, TimeUnit.SECONDS)
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

    private boolean isPasswordValid(String password) {
        // Ensure the password is at least 8 characters and contains uppercase, lowercase, digits, and special characters
        String passwordRegex = "^(?=.*[A-Za-z])(?=.*\\d)(?=.*[!@#$%^&*()_+\\-=\\[\\]{};':\"\\\\|,.<>\\/?]).{8,}$";
        return password.matches(passwordRegex);
    }

    private boolean isAgeValid(String birthDate) {
        try {
            String[] parts = birthDate.split("/");
            int day = Integer.parseInt(parts[0]);
            int month = Integer.parseInt(parts[1]) - 1; // Month is 0-based in Calendar
            int year = Integer.parseInt(parts[2]);

            Calendar birthDateCalendar = Calendar.getInstance();
            birthDateCalendar.set(year, month, day);

            Calendar today = Calendar.getInstance();
            int age = today.get(Calendar.YEAR) - birthDateCalendar.get(Calendar.YEAR);

            // Adjust age if birthday has not occurred this year
            if (today.get(Calendar.DAY_OF_YEAR) < birthDateCalendar.get(Calendar.DAY_OF_YEAR)) {
                age--;
            }

            return age >= 13; // Valid if age is 13 or older
        } catch (Exception e) {
            return false; // If there's an error parsing the date, consider it invalid
        }
    }



    private void showCodeInput(String phoneNumber) {
        codeInput.setVisibility(View.VISIBLE);
        codeInputLabel.setVisibility(View.VISIBLE);
        sendCodeButton.setVisibility(View.VISIBLE);
        codeInputLabel.setText("Enter the code sent to " + phoneNumber);
        backButton2.setVisibility(View.VISIBLE);

        birthdayLabel.setVisibility(View.GONE);
        birthDateInput.setVisibility(View.GONE);
        backButton.setVisibility(View.GONE);
        password.setVisibility(View.GONE);
        passwordLabel.setVisibility(View.GONE);
        phoneLabel.setVisibility(View.GONE);
        emailLabel.setVisibility(View.GONE);
        phoneLayout.setVisibility(View.GONE);
        passwordlayout.setVisibility(View.GONE);
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
        String password = this.password.getText().toString().trim(); // Use the password from the EditText
        if (password.isEmpty()) {
            Toast.makeText(this, "Please enter a password.", Toast.LENGTH_SHORT).show();
            return;
        }
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




    private void togglePasswordVisibility() {
        // Toggle the password visibility
        if (password.getTransformationMethod() != null) {
            // If the password is currently hidden, show it
            password.setTransformationMethod(null);
            showPasswordButton.setText("Hide");
        } else {
            // If the password is currently visible, hide it
            password.setTransformationMethod(new android.text.method.PasswordTransformationMethod());
            showPasswordButton.setText("Show");
        }
        // Move the cursor to the end of the text
        password.setSelection(password.getText().length());
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
        backButton2.setVisibility(View.GONE);

        birthdayLabel.setVisibility(View.VISIBLE);
        birthDateInput.setVisibility(View.VISIBLE);
        backButton.setVisibility(View.VISIBLE);
        password.setVisibility(View.VISIBLE);
        phoneLabel.setVisibility(View.VISIBLE);
        emailLabel.setVisibility(View.VISIBLE);
        phoneLayout.setVisibility(View.VISIBLE);
        passwordlayout.setVisibility(View.VISIBLE);
        passwordLabel.setVisibility(View.VISIBLE);
        emailText.setVisibility(View.VISIBLE);
        phoneNumberText.setVisibility(View.VISIBLE);
        createProfileButton.setVisibility(View.VISIBLE);
    }

    private void showDatePickerDialog() {
        final Calendar calendar = Calendar.getInstance();
        int year = calendar.get(Calendar.YEAR);
        int month = calendar.get(Calendar.MONTH);
        int day = calendar.get(Calendar.DAY_OF_MONTH);

        DatePickerDialog datePickerDialog = new DatePickerDialog(
                this,
                (view, selectedYear, selectedMonth, selectedDay) -> {
                    // Format month to be 1-based (January is 0)
                    String formattedDate = selectedDay + "/" + (selectedMonth + 1) + "/" + selectedYear;
                    birthDateInput.setText(formattedDate);
                },
                year, month, day);

        datePickerDialog.show();
    }
    @Override
    protected void onDestroy() {
        super.onDestroy();
    }
}