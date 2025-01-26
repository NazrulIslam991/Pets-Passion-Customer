package com.example.petspassion;

import android.content.Intent;
import android.content.IntentFilter;
import android.graphics.Paint;
import android.net.ConnectivityManager;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.content.ContextCompat;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.squareup.picasso.Picasso;

public class ProductDetailsCustomers extends AppCompatActivity implements NetworkChangeReceiver.NetworkListener {

    private ImageView productImage;
    private TextView productName, productDescription, productPrice, productDiscount;
    private TextView productOriginalPrice;
    private Button buy, cart;

    private ImageView return_c_home;
    private SessionManager sessionManager;

    private FirebaseAuth mAuth;
    private DatabaseReference cartDatabaseRef;
    private NetworkChangeReceiver networkChangeReceiver;
    private AlertDialog networkDialog;
    private ALodingDialog loadingDialog;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_product_details_customers);

        getWindow().setStatusBarColor(ContextCompat.getColor(ProductDetailsCustomers.this, R.color.adminHome));

        productImage = findViewById(R.id.product_image);
        productName = findViewById(R.id.product_name);
        productDescription = findViewById(R.id.product_description);
        productPrice = findViewById(R.id.product_price);
        productOriginalPrice = findViewById(R.id.product_original_price);
        productDiscount = findViewById(R.id.product_discount);
        buy = findViewById(R.id.btn_buy_now);
        cart = findViewById(R.id.btn_cart);

        mAuth = FirebaseAuth.getInstance();
        cartDatabaseRef = FirebaseDatabase.getInstance().getReference("Cart");
        sessionManager = new SessionManager(this);
        loadingDialog = new ALodingDialog(this);

        Intent intent = getIntent();
        if (intent != null) {
            String name = intent.getStringExtra("product_name");
            String description = intent.getStringExtra("product_description");
            String price = intent.getStringExtra("product_price");
            String originalPrice = intent.getStringExtra("product_original_price");
            String discount = intent.getStringExtra("product_discount");
            String imageUrl = intent.getStringExtra("product_image");
            String productId = intent.getStringExtra("product_id");

            productName.setText(name);
            productDescription.setText(description);
            productPrice.setText(price);
            productDiscount.setText("-" + discount + "%");

            if (discount != null && !discount.isEmpty() && Double.parseDouble(discount) > 0) {
                productOriginalPrice.setText(originalPrice);
                productOriginalPrice.setPaintFlags(productOriginalPrice.getPaintFlags() | Paint.STRIKE_THRU_TEXT_FLAG);
                productOriginalPrice.setVisibility(View.VISIBLE);
            } else {
                productOriginalPrice.setVisibility(View.GONE);
            }

            Picasso.get().load(imageUrl).into(productImage);
        }

        buy.setOnClickListener(v -> Toast.makeText(ProductDetailsCustomers.this, "Buy", Toast.LENGTH_SHORT).show());

        cart.setOnClickListener(v -> {
            if (sessionManager.isLoggedIn()) {
                addProductToCart();
            } else {
                Intent loginIntent = new Intent(ProductDetailsCustomers.this, Login_Page.class);
                startActivity(loginIntent);
            }
        });

        // Network receiver to monitor network changes
        networkChangeReceiver = new NetworkChangeReceiver(this);
        registerReceiver(networkChangeReceiver, new IntentFilter(ConnectivityManager.CONNECTIVITY_ACTION));

        showNetworkDialogIfNeeded(); // Check on startup if network is available
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        if (networkChangeReceiver != null) {
            unregisterReceiver(networkChangeReceiver);
        }
    }

    private void addProductToCart() {
        loadingDialog.show();  // Show the loading dialog when the user clicks the add-to-cart button

        String userUid = sessionManager.getUserUID();
        if (userUid != null) {
            Intent intent = getIntent();
            if (intent != null) {
                String productId = intent.getStringExtra("product_id");

                // Check if the product is already in the user's cart
                cartDatabaseRef.child(userUid).orderByChild("product_id").equalTo(productId)
                        .addListenerForSingleValueEvent(new ValueEventListener() {
                            @Override
                            public void onDataChange(DataSnapshot dataSnapshot) {
                                if (dataSnapshot.exists()) {
                                    // Product is already in the cart
                                    Toast.makeText(ProductDetailsCustomers.this, "Product is already in your cart.", Toast.LENGTH_SHORT).show();
                                    loadingDialog.dismiss();  // Hide the dialog
                                } else {
                                    // Product is not in the cart, so add it
                                    String productKey = cartDatabaseRef.push().getKey();
                                    String name = intent.getStringExtra("product_name");
                                    String price = intent.getStringExtra("product_price");

                                    // Remove "tk" or any non-digit characters from the price
                                    String numericPrice = price.replaceAll("[^\\d.]", ""); // Keep only digits and decimal points

                                    String imageUrl = intent.getStringExtra("product_image");

                                    // Quantity and total price
                                    String quantity = "1"; // Default quantity is set to 1
                                    String totalPrice = numericPrice;  // Use the numeric price as the total initially

                                    // Create a new DataClass object with the default quantity and total price
                                    DataClass product = new DataClass(name, numericPrice, imageUrl);
                                    product.setProduct_id(productId); // Set product_id
                                    product.setQuantity(quantity); // Set quantity
                                    product.setTotle_price(totalPrice);  // Store total_price as a numeric string

                                    // Save product data to the cart in Firebase
                                    cartDatabaseRef.child(userUid).child(productKey).setValue(product)
                                            .addOnCompleteListener(task -> {
                                                if (task.isSuccessful()) {
                                                    Toast.makeText(ProductDetailsCustomers.this, "Product added to cart successfully.", Toast.LENGTH_SHORT).show();
                                                    loadingDialog.dismiss();  // Hide the dialog after adding the product
                                                    finish();
                                                } else {
                                                    Toast.makeText(ProductDetailsCustomers.this, "Failed to add product to cart.", Toast.LENGTH_SHORT).show();
                                                    loadingDialog.dismiss();  // Hide the dialog if there is a failure
                                                }
                                            });
                                }
                            }

                            @Override
                            public void onCancelled(DatabaseError databaseError) {
                                Toast.makeText(ProductDetailsCustomers.this, "Error: " + databaseError.getMessage(), Toast.LENGTH_SHORT).show();
                                loadingDialog.dismiss();  // Hide the dialog in case of cancellation
                            }
                        });
            }
        }
    }

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
