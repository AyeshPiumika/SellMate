package com.app.sellmate;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.ImageButton;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import java.util.List;

public class SalesmenAdapter extends ArrayAdapter<Salesmen> {

    private Context context;
    private List<Salesmen> salesmen;

    public SalesmenAdapter(@NonNull Context context, @NonNull List<Salesmen> salesmen) {
        super(context, 0, salesmen);
        this.context = context;
        this.salesmen = salesmen;
    }

    @NonNull
    @Override
    public View getView(int position, @Nullable View convertView, @NonNull ViewGroup parent) {
        if (convertView == null) {
            convertView = LayoutInflater.from(context).inflate(R.layout.item_salesman, parent, false);
        }

        Salesmen salesman = salesmen.get(position);

        TextView nameTextView = convertView.findViewById(R.id.salesman_name);
        TextView contactTextView = convertView.findViewById(R.id.contact_number);
        TextView nicTextView = convertView.findViewById(R.id.nic_number);
        ImageButton updateButton = convertView.findViewById(R.id.update_button);
        ImageButton deleteButton = convertView.findViewById(R.id.delete_button);

        nameTextView.setText(salesman.getName());
        contactTextView.setText(salesman.getContactNumber());
        nicTextView.setText(salesman.getNicNumber());

        updateButton.setOnClickListener(v -> {
            ((SalesmenDetails) context).showSalesmenDialog(salesman);
        });

        deleteButton.setOnClickListener(v -> {
            SalesmenDetails activity = (SalesmenDetails) context;
            activity.getDatabaseHelper().deleteSalesman(salesman);
            activity.loadSalesmen();
        });

        return convertView;
    }
}
