package com.abunayem.digibin.Fragment;

import android.content.Intent;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import androidx.fragment.app.Fragment;

import com.abunayem.digibin.feedback; // Import FeedbackActivity
import com.abunayem.digibin.devloper;
import com.abunayem.digibin.LoginCustomer;
import com.abunayem.digibin.shareapp;
import com.abunayem.digibin.support;
import com.abunayem.digibin.R; // Assuming your R file is in com.abunayem.digibin
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.FirebaseFirestore;

public class ProfileFragment extends Fragment {

    private TextView profileName, profileEmail;
    private Button buttonHome, buttonSupport, buttonDeveloper, buttonShare, buttonFeedback, buttonLogout;
    private FirebaseAuth mAuth;
    private FirebaseFirestore firestore;

    public ProfileFragment() {
        // Required empty public constructor
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        View view = inflater.inflate(R.layout.fragment_profile, container, false);

        // Initialize Firebase Auth and Firestore
        mAuth = FirebaseAuth.getInstance();
        firestore = FirebaseFirestore.getInstance();

        // Initialize the TextViews -  Updated IDs to match XML
        profileName = view.findViewById(R.id.profileTextViewUserName);
        profileEmail = view.findViewById(R.id.profileTextViewUserEmail);

        // Initialize the buttons - Updated IDs to match XML
        buttonHome = view.findViewById(R.id.profileButtonHome);
        buttonSupport = view.findViewById(R.id.profileButtonSupport);
        buttonDeveloper = view.findViewById(R.id.profileButtonDeveloper);
        buttonShare = view.findViewById(R.id.profileButtonShareApp);
        buttonFeedback = view.findViewById(R.id.feedback); // New Feedback button
        buttonLogout = view.findViewById(R.id.profileButtonLogOut);

        // Load current user profile
        FirebaseUser currentUser = mAuth.getCurrentUser();

        if (currentUser != null) {
            String userId = currentUser.getUid();
            loadUserProfile(userId);
        } else {
            Toast.makeText(getActivity(), "User not logged in", Toast.LENGTH_SHORT).show();
            // Optionally, redirect to login or disable profile functionality
        }

        // Button click listeners
        buttonHome.setOnClickListener(v -> {
            // Implement Home button functionality if needed
            // For example, navigate to a HomeActivity or switch to a HomeFragment
            Toast.makeText(getActivity(), "Home button clicked", Toast.LENGTH_SHORT).show();
            // if (getActivity() instanceof YourMainActivity) {
            //    ((YourMainActivity) getActivity()).navigateToHomeFragment();
            // }
        });

        buttonSupport.setOnClickListener(v -> {
            Intent intent = new Intent(getActivity(), support.class);
            startActivity(intent);
        });

        buttonDeveloper.setOnClickListener(v -> {
            Intent intent = new Intent(getActivity(), devloper.class);
            startActivity(intent);
        });

        buttonShare.setOnClickListener(v -> {
            Intent intent = new Intent(getActivity(), shareapp.class);
            startActivity(intent);
        });

        buttonFeedback.setOnClickListener(v -> {
            Intent intent = new Intent(getActivity(), feedback.class); // Navigate to FeedbackActivity
            startActivity(intent);
        });

        buttonLogout.setOnClickListener(v -> {
            mAuth.signOut();
            Toast.makeText(getActivity(), "Logged out successfully", Toast.LENGTH_SHORT).show();
            Intent intent = new Intent(getActivity(), LoginCustomer.class);
            intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK); // Clear back stack
            startActivity(intent);
            if (getActivity() != null) {
                getActivity().finish(); // Finish current activity hosting the fragment
            }
        });

        return view;
    }

    private void loadUserProfile(String userId) {
        // Assuming your users' data is in a "customers" collection
        // If it's in a different collection (e.g., "users"), change "customers" accordingly
        DocumentReference userRef = firestore.collection("customers").document(userId);

        userRef.get().addOnSuccessListener(documentSnapshot -> {
            if (documentSnapshot.exists()) {
                // Make sure the field names "name" and "email" match your Firestore document
                String name = documentSnapshot.getString("name");
                String email = documentSnapshot.getString("email");

                if (name != null) {
                    profileName.setText(name);
                } else {
                    profileName.setText("N/A"); // Default if name is not found
                }

                if (email != null) {
                    profileEmail.setText(email);
                } else {
                    profileEmail.setText("N/A"); // Default if email is not found
                }
            } else {
                Toast.makeText(getActivity(), "Profile data not found in database.", Toast.LENGTH_SHORT).show();
                profileName.setText("User Name");
                profileEmail.setText("user@example.com");
            }
        }).addOnFailureListener(e -> {
            Toast.makeText(getActivity(), "Failed to load profile: " + e.getMessage(), Toast.LENGTH_SHORT).show();
            profileName.setText("Error loading name");
            profileEmail.setText("Error loading email");
        });
    }
}
