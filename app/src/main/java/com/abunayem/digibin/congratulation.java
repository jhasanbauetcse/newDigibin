package com.abunayem.digibin;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;

import androidx.appcompat.app.AppCompatActivity;

public class congratulation extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_congratulation);  // Use your provided layout

        // Find the "Back to Home" button
        Button backToHomeButton = findViewById(R.id.button_submit);

        // Set an OnClickListener for the button
        backToHomeButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                // Create an Intent to navigate to MainActivity
                Intent intent = new Intent(congratulation.this, MainActivity.class);

                // Start MainActivity
                startActivity(intent);

                // Optionally finish this activity to prevent the user from returning here
                finish();
            }
        });
    }
}
