package com.example.petspassion;

import android.Manifest;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.pm.PackageManager;
import android.net.ConnectivityManager;
import android.os.Bundle;
import android.os.Handler;
import android.speech.RecognizerIntent;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import androidx.swiperefreshlayout.widget.SwipeRefreshLayout;

import com.mancj.materialsearchbar.MaterialSearchBar;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.util.ArrayList;
import java.util.List;

public class Home_Fragment extends Fragment implements NetworkChangeReceiver.NetworkListener {

    private RecyclerView parentRecyclerView;
    private CustomerParentAdapter customerParentAdapter;
    private List<CustomerParentItem> customerParentItemList;
    private List<DataClass> originalProductList;

    private DatabaseReference databaseReference;
    private MaterialSearchBar searchBar;
    private TextView hintMessage;
    private SwipeRefreshLayout swipeRefreshLayout;
    private ALodingDialog aLodingDialog;

    private static final int REQUEST_CODE_SPEECH_INPUT = 100;

    private NetworkChangeReceiver networkChangeReceiver;
    private SearchSuggestionDBHelper dbHelper;


    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View rootView = inflater.inflate(R.layout.fragment_home_, container, false);

        swipeRefreshLayout = rootView.findViewById(R.id.swipe_refresh_layout);
        parentRecyclerView = rootView.findViewById(R.id.recycler_view2);
        parentRecyclerView.setLayoutManager(new LinearLayoutManager(getContext()));

        aLodingDialog = new ALodingDialog(getContext());

        // Initialize database helper for search suggestion
        dbHelper = new SearchSuggestionDBHelper(getContext());


        customerParentItemList = new ArrayList<>();
        customerParentAdapter = new CustomerParentAdapter(getContext(), customerParentItemList);
        parentRecyclerView.setAdapter(customerParentAdapter);

        searchBar = rootView.findViewById(R.id.searchBar);
        hintMessage = rootView.findViewById(R.id.no_products_found);

        databaseReference = FirebaseDatabase.getInstance().getReference();

        // Load saved suggestions from the sql database
        updateSearchSuggestions();

        // Set up search bar listener
        searchBar.setOnSearchActionListener(new MaterialSearchBar.OnSearchActionListener() {
            @Override
            public void onSearchStateChanged(boolean enabled) {
                if (!enabled) {
                    restoreOriginalProductList();
                }
            }

            @Override
            public void onSearchConfirmed(CharSequence text) {
                if (!TextUtils.isEmpty(text)) {
                    String query = text.toString();

                    // Save search query to sql database
                    dbHelper.addSuggestion(query);

                    // Update the search bar's suggestions
                    updateSearchSuggestions();

                    searchProducts(query);
                }
            }

            @Override
            public void onButtonClicked(int buttonCode) {
                if (buttonCode == MaterialSearchBar.BUTTON_SPEECH) {
                    if (ContextCompat.checkSelfPermission(getContext(), Manifest.permission.RECORD_AUDIO) != PackageManager.PERMISSION_GRANTED) {
                        ActivityCompat.requestPermissions(getActivity(), new String[]{Manifest.permission.RECORD_AUDIO}, REQUEST_CODE_SPEECH_INPUT);
                    } else {
                        startSpeechToText();
                    }
                }
            }
        });

        swipeRefreshLayout.setOnRefreshListener(this::fetchCategoriesAndProducts);

        // Fetch categories and products from Firebase
        fetchCategoriesAndProducts();

        // Initialize and register the network change receiver
        initializeNetworkChangeReceiver();

