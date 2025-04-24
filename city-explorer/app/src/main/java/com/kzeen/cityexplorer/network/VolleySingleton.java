package com.kzeen.cityexplorer.network;

import android.content.Context;

import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.toolbox.Volley;

public class VolleySingleton {
    private static VolleySingleton instance;
    private final RequestQueue queue;

    private VolleySingleton(Context context) {
        queue = Volley.newRequestQueue(context.getApplicationContext());
    }

    public static synchronized VolleySingleton get(Context context) {
        if (instance == null) instance = new VolleySingleton(context);
        return instance;
    }

    public <T> void add(Request<T> req) { queue.add(req); }
}
