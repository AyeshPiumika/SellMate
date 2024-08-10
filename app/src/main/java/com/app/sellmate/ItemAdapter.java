package com.app.sellmate;

import android.app.AlertDialog;
import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.ImageButton;
import android.widget.TextView;
import android.widget.Toast;

import java.util.ArrayList;

public class ItemAdapter extends BaseAdapter {
    private Context context;
    private ArrayList<Item> itemList;
    private LayoutInflater inflater;
    private DatabaseHelper databaseHelper;

    public ItemAdapter(Context context, ArrayList<Item> itemList) {
        this.context = context;
        this.itemList = itemList;
        this.inflater = LayoutInflater.from(context);
        this.databaseHelper = new DatabaseHelper(context);
    }

    @Override
    public int getCount() {
        return itemList.size();
    }

    @Override
    public Object getItem(int position) {
        return itemList.get(position);
    }

    @Override
    public long getItemId(int position) {
        return position;
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        ViewHolder holder;
        if (convertView == null) {
            convertView = inflater.inflate(R.layout.item_list_row, parent, false);
            holder = new ViewHolder();
            holder.itemNameTextView = convertView.findViewById(R.id.item_name);
            holder.sellingPriceTextView = convertView.findViewById(R.id.selling_price);
            holder.editButton = convertView.findViewById(R.id.edit_button);
            holder.deleteButton = convertView.findViewById(R.id.delete_button);
            convertView.setTag(holder);
        } else {
            holder = (ViewHolder) convertView.getTag();
        }

        Item item = itemList.get(position);
        holder.itemNameTextView.setText(item.getName());
        holder.sellingPriceTextView.setText(String.valueOf(item.getSellingPrice()));

        holder.editButton.setOnClickListener(v -> {
            if (context instanceof ItemDetails) {
                ((ItemDetails) context).showItemDialog(item);
            }
        });

        holder.deleteButton.setOnClickListener(v -> {
            new AlertDialog.Builder(context)
                    .setTitle("Delete Item")
                    .setMessage("Are you sure you want to delete this item?")
                    .setPositiveButton("Yes", (dialog, which) -> {
                        databaseHelper.deleteItem(item.getId());
                        itemList.remove(position);
                        notifyDataSetChanged();
                        Toast.makeText(context, "Item deleted", Toast.LENGTH_SHORT).show();
                    })
                    .setNegativeButton("No", null)
                    .show();
        });

        return convertView;
    }

    private static class ViewHolder {
        TextView itemNameTextView;
        TextView sellingPriceTextView;
        ImageButton editButton;
        ImageButton deleteButton;
    }
}
