package com.abunayem.digibin;

import android.os.Bundle;
import android.widget.TextView;
import android.widget.Toast;
import android.view.View;

import androidx.appcompat.app.AppCompatActivity;
import androidx.navigation.NavController;
import androidx.navigation.Navigation;
import androidx.navigation.ui.NavigationUI;
import android.content.Intent;
import com.google.android.material.bottomnavigation.BottomNavigationView;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.FirebaseFirestoreSettings;
import com.google.firebase.firestore.Source;

public class MainActivity extends AppCompatActivity {

    BottomNavigationView bottomNavigationView;
    NavController navController;
    TextView userNameTextView;
    TextView locationTextView;

    private FirebaseAuth mAuth;
    private FirebaseFirestore firestore;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        // Initialize Firebase with persistence
        mAuth = FirebaseAuth.getInstance();
        firestore = FirebaseFirestore.getInstance();

        // Enable Firestore persistence
        FirebaseFirestoreSettings settings = new FirebaseFirestoreSettings.Builder()
                .setPersistenceEnabled(true)
                .build();
        firestore.setFirestoreSettings(settings);

        // Initialize views
        userNameTextView = findViewById(R.id.textView12);
        locationTextView = findViewById(R.id.textView13);

        // Make location TextView clickable
        locationTextView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                // Redirect to ChooseLocationActivity
                Intent intent = new Intent(MainActivity.this, ChooseLocationActivity.class);
                startActivity(intent);
            }
        });

       
        if (mAuth.getCurrentUser() != null) {
            String userId = mAuth.getCurrentUser().getUid();
            loadUserData(userId);
        } else {
            redirectToLogin();
            return;
        }

        bottomNavigationView = findViewById(R.id.bottomNavigationView);
        navController = Navigation.findNavController(this, R.id.nav_host_fragment);
        NavigationUI.setupWithNavController(bottomNavigationView, navController);
    }

    private void loadUserData(String userId) {
        // Try to get cached data first
        firestore.collection("customers").document(userId)
                .get(Source.CACHE)
                .addOnCompleteListener(cacheTask -> {
                    if (cacheTask.isSuccessful() && cacheTask.getResult().exists()) {
                        updateUI(cacheTask.getResult());
                    } else {
            
                        firestore.collection("customers").document(userId)
                                .get(Source.SERVER)
                                .addOnCompleteListener(serverTask -> {
                                    if (serverTask.isSuccessful() && serverTask.getResult().exists()) {
                                        updateUI(serverTask.getResult());
                                    } else {
                                        showDataError();
                                    }
                                });
                    }
                });
    }

    private void updateUI(DocumentSnapshot document) {
        String userName = document.getString("name");
        String location = document.getString("location");

        if (userName != null) {
            userNameTextView.setText(userName);
        } else {
            userNameTextView.setText("Name not found");
        }

        if (location != null) {
            locationTextView.setText(location);
        } else {
            locationTextView.setText("Location not found");
        }
    }

    private void showDataError() {
        userNameTextView.setText("Error loading data");
        locationTextView.setText("Try again later");
    }

    private void redirectToLogin() {
        Toast.makeText(this, "Please sign in", Toast.LENGTH_SHORT).show();
        finish();
        startActivity(new Intent(this, LoginCustomer.class));
    }
}
