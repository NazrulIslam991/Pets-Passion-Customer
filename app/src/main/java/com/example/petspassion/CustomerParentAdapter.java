package com.example.petspassion;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.GridLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import java.util.List;

public class CustomerParentAdapter extends RecyclerView.Adapter<CustomerParentAdapter.ParentViewHolder> {

    private Context context;
    private List<CustomerParentItem> customerParentItemList;

    public CustomerParentAdapter(Context context, List<CustomerParentItem> customerParentItemList) {
        this.context = context;
        this.customerParentItemList = customerParentItemList;
    }

    @NonNull
    @Override
    public ParentViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(context).inflate(R.layout.parent_item, parent, false);
        return new ParentViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ParentViewHolder holder, int position) {
        CustomerParentItem customerParentItem = customerParentItemList.get(position);
        holder.titleTextView.setText(customerParentItem.getTitle());

        if (customerParentItem.getTitle().equalsIgnoreCase("Categories")) {
            // Set up CategoryAdapter with horizontal grid layout (4 columns)
            CategoryAdapter categoryAdapter = new CategoryAdapter(context, customerParentItem.getChildItemList());
            GridLayoutManager gridLayoutManager = new GridLayoutManager(context, 1);
            gridLayoutManager.setOrientation(GridLayoutManager.HORIZONTAL);
            holder.childRecyclerView.setLayoutManager(gridLayoutManager);
            holder.childRecyclerView.setAdapter(categoryAdapter);
        } else {
            // Set up CustomerChildAdapter with vertical grid layout
            CustomerChildAdapter customerChildAdapter = new CustomerChildAdapter(context, customerParentItem.getChildItemList());
            holder.childRecyclerView.setLayoutManager(new GridLayoutManager(context, 2));
            holder.childRecyclerView.setAdapter(customerChildAdapter);
        }
    }

    @Override
    public int getItemCount() {
        return customerParentItemList.size();
    }

    static class ParentViewHolder extends RecyclerView.ViewHolder {
        TextView titleTextView;
        RecyclerView childRecyclerView;

        public ParentViewHolder(@NonNull View itemView) {
            super(itemView);
            titleTextView = itemView.findViewById(R.id.parent_item_title);
            childRecyclerView = itemView.findViewById(R.id.child_recycler_view);
        }
    }
}


