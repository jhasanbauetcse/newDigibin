package com.abunayem.digibin;

import android.os.Bundle;
import android.os.Handler;
import android.view.View;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FieldValue;
import com.google.firebase.firestore.FirebaseFirestore;

import java.util.HashMap;
import java.util.Map;

public class Admin_coll_assign extends AppCompatActivity {

    private LinearLayout linearLayoutContainer;
    private int boxCount = 1;
    private FirebaseFirestore firestore;
    private Bundle requestData;
    private LoadingDialog loadingDialog;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_collectors);

        firestore = FirebaseFirestore.getInstance();
        linearLayoutContainer = findViewById(R.id.linearLayoutContainer);
        loadingDialog = new LoadingDialog(this);

        requestData = getIntent().getExtras();

        loadingDialog.show(); // Show loading dialog before fetching
        fetchCollectors();
    }

    private void fetchCollectors() {
        firestore.collection("collector")
                .get()
                .addOnSuccessListener(queryDocumentSnapshots -> {
                    loadingDialog.dismiss();
                    linearLayoutContainer.removeAllViews();
                    boxCount = 1;

                    for (DocumentSnapshot document : queryDocumentSnapshots) {
                        String collectorId = document.getId();
                        String name = document.getString("name");
                        String email = document.getString("email");

                        addCollectorBox(collectorId, name, email);
                    }
                })
                .addOnFailureListener(e -> {
                    loadingDialog.dismiss();
                    Toast.makeText(this, "Failed to fetch collectors: " + e.getMessage(), Toast.LENGTH_SHORT).show();
                    e.printStackTrace();
                });
    }

    private void addCollectorBox(String collectorId, String name, String email) {
        RelativeLayout collectorBox = new RelativeLayout(this);

        LinearLayout.LayoutParams params = new LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.MATCH_PARENT,
                LinearLayout.LayoutParams.WRAP_CONTENT);
        params.setMargins(dpToPx(16), 0, dpToPx(16), dpToPx(16));
        collectorBox.setLayoutParams(params);
        collectorBox.setBackgroundResource(R.drawable.box_border_green);
        collectorBox.setPadding(dpToPx(12), dpToPx(12), dpToPx(12), dpToPx(12));

        Button assignButton = new Button(this);
        assignButton.setText("Assign");
        assignButton.setTextSize(14f);
        assignButton.setBackgroundResource(R.drawable.box_green);
        assignButton.setTextColor(getResources().getColor(android.R.color.white));
        assignButton.setPadding(dpToPx(8), dpToPx(4), dpToPx(8), dpToPx(4));
        assignButton.setId(View.generateViewId());

        RelativeLayout.LayoutParams buttonParams = new RelativeLayout.LayoutParams(
                RelativeLayout.LayoutParams.WRAP_CONTENT,
                RelativeLayout.LayoutParams.WRAP_CONTENT);
        buttonParams.addRule(RelativeLayout.ALIGN_PARENT_END);
        buttonParams.addRule(RelativeLayout.ALIGN_PARENT_TOP);
        assignButton.setLayoutParams(buttonParams);
        collectorBox.addView(assignButton);

        assignButton.setOnClickListener(v -> {
            if (requestData != null) {
                loadingDialog.show();
                // Optional: Delay to make sure dialog is visible
                new Handler().postDelayed(() -> assignRequestToCollector(collectorId, requestData), 300);
            } else {
                Toast.makeText(this, "No request data found", Toast.LENGTH_SHORT).show();
            }
        });

        String collectorInfo = boxCount + ". Collector ID: \n" + collectorId
                + "\nName: " + name
                + "\nEmail: " + email;

        TextView collectorText = new TextView(this);
        collectorText.setText(collectorInfo);
        collectorText.setTextSize(16f);
        collectorText.setTextColor(getResources().getColor(android.R.color.black));

        RelativeLayout.LayoutParams textParams = new RelativeLayout.LayoutParams(
                RelativeLayout.LayoutParams.MATCH_PARENT,
                RelativeLayout.LayoutParams.WRAP_CONTENT);
        textParams.addRule(RelativeLayout.ALIGN_PARENT_START);
        textParams.addRule(RelativeLayout.LEFT_OF, assignButton.getId());
        collectorText.setLayoutParams(textParams);
        collectorBox.addView(collectorText);

        linearLayoutContainer.addView(collectorBox);
        boxCount++;
    }

    private void assignRequestToCollector(String collectorId, Bundle requestData) {
        Map<String, Object> assignmentData = new HashMap<>();
        assignmentData.put("requestId", requestData.getString("REQUEST_ID"));
        assignmentData.put("phone", requestData.getString("PHONE"));
        assignmentData.put("address", requestData.getString("ADDRESS"));
        assignmentData.put("schedule", requestData.getString("SCHEDULE"));
        assignmentData.put("wasteType", requestData.getString("WASTE_TYPE"));
        assignmentData.put("customerEmail", requestData.getString("EMAIL"));
        assignmentData.put("status", "assigned");
        assignmentData.put("timestamp", FieldValue.serverTimestamp());

        firestore.collection("collector").document(collectorId)
                .collection("assignedRequests")
                .document(requestData.getString("REQUEST_ID"))
                .set(assignmentData)
                .addOnSuccessListener(aVoid -> {
                    Toast.makeText(this, "Request assigned to collector successfully", Toast.LENGTH_SHORT).show();

                    firestore.collection("pickupRequests")
                            .document(requestData.getString("REQUEST_ID"))
                            .update("status", "assigned", "assignedTo", collectorId)
                            .addOnSuccessListener(aVoid1 -> {
                                updateCustomerRequestStatus(requestData.getString("EMAIL"), requestData.getString("REQUEST_ID"), "assigned");
                                loadingDialog.dismiss();
                                finish();
                            })
                            .addOnFailureListener(e -> {
                                loadingDialog.dismiss();
                                Toast.makeText(this, "Failed to update pickupRequests status", Toast.LENGTH_SHORT).show();
                            });
                })
                .addOnFailureListener(e -> {
                    loadingDialog.dismiss();
                    Toast.makeText(this, "Failed to assign request: " + e.getMessage(), Toast.LENGTH_SHORT).show();
                });
    }

    private void updateCustomerRequestStatus(String customerEmail, String requestId, String status) {
        firestore.collection("customers")
                .whereEqualTo("email", customerEmail)
                .get()
                .addOnSuccessListener(queryDocumentSnapshots -> {
                    if (!queryDocumentSnapshots.isEmpty()) {
                        String customerId = queryDocumentSnapshots.getDocuments().get(0).getId();
                        firestore.collection("customers")
                                .document(customerId)
                                .collection("pickupRequests")
                                .document(requestId)
                                .update("status", status)
                                .addOnSuccessListener(aVoid -> {
                                    // No further action needed here
                                })
                                .addOnFailureListener(e -> Toast.makeText(this, "Failed to update customer request status", Toast.LENGTH_SHORT).show());
                    }
                })
                .addOnFailureListener(e -> Toast.makeText(this, "Failed to find customer", Toast.LENGTH_SHORT).show());
    }

    private int dpToPx(int dp) {
        float density = getResources().getDisplayMetrics().density;
        return Math.round(dp * density);
    }
}
