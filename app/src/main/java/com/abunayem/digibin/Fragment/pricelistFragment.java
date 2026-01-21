package com.abunayem.digibin.Fragment; // Updated package name

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView; // ImageView is not strictly needed if not interacting with them

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;

import com.abunayem.digibin.R; // Updated R class import
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.ListenerRegistration;

public class pricelistFragment extends Fragment {

    private FirebaseFirestore db;
    private ListenerRegistration priceListener;

    // TextViews for showing prices, linked to the perfected XML layout IDs
    private TextView priceTextBook, priceNewspaper, priceCartonBox, priceSoftPlastics,
            priceHardPlastics, priceCPU, priceMonitor,
            priceTablet, priceSteel, priceTin, priceAluminium;
    // Note: 'Fiber' was present in the original Java snippet but not explicitly in the perfected XML.
    // Assuming it's part of the 'Plastic' section or will be added to the XML later.
    // For now, I'll keep the TextView declaration but it won't be found by ID if not in XML.
    // If you intend to have a 'Fiber' item, please ensure its TextView ID is added to the XML.
    private TextView priceFiber;


    // Default constructor
    public pricelistFragment() {
        // Required empty public constructor
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment.
        // Ensure 'fragment_pricelist' matches your actual layout file name in res/layout.
        View view = inflater.inflate(R.layout.fragment_pricelist, container, false);

        // Firestore initialization
        db = FirebaseFirestore.getInstance();

        // Link TextViews to their corresponding IDs from the perfected XML layout
        priceTextBook     = view.findViewById(R.id.paper_price_1);
        priceNewspaper    = view.findViewById(R.id.paper_price_2);
        priceCartonBox    = view.findViewById(R.id.paper_price_3);
        priceSoftPlastics = view.findViewById(R.id.plastic_price_1);
        priceHardPlastics = view.findViewById(R.id.plastic_price_2);
        priceCPU          = view.findViewById(R.id.ewaste_price_1);
        priceMonitor      = view.findViewById(R.id.ewaste_price_2);
        priceTablet       = view.findViewById(R.id.ewaste_price_3);
        priceSteel        = view.findViewById(R.id.metals_price_1);
        priceTin          = view.findViewById(R.id.metals_price_2);
        priceAluminium    = view.findViewById(R.id.metals_price_3);

        // If you have a 'Fiber' item in your XML, link it here.
        // For example: priceFiber = view.findViewById(R.id.fiber_price_id);
        // Currently, based on the provided XML, there isn't a specific TextView for Fiber.
        // If you add it to your XML, uncomment and update the line below:
        // priceFiber        = view.findViewById(R.id.your_fiber_price_id);


        // Removed example ImageView click actions as they are not part of the core price display
        // and used old, non-descriptive IDs.

        return view;
    }

    @Override
    public void onStart() {
        super.onStart();
        // Set up the Firestore listener when the fragment starts
        setupFirestoreListener();
    }

    @Override
    public void onStop() {
        super.onStop();
        // Remove the Firestore listener when the fragment stops to prevent memory leaks
        if (priceListener != null) {
            priceListener.remove();
        }
    }

    /**
     * Sets up a real-time Firestore snapshot listener for the "Pricelist" collection,
     * specifically for the document named "current_prices".
     * When data changes or on initial load, it updates the UI.
     */
    private void setupFirestoreListener() {
        priceListener = db.collection("Pricelist")
                .document("current_prices")
                .addSnapshotListener((documentSnapshot, error) -> {
                    // Handle any errors that occur during the snapshot listening
                    if (error != null) {
                        // Log the error or display a user-friendly message
                        // For debugging: Log.e("PricelistFragment", "Listen failed.", error);
                        return;
                    }

                    // Check if the document exists and is not null
                    if (documentSnapshot != null && documentSnapshot.exists()) {
                        // Update the UI with the new data from Firestore
                        updatePricesFromFirestore(documentSnapshot);
                    } else {
                        // Optional: Handle case where document does not exist or is null
                        // For example, display default prices or a "No data" message
                        // Log.d("PricelistFragment", "Current data: null");
                    }
                });
    }

    /**
     * Updates all price TextViews in the UI using data from the provided Firestore DocumentSnapshot.
     * It maps Firestore document fields to specific TextViews.
     * @param document The DocumentSnapshot containing the price data.
     */
    private void updatePricesFromFirestore(@NonNull DocumentSnapshot document) {
        // Update each price TextView using the helper function
        // The second argument is the field name in your Firestore document
        // The third argument is the prefix for the displayed text (e.g., "Text Book\n৳")
        updatePriceView(priceTextBook,     "textbook",      "Text Book\n৳", document);
        updatePriceView(priceNewspaper,    "newspaper",     "News Paper\n৳", document);
        updatePriceView(priceCartonBox,    "carton_box",    "Carton Box\n৳", document);
        updatePriceView(priceSoftPlastics, "soft_plastics", "Soft Plastics\n৳", document);
        updatePriceView(priceHardPlastics, "hard_plastics", "Hard Plastics\n৳", document);
        updatePriceView(priceCPU,          "cpu",           "CPU\n৳", document);
        updatePriceView(priceMonitor,      "monitor",       "Monitor\n৳", document);
        updatePriceView(priceTablet,       "tablet",        "Tablet\n৳", document);
        updatePriceView(priceSteel,        "steel",         "Steel\n৳", document);
        updatePriceView(priceTin,          "tin",           "Tin\n৳", document);
        updatePriceView(priceAluminium,    "aluminium",     "Aluminium\n৳", document);

        // If you have a TextView for Fiber and a corresponding field in Firestore, update it here:
        updatePriceView(priceFiber,        "fiber",         "Fiber\n৳", document);
    }

    /**
     * Helper function to safely update a TextView with a price from a Firestore document.
     * It retrieves the price for a given field name and formats it.
     * @param textView The TextView to update.
     * @param fieldName The name of the field in the Firestore document (e.g., "textbook").
     * @param prefix The static text to prepend to the price (e.g., "Text Book\n৳").
     * @param document The DocumentSnapshot from which to retrieve the price.
     */
    private void updatePriceView(TextView textView, String fieldName, String prefix, DocumentSnapshot document) {
        // Ensure the TextView is not null before attempting to set text
        if (textView == null) {
            // Optional: Log a warning if a TextView is not found, which might indicate a mismatch
            // between XML IDs and Java code.
            // Log.w("PricelistFragment", "TextView for field '" + fieldName + "' not found.");
            return;
        }

        // Get the price object from the document. It could be a Long, Double, or String.
        Object priceObj = document.get(fieldName);
        String priceStr;

        // Convert the price object to a string. Default to "0" if null.
        if (priceObj != null) {
            priceStr = priceObj.toString();
        } else {
            priceStr = "0"; // Default price if the field is missing in Firestore
            // Optional: Log that a price field was missing
            // Log.d("PricelistFragment", "Firestore field '" + fieldName + "' is missing or null.");
        }

        // Set the formatted text to the TextView
        textView.setText(prefix + priceStr + "/Kg");
    }
}
