package com.example.petspassion;

import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

public class PasswordReSet extends AppCompatActivity {

    private EditText resetPass;
    private Button resetButton;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_password_re_set);

        // Initialize views
        resetPass = findViewById(R.id.resetemail);
        resetButton = findViewById(R.id.resetbutton);

        // Add any additional setup or event listeners here
        resetButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                String email = resetPass.getText().toString().trim();

                // Check if the email field is empty
                if (email.isEmpty()) {
                    resetPass.requestFocus();
                    resetPass.setError("Email cannot be empty");
                    return;
                }

                // Check if the email format is valid
                else if (!email.matches("^[a-zA-Z0-9]+([._-][a-zA-Z0-9]+)*@(gmail\\.com|outlook\\.com|yahoo\\.com)$") && !email.matches("^cse_[0-9]{16}@lus\\.ac\\.bd$")) {
                    resetPass.requestFocus();
                    resetPass.setError("Invalid email format");
                    return;
                }
                else{
                    Toast.makeText(PasswordReSet.this, "Password reset email is sent.", Toast.LENGTH_SHORT).show();
                    Intent intent = new Intent(PasswordReSet.this, Login_Page.class);
                    startActivity(intent);
                    finish();
                }
            }
        });
    }
}