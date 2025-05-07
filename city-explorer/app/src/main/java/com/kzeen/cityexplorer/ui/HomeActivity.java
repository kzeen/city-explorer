package com.kzeen.cityexplorer.ui;

import android.annotation.SuppressLint;
import android.content.pm.PackageManager;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Toast;
import android.Manifest;

import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.core.content.ContextCompat;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.swiperefreshlayout.widget.SwipeRefreshLayout;

import com.android.volley.Request;
import com.android.volley.toolbox.JsonObjectRequest;
import com.google.android.gms.location.FusedLocationProviderClient;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.tasks.Task;
import com.google.android.libraries.places.api.model.PlaceLikelihood;
import com.google.android.libraries.places.api.net.FindCurrentPlaceRequest;
import com.google.android.libraries.places.api.net.FindCurrentPlaceResponse;
import com.google.android.material.chip.Chip;
import com.google.android.material.snackbar.Snackbar;
import com.kzeen.cityexplorer.BuildConfig;
import com.kzeen.cityexplorer.R;
import com.kzeen.cityexplorer.databinding.ActivityHomeBinding;
import com.google.android.libraries.places.api.model.Place;
import com.kzeen.cityexplorer.models.NearbyPlace;
import com.kzeen.cityexplorer.network.VolleySingleton;
import com.kzeen.cityexplorer.ui.adapters.NearbyPlaceAdapter;
import com.kzeen.cityexplorer.ui.adapters.PlaceAdapter;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Locale;

public class HomeActivity extends BaseActivity {
    @Override protected int getNavItemId() { return R.id.nav_home; }
    @Override protected int getToolbarTitleRes() { return R.string.home; }

    private final List<Place> places = new ArrayList<>();
    private final List<NearbyPlace> nearbyPlaces = new ArrayList<>();

    private PlaceAdapter adapter;
    private NearbyPlaceAdapter httpAdapter;
    private SwipeRefreshLayout swipe;
    private ActivityHomeBinding binding;
    private FusedLocationProviderClient fusedClient;
    private double lastLat, lastLng;


    @SuppressLint("MissingPermission")
    @Override protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        binding = ActivityHomeBinding.inflate(getLayoutInflater());
        inflateLayout(binding.getRoot());

        fusedClient = LocationServices.getFusedLocationProviderClient(this);

        ActivityResultLauncher<String> permLauncher = registerForActivityResult(
                new ActivityResultContracts.RequestPermission(),
                granted -> {
                    if (granted) {
                        fusedClient.getLastLocation()
                                .addOnSuccessListener(this, location -> {
                                    if (location != null) {
                                        lastLat = location.getLatitude();
                                        lastLng = location.getLongitude();
                                    }
                                    loadPlaces();
                                });
                    } else
                        Toast.makeText(this, "Location permission denied", Toast.LENGTH_LONG).show();
                });


        List<String> categories = Arrays.asList("All", "Food", "Parks", "Shopping", "Parking");
        binding.chipGroup.setSingleSelection(true);
        for (String c : categories) {
            Chip chip = new Chip(this);
            chip.setCheckable(true);
            chip.setId(View.generateViewId());
            chip.setText(c);
            binding.chipGroup.addView(chip);
        }

        binding.chipGroup.setOnCheckedStateChangeListener(
                (group, ids) -> loadPlaces()
        );

        adapter = new PlaceAdapter(places, placesClient);
        httpAdapter = new NearbyPlaceAdapter(nearbyPlaces);
        binding.rvPlaces.setLayoutManager(new LinearLayoutManager(this));
        binding.rvPlaces.setAdapter(adapter);

        swipe = binding.swipe;
        swipe.setOnRefreshListener(this::loadPlaces);

