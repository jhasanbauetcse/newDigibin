package com.abunayem.digibin;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.RelativeLayout;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.Query;

public class Status_Collector extends AppCompatActivity {

    private LinearLayout linearLayoutContainer;
    private int boxCount = 1;
    private FirebaseFirestore firestore;
    private LoadingDialog loadingDialog; // Add LoadingDialog

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.collector_status);

        firestore = FirebaseFirestore.getInstance();
        linearLayoutContainer = findViewById(R.id.linearLayoutContainer);
        loadingDialog = new LoadingDialog(this); // Initialize it

        loadingDialog.show(); // **Show loading dialog here before fetching data**
        fetchPickupRequests();
    }

    private void fetchPickupRequests() {

        String collectorId = getCurrentCollectorId();
        if (collectorId != null) {
            firestore.collection("collector").document(collectorId)
                    .collection("assignedRequests")
                    .orderBy("timestamp", Query.Direction.DESCENDING)
                    .get()
                    .addOnSuccessListener(queryDocumentSnapshots -> {
                        loadingDialog.dismiss(); // Dismiss on success
                        linearLayoutContainer.removeAllViews();
                        boxCount = 1;

                        for (DocumentSnapshot document : queryDocumentSnapshots) {
                            String requestId = document.getString("requestId");
                            String phone = document.getString("phone");
                            String address = document.getString("address");
                            String schedule = document.getString("schedule");
                            String wasteType = document.getString("wasteType");
                            String email = document.getString("customerEmail");
                            String status = document.getString("status");

                            addPickupRequestBox(requestId, phone, address, schedule, wasteType, email, status);
                        }
                    })
                    .addOnFailureListener(e -> {
                        loadingDialog.dismiss(); // Dismiss on failure
                        Toast.makeText(this, "Failed to load requests: " + e.getMessage(), Toast.LENGTH_SHORT).show();
                        e.printStackTrace();
                    });
        } else {
            loadingDialog.dismiss(); // Dismiss if collector ID not found
            Toast.makeText(this, "Collector ID not found", Toast.LENGTH_SHORT).show();
        }
    }

    private void addPickupRequestBox(String requestId, String phone, String address,
                                     String schedule, String wasteType, String email,
                                     String status) {
        RelativeLayout requestBox = new RelativeLayout(this);

        LinearLayout.LayoutParams params = new LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.MATCH_PARENT,
                LinearLayout.LayoutParams.WRAP_CONTENT  // Dynamic height to expand as needed
        );
        params.setMargins(dpToPx(16), 0, dpToPx(16), dpToPx(16));
        requestBox.setLayoutParams(params);
        requestBox.setBackgroundResource(R.drawable.box_border_green);
        requestBox.setPadding(dpToPx(12), dpToPx(12), dpToPx(12), dpToPx(12));

        // Info Text
        String requestInfo = boxCount + ". Request ID: \n\n" + requestId
                + "\nPhone: " + phone
                + "\nAddress: " + address
                + "\nSchedule: " + schedule
                + "\nWaste Type: " + wasteType
                + "\nEmail: " + email;

        TextView requestText = new TextView(this);
        requestText.setText(requestInfo);
        requestText.setTextSize(16f);
        requestText.setTextColor(getResources().getColor(android.R.color.black));
        RelativeLayout.LayoutParams textParams = new RelativeLayout.LayoutParams(
                RelativeLayout.LayoutParams.MATCH_PARENT,
                RelativeLayout.LayoutParams.WRAP_CONTENT
        );
        textParams.addRule(RelativeLayout.ALIGN_PARENT_START);
        requestText.setLayoutParams(textParams);

        requestBox.addView(requestText);

        // Details/Collected Button
        Button detailsCollectedButton = new Button(this);
        detailsCollectedButton.setTextSize(14f);
        RelativeLayout.LayoutParams buttonParams = new RelativeLayout.LayoutParams(
                RelativeLayout.LayoutParams.WRAP_CONTENT,
                RelativeLayout.LayoutParams.WRAP_CONTENT);
        buttonParams.addRule(RelativeLayout.ALIGN_PARENT_END);
        buttonParams.addRule(RelativeLayout.ALIGN_PARENT_TOP); // Align to top right
        buttonParams.setMargins(0, 0, 0, dpToPx(8));
        detailsCollectedButton.setLayoutParams(buttonParams);

        if ("Collected".equalsIgnoreCase(status)) {
            detailsCollectedButton.setText("Collected");
            detailsCollectedButton.setBackgroundColor(getResources().getColor(android.R.color.transparent));
            detailsCollectedButton.setTextColor(getResources().getColor(android.R.color.holo_green_dark));
            detailsCollectedButton.setClickable(false);
            requestBox.addView(detailsCollectedButton);
        } else {
            detailsCollectedButton.setText("Details");
            detailsCollectedButton.setBackgroundResource(R.drawable.box_green);
            detailsCollectedButton.setTextColor(getResources().getColor(android.R.color.white));
            detailsCollectedButton.setOnClickListener(v -> {
                Toast.makeText(this, "Details for Request ID: " + requestId, Toast.LENGTH_SHORT).show();
                Intent intent = new Intent(Status_Collector.this, Collector_calculate.class);
                intent.putExtra("REQUEST_ID", requestId);
                intent.putExtra("CUSTOMER_EMAIL", email);
                startActivity(intent);
            });
            requestBox.addView(detailsCollectedButton);
        }

        linearLayoutContainer.addView(requestBox);
        boxCount++;
    }

    private String getCurrentCollectorId() {
        if (FirebaseAuth.getInstance().getCurrentUser() != null) {
            return FirebaseAuth.getInstance().getCurrentUser().getUid();
        }
        return null;
    }

    private int dpToPx(int dp) {
        float density = getResources().getDisplayMetrics().density;
        return Math.round(dp * density);
    }
}
