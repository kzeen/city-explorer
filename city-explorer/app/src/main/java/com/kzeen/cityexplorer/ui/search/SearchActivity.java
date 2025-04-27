package com.kzeen.cityexplorer.ui.search;

import android.content.Intent;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.KeyEvent;
import android.view.View;
import android.view.inputmethod.EditorInfo;
import android.widget.ArrayAdapter;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.LinearLayoutManager;

import com.google.android.libraries.places.api.model.AutocompletePrediction;
import com.google.android.libraries.places.api.model.AutocompleteSessionToken;
import com.google.android.libraries.places.api.model.Place;
import com.google.android.libraries.places.api.net.FindAutocompletePredictionsRequest;
import com.kzeen.cityexplorer.ui.BaseActivity;
import com.kzeen.cityexplorer.R;
import com.kzeen.cityexplorer.databinding.ActivitySearchBinding;
import com.kzeen.cityexplorer.ui.PlaceDetailActivity;
import com.kzeen.cityexplorer.ui.adapters.PlaceAdapter;

import java.util.ArrayList;
import java.util.List;

public class SearchActivity extends BaseActivity {

    private ActivitySearchBinding binding;
    private ArrayAdapter<String> suggestionAdapter;
    private final List<Place> results = new ArrayList<>();
    private PlaceAdapter adapter;
    private AutocompleteSessionToken sessionToken;

    @Override protected int getNavItemId()     { return R.id.nav_search; }
    @Override protected int getToolbarTitleRes(){ return R.string.search; }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = ActivitySearchBinding.inflate(getLayoutInflater());
        inflateLayout(binding.getRoot());

        sessionToken = AutocompleteSessionToken.newInstance();

        setupAutocomplete();
        setupResultsList();
        setupSearchTriggers();
    }

    private void setupAutocomplete() {
        suggestionAdapter = new ArrayAdapter<>(
                this,
                android.R.layout.simple_dropdown_item_1line,
                new ArrayList<>());
        binding.searchEditText.setAdapter(suggestionAdapter);

        binding.searchEditText.addTextChangedListener(new TextWatcher() {
            @Override public void beforeTextChanged(CharSequence s,int start,int count,int after){}
            @Override public void onTextChanged(CharSequence s,int start,int before,int count){
                if (s.length() < 2) { suggestionAdapter.clear(); return; }
                fetchPredictions(s.toString());
            }
            @Override public void afterTextChanged(Editable s){}
        });

        binding.searchEditText.setOnItemClickListener((parent, view, pos, id) -> {
            String sel = suggestionAdapter.getItem(pos);
            binding.searchEditText.setText(sel);
            binding.searchEditText.setSelection(sel.length());
            performSearch(sel);
        });
    }

    private void fetchPredictions(String query) {
        if (query.isEmpty()) {
            suggestionAdapter.clear();
            return;
        }
        FindAutocompletePredictionsRequest request =
                FindAutocompletePredictionsRequest.builder()
                        .setSessionToken(sessionToken)
                        .setQuery(query)
                        .build();

        placesClient.findAutocompletePredictions(request)
                .addOnSuccessListener(response -> {
                    suggestionAdapter.clear();
                    for (AutocompletePrediction p : response.getAutocompletePredictions()) {
                        suggestionAdapter.add(p.getFullText(null).toString());
                    }
                    suggestionAdapter.notifyDataSetChanged();
                })
                .addOnFailureListener(e -> Toast.makeText(this, e.getMessage(), Toast.LENGTH_SHORT).show());
    }

    private void setupResultsList() {
        adapter = new PlaceAdapter(results, placesClient);
        binding.searchRecycler.setLayoutManager(new LinearLayoutManager(this));
        binding.searchRecycler.setAdapter(adapter);
    }

    private void setupSearchTriggers() {
        binding.searchEditText.setOnEditorActionListener((v, actionId, event) -> {
            if (actionId == EditorInfo.IME_ACTION_SEARCH ||
                (event != null && event.getKeyCode() == KeyEvent.KEYCODE_ENTER)) {
                performSearch(binding.searchEditText.getText().toString().trim());
                return true;
            }
            return false;
        });

        binding.searchInputLayout.setEndIconOnClickListener(v ->
                performSearch(binding.searchEditText.getText().toString().trim()));
    }

    private void performSearch(@NonNull String query) {
        if (query.isEmpty()) return;

        binding.searchProgress.setVisibility(View.VISIBLE);
        PlacesRepository.searchPlaces(this, query, new PlacesRepository.SearchCallback() {
            @Override public void onSuccess(List<Place> places) {
                binding.searchProgress.setVisibility(View.GONE);
                results.clear();
                results.addAll(places);
                adapter.notifyDataSetChanged();
                binding.searchRecycler.scrollToPosition(0);
            }
            @Override public void onError(Exception e) {
                binding.searchProgress.setVisibility(View.GONE);
                Toast.makeText(SearchActivity.this, e.getMessage(), Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void openDetails(@NonNull Place p) {
        Intent i = new Intent(this, PlaceDetailActivity.class);
        i.putExtra(PlaceDetailActivity.EXTRA_PLACE_ID, p.getId());
        startActivity(i);
    }
}