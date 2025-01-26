package com.example.petspassion;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.content.ContextCompat;
import android.content.Intent;
import android.content.IntentFilter;
import android.net.ConnectivityManager;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.util.Log;
import android.view.Gravity;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.EditText;
import android.widget.FrameLayout;
import android.widget.TextView;
import android.widget.Toast;
import com.google.android.gms.auth.api.signin.GoogleSignIn;
import com.google.android.gms.auth.api.signin.GoogleSignInAccount;
import com.google.android.gms.auth.api.signin.GoogleSignInClient;
import com.google.android.gms.auth.api.signin.GoogleSignInOptions;
import com.google.android.gms.common.api.ApiException;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.AuthCredential;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.auth.GoogleAuthProvider;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;

public class Login_Page extends AppCompatActivity implements NetworkChangeReceiver.NetworkListener {

    private static final int RC_SIGN_IN = 9001;
    private static final long LOGIN_TIMEOUT_MS = 30000;
    private GoogleSignInClient mGoogleSignInClient;
    private FirebaseAuth mAuth;
    private ALodingDialog alodingDialog;
    private Handler handler;
    private Runnable timeoutRunnable;

    private EditText loginEmail;
    private EditText loginPassword;
    private Button loginButton;
    private TextView createAccount, reset_pass;
    private CheckBox showPasswordCheckBox;

