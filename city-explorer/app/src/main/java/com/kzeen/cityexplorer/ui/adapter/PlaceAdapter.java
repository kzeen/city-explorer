package com.kzeen.cityexplorer.ui.adapter;

import android.view.LayoutInflater;
import android.view.ViewGroup;

import com.bumptech.glide.Glide;
import com.kzeen.cityexplorer.R;
import com.kzeen.cityexplorer.databinding.RowPlaceBinding;
import com.kzeen.cityexplorer.model.Place;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import java.util.List;

public class PlaceAdapter extends RecyclerView.Adapter<PlaceAdapter.ViewHolder> {
    private final List<Place> data;
    public PlaceAdapter(List<Place> data) { this.data = data; }

    public static class ViewHolder extends RecyclerView.ViewHolder {
        final RowPlaceBinding binding;
        public ViewHolder(RowPlaceBinding binding) {
            super(binding.getRoot());
            this.binding = binding;
        }
    }

    @NonNull
    @Override public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        RowPlaceBinding binding = RowPlaceBinding.inflate(
                LayoutInflater.from(parent.getContext()), parent, false);
        return new ViewHolder(binding);
    }

    @Override public void onBindViewHolder(@NonNull ViewHolder h, int pos) {
        Place p = data.get(pos);
        h.binding.rowTitle.setText(p.name);
        h.binding.rowSubtitle.setText(p.description);

        Glide.with(h.itemView)
                .load(p.imageURL)
                .placeholder(R.drawable.ic_placeholder)
                .error(R.drawable.ic_error)
                .into(h.binding.rowThumbnail);
    }

    @Override public int getItemCount() { return data.size(); }
}
