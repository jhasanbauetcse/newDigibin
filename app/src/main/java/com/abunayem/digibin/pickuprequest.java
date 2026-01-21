package com.abunayem.digibin;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.AutoCompleteTextView;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.google.android.material.textfield.TextInputLayout;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.FirebaseFirestore;

public class pickuprequest extends AppCompatActivity {

    private EditText phoneInput, addressInput;
    private AutoCompleteTextView scheduleAutoCompleteText, wasteTypeAutoCompleteText;
    private Button submitButton;

    private FirebaseAuth mAuth;
    private FirebaseFirestore firestore;
    private LoadingDialog loadingDialog;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_pickuprequest);

        // Initialize Firebase Auth and Firestore
        mAuth = FirebaseAuth.getInstance();
        firestore = FirebaseFirestore.getInstance();
        loadingDialog = new LoadingDialog(this);

        // Initialize views
        phoneInput = findViewById(R.id.phone_input);
        addressInput = findViewById(R.id.address_input);
        submitButton = findViewById(R.id.submit_button);

        // Setup dropdown for schedule
        TextInputLayout scheduleInputLayout = findViewById(R.id.schedule_input_layout);
        scheduleAutoCompleteText = (AutoCompleteTextView) scheduleInputLayout.getEditText();

        // Setup dropdown for waste type
        TextInputLayout wasteTypeInputLayout = findViewById(R.id.waste_type_input_layout);
        wasteTypeAutoCompleteText = (AutoCompleteTextView) wasteTypeInputLayout.getEditText();

        // Set up adapters for dropdown menus
        setupDropdowns();

        // Set click listener for submit button
        submitButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                submitPickupRequest();
            }
        });
    }

    private void setupDropdowns() {
        // Schedule options
        String[] scheduleOptions = new String[]{
                "06:00 AM",
                "10:00 AM",
                "02:00 PM",
                "06:00 PM",
                "08:00 PM"
        };

        ArrayAdapter<String> scheduleAdapter = new ArrayAdapter<>(
                this,
                android.R.layout.simple_dropdown_item_1line,
                scheduleOptions
        );
        scheduleAutoCompleteText.setAdapter(scheduleAdapter);

        // Waste type options
        String[] wasteTypeOptions = new String[]{
                "Plastic",
                "Paper",
                "Glass",
                "Metal",
                "Organic",
                "E-waste",
                "Other"
        };

        ArrayAdapter<String> wasteTypeAdapter = new ArrayAdapter<>(
                this,
                android.R.layout.simple_dropdown_item_1line,
                wasteTypeOptions
        );
        wasteTypeAutoCompleteText.setAdapter(wasteTypeAdapter);
    }

    private void submitPickupRequest() {
        String phone = phoneInput.getText().toString().trim();
        String address = addressInput.getText().toString().trim();
        String schedule = scheduleAutoCompleteText.getText().toString().trim();
        String wasteType = wasteTypeAutoCompleteText.getText().toString().trim();

        // Basic validation
        if (phone.isEmpty()) {
            phoneInput.setError("Phone number is required");
            phoneInput.requestFocus();
            return;
        }

        if (address.isEmpty()) {
            addressInput.setError("Address is required");
            addressInput.requestFocus();
            return;
        }

        if (schedule.isEmpty()) {
            scheduleAutoCompleteText.setError("Schedule is required");
            scheduleAutoCompleteText.requestFocus();
            return;
        }

        if (wasteType.isEmpty()) {
            wasteTypeAutoCompleteText.setError("Waste type is required");
            wasteTypeAutoCompleteText.requestFocus();
            return;
        }

        // Get current user info
        String userId = mAuth.getCurrentUser().getUid();
        String email = mAuth.getCurrentUser().getEmail();

        // Create pickup request object
        PickupRequest pickupRequest = new PickupRequest(phone, address, schedule, wasteType, email);

        // Show loading dialog
        loadingDialog.show();

        // Save to user's pickupRequests subcollection
        firestore.collection("customers")
                .document(userId)
                .collection("pickupRequests")
                .add(pickupRequest)
                .addOnCompleteListener(task -> {
                    if (task.isSuccessful()) {
                        String requestId = task.getResult().getId();

                        // Save to global pickupRequests collection
                        firestore.collection("pickupRequests")
                                .document(requestId)
                                .set(pickupRequest)
                                .addOnCompleteListener(globalTask -> {
                                    loadingDialog.dismiss();
                                    if (globalTask.isSuccessful()) {
                                        // Redirect to congratulations activity
                                        Intent intent = new Intent(pickuprequest.this, congratulation.class);
                                        intent.putExtra("REQUEST_ID", requestId);
                                        startActivity(intent);
                                        finish();
                                    } else {
                                        Toast.makeText(pickuprequest.this,
                                                "Failed to save globally. Try again!",
                                                Toast.LENGTH_SHORT).show();
                                    }
                                });
                    } else {
                        loadingDialog.dismiss();
                        Toast.makeText(pickuprequest.this,
                                "Failed to submit request. Try again!",
                                Toast.LENGTH_SHORT).show();
                    }
                });
    }

    public static class PickupRequest {
        private String phone;
        private String address;
        private String schedule;
        private String wasteType;
        private String email;
        private long timestamp;

        public PickupRequest() {
            // Required empty constructor for Firestore
        }

        public PickupRequest(String phone, String address, String schedule, String wasteType, String email) {
            this.phone = phone;
            this.address = address;
            this.schedule = schedule;
            this.wasteType = wasteType;
            this.email = email;
            this.timestamp = System.currentTimeMillis();
        }

        public String getPhone() {
            return phone;
        }

        public String getAddress() {
            return address;
        }

        public String getSchedule() {
            return schedule;
        }

        public String getWasteType() {
            return wasteType;
        }

        public String getEmail() {
            return email;
        }

        public long getTimestamp() {
            return timestamp;
        }
    }
}