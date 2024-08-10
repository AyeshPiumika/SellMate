package com.app.sellmate;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import java.util.ArrayList;

public class SummaryAdapter extends RecyclerView.Adapter<SummaryAdapter.ViewHolder> {

    private ArrayList<Invoice> invoiceList;

    public SummaryAdapter(ArrayList<Invoice> invoiceList) {
        this.invoiceList = invoiceList;
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_summary_report, parent, false);
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        Invoice invoice = invoiceList.get(position);
        holder.textViewInvoiceNumber.setText("Invoice Number: " + invoice.getInvoiceNumber());
        holder.textViewInvoiceDate.setText("Date: " + invoice.getInvoiceDate());
        holder.textViewTotalAmount.setText("Total Amount: " + String.format("%.2f", invoice.getTotalAmount()));
        holder.textViewDiscount.setText("Discount: " + String.format("%.2f", invoice.getDiscount()));
    }

    @Override
    public int getItemCount() {
        return invoiceList.size();
    }

    public static class ViewHolder extends RecyclerView.ViewHolder {
        TextView textViewInvoiceNumber, textViewInvoiceDate, textViewTotalAmount, textViewDiscount;

        public ViewHolder(@NonNull View itemView) {
            super(itemView);
            textViewInvoiceNumber = itemView.findViewById(R.id.textViewInvoiceNumber);
            textViewInvoiceDate = itemView.findViewById(R.id.textViewInvoiceDate);
            textViewTotalAmount = itemView.findViewById(R.id.textViewTotalAmount);
            textViewDiscount = itemView.findViewById(R.id.textViewDiscount);
        }
    }
}
