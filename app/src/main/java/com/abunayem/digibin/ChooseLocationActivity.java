package com.abunayem.digibin;

import android.content.Intent;
import android.os.Bundle;
import android.widget.ArrayAdapter;
import android.widget.AutoCompleteTextView;
import android.widget.Button;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.DocumentReference;

public class ChooseLocationActivity extends AppCompatActivity {

    AutoCompleteTextView autoCompleteTextView;
    Button btnNext;

    private FirebaseAuth mAuth;
    private FirebaseFirestore firestore;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_choose_location);

        // Initialize AutoCompleteTextView and Button
        autoCompleteTextView = findViewById(R.id.autoCompleteTextView);
        btnNext = findViewById(R.id.button2);

        // Initialize Firebase Auth and Firestore
        mAuth = FirebaseAuth.getInstance();
        firestore = FirebaseFirestore.getInstance();

        // Define a list of locations for the dropdown
        String[] locations = {"Rajshahi", "Natore", "Bogura", "Dhaka", "Chapainawabganj", "Rangpur", "Chittagong", "Khulna", "Sylhet"};

        // Set up an ArrayAdapter
        ArrayAdapter<String> adapter = new ArrayAdapter<>(this, android.R.layout.simple_dropdown_item_1line, locations);

        // Set the adapter to AutoCompleteTextView
        autoCompleteTextView.setAdapter(adapter);

        // Set OnClickListener for the Next button
        btnNext.setOnClickListener(v -> {
            String selectedLocation = autoCompleteTextView.getText().toString();

            if (!selectedLocation.isEmpty()) {
                // Get the current user's UID
                String userId = mAuth.getCurrentUser().getUid();

                // Save location under the current user's document in Firestore
                DocumentReference userRef = firestore.collection("customers").document(userId);
                userRef.update("location", selectedLocation)
                        .addOnCompleteListener(task -> {
                            if (task.isSuccessful()) {
                                Toast.makeText(ChooseLocationActivity.this, "Location saved!", Toast.LENGTH_SHORT).show();
                            } else {
                                Toast.makeText(ChooseLocationActivity.this, "Failed to save location.", Toast.LENGTH_SHORT).show();
                            }
                        });

                // Pass the selected location to MainActivity.java
                Intent intent = new Intent(ChooseLocationActivity.this, MainActivity.class);
                intent.putExtra("selectedLocation", selectedLocation); // Pass location to next activity
                startActivity(intent);

                // Optionally, finish this activity to prevent navigating back to it
                finish();
            } else {
                Toast.makeText(ChooseLocationActivity.this, "Please select a location", Toast.LENGTH_SHORT).show();
            }
        });
    }
}
