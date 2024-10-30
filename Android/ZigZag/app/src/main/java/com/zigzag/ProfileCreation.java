package com.zigzag;

import android.content.Intent;
import android.os.Bundle;
import android.text.InputFilter;
import android.text.InputType;
import android.view.View;
import android.view.inputmethod.EditorInfo;
import android.view.inputmethod.InputMethodManager;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;

import androidx.appcompat.app.AppCompatActivity;

public class ProfileCreation extends AppCompatActivity {
    private EditText userNameText;
    private EditText phoneNumberText;
    private TextView formatText;
    private EditText codeInput;
    private TextView codeInputLabel;
    private Button createProfileButton;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.create_profile); // Ensure this is the correct layout file

        // Tie the IDs from XML to the Java variables
        userNameText = findViewById(R.id.userName);
        phoneNumberText = findViewById(R.id.phoneNumber);
        codeInput = findViewById(R.id.CodeInput);
        codeInputLabel = findViewById(R.id.codeInputText); // This is the label for the code input
        createProfileButton = findViewById(R.id.create_profile_button);
        formatText = findViewById(R.id.formatNumber);
        // Set IME options
        userNameText.setInputType(InputType.TYPE_CLASS_TEXT);
        userNameText.setImeOptions(EditorInfo.IME_ACTION_DONE);
        userNameText.setFilters(new InputFilter[]{new InputFilter.LengthFilter(11)}); // 11 char limit for username

        phoneNumberText.setInputType(InputType.TYPE_CLASS_PHONE);
        phoneNumberText.setImeOptions(EditorInfo.IME_ACTION_DONE);
        phoneNumberText.setFilters(new InputFilter[]{new InputFilter.LengthFilter(12)}); // 12 char limit for phone number

        codeInput.setInputType(InputType.TYPE_CLASS_TEXT);
        codeInput.setImeOptions(EditorInfo.IME_ACTION_DONE);

        // Clear text inputs on load
        userNameText.setText("");
        phoneNumberText.setText("");
        codeInput.setText("");

        // Initially hide the code input and label
        codeInput.setVisibility(View.GONE);
        codeInputLabel.setVisibility(View.GONE); // Hide the label at startup

        // Create Profile button click listener
        createProfileButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                codeInput.setVisibility(View.VISIBLE);
                codeInputLabel.setVisibility(View.VISIBLE);
                hideKeyboard(v);

                formatText.setVisibility(View.GONE);
                userNameText.setVisibility(View.GONE);
                phoneNumberText.setVisibility(View.GONE);
                createProfileButton.setVisibility(View.GONE);
            }
        });

        // Setup key listeners
        setupKeyListener(userNameText);
        setupKeyListener(phoneNumberText);
        setupKeyListener(codeInput);

    }

    private void setupKeyListener(EditText editText) {
        editText.setOnKeyListener((v, keyCode, event) -> {
            if (keyCode == android.view.KeyEvent.KEYCODE_ENTER) {
                hideKeyboard(v);
                switchToMainActivity(); // Call function to switch to MainActivity
                return true;
            }
            return false;
        });
    }

    private void switchToMainActivity() {
        Intent intent = new Intent(ProfileCreation.this, MainActivity.class);
        startActivity(intent);
        finish(); // Optional: Close the current activity
    }

    private void hideKeyboard(View view) {
        InputMethodManager imm = (InputMethodManager) getSystemService(INPUT_METHOD_SERVICE);
        if (imm != null) {
            imm.hideSoftInputFromWindow(view.getWindowToken(), 0);
        }
    }
}
