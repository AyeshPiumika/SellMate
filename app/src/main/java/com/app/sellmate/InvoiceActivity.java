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

import org.apache.poi.hssf.usermodel.HSSFWorkbook;
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
        String salesmanId = "byref"; // Replace with logged-in salesman ID
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
                Invoice newInvoice = new Invoice(invoiceNumber, date, customer, total, discountAmount);
                long invoiceId = databaseHelper.addInvoice(newInvoice);

                // Save invoice items to database
                for (int i = 0; i < itemsContainer.getChildCount(); i++) {
                    View itemRow = itemsContainer.getChildAt(i);
                    AutoCompleteTextView itemAutoComplete = itemRow.findViewById(R.id.itemAutoComplete);
                    EditText quantityEditText = itemRow.findViewById(R.id.quantityEditText);
                    EditText unitPriceEditText = itemRow.findViewById(R.id.unitPriceEditText);
                    EditText unitDiscountEditText = itemRow.findViewById(R.id.unitDiscount);
                    TextView totalPriceTextView = itemRow.findViewById(R.id.totalPriceTextView);

                    String itemName = itemAutoComplete.getText().toString().trim();
                    int quantity = TextUtils.isEmpty(quantityEditText.getText().toString()) ? 0 : Integer.parseInt(quantityEditText.getText().toString());
                    double unitPrice = TextUtils.isEmpty(unitPriceEditText.getText().toString()) ? 0 : Double.parseDouble(unitPriceEditText.getText().toString());
                    double unitDiscount = TextUtils.isEmpty(unitDiscountEditText.getText().toString()) ? 0 : Double.parseDouble(unitDiscountEditText.getText().toString());
                    double itemTotal = TextUtils.isEmpty(totalPriceTextView.getText().toString()) ? 0 : Double.parseDouble(totalPriceTextView.getText().toString());

                    // Get the item ID from the item name
                    Item item = databaseHelper.getItemByName(itemName);
                    if (item != null) {
                        // Use item.getId() and invoiceId to create InvoiceItem
                        InvoiceItem invoiceItem = new InvoiceItem(
                                "", // Placeholder for id or generate an id if necessary
                                String.valueOf(invoiceId),
                                item.getId(),
                                quantity,
                                itemTotal
                        );
                        databaseHelper.addInvoiceItem(invoiceItem);
                    }
                }

                // Refresh the invoice list
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
        SimpleDateFormat sdfDate = new SimpleDateFormat("yyyyMMdd", Locale.getDefault());
        SimpleDateFormat sdfTime = new SimpleDateFormat("HHmmss", Locale.getDefault());
        String datePart = sdfDate.format(new Date());
        String timePart = sdfTime.format(new Date());

        return "INV-" + datePart + "-" + timePart + "-" + salesmanId;
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

    @Override
    public void onRequestPermissionsResult(int requestCode, String[] permissions, int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);

        if (requestCode == REQUEST_WRITE_PERMISSION) {
            if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                exportToExcel();
            } else {
                Toast.makeText(this, "Permission denied", Toast.LENGTH_SHORT).show();
            }
        }
    }

    // Save Excel File
    private void saveExcelFile(Context context, String fileName, Workbook workbook) throws IOException {
        FileOutputStream fileOut = null;

        try {
            // Ensure your app has WRITE_EXTERNAL_STORAGE permission
            File file = new File(context.getExternalFilesDir(null), fileName);
            fileOut = new FileOutputStream(file);
            workbook.write(fileOut);
            Toast.makeText(context, "Excel file saved: " + file.getAbsolutePath(), Toast.LENGTH_LONG).show();
        } catch (IOException e) {
            throw new IOException("Error saving Excel file", e);
        } finally {
            if (fileOut != null) {
                fileOut.close();
            }
        }
    }

    private void exportToExcel() {
        Workbook workbook = new XSSFWorkbook();

        // Create Invoice Sheet
        Sheet invoiceSheet = workbook.createSheet("Invoices");
        Row invoiceHeaderRow = invoiceSheet.createRow(0);
        invoiceHeaderRow.createCell(0).setCellValue("ID");
        invoiceHeaderRow.createCell(1).setCellValue("Invoice Number");
        invoiceHeaderRow.createCell(2).setCellValue("Customer Name");
        invoiceHeaderRow.createCell(3).setCellValue("Invoice Date");
        invoiceHeaderRow.createCell(4).setCellValue("Total Amount");
        invoiceHeaderRow.createCell(5).setCellValue("Discount");

        // Create Invoice Items Sheet
        Sheet invoiceItemSheet = workbook.createSheet("Invoice Items");
        Row itemHeaderRow = invoiceItemSheet.createRow(0);
        itemHeaderRow.createCell(0).setCellValue("ID");
        itemHeaderRow.createCell(1).setCellValue("Invoice Number");
        itemHeaderRow.createCell(2).setCellValue("Item Name");
        itemHeaderRow.createCell(3).setCellValue("Quantity");
        itemHeaderRow.createCell(4).setCellValue("Item Total");

        try {
            ArrayList<Invoice> invoiceList = databaseHelper.getInvoicesForToday();
            int invoiceRowIndex = 1;
            int itemRowIndex = 1;

            for (Invoice invoice : invoiceList) {

                // Populate Invoice Sheet
                Row invoiceRow = invoiceSheet.createRow(invoiceRowIndex++);
                invoiceRow.createCell(0).setCellValue(invoice.getId());
                invoiceRow.createCell(1).setCellValue(invoice.getInvoiceNumber());
                invoiceRow.createCell(2).setCellValue(invoice.getCustomerId());
                invoiceRow.createCell(3).setCellValue(invoice.getInvoiceDate());
                invoiceRow.createCell(4).setCellValue(invoice.getTotalAmount());
                invoiceRow.createCell(5).setCellValue(invoice.getDiscount());

                ArrayList<InvoiceItem> invoiceItems = databaseHelper.getInvoiceItems(invoice.getId());

                for (InvoiceItem item : invoiceItems) {

                    // Populate Invoice Items Sheet
                    Row itemRow = invoiceItemSheet.createRow(itemRowIndex++);
                    itemRow.createCell(0).setCellValue(item.getId());
                    itemRow.createCell(1).setCellValue(invoice.getInvoiceNumber());
                    itemRow.createCell(2).setCellValue(item.getItemId());
                    itemRow.createCell(3).setCellValue(item.getQuantity());
                    itemRow.createCell(4).setCellValue(item.getItemTotal());
                }
            }

            // Save the Excel file to the device
            saveExcelFile(this, "Invoices.xlsx", workbook);
            Toast.makeText(this, "Invoices and items exported successfully.", Toast.LENGTH_SHORT).show();

        } catch (IOException e) {
            e.printStackTrace();
            Toast.makeText(this, "Error occurred while exporting invoices: " + e.getMessage(), Toast.LENGTH_LONG).show();
        } catch (Exception e) {
            e.printStackTrace();
            Toast.makeText(this, "An unexpected error occurred: " + e.getMessage(), Toast.LENGTH_LONG).show();
        } finally {
            try {
                workbook.close();
            } catch (IOException e) {
                e.printStackTrace();
                Toast.makeText(this, "Error occurred while closing the workbook: " + e.getMessage(), Toast.LENGTH_LONG).show();
            }
        }
    }

}
