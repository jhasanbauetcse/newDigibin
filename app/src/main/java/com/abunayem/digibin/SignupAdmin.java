package com.abunayem.digibin;

import android.content.Intent;
import android.os.Bundle;
import android.text.TextUtils;
import android.util.Patterns;
import android.widget.EditText;
import android.widget.Toast;
import androidx.appcompat.app.AppCompatActivity;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.FirebaseFirestoreSettings;
import com.google.firebase.firestore.SetOptions;
import java.util.HashMap;
import java.util.Map;

public class SignupAdmin extends AppCompatActivity {

    private EditText nameField, emailField, passwordField;
    private FirebaseAuth mAuth;
    private FirebaseFirestore firestore;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_adminsignup);

        // Initialize Firestore with offline persistence
        firestore = FirebaseFirestore.getInstance();
        FirebaseFirestoreSettings settings = new FirebaseFirestoreSettings.Builder()
                .setPersistenceEnabled(true)
                .build();
        firestore.setFirestoreSettings(settings);

        mAuth = FirebaseAuth.getInstance();
        nameField = findViewById(R.id.admin_signup_name_input);
        emailField = findViewById(R.id.admin_signup_email_input);
        passwordField = findViewById(R.id.admin_signup_password_input);

        findViewById(R.id.admin_signup_submit_button).setOnClickListener(v -> registerAdmin());
        findViewById(R.id.admin_signup_login_cta).setOnClickListener(v ->
                startActivity(new Intent(this, LoginAdmin.class)));
    }

    private void registerAdmin() {
        String name = nameField.getText().toString().trim();
        String email = emailField.getText().toString().trim();
        String password = passwordField.getText().toString().trim();

        if (TextUtils.isEmpty(name)) {
            nameField.setError("Admin name is required");
            return;
        }

        if (TextUtils.isEmpty(email) || !Patterns.EMAIL_ADDRESS.matcher(email).matches()) {
            emailField.setError("Valid admin email is required");
            return;
        }

        if (TextUtils.isEmpty(password) || password.length() < 6) {
            passwordField.setError("Admin password must be at least 6 characters");
            return;
        }

        mAuth.createUserWithEmailAndPassword(email, password).addOnSuccessListener(authResult -> {
            String adminId = authResult.getUser().getUid();

            Map<String, Object> adminData = new HashMap<>();
            adminData.put("name", name);
            adminData.put("email", email);
            adminData.put("role", "admin");

            // Use merge options to ensure data is cached
            firestore.collection("Admins").document(adminId)
                    .set(adminData, SetOptions.merge())
                    .addOnSuccessListener(unused -> {
                        Toast.makeText(this,
                                "Admin account created successfully",
                                Toast.LENGTH_SHORT).show();
                        startActivity(new Intent(this, LoginAdmin.class));
                        finish();
                    })
                    .addOnFailureListener(e -> {
                        // Delete the admin account if Firestore fails
                        authResult.getUser().delete();
                        Toast.makeText(this,
                                "Failed to create admin account: " + e.getMessage(),
                                Toast.LENGTH_LONG).show();
                    });

        }).addOnFailureListener(e ->
                Toast.makeText(this,
                        "Admin registration failed: " + e.getMessage(),
                        Toast.LENGTH_LONG).show());
    }
}