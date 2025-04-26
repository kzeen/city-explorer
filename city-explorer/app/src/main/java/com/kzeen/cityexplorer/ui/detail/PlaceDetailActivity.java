package com.kzeen.cityexplorer.ui.detail;

import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.ImageView;

import androidx.activity.EdgeToEdge;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.lifecycle.ViewModelProvider;

import com.bumptech.glide.Glide;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.libraries.places.api.Places;
import com.google.android.libraries.places.api.model.PhotoMetadata;
import com.google.android.libraries.places.api.model.Place;
import com.google.android.libraries.places.api.net.FetchPhotoRequest;
import com.google.android.libraries.places.api.net.PlacesClient;
import com.kzeen.cityexplorer.BuildConfig;
import com.kzeen.cityexplorer.R;
import com.kzeen.cityexplorer.databinding.ActivityPlaceDetailBinding;

import java.util.ArrayList;
import java.util.List;
import com.kzeen.cityexplorer.util.ShareUtils;
import com.kzeen.cityexplorer.util.RecentsManager;

public class PlaceDetailActivity extends AppCompatActivity {

    public static final String EXTRA_PLACE_ID = "extra_place_id";

    private ActivityPlaceDetailBinding binding;
    private PlacesClient placesClient;
    private DetailImagePagerAdapter imageAdapter;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);

        binding = ActivityPlaceDetailBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        setSupportActionBar(binding.toolbar);
        if (getSupportActionBar() != null) getSupportActionBar().setDisplayHomeAsUpEnabled(true);

        if (!Places.isInitialized()) {
            Places.initialize(getApplicationContext(), BuildConfig.MAPS_API_KEY);
        }
        placesClient = Places.createClient(this);

        imageAdapter = new DetailImagePagerAdapter(placesClient);
        binding.viewPagerPhotos.setAdapter(imageAdapter);

        String placeId = getIntent().getStringExtra(EXTRA_PLACE_ID);
        if (placeId != null) {
            RecentsManager.addRecent(this, placeId);
        }

        PlaceDetailViewModel viewModel = new ViewModelProvider(this).get(PlaceDetailViewModel.class);

        viewModel.getPlace().observe(this, this::bindPlace);
        viewModel.getError().observe(this, Throwable::printStackTrace);

        if (placeId != null) {
            viewModel.loadPlace(placeId, placesClient);
        } else {
            finish();
        }
    }

    private void bindPlace(@NonNull Place place) {
        binding.collapsingToolbar.setTitle(place.getName());

        binding.textAddress.setText(place.getAddress());
        binding.textPhone.setText(place.getPhoneNumber() != null
                ? place.getPhoneNumber() : getString(R.string.phone_unavailable));
        binding.textRating.setText(place.getRating() != null
                ? String.valueOf(place.getRating()) : getString(R.string.rating_unavailable));

        List<PhotoMetadata> photos = place.getPhotoMetadatas();
        if (photos != null && !photos.isEmpty()) {
            loadPhoto(photos.get(0), binding.imageHero);
            if (photos.size() > 1) {
                imageAdapter.submitList(new ArrayList<>(photos.subList(1, photos.size())));
            }
        } else {
            binding.imageHero.setImageResource(R.drawable.ic_placeholder);
        }

        setupMapButton(place);
        setupWebsiteButton(place);
        setupShareButton(place);
    }

    private void setupMapButton(Place place) {
        LatLng ll = place.getLatLng();
        binding.buttonMaps.setOnClickListener(v -> {
            if (ll != null) {
                Uri uri = Uri.parse("geo:" + ll.latitude + "," + ll.longitude +
                        "?q=" + Uri.encode(place.getName()));
                Intent i = new Intent(Intent.ACTION_VIEW, uri);
                i.setPackage("com.google.android.apps.maps");
                startActivity(i);
            }
        });
    }

    private void setupWebsiteButton(Place place) {
        if (place.getWebsiteUri() != null) {
            binding.buttonWebsite.setOnClickListener(
                    v -> startActivity(new Intent(Intent.ACTION_VIEW, place.getWebsiteUri())));
        } else {
            binding.buttonWebsite.setEnabled(false);
        }
    }

    private void setupShareButton(Place place) {
        binding.buttonShare.setOnClickListener(v -> {
            Intent shareIntent = ShareUtils.createShareIntent(
                    this,
                    place.getName(),
                    place.getAddress(),
                    place.getLatLng() != null ? place.getLatLng().latitude : null,
                    place.getLatLng() != null ? place.getLatLng().longitude : null);
            startActivity(shareIntent);
        });
    }

    private void loadPhoto(PhotoMetadata meta, ImageView target) {
        FetchPhotoRequest req = FetchPhotoRequest.builder(meta)
                .setMaxWidth(1600).setMaxHeight(900).build();

        placesClient.fetchPhoto(req)
                .addOnSuccessListener(r -> Glide.with(this)
                        .load(r.getBitmap())
                        .placeholder(R.drawable.ic_placeholder)
                        .into(target))
                .addOnFailureListener(Throwable::printStackTrace);
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.menu_place_detail, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(@NonNull MenuItem item) {
        int id = item.getItemId();
        if (id == android.R.id.home) {
            getOnBackPressedDispatcher().onBackPressed();
            return true;
        } else if (id == R.id.action_share) {
            binding.buttonShare.performClick();
            return true;
        } else if (id == R.id.action_open_map) {
            binding.buttonMaps.performClick();
            return true;
        }
        return super.onOptionsItemSelected(item);
    }
}
