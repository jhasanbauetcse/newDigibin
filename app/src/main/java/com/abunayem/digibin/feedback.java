package com.abunayem.digibin; // Make sure this matches your package name

import android.os.Bundle;
import android.text.TextUtils;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar; // Optional: if you want a toolbar

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.FieldValue;
import com.google.firebase.firestore.FirebaseFirestore;

import java.util.HashMap;
import java.util.Map;

public class feedback extends AppCompatActivity {

    private static final String TAG = "FeedbackActivity";
    private static final String COLLECTION_FEEDBACKS = "feedbacks"; // Firestore collection name

    private EditText feedbackEditTextMessage;
    private Button feedbackButtonSubmit;

    private FirebaseFirestore db;
    private FirebaseAuth mAuth;
    private LoadingDialog loadingDialog;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_feedback); // Your XML layout file

        // Initialize Firebase Auth and Firestore
        mAuth = FirebaseAuth.getInstance();
        db = FirebaseFirestore.getInstance();

        // Initialize LoadingDialog (ensure you have this class in your project)
        loadingDialog = new LoadingDialog(this);

        // Initialize UI elements
        feedbackEditTextMessage = findViewById(R.id.feedbackEditTextMessage);
        feedbackButtonSubmit = findViewById(R.id.feedbackButtonSubmit);

        // Optional: Setup a Toolbar
        // Toolbar toolbar = findViewById(R.id.toolbar_feedback); // Add a Toolbar with this ID to your XML
        // setSupportActionBar(toolbar);
        // if (getSupportActionBar() != null) {
        //     getSupportActionBar().setTitle("Submit Feedback");
        //     getSupportActionBar().setDisplayHomeAsUpEnabled(true); // Show back button
        // }


        feedbackButtonSubmit.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                submitFeedback();
            }
        });
    }

    private void submitFeedback() {
        String feedbackMessage = feedbackEditTextMessage.getText().toString().trim();

        if (TextUtils.isEmpty(feedbackMessage)) {
            feedbackEditTextMessage.setError("Feedback cannot be empty.");
            feedbackEditTextMessage.requestFocus();
            return;
        }

        FirebaseUser currentUser = mAuth.getCurrentUser();
        if (currentUser == null) {
            Toast.makeText(this, "You need to be logged in to submit feedback.", Toast.LENGTH_LONG).show();
            // Optionally, redirect to login screen
            return;
        }

        String userId = currentUser.getUid();
        String userEmail = currentUser.getEmail(); // Optional: store user's email

        loadingDialog.show(); // Show loading indicator

        Map<String, Object> feedbackData = new HashMap<>();
        feedbackData.put("userId", userId);
        if (userEmail != null) {
            feedbackData.put("userEmail", userEmail);
        }
        feedbackData.put("feedbackText", feedbackMessage);
        feedbackData.put("timestamp", FieldValue.serverTimestamp()); // Adds a server-side timestamp
        feedbackData.put("status", "new"); // Optional: to track feedback status (e.g., new, read, addressed)


        // Add a new document with a generated ID to the "feedbacks" collection
        db.collection(COLLECTION_FEEDBACKS)
                .add(feedbackData)
                .addOnSuccessListener(documentReference -> {
                    loadingDialog.dismiss();
                    Log.d(TAG, "Feedback submitted successfully with ID: " + documentReference.getId());
                    Toast.makeText(feedback.this, "Feedback submitted successfully!", Toast.LENGTH_SHORT).show();
                    feedbackEditTextMessage.setText(""); // Clear the input field
                    // finish(); // Optionally close the activity after submission
                })
                .addOnFailureListener(e -> {
                    loadingDialog.dismiss();
                    Log.w(TAG, "Error submitting feedback", e);
                    Toast.makeText(feedback.this, "Error submitting feedback: " + e.getMessage(), Toast.LENGTH_LONG).show();
                });
    }

    // Optional: Handle Toolbar back button press
    @Override
    public boolean onSupportNavigateUp() {
        onBackPressed();
        return true;
    }

    // Ensure you have a LoadingDialog class, for example:
    // public class LoadingDialog {
    //     private android.app.AlertDialog dialog;
    //     private android.content.Context context;
    //
    //     public LoadingDialog(android.content.Context context) {
    //         this.context = context;
    //     }
    //
    //     public void show() {
    //         android.app.AlertDialog.Builder builder = new android.app.AlertDialog.Builder(context);
    //         // For a simple ProgressBar:
    //         android.widget.ProgressBar progressBar = new android.widget.ProgressBar(context, null, android.R.attr.progressBarStyleLarge);
    //         LinearLayout.LayoutParams lp = new LinearLayout.LayoutParams(
    //                 LinearLayout.LayoutParams.WRAP_CONTENT,
    //                 LinearLayout.LayoutParams.WRAP_CONTENT);
    //         progressBar.setLayoutParams(lp);
    //         builder.setView(progressBar);
    //         builder.setCancelable(false);
    //         dialog = builder.create();
    //         dialog.show();
    //     }
    //
    //     public void dismiss() {
    //         if (dialog != null && dialog.isShowing()) {
    //             dialog.dismiss();
    //         }
    //     }
    // }
}
