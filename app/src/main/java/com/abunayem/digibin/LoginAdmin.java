package com.abunayem.digibin;

import android.content.Intent;
import android.os.Bundle;
import android.text.TextUtils;
import android.widget.EditText;
import android.widget.Toast;
import androidx.appcompat.app.AppCompatActivity;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.FirebaseFirestoreSettings;
import com.google.firebase.firestore.Source;

public class LoginAdmin extends AppCompatActivity {
    private EditText emailField, passwordField;
    private FirebaseAuth mAuth;
    private FirebaseFirestore firestore;
    private LoadingDialog loadingDialog;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_adminlogin);

        loadingDialog = new LoadingDialog(this, "Verifying admin...");

        firestore = FirebaseFirestore.getInstance();
        FirebaseFirestoreSettings settings = new FirebaseFirestoreSettings.Builder()
                .setPersistenceEnabled(true)
                .build();
        firestore.setFirestoreSettings(settings);

        mAuth = FirebaseAuth.getInstance();
        emailField = findViewById(R.id.emailEditText);
        passwordField = findViewById(R.id.passwordEditText);

        findViewById(R.id.loginBtn).setOnClickListener(v -> loginAdmin());

        if (mAuth.getCurrentUser() != null) {
            loadingDialog.show();
            verifyAdminRole(mAuth.getCurrentUser().getUid());
        }
    }

    private void loginAdmin() {
        String email = emailField.getText().toString().trim();
        String password = passwordField.getText().toString().trim();

        if (TextUtils.isEmpty(email)) {
            emailField.setError("Admin email is required");
            return;
        }

        if (TextUtils.isEmpty(password)) {
            passwordField.setError("Admin password is required");
            return;
        }

        loadingDialog.show();
        mAuth.signInWithEmailAndPassword(email, password).addOnCompleteListener(task -> {
            if (task.isSuccessful()) {
                verifyAdminRole(mAuth.getCurrentUser().getUid());
            } else {
                loadingDialog.dismiss();
                Toast.makeText(this,
                        "Admin login failed: " + task.getException().getMessage(),
                        Toast.LENGTH_LONG).show();
            }
        });
    }

    private void verifyAdminRole(String adminId) {
        boolean isOnline = NetworkUtils.isNetworkAvailable(this);
        Source source = isOnline ? Source.SERVER : Source.CACHE;

        if (!isOnline) {
            Toast.makeText(this, "Offline mode - using cached data", Toast.LENGTH_SHORT).show();
        }

        firestore.collection("Admins").document(adminId).get(source)
                .addOnCompleteListener(task -> {
                    loadingDialog.dismiss();
                    if (task.isSuccessful()) {
                        if (task.getResult().exists() && "admin".equals(task.getResult().getString("role"))) {
                            startActivity(new Intent(this, adminmain.class));
                            finish();
                        } else {
                            mAuth.signOut();
                            Toast.makeText(this,
                                    "Access restricted to administrators only",
                                    Toast.LENGTH_LONG).show();
                        }
                    } else {
                        Toast.makeText(this,
                                "Error verifying admin credentials",
                                Toast.LENGTH_SHORT).show();
                    }
                });
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        if (loadingDialog != null && loadingDialog.isShowing()) {
            loadingDialog.dismiss();
        }
    }
}