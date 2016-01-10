package com.indiahacks16.fintech.qrmoney;

import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import java.util.ArrayList;

public class HistoryRecyclerAdapter extends RecyclerView.Adapter<HistoryRecyclerAdapter.CustomViewHolder>{
    ArrayList<Transaction> history;

    public HistoryRecyclerAdapter(ArrayList<Transaction> historyItems) {
        this.history = historyItems;
    }

    @Override
    public CustomViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.history_item, null);
        CustomViewHolder viewHolder = new CustomViewHolder(view);
        return viewHolder;
    }

    @Override
    public void onBindViewHolder(CustomViewHolder holder, int position) {
        Transaction item = history.get(position);
        holder.receiver.setText("TO : " + item.getReceiver());
        holder.date_time.setText("DATE : " + item.getDate_time());
        holder.amount.setText("Rs. " + item.getAmount() + "");
    }

    @Override
    public int getItemCount() {
        return history.size();
    }

    public class CustomViewHolder extends RecyclerView.ViewHolder {
        protected TextView receiver, date_time, amount;
        public CustomViewHolder(View view) {
            super(view);
            this.receiver = (TextView) view.findViewById(R.id.history_receiver);
            this.date_time = (TextView) view.findViewById(R.id.history_date_time);
            this.amount = (TextView) view.findViewById(R.id.history_amount);
        }
    }
}
