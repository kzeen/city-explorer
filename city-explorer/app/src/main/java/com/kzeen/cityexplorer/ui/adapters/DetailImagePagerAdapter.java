package com.kzeen.cityexplorer.ui.adapters;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;
import com.google.android.libraries.places.api.model.PhotoMetadata;
import com.google.android.libraries.places.api.net.FetchPhotoRequest;
import com.google.android.libraries.places.api.net.PlacesClient;
import com.kzeen.cityexplorer.R;

import java.util.ArrayList;
import java.util.List;

public class DetailImagePagerAdapter extends RecyclerView.Adapter<DetailImagePagerAdapter.PhotoViewHolder> {
    private final PlacesClient placesClient;
    private final List<PhotoMetadata> items = new ArrayList<>();

    public DetailImagePagerAdapter(@NonNull PlacesClient client) {
        this.placesClient = client;
    }

    @NonNull
    @Override
    public PhotoViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View v = LayoutInflater.from(parent.getContext())
                               .inflate(R.layout.item_photo, parent, false);
        return new PhotoViewHolder(v);
    }

    @Override
    public void onBindViewHolder(@NonNull PhotoViewHolder holder, int position) {
        PhotoMetadata meta = items.get(position);

        FetchPhotoRequest req = FetchPhotoRequest.builder(meta)
                .setMaxWidth(1200)
                .setMaxHeight(800)
                .build();

        placesClient.fetchPhoto(req)
                .addOnSuccessListener(response -> Glide.with(holder.itemView.getContext())
                        .load(response.getBitmap())
                        .placeholder(R.drawable.ic_placeholder)
                        .error(R.drawable.ic_error)
                        .into(holder.imageView))
                .addOnFailureListener(e -> holder.imageView
                        .setImageResource(R.drawable.ic_placeholder));
    }

    @Override
    public int getItemCount() {
        return items.size();
    }

    public void submitList(@NonNull List<PhotoMetadata> newItems) {
        items.clear();
        items.addAll(newItems);
        notifyDataSetChanged();
    }

    static class PhotoViewHolder extends RecyclerView.ViewHolder {
        final ImageView imageView;

        PhotoViewHolder(@NonNull View itemView) {
            super(itemView);
            imageView = itemView.findViewById(R.id.imagePhoto);
        }
    }
}
