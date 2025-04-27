package com.kzeen.cityexplorer.ui.adapters;

import android.content.Intent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;
import com.kzeen.cityexplorer.BuildConfig;
import com.kzeen.cityexplorer.R;
import com.kzeen.cityexplorer.databinding.RowPlaceBinding;
import com.kzeen.cityexplorer.models.NearbyPlace;
import com.kzeen.cityexplorer.ui.placedetail.PlaceDetailActivity;

import java.util.List;
import java.util.Locale;

public class NearbyPlaceAdapter extends RecyclerView.Adapter<NearbyPlaceAdapter.ViewHolder> {
    private final List<NearbyPlace> data;

    public NearbyPlaceAdapter(@NonNull List<NearbyPlace> data) {
        this.data = data;
    }

    public static class ViewHolder extends RecyclerView.ViewHolder {
        final RowPlaceBinding binding;
        public ViewHolder(RowPlaceBinding b) {
            super(b.getRoot());
            binding = b;
        }
    }

    @NonNull @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        RowPlaceBinding b = RowPlaceBinding.inflate(
                LayoutInflater.from(parent.getContext()), parent, false);
        return new ViewHolder(b);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder h,int pos) {
        NearbyPlace np = data.get(pos);
        h.binding.rowTitle.setText(   np.getName()   );
        h.binding.rowSubtitle.setText(np.getAddress());

        Float r = np.getRating() == null ? 0f : np.getRating();

        if (r > 0f) {
            h.binding.rowRating.setVisibility(View.VISIBLE);
            h.binding.rowRating.setRating(r);
        } else {
            h.binding.rowRating.setVisibility(View.GONE);
        }

        String ref = np.getPhotoRef();
        if (ref != null) {
            String url = String.format(
                    Locale.US,
                    "https://maps.googleapis.com/maps/api/place/photo" +
                            "?maxwidth=400" +
                            "&photoreference=%s" +
                            "&key=%s",
                    ref,
                    BuildConfig.MAPS_API_KEY
            );
            Glide.with(h.itemView.getContext())
                    .load(url)
                    .placeholder(R.drawable.ic_placeholder)
                    .error(R.drawable.ic_error)
                    .into(h.binding.rowThumbnail);
        } else {
            Glide.with(h.itemView.getContext())
                    .load(R.drawable.ic_placeholder)
                    .into(h.binding.rowThumbnail);
        }

        h.itemView.setOnClickListener(v -> {
            Intent i = new Intent(v.getContext(), PlaceDetailActivity.class);
            i.putExtra(PlaceDetailActivity.EXTRA_PLACE_ID, np.getId());
            v.getContext().startActivity(i);
        });
    }

    @Override public int getItemCount() { return data.size(); }
}
