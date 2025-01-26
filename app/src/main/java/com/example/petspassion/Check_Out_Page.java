package com.example.petspassion;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.content.ContextCompat;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.annotation.SuppressLint;
import android.os.Bundle;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;
import java.util.Objects;

public class Check_Out_Page extends AppCompatActivity {

    private ImageView expandShippingIcon;
    private LinearLayout shippingInfoSection;

    private RecyclerView orderRecyclerView;
    private ChekOutPage_Adapter checkoutAdapter;
    private ArrayList<CartItem> cartItemList;
    private DatabaseReference cartDatabaseRef;
    private FirebaseAuth mAuth;
    private Spinner regionSpinner, districtSpinner, upazilaSpinner;

    private Map<String, String[]> districtsMap = new HashMap<>();
    private Map<String, String[]> upazilasMap = new HashMap<>();

    private TextView subtotalPrice, deliveryPrice, taxPrice, totalPrice, grantTotal;
    private static final double DELIVERY_COST = 120.0;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_check_out_page);
        getWindow().setStatusBarColor(ContextCompat.getColor(Check_Out_Page.this, R.color.adminHome));

        orderRecyclerView = findViewById(R.id.order_recycleview);
        subtotalPrice = findViewById(R.id.Subtotal_price);
        deliveryPrice = findViewById(R.id.Delivery_price);
        taxPrice = findViewById(R.id.Tax_price);
        totalPrice = findViewById(R.id.Total_price);
        grantTotal = findViewById(R.id.grant_total);

        mAuth = FirebaseAuth.getInstance();
        cartDatabaseRef = FirebaseDatabase.getInstance().getReference("Cart");

        cartItemList = new ArrayList<>();
        checkoutAdapter = new ChekOutPage_Adapter(this, cartItemList);
        orderRecyclerView.setLayoutManager(new LinearLayoutManager(this));
        orderRecyclerView.setAdapter(checkoutAdapter);

        // Fetch Cart Information
        fetchCartItems();

        expandShippingIcon = findViewById(R.id.expand_shipping);
        shippingInfoSection = findViewById(R.id.expand_shipping_information);

        // Set click listeners to expand/collapse sections
        setupExpandCollapse(expandShippingIcon, shippingInfoSection);
        regionSpinner = findViewById(R.id.region_spinner);
        districtSpinner = findViewById(R.id.district_spinner);


        upazilaSpinner = findViewById(R.id.upazila_spinner);

        // Initialize districts and upazilas data
        initializeData();

        // Set Region Spinner
        String[] regions = {"Sylhet"};
        ArrayAdapter<String> regionAdapter = new ArrayAdapter<>(this, android.R.layout.simple_spinner_item, regions);
        regionAdapter.setDropDownViewResource(android.R.layout.select_dialog_singlechoice);
        regionSpinner.setAdapter(regionAdapter);

        // Set listener for Region Spinner
        regionSpinner.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                String selectedRegion = regions[position];
                updateDistrictSpinner(selectedRegion);
            }

            @Override
            public void onNothingSelected(AdapterView<?> parent) {}
        });

        // Set listener for District Spinner
        districtSpinner.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                String selectedDistrict = (String) districtSpinner.getSelectedItem();
                updateUpazilaSpinner(selectedDistrict);
            }

            @Override
            public void onNothingSelected(AdapterView<?> parent) {}
        });
    }

    private void fetchCartItems() {
        String userUid = Objects.requireNonNull(mAuth.getCurrentUser()).getUid();
        cartDatabaseRef.child(userUid).addValueEventListener(new ValueEventListener() {
            @SuppressLint("NotifyDataSetChanged")
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                cartItemList.clear();
                double subtotal = 0.0;

                if (snapshot.exists()) {
                    for (DataSnapshot productSnapshot : snapshot.getChildren()) {
                        CartItem product = productSnapshot.getValue(CartItem.class);
                        if (product != null) {
                            cartItemList.add(product);
                            try {
                                double productPrice = Double.parseDouble(product.getTotle_price());
                                subtotal += productPrice;
                            } catch (NumberFormatException e) {
                                Toast.makeText(getApplicationContext(), "Invalid price format", Toast.LENGTH_SHORT).show();
                            }
                        }
                    }
                    checkoutAdapter.notifyDataSetChanged();

                    // Calculate total price, tax, and grand total
                    updatePrices(subtotal);

                } else {
                    Toast.makeText(getApplicationContext(), "No items in cart.", Toast.LENGTH_SHORT).show();
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {
                Toast.makeText(getApplicationContext(), "Failed to load cart items.", Toast.LENGTH_SHORT).show();
            }
        });
    }

    // Method to calculate and set prices
    private void updatePrices(double subtotal) {
        // Delivery cost is a constant
        double tax = subtotal * 0.02;
        double totalPriceAmount = subtotal + DELIVERY_COST + tax; // Calculate the grand total

        // Set calculated values to TextViews
        subtotalPrice.setText(String.format("%.2f", subtotal)+" tk");
        deliveryPrice.setText(String.format("%.2f", DELIVERY_COST)+" tk");
        taxPrice.setText(String.format("%.2f", tax)+" tk");
        totalPrice.setText(String.format("%.2f", totalPriceAmount)+" tk");
        grantTotal.setText(String.format("%.2f", totalPriceAmount)  +" tk" );
    }

    // Helper method to toggle visibility
    private void setupExpandCollapse(final ImageView icon, final LinearLayout section) {
        icon.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (section.getVisibility() == View.GONE) {
                    section.setVisibility(View.VISIBLE); // Expand section
                    icon.setImageResource(R.drawable.up_icon); // Change icon to up arrow
                } else {
                    section.setVisibility(View.GONE); // Collapse section
                    icon.setImageResource(R.drawable.down); // Change icon to down arrow
                }
            }
        });
    }


    // Initialize data for districts and upazilas
    private void initializeData() {
        // Districts in Sylhet Region
        districtsMap.put("Sylhet", new String[]{"Sylhet", "Sunamganj", "Moulvibazar", "Habiganj"});

        // Upazilas for Sylhet District
        upazilasMap.put("Sylhet", new String[]{"Balaganj", "Beanibazar", "Bishwanath", "Companiganj", "Dakshin Surma", "Fenchuganj", "Golapganj", "Gowainghat", "Jaintiapur", "Kanaighat", "Osmani Nagar", "Sylhet Sadar", "Zakiganj"});
        upazilasMap.put("Sunamganj", new String[]{"Bishwamvarpur", "Chhatak", "Shantiganj", "Derai", "Dharamapasha", "Dowarabazar", "Jagannathpur", "Jamalganj", "Sullah", "Sunamganj Sadar", "Tahirpur", "Madhyanagar"});
        upazilasMap.put("Moulvibazar", new String[]{"Barlekha", "Kamalganj", "Kulaura", "Moulvibazar Sadar", "Rajnagar", "Sreemangal", "Juri"});
        upazilasMap.put("Habiganj", new String[]{"Ajmiriganj", "Baniachong", "Bahubal", "Chunarughat", "Habiganj Sadar", "Lakhai", "Madhabpur", "Nabiganj", "Shaistaganj"});
    }

    // Update District Spinner based on selected region
    private void updateDistrictSpinner(String region) {
        String[] districts = districtsMap.get(region);
        ArrayAdapter<String> districtAdapter = new ArrayAdapter<>(this, android.R.layout.simple_spinner_item, districts);
        districtAdapter.setDropDownViewResource(android.R.layout.select_dialog_singlechoice);
        districtSpinner.setAdapter(districtAdapter);
    }

    // Update Upazila Spinner based on selected district
    private void updateUpazilaSpinner(String district) {
        String[] upazilas = upazilasMap.get(district);
        ArrayAdapter<String> upazilaAdapter = new ArrayAdapter<>(this, android.R.layout.simple_spinner_item, upazilas);
        upazilaAdapter.setDropDownViewResource(android.R.layout.select_dialog_singlechoice);
        upazilaSpinner.setAdapter(upazilaAdapter);
    }
}
