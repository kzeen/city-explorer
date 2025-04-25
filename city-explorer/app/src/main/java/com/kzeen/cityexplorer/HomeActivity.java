package com.kzeen.cityexplorer;

import android.os.Bundle;
import android.util.Log;
import android.widget.Toast;

import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import androidx.swiperefreshlayout.widget.SwipeRefreshLayout;

import com.android.volley.Request;
import com.android.volley.toolbox.JsonArrayRequest;
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

    @Override protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        ActivityHomeBinding binding = ActivityHomeBinding.inflate(getLayoutInflater());
        inflateLayout(binding.getRoot());

        swipe = binding.swipe;
        RecyclerView rv = binding.homeRecycler;
        rv.setLayoutManager(new LinearLayoutManager(this));
        adapter = new PlaceAdapter(places);
        rv.setAdapter(adapter);

        swipe.setOnRefreshListener(this::loadPlaces);
        loadPlaces();
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