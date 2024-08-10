package com.app.sellmate;

import android.content.Intent;
import android.os.Bundle;
import android.widget.AdapterView;
import android.widget.GridView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

public class Home extends AppCompatActivity {

    GridView gridView;
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

        isAdmin = getIntent().getBooleanExtra("isAdmin", false);

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
                case 4: // Reports
                    if (isAdmin) {
                        startActivity(new Intent(Home.this, SummaryReportActivity.class));
                    } else {
                        showAccessDenied();
                    }
                    break;
//                case 5: // Settings
//                    if (isAdmin) {
//                        startActivity(new Intent(Home.this, SettingsActivity.class));
//                    } else {
//                        showAccessDenied();
//                    }
//                    break;
                default:
                    Toast.makeText(this, "Invalid option selected", Toast.LENGTH_SHORT).show();
            }
        } catch (Exception e) {
            Toast.makeText(this, "An error occurred: " + e.getMessage(), Toast.LENGTH_LONG).show();
        }
    }

    private void showAccessDenied() {
        Toast.makeText(this, "Access Denied", Toast.LENGTH_SHORT).show();
    }
}
