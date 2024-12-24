package com.example.petspassion;

import androidx.appcompat.app.AppCompatActivity;
import androidx.core.content.ContextCompat;

import android.os.Bundle;

public class Create_Account_Page extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_create_account_page);
        getWindow().setStatusBarColor(ContextCompat.getColor(Create_Account_Page.this, R.color.adminHome));

    }
}