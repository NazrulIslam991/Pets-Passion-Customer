package com.example.petspassion;

import android.content.Intent;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;

import com.google.android.gms.auth.api.signin.GoogleSignIn;
import com.google.android.gms.auth.api.signin.GoogleSignInOptions;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

public class Profile_Fragment extends Fragment {

    private FirebaseAuth mAuth;
    private DatabaseReference mDatabase;
    private SessionManager sessionManager;

    private TextView userNameTextView, userEmailTextView, userMobileTextView, userAddressTextView;
    private Button logoutButton;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        View view = inflater.inflate(R.layout.fragment_profile_, container, false);


        // Initialize Firebase Auth
        mAuth = FirebaseAuth.getInstance();
        mDatabase = FirebaseDatabase.getInstance().getReference();
        sessionManager = new SessionManager(getContext());

        // Initialize UI elements
        userNameTextView = view.findViewById(R.id.user_name);
        userEmailTextView = view.findViewById(R.id.user_email);
        userMobileTextView = view.findViewById(R.id.user_mobile);
        userAddressTextView = view.findViewById(R.id.user_address);
        logoutButton = view.findViewById(R.id.logout);

        // Fetch and display user information
        fetchUserInfo();

        // Handle logout button click
        logoutButton.setOnClickListener(view1 -> logout());

        return view;
    }

    private void fetchUserInfo() {
        String userId = mAuth.getCurrentUser().getUid();

        mDatabase.child("users_information").child(userId).addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                if (dataSnapshot.exists()) {
                    User user = dataSnapshot.getValue(User.class);
                    if (user != null) {
                        userNameTextView.setText(user.getName());
                        userEmailTextView.setText(user.getEmail());
                        userMobileTextView.setText(user.getphone());
                        userAddressTextView.setText(user.getAddress());
                    }
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {
                Toast.makeText(getContext(), "Failed to load user info.", Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void logout() {
        mAuth.signOut();
        sessionManager.logout();
        Toast.makeText(getContext(), "Logged out successfully", Toast.LENGTH_SHORT).show();

        // Clear Google Sign-In cache to show account chooser dialog again
        GoogleSignIn.getClient(getContext(), GoogleSignInOptions.DEFAULT_SIGN_IN).signOut();

        // Start the Login_Page activity and clear all activities from the stack
        Intent intent = new Intent(getActivity(), MainActivity.class);
        intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
        startActivity(intent);

        // Optionally, finish the current activity (not necessary if using FLAG_ACTIVITY_NEW_TASK)
        if (getActivity() != null) {
            getActivity().finish();
        }
    }



}
