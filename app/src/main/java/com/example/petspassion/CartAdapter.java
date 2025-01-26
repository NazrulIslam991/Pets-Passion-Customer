package com.example.petspassion;

import android.annotation.SuppressLint;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;
import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.squareup.picasso.Picasso;

import java.util.ArrayList;
import java.util.Objects;

public class CartAdapter extends RecyclerView.Adapter<CartAdapter.CartViewHolder> {

    private ArrayList<DataClass> cartProductList;
    private FirebaseAuth mAuth;
    private DatabaseReference cartDatabaseRef;
    private CartTotalListener totalListener;

    public CartAdapter(ArrayList<DataClass> cartProductList, CartTotalListener totalListener) {
        this.cartProductList = cartProductList;
        this.totalListener = totalListener;
        mAuth = FirebaseAuth.getInstance();
        cartDatabaseRef = FirebaseDatabase.getInstance().getReference("Cart");
    }

    @NonNull
    @Override
    public CartViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.cart_item, parent, false);
        return new CartViewHolder(view);
    }

    @SuppressLint("SetTextI18n")
    @Override
    public void onBindViewHolder(@NonNull CartViewHolder holder, int position) {
        DataClass product = cartProductList.get(position);

        holder.cartTitle.setText(product.getName());
        Picasso.get().load(product.getImageUrl()).into(holder.cartImage);

        // Remove any currency symbols ("tk") from the price and convert it to a double
        String priceString = product.getDiscountPrice().replaceAll("[^\\d.]", "");
        double productPrice = Double.parseDouble(priceString);

        holder.productPrice.setText(product.getDiscountPrice() + " tk");

        // Initialize quantity and set it to the view
        holder.pQuanty = Integer.parseInt(product.getQuantity());
        holder.pQuantyView.setText(String.valueOf(holder.pQuanty));

        // Calculate total price
        double totalPrice = productPrice * holder.pQuanty;

        // Set total price in the view
        holder.productTotalPrice.setText(totalPrice + " tk");

        // Handle quantity increase
        holder.plusButton.setOnClickListener(v -> {
            holder.pQuanty++;
            holder.pQuantyView.setText(String.valueOf(holder.pQuanty));

            // Recalculate the total price after increasing the quantity
            double updatedTotalPrice = productPrice * holder.pQuanty;
            holder.productTotalPrice.setText(String.valueOf(updatedTotalPrice));

            // Update the quantity and total price in Firebase
            updateQuantityAndTotalInFirebase(product.getProduct_id(), holder.pQuanty, updatedTotalPrice);

            // Update subtotal in CartFragment
            calculateCartTotal();
        });

        // Handle quantity decrease
        holder.minusButton.setOnClickListener(v -> {
            if (holder.pQuanty > 0) {
                holder.pQuanty--;
                holder.pQuantyView.setText(String.valueOf(holder.pQuanty));

                // Recalculate the total price after decreasing the quantity
                double updatedTotalPrice = productPrice * holder.pQuanty;
                holder.productTotalPrice.setText(String.valueOf(updatedTotalPrice));

                // If quantity is zero, remove the product from Firebase
                if (holder.pQuanty == 0) {
                    removeProductFromFirebase(product.getProduct_id(), position);
                } else {
                    // Update the quantity and total price in Firebase if quantity is greater than 0
                    updateQuantityAndTotalInFirebase(product.getProduct_id(), holder.pQuanty, updatedTotalPrice);
                }

                // Update subtotal in CartFragment
                calculateCartTotal();
            }
        });

        // Update subtotal in CartFragment on initial load
        calculateCartTotal();
    }

    @Override
    public int getItemCount() {
        return cartProductList.size();
    }

    private void updateQuantityAndTotalInFirebase(String productId, int newQuantity, double newTotalPrice) {
        String userUid = Objects.requireNonNull(mAuth.getCurrentUser()).getUid();
        cartDatabaseRef.child(userUid).orderByChild("product_id").equalTo(productId)
                .addListenerForSingleValueEvent(new ValueEventListener() {
                    @Override
                    public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                        for (DataSnapshot snapshot : dataSnapshot.getChildren()) {
                            String key = snapshot.getKey();
                            if (key != null) {
                                cartDatabaseRef.child(userUid).child(key).child("quantity").setValue(String.valueOf(newQuantity));
                                cartDatabaseRef.child(userUid).child(key).child("totle_price").setValue(String.valueOf(newTotalPrice));
                            }
                        }
                    }

                    @Override
                    public void onCancelled(@NonNull DatabaseError error) {
                        // Handle the error
                    }
                });
    }

    private void removeProductFromFirebase(String productId, int ignoredPosition) {
        String userUid = Objects.requireNonNull(mAuth.getCurrentUser()).getUid();
        cartDatabaseRef.child(userUid).orderByChild("product_id").equalTo(productId)
                .addListenerForSingleValueEvent(new ValueEventListener() {
                    @Override
                    public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                        for (DataSnapshot snapshot : dataSnapshot.getChildren()) {
                            String key = snapshot.getKey();
                            if (key != null) {
                                cartDatabaseRef.child(userUid).child(key).removeValue()
                                        .addOnSuccessListener(aVoid -> {
                                        })
                                        .addOnFailureListener(e -> {
                                            // Handle failure to remove
                                        });
                            }
                        }
                    }

                    @Override
                    public void onCancelled(@NonNull DatabaseError error) {
                        // Handle the error
                    }
                });
    }

    private void calculateCartTotal() {
        double subtotal = 0.0;
        for (DataClass product : cartProductList) {
            double price = Double.parseDouble(product.getDiscountPrice().replaceAll("[^\\d.]", ""));
            subtotal += price * Integer.parseInt(product.getQuantity());
        }
        totalListener.onCartTotalUpdated(subtotal);
    }

    public interface CartTotalListener {
        void onCartTotalUpdated(double subtotal);
    }

    public static class CartViewHolder extends RecyclerView.ViewHolder {
        ImageView cartImage, plusButton, minusButton;
        TextView cartTitle, productPrice, productTotalPrice, pQuantyView;
        int pQuanty;

        public CartViewHolder(@NonNull View itemView) {
            super(itemView);

            cartImage = itemView.findViewById(R.id.cart_image);
            cartTitle = itemView.findViewById(R.id.cart_title);
            productPrice = itemView.findViewById(R.id.product_price);
            productTotalPrice = itemView.findViewById(R.id.product_total_price);
            plusButton = itemView.findViewById(R.id.plus);
            minusButton = itemView.findViewById(R.id.minus);
            pQuantyView = itemView.findViewById(R.id.p_quanty);
        }
    }
}
