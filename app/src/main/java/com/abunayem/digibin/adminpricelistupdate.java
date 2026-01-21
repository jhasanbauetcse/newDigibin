package com.abunayem.digibin; // Replace with your actual package name

import androidx.appcompat.app.AppCompatActivity;

import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import com.google.firebase.firestore.FirebaseFirestore;

import java.util.HashMap;
import java.util.Map;

public class adminpricelistupdate extends AppCompatActivity {

    private FirebaseFirestore db;
    private Button updateButton;

    // EditText fields for all price inputs
    private EditText editTextBook, editTextNewspaper, editTextCartonBox, editTextSoftPlastics,
            editTextHardPlastics, editTextFiber, editTextCPU, editTextMonitor,
            editTextTablet, editTextSteel, editTextTin, editTextAluminium;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_adminpricelistupdate);

        // Initialize Firestore
        db = FirebaseFirestore.getInstance();

        // Initialize all EditText fields
        editTextBook = findViewById(R.id.editTe85xtText);
        editTextNewspaper = findViewById(R.id.editTe85xtText2);
        editTextCartonBox = findViewById(R.id.editTe85xtText3);
        editTextSoftPlastics = findViewById(R.id.editTe85xtText4);
        editTextHardPlastics = findViewById(R.id.editTe85xtText5);
        editTextFiber = findViewById(R.id.editTe85xtText6);
        editTextCPU = findViewById(R.id.editTe85xtText7);
        editTextMonitor = findViewById(R.id.editTe85xtText8);
        editTextTablet = findViewById(R.id.editTe85xtText9);
        editTextSteel = findViewById(R.id.editTe85xtText10);
        editTextTin = findViewById(R.id.editTe85xtText11);
        editTextAluminium = findViewById(R.id.editTe85xtText12);

        updateButton = findViewById(R.id.button3788555);

        updateButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                updatePricesInFirestore();
            }
        });
    }

    private void updatePricesInFirestore() {
        // Create a map with all the price data
        Map<String, Object> priceData = new HashMap<>();

        // Add all prices to the map
        priceData.put("textbook", getPriceFromEditText(editTextBook));
        priceData.put("newspaper", getPriceFromEditText(editTextNewspaper));
        priceData.put("carton_box", getPriceFromEditText(editTextCartonBox));
        priceData.put("soft_plastics", getPriceFromEditText(editTextSoftPlastics));
        priceData.put("hard_plastics", getPriceFromEditText(editTextHardPlastics));
        priceData.put("fiber", getPriceFromEditText(editTextFiber));
        priceData.put("cpu", getPriceFromEditText(editTextCPU));
        priceData.put("monitor", getPriceFromEditText(editTextMonitor));
        priceData.put("tablet", getPriceFromEditText(editTextTablet));
        priceData.put("steel", getPriceFromEditText(editTextSteel));
        priceData.put("tin", getPriceFromEditText(editTextTin));
        priceData.put("aluminium", getPriceFromEditText(editTextAluminium));

        // Update the document in Firestore
        db.collection("Pricelist").document("current_prices") // You can change the document ID if needed
                .set(priceData)
                .addOnSuccessListener(aVoid -> {
                    Toast.makeText(adminpricelistupdate.this, "Prices updated successfully!", Toast.LENGTH_SHORT).show();
                })
                .addOnFailureListener(e -> {
                    Toast.makeText(adminpricelistupdate.this, "Error updating prices: " + e.getMessage(), Toast.LENGTH_SHORT).show();
                });
    }

    private String getPriceFromEditText(EditText editText) {
        String price = editText.getText().toString().trim();
        // Return empty string if no price was entered (or handle differently as needed)
        return price.isEmpty() ? "0" : price; // Default to "0" if empty
    }
}