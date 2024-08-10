package com.app.sellmate;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ListView;

import androidx.appcompat.app.AppCompatActivity;

import java.util.ArrayList;

public class ItemSelectionActivity extends AppCompatActivity {

    private ListView itemListView;
    private DatabaseHelper databaseHelper;
    private ArrayList<Item> items;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_item_selection);

        itemListView = findViewById(R.id.item_list_view);
        databaseHelper = new DatabaseHelper(this);

        // Load items from the database
        items = databaseHelper.getAllItems();

        ItemAdapter itemAdapter = new ItemAdapter(this, items);
        itemListView.setAdapter(itemAdapter);

        itemListView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                Item selectedItem = items.get(position);
                Intent resultIntent = new Intent();
                resultIntent.putExtra("selected_item", selectedItem);
                setResult(RESULT_OK, resultIntent);
                finish();
            }
        });
    }
}
