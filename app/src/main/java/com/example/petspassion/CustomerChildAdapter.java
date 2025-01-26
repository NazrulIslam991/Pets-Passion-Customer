package com.example.petspassion;

import android.content.Context;
import android.content.Intent;
import android.graphics.Paint;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.squareup.picasso.Picasso;

import java.util.List;

public class CustomerChildAdapter extends RecyclerView.Adapter<CustomerChildAdapter.ChildViewHolder> {

    private Context context;
    private List<DataClass> childItemList;

    public CustomerChildAdapter(Context context, List<DataClass> childItemList) {
        this.context = context;
        this.childItemList = childItemList;
    }

    @NonNull
    @Override
    public ChildViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(context).inflate(R.layout.child_item, parent, false);
        return new ChildViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ChildViewHolder holder, int position) {
        DataClass item = childItemList.get(position);
        holder.productName.setText(item.getProduct_name());

        // Original price
        double originalPrice = Double.parseDouble(item.getProduct_price());

        // Check if there's a discount
        if (item.getProduct_discount() != null && !item.getProduct_discount().isEmpty()) {
            double discount = Double.parseDouble(item.getProduct_discount());

            if (discount > 0) {
                double discountedPrice = originalPrice - (originalPrice * discount / 100);

                // Set the discounted price
                holder.productDiscountPrice.setText(String.format("%.2f tk", discountedPrice));

                // Show the original price with strikethrough
                holder.productPrice.setText(String.format("%.2f tk", originalPrice));
                holder.productPrice.setPaintFlags(holder.productPrice.getPaintFlags() | Paint.STRIKE_THRU_TEXT_FLAG);

                // Set the discount percentage
                holder.productDiscount.setText(String.format("%s%% off ", item.getProduct_discount()));
                holder.productDiscount.setVisibility(View.VISIBLE);

                // Show the original price
                holder.productPrice.setVisibility(View.VISIBLE);
            } else {
                // No discount, show only the discounted price without original price
                holder.productDiscountPrice.setText(String.format("%.2f tk", originalPrice));
                holder.productPrice.setVisibility(View.GONE);
                holder.productDiscount.setVisibility(View.GONE);
            }
        } else {
            // If no discount, show the original price as the discounted price and hide the original price view
            holder.productDiscountPrice.setText(String.format("%.2f tk", originalPrice));
            holder.productPrice.setVisibility(View.GONE);
            holder.productDiscount.setVisibility(View.GONE);
        }

        // Convert the product quantity from String to int
        int productQuantity = Integer.parseInt(item.getProduct_quantity());

        // Check the product quantity
        if (productQuantity <= 0) {
            holder.outOfStock.setVisibility(View.VISIBLE);
        } else {
            holder.outOfStock.setVisibility(View.GONE);
        }

        // Load the product image using Picasso
        Picasso.get().load(item.getProduct_image()).into(holder.productImage);

        // Handle item click to navigate to product details
        holder.itemView.setOnClickListener(v -> {
            Intent intent = new Intent(context, ProductDetailsCustomers.class);
            intent.putExtra("product_id", item.getProduct_id()); // Pass product_id
            intent.putExtra("product_name", item.getProduct_name());
            intent.putExtra("product_price", holder.productDiscountPrice.getText().toString());
            intent.putExtra("product_description", item.getProduct_description());
            intent.putExtra("product_image", item.getProduct_image());
            intent.putExtra("product_original_price", holder.productPrice.getText().toString());
            intent.putExtra("product_discount", item.getProduct_discount());
            context.startActivity(intent);
        });
    }

    @Override
    public int getItemCount() {
        return childItemList.size();
    }

    static class ChildViewHolder extends RecyclerView.ViewHolder {
        TextView productName, productPrice, productDiscountPrice, productDiscount, outOfStock;
        ImageView productImage;

        public ChildViewHolder(@NonNull View itemView) {
            super(itemView);
            productName = itemView.findViewById(R.id.product_name);
            productPrice = itemView.findViewById(R.id.product_original_price);
            productDiscountPrice = itemView.findViewById(R.id.Discount_product_price);
            productDiscount = itemView.findViewById(R.id.product_discount);
            productImage = itemView.findViewById(R.id.product_image);
            outOfStock = itemView.findViewById(R.id.out_of_stock);
        }
    }
}
