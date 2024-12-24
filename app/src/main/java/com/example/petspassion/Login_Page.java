package com.example.petspassion;

import androidx.appcompat.app.AppCompatActivity;
import androidx.core.content.ContextCompat;

import android.content.Intent;
import android.os.Bundle;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.EditText;
import android.widget.TextView;
import android.view.View;
import android.widget.Toast;

import com.google.android.gms.common.SignInButton;

public class Login_Page extends AppCompatActivity {

    private EditText loginEmail, loginPassword;
    private CheckBox passwordShowHide;
    private TextView forgotPassword, createAccount;
    private Button loginButton;
    private SignInButton googleSignInButton;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login_page);
        getWindow().setStatusBarColor(ContextCompat.getColor(Login_Page.this, R.color.adminHome));


        // Initialize views
        loginEmail = findViewById(R.id.login_email);
        loginPassword = findViewById(R.id.login_password);
        passwordShowHide = findViewById(R.id.login_password_show_hide);
        forgotPassword = findViewById(R.id.forgot_password);
        createAccount = findViewById(R.id.create_account);
        loginButton = findViewById(R.id.login_button);
        googleSignInButton = findViewById(R.id.google_sign_in_button);



        // Show/Hide password functionality
        passwordShowHide.setOnCheckedChangeListener((buttonView, isChecked) -> {
            if (isChecked) {
                loginPassword.setInputType(android.text.InputType.TYPE_TEXT_VARIATION_VISIBLE_PASSWORD);
            } else {
                loginPassword.setInputType(android.text.InputType.TYPE_CLASS_TEXT | android.text.InputType.TYPE_TEXT_VARIATION_PASSWORD);
            }
            loginPassword.setSelection(loginPassword.getText().length());
        });



        forgotPassword.setOnClickListener(v -> {
            Intent intent = new Intent(Login_Page.this, PasswordReSet.class);
            startActivity(intent);
        });



        createAccount.setOnClickListener(v -> {
            Intent intent = new Intent(Login_Page.this, Create_Account_Page.class);
            startActivity(intent);
        });



        loginButton.setOnClickListener(v -> {
            // Perform validation
            if (!validateEmail()) {
                return;
            } else if (!validatePassword()) {
                return;
            }
            else{
                // Perform login or authentication
                String email = loginEmail.getText().toString().trim();
                String password = loginPassword.getText().toString().trim();
                Toast.makeText(this, "Login Successful!", Toast.LENGTH_SHORT).show();
                // After checking, proceed to the home page
                Intent intent = new Intent(Login_Page.this, Customer_Home.class);
                intent.putExtra("fragmentToLoad", "Home");
                startActivity(intent);
                finish();
            }
        });

        googleSignInButton.setOnClickListener(v -> {
            // Handle Google Sign-In button click
        });
    }




    // Email validation method
    private boolean validateEmail() {
        String email = loginEmail.getText().toString().trim();
        if (email.isEmpty()) {
            loginEmail.setError("Email cannot be empty");
            loginEmail.requestFocus();
            return false;
        } else if (!email.matches("^[a-zA-Z0-9]+([._-][a-zA-Z0-9]+)*@(gmail\\.com|outlook\\.com|yahoo\\.com)$") &&
                !email.matches("^cse_[0-9]{16}@lus\\.ac\\.bd$")) {
            loginEmail.setError("Invalid email format");
            loginEmail.requestFocus();
            return false;
        } else {
            loginEmail.setError(null);
            return true;
        }
    }

    // Password validation method
    private boolean validatePassword() {
        String password = loginPassword.getText().toString().trim();
        if (password.isEmpty()) {
            loginPassword.setError("Password cannot be empty");
            loginPassword.requestFocus();
            return false;
        } else if (!password.matches("^(?=.*[0-9])(?=.*[a-z])(?=.*[A-Z])(?=.*[\\W_]).{6,16}$")) {
            loginPassword.setError("Password must be 6-16 characters, include at least one digit, one lowercase letter, one uppercase letter, and one special character.");
            loginPassword.requestFocus();
            return false;
        } else {
            loginPassword.setError(null);
            return true;
        }
    }
}
