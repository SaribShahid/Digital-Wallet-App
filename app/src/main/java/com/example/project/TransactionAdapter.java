package com.example.project;

import android.content.Context;
import android.graphics.Color;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import java.util.Date;
import java.util.List;

public class TransactionAdapter
        extends RecyclerView.Adapter<TransactionAdapter.ViewHolder> {

    Context context;
    List<TransactionModel> list;

    public TransactionAdapter(Context context, List<TransactionModel> list) {
        this.context = context;
        this.list = list;
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(
            @NonNull ViewGroup parent,
            int viewType) {

        View view = LayoutInflater.from(context)
                .inflate(R.layout.item_transaction, parent, false);
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(
            @NonNull ViewHolder holder,
            int position) {

        TransactionModel t = list.get(position);

        // 👤 Name
        holder.tvName.setText(t.otherParty);

        // 📄 Description
        if ("received".equals(t.type)) {
            holder.tvDesc.setText("Money received");
            holder.tvAmount.setText("+ Rs. " + (int) t.amount);
            holder.tvAmount.setTextColor(Color.parseColor("#2ECC71"));
        } else {
            holder.tvDesc.setText("Money sent");
            holder.tvAmount.setText("- Rs. " + (int) t.amount);
            holder.tvAmount.setTextColor(Color.parseColor("#E74C3C"));
        }

        // 📅 Date header
        String currentDate = formatDate(t.timestamp);

        if (position == 0 ||
                !currentDate.equals(
                        formatDate(list.get(position - 1).timestamp))) {

            holder.tvDateHeader.setVisibility(View.VISIBLE);
            holder.tvDateHeader.setText(currentDate);
        } else {
            holder.tvDateHeader.setVisibility(View.GONE);
        }
    }

    private String formatDate(long time) {
        return android.text.format.DateFormat
                .format("dd MMMM yyyy", new Date(time))
                .toString()
                .toUpperCase();
    }

    @Override
    public int getItemCount() {
        return list == null ? 0 : list.size();
    }

    static class ViewHolder extends RecyclerView.ViewHolder {

        TextView tvDateHeader, tvName, tvDesc, tvAmount;

        ViewHolder(View itemView) {
            super(itemView);
            tvDateHeader = itemView.findViewById(R.id.tvDateHeader);
            tvName = itemView.findViewById(R.id.tvName);
            tvDesc = itemView.findViewById(R.id.tvDesc);
            tvAmount = itemView.findViewById(R.id.tvAmount);
        }
    }
}
