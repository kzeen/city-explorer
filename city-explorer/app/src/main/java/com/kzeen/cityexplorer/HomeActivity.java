package com.kzeen.cityexplorer;

import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.util.Log;
import android.widget.Toast;

import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import androidx.swiperefreshlayout.widget.SwipeRefreshLayout;

import com.android.volley.Request;
import com.android.volley.toolbox.JsonArrayRequest;
import com.google.android.material.chip.Chip;
import com.google.android.material.snackbar.Snackbar;
import com.kzeen.cityexplorer.databinding.ActivityHomeBinding;
import com.kzeen.cityexplorer.model.Place;
import com.kzeen.cityexplorer.network.VolleySingleton;
import com.kzeen.cityexplorer.ui.adapter.PlaceAdapter;

import org.json.JSONArray;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public class HomeActivity extends BaseActivity {

    @Override protected int getNavItemId() { return R.id.nav_home; }
    @Override protected int getToolbarTitleRes() { return R.string.home; }

    private final List<Place> places = new ArrayList<>();
    private PlaceAdapter adapter;
    private SwipeRefreshLayout swipe;
    private ActivityHomeBinding binding;

    @Override protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        binding = ActivityHomeBinding.inflate(getLayoutInflater());
        inflateLayout(binding.getRoot());

        List<String> categories = Arrays.asList("All", "Food", "Parks", "Shopping");
        for (String c : categories) {
            Chip chip = new Chip(this);
            chip.setText(c);
            binding.chipGroup.addView(chip);
        }
        binding.chipGroup.setOnCheckedStateChangeListener(
                (group, ids) -> filter(group.getCheckedChipId())
        );

        RecyclerView rv = binding.homeRecycler;
        adapter = new PlaceAdapter(places);
        rv.setLayoutManager(new LinearLayoutManager(this));
        rv.setAdapter(adapter);

        swipe = binding.swipe;
        swipe.setOnRefreshListener(this::loadPlaces);

        binding.fabMap.setOnClickListener(
                v -> startActivity(new Intent(Intent.ACTION_VIEW,
                        Uri.parse("geo:0,0?q=Lebanon")))
        );

        loadPlaces();
    }

    private void filter(int checkedId) {

    }

    private void loadPlaces() {
        swipe.setRefreshing(true);

        String url = "http://192.168.0.109/mock-android.json";
        JsonArrayRequest req = new JsonArrayRequest(Request.Method.GET, url, null,
                response -> {
                    parseJson(response);
                    adapter.notifyDataSetChanged();
                    swipe.setRefreshing(false);
                },
                error -> {
                    swipe.setRefreshing(false);
                    Snackbar.make(swipe, "Load failed. tap RETRY", Snackbar.LENGTH_INDEFINITE)
                            .setAction("RETRY", v -> loadPlaces())
                            .show();
                    Log.e("Volley", "Home JSON error", error);
                });
        VolleySingleton.get(this).add(req);
    }

    private void parseJson(JSONArray arr) {
        places.clear();
        for (int i = 0; i < arr.length(); i++) {
            JSONObject o = arr.optJSONObject(i);
            if (o == null) continue;
            places.add(new Place(
                    o.optString("name"),
                    o.optString("description"),
                    o.optString("image")
            ));
        }
    }
}