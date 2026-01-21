package com.abunayem.digibin;

import android.content.Intent;
import android.os.Bundle;
import android.widget.EditText;
import android.widget.Toast;
import androidx.appcompat.app.AppCompatActivity;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.FirebaseFirestoreSettings;

public class LoginCustomer extends AppCompatActivity {
    private EditText emailEditText, passwordEditText;
    private FirebaseAuth mAuth;
    private FirebaseFirestore firestore;
    private LoadingDialog loadingDialog;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        loadingDialog = new LoadingDialog(this, "Signing in...");

        mAuth = FirebaseAuth.getInstance();
        firestore = FirebaseFirestore.getInstance();
        FirebaseFirestoreSettings settings = new FirebaseFirestoreSettings.Builder()
                .setPersistenceEnabled(true)
                .build();
        firestore.setFirestoreSettings(settings);

        FirebaseUser currentUser = mAuth.getCurrentUser();
        if (currentUser != null) {
            loadingDialog.show();
            verifyCustomer(currentUser.getUid());
            return;
        }

        setContentView(R.layout.activity_login);

        emailEditText = findViewById(R.id.editTextTextEmailAddress);
        passwordEditText = findViewById(R.id.editTextTextPassword);

        findViewById(R.id.loginButton).setOnClickListener(v -> loginUser());
        findViewById(R.id.textView4).setOnClickListener(v ->
                startActivity(new Intent(this, SignupCustomer.class)));
    }

    private void loginUser() {
        String email = emailEditText.getText().toString().trim();
        String password = passwordEditText.getText().toString().trim();

        if (email.isEmpty() || password.isEmpty()) {
            Toast.makeText(this, "Please fill in all fields", Toast.LENGTH_SHORT).show();
            return;
        }

        loadingDialog.show();
        mAuth.signInWithEmailAndPassword(email, password)
                .addOnCompleteListener(this, task -> {
                    if (task.isSuccessful()) {
                        verifyCustomer(mAuth.getCurrentUser().getUid());
                    } else {
                        loadingDialog.dismiss();
                        Toast.makeText(this, "Login failed", Toast.LENGTH_SHORT).show();
                    }
                });
    }

    private void verifyCustomer(String uid) {
        firestore.collection("customers").document(uid).get()
                .addOnCompleteListener(task -> {
                    loadingDialog.dismiss();
                    if (task.isSuccessful() && task.getResult().exists()) {
                        String location = task.getResult().getString("location");
                        if (location != null) {
                            startActivity(new Intent(this, MainActivity.class));
                        } else {
                            startActivity(new Intent(this, ChooseLocationActivity.class));
                        }
                        finish();
                    } else {
                        mAuth.signOut();
                        Toast.makeText(this, "Not a customer account", Toast.LENGTH_SHORT).show();
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