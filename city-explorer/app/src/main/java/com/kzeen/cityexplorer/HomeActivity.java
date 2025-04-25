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

    @Override protected int getNavItemId() { return R.id.nav_home; }
    @Override protected int getToolbarTitleRes() { return R.string.home; }

    private final List<Place> places = new ArrayList<>();
    private PlaceAdapter adapter;
    private SwipeRefreshLayout swipe;
    private PlacesClient placesClient;
    private FusedLocationProviderClient fusedClient;
    private ActivityResultLauncher<String> permLauncher;
    private ActivityHomeBinding binding;

    @Override protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        binding = ActivityHomeBinding.inflate(getLayoutInflater());
        inflateLayout(binding.getRoot());

        if (!Places.isInitialized()) {
            Places.initialize(getApplicationContext(), BuildConfig.MAPS_API_KEY);
        }
        placesClient = Places.createClient(this);
        fusedClient  = LocationServices.getFusedLocationProviderClient(this);
        permLauncher = registerForActivityResult(
                new ActivityResultContracts.RequestPermission(),
                granted -> {
                    if (granted) loadPlaces();
                    else Toast.makeText(this, "Location permission denied", Toast.LENGTH_LONG).show();
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
        adapter = new PlaceAdapter(places);
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

    @SuppressLint("MissingPermission")
    private void loadPlaces() {
        swipe.setRefreshing(true);

        List<Place.Field> fields = Arrays.asList(
                Place.Field.ID, Place.Field.NAME, Place.Field.ADDRESS,
                Place.Field.LAT_LNG, Place.Field.PHOTO_METADATAS
        );
        FindCurrentPlaceRequest request = FindCurrentPlaceRequest.newInstance(fields);
        Task<FindCurrentPlaceResponse> task = placesClient.findCurrentPlace(request);

        task.addOnSuccessListener(response -> {
            places.clear();
            for (PlaceLikelihood pl : response.getPlaceLikelihoods()) {
                places.add(pl.getPlace());
            }
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