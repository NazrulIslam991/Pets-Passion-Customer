package com.example.petspassion;

import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.content.ContextCompat;

import android.content.Intent;
import android.content.IntentFilter;
import android.net.ConnectivityManager;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.Gravity;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.FrameLayout;
import android.widget.ProgressBar;
import android.widget.Toast;

import com.google.firebase.auth.FirebaseAuth;

public class PasswordReSet extends AppCompatActivity implements NetworkChangeReceiver.NetworkListener {

    private EditText resetEmailEditText;
    private Button resetButton;
    private ALodingDialog loadingDialog;
    private FirebaseAuth firebaseAuth;
    private NetworkChangeReceiver networkChangeReceiver;
    private AlertDialog networkDialog;
    private static final String IGNORED_EMAIL = "nazrulislamnayon991@gmail.com";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_password_re_set);
        getWindow().setStatusBarColor(ContextCompat.getColor(PasswordReSet.this, R.color.adminHome));

        // Initialize FirebaseAuth instance
        firebaseAuth = FirebaseAuth.getInstance();

        // Initialize the custom loading dialog
        loadingDialog = new ALodingDialog(this);

        // Bind views
        resetEmailEditText = findViewById(R.id.resetemail);
        resetButton = findViewById(R.id.resetbutton);

        // Set onClickListener for reset button
        resetButton.setOnClickListener(v -> {
            String email = resetEmailEditText.getText().toString().trim();

            if(email.isEmpty()){
                resetEmailEditText.requestFocus();
                resetEmailEditText.setError("Email cannot be empty");
                return;
            }

            else if (!email.matches("^[a-zA-Z0-9]+([._-][a-zA-Z0-9]+)*@(gmail\\.com|outlook\\.com|yahoo\\.com)$") && !email.matches("^cse_[0-9]{16}@lus\\.ac\\.bd$")) {
                resetEmailEditText.requestFocus();
                resetEmailEditText.setError("Invalid email format");
                return;
            }

            // Check if the email is the ignored one
            else if (email.equalsIgnoreCase(IGNORED_EMAIL)) {
                resetEmailEditText.setError("Invalid Email !!");
                return;
            }

            else{
                // Show the progress bar
                loadingDialog.show(); // Show custom loading dialog

                // Send password reset email for other emails
                firebaseAuth.sendPasswordResetEmail(email)
                        .addOnCompleteListener(task -> {
                            // Hide the progress bar
                            loadingDialog.dismiss(); // Hide loading dialog after operation

                            if (task.isSuccessful()) {
                                Toast.makeText(PasswordReSet.this, "Password reset email sent!", Toast.LENGTH_SHORT).show();
                                Intent intent = new Intent(PasswordReSet.this, Login_Page.class);
                                startActivity(intent);
                                finish();
                            } else {
                                Toast.makeText(PasswordReSet.this, "Error: " + task.getException().getMessage(), Toast.LENGTH_SHORT).show();
                            }
                        });
            }
        });

        // Network receiver to monitor network changes
        networkChangeReceiver = new NetworkChangeReceiver(this);
        registerReceiver(networkChangeReceiver, new IntentFilter(ConnectivityManager.CONNECTIVITY_ACTION));

        showNetworkDialogIfNeeded(); // Check on startup if network is available
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        if (networkChangeReceiver != null) {
            unregisterReceiver(networkChangeReceiver);
        }
    }

    @Override
    public void onNetworkConnected() {
        if (networkDialog != null && networkDialog.isShowing()) {
            networkDialog.dismiss();
        }
    }

    @Override
    public void onNetworkDisconnected() {
        showNetworkDialog();
    }

    private void showNetworkDialog() {
        if (networkDialog == null || !networkDialog.isShowing()) {
            // Inflate the custom layout for the dialog
            View dialogView = getLayoutInflater().inflate(R.layout.dialog_check_connection, null);

            networkDialog = new AlertDialog.Builder(this)
                    .setView(dialogView)
                    .setCancelable(false)
                    .create();

            networkDialog.show();
        }
    }

    private void showNetworkDialogIfNeeded() {
        if (!NetworkUtils.isNetworkAvailable(this)) {
            showNetworkDialog();
        }
    }
}
