package com.abunayem.digibin.Fragment;

import android.content.Intent;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.fragment.app.Fragment;

import com.abunayem.digibin.Status_Customer;
import com.abunayem.digibin.R;
import com.abunayem.digibin.pickuprequest;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.Source;

public class HomeFragment extends Fragment {

    private Button pickupRequestButton;
    private ImageView historyImageView;
    private TextView statusTextView;
    private TextView pointsTextView;
    private FirebaseFirestore firestore;
    private FirebaseAuth mAuth;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View rootView = inflater.inflate(R.layout.fragment_home, container, false);

        // Initialize Firebase with persistence
        firestore = FirebaseFirestore.getInstance();
        mAuth = FirebaseAuth.getInstance();

        // Initialize views
        pickupRequestButton = rootView.findViewById(R.id.button34);
        historyImageView = rootView.findViewById(R.id.imageView10);
        statusTextView = rootView.findViewById(R.id.textView15);
        pointsTextView = rootView.findViewById(R.id.textView14);

        // Set click listeners
        pickupRequestButton.setOnClickListener(v -> {
            Intent intent = new Intent(getActivity(), pickuprequest.class);
            startActivity(intent);
        });

        View.OnClickListener statusClickListener = v -> {
            Intent historyIntent = new Intent(getActivity(), Status_Customer.class);
            startActivity(historyIntent);
        };

        historyImageView.setOnClickListener(statusClickListener);
        statusTextView.setOnClickListener(statusClickListener);

        // Load points
        loadCustomerPoints();

        return rootView;
    }

    private void loadCustomerPoints() {
        String userId = mAuth.getCurrentUser() != null ? mAuth.getCurrentUser().getUid() : null;

        if (userId == null) {
            return;
        }

        // Try cache first
        firestore.collection("customers").document(userId)
                .get(Source.CACHE)
                .addOnCompleteListener(cacheTask -> {
                    if (cacheTask.isSuccessful() && cacheTask.getResult().exists()) {
                        updatePointsUI(cacheTask.getResult());
                    } else {
                        // Try server if cache fails
                        firestore.collection("customers").document(userId)
                                .get(Source.SERVER)
                                .addOnCompleteListener(serverTask -> {
                                    if (serverTask.isSuccessful() && serverTask.getResult().exists()) {
                                        updatePointsUI(serverTask.getResult());
                                    }
                                });
                    }
                });
    }

    private void updatePointsUI(DocumentSnapshot document) {
        Long points = document.getLong("points");
        int displayPoints = points != null ? points.intValue() : 0;
        pointsTextView.setText(String.valueOf(displayPoints));
    }

    @Override
    public void onResume() {
        super.onResume();
        loadCustomerPoints();
    }
}