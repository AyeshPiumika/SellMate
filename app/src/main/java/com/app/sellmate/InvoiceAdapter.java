package com.app.sellmate;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.TextView;
import java.util.ArrayList;

public class InvoiceAdapter extends ArrayAdapter<String> {

    public InvoiceAdapter(Context context, ArrayList<String> invoices) {
        super(context, 0, invoices);
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        String invoice = getItem(position);

        if (convertView == null) {
            convertView = LayoutInflater.from(getContext()).inflate(android.R.layout.simple_list_item_1, parent, false);
        }

        TextView textView = convertView.findViewById(android.R.id.text1);
        textView.setText(invoice);

        return convertView;
    }
}