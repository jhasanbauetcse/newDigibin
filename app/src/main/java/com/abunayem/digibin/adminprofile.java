package com.abunayem.digibin;

import android.content.Intent;
import android.os.Bundle;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;
import androidx.appcompat.app.AppCompatActivity;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;

public class adminprofile extends AppCompatActivity {

    private TextView nameText, emailText, idText;
    private FirebaseAuth mAuth;
    private FirebaseFirestore firestore;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_adminprofile); // Ensure this matches your XML filename

        // Initialize Firebase
        mAuth = FirebaseAuth.getInstance();
        firestore = FirebaseFirestore.getInstance();

        // Link views
        nameText = findViewById(R.id.nameValue82);
        emailText = findViewById(R.id.emailValue78);
        idText = findViewById(R.id.idValue784);
        Button backButton = findViewById(R.id.backToHomeButton857);

        loadAdminProfile();

        backButton.setOnClickListener(v -> {
            startActivity(new Intent(adminprofile.this, adminmain.class));
            finish();
        });
    }

    private void loadAdminProfile() {
        if (mAuth.getCurrentUser() == null) {
            Toast.makeText(this, "User not logged in", Toast.LENGTH_SHORT).show();
            return;
        }

        String uid = mAuth.getCurrentUser().getUid();
        idText.setText(uid);  // Show UID

        firestore.collection("Users").document(uid).get()
                .addOnSuccessListener(documentSnapshot -> {
                    if (documentSnapshot.exists()) {
                        String name = documentSnapshot.getString("name");
                        String email = documentSnapshot.getString("email");

                        nameText.setText(name != null ? name : "N/A");
                        emailText.setText(email != null ? email : "N/A");
                    } else {
                        Toast.makeText(this, "User data not found", Toast.LENGTH_SHORT).show();
                    }
                })
                .addOnFailureListener(e -> Toast.makeText(this, "Failed to load data", Toast.LENGTH_SHORT).show());
    }
}
