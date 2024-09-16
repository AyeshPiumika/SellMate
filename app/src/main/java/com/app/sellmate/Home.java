package com.app.sellmate;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.widget.GridView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

public class Home extends AppCompatActivity {

    GridView gridView;
    // Adding "Reports" and "Settings" to the grid items
    String[] gridItems = {"Manage Customers", "Manage Salesmen", "Manage Products", "Manage Orders", "Reports", "Settings"};
    int[] gridImages = {R.drawable.ic_customers, R.drawable.ic_salesmen, R.drawable.ic_products, R.drawable.ic_orders, R.drawable.ic_reports, R.drawable.ic_settings};

    private boolean isAdmin;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_home);

        gridView = findViewById(R.id.gridview);
        GridAdapter adapter = new GridAdapter(Home.this, gridItems, gridImages);
        gridView.setAdapter(adapter);

        // Retrieve the isAdmin value from the Intent and check for errors
        try {
            Intent intent = getIntent();
            if (intent != null && intent.hasExtra("isAdmin")) {
                isAdmin = intent.getBooleanExtra("isAdmin", false);
                Log.d("HomeActivity", "isAdmin: " + isAdmin);  // Debugging log
            } else {
                throw new Exception("Admin status not passed correctly.");
            }
        } catch (Exception e) {
            Log.e("HomeActivity", "Error retrieving isAdmin flag: " + e.getMessage());
            showToast("An error occurred while retrieving permissions.");
            finish(); // Exit the activity if there's an error
            return;
        }

        // Set the item click listener
        gridView.setOnItemClickListener((parent, view, position, id) -> handleItemClick(position));
    }

    private void handleItemClick(int position) {
        try {
            switch (position) {
                case 0: // Manage Customers
                    startActivity(new Intent(Home.this, CustomerDetails.class));
                    break;
                case 1: // Manage Salesmen
                    if (isAdmin) {
                        startActivity(new Intent(Home.this, SalesmenDetails.class));
                    } else {
                        showAccessDenied();
                    }
                    break;
                case 2: // Manage Products
                    if (isAdmin) {
                        startActivity(new Intent(Home.this, ItemDetails.class));
                    } else {
                        showAccessDenied();
                    }
                    break;
                case 3: // Manage Orders / Invoice Activity
                    startActivity(new Intent(Home.this, InvoiceActivity.class));
                    break;
                case 4: // Reports (Additional Feature)
                    showToast("Additional Feature! You can buy it");
                    break;
                case 5: // Settings (Additional Feature)
                    showToast("Additional Feature! You can buy it");
                    break;
                default:
                    showToast("Invalid option selected");
            }
        } catch (Exception e) {
            Log.e("HomeActivity", "Error in item selection: " + e.getMessage());
            showToast("An error occurred: " + e.getMessage());
        }
    }

    private void showAccessDenied() {
        Log.d("HomeActivity", "Access Denied for non-admin");
        showToast("Access Denied: Admin only");
    }

    private void showToast(String message) {
        Toast.makeText(this, message, Toast.LENGTH_SHORT).show();
    }
}
