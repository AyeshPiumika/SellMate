package com.app.sellmate;

import android.os.Bundle;
import android.widget.TextView;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import java.util.ArrayList;

public class SummaryReportActivity extends AppCompatActivity {

    private RecyclerView recyclerView;
    private SummaryAdapter summaryAdapter;
    private TextView textViewTotalSales, textViewTotalDiscount, textViewNetTotal;
    private DatabaseHelper databaseHelper;
    private ArrayList<Invoice> invoiceList;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_summary_report);

        // Initialize views
        recyclerView = findViewById(R.id.recyclerViewSummary);
        textViewTotalSales = findViewById(R.id.textViewTotalSales);
        textViewTotalDiscount = findViewById(R.id.textViewTotalDiscount);
        textViewNetTotal = findViewById(R.id.textViewNetTotal);

        // Initialize DatabaseHelper
        databaseHelper = new DatabaseHelper(this);

        // Load data and set up RecyclerView
        loadData();
        setupRecyclerView();
        updateSummary();
    }

    private void loadData() {
        // Fetch invoices from the database
        invoiceList = databaseHelper.getAllInvoices();
    }

    private void setupRecyclerView() {
        recyclerView.setLayoutManager(new LinearLayoutManager(this));
        summaryAdapter = new SummaryAdapter(invoiceList);
        recyclerView.setAdapter(summaryAdapter);
    }

    private void updateSummary() {
        double totalSales = 0.0;
        double totalDiscount = 0.0;

        for (Invoice invoice : invoiceList) {
            totalSales += invoice.getTotalAmount();
            totalDiscount += invoice.getDiscount();
        }

        double netTotal = totalSales - totalDiscount;

        // Update TextViews
        textViewTotalSales.setText("Total Sales: " + String.format("%.2f", totalSales));
        textViewTotalDiscount.setText("Total Discount: " + String.format("%.2f", totalDiscount));
        textViewNetTotal.setText("Net Total: " + String.format("%.2f", netTotal));
    }
}
