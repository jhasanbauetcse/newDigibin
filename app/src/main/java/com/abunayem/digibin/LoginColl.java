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
import com.google.firebase.firestore.Source;

public class LoginColl extends AppCompatActivity {
    private EditText emailEditText, passwordEditText;
    private FirebaseAuth mAuth;
    private FirebaseFirestore firestore;
    private LoadingDialog loadingDialog;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        loadingDialog = new LoadingDialog(this, "Authenticating collector...");

        firestore = FirebaseFirestore.getInstance();
        FirebaseFirestoreSettings settings = new FirebaseFirestoreSettings.Builder()
                .setPersistenceEnabled(true)
                .build();
        firestore.setFirestoreSettings(settings);

        mAuth = FirebaseAuth.getInstance();
        FirebaseUser currentUser = mAuth.getCurrentUser();
        if (currentUser != null) {
            loadingDialog.show();
            verifyCollector(currentUser.getUid());
            return;
        }

        setContentView(R.layout.activity_coll_login);

        emailEditText = findViewById(R.id.editTextTextEmailAddress);
        passwordEditText = findViewById(R.id.editTextTextPassword);

        findViewById(R.id.loginButton).setOnClickListener(v -> loginUser());
        findViewById(R.id.textView4).setOnClickListener(v ->
                startActivity(new Intent(this, SignupColl.class)));
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
                        verifyCollector(mAuth.getCurrentUser().getUid());
                    } else {
                        loadingDialog.dismiss();
                        Toast.makeText(this, "Authentication failed", Toast.LENGTH_SHORT).show();
                    }
                });
    }

    private void verifyCollector(String uid) {
        firestore.collection("collector").document(uid).get(Source.CACHE)
                .addOnCompleteListener(cacheTask -> {
                    if (cacheTask.isSuccessful() && cacheTask.getResult().exists()) {
                        loadingDialog.dismiss();
                        proceedToMainActivity();
                    } else {
                        firestore.collection("collector").document(uid).get(Source.SERVER)
                                .addOnCompleteListener(serverTask -> {
                                    loadingDialog.dismiss();
                                    if (serverTask.isSuccessful() && serverTask.getResult().exists()) {
                                        proceedToMainActivity();
                                    } else {
                                        mAuth.signOut();
                                        Toast.makeText(this,
                                                "Access restricted to collectors only",
                                                Toast.LENGTH_LONG).show();
                                    }
                                });
                    }
                });
    }

    private void proceedToMainActivity() {
        startActivity(new Intent(this, CollMainActivity.class));
        finish();
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        if (loadingDialog != null && loadingDialog.isShowing()) {
            loadingDialog.dismiss();
        }
    }
}