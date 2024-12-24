package com.example.petspassion;

import android.content.Intent;
import android.content.IntentFilter;
import android.net.ConnectivityManager;
import android.os.Bundle;
import android.view.MenuItem;
import android.view.View;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.content.ContextCompat;
import androidx.fragment.app.Fragment;

import com.google.android.material.bottomnavigation.BottomNavigationView;
import com.google.android.material.navigation.NavigationBarView;

import java.util.ArrayDeque;
import java.util.Deque;

public class Customer_Home extends AppCompatActivity implements NetworkChangeReceiver.NetworkListener {

    private BottomNavigationView bottomNavigationView;
    Deque<Integer> integerDeque = new ArrayDeque<>(3);
    private SessionManager sessionManager;

    private NetworkChangeReceiver networkChangeReceiver;
    private AlertDialog networkDialog;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_customer_home);

        getWindow().setStatusBarColor(ContextCompat.getColor(Customer_Home.this, R.color.adminHome));

        bottomNavigationView = findViewById(R.id.bottom_navigation);

        integerDeque.push(R.id.navigation_A_home);
        sessionManager = new SessionManager(this);

        loadFragment(new Home_Fragment());
        bottomNavigationView.setSelectedItemId(R.id.navigation_A_home);

        bottomNavigationView.setOnItemSelectedListener(new NavigationBarView.OnItemSelectedListener() {
            @Override
            public boolean onNavigationItemSelected(@NonNull MenuItem item) {
                int id = item.getItemId();

                //if ((id == R.id.navigation_Cart || id == R.id.navigation_A_profile) && !sessionManager.isLoggedIn()) {

                if (( id == R.id.navigation_A_profile) && !sessionManager.isLoggedIn()) {
                    Intent loginIntent = new Intent(Customer_Home.this, Login_Page.class);
                    loginIntent.putExtra("fragmentToLoad", id == R.id.navigation_Cart ? "Cart" : "Profile");
                    startActivity(loginIntent);
                    return false;
                }


                if (integerDeque.contains(id)) {
                    integerDeque.remove(id);
                }

                integerDeque.push(id);
                loadFragment(getFragment(item.getItemId()));
                return true;
            }
        });

        networkChangeReceiver = new NetworkChangeReceiver(this);
        registerReceiver(networkChangeReceiver, new IntentFilter(ConnectivityManager.CONNECTIVITY_ACTION));

        showNetworkDialogIfNeeded();

        String fragmentToLoad = getIntent().getStringExtra("fragmentToLoad");
        if (fragmentToLoad != null) {
            if (fragmentToLoad.equals("Cart")) {
                loadFragment(new Cart_Fragment());
            } else if (fragmentToLoad.equals("Profile")) {
                loadFragment(new Profile_Fragment());
            } // Remove Chat reference here
        }

    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        if (networkChangeReceiver != null) {
            unregisterReceiver(networkChangeReceiver);
        }
    }

    private Fragment getFragment(int itemId) {
        if (itemId == R.id.navigation_A_home) {
            bottomNavigationView.getMenu().getItem(0).setChecked(true);
            return new Home_Fragment();
        } else if (itemId == R.id.navigation_Cart) {
            bottomNavigationView.getMenu().getItem(1).setChecked(true);
            return new Cart_Fragment();
        } else { // For Profile
            bottomNavigationView.getMenu().getItem(2).setChecked(true);
            return new Profile_Fragment();
        }
    }


    private void loadFragment(Fragment fragment) {
        getSupportFragmentManager()
                .beginTransaction()
                .replace(R.id.fragment, fragment, fragment.getClass().getSimpleName())
                .commit();
    }

    @Override
    public void onBackPressed() {
        Fragment currentFragment = getSupportFragmentManager().findFragmentById(R.id.fragment);

        if (currentFragment instanceof Home_Fragment) {
            new AlertDialog.Builder(this)
                    .setTitle("Exit")
                    .setMessage("Are you sure you want to exit?")
                    .setPositiveButton("Yes", (dialog, which) -> finish())
                    .setNegativeButton("No", null)
                    .show();
        } else {
            integerDeque.pop();
            if (!integerDeque.isEmpty()) {
                loadFragment(getFragment(integerDeque.peek()));
            } else {
                super.onBackPressed();
            }
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
