package com.abunayem.digibin;

import android.content.Intent;
import android.os.Bundle;
import android.widget.Button;

import androidx.appcompat.app.AppCompatActivity;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;

public class StartActivity extends AppCompatActivity {

    Button customerButton;
    Button collectorButton;
    Button adminButton; // Declare admin button

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        // Check if user is already logged in
        FirebaseUser currentUser = FirebaseAuth.getInstance().getCurrentUser();
        if (currentUser != null) {
            // User logged in, redirect to MainActivity and finish StartActivity
            Intent intent = new Intent(StartActivity.this, MainActivity.class);
            startActivity(intent);
            finish();
            return;
        }

        // User not logged in, load the start screen layout and initialize buttons
        setContentView(R.layout.activity_start);

        // Initialize buttons
        customerButton = findViewById(R.id.customerButton);
        collectorButton = findViewById(R.id.collectorButton);
        adminButton = findViewById(R.id.adminButton);

        // Set OnClickListener for customerButton
        customerButton.setOnClickListener(v -> {
            Intent intent = new Intent(StartActivity.this, LoginCustomer.class);
            startActivity(intent);
        });

        // Set OnClickListener for collectorButton
        collectorButton.setOnClickListener(v -> {
            Intent intent = new Intent(StartActivity.this, LoginColl.class);
            startActivity(intent);
        });

        // Set OnClickListener for adminButton
        adminButton.setOnClickListener(v -> {
            Intent intent = new Intent(StartActivity.this, LoginAdmin.class);
            startActivity(intent);
        });
    }
}
