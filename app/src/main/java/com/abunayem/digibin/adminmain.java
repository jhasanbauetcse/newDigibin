package com.abunayem.digibin;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.widget.ImageView;
import android.widget.Toast;
import androidx.appcompat.app.AppCompatActivity;
import com.google.firebase.auth.FirebaseAuth;

public class adminmain extends AppCompatActivity {

    private static final String TAG = "AdminMainActivity";
    ImageView profileBtn, requestBtn, priceListBtn, logoutBtn, totalCollectionBtn;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_adminmain);

        try {
            // Initialize views
            profileBtn = findViewById(R.id.profileAdminImage213);
            requestBtn = findViewById(R.id.requestAdminImage28);
            priceListBtn = findViewById(R.id.priceListAdminImage26);
            logoutBtn = findViewById(R.id.logoutAdminImage214);
            totalCollectionBtn = findViewById(R.id.requestAdminImage); // This is the Total Collection image

            // Set click listeners
            profileBtn.setOnClickListener(v -> navigateTo(adminprofile.class));
            requestBtn.setOnClickListener(v -> navigateTo(Status_Admin.class));
            priceListBtn.setOnClickListener(v -> navigateTo(adminpricelistupdate.class));
            logoutBtn.setOnClickListener(v -> logoutUser());

            // Set click listener for Total Collection
            totalCollectionBtn.setOnClickListener(v -> {
                try {
                    if (NetworkUtils.isNetworkAvailable(this)) {
                        Intent intent = new Intent(adminmain.this, admin_total_collection.class);
                        startActivity(intent);
                    } else {
                        showToast("This feature requires internet connection");
                    }
                } catch (Exception e) {
                    Log.e(TAG, "Error navigating to total collection", e);
                    showToast("Error opening collection page");
                }
            });

        } catch (Exception e) {
            Log.e(TAG, "Initialization error", e);
            showToast("App initialization failed");
        }
    }

    private void navigateTo(Class<?> cls) {
        try {
            if (NetworkUtils.isNetworkAvailable(this)) {
                startActivity(new Intent(this, cls));
            } else {
                showToast("This feature requires internet connection");
            }
        } catch (Exception e) {
            Log.e(TAG, "Navigation error", e);
            showToast("Error opening page");
        }
    }

    private void logoutUser() {
        try {
            FirebaseAuth.getInstance().signOut();
            showToast("Logged out");
            startActivity(new Intent(this, LoginAdmin.class));
            finish();
        } catch (Exception e) {
            Log.e(TAG, "Logout error", e);
            showToast("Logout failed");
        }
    }

    private void showToast(String message) {
        Toast.makeText(this, message, Toast.LENGTH_LONG).show();
    }
}