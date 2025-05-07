package com.kzeen.cityexplorer.util;

import android.content.Context;
import android.content.Intent;
import android.net.Uri;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
public final class ShareUtils {
    private ShareUtils() {
    }

    private static final String MAPS_QUERY_TEMPLATE = "https://www.google.com/maps/search/?api=1&query=%f,%f";

    @NonNull
    public static Intent createShareIntent(@NonNull Context ctx, @NonNull String name, @Nullable String addr, @Nullable Double lat, @Nullable Double lng) {
        String mapsUrl = (lat != null && lng != null)
                ? String.format(MAPS_QUERY_TEMPLATE, lat, lng)
                : "https://www.google.com/maps/search/?q=" + Uri.encode(name);

        StringBuilder sb = new StringBuilder();
        sb.append(name);
        if (addr != null && !addr.isEmpty()) {
            sb.append(" — ").append(addr);
        }
        sb.append("\n").append(mapsUrl);

        Intent i = new Intent(Intent.ACTION_SEND);
        i.setType("text/plain");
        i.putExtra(Intent.EXTRA_TEXT, sb.toString());
        return Intent.createChooser(i, null);
    }
    @NonNull
    public static Intent createMapIntent(@NonNull String name, @NonNull Double lat, @NonNull Double lng) {
        Uri geo = Uri.parse("geo:" + lat + "," + lng + "?q=" + Uri.encode(name));

        Intent i = new Intent(Intent.ACTION_VIEW, geo);
        i.setPackage("com.google.android.apps.maps");
        return i;
    }
}
