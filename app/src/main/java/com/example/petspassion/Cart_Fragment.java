package com.example.petspassion;

import android.annotation.SuppressLint;
import android.content.Intent;
import android.os.Bundle;
import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.util.ArrayList;
import java.util.Objects;

public class Cart_Fragment extends Fragment implements CartAdapter.CartTotalListener {

    private RecyclerView cartView;
    private TextView emptyTxt, subtotalPrice, deliveryPrice, taxPrice, totalPrice;
    private Button checkOutButtond;
    private CartAdapter cartAdapter;
    private ArrayList<DataClass> cartProductList;
    private FirebaseAuth mAuth;
    private DatabaseReference cartDatabaseRef;
    private static final double DELIVERY_COST = 120.0;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_cart_, container, false);

        // Initialize views
        cartView = view.findViewById(R.id.cartView);
        emptyTxt = view.findViewById(R.id.emptyTxt);
        subtotalPrice = view.findViewById(R.id.Subtotal_price);
        deliveryPrice = view.findViewById(R.id.Delivery_price);
        taxPrice = view.findViewById(R.id.Tax_price);
        totalPrice = view.findViewById(R.id.Total_price);
        checkOutButtond = view.findViewById(R.id.checkOutButton);

        // Initialize Firebase
        mAuth = FirebaseAuth.getInstance();
        cartDatabaseRef = FirebaseDatabase.getInstance().getReference("Cart");

        // Set up RecyclerView
        cartView.setLayoutManager(new LinearLayoutManager(getContext()));
        cartProductList = new ArrayList<>();
        cartAdapter = new CartAdapter(cartProductList, this);
        cartView.setAdapter(cartAdapter);

        // Fetch cart products
        fetchCartProducts();

        checkOutButtond.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(getActivity(), Check_Out_Page.class);
                startActivity(intent);
            }
        });


        return view;


    }

    private void fetchCartProducts() {
        String userUid = Objects.requireNonNull(mAuth.getCurrentUser()).getUid();
        cartDatabaseRef.child(userUid).addValueEventListener(new ValueEventListener() {
            @SuppressLint("NotifyDataSetChanged")
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                cartProductList.clear();
                if (snapshot.exists()) {
                    for (DataSnapshot productSnapshot : snapshot.getChildren()) {
                        DataClass product = productSnapshot.getValue(DataClass.class);
                        cartProductList.add(product);
                    }
                    cartAdapter.notifyDataSetChanged();
                    emptyTxt.setVisibility(View.GONE);
                } else {
                    // If no items in the cart, show empty message and reset all prices to 0
                    emptyTxt.setVisibility(View.VISIBLE);
                    resetPricesToZero();
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {
                Toast.makeText(getContext(), "Failed to load cart items.", Toast.LENGTH_SHORT).show();
            }
        });
    }

    @SuppressLint("SetTextI18n")
    private void resetPricesToZero() {
        subtotalPrice.setText("0.00 tk");
        deliveryPrice.setText("0.00 tk");
        taxPrice.setText("0.00 tk");
        totalPrice.setText("0.00 tk");
    }


    @SuppressLint("DefaultLocale")
    @Override
    public void onCartTotalUpdated(double subtotal) {
        double tax = subtotal * 0.02; // 2% tax
        double total = subtotal + DELIVERY_COST + tax;

        subtotalPrice.setText(String.format("%.2f tk", subtotal));
        deliveryPrice.setText(String.format("%.2f tk", DELIVERY_COST));
        taxPrice.setText(String.format("%.2f tk", tax));
        totalPrice.setText(String.format("%.2f tk", total));
    }
}
