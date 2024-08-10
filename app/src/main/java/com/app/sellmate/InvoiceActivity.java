package com.app.sellmate;

import android.Manifest;
import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.net.Uri;
import android.os.Bundle;
import android.os.Environment;
import android.text.Editable;
import android.text.TextUtils;
import android.text.TextWatcher;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.*;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import com.google.android.material.floatingactionbutton.FloatingActionButton;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.Locale;

import org.apache.poi.ss.usermodel.*;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;

public class InvoiceActivity extends AppCompatActivity {

    private ListView invoiceListView;
    private FloatingActionButton fab;
    private ArrayList<String> invoices;
    private InvoiceAdapter invoiceAdapter;
    private DatabaseHelper databaseHelper;
    private static final int REQUEST_WRITE_PERMISSION = 786;
    private int invoiceIncrement = 1; // This should be managed in the database

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_invoice);

        databaseHelper = new DatabaseHelper(this);
        invoiceListView = findViewById(R.id.invoiceListView);
        fab = findViewById(R.id.fab);
        ImageButton exportButton = findViewById(R.id.exportButton);

        invoices = new ArrayList<>();
        invoiceAdapter = new InvoiceAdapter(this, invoices);
        invoiceListView.setAdapter(invoiceAdapter);

        fab.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                showCreateInvoiceDialog();
            }
        });

        exportButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                checkPermissions();
            }
        });

        loadInvoicesForToday();
    }

    private void loadInvoicesForToday() {
        ArrayList<Invoice> invoiceList = databaseHelper.getInvoicesForToday();
        invoices.clear();
        for (Invoice invoice : invoiceList) {
            invoices.add(invoice.getInvoiceNumber());
        }
        invoiceAdapter.notifyDataSetChanged();
    }

    private void showCreateInvoiceDialog() {
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        LayoutInflater inflater = this.getLayoutInflater();
        View dialogView = inflater.inflate(R.layout.dialog_create_invoice, null);
        builder.setView(dialogView);

        AutoCompleteTextView customerAutoComplete = dialogView.findViewById(R.id.customerAutoComplete);
        EditText invoiceNumberEditText = dialogView.findViewById(R.id.invoiceNumberEditText);
        EditText dateEditText = dialogView.findViewById(R.id.dateEditText);
        LinearLayout itemsContainer = dialogView.findViewById(R.id.itemsContainer);
        EditText discountEditText = dialogView.findViewById(R.id.discountEditText);
        TextView subTotalTextView = dialogView.findViewById(R.id.subTotalTextView);
        TextView discountTextView = dialogView.findViewById(R.id.discountTextView);
        TextView totalTextView = dialogView.findViewById(R.id.totalTextView);
        Button addItemButton = dialogView.findViewById(R.id.addItemButton);
        Button createButton = dialogView.findViewById(R.id.createButton);

        AlertDialog alertDialog = builder.create();

        // Set current date
        SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd", Locale.getDefault());
        dateEditText.setText(sdf.format(new Date()));

        // Generate invoice number
        String salesmanId = "123"; // Replace with logged-in salesman ID
        String invoiceNumber = generateInvoiceNumber(salesmanId);
        invoiceNumberEditText.setText(invoiceNumber);
        invoiceNumberEditText.setEnabled(false); // Make it not editable

        setupCustomerAutoComplete(customerAutoComplete);

        addItemButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                // Add a new item row
                View itemRow = inflater.inflate(R.layout.item_row, null);
                AutoCompleteTextView itemAutoComplete = itemRow.findViewById(R.id.itemAutoComplete);
                EditText quantityEditText = itemRow.findViewById(R.id.quantityEditText);
                EditText unitPriceEditText = itemRow.findViewById(R.id.unitPriceEditText);
                EditText unitDiscountEditText = itemRow.findViewById(R.id.unitDiscount);
                TextView totalPriceTextView = itemRow.findViewById(R.id.totalPriceTextView);

                setupItemAutoComplete(itemAutoComplete, unitPriceEditText);

                // Add text watchers to calculate total price
                TextWatcher textWatcher = new TextWatcher() {
                    @Override
                    public void beforeTextChanged(CharSequence s, int start, int count, int after) {}

                    @Override
                    public void onTextChanged(CharSequence s, int start, int before, int count) {}

                    @Override
                    public void afterTextChanged(Editable s) {
                        calculateTotalPrice(quantityEditText, unitPriceEditText, unitDiscountEditText, totalPriceTextView);
                        updateInvoiceSummary(itemsContainer, subTotalTextView, discountEditText, discountTextView, totalTextView);
                    }
                };

                quantityEditText.addTextChangedListener(textWatcher);
                unitPriceEditText.addTextChangedListener(textWatcher);
                unitDiscountEditText.addTextChangedListener(textWatcher);

                itemsContainer.addView(itemRow);
            }
        });

        discountEditText.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {}

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                updateInvoiceSummary(itemsContainer, subTotalTextView, discountEditText, discountTextView, totalTextView);
            }

            @Override
            public void afterTextChanged(Editable s) {}
        });

        createButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                String customer = customerAutoComplete.getText().toString().trim();
                String invoiceNumber = invoiceNumberEditText.getText().toString().trim();
                String date = dateEditText.getText().toString().trim();
                String discount = discountEditText.getText().toString().trim();

                if (TextUtils.isEmpty(customer) || TextUtils.isEmpty(invoiceNumber) || TextUtils.isEmpty(date)) {
                    Toast.makeText(InvoiceActivity.this, "Please enter all details", Toast.LENGTH_SHORT).show();
                    return;
                }

                // Calculate subtotal, discount, and total
                double subTotal = calculateSubTotal(itemsContainer);
                double discountAmount = TextUtils.isEmpty(discount) ? 0.00 : Double.parseDouble(discount);
                double total = subTotal - discountAmount;

                subTotalTextView.setText(String.format("%.2f", subTotal));
                discountTextView.setText(String.format("%.2f", discountAmount));
                totalTextView.setText(String.format("%.2f", total));

                // Save new invoice to database
                Invoice newInvoice = new Invoice(invoiceNumber, date, customer, subTotal, discountAmount);
                databaseHelper.addInvoice(newInvoice);

                invoices.add(invoiceNumber);
                invoiceAdapter.notifyDataSetChanged();
                alertDialog.dismiss();
            }
        });

        alertDialog.show();
    }

    private void setupCustomerAutoComplete(AutoCompleteTextView customerAutoComplete) {
        ArrayList<Customer> customers = databaseHelper.getAllCustomers();
        ArrayAdapter<Customer> adapter = new ArrayAdapter<>(this,
                android.R.layout.simple_dropdown_item_1line, customers);
        customerAutoComplete.setAdapter(adapter);
        customerAutoComplete.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                showCustomerDialog(customerAutoComplete);
            }
        });
    }

    private void setupItemAutoComplete(AutoCompleteTextView itemAutoComplete, EditText unitPriceEditText) {
        ArrayList<Item> items = databaseHelper.getAllItems();
        ArrayAdapter<Item> adapter = new ArrayAdapter<>(this,
                android.R.layout.simple_dropdown_item_1line, items);
        itemAutoComplete.setAdapter(adapter);
        itemAutoComplete.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                showItemDialog(itemAutoComplete, unitPriceEditText);
            }
        });
    }

    private void showCustomerDialog(AutoCompleteTextView customerAutoComplete) {
        ArrayList<Customer> customers = databaseHelper.getAllCustomers();
        ArrayList<String> customerNames = new ArrayList<>();
        for (Customer customer : customers) {
            customerNames.add(customer.getName());
        }

        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setTitle("Select Customer");

        ArrayAdapter<String> adapter = new ArrayAdapter<>(this, android.R.layout.simple_list_item_1, customerNames);
        builder.setAdapter(adapter, new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                customerAutoComplete.setText(customerNames.get(which));
            }
        });

        builder.show();
    }

    private void showItemDialog(AutoCompleteTextView itemAutoComplete, EditText unitPriceEditText) {
        ArrayList<Item> items = databaseHelper.getAllItems();
        ArrayList<String> itemNames = new ArrayList<>();
        for (Item item : items) {
            itemNames.add(item.getName());
        }

        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setTitle("Select Item");

        ArrayAdapter<String> adapter = new ArrayAdapter<>(this, android.R.layout.simple_list_item_1, itemNames);
        builder.setAdapter(adapter, new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                Item selectedItem = items.get(which);
                itemAutoComplete.setText(selectedItem.getName());
                unitPriceEditText.setText(String.format("%.2f", selectedItem.getSellingPrice()));
            }
        });

        builder.show();
    }

    private String generateInvoiceNumber(String salesmanId) {
        SimpleDateFormat sdf = new SimpleDateFormat("yyMMdd", Locale.getDefault());
        String datePart = sdf.format(new Date());
        return salesmanId + datePart + String.format("%04d", invoiceIncrement++);
    }

    private void calculateTotalPrice(EditText quantityEditText, EditText unitPriceEditText, EditText unitDiscountEditText, TextView totalPriceTextView) {
        try {
            double quantity = TextUtils.isEmpty(quantityEditText.getText().toString()) ? 0 : Double.parseDouble(quantityEditText.getText().toString());
            double unitPrice = TextUtils.isEmpty(unitPriceEditText.getText().toString()) ? 0 : Double.parseDouble(unitPriceEditText.getText().toString());
            double unitDiscount = TextUtils.isEmpty(unitDiscountEditText.getText().toString()) ? 0 : Double.parseDouble(unitDiscountEditText.getText().toString());
            double totalPrice = (unitPrice - unitDiscount) * quantity;
            totalPriceTextView.setText(String.format("%.2f", totalPrice));
        } catch (NumberFormatException e) {
            totalPriceTextView.setText("0.00");
        }
    }

    private double calculateSubTotal(LinearLayout itemsContainer) {
        double subTotal = 0;
        for (int i = 0; i < itemsContainer.getChildCount(); i++) {
            View itemRow = itemsContainer.getChildAt(i);
            TextView totalPriceTextView = itemRow.findViewById(R.id.totalPriceTextView);
            try {
                double totalPrice = TextUtils.isEmpty(totalPriceTextView.getText().toString()) ? 0 : Double.parseDouble(totalPriceTextView.getText().toString());
                subTotal += totalPrice;
            } catch (NumberFormatException e) {
                // Ignore invalid values
            }
        }
        return subTotal;
    }

    private void updateInvoiceSummary(LinearLayout itemsContainer, TextView subTotalTextView, EditText discountEditText, TextView discountTextView, TextView totalTextView) {
        double subTotal = calculateSubTotal(itemsContainer);
        double discount = TextUtils.isEmpty(discountEditText.getText().toString()) ? 0 : Double.parseDouble(discountEditText.getText().toString());
        double total = subTotal - discount;

        subTotalTextView.setText(String.format("%.2f", subTotal));
        discountTextView.setText(String.format("%.2f", discount));
        totalTextView.setText(String.format("%.2f", total));
    }

    private void checkPermissions() {
        if (ContextCompat.checkSelfPermission(this, Manifest.permission.WRITE_EXTERNAL_STORAGE) != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.WRITE_EXTERNAL_STORAGE}, REQUEST_WRITE_PERMISSION);
        } else {
            exportToExcel();
        }
    }

    public void onRequestPermissionsResult(int requestCode, String[] permissions, int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults); // Call super method

        if (requestCode == REQUEST_WRITE_PERMISSION) {
            if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                exportToExcel();
            } else {
                Toast.makeText(this, "Permission denied", Toast.LENGTH_SHORT).show();
            }
        }
    }

    private void exportToExcel() {
        Workbook workbook = new XSSFWorkbook();
        Sheet sheet = workbook.createSheet("Invoices");

        // Create header row
        Row headerRow = sheet.createRow(0);
        headerRow.createCell(0).setCellValue("Invoice Number");
        headerRow.createCell(1).setCellValue("Customer");
        headerRow.createCell(2).setCellValue("Date");
        headerRow.createCell(4).setCellValue("Discount");
        headerRow.createCell(5).setCellValue("Total");

        ArrayList<Invoice> invoiceList = databaseHelper.getInvoicesForToday();
        int rowIndex = 1;
        for (Invoice invoice : invoiceList) {
            Row row = sheet.createRow(rowIndex++);
            row.createCell(0).setCellValue(invoice.getInvoiceNumber());
            row.createCell(1).setCellValue(invoice.getCustomerId());
            row.createCell(2).setCellValue(invoice.getInvoiceDate());
            row.createCell(4).setCellValue(invoice.getDiscount());
            row.createCell(5).setCellValue(invoice.getTotalAmount());
        }

        // Save the Excel file
        try {
            File file = new File(Environment.getExternalStorageDirectory(), "Invoices.xlsx");
            FileOutputStream fileOutputStream = new FileOutputStream(file);
            workbook.write(fileOutputStream);
            fileOutputStream.close();
            Toast.makeText(this, "Export successful", Toast.LENGTH_SHORT).show();
        } catch (IOException e) {
            Toast.makeText(this, "Export failed", Toast.LENGTH_SHORT).show();
        }
    }
}
