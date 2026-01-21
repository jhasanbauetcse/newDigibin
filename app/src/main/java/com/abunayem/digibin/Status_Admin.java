package com.abunayem.digibin;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.Query;

public class Status_Admin extends AppCompatActivity {

    private LinearLayout linearLayoutContainer;
    private int boxCount = 1;
    private FirebaseFirestore firestore;
    private LoadingDialog loadingDialog;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.status_admin);

        firestore = FirebaseFirestore.getInstance();
        linearLayoutContainer = findViewById(R.id.linearLayoutContainer);

        loadingDialog = new LoadingDialog(this);

        fetchPickupRequests();
    }

    private void fetchPickupRequests() {
        loadingDialog.show();

        firestore.collection("pickupRequests")
                .orderBy("timestamp", Query.Direction.DESCENDING)
                .get()
                .addOnSuccessListener(queryDocumentSnapshots -> {
                    loadingDialog.dismiss();
                    linearLayoutContainer.removeAllViews();
                    boxCount = 1;

                    for (DocumentSnapshot document : queryDocumentSnapshots) {
                        String requestId = document.getId();
                        String phone = document.getString("phone");
                        String address = document.getString("address");
                        String schedule = document.getString("schedule");
                        String wasteType = document.getString("wasteType");
                        String email = document.getString("email");
                        String status = document.getString("status");

                        addPickupRequestBox(requestId, phone, address, schedule, wasteType, email, status);
                    }
                })
                .addOnFailureListener(e -> {
                    loadingDialog.dismiss();
                    Toast.makeText(this, "Failed to load requests: " + e.getMessage(), Toast.LENGTH_SHORT).show();
                    e.printStackTrace();
                });
    }

    private void addPickupRequestBox(String requestId, String phone, String address,
                                     String schedule, String wasteType, String email,
                                     String status) {
        RelativeLayout requestBox = new RelativeLayout(this);

        LinearLayout.LayoutParams params = new LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.MATCH_PARENT, LinearLayout.LayoutParams.WRAP_CONTENT);
        params.setMargins(dpToPx(16), 0, dpToPx(16), dpToPx(16));
        requestBox.setLayoutParams(params);
        requestBox.setBackgroundResource(R.drawable.box_border_green);
        requestBox.setPadding(dpToPx(12), dpToPx(12), dpToPx(12), dpToPx(12));

        String requestInfo = boxCount + ". Request ID: " + requestId
                + "\nPhone: " + phone
                + "\nAddress: " + address
                + "\nSchedule: " + schedule
                + "\nWaste Type: " + wasteType
                + "\nEmail: " + email;

        TextView requestText = new TextView(this);
        requestText.setText(requestInfo);
        requestText.setTextSize(16f);
        requestText.setTextColor(getResources().getColor(android.R.color.black));
        requestText.setId(View.generateViewId());

        RelativeLayout.LayoutParams textParams = new RelativeLayout.LayoutParams(
                RelativeLayout.LayoutParams.MATCH_PARENT,
                RelativeLayout.LayoutParams.WRAP_CONTENT
        );
        textParams.addRule(RelativeLayout.ALIGN_PARENT_START);
        textParams.addRule(RelativeLayout.ALIGN_PARENT_TOP);
        textParams.setMargins(0, 0, dpToPx(90), 0);
        requestText.setLayoutParams(textParams);

        requestBox.addView(requestText);

        Button approveStatusButton = new Button(this);
        approveStatusButton.setTextSize(14f);
        RelativeLayout.LayoutParams buttonParams = new RelativeLayout.LayoutParams(
                RelativeLayout.LayoutParams.WRAP_CONTENT, RelativeLayout.LayoutParams.WRAP_CONTENT);
        buttonParams.addRule(RelativeLayout.ALIGN_PARENT_END);
        buttonParams.addRule(RelativeLayout.ALIGN_PARENT_TOP);
        approveStatusButton.setLayoutParams(buttonParams);

        if ("pending".equalsIgnoreCase(status) || status == null || status.isEmpty()) {
            approveStatusButton.setText("Approve");
            approveStatusButton.setBackgroundResource(R.drawable.box_green);
            approveStatusButton.setTextColor(getResources().getColor(android.R.color.white));
            approveStatusButton.setOnClickListener(v -> {
                Bundle requestData = new Bundle();
                requestData.putString("REQUEST_ID", requestId);
                requestData.putString("PHONE", phone);
                requestData.putString("ADDRESS", address);
                requestData.putString("SCHEDULE", schedule);
                requestData.putString("WASTE_TYPE", wasteType);
                requestData.putString("EMAIL", email);

                updateCustomerRequestStatus(email, requestId, "assigned");

                Intent intent = new Intent(Status_Admin.this, Admin_coll_assign.class);
                intent.putExtras(requestData);
                startActivity(intent);
            });
            requestBox.addView(approveStatusButton);
        } else if ("assigned".equalsIgnoreCase(status) || "collected".equalsIgnoreCase(status)) {
            approveStatusButton.setText(status);
            approveStatusButton.setBackgroundColor(getResources().getColor(android.R.color.transparent));
            approveStatusButton.setTextColor(getResources().getColor(
                    "assigned".equalsIgnoreCase(status) ? android.R.color.holo_blue_dark :
                            android.R.color.holo_green_dark
            ));
            approveStatusButton.setClickable(false);
            requestBox.addView(approveStatusButton);
        }

        linearLayoutContainer.addView(requestBox);
        boxCount++;
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
                                    fetchPickupRequests();
                                });
                    }
                });
    }

    private int dpToPx(int dp) {
        float density = getResources().getDisplayMetrics().density;
        return Math.round(dp * density);
    }
}
