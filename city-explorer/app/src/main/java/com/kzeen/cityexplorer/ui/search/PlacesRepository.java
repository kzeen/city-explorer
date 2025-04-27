package com.kzeen.cityexplorer.ui.search;

import android.content.Context;

import androidx.annotation.NonNull;

import com.google.android.libraries.places.api.model.AutocompletePrediction;
import com.google.android.libraries.places.api.model.AutocompleteSessionToken;
import com.google.android.libraries.places.api.net.FetchPlaceRequest;
import com.google.android.libraries.places.api.net.FindAutocompletePredictionsRequest;
import com.google.android.libraries.places.api.model.Place;
import com.google.android.libraries.places.api.net.PlacesClient;

import java.util.ArrayList;
import java.util.List;
import java.util.Arrays;

public class PlacesRepository {

    public interface SearchCallback {
        void onSuccess(@NonNull List<Place> places);
        void onError(@NonNull Exception e);
    }

    public static void searchPlaces(@NonNull Context ctx,
                                    @NonNull String query,
                                    @NonNull SearchCallback cb) {

        PlacesClient client = com.google.android.libraries.places.api.Places.createClient(ctx);
        AutocompleteSessionToken token = AutocompleteSessionToken.newInstance();

        FindAutocompletePredictionsRequest req =
                FindAutocompletePredictionsRequest.builder()
                        .setSessionToken(token)
                        .setQuery(query)
                        .build();

        client.findAutocompletePredictions(req)
                .addOnSuccessListener(predResp -> {
                    List<AutocompletePrediction> preds = predResp.getAutocompletePredictions();
                    if (preds.isEmpty()) {
                        cb.onSuccess(new ArrayList<>());
                        return;
                    }
                    fetchDetailsForPredictions(client, preds, cb);
                })
                .addOnFailureListener(cb::onError);
    }

    private static void fetchDetailsForPredictions(PlacesClient client,
                                                   List<AutocompletePrediction> preds,
                                                   SearchCallback cb) {

        List<Place> results = new ArrayList<>();
        final int[] remaining = {preds.size()};

        for (AutocompletePrediction p : preds) {
            FetchPlaceRequest placeReq = FetchPlaceRequest.builder(
                    p.getPlaceId(),
                    Arrays.asList(
                            Place.Field.ID,
                            Place.Field.NAME,
                            Place.Field.ADDRESS,
                            Place.Field.PHOTO_METADATAS,
                            Place.Field.RATING))
                .build();

            client.fetchPlace(placeReq)
                  .addOnSuccessListener(placeResp -> {
                      Place place = placeResp.getPlace();

                      results.add(place);

                      if (--remaining[0] == 0) cb.onSuccess(results);
                  })
                  .addOnFailureListener(e -> {
                      if (--remaining[0] == 0) cb.onSuccess(results);
                  });
        }
    }
}