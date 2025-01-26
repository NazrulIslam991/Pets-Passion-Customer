package com.example.petspassion;

import android.Manifest;
import android.annotation.SuppressLint;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.pm.PackageManager;
import android.net.ConnectivityManager;
import android.os.Bundle;
import android.os.Handler;
import android.speech.RecognizerIntent;
import android.text.TextUtils;
import android.view.View;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import androidx.recyclerview.widget.GridLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import androidx.swiperefreshlayout.widget.SwipeRefreshLayout;

import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.mancj.materialsearchbar.MaterialSearchBar;

import java.util.ArrayList;
import java.util.List;

public class Categories_Base_CustomerProduct_Show extends AppCompatActivity implements NetworkChangeReceiver.NetworkListener {

    private TextView categoryTitle, noProductsFound;
    private RecyclerView recyclerView;
    private CustomerChildAdapter adapter;
    private List<DataClass> productList;
    private DatabaseReference databaseReference;
    private MaterialSearchBar materialSearchBar;
    private List<DataClass> filteredProductList;
    private SwipeRefreshLayout swipeRefreshLayout;
    private NetworkChangeReceiver networkChangeReceiver;
    private AlertDialog networkDialog;
    private SearchSuggestionDBHelper dbHelper;


    private static final int REQUEST_CODE_SPEECH_INPUT = 100;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_categories_base_customer_product_show);

        categoryTitle = findViewById(R.id.category_title);
        recyclerView = findViewById(R.id.recycler_view2);
        materialSearchBar = findViewById(R.id.material_search_bar);
        noProductsFound = findViewById(R.id.no_products_found);
        swipeRefreshLayout = findViewById(R.id.swipe_refresh_layout);

        productList = new ArrayList<>();
        filteredProductList = new ArrayList<>();
        adapter = new CustomerChildAdapter(this, filteredProductList);

        // Set RecyclerView to use GridLayoutManager with 2 columns
        recyclerView.setLayoutManager(new GridLayoutManager(this, 2));
        recyclerView.setAdapter(adapter);

        // Initialize database helper
        dbHelper = new SearchSuggestionDBHelper(this);

        // Load saved suggestions from the database
        updateSearchSuggestions();


        getWindow().setStatusBarColor(ContextCompat.getColor(Categories_Base_CustomerProduct_Show.this, R.color.adminHome));

        // Get the category name from the intent
        String categoryName = getIntent().getStringExtra("category_name");

        // Set category title
        categoryTitle.setText(categoryName);

        // Initialize Firebase Database reference
        databaseReference = FirebaseDatabase.getInstance().getReference("Products");

        // Fetch products from Firebase
        fetchProductsByCategory(categoryName);

        // Setup MaterialSearchBar listener
        materialSearchBar.setOnSearchActionListener(new MaterialSearchBar.OnSearchActionListener() {
            @Override
            public void onSearchStateChanged(boolean enabled) {
                if (!enabled) {
                    restoreOriginalProductList();
                }
            }

            @Override
            public void onSearchConfirmed(CharSequence text) {
                if (text != null && text.length() > 0) {
                    String query = text.toString();

                    // Save search query to local database
                    dbHelper.addSuggestion(query);

                    // Update the search bar's suggestions
                    updateSearchSuggestions();

                    filterProducts(query);  // Search products based on user input
                }
            }

            @Override
            public void onButtonClicked(int buttonCode) {
                if (buttonCode == MaterialSearchBar.BUTTON_SPEECH) {
                    if (ContextCompat.checkSelfPermission(Categories_Base_CustomerProduct_Show.this, Manifest.permission.RECORD_AUDIO) != PackageManager.PERMISSION_GRANTED) {
                        ActivityCompat.requestPermissions(Categories_Base_CustomerProduct_Show.this, new String[]{Manifest.permission.RECORD_AUDIO}, REQUEST_CODE_SPEECH_INPUT);
                    } else {
                        startSpeechToText();
                    }
                }
            }
        });


        // Network receiver to monitor network changes
        networkChangeReceiver = new NetworkChangeReceiver(this);
        registerReceiver(networkChangeReceiver, new IntentFilter(ConnectivityManager.CONNECTIVITY_ACTION));

        showNetworkDialogIfNeeded(); // Check on startup if network is available

        // Setup SwipeRefreshLayout listener
        swipeRefreshLayout.setOnRefreshListener(() -> {
            fetchProductsByCategory(categoryName);

            // Hide SwipeRefreshLayout after 5 seconds
            new Handler().postDelayed(() -> swipeRefreshLayout.setRefreshing(false), 5000);
        });
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        if (networkChangeReceiver != null) {
            unregisterReceiver(networkChangeReceiver);
        }
    }

    private void fetchProductsByCategory(String categoryName) {
        databaseReference.orderByChild("product_categories").equalTo(categoryName).addValueEventListener(new ValueEventListener() {
            @SuppressLint("NotifyDataSetChanged")
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                productList.clear();
                for (DataSnapshot snapshot : dataSnapshot.getChildren()) {
                    DataClass product = snapshot.getValue(DataClass.class);
                    if (product != null) {
                        productList.add(product);
                    }
                }
                // Initially, show all products
                filteredProductList.clear();
                filteredProductList.addAll(productList);
                adapter.notifyDataSetChanged();

                // If no products found, show "No products found" message
                noProductsFound.setVisibility(productList.isEmpty() ? View.VISIBLE : View.GONE);
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {
            }
        });
    }

    @SuppressLint("NotifyDataSetChanged")
    private void filterProducts(String query) {
        List<DataClass> filteredList = new ArrayList<>();
        String nameQuery = query.toLowerCase().trim();

        try {
            double maxPrice = Double.parseDouble(nameQuery);

            for (DataClass product : productList) {
                double productPrice = Double.parseDouble(product.getProduct_price());

                if (productPrice <= maxPrice) {
                    filteredList.add(product);
                }
            }
        } catch (NumberFormatException e) {
            for (DataClass product : productList) {
                String productName = product.getProduct_name() != null ? product.getProduct_name().toLowerCase() : "";

                if (productName.contains(nameQuery)) {
                    filteredList.add(product);
                }
            }
        }

        // Update the filtered list and notify adapter
        filteredProductList.clear();
        filteredProductList.addAll(filteredList);
        adapter.notifyDataSetChanged();

        // Show "No products found" message if filtered list is empty
        noProductsFound.setVisibility(filteredProductList.isEmpty() ? View.VISIBLE : View.GONE);
    }

    private void updateSearchSuggestions() {
        List<String> suggestions = dbHelper.getAllSuggestions();
        materialSearchBar.setLastSuggestions(suggestions);
    }


    @SuppressLint("NotifyDataSetChanged")
    private void restoreOriginalProductList() {
        // Restore the original product list
        filteredProductList.clear();
        filteredProductList.addAll(productList);
        adapter.notifyDataSetChanged();
        noProductsFound.setVisibility(productList.isEmpty() ? View.VISIBLE : View.GONE);
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

    private void startSpeechToText() {
        Intent intent = new Intent(RecognizerIntent.ACTION_RECOGNIZE_SPEECH);
        intent.putExtra(RecognizerIntent.EXTRA_LANGUAGE_MODEL, RecognizerIntent.LANGUAGE_MODEL_FREE_FORM);
        intent.putExtra(RecognizerIntent.EXTRA_PROMPT, "Speak now...");
        startActivityForResult(intent, REQUEST_CODE_SPEECH_INPUT);
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == REQUEST_CODE_SPEECH_INPUT && resultCode == RESULT_OK) {
            ArrayList<String> results = data.getStringArrayListExtra(RecognizerIntent.EXTRA_RESULTS);
            if (results != null && !results.isEmpty()) {
                String voiceInput = results.get(0);
                materialSearchBar.setText(voiceInput);

                // Save the voice input to the local database as a search suggestion
                dbHelper.addSuggestion(voiceInput);

                // Update the search bar's suggestions
                updateSearchSuggestions();

                filterProducts(voiceInput);
            }
        }
    }

}