        if (ContextCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION)
                == PackageManager.PERMISSION_GRANTED) {
            fusedClient.getLastLocation()
                    .addOnSuccessListener(this, location -> {
                        if (location != null) {
                            lastLat = location.getLatitude();
                            lastLng = location.getLongitude();
                        }
                        loadPlaces();
                    });
        } else {
            permLauncher.launch(Manifest.permission.ACCESS_FINE_LOCATION);
        }
    }

    private void loadPlaces() {
        swipe.setRefreshing(true);

        int checkedId = binding.chipGroup.getCheckedChipId();
        String category = "All";
        if (checkedId != View.NO_ID) {
            category = ((Chip)binding.chipGroup.findViewById(checkedId)).getText().toString();
        }

        if ("All".equals(category)) {
            places.clear();
            adapter.notifyDataSetChanged();
            List<Place.Field> fields = Arrays.asList(
                    Place.Field.ID,
                    Place.Field.NAME,
                    Place.Field.ADDRESS,
                    Place.Field.PHOTO_METADATAS,
                    Place.Field.LAT_LNG,
                    Place.Field.RATING
            );
            FindCurrentPlaceRequest req = FindCurrentPlaceRequest.newInstance(fields);
            @SuppressLint("MissingPermission")
            Task<FindCurrentPlaceResponse> task = placesClient.findCurrentPlace(req);
            task.addOnSuccessListener(r -> {
                for (PlaceLikelihood pl : r.getPlaceLikelihoods()) {
                    Place p = pl.getPlace();
                    if (p.getAddress() == null) continue;
                    if (p.getPhotoMetadatas() == null || p.getPhotoMetadatas().isEmpty()) continue;

                    places.add(p);
                }
                if (places.size() > 20)
                    places.subList(20, places.size()).clear();
                adapter.notifyDataSetChanged();
                swipe.setRefreshing(false);
            }).addOnFailureListener(e -> {
                swipe.setRefreshing(false);
                Snackbar.make(swipe, "Load failed: " + e.getMessage(),
                                Snackbar.LENGTH_LONG)
                        .setAction("RETRY", v -> loadPlaces())
                        .show();
                Log.e("Places", "findCurrentPlace error", e);
            });

            binding.rvPlaces.setAdapter(adapter);
        } else {
            nearbyPlaces.clear();
            httpAdapter.notifyDataSetChanged();
            binding.rvPlaces.setAdapter(httpAdapter);
            fetchNearbyByType(category);
        }
    }

    private void fetchNearbyByType(String category) {
        String typeParam = "";
        switch (category) {
            case "Food": typeParam = "restaurant"; break;
            case "Parks": typeParam = "park"; break;
            case "Shopping": typeParam = "shopping_mall"; break;
            case "Parking": typeParam = "parking"; break;
        }
        String url = String.format(Locale.US,
                "https://maps.googleapis.com/maps/api/place/nearbysearch/json?" +
                        "location=%f,%f&radius=1500&type=%s&key=%s",
                lastLat, lastLng, typeParam,
                BuildConfig.MAPS_API_KEY);

        JsonObjectRequest req = new JsonObjectRequest(
                Request.Method.GET, url, null,
                this::parseNearbyJson,
                error -> {
                    swipe.setRefreshing(false);
                    Snackbar.make(swipe, "Nearby search failed: " + error.getMessage(), Snackbar.LENGTH_LONG)
                            .setAction("RETRY", v -> loadPlaces())
                            .show();
                });
        VolleySingleton.getInstance(this).addToRequestQueue(req);
    }
    private void parseNearbyJson(JSONObject json) {
        nearbyPlaces.clear();
        try {
            JSONArray arr = json.getJSONArray("results");
            for (int i = 0; i < arr.length(); i++) {
                JSONObject o = arr.getJSONObject(i);
                String pid = o.optString("place_id", "");
                String pname = o.optString("name",     "Unnamed");
                String pavn = o.optString("vicinity","No address");
                String ref = null;

                JSONArray photos = o.optJSONArray("photos");
                if (photos != null && photos.length() > 0)  {
                    ref = photos.optJSONObject(0).optString("photo_reference", null);
                }

                Float rating = null;
                if (o.has("rating") && !o.isNull("rating")) {
                    rating = (float) o.optDouble("rating", 0.0);
                }

                NearbyPlace np = new NearbyPlace(pid, pname, pavn, ref, rating);
                nearbyPlaces.add(np);
            }
        } catch (JSONException e) {
            Log.e("Places", "parseNearbyJson error", e);
        }

        httpAdapter.notifyDataSetChanged();
        swipe.setRefreshing(false);
    }
}