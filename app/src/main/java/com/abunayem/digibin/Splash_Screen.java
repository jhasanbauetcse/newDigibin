package com.abunayem.digibin;

import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.widget.Toast;
import androidx.appcompat.app.AppCompatActivity;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.FirebaseFirestoreSettings;
import com.google.firebase.firestore.Source;

public class Splash_Screen extends AppCompatActivity {
    private Handler handler = new Handler();
    private Runnable runnable;
    private FirebaseAuth mAuth;
    private FirebaseFirestore firestore;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_splash_screen);

        // Check network status
        if (!NetworkUtils.isNetworkAvailable(this)) {
            Toast.makeText(this, "Offline mode - using cached data", Toast.LENGTH_SHORT).show();
        }

        // Initialize Firebase with persistence
        mAuth = FirebaseAuth.getInstance();
        firestore = FirebaseFirestore.getInstance();
        FirebaseFirestoreSettings settings = new FirebaseFirestoreSettings.Builder()
                .setPersistenceEnabled(true)
                .build();
        firestore.setFirestoreSettings(settings);

        runnable = new Runnable() {
            @Override
            public void run() {
                checkAuthState();
            }
        };
        handler.postDelayed(runnable, 1500); // Reduced splash time
    }

    private void checkAuthState() {
        FirebaseUser currentUser = mAuth.getCurrentUser();
        if (currentUser != null) {
            verifyUserRole(currentUser.getUid());
        } else {
            redirectToStartActivity();
        }
    }

    private void verifyUserRole(String userId) {
        // First try to verify as admin
        verifyAdmin(userId);
    }

    private void verifyAdmin(String adminId) {
        Source source = NetworkUtils.isNetworkAvailable(this) ? Source.SERVER : Source.CACHE;

        firestore.collection("Admins").document(adminId).get(source)
                .addOnCompleteListener(task -> {
                    if (task.isSuccessful() && task.getResult().exists()
                            && "admin".equals(task.getResult().getString("role"))) {
                        redirectToAdminMain();
                    } else {
                        // If not admin, check collector or regular user
                        verifyCollector(adminId);
                    }
                });
    }

    private void verifyCollector(String collectorId) {
        Source source = NetworkUtils.isNetworkAvailable(this) ? Source.SERVER : Source.CACHE;

        firestore.collection("collector").document(collectorId).get(source)
                .addOnCompleteListener(task -> {
                    if (task.isSuccessful() && task.getResult().exists()) {
                        redirectToCollMainActivity();
                    } else {
                        // If not collector either, go to regular start
                        redirectToStartActivity();
                    }
                });
    }

    private void redirectToAdminMain() {
        startActivity(new Intent(this, adminmain.class));
        finish();
    }

    private void redirectToCollMainActivity() {
        startActivity(new Intent(this, CollMainActivity.class));
        finish();
    }

    private void redirectToStartActivity() {
        startActivity(new Intent(this, StartActivity.class));
        finish();
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        handler.removeCallbacks(runnable);
    }
}