package com.abunayem.digibin;

import android.os.Bundle;
import android.view.View;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.google.firebase.Timestamp;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.Query;

import java.util.Map;

public class admin_total_collection extends AppCompatActivity {

    private LinearLayout linearLayoutContainer;
    private int boxCount = 1;
    private FirebaseFirestore firestore;
    private LoadingDialog loadingDialog;  // Declare loading dialog

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_admin_total_collection);

        firestore = FirebaseFirestore.getInstance();
        linearLayoutContainer = findViewById(R.id.linearLayoutContainer);

        // Initialize loading dialog with custom message if you want
        loadingDialog = new LoadingDialog(this, "Loading collections...");
        loadingDialog.show();  // Show loading dialog before starting fetch

        fetchWasteCollections();
    }

    private void fetchWasteCollections() {
        firestore.collection("wasteCollections")
                .orderBy("collection_timestamp", Query.Direction.DESCENDING)
                .get()
                .addOnSuccessListener(queryDocumentSnapshots -> {
                    loadingDialog.dismiss();  // Dismiss loading dialog on success
                    linearLayoutContainer.removeAllViews();
                    boxCount = 1;

                    for (DocumentSnapshot document : queryDocumentSnapshots) {
                        String collectionId = document.getId();
                        String collectorId = document.getString("collector_id");
                        String requestId = document.getString("request_id");
                        String status = document.getString("status");

                        Timestamp timestampObj = document.getTimestamp("collection_timestamp");
                        String timestamp = (timestampObj != null) ? timestampObj.toDate().toString() : "N/A";

                        Long pointsEarned = document.getLong("points_earned");
                        Long totalValue = document.getLong("total_value");
                        Long totalWeight = document.getLong("total_weight");

                        Map<String, Object> collectedItems = (Map<String, Object>) document.get("collected_items");

                        StringBuilder itemsDetails = new StringBuilder();

                        if (collectedItems != null) {
                            for (Map.Entry<String, Object> entry : collectedItems.entrySet()) {
                                String category = entry.getKey();
                                Map<String, Object> categoryData = (Map<String, Object>) entry.getValue();

                                itemsDetails.append(category).append(":\n");
                                if (categoryData != null) {
                                    for (Map.Entry<String, Object> itemEntry : categoryData.entrySet()) {
                                        itemsDetails.append("  ")
                                                .append(itemEntry.getKey())
                                                .append(": ")
                                                .append(itemEntry.getValue())
                                                .append("\n");
                                    }
                                }
                                itemsDetails.append("\n");
                            }
                        }

                        addWasteCollectionBox(collectionId, collectorId, requestId,
                                status, timestamp, pointsEarned,
                                totalValue, totalWeight, itemsDetails.toString());
                    }
                })
                .addOnFailureListener(e -> {
                    loadingDialog.dismiss(); // Dismiss loading dialog on failure
                    Toast.makeText(this, "Failed to load collections: " + e.getMessage(), Toast.LENGTH_SHORT).show();
                    e.printStackTrace();
                });
    }

    private void addWasteCollectionBox(String collectionId, String collectorId, String requestId,
                                       String status, String timestamp, Long pointsEarned,
                                       Long totalValue, Long totalWeight, String itemsDetails) {
        RelativeLayout collectionBox = new RelativeLayout(this);

        LinearLayout.LayoutParams params = new LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.MATCH_PARENT,
                LinearLayout.LayoutParams.WRAP_CONTENT
        );
        params.setMargins(dpToPx(16), 0, dpToPx(16), dpToPx(16));
        collectionBox.setLayoutParams(params);
        collectionBox.setBackgroundResource(R.drawable.box_border_green);
        collectionBox.setPadding(dpToPx(12), dpToPx(12), dpToPx(12), dpToPx(12));

        String collectionInfo = boxCount + ". Collection ID: " + collectionId
                + "\nRequest ID: " + requestId
                + "\nCollector ID: " + collectorId
                + "\nStatus: " + status
                + "\nTimestamp: " + timestamp
                + "\nPoints Earned: " + pointsEarned
                + "\nTotal Value: " + totalValue
                + "\nTotal Weight: " + totalWeight + "kg"
                + "\n\nItems Collected:\n" + itemsDetails;

        TextView collectionText = new TextView(this);
        collectionText.setText(collectionInfo);
        collectionText.setTextSize(14f);
        collectionText.setTextColor(getResources().getColor(android.R.color.black));
        collectionText.setId(View.generateViewId());

        RelativeLayout.LayoutParams textParams = new RelativeLayout.LayoutParams(
                RelativeLayout.LayoutParams.MATCH_PARENT,
                RelativeLayout.LayoutParams.WRAP_CONTENT
        );
        textParams.addRule(RelativeLayout.ALIGN_PARENT_START);
        collectionText.setLayoutParams(textParams);

        collectionBox.addView(collectionText);

        linearLayoutContainer.addView(collectionBox);
        boxCount++;
    }

    private int dpToPx(int dp) {
        float density = getResources().getDisplayMetrics().density;
        return Math.round(dp * density);
    }
}
