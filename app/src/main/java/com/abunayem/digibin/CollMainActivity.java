package com.abunayem.digibin;

import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.DocumentSnapshot;

public class CollMainActivity extends AppCompatActivity {

    // Request code for starting CollLocationActivity
    private static final int LOCATION_SELECTION_REQUEST_CODE = 1;

    private ImageView profileIcon, logoutIcon;
    private ImageView collectionRequestIcon, collectionBox, profileImage;
    private TextView collectionRequestText;
    private TextView nameTextView, locationTextView;

    private FirebaseAuth mAuth;
    private FirebaseFirestore firestore;

    private LoadingDialog loadingDialog;  // Loading dialog for logout

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_coll_main);

        // Initialize Firebase Auth and Firestore
        mAuth = FirebaseAuth.getInstance();
        firestore = FirebaseFirestore.getInstance();

        // Initialize LoadingDialog
        loadingDialog = new LoadingDialog(this);

        // Check network status
        if (!NetworkUtils.isNetworkAvailable(this)) {
            Toast.makeText(this,
                    "Offline mode - some features may be limited",
                    Toast.LENGTH_LONG).show();
        }

        // Initialize views
        profileIcon = findViewById(R.id.imageView103);
        logoutIcon = findViewById(R.id.imageView1055);
        collectionRequestIcon = findViewById(R.id.imageView1053);
        collectionRequestText = findViewById(R.id.textView26858);
        collectionBox = findViewById(R.id.imageView93);
        profileImage = findViewById(R.id.imageView95);
        nameTextView = findViewById(R.id.textView18);
        locationTextView = findViewById(R.id.textView25);

        // Set click listeners
        profileIcon.setOnClickListener(v -> {
            startActivity(new Intent(this, collprofile.class));
        });

        profileImage.setOnClickListener(v -> {
            startActivity(new Intent(this, collprofile.class));
        });

        // Logout with loading dialog
        logoutIcon.setOnClickListener(v -> {
            loadingDialog.show();  // Show loading

            FirebaseAuth.getInstance().signOut();

            Toast.makeText(this, "Logged out", Toast.LENGTH_SHORT).show();

            startActivity(new Intent(this, LoginColl.class));

            // Delay dismiss and finish to let loading show briefly
            new Handler().postDelayed(() -> {
                loadingDialog.dismiss();
                finish();
            }, 500);
        });

        // Collection request click listeners
        View.OnClickListener collectionRequestListener = v -> {
            if (NetworkUtils.isNetworkAvailable(this)) {
                startActivity(new Intent(this, Status_Collector.class));
            } else {
                Toast.makeText(this,
                        "This feature requires internet connection",
                        Toast.LENGTH_LONG).show();
            }
        };

        collectionRequestIcon.setOnClickListener(collectionRequestListener);
        collectionRequestText.setOnClickListener(collectionRequestListener);
        collectionBox.setOnClickListener(collectionRequestListener);

        // Location selection
        locationTextView.setOnClickListener(v -> {
            Intent intent = new Intent(CollMainActivity.this, CollLocationActivity.class);
            startActivityForResult(intent, LOCATION_SELECTION_REQUEST_CODE);
        });

        // Fetch and display collector data
        fetchCollectorData();
    }

    @Override
    protected void onResume() {
        super.onResume();
        fetchCollectorData();
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        if (requestCode == LOCATION_SELECTION_REQUEST_CODE && resultCode == RESULT_OK && data != null) {
            String selectedLocation = data.getStringExtra("selectedLocation");
            if (selectedLocation != null) {
                locationTextView.setText(selectedLocation);
                Toast.makeText(this, "Location updated: " + selectedLocation, Toast.LENGTH_SHORT).show();
            }
        }
    }

    private void fetchCollectorData() {
        String collectorId = getCurrentCollectorId();
        if (collectorId != null) {
            firestore.collection("collector").document(collectorId)
                    .get()
                    .addOnSuccessListener(documentSnapshot -> {
                        if (documentSnapshot.exists()) {
                            String name = documentSnapshot.getString("name");
                            String location = documentSnapshot.getString("location");

                            if (name != null && !name.isEmpty()) {
                                nameTextView.setText(name);
                            } else {
                                nameTextView.setText("Collector Name");
                            }

                            if (location != null && !location.isEmpty()) {
                                locationTextView.setText(location);
                            } else {
                                locationTextView.setText("Select Location");
                            }
                        } else {
                            Toast.makeText(CollMainActivity.this, "Collector data not found.", Toast.LENGTH_SHORT).show();
                            nameTextView.setText("Collector Name");
                            locationTextView.setText("Select Location");
                        }
                    })
                    .addOnFailureListener(e -> {
                        Toast.makeText(CollMainActivity.this, "Error fetching collector data: " + e.getMessage(), Toast.LENGTH_SHORT).show();
                        nameTextView.setText("Error");
                        locationTextView.setText("Error");
                    });
        } else {
            nameTextView.setText("Logged Out");
            locationTextView.setText("Select Location");
        }
    }

    private String getCurrentCollectorId() {
        if (mAuth.getCurrentUser() != null) {
            return mAuth.getCurrentUser().getUid();
        }
        return null;
    }
}
