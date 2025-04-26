package com.kzeen.cityexplorer;

import android.annotation.SuppressLint;
import android.content.pm.PackageManager;
import android.os.Bundle;
import android.util.Log;
import android.widget.Toast;
import android.Manifest;

import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.core.content.ContextCompat;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import androidx.swiperefreshlayout.widget.SwipeRefreshLayout;

import com.google.android.gms.location.FusedLocationProviderClient;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.tasks.Task;
import com.google.android.libraries.places.api.Places;
import com.google.android.libraries.places.api.model.PlaceLikelihood;
import com.google.android.libraries.places.api.net.FindCurrentPlaceRequest;
import com.google.android.libraries.places.api.net.FindCurrentPlaceResponse;
import com.google.android.libraries.places.api.net.PlacesClient;
import com.google.android.material.chip.Chip;
import com.google.android.material.snackbar.Snackbar;
import com.kzeen.cityexplorer.databinding.ActivityHomeBinding;
import com.google.android.libraries.places.api.model.Place;
import com.kzeen.cityexplorer.ui.adapter.PlaceAdapter;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public class HomeActivity extends BaseActivity {

    private static final String[] CITIES = {
        "Paris", "Tokyo", "New York", "Rio de Janeiro", "Sydney",
        "Cape Town", "Rome", "Barcelona", "Singapore", "Dubai",
        "Istanbul", "Toronto", "Amsterdam", "Bangkok", "Berlin",
        "Los Angeles", "Athens", "Prague", "Seoul", "Buenos Aires"
    };

    @Override protected int getNavItemId() { return R.id.nav_home; }
    @Override protected int getToolbarTitleRes() { return R.string.home; }

    private final List<Place> places = new ArrayList<>();
    private PlaceAdapter adapter;
    private SwipeRefreshLayout swipe;

    @Override protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        com.kzeen.cityexplorer.databinding.ActivityHomeBinding binding = ActivityHomeBinding.inflate(getLayoutInflater());
        inflateLayout(binding.getRoot());

        ActivityResultLauncher<String> permLauncher = registerForActivityResult(
                new ActivityResultContracts.RequestPermission(),
                granted -> {
                    if (granted) loadPlaces();
                    else
                        Toast.makeText(this, "Location permission denied", Toast.LENGTH_LONG).show();
                });


        List<String> categories = Arrays.asList("All", "Food", "Parks", "Shopping");
        for (String c : categories) {
            Chip chip = new Chip(this);
            chip.setText(c);
            binding.chipGroup.addView(chip);
        }
        binding.chipGroup.setOnCheckedStateChangeListener(
                (group, ids) -> filter(group.getCheckedChipId())
        );

        RecyclerView rv = binding.rvPlaces;
        adapter = new PlaceAdapter(places, placesClient);
        rv.setLayoutManager(new LinearLayoutManager(this));
        rv.setAdapter(adapter);

        swipe = binding.swipe;
        swipe.setOnRefreshListener(this::loadPlaces);

        if (ContextCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION)
                == PackageManager.PERMISSION_GRANTED) {
            loadPlaces();
        } else {
            permLauncher.launch(Manifest.permission.ACCESS_FINE_LOCATION);
        }
    }

    private void filter(int checkedId) {

    }

    private void loadPlaces() {
        swipe.setRefreshing(true);
        places.clear();

        List<Place.Field> fields = Arrays.asList(
                Place.Field.ID, Place.Field.NAME, Place.Field.ADDRESS,
                Place.Field.PHOTO_METADATAS, Place.Field.LAT_LNG
        );

        String randomCity = CITIES[(int) (Math.random() * CITIES.length)];
        FindCurrentPlaceRequest request = FindCurrentPlaceRequest.newInstance(fields);

        @SuppressLint("MissingPermission")
        Task<FindCurrentPlaceResponse> task = placesClient.findCurrentPlace(request);

        task.addOnSuccessListener(r -> {
            for (PlaceLikelihood pl : r.getPlaceLikelihoods()) {
                if (pl.getPlace().getAddress() != null)
                    places.add(pl.getPlace());
            }
            if (places.size() > 20) places.subList(20, places.size()).clear();
            adapter.notifyDataSetChanged();
            swipe.setRefreshing(false);
        }).addOnFailureListener(e -> {
            swipe.setRefreshing(false);
            Snackbar.make(swipe, "Load failed: " + e.getMessage(), Snackbar.LENGTH_LONG)
                    .setAction("RETRY", v -> loadPlaces())
                    .show();
            Log.e("Places", "findCurrentPlace error", e);
        });
    }
}