    private NetworkChangeReceiver networkChangeReceiver;
    private AlertDialog networkDialog;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login_page);

        getWindow().setStatusBarColor(ContextCompat.getColor(Login_Page.this, R.color.adminHome));

        loginEmail = findViewById(R.id.login_email);
        loginPassword = findViewById(R.id.login_password);
        loginButton = findViewById(R.id.login_button);
        createAccount = findViewById(R.id.create_account);
        showPasswordCheckBox = findViewById(R.id.login_password_show_hide);

        reset_pass = findViewById(R.id.forgot_password);
        reset_pass.setOnClickListener(v -> {
            Intent intent = new Intent(Login_Page.this, PasswordReSet.class);
            startActivity(intent);
        });

        mAuth = FirebaseAuth.getInstance();

        // Configure Google Sign-In
        GoogleSignInOptions gso = new GoogleSignInOptions.Builder(GoogleSignInOptions.DEFAULT_SIGN_IN)
                .requestIdToken(getString(R.string.default_web_client_id))
                .requestEmail()
                .build();

        mGoogleSignInClient = GoogleSignIn.getClient(this, gso);

        // Initialize ALodingDialog
        initializeLoadingDialog();

        loginButton.setOnClickListener(v -> loginWithEmailPassword());

        createAccount.setOnClickListener(v -> {
            Intent intent = new Intent(Login_Page.this, Create_Account_Page.class);
            startActivity(intent);
        });

        findViewById(R.id.google_sign_in_button).setOnClickListener(view -> {
            // Sign out before sign-in to ensure account chooser is shown
            mGoogleSignInClient.signOut().addOnCompleteListener(Login_Page.this, task -> signInWithGoogle());
        });

        showPasswordCheckBox.setOnCheckedChangeListener((buttonView, isChecked) -> {
            if (isChecked) {
                loginPassword.setInputType(android.text.InputType.TYPE_TEXT_VARIATION_VISIBLE_PASSWORD);
            } else {
                loginPassword.setInputType(android.text.InputType.TYPE_CLASS_TEXT | android.text.InputType.TYPE_TEXT_VARIATION_PASSWORD);
            }
        });

        // Network receiver to monitor network changes
        networkChangeReceiver = new NetworkChangeReceiver(this);
        registerReceiver(networkChangeReceiver, new IntentFilter(ConnectivityManager.CONNECTIVITY_ACTION));

        showNetworkDialogIfNeeded(); // Check on startup if network is available

    }



    private void initializeLoadingDialog() {
        alodingDialog = new ALodingDialog(this);

        handler = new Handler(Looper.getMainLooper());
        timeoutRunnable = () -> {
            if (alodingDialog.isShowing()) {
                alodingDialog.dismiss();
                Toast.makeText(Login_Page.this, "Login timed out. Please try again.", Toast.LENGTH_SHORT).show();
            }
        };
    }

    private void signInWithGoogle() {
        alodingDialog.show();

        // Start the timeout timer
        handler.postDelayed(timeoutRunnable, LOGIN_TIMEOUT_MS);

        Intent signInIntent = mGoogleSignInClient.getSignInIntent();
        startActivityForResult(signInIntent, RC_SIGN_IN);
    }


    private void loginWithEmailPassword() {

        String email = loginEmail.getText().toString().trim();
        String password = loginPassword.getText().toString().trim();


        if(email.isEmpty()){
            loginEmail.requestFocus();
            loginEmail.setError("Email cannot be empty");
            return;
        }

        else if (!email.matches("^[a-zA-Z0-9]+([._-][a-zA-Z0-9]+)*@(gmail\\.com|outlook\\.com|yahoo\\.com)$") && !email.matches("^cse_[0-9]{16}@lus\\.ac\\.bd$")) {
            loginEmail.requestFocus();
            loginEmail.setError("Invalid email format");
            return;
        }
        // Check if the email is restricted
        else if (email.equals("nazrulislamnayon991@gmail.com")) {
            loginEmail.requestFocus();
            loginEmail.setError("Invalid email");
            return;
        }

        else if (password.isEmpty()) {
            loginPassword.requestFocus();
            loginPassword.setError("Password cannot be empty");
            return;
        }

        else if (!password.matches("^(?=.*[0-9])(?=.*[a-z])(?=.*[A-Z])(?=.*[\\W_]).{6,16}$")) {
            loginPassword.requestFocus();
            loginPassword.setError("Password must be between 6 to 16 characters long, " + "include at least one digit, one lowercase letter, " + "one uppercase letter, and one special character.");
            return;
        }


        else {
            alodingDialog.show();
            handler.postDelayed(timeoutRunnable, LOGIN_TIMEOUT_MS);

            mAuth.signInWithEmailAndPassword(email, password)
                    .addOnCompleteListener(this, task -> {
                        handler.removeCallbacks(timeoutRunnable);

                        if (task.isSuccessful()) {
                            Log.d("Login_Page", "signInWithEmail:success");
                            FirebaseUser user = mAuth.getCurrentUser();
                            updateUI(user);
                        } else {
                            Log.w("Login_Page", "signInWithEmail:failure", task.getException());
                            Toast.makeText(Login_Page.this, "Authentication Failed.", Toast.LENGTH_SHORT).show();
                            alodingDialog.dismiss();
                        }
                    });
        }

    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        if (requestCode == RC_SIGN_IN) {
            Task<GoogleSignInAccount> task = GoogleSignIn.getSignedInAccountFromIntent(data);
            try {
                GoogleSignInAccount account = task.getResult(ApiException.class);
                firebaseAuthWithGoogle(account);
            } catch (ApiException e) {
                Log.w("Login_Page", "Google sign in failed", e);
                Toast.makeText(Login_Page.this, "Google Sign-In Failed.", Toast.LENGTH_SHORT).show();
                alodingDialog.dismiss();
                handler.removeCallbacks(timeoutRunnable);
            }
        }
    }

    private void firebaseAuthWithGoogle(GoogleSignInAccount acct) {
        Log.d("Login_Page", "firebaseAuthWithGoogle:" + acct.getId());

        String email = acct.getEmail();
        if (email != null && email.equals("nazrulislamnayon991@gmail.com")) {
            Toast.makeText(Login_Page.this, "Invalid Email", Toast.LENGTH_SHORT).show();
            alodingDialog.dismiss();
            handler.removeCallbacks(timeoutRunnable);
            mGoogleSignInClient.signOut();
            return;
        }
        else{
            AuthCredential credential = GoogleAuthProvider.getCredential(acct.getIdToken(), null);
            mAuth.signInWithCredential(credential)
                    .addOnCompleteListener(this, task -> {
                        handler.removeCallbacks(timeoutRunnable);

                        if (task.isSuccessful()) {
                            Log.d("Login_Page", "signInWithCredential:success");
                            FirebaseUser user = mAuth.getCurrentUser();
                            updateUI(user);
                        } else {
                            Log.w("Login_Page", "signInWithCredential:failure", task.getException());
                            Toast.makeText(Login_Page.this, "Authentication Failed.", Toast.LENGTH_SHORT).show();
                            alodingDialog.dismiss();
                            updateUI(null);
                        }
                    });
        }
    }

    private void updateUI(FirebaseUser user) {
        if (user != null) {
            SessionManager sessionManager = new SessionManager(Login_Page.this);
            sessionManager.saveUserUID(user.getUid());

            // Check if user information already exists in the database
            DatabaseReference database = FirebaseDatabase.getInstance().getReference();
            DatabaseReference userRef = database.child("users_information").child(user.getUid());

            userRef.get().addOnCompleteListener(task -> {
                if (task.isSuccessful()) {
                    DataSnapshot dataSnapshot = task.getResult();
                    if (dataSnapshot.exists()) {
                        // User information already exists, no need to save again
                        Log.d("Login_Page", "User information already exists in the database.");
                    } else {
                        // User information does not exist, save it to the database
                        saveUserInfoToDatabase(user);
                    }

                    // After checking, proceed to the home page
                    Intent intent = new Intent(Login_Page.this, Customer_Home.class);
                    intent.putExtra("fragmentToLoad", "Home");
                    startActivity(intent);
                    finish();
                } else {
                    Log.w("Login_Page", "Failed to check user information", task.getException());
                    alodingDialog.dismiss();
                }
            });
        } else {
            alodingDialog.dismiss();
        }
    }

    private void saveUserInfoToDatabase(FirebaseUser user) {
        DatabaseReference database = FirebaseDatabase.getInstance().getReference();
        DatabaseReference userRef = database.child("users_information").child(user.getUid());

        userRef.child("email").setValue(user.getEmail());
        userRef.child("name").setValue(user.getDisplayName());
    }



    private void showNetworkDialogIfNeeded() {
        ConnectivityManager connectivityManager = (ConnectivityManager) getSystemService(CONNECTIVITY_SERVICE);
        boolean isConnected = connectivityManager.getActiveNetworkInfo() != null &&
                connectivityManager.getActiveNetworkInfo().isConnected();

        if (!isConnected) {
            showNetworkDialog();
        }
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

    @Override
    protected void onDestroy() {
        super.onDestroy();
        if (networkChangeReceiver != null) {
            unregisterReceiver(networkChangeReceiver);
        }
    }
}
