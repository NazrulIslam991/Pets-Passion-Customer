package com.example.petspassion;

import android.os.Bundle;

import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.RecyclerView;
import androidx.swiperefreshlayout.widget.SwipeRefreshLayout;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import com.mancj.materialsearchbar.MaterialSearchBar;


public class Home_Fragment extends Fragment {
    private RecyclerView RecyclerView;
    private MaterialSearchBar searchBar;
    private TextView hintMessage;
    private SwipeRefreshLayout swipeRefreshLayout;


    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View rootView = inflater.inflate(R.layout.fragment_home_, container, false);

        swipeRefreshLayout = rootView.findViewById(R.id.swipe_refresh_layout);
        searchBar = rootView.findViewById(R.id.searchBar);
        hintMessage = rootView.findViewById(R.id.no_products_found);




        // Set up search bar listener
        searchBar.setOnSearchActionListener(new MaterialSearchBar.OnSearchActionListener() {
            @Override
            public void onSearchStateChanged(boolean enabled) {
                if (!enabled) {
                    //restoreOriginalProductList();
                }
            }

            @Override
            public void onSearchConfirmed(CharSequence text) {

            }

            @Override
            public void onButtonClicked(int buttonCode) {

            }
        });


        return rootView;
    }
}