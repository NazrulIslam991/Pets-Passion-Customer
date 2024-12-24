package com.example.petspassion;

import androidx.appcompat.app.AppCompatActivity;
import androidx.core.content.ContextCompat;

import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;

public class MainActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        getWindow().setStatusBarColor(ContextCompat.getColor(MainActivity.this, R.color.adminHome));

        new Handler().postDelayed(() -> {
            startActivity(new Intent(MainActivity.this, Customer_Home.class));  // ................................................................

            finish();
        }, 800); // Delay for 800 milliseconds
    }
}