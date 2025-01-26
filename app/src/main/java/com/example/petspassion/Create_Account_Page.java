package com.example.petspassion;

import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.content.ContextCompat;

import android.content.Intent;
import android.content.IntentFilter;
import android.net.ConnectivityManager;
import android.os.Bundle;
import android.os.Handler;
import android.util.Log;
import android.view.Gravity;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class Create_Account_Page extends AppCompatActivity implements NetworkChangeReceiver.NetworkListener {

    private EditText name, email, password, confirmpassword, mobile;
    private Button create_done;
    private FirebaseDatabase database;
    private DatabaseReference reference;
    private ALodingDialog loadingDialog;
    private Handler handler;
    private FirebaseAuth auth;
    private FirebaseUser currentUser;
    private NetworkChangeReceiver networkChangeReceiver;
    private AlertDialog networkDialog;
    private boolean isEmailVerified = false;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_create_account_page);

        name = findViewById(R.id.create_name);
        email = findViewById(R.id.create_email);
        password = findViewById(R.id.create_password);
        confirmpassword = findViewById(R.id.create_repassword);
        mobile = findViewById(R.id.create_mobile);
        create_done = findViewById(R.id.Create_done);
        handler = new Handler();

        auth = FirebaseAuth.getInstance();
        database = FirebaseDatabase.getInstance();
        reference = database.getReference("users_information");

        getWindow().setStatusBarColor(ContextCompat.getColor(Create_Account_Page.this, R.color.adminHome));

        // Initialize the custom loading dialog
        loadingDialog = new ALodingDialog(this);

        TextView move_login = findViewById(R.id.login_page);
        move_login.setOnClickListener(v -> {
            Toast.makeText(Create_Account_Page.this, "Login Page", Toast.LENGTH_SHORT).show();
            Intent intent = new Intent(Create_Account_Page.this, Login_Page.class);
            startActivity(intent);
            finish();
        });

        create_done.setOnClickListener(v -> {
            String user_name = name.getText().toString();
            String user_email = email.getText().toString();
            String user_password = password.getText().toString();
            String user_conpassword = confirmpassword.getText().toString();
            String user_mobile = mobile.getText().toString();

            Log.d("CreateAccount", "Name: " + user_name);
            Log.d("CreateAccount", "Email: " + user_email);
            Log.d("CreateAccount", "Password: " + user_password);
            Log.d("CreateAccount", "Confirm Password: " + user_conpassword);
            Log.d("CreateAccount", "Mobile: " + user_mobile);



            if(!validateUserName()){
                return;
            }

            else if(!validateUseremail()){
                return;
            }

            else if (!validatePassword()) {
                return;
            }

            else if (!validateCom_Password()) {
                return;
            }

            else if(!validateUserMobile()){
                return;
            }


            else if (!user_password.equals(user_conpassword)) {
                confirmpassword.requestFocus();
                confirmpassword.setError("password doesn't match");
                return;
            }


            else{
                loadingDialog.show(); // Show custom loading dialog
                createUserAccount(user_name, user_email, user_password, user_mobile);
            }


        });

        // Network receiver to monitor network changes
        networkChangeReceiver = new NetworkChangeReceiver(this);
        registerReceiver(networkChangeReceiver, new IntentFilter(ConnectivityManager.CONNECTIVITY_ACTION));

        showNetworkDialogIfNeeded(); // Check on startup if network is available
    }





    private void createUserAccount(String user_name, String user_email, String user_password, String user_mobile) {
        auth.createUserWithEmailAndPassword(user_email, user_password)
                .addOnCompleteListener(this, authTask -> {
                    if (authTask.isSuccessful()) {
                        currentUser = auth.getCurrentUser();
                        if (currentUser != null) {
                            String uid = currentUser.getUid();
                            // Send email verification
                            currentUser.sendEmailVerification()
                                    .addOnCompleteListener(emailTask -> {
                                        if (emailTask.isSuccessful()) {
                                            Toast.makeText(getApplicationContext(), "Verification email sent.", Toast.LENGTH_SHORT).show();
                                            // Start timer to check email verification
                                            startEmailVerificationTimer(user_name, user_email, user_password, user_mobile, uid);
                                        } else {
                                            loadingDialog.dismiss(); // Hide loading dialog after operation
                                            Toast.makeText(getApplicationContext(), "Failed to send verification email: " + emailTask.getException().getMessage(), Toast.LENGTH_SHORT).show();
                                            // Optionally, delete the account if verification fails
                                            if (currentUser != null) {
                                                currentUser.delete().addOnCompleteListener(deleteTask -> {
                                                    if (deleteTask.isSuccessful()) {
                                                        Toast.makeText(getApplicationContext(), "Account deleted due to email verification failure.", Toast.LENGTH_SHORT).show();
                                                    }
                                                });
                                            }
                                        }
                                    });
                        }
                    } else {
                        loadingDialog.dismiss(); // Hide loading dialog after operation
                        Toast.makeText(getApplicationContext(), "Failed to create account: " + authTask.getException().getMessage(), Toast.LENGTH_SHORT).show();
                    }
                });
    }

    private void startEmailVerificationTimer(String user_name, String user_email, String user_password, String user_mobile, String uid) {
        handler.postDelayed(() -> {
            if (currentUser != null) {
                currentUser.reload().addOnCompleteListener(reloadTask -> {
                    if (reloadTask.isSuccessful()) {
                        if (!currentUser.isEmailVerified()) {
                            // Email not verified in time
                            loadingDialog.dismiss(); // Hide loading dialog
                            Toast.makeText(getApplicationContext(), "Email not verified in time. Deleting account...", Toast.LENGTH_SHORT).show();
                            // Delete the account from Firebase Authentication
                            currentUser.delete().addOnCompleteListener(deleteTask -> {
                                if (deleteTask.isSuccessful()) {
                                    Toast.makeText(getApplicationContext(), "Account deleted due to email verification timeout.", Toast.LENGTH_SHORT).show();
                                }
                            });
                        } else {
                            // Email verified
                            isEmailVerified = true;
                            User user = new User(uid, user_name, user_email, user_mobile, ""); // Assuming address is empty
                            reference.child(uid).setValue(user).addOnCompleteListener(dbTask -> {
                                loadingDialog.dismiss(); // Hide loading dialog after operation
                                if (dbTask.isSuccessful()) {
                                    Toast.makeText(getApplicationContext(), "Account created successfully!", Toast.LENGTH_SHORT).show();
                                    Intent intent = new Intent(Create_Account_Page.this, Login_Page.class);
                                    startActivity(intent);
                                    finish();
                                } else {
                                    Toast.makeText(getApplicationContext(), "Failed to save user data: " + dbTask.getException().getMessage(), Toast.LENGTH_SHORT).show();
                                }
                            });
                        }
                    } else {
                        loadingDialog.dismiss(); // Hide loading dialog
                        Toast.makeText(getApplicationContext(), "Failed to reload user data: " + reloadTask.getException().getMessage(), Toast.LENGTH_SHORT).show();
                    }
                });
            }
        }, 30000); // 30 seconds
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



    public boolean validateUseremail(){
        String user_email = email.getText().toString();
        if(user_email.isEmpty()){
            email.requestFocus();
            email.setError("Email cannot be empty");
            return false;
        }
        // Check if the email format is valid
        else if (!user_email.matches("^[a-zA-Z0-9]+([._-][a-zA-Z0-9]+)*@(gmail\\.com|outlook\\.com|yahoo\\.com)$") && !user_email.matches("^cse_[0-9]{16}@lus\\.ac\\.bd$")) {
            email.requestFocus();
            email.setError("Invalid email format");
            return false;
        }
        else {
            email.setError(null);
            return true;
        }
    }




    public Boolean validatePassword() {
        String pass = password.getText().toString();

        if (pass.isEmpty()) {
            password.requestFocus();
            password.setError("Password cannot be empty");
            return false;
        }

        else if (!pass.matches("^(?=.*[0-9])(?=.*[a-z])(?=.*[A-Z])(?=.*[\\W_]).{6,16}$")) {
            password.requestFocus();
            password.setError("Password must be between 6 to 16 characters long, " + "include at least one digit, one lowercase letter, " + "one uppercase letter, and one special character.");
            return false;
        }
        else {
            password.setError(null);
            return true;
        }
    }

    private boolean validateCom_Password() {
        String pass = confirmpassword.getText().toString();

        if (pass.isEmpty()) {
            confirmpassword.requestFocus();
            confirmpassword.setError("Password cannot be empty");
            return false;
        }

        else if (!pass.matches("^(?=.*[0-9])(?=.*[a-z])(?=.*[A-Z])(?=.*[\\W_]).{6,16}$")) {
            confirmpassword.requestFocus();
            confirmpassword.setError("Password must be between 6 to 16 characters long, " + "include at least one digit, one lowercase letter, " + "one uppercase letter, and one special character.");
            return false;
        }
        else {
            confirmpassword.setError(null);
            return true;
        }
    }


    private boolean validateUserName() {
        String user_name = name.getText().toString();

        if (user_name.isEmpty()) {
            name.requestFocus();
            name.setError("name cannot be empty");
            return false;
        }

        else if (!user_name.matches("^[a-zA-Z][a-zA-Z\\s]*$")) {
            name.requestFocus();
            name.setError("Name contain only Character");
            return false;
        }
        else {
            name.setError(null);
            return true;
        }
    }



    private boolean validateUserMobile() {
        String user_mobile = mobile.getText().toString();

        if (user_mobile.isEmpty()) {
            mobile.requestFocus();
            mobile.setError("mobile cannot be empty");
            return false;
        }

        else if (!user_mobile.matches("^\\+880\\d{10}$")) {
            mobile.requestFocus();
            mobile.setError("number start with +880...");
            return false;
        }
        else {
            mobile.setError(null);
            return true;
        }
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        if (networkChangeReceiver != null) {
            unregisterReceiver(networkChangeReceiver);
        }
    }
}



