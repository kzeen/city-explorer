package com.kzeen.cityexplorer;

import android.os.Bundle;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import java.util.Arrays;
import java.util.List;

public class PlacesActivity extends BaseActivity {

    @Override protected int getNavItemId() { return R.id.nav_places; }
    @Override protected int getToolbarTitleRes() { return R.string.places; }


    @Override protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        inflateLayout(R.layout.activity_places);   // see XML below

        RecyclerView rv = findViewById(R.id.places_recycler);
        rv.setLayoutManager(new LinearLayoutManager(this));
        rv.setAdapter(new SimpleStringAdapter(mockData()));
    }

    private List<String> mockData() {
        return Arrays.asList("Places 1", "Item 2", "Item 3");
    }
}