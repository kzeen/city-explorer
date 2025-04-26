package com.kzeen.cityexplorer.util;

import android.content.Context;
import android.content.SharedPreferences;

import androidx.annotation.NonNull;

import org.json.JSONArray;
import org.json.JSONException;

import java.util.ArrayList;
import java.util.List;

public final class RecentsManager {

    private static final String PREF_NAME  = "recents_prefs";
    private static final String KEY_IDS    = "recent_place_ids";
    private static final int    MAX_ITEMS  = 6;

    private RecentsManager() { }

    @NonNull
    public static List<String> getRecents(@NonNull Context ctx) {
        SharedPreferences sp = ctx.getSharedPreferences(PREF_NAME, Context.MODE_PRIVATE);
        String raw = sp.getString(KEY_IDS, "[]");
        List<String> list = new ArrayList<>();
        try {
            JSONArray arr = new JSONArray(raw);
            for (int i = 0; i < arr.length(); i++) {
                list.add(arr.getString(i));
            }
        } catch (JSONException ignored) {

        }
        return list;
    }

    public static void addRecent(@NonNull Context ctx, @NonNull String placeId) {
        List<String> list = getRecents(ctx);
        list.remove(placeId);
        list.add(0, placeId);
        if (list.size() > MAX_ITEMS) {
            list = list.subList(0, MAX_ITEMS);
        }
        saveList(ctx, list);
    }

    private static void saveList(Context ctx, List<String> list) {
        JSONArray arr = new JSONArray();
        for (String id : list) arr.put(id);
        ctx.getSharedPreferences(PREF_NAME, Context.MODE_PRIVATE)
           .edit()
           .putString(KEY_IDS, arr.toString())
           .apply();
    }
}
