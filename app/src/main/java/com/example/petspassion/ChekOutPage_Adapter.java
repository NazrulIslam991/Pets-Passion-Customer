package com.example.petspassion;

import android.annotation.SuppressLint;
import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.squareup.picasso.Picasso;

import java.util.ArrayList;

public class ChekOutPage_Adapter extends RecyclerView.Adapter<ChekOutPage_Adapter.ViewHolder> {

    private Context context;
    private ArrayList<CartItem> cartItems;

    public ChekOutPage_Adapter(Context context, ArrayList<CartItem> cartItems) {
        this.context = context;
        this.cartItems = cartItems;
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(context).inflate(R.layout.checkout_item, parent, false);
        return new ViewHolder(view);
    }

    @SuppressLint("SetTextI18n")
    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        CartItem cartItem = cartItems.get(position);

        holder.checkoutTitle.setText(cartItem.getName());
        holder.productQuantity.setText("Quantity: " + cartItem.getQuantity());
        holder.productTotalPrice.setText("Price: " + cartItem.getTotle_price());

        // Load image using Picasso
        Picasso.get().load(cartItem.getImageUrl()).into(holder.cartImage);
    }

    @Override
    public int getItemCount() {
        return cartItems.size();
    }

    public static class ViewHolder extends RecyclerView.ViewHolder {
        private ImageView cartImage;
        private TextView checkoutTitle;
        private TextView productQuantity;
        private TextView productTotalPrice;

        public ViewHolder(@NonNull View itemView) {
            super(itemView);

            cartImage = itemView.findViewById(R.id.cart_image);
            checkoutTitle = itemView.findViewById(R.id.checkout_title);
            productQuantity = itemView.findViewById(R.id.product_quantity);
            productTotalPrice = itemView.findViewById(R.id.product_total_price);
        }
    }
}
