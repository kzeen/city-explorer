package com.kzeen.cityexplorer;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.FrameLayout;

import androidx.annotation.LayoutRes;
import androidx.annotation.NonNull;
import androidx.annotation.StringRes;
import androidx.appcompat.app.AppCompatActivity;

import com.google.android.material.appbar.MaterialToolbar;
import com.google.android.material.bottomnavigation.BottomNavigationView;
import com.kzeen.cityexplorer.ui.search.SearchActivity;

import java.util.Objects;

public abstract class BaseActivity extends AppCompatActivity {
    protected abstract int getNavItemId();

    protected @StringRes int getToolbarTitleRes() {
        return R.string.app_name;
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_base);

        MaterialToolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        Objects.requireNonNull(getSupportActionBar()).setTitle(getToolbarTitleRes());

        BottomNavigationView nav = findViewById(R.id.bottom_nav);
        nav.setOnItemSelectedListener(item -> {
            if (item.getItemId() == getNavItemId()) return true;

            Intent intent;
            if (item.getItemId() == R.id.nav_home) {
                intent = new Intent(this, HomeActivity.class);
            } else if (item.getItemId() == R.id.nav_search) {
                intent = new Intent(this, SearchActivity.class);
            } else if (item.getItemId() == R.id.nav_recents) {
                intent = new Intent(this, RecentsActivity.class);
            } else if (item.getItemId() == R.id.nav_settings) {
                intent = new Intent(this, SettingsActivity.class);
            } else {
                throw new IllegalArgumentException("Unknown navigation item");
            }
            intent.addFlags(Intent.FLAG_ACTIVITY_REORDER_TO_FRONT);
            startActivity(intent);
            overridePendingTransition(0, 0);
            return true;
        });

        nav.getMenu().findItem(getNavItemId()).setChecked(true);
    }

    protected void inflateLayout(@LayoutRes int layoutResId) {
        getLayoutInflater()
                .inflate(layoutResId, findViewById(R.id.content_container), true);
    }

    protected void inflateLayout(@NonNull View view) {
        FrameLayout container = findViewById(R.id.content_container);
        container.addView(view);
    }
}