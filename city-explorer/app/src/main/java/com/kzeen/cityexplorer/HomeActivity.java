package com.kzeen.cityexplorer;

import android.os.Bundle;
import android.util.Log;
import android.widget.Toast;

import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.android.volley.Request;
import com.android.volley.toolbox.JsonArrayRequest;
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

    @Override protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        inflateLayout(R.layout.activity_home);

        RecyclerView rv = findViewById(R.id.home_recycler);
        rv.setLayoutManager(new LinearLayoutManager(this));
        adapter = new PlaceAdapter(places);
        rv.setAdapter(adapter);

        loadPlaces();
    }

    private void loadPlaces() {
        String url = "http://192.168.0.109/mock-android.json";
        JsonArrayRequest req = new JsonArrayRequest(Request.Method.GET, url, null,
                response -> {
                    parseJson(response);
                    adapter.notifyDataSetChanged();
                },
                error -> {
                    Log.e("Volley", "Home JSON error", error);
                    Toast.makeText(this, "Load failed: " + error.getClass().getSimpleName(),
                            Toast.LENGTH_SHORT).show();
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
                    o.optString("description")));
        }
    }
}