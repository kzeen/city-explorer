package com.kzeen.cityexplorer;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import java.util.List;

public class SimpleStringAdapter extends RecyclerView.Adapter<SimpleStringAdapter.ViewHolder> {
    private final List<String> data;

    public SimpleStringAdapter(List<String> data) {
        this.data = data;
    }

    /** Holds each row’s views */
    static class ViewHolder extends RecyclerView.ViewHolder {
        TextView label;
        ViewHolder(@NonNull View itemView) {
            super(itemView);
            label = itemView.findViewById(android.R.id.text1);
        }
    }

    @NonNull @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        // Android’s built-in 1-line layout is fine for a demo
        View v = LayoutInflater.from(parent.getContext())
                .inflate(android.R.layout.simple_list_item_1, parent, false);
        return new ViewHolder(v);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        holder.label.setText(data.get(position));
    }

    @Override
    public int getItemCount() { return data.size(); }
}
