package com.app.sellmate;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ListView;

import androidx.appcompat.app.AppCompatActivity;

import java.util.ArrayList;

public class CustomerSelectionActivity extends AppCompatActivity {

    private ListView customerListView;
    private DatabaseHelper databaseHelper;
    private ArrayList<Customer> customers;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.popup_customers);

        customerListView = findViewById(R.id.customer_list_view);
        databaseHelper = new DatabaseHelper(this);

        // Load customers from the database
        customers = databaseHelper.getAllCustomers();

        CustomerAdapter customerAdapter = new CustomerAdapter(this, customers);
        customerListView.setAdapter(customerAdapter);

        customerListView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                Customer selectedCustomer = customers.get(position);
                Intent resultIntent = new Intent();
                resultIntent.putExtra("selected_customer", selectedCustomer);
                setResult(RESULT_OK, resultIntent);
                finish();
            }
        });
    }
}