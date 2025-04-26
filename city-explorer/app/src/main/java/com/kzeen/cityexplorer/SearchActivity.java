package com.kzeen.cityexplorer;

import android.os.Bundle;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import java.util.Arrays;
import java.util.List;

public class SearchActivity extends BaseActivity {

    @Override protected int getNavItemId() { return R.id.nav_search; }
    @Override protected int getToolbarTitleRes() { return R.string.search; }


    @Override protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        inflateLayout(R.layout.activity_search);

        RecyclerView rv = findViewById(R.id.search_recycler);
        rv.setLayoutManager(new LinearLayoutManager(this));
        rv.setAdapter(new SimpleStringAdapter(mockData()));
    }

    private List<String> mockData() {
        return Arrays.asList("Search 1", "Item 2", "Item 3");
    }
}