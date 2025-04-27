package com.kzeen.cityexplorer.ui.adapter;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.content.Intent;

import com.bumptech.glide.Glide;
import com.kzeen.cityexplorer.R;
import com.kzeen.cityexplorer.databinding.RowPlaceBinding;

import com.google.android.libraries.places.api.model.PhotoMetadata;
import com.google.android.libraries.places.api.model.Place;
import com.google.android.libraries.places.api.net.FetchPhotoRequest;
import com.google.android.libraries.places.api.net.PlacesClient;

import com.kzeen.cityexplorer.ui.detail.PlaceDetailActivity;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import java.util.List;

public class PlaceAdapter extends RecyclerView.Adapter<PlaceAdapter.ViewHolder> {
    private final List<Place> data;
    private final PlacesClient placesClient;
    public PlaceAdapter(List<Place> data, PlacesClient client) {
        this.data = data;
        this.placesClient = client;
    }

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
        h.binding.rowTitle.setText(p.getName());
        h.binding.rowSubtitle.setText(p.getAddress());

        Float rating = p.getRating() == null ? 0f : p.getRating().floatValue();

        if (rating > 0f) {
            h.binding.rowRating.setVisibility(View.VISIBLE);
            h.binding.rowRating.setRating(rating);
        } else {
            h.binding.rowRating.setVisibility(View.GONE);
        }

        h.itemView.setOnClickListener(v -> {
            Intent i = new Intent(v.getContext(), PlaceDetailActivity.class);
            i.putExtra(PlaceDetailActivity.EXTRA_PLACE_ID, p.getId());
            v.getContext().startActivity(i);
        });

        List<PhotoMetadata> meta = p.getPhotoMetadatas();
        if (meta != null && !meta.isEmpty()) {
            FetchPhotoRequest req = FetchPhotoRequest.builder(meta.get(0)).build();
            placesClient.fetchPhoto(req).addOnSuccessListener(r ->
                    Glide.with(h.itemView.getContext().getApplicationContext())
                            .load(r.getBitmap())
                            .placeholder(R.drawable.ic_placeholder)
                            .error(R.drawable.ic_error)
                            .into(h.binding.rowThumbnail));
        } else {
            Glide.with(h.itemView.getContext().getApplicationContext())
                    .load(R.drawable.ic_placeholder)
                    .into(h.binding.rowThumbnail);
        }
    }

    @Override public int getItemCount() { return data.size(); }
}
