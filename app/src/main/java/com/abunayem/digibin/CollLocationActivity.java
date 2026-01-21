package com.abunayem.digibin;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.AutoCompleteTextView;
import android.widget.Button; // Changed from AppCompatButton
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.DocumentReference; // Added for DocumentReference

public class CollLocationActivity extends AppCompatActivity { // Original name, assuming this is the one to modify

    AutoCompleteTextView autoCompleteTextView;
    Button btnNext; // Changed from AppCompatButton

    private FirebaseAuth mAuth; // Renamed from firebaseAuth
    private FirebaseFirestore firestore;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_coll_location); // Assuming this is the correct layout name

        // Initialize AutoCompleteTextView and Button
        autoCompleteTextView = findViewById(R.id.autoCompleteTextView);
        btnNext = findViewById(R.id.button2); // Assuming button ID is still button2

        // Initialize Firebase Auth and Firestore
        mAuth = FirebaseAuth.getInstance();
        firestore = FirebaseFirestore.getInstance();

        // Define a list of locations for the dropdown directly in code
        String[] locations = {"Rajshahi", "Natore", "Bogura", "Dhaka", "Chapainawabganj", "Rangpur", "Chittagong", "Khulna", "Sylhet"};

        // Set up an ArrayAdapter
        ArrayAdapter<String> adapter = new ArrayAdapter<>(this, android.R.layout.simple_dropdown_item_1line, locations);

        // Set the adapter to AutoCompleteTextView
        autoCompleteTextView.setAdapter(adapter);

        // Set OnClickListener for the Next button
        btnNext.setOnClickListener(v -> {
            String selectedLocation = autoCompleteTextView.getText().toString().trim(); // Trim whitespace

            if (!selectedLocation.isEmpty()) {
                // Get the current user's UID
                // Ensure a user is logged in before attempting to get UID
                if (mAuth.getCurrentUser() != null) {
                    String userId = mAuth.getCurrentUser().getUid();

                    // Save location under the current user's document in Firestore
                    // Assuming 'customers' collection for user data, as per your example
                    DocumentReference userRef = firestore.collection("collector").document(userId);
                    userRef.update("location", selectedLocation)
                            .addOnCompleteListener(task -> {
                                if (task.isSuccessful()) {
                                    Toast.makeText(CollLocationActivity.this, "Location saved!", Toast.LENGTH_SHORT).show();
                                    finish(); // Go back to the calling activity (CollMainActivity)
                                } else {
                                    Toast.makeText(CollLocationActivity.this, "Failed to save location: " + task.getException().getMessage(), Toast.LENGTH_SHORT).show();
                                }
                            });
                } else {
                    Toast.makeText(CollLocationActivity.this, "User not logged in.", Toast.LENGTH_SHORT).show();
                }
            } else {
                Toast.makeText(CollLocationActivity.this, "Please select a location", Toast.LENGTH_SHORT).show();
            }
        });
    }
}