package com.example.petspassion;

import android.annotation.SuppressLint;
import android.content.Context;
import android.content.Intent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.squareup.picasso.Picasso;

import java.util.List;

public class CategoryAdapter extends RecyclerView.Adapter<CategoryAdapter.CategoryViewHolder> {

    private Context context;
    private List<DataClass> categoryList;

    public CategoryAdapter(Context context, List<DataClass> categoryList) {
        this.context = context;
        this.categoryList = categoryList;
    }

    @NonNull
    @Override
    public CategoryViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(context).inflate(R.layout.item_category, parent, false);
        return new CategoryViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull CategoryViewHolder holder, int position) {
        DataClass category = categoryList.get(position);
        holder.categoriesName.setText(category.getCategory_name());
        Picasso.get().load(category.getCategory_image()).into(holder.categoriesImage);

        holder.itemView.setOnClickListener(v -> {
            Intent intent = new Intent(context, Categories_Base_CustomerProduct_Show.class);
            intent.putExtra("category_name", category.getCategory_name());
            context.startActivity(intent);
        });
    }

    @Override
    public int getItemCount() {
        return categoryList.size();
    }


    static class CategoryViewHolder extends RecyclerView.ViewHolder {
        ImageView categoriesImage;
        TextView categoriesName;

        public CategoryViewHolder(@NonNull View itemView) {
            super(itemView);
            categoriesImage = itemView.findViewById(R.id.category_image);
            categoriesName = itemView.findViewById(R.id.category_name);
        }
    }
}
