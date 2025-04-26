package com.kzeen.cityexplorer;

import android.os.Bundle;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import java.util.Arrays;
import java.util.List;

public class SettingsActivity extends BaseActivity {

    @Override protected int getNavItemId() { return R.id.nav_settings; }
    @Override protected int getToolbarTitleRes() { return R.string.settings; }


    @Override protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        inflateLayout(R.layout.activity_settings);

        RecyclerView rv = findViewById(R.id.settings_recycler);
        rv.setLayoutManager(new LinearLayoutManager(this));
        rv.setAdapter(new SimpleStringAdapter(mockData()));
    }

    private List<String> mockData() {
        return Arrays.asList("Settings 1", "Item 2", "Item 3");
    }
}