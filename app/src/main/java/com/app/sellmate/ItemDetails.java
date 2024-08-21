package com.app.sellmate;

import android.Manifest;
import android.app.Dialog;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.database.Cursor;
import android.net.Uri;
import android.os.Bundle;
import android.util.Log;
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

public class ItemDetails extends AppCompatActivity {

    private static final int PICK_EXCEL_FILE = 1;
    private static final int REQUEST_PERMISSION = 100;

    private ListView itemListView;
    private ImageButton addButton, uploadButton, downloadButton;
    private DatabaseHelper databaseHelper;
    private ArrayList<Item> itemList;
    private ItemAdapter itemAdapter;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_item_details);

        itemListView = findViewById(R.id.item_list);
        addButton = findViewById(R.id.add_button);
        uploadButton = findViewById(R.id.upload_button);
        downloadButton = findViewById(R.id.download_button);

        databaseHelper = new DatabaseHelper(this);
        itemList = new ArrayList<>();

        addButton.setOnClickListener(v -> showItemDialog(null));
        uploadButton.setOnClickListener(v -> uploadItems());
        downloadButton.setOnClickListener(v -> downloadItems());

        loadItems();

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

    public void loadItems() {
        itemList = databaseHelper.getAllItems();
        itemAdapter = new ItemAdapter(this, itemList);
        itemListView.setAdapter(itemAdapter);
    }

    public void showItemDialog(@Nullable Item item) {
        final Dialog dialog = new Dialog(this);
        dialog.setContentView(R.layout.dialog_add_item);

        EditText editItemName = dialog.findViewById(R.id.edit_item_name);
        EditText editSellingPrice = dialog.findViewById(R.id.edit_selling_price);
        EditText editExistingId = dialog.findViewById(R.id.edit_existing_id);
        Button saveButton = dialog.findViewById(R.id.save_button);

        if (item != null) {
            editItemName.setText(item.getName());
            editSellingPrice.setText(String.valueOf(item.getSellingPrice()));
            editExistingId.setText(item.getExistingid());
        }

        saveButton.setOnClickListener(v -> {
            String name = editItemName.getText().toString();
            String sellingPriceStr = editSellingPrice.getText().toString();
            String existingId = editExistingId.getText().toString();
            double sellingPrice = 0;

            try {
                sellingPrice = Double.parseDouble(sellingPriceStr);
            } catch (NumberFormatException e) {
                Toast.makeText(ItemDetails.this, "Invalid price format", Toast.LENGTH_SHORT).show();
                return;
            }

            if (item == null) {
                Item newItem = new Item(name, sellingPrice, existingId);
                databaseHelper.addItem(newItem);
            } else {
                item.setName(name);
                item.setSellingPrice(sellingPrice);
                item.setExistingid(existingId);
                databaseHelper.updateItem(item);
            }
            loadItems();
            dialog.dismiss();
        });

        dialog.show();
    }

    private void uploadItems() {
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
                    double sellingPrice = row.getCell(1).getNumericCellValue();
                    String existingId = row.getCell(2).getStringCellValue();
                    Item item = new Item(name, sellingPrice, existingId);
                    databaseHelper.addItem(item);
                }
                loadItems();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }

    private void downloadItems() {
        Workbook workbook = new XSSFWorkbook();
        Sheet sheet = workbook.createSheet("Items");

        Row headerRow = sheet.createRow(0);
        headerRow.createCell(0).setCellValue("Item ID");
        headerRow.createCell(1).setCellValue("Item Name");
        headerRow.createCell(2).setCellValue("Selling Price");
        headerRow.createCell(3).setCellValue("Existing ID");

        int rowNum = 1;
        for (Item item : itemList) {
            Row row = sheet.createRow(rowNum++);
            row.createCell(0).setCellValue(item.getId());
            row.createCell(1).setCellValue(item.getName());
            row.createCell(2).setCellValue(item.getSellingPrice());
            row.createCell(3).setCellValue(item.getExistingid());
        }

        try {
            File file = new File(getExternalFilesDir(null), "items.xlsx");
            try (FileOutputStream outputStream = new FileOutputStream(file)) {
                workbook.write(outputStream);
            }
            workbook.close();
            Toast.makeText(this, "Item list downloaded to " + file.getAbsolutePath(), Toast.LENGTH_LONG).show();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}