        return rootView;
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
        if (requestCode == REQUEST_CODE_SPEECH_INPUT && resultCode == getActivity().RESULT_OK) {
            ArrayList<String> results = data.getStringArrayListExtra(RecognizerIntent.EXTRA_RESULTS);
            if (results != null && !results.isEmpty()) {
                String voiceInput = results.get(0);
                searchBar.setText(voiceInput);

                // Save the voice input to the sql database as a search suggestion
                dbHelper.addSuggestion(voiceInput);

                // Update the search bar's suggestions
                updateSearchSuggestions();

                // Perform the search
                searchProducts(voiceInput);
            }
        }
    }


    private void fetchCategoriesAndProducts() {
        // Check network status before fetching data
        if (!NetworkUtils.isNetworkAvailable(getContext())) {
            aLodingDialog.dismiss();
            swipeRefreshLayout.setRefreshing(false);
            return;
        }

        // Show the custom loading dialog
        aLodingDialog.show();


        databaseReference.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                customerParentItemList.clear();

                List<DataClass> categoryList = new ArrayList<>();
                for (DataSnapshot categorySnapshot : snapshot.child("Categories").getChildren()) {
                    DataClass category = categorySnapshot.getValue(DataClass.class);
                    categoryList.add(category);
                }

                CustomerParentItem categoryCustomerParentItem = new CustomerParentItem("Categories", categoryList);
                customerParentItemList.add(categoryCustomerParentItem);

                List<DataClass> productList = new ArrayList<>();
                for (DataSnapshot productSnapshot : snapshot.child("Products").getChildren()) {
                    DataClass product = productSnapshot.getValue(DataClass.class);
                    productList.add(product);
                }

                originalProductList = new ArrayList<>(productList);

                CustomerParentItem productCustomerParentItem = new CustomerParentItem("Products", productList);
                customerParentItemList.add(productCustomerParentItem);

                customerParentAdapter.notifyDataSetChanged();

                new Handler().postDelayed(() -> {
                    // Dismiss the loading dialog when data is fetched
                    aLodingDialog.dismiss();
                    swipeRefreshLayout.setRefreshing(false);
                }, 100);
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {
                // Dismiss the loading dialog if there is an error
                aLodingDialog.dismiss();
                new Handler().postDelayed(() -> swipeRefreshLayout.setRefreshing(false), 100);
            }
        });
    }

    private void searchProducts(String query) {
        List<DataClass> filteredList = new ArrayList<>();
        String nameQuery = query.toLowerCase().trim();

        try {
            double maxPrice = Double.parseDouble(nameQuery);  // Check if query is a price

            for (DataClass product : originalProductList) {
                double productPrice = Double.parseDouble(product.getProduct_price());

                if (productPrice <= maxPrice) {
                    filteredList.add(product);
                }
            }
        } catch (NumberFormatException e) {  // If query is not a price, search by name
            for (DataClass product : originalProductList) {
                String productName = product.getProduct_name() != null ? product.getProduct_name().toLowerCase() : "";

                if (productName.contains(nameQuery)) {
                    filteredList.add(product);
                }
            }
        }

        // Update the UI with filtered products
        updateFilteredList(filteredList);
    }


    private void updateFilteredList(List<DataClass> filteredList) {
        if (filteredList.isEmpty()) {
            // If no products found, hide the categories and products lists
            hintMessage.setVisibility(View.VISIBLE);  // Show "no products found" message

            // Clear both categories and products
            customerParentItemList.clear();
            customerParentAdapter.notifyDataSetChanged();
        } else {
            // If products are found, hide categories and show only matching products
            hintMessage.setVisibility(View.GONE);  // Hide "no products found" message

            customerParentItemList.clear();  // Clear the list to remove categories

            // Create a new item with the filtered products
            CustomerParentItem productCustomerParentItem = new CustomerParentItem("Search Results", filteredList);
            customerParentItemList.add(productCustomerParentItem);

            // Update the adapter with the filtered product list
            customerParentAdapter.notifyDataSetChanged();
        }
    }


    private void restoreOriginalProductList() {
        if (originalProductList != null) {
            // Show both categories and products lists
            hintMessage.setVisibility(View.GONE);  // Hide "no products found" message

            // Clear current items
            customerParentItemList.clear();

            // Fetch categories again from Firebase snapshot
            databaseReference.child("Categories").addListenerForSingleValueEvent(new ValueEventListener() {
                @Override
                public void onDataChange(@NonNull DataSnapshot snapshot) {
                    List<DataClass> categoryList = new ArrayList<>();
                    for (DataSnapshot categorySnapshot : snapshot.getChildren()) {
                        DataClass category = categorySnapshot.getValue(DataClass.class);
                        if (category != null) {
                            categoryList.add(category);
                        }
                    }

                    CustomerParentItem categoryCustomerParentItem = new CustomerParentItem("Categories", categoryList);
                    customerParentItemList.add(categoryCustomerParentItem);

                    // Add original products list
                    CustomerParentItem productCustomerParentItem = new CustomerParentItem("Products", originalProductList);
                    customerParentItemList.add(productCustomerParentItem);

                    // Update the adapter with the restored lists
                    customerParentAdapter.notifyDataSetChanged();
                }

                @Override
                public void onCancelled(@NonNull DatabaseError error) {
                    // Handle potential error here
                }
            });
        }
    }




    // Update search bar suggestions from the local database
    private void updateSearchSuggestions() {
        List<String> suggestions = dbHelper.getAllSuggestions();
        searchBar.setLastSuggestions(suggestions);
    }



    private void initializeNetworkChangeReceiver() {
        networkChangeReceiver = new NetworkChangeReceiver(this);

        // Register the receiver
        IntentFilter filter = new IntentFilter(ConnectivityManager.CONNECTIVITY_ACTION);
        getContext().registerReceiver(networkChangeReceiver, filter);
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        // Unregister the receiver when the fragment is destroyed
        if (networkChangeReceiver != null) {
            getContext().unregisterReceiver(networkChangeReceiver);
        }
    }

    // Implement NetworkChangeReceiver.NetworkListener methods
    @Override
    public void onNetworkConnected() {
        // Fetch data when network is connected
        fetchCategoriesAndProducts();
    }

    @Override
    public void onNetworkDisconnected() {

    }
}