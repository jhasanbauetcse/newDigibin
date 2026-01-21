package com.abunayem.digibin;

import android.os.Bundle;
import android.widget.TextView;
import android.widget.Toast;
import androidx.appcompat.app.AppCompatActivity;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.Source;

public class collprofile extends AppCompatActivity {

    private TextView nameTextView, emailTextView;
    private FirebaseAuth mAuth;
    private FirebaseFirestore firestore;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_collprofile);

        // Initialize Firebase instances
        mAuth = FirebaseAuth.getInstance();
        firestore = FirebaseFirestore.getInstance();

        // Initialize views - only those present in XML
        nameTextView = findViewById(R.id.textView28);
        emailTextView = findViewById(R.id.textView42);

        // Load collector profile data
        loadCollectorProfile();
    }

    private void loadCollectorProfile() {
        FirebaseUser currentUser = mAuth.getCurrentUser();
        if (currentUser == null) {
            Toast.makeText(this, "Not authenticated", Toast.LENGTH_SHORT).show();
            finish();
            return;
        }

        // Determine data source based on network availability
        Source source = NetworkUtils.isNetworkAvailable(this) ? Source.SERVER : Source.CACHE;

        if (!NetworkUtils.isNetworkAvailable(this)) {
            Toast.makeText(this, "Offline mode - using cached data", Toast.LENGTH_SHORT).show();
        }

        firestore.collection("collector").document(currentUser.getUid())
                .get(source)
                .addOnCompleteListener(task -> {
                    if (task.isSuccessful()) {
                        DocumentSnapshot document = task.getResult();
                        if (document.exists()) {
                            // Set profile data from Firestore
                            String name = document.getString("name");
                            String email = document.getString("email");

                            nameTextView.setText(name != null ? name : "Not available");
                            emailTextView.setText(email != null ? email : "Not available");
                        } else {
                            Toast.makeText(this, "Profile data not found", Toast.LENGTH_SHORT).show();
                        }
                    } else {
                        // If cache failed but we're online, try server directly
                        if (source == Source.CACHE && NetworkUtils.isNetworkAvailable(this)) {
                            loadCollectorProfile(); // Retry with server source
                            return;
                        }
                        Toast.makeText(this,
                                "Failed to load profile: " + task.getException().getMessage(),
                                Toast.LENGTH_SHORT).show();
                    }
                });
    }

    @Override
    protected void onStart() {
        super.onStart();
        // Check if user is signed in (non-null)
        if (mAuth.getCurrentUser() == null) {
            finish(); // Close activity if not authenticated
        }
    }
}