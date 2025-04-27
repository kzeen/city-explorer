package com.kzeen.cityexplorer.ui;

import android.os.Bundle;
import android.util.Log;

import androidx.recyclerview.widget.LinearLayoutManager;

import com.google.android.libraries.places.api.net.FetchPlaceRequest;
import com.google.android.libraries.places.api.model.Place;
import com.kzeen.cityexplorer.R;
import com.kzeen.cityexplorer.databinding.ActivityRecentsBinding;
import com.kzeen.cityexplorer.ui.adapters.PlaceAdapter;
import com.kzeen.cityexplorer.util.RecentsManager;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public class RecentsActivity extends BaseActivity {

    @Override protected int getNavItemId()     { return R.id.nav_recents; }
    @Override protected int getToolbarTitleRes(){ return R.string.recents; }
    private final List<Place> recents = new ArrayList<>();
    private PlaceAdapter adapter;

    @Override protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        ActivityRecentsBinding binding = ActivityRecentsBinding.inflate(getLayoutInflater());
        inflateLayout(binding.getRoot());

        adapter = new PlaceAdapter(recents, placesClient);
        binding.recentsRecycler.setLayoutManager(new LinearLayoutManager(this));
        binding.recentsRecycler.setAdapter(adapter);

        loadRecents();
    }

    private void loadRecents() {
        List<String> ids = RecentsManager.getRecents(this);
        if (ids.isEmpty()) {
            adapter.notifyDataSetChanged();
            return;
        }

        recents.clear();
        final int[] remaining = {ids.size()};

        for (String id : ids) {
            FetchPlaceRequest req = FetchPlaceRequest.builder(
                    id,
                    Arrays.asList(
                            Place.Field.ID,
                            Place.Field.NAME,
                            Place.Field.ADDRESS,
                            Place.Field.PHOTO_METADATAS,
                            Place.Field.RATING))
                    .build();

            placesClient.fetchPlace(req)
                    .addOnSuccessListener(r -> {
                        recents.add(r.getPlace());
                        if (--remaining[0] == 0) adapter.notifyDataSetChanged();
                    })
                    .addOnFailureListener(e -> {
                        Log.e("Recents", "fetchPlace failed", e);
                        if (--remaining[0] == 0) adapter.notifyDataSetChanged();
                    });
        }
    }
}