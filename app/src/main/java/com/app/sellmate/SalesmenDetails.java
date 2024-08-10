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

public class SalesmenDetails extends AppCompatActivity {

    private static final int PICK_EXCEL_FILE = 1;
    private static final int REQUEST_PERMISSION = 100;

    private ListView salesmenListView;
    private ImageButton addButton, uploadButton, downloadButton;
    private DatabaseHelper databaseHelper;
    private ArrayList<Salesmen> salesmenList;
    private SalesmenAdapter salesmenAdapter;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_salesmen_details);

        salesmenListView = findViewById(R.id.salesmen_list);
        addButton = findViewById(R.id.add_button);
        uploadButton = findViewById(R.id.upload_button);
        downloadButton = findViewById(R.id.download_button);

        databaseHelper = new DatabaseHelper(this);
        salesmenList = new ArrayList<>();

        addButton.setOnClickListener(v -> showSalesmenDialog(null));
        uploadButton.setOnClickListener(v -> uploadSalesmen());
        downloadButton.setOnClickListener(v -> downloadSalesmen());

        loadSalesmen();

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

    public void loadSalesmen() {
        salesmenList = databaseHelper.getAllSalesmen();
        salesmenAdapter = new SalesmenAdapter(this, salesmenList);
        salesmenListView.setAdapter(salesmenAdapter);
    }

    public void showSalesmenDialog(@Nullable Salesmen salesmen) {
        final Dialog dialog = new Dialog(this);
        dialog.setContentView(R.layout.dialog_add_salesmen);

        EditText editSalesmenName = dialog.findViewById(R.id.edit_salesman_name);
        EditText editContactNumber = dialog.findViewById(R.id.edit_contact_number);
        EditText editNICNumber = dialog.findViewById(R.id.edit_nic_number);
        Button saveButton = dialog.findViewById(R.id.save_button);

        if (salesmen != null) {
            editSalesmenName.setText(salesmen.getName());
            editContactNumber.setText(salesmen.getContactNumber());
            editNICNumber.setText(salesmen.getNicNumber());
        }

        saveButton.setOnClickListener(v -> {
            String name = editSalesmenName.getText().toString();
            String contactNumber = editContactNumber.getText().toString();
            String nicNumber = editNICNumber.getText().toString();

            if (salesmen == null) {
                Salesmen newSalesmen = new Salesmen(name, contactNumber, nicNumber);
                databaseHelper.addSalesman(newSalesmen);
            } else {
                salesmen.setName(name);
                salesmen.setContactNumber(contactNumber);
                salesmen.setNicNumber(nicNumber);
                databaseHelper.updateSalesman(salesmen);
            }
            loadSalesmen();
            dialog.dismiss();
        });

        dialog.show();
    }

    public DatabaseHelper getDatabaseHelper() {
        return databaseHelper;
    }

    private void uploadSalesmen() {
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
                    String nicNumber = row.getCell(2).getStringCellValue();
                    Salesmen salesmen = new Salesmen(name, contactNumber, nicNumber);
                    databaseHelper.addSalesman(salesmen);
                }
                loadSalesmen();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }

    private void downloadSalesmen() {
        Workbook workbook = new XSSFWorkbook();
        Sheet sheet = workbook.createSheet("Salesmen");

        Row headerRow = sheet.createRow(0);
        headerRow.createCell(0).setCellValue("Salesmen Name");
        headerRow.createCell(1).setCellValue("Contact Number");
        headerRow.createCell(2).setCellValue("NIC Number");

        int rowNum = 1;
        for (Salesmen salesmen : salesmenList) {
            Row row = sheet.createRow(rowNum++);
            row.createCell(0).setCellValue(salesmen.getName());
            row.createCell(1).setCellValue(salesmen.getContactNumber());
            row.createCell(2).setCellValue(salesmen.getNicNumber());
        }

        try {
            File file = new File(getExternalFilesDir(null), "salesmen.xlsx");
            try (FileOutputStream outputStream = new FileOutputStream(file)) {
                workbook.write(outputStream);
                workbook.close();
            }
            Toast.makeText(this, "Salesmen list downloaded to " + file.getAbsolutePath(), Toast.LENGTH_LONG).show();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}
