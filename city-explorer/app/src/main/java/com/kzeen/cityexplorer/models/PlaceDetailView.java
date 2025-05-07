package com.kzeen.cityexplorer.models;

import androidx.annotation.NonNull;
import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;
import androidx.lifecycle.ViewModel;

import com.google.android.libraries.places.api.model.PhotoMetadata;
import com.google.android.libraries.places.api.model.Place;
import com.google.android.libraries.places.api.net.FetchPlaceRequest;
import com.google.android.libraries.places.api.net.PlacesClient;

import java.util.Arrays;
import java.util.List;

public class PlaceDetailView extends ViewModel {
    private final MutableLiveData<Place> placeLiveData = new MutableLiveData<>();
    private final MutableLiveData<List<PhotoMetadata>> photosLiveData = new MutableLiveData<>();
    private final MutableLiveData<Throwable> errorLiveData = new MutableLiveData<>();

    public LiveData<Place> getPlace() { return placeLiveData;   }
    public LiveData<List<PhotoMetadata>> getPhotos() { return photosLiveData; }
    public LiveData<Throwable> getError() { return errorLiveData;   }

    public void loadPlace(@NonNull String placeId, @NonNull PlacesClient placesClient) {
        if (placeLiveData.getValue() != null) return;

        List<Place.Field> fields = Arrays.asList(
                Place.Field.ID, Place.Field.NAME,
                Place.Field.ADDRESS, Place.Field.LAT_LNG,
                Place.Field.PHONE_NUMBER,
                Place.Field.WEBSITE_URI,
                Place.Field.RATING,
                Place.Field.PHOTO_METADATAS);

        FetchPlaceRequest request = FetchPlaceRequest.builder(placeId, fields).build();

        placesClient.fetchPlace(request)
                .addOnSuccessListener(response -> {
                    Place p = response.getPlace();
                    placeLiveData.postValue(p);
                    photosLiveData.postValue(p.getPhotoMetadatas());
                })
                .addOnFailureListener(errorLiveData::postValue);
    }
}
