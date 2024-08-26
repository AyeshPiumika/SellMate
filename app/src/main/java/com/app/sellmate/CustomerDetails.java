package com.app.sellmate;

import android.Manifest;
import android.app.Dialog;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.database.Cursor;
import android.net.Uri;
import android.os.Bundle;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.ListView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;

import org.apache.poi.ss.usermodel.Row;
import org.apache.poi.ss.usermodel.Sheet;
import org.apache.poi.ss.usermodel.Workbook;
import org.apache.poi.ss.usermodel.WorkbookFactory;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;

public class CustomerDetails extends AppCompatActivity {

    private static final int PICK_EXCEL_FILE = 1;
    private static final int REQUEST_PERMISSION = 100;

    private ListView customerListView;
    private ImageButton addButton, uploadButton, downloadButton;
    private DatabaseHelper databaseHelper;
    private ArrayList<Customer> customerList;
    private CustomerAdapter customerAdapter;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_customer_details);

        customerListView = findViewById(R.id.customer_list);
        addButton = findViewById(R.id.add_button);
        uploadButton = findViewById(R.id.upload_button);
        downloadButton = findViewById(R.id.download_button);

        databaseHelper = new DatabaseHelper(this);
        customerList = new ArrayList<>();

        addButton.setOnClickListener(v -> showCustomerDialog(null));
        uploadButton.setOnClickListener(v -> uploadCustomers());
        downloadButton.setOnClickListener(v -> downloadCustomers());

        loadCustomers();

        if (ContextCompat.checkSelfPermission(this, Manifest.permission.READ_EXTERNAL_STORAGE) != PackageManager.PERMISSION_GRANTED ||
                ContextCompat.checkSelfPermission(this, Manifest.permission.WRITE_EXTERNAL_STORAGE) != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.READ_EXTERNAL_STORAGE, Manifest.permission.WRITE_EXTERNAL_STORAGE}, REQUEST_PERMISSION);
        }
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        if (requestCode == REQUEST_PERMISSION) {
            if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                // Permission granted
            } else {
                Toast.makeText(this, "Permission denied", Toast.LENGTH_SHORT).show();
            }
        }
    }

    public void loadCustomers() {
        customerList = databaseHelper.getAllCustomers();
        customerAdapter = new CustomerAdapter(this, customerList);
        customerListView.setAdapter(customerAdapter);
    }

    public void showCustomerDialog(@Nullable Customer customer) {
        final Dialog dialog = new Dialog(this);
        dialog.setContentView(R.layout.dialog_add_customer);

        EditText editCustomerName = dialog.findViewById(R.id.edit_customer_name);
        EditText editContactNumber = dialog.findViewById(R.id.edit_contact_number);
        EditText editContactPersonName = dialog.findViewById(R.id.edit_contact_person_name);
        EditText editAddress = dialog.findViewById(R.id.edit_address);
        EditText editExistingId = dialog.findViewById(R.id.edit_existing_id);
        Button saveButton = dialog.findViewById(R.id.save_button);

        if (customer != null) {
            editCustomerName.setText(customer.getName());
            editContactNumber.setText(customer.getContactNumber());
            editContactPersonName.setText(customer.getContactPersonName());
            editAddress.setText(customer.getAddress());
            editExistingId.setText(customer.getExistingid());
        }

        saveButton.setOnClickListener(v -> {
            String name = editCustomerName.getText().toString();
            String contactNumber = editContactNumber.getText().toString();
            String contactPersonName = editContactPersonName.getText().toString();
            String address = editAddress.getText().toString();
            String existingid = editExistingId.getText().toString();

            if (customer == null) {
                Customer newCustomer = new Customer(name, contactNumber, contactPersonName, address, existingid);
                databaseHelper.addCustomer(newCustomer);
            } else {
                customer.setName(name);
                customer.setContactNumber(contactNumber);
                customer.setContactPersonName(contactPersonName);
                customer.setAddress(address);
                customer.setExistingid(existingid);
                databaseHelper.updateCustomer(customer);
            }
            loadCustomers();
            dialog.dismiss();
        });

        dialog.show();
    }

    public DatabaseHelper getDatabaseHelper() {
        return databaseHelper;
    }

    private void uploadCustomers() {
        Intent intent = new Intent(Intent.ACTION_GET_CONTENT);
        intent.setType("application/vnd.openxmlformats-officedocument.spreadsheetml.sheet");
        startActivityForResult(intent, PICK_EXCEL_FILE);
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == PICK_EXCEL_FILE && resultCode == RESULT_OK && data != null) {
            Uri fileUri = data.getData();
            try (InputStream inputStream = getContentResolver().openInputStream(fileUri);
                 Workbook workbook = WorkbookFactory.create(inputStream)) {
                Sheet sheet = workbook.getSheetAt(0);
                for (Row row : sheet) {
                    if (row.getRowNum() == 0) continue; // Skip header row
                    String name = row.getCell(0).getStringCellValue();
                    String contactNumber = row.getCell(1).getStringCellValue();
                    String contactPersonName = row.getCell(2).getStringCellValue();
                    String address = row.getCell(3).getStringCellValue();
                    String existingid = row.getCell(4).getStringCellValue();  // Read existingid
                    Customer customer = new Customer(name, contactNumber, contactPersonName, address, existingid);
                    databaseHelper.addCustomer(customer);
                }
                loadCustomers();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }

    private void downloadCustomers() {
        Workbook workbook = new XSSFWorkbook();
        Sheet sheet = workbook.createSheet("Customers");

        Row headerRow = sheet.createRow(0);
        headerRow.createCell(0).setCellValue("Database ID");
        headerRow.createCell(1).setCellValue("Customer Name");
        headerRow.createCell(2).setCellValue("Contact Number");
        headerRow.createCell(3).setCellValue("Contact Person Name");
        headerRow.createCell(4).setCellValue("Address");
        headerRow.createCell(5).setCellValue("Existing ID");

        int rowNum = 1;
        for (Customer customer : customerList) {
            Row row = sheet.createRow(rowNum++);
            row.createCell(0).setCellValue(customer.getId());
            row.createCell(1).setCellValue(customer.getName());
            row.createCell(2).setCellValue(customer.getContactNumber());
            row.createCell(3).setCellValue(customer.getContactPersonName());
            row.createCell(4).setCellValue(customer.getAddress());
            row.createCell(5).setCellValue(customer.getExistingid());
        }

        try {
            File file = new File(getExternalFilesDir(null), "customers.xlsx");
            try (FileOutputStream outputStream = new FileOutputStream(file)) {
                workbook.write(outputStream);
                workbook.close();
            }
            Toast.makeText(this, "Customer list downloaded to " + file.getAbsolutePath(), Toast.LENGTH_LONG).show();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}
