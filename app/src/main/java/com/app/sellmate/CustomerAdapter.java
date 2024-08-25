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

public class CustomerAdapter extends ArrayAdapter<Customer> {

    private Context context;
    private List<Customer> customers;

    public CustomerAdapter(@NonNull Context context, @NonNull List<Customer> customers) {
        super(context, 0, customers);
        this.context = context;
        this.customers = customers;
    }

    @NonNull
    @Override
    public View getView(int position, @Nullable View convertView, @NonNull ViewGroup parent) {
        if (convertView == null) {
            convertView = LayoutInflater.from(context).inflate(R.layout.item_customer, parent, false);
        }

        Customer customer = customers.get(position);

        // Find the views
        TextView existingIdTextView = convertView.findViewById(R.id.existing_id);
        TextView nameTextView = convertView.findViewById(R.id.customer_name);
        TextView contactTextView = convertView.findViewById(R.id.contact_number);
        ImageButton updateButton = convertView.findViewById(R.id.update_button);
        ImageButton deleteButton = convertView.findViewById(R.id.delete_button);

        // Bind data to the views
        existingIdTextView.setText(customer.getExistingid());
        nameTextView.setText(customer.getName());
        contactTextView.setText(customer.getContactNumber());

        // Handle the update button click
        updateButton.setOnClickListener(v -> {
            ((CustomerDetails) context).showCustomerDialog(customer);
        });

        // Handle the delete button click
        deleteButton.setOnClickListener(v -> {
            CustomerDetails activity = (CustomerDetails) context;
            activity.getDatabaseHelper().deleteCustomer(customer);
            activity.loadCustomers();
        });

        return convertView;
    }
}
