package com.abunayem.digibin;

import android.os.Bundle;
import android.util.Log;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.FieldValue;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QuerySnapshot; // Kept for potential future use, not directly used in this version's get()

import java.util.HashMap;
import java.util.Map;

public class Collector_calculate extends AppCompatActivity {

    private static final String TAG = "Collector_calculate";

    // Intent Extras
    public static final String EXTRA_REQUEST_ID = "REQUEST_ID";
    public static final String EXTRA_CUSTOMER_EMAIL = "CUSTOMER_EMAIL";

    // Firestore Collections
    public static final String COLLECTION_WASTE_COLLECTIONS = "wasteCollections";
    public static final String COLLECTION_CUSTOMERS = "customers";
    public static final String COLLECTION_PICKUP_REQUESTS = "pickupRequests";
    public static final String COLLECTION_COLLECTORS = "collector"; // Assuming "collector" is the top-level collection for collectors
    public static final String SUBCOLLECTION_ASSIGNED_REQUESTS = "assignedRequests";
    public static final String SUBCOLLECTION_CUSTOMER_PICKUP_REQUESTS = "pickupRequests";


    // Firestore Fields
    public static final String FIELD_EMAIL = "email";
    public static final String FIELD_POINTS = "points";
    public static final String FIELD_REQUEST_ID = "request_id";
    public static final String FIELD_COLLECTOR_ID = "collector_id";
    public static final String FIELD_COLLECTED_ITEMS = "collected_items";
    public static final String FIELD_TOTAL_WEIGHT = "total_weight";
    public static final String FIELD_TOTAL_VALUE = "total_value";
    public static final String FIELD_COLLECTION_TIMESTAMP = "collection_timestamp";
    public static final String FIELD_STATUS = "status";
    public static final String FIELD_POINTS_EARNED = "points_earned";
    public static final String FIELD_LAST_UPDATED = "last_updated";

    // Waste Item Details in Map
    public static final String ITEM_FIELD_WEIGHT = "weight";
    public static final String ITEM_FIELD_PRICE_PER_KG = "price_per_kg";
    public static final String ITEM_FIELD_VALUE = "value";

    // Waste Types (as keys in the map)
    public static final String WASTE_TYPE_PAPER = "paper";
    public static final String WASTE_TYPE_PLASTIC = "plastic";
    public static final String WASTE_TYPE_METALS = "metals";
    public static final String WASTE_TYPE_MOTOR = "motor";
    public static final String WASTE_TYPE_E_WASTE = "e_waste";
    public static final String WASTE_TYPE_OTHER_WASTE = "other_waste";
    public static final String WASTE_TYPE_ORGANIC = "organic";


    private EditText editTextPaperWeight, editTextPaperPricePerKg;
    private EditText editTextPlasticWeight, editTextPlasticPricePerKg;
    private EditText editTextMetalsWeight, editTextMetalsPricePerKg;
    private EditText editTextMotorWeight, editTextMotorPricePerKg;
    private EditText editTextEWasteWeight, editTextEWastePricePerKg;
    private EditText editTextOtherWasteWeight, editTextOtherWastePricePerKg;
    private EditText editTextOrganicWasteWeight, editTextOrganicWastePricePerKg;

    private Button buttonCollect;
    private FirebaseFirestore firestore;
    private FirebaseAuth mAuth;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.collector_calculate); // Ensure this matches your XML file name

        // Initialize Firestore and FirebaseAuth
        firestore = FirebaseFirestore.getInstance();
        mAuth = FirebaseAuth.getInstance();

        // Initialize all EditText fields
        initializeViews();

        buttonCollect = findViewById(R.id.buttonCollect); // Updated ID
        buttonCollect.setOnClickListener(v -> submitWasteData());
    }

    private void initializeViews() {
        // Paper
        editTextPaperWeight = findViewById(R.id.editTextPaperWeight);
        editTextPaperPricePerKg = findViewById(R.id.editTextPaperPricePerKg);

        // Plastic
        editTextPlasticWeight = findViewById(R.id.editTextPlasticWeight);
        editTextPlasticPricePerKg = findViewById(R.id.editTextPlasticPricePerKg);

        // Metals
        editTextMetalsWeight = findViewById(R.id.editTextMetalsWeight);
        editTextMetalsPricePerKg = findViewById(R.id.editTextMetalsPricePerKg);

        // Motor
        editTextMotorWeight = findViewById(R.id.editTextMotorWeight);
        editTextMotorPricePerKg = findViewById(R.id.editTextMotorPricePerKg);

        // E-Waste
        editTextEWasteWeight = findViewById(R.id.editTextEWasteWeight);
        editTextEWastePricePerKg = findViewById(R.id.editTextEWastePricePerKg);

        // Other Waste
        editTextOtherWasteWeight = findViewById(R.id.editTextOtherWasteWeight);
        editTextOtherWastePricePerKg = findViewById(R.id.editTextOtherWastePricePerKg);

        // Organic
        editTextOrganicWasteWeight = findViewById(R.id.editTextOrganicWasteWeight);
        editTextOrganicWastePricePerKg = findViewById(R.id.editTextOrganicWastePricePerKg);
    }

    private void submitWasteData() {
        String requestId = getIntent().getStringExtra(EXTRA_REQUEST_ID);
        String customerEmail = getIntent().getStringExtra(EXTRA_CUSTOMER_EMAIL);

        FirebaseUser currentUser = mAuth.getCurrentUser();
        if (currentUser == null) {
            Toast.makeText(this, "Collector not logged in. Please log in and try again.", Toast.LENGTH_LONG).show();
            Log.e(TAG, "Collector not logged in.");
            return;
        }
        String collectorId = currentUser.getUid();

        if (requestId == null || requestId.isEmpty()) {
            Toast.makeText(this, "Request ID is missing. Cannot proceed.", Toast.LENGTH_LONG).show();
            Log.e(TAG, "Request ID is null or empty.");
            return;
        }

        Map<String, Object> collectedItemsMap = new HashMap<>();
        // Add each waste type with weight and calculated value
        // Base prices per KG: Paper:15, Plastic:12, Metals:80, Motor:90, E-Waste:100, Other:0, Organic:60
        addWasteTypeToMap(collectedItemsMap, WASTE_TYPE_PAPER, editTextPaperWeight, editTextPaperPricePerKg, 15.0);
        addWasteTypeToMap(collectedItemsMap, WASTE_TYPE_PLASTIC, editTextPlasticWeight, editTextPlasticPricePerKg, 12.0);
        addWasteTypeToMap(collectedItemsMap, WASTE_TYPE_METALS, editTextMetalsWeight, editTextMetalsPricePerKg, 80.0);
        addWasteTypeToMap(collectedItemsMap, WASTE_TYPE_MOTOR, editTextMotorWeight, editTextMotorPricePerKg, 90.0);
        addWasteTypeToMap(collectedItemsMap, WASTE_TYPE_E_WASTE, editTextEWasteWeight, editTextEWastePricePerKg, 100.0);
        addWasteTypeToMap(collectedItemsMap, WASTE_TYPE_OTHER_WASTE, editTextOtherWasteWeight, editTextOtherWastePricePerKg, 0.0);
        addWasteTypeToMap(collectedItemsMap, WASTE_TYPE_ORGANIC, editTextOrganicWasteWeight, editTextOrganicWastePricePerKg, 60.0);

        if (collectedItemsMap.isEmpty()) {
            Toast.makeText(this, "No waste items entered. Please enter weight for at least one item.", Toast.LENGTH_LONG).show();
            return;
        }

        double totalWeight = calculateTotal(collectedItemsMap, ITEM_FIELD_WEIGHT);
        double totalValue = calculateTotal(collectedItemsMap, ITEM_FIELD_VALUE);

        // Calculate points (e.g., 5 points per 1 Taka of value)
        int pointsEarned = (int) (totalValue * 5); // Assuming 5 points per Taka value

        Map<String, Object> collectionRecord = buildCollectionRecord(requestId, collectorId, collectedItemsMap, totalWeight, totalValue, pointsEarned);

        // Save to Firestore in 'wasteCollections'
        firestore.collection(COLLECTION_WASTE_COLLECTIONS)
                .add(collectionRecord)
                .addOnSuccessListener(documentReference -> {
                    Log.d(TAG, "Waste collection recorded successfully with ID: " + documentReference.getId());
                    Toast.makeText(Collector_calculate.this, "Waste collection recorded successfully!", Toast.LENGTH_SHORT).show();

                    if (customerEmail != null && !customerEmail.isEmpty()) {
                        updateCustomerPoints(customerEmail, pointsEarned);
                    } else {
                        Log.w(TAG, "Customer email is missing, cannot update points.");
                    }

                    updateAllRequestStatuses(requestId, customerEmail, collectorId, "Collected");
                    finish(); // Close this activity
                })
                .addOnFailureListener(e -> {
                    Log.e(TAG, "Error recording waste collection", e);
                    Toast.makeText(Collector_calculate.this, "Error recording waste collection: " + e.getMessage(), Toast.LENGTH_LONG).show();
                });
    }

    private Map<String, Object> buildCollectionRecord(String requestId, String collectorId, Map<String, Object> collectedItems,
                                                      double totalWeight, double totalValue, int pointsEarned) {
        Map<String, Object> record = new HashMap<>();
        record.put(FIELD_REQUEST_ID, requestId);
        record.put(FIELD_COLLECTOR_ID, collectorId);
        record.put(FIELD_COLLECTED_ITEMS, collectedItems);
        record.put(FIELD_TOTAL_WEIGHT, totalWeight);
        record.put(FIELD_TOTAL_VALUE, totalValue);
        record.put(FIELD_COLLECTION_TIMESTAMP, FieldValue.serverTimestamp());
        record.put(FIELD_STATUS, "Collected"); // Status of this collection record
        record.put(FIELD_POINTS_EARNED, pointsEarned);
        return record;
    }


    private void updateCustomerPoints(String customerEmail, int pointsEarned) {
        if (pointsEarned <= 0) {
            Log.d(TAG, "No points earned, skipping customer points update.");
            return;
        }

        firestore.collection(COLLECTION_CUSTOMERS)
                .whereEqualTo(FIELD_EMAIL, customerEmail)
                .get()
                .addOnCompleteListener(task -> {
                    if (task.isSuccessful()) {
                        QuerySnapshot querySnapshot = task.getResult();
                        if (querySnapshot != null && !querySnapshot.isEmpty()) {
                            String customerId = querySnapshot.getDocuments().get(0).getId();
                            firestore.collection(COLLECTION_CUSTOMERS)
                                    .document(customerId)
                                    .update(FIELD_POINTS, FieldValue.increment(pointsEarned))
                                    .addOnSuccessListener(aVoid -> Log.d(TAG, "Customer points updated successfully for " + customerEmail))
                                    .addOnFailureListener(e -> Log.e(TAG, "Error updating customer points for " + customerEmail, e));
                        } else {
                            Log.w(TAG, "Customer not found with email: " + customerEmail + ". Cannot update points.");
                        }
                    } else {
                        Log.e(TAG, "Error fetching customer to update points for " + customerEmail, task.getException());
                    }
                });
    }

    private void addWasteTypeToMap(Map<String, Object> map, String typeKey,
                                   EditText weightEditText, EditText pricePerKgEditText,
                                   double defaultPricePerKg) {
        try {
            String weightStr = weightEditText.getText().toString().trim();
            String pricePerKgStr = pricePerKgEditText.getText().toString().trim();

            double weight = weightStr.isEmpty() ? 0.0 : Double.parseDouble(weightStr);

            // Only proceed if weight is greater than 0
            if (weight > 0) {
                double pricePerKg = pricePerKgStr.isEmpty() ? defaultPricePerKg : Double.parseDouble(pricePerKgStr);
                if (pricePerKg < 0) pricePerKg = defaultPricePerKg; // Ensure price is not negative, fallback to default

                double value = weight * pricePerKg;

                Map<String, Object> wasteTypeDetails = new HashMap<>();
                wasteTypeDetails.put(ITEM_FIELD_WEIGHT, weight);
                wasteTypeDetails.put(ITEM_FIELD_PRICE_PER_KG, pricePerKg);
                wasteTypeDetails.put(ITEM_FIELD_VALUE, value);

                map.put(typeKey, wasteTypeDetails);
            }
        } catch (NumberFormatException e) {
            Log.e(TAG, "Invalid number format for " + typeKey + ": " + e.getMessage());
            Toast.makeText(this, "Invalid input for " + typeKey + ". Please enter valid numbers.", Toast.LENGTH_SHORT).show();
            // Optionally, you could throw this exception to stop the submission process if any field is invalid.
        }
    }

    private double calculateTotal(Map<String, Object> collectedItemsMap, String fieldToSum) {
        double total = 0;
        for (Map.Entry<String, Object> entry : collectedItemsMap.entrySet()) {
            if (entry.getValue() instanceof Map) {
                @SuppressWarnings("unchecked") // Known to be Map<String, Object> from addWasteTypeToMap
                Map<String, Object> typeData = (Map<String, Object>) entry.getValue();
                Object fieldValue = typeData.get(fieldToSum);
                if (fieldValue instanceof Number) {
                    total += ((Number) fieldValue).doubleValue();
                }
            }
        }
        return total;
    }

    private void updateAllRequestStatuses(String requestId, String customerEmail, String collectorId, String newStatus) {
        // 1. Update the main pickupRequests collection
        firestore.collection(COLLECTION_PICKUP_REQUESTS)
                .document(requestId)
                .update(
                        FIELD_STATUS, newStatus,
                        FIELD_COLLECTOR_ID, collectorId,
                        FIELD_LAST_UPDATED, FieldValue.serverTimestamp()
                )
                .addOnSuccessListener(aVoid -> Log.d(TAG, "Main pickupRequest (" + requestId + ") status updated to " + newStatus))
                .addOnFailureListener(e -> Log.e(TAG, "Failed to update main pickupRequest (" + requestId + ") status", e));

        // 2. Update the customer's pickupRequests subcollection (if customerEmail is available)
        if (customerEmail != null && !customerEmail.isEmpty()) {
            firestore.collection(COLLECTION_CUSTOMERS).whereEqualTo(FIELD_EMAIL, customerEmail).get()
                    .addOnCompleteListener(task -> {
                        if (task.isSuccessful()) {
                            QuerySnapshot querySnapshot = task.getResult();
                            if (querySnapshot != null && !querySnapshot.isEmpty()) {
                                String customerDocId = querySnapshot.getDocuments().get(0).getId();
                                firestore.collection(COLLECTION_CUSTOMERS)
                                        .document(customerDocId)
                                        .collection(SUBCOLLECTION_CUSTOMER_PICKUP_REQUESTS) // Assuming subcollection name
                                        .document(requestId)
                                        .update(
                                                FIELD_STATUS, newStatus,
                                                FIELD_LAST_UPDATED, FieldValue.serverTimestamp()
                                        )
                                        .addOnSuccessListener(aVoid -> Log.d(TAG, "Customer's pickupRequest (" + requestId + ") status updated"))
                                        .addOnFailureListener(e -> Log.e(TAG, "Failed to update customer's pickupRequest (" + requestId + ") status", e));
                            } else {
                                Log.w(TAG, "Customer not found by email " + customerEmail + " for updating subcollection request status.");
                            }
                        } else {
                            Log.e(TAG, "Error fetching customer by email " + customerEmail + " for status update.", task.getException());
                        }
                    });
        } else {
            Log.w(TAG, "Customer email not provided, skipping update of customer's pickupRequest subcollection.");
        }


        // 3. Update the collector's assignedRequests subcollection
        // Assuming 'collectorId' is the document ID in the 'collectors' collection.
        firestore.collection(COLLECTION_COLLECTORS)
                .document(collectorId)
                .collection(SUBCOLLECTION_ASSIGNED_REQUESTS)
                .document(requestId)
                .update(
                        FIELD_STATUS, newStatus,
                        FIELD_LAST_UPDATED, FieldValue.serverTimestamp()
                )
                .addOnSuccessListener(aVoid -> Log.d(TAG, "Collector's assignedRequest (" + requestId + ") status updated"))
                .addOnFailureListener(e -> Log.e(TAG, "Failed to update collector's assignedRequest (" + requestId + ") status", e));
    }
}
