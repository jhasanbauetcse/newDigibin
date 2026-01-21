package com.abunayem.digibin;

import android.graphics.Typeface; // Import Typeface
import android.os.Bundle;
import android.util.Log;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.RelativeLayout;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.content.ContextCompat; // For ContextCompat.getColor

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.Query; // Added for Query.Direction
// import java.util.ArrayList; // Not strictly necessary if only used locally in fetch

public class Status_Customer extends AppCompatActivity {

    private static final String TAG = "Status_Customer";

    private LinearLayout linearLayoutContainer;
    private int boxCount = 1;
    // private ArrayList<String> requestIds; // Not strictly necessary if only used locally in fetch
    private FirebaseAuth mAuth;
    private FirebaseFirestore firestore;
    private LoadingDialog loadingDialog; // Assuming LoadingDialog class is defined elsewhere

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_status); // Ensure this layout file exists and contains R.id.linearLayoutContainer

        mAuth = FirebaseAuth.getInstance();
        firestore = FirebaseFirestore.getInstance();
        // requestIds = new ArrayList<>(); // Initialize if needed globally
        linearLayoutContainer = findViewById(R.id.linearLayoutContainer);
        loadingDialog = new LoadingDialog(this);

        loadingDialog.show();
        fetchPickupRequests();
    }

    private void fetchPickupRequests() {
        FirebaseUser currentUser = mAuth.getCurrentUser();
        if (currentUser == null) {
            Log.e(TAG, "User not logged in.");
            loadingDialog.dismiss();
            // Optionally, show a Toast or redirect to login
            // Toast.makeText(this, "Please log in to view status.", Toast.LENGTH_SHORT).show();
            return;
        }
        String userId = currentUser.getUid();

        firestore.collection("customers")
                .document(userId)
                .collection("pickupRequests")
                .orderBy("timestamp", Query.Direction.DESCENDING) // Ensure "timestamp" field exists
                .get()
                .addOnSuccessListener(queryDocumentSnapshots -> {
                    loadingDialog.dismiss();
                    boxCount = 1;
                    linearLayoutContainer.removeAllViews(); // Clear previous views before adding new ones
                    if (queryDocumentSnapshots == null || queryDocumentSnapshots.isEmpty()) {
                        Log.d(TAG, "No pickup requests found for user: " + userId);
                        // Optionally, display a message to the user (e.g., "No requests yet.")
                        TextView noRequestsView = new TextView(this);
                        noRequestsView.setText("You have no pickup requests yet.");
                        noRequestsView.setTextSize(18f);
                        noRequestsView.setPadding(dpToPx(16), dpToPx(16), dpToPx(16), dpToPx(16));
                        noRequestsView.setGravity(android.view.Gravity.CENTER);
                        linearLayoutContainer.addView(noRequestsView);
                        return;
                    }
                    for (DocumentSnapshot document : queryDocumentSnapshots) {
                        String requestId = document.getId();
                        String status = document.getString("status");
                        // com.google.firebase.Timestamp timestamp = document.getTimestamp("timestamp");
                        // String formattedTime = timestamp != null ? new java.text.SimpleDateFormat("dd MMM yyyy, hh:mm a", java.util.Locale.getDefault()).format(timestamp.toDate()) : "No timestamp";
                        addPickupRequestBox(requestId, status /*, formattedTime */);
                    }
                })
                .addOnFailureListener(e -> {
                    loadingDialog.dismiss();
                    Log.e(TAG, "Error fetching pickup requests", e);
                    // Optionally, show a Toast to the user
                    // Toast.makeText(this, "Error fetching requests.", Toast.LENGTH_SHORT).show();
                });
    }

    private void addPickupRequestBox(String requestId, String status /*, String formattedTime */) {
        RelativeLayout requestBox = new RelativeLayout(this);

        LinearLayout.LayoutParams params = new LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.MATCH_PARENT,
                LinearLayout.LayoutParams.WRAP_CONTENT
        );
        params.setMargins(dpToPx(16), dpToPx(8), dpToPx(16), dpToPx(8));
        requestBox.setLayoutParams(params);
        requestBox.setBackgroundResource(R.drawable.box_border_green); // Ensure this drawable exists
        requestBox.setPadding(dpToPx(16), dpToPx(16), dpToPx(16), dpToPx(16));

        // Request ID Text
        TextView requestText = new TextView(this);
        requestText.setId(TextView.generateViewId());
        requestText.setText(String.format("%d. RequestID:\n%s", boxCount, requestId));
        requestText.setTextSize(18f);
        // Use ContextCompat.getColor for backward compatibility
        requestText.setTextColor(ContextCompat.getColor(this, android.R.color.black));
        RelativeLayout.LayoutParams requestTextParams = new RelativeLayout.LayoutParams(
                RelativeLayout.LayoutParams.WRAP_CONTENT, RelativeLayout.LayoutParams.WRAP_CONTENT);
        requestTextParams.addRule(RelativeLayout.ALIGN_PARENT_START);
        requestTextParams.addRule(RelativeLayout.ALIGN_PARENT_TOP);
        requestTextParams.rightMargin = dpToPx(80); // Add margin to avoid overlap with statusText
        requestText.setLayoutParams(requestTextParams);
        requestBox.addView(requestText);

        // Status Text
        TextView statusText = new TextView(this);
        statusText.setId(TextView.generateViewId());
        statusText.setText(status != null ? status.substring(0, 1).toUpperCase() + status.substring(1) : "Pending"); // Capitalize first letter
        statusText.setTextSize(16f);
        statusText.setTypeface(null, Typeface.BOLD); // Corrected line

        RelativeLayout.LayoutParams statusParams = new RelativeLayout.LayoutParams(
                RelativeLayout.LayoutParams.WRAP_CONTENT, RelativeLayout.LayoutParams.WRAP_CONTENT);
        statusParams.addRule(RelativeLayout.ALIGN_PARENT_END);
        statusParams.addRule(RelativeLayout.ALIGN_PARENT_TOP);
        statusText.setLayoutParams(statusParams);

        // Set status text color
        if ("Pending".equalsIgnoreCase(status)) {
            statusText.setTextColor(ContextCompat.getColor(this, android.R.color.holo_red_dark));
        } else if ("approved".equalsIgnoreCase(status)) {
            statusText.setTextColor(ContextCompat.getColor(this, android.R.color.holo_orange_dark));
        } else if ("assigned".equalsIgnoreCase(status)) {
            statusText.setTextColor(ContextCompat.getColor(this, android.R.color.holo_blue_dark));
        } else if ("Collected".equalsIgnoreCase(status)) {
            statusText.setTextColor(ContextCompat.getColor(this, android.R.color.holo_green_dark));
        } else {
            statusText.setTextColor(ContextCompat.getColor(this, android.R.color.darker_gray));
        }
        requestBox.addView(statusText);

        // If collected, fetch and display value/weight
        if ("Collected".equalsIgnoreCase(status)) {
            final TextView valueText = new TextView(this);
            valueText.setId(TextView.generateViewId());
            valueText.setTextSize(16f);
            valueText.setTextColor(ContextCompat.getColor(this, android.R.color.black));
            RelativeLayout.LayoutParams valueParams = new RelativeLayout.LayoutParams(
                    RelativeLayout.LayoutParams.WRAP_CONTENT, RelativeLayout.LayoutParams.WRAP_CONTENT);
            valueParams.addRule(RelativeLayout.BELOW, requestText.getId());
            valueParams.addRule(RelativeLayout.ALIGN_PARENT_START);
            valueParams.topMargin = dpToPx(8);
            valueText.setLayoutParams(valueParams);
            requestBox.addView(valueText);

            final TextView weightText = new TextView(this);
            weightText.setId(TextView.generateViewId());
            weightText.setTextSize(16f);
            weightText.setTextColor(ContextCompat.getColor(this, android.R.color.black));
            RelativeLayout.LayoutParams weightParams = new RelativeLayout.LayoutParams(
                    RelativeLayout.LayoutParams.WRAP_CONTENT, RelativeLayout.LayoutParams.WRAP_CONTENT);
            weightParams.addRule(RelativeLayout.BELOW, valueText.getId());
            weightParams.addRule(RelativeLayout.ALIGN_PARENT_START);
            weightParams.topMargin = dpToPx(4);
            weightText.setLayoutParams(weightParams);
            requestBox.addView(weightText);

            valueText.setText("Total Value: Loading...");
            weightText.setText("Total Weight: Loading...");

            firestore.collection("wasteCollections")
                    .whereEqualTo("request_id", requestId)
                    .limit(1)
                    .get()
                    .addOnSuccessListener(queryDocumentSnapshotsWc -> { // Renamed to avoid conflict
                        if (queryDocumentSnapshotsWc != null && !queryDocumentSnapshotsWc.isEmpty()) {
                            DocumentSnapshot documentWc = queryDocumentSnapshotsWc.getDocuments().get(0); // Renamed
                            Double totalValueDouble = documentWc.getDouble("total_value");
                            Double totalWeightDouble = documentWc.getDouble("total_weight");

                            valueText.setText(String.format(java.util.Locale.getDefault(),"Total Value: %.2f BDT", (totalValueDouble != null ? totalValueDouble : 0.0)));
                            weightText.setText(String.format(java.util.Locale.getDefault(),"Total Weight: %.2f kg", (totalWeightDouble != null ? totalWeightDouble : 0.0)));
                        } else {
                            valueText.setText("Total Value: N/A");
                            weightText.setText("Total Weight: N/A");
                            Log.w(TAG, "No wasteCollection document found for requestId: " + requestId);
                        }
                    })
                    .addOnFailureListener(e -> {
                        Log.e(TAG, "Error fetching waste collection details for " + requestId, e);
                        valueText.setText("Total Value: Error");
                        weightText.setText("Total Weight: Error");
                    });
        }

        boxCount++;
        linearLayoutContainer.addView(requestBox);
    }

    private int dpToPx(int dp) {
        float density = getResources().getDisplayMetrics().density;
        return (int) (dp * density + 0.5f);
    }

    // Define or import your LoadingDialog class
    // Example:
    // public static class LoadingDialog { // Made static if it's an inner class and doesn't need outer class instance
    //     private android.app.AlertDialog dialog;
    //     private android.content.Context context; // Store context
    //
    //     public LoadingDialog(android.content.Context context) {
    //         this.context = context;
    //     }
    //
    //     public void show() {
    //         if (context == null) return; // Avoid errors if context is null
    //         android.app.AlertDialog.Builder builder = new android.app.AlertDialog.Builder(context);
    //         // Assuming you have a layout file: loading_dialog_layout.xml
    //         // android.view.LayoutInflater inflater = android.view.LayoutInflater.from(context);
    //         // android.view.View dialogView = inflater.inflate(R.layout.loading_dialog_layout, null);
    //         // builder.setView(dialogView);
    //         // For a simple ProgressBar:
    //         android.widget.ProgressBar progressBar = new android.widget.ProgressBar(context, null, android.R.attr.progressBarStyleLarge);
    //         LinearLayout.LayoutParams lp = new LinearLayout.LayoutParams(
    //                 LinearLayout.LayoutParams.WRAP_CONTENT,
    //                 LinearLayout.LayoutParams.WRAP_CONTENT);
    //         progressBar.setLayoutParams(lp);
    //         builder.setView(progressBar);
    //         builder.setCancelable(false);
    //         dialog = builder.create();
    //         dialog.show();
    //     }
    //
    //     public void dismiss() {
    //         if (dialog != null && dialog.isShowing()) {
    //             dialog.dismiss();
    //         }
    //     }
    // }
}
