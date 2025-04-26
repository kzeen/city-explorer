package com.kzeen.cityexplorer.model;

import android.os.Parcel;
import android.os.Parcelable;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

public class NearbyPlace implements Parcelable {
    private final @NonNull String id;
    private final @NonNull String name;
    private final @NonNull String address;
    private final String photoRef;

    public NearbyPlace(@NonNull String id,
                       @NonNull String name,
                       @NonNull String address,
                       @Nullable String photoRef) {
        this.id      = id;
        this.name    = name;
        this.address = address;
        this.photoRef = photoRef;
    }

    protected NearbyPlace(Parcel in) {
        id      = in.readString();
        name    = in.readString();
        address = in.readString();
        photoRef = in.readString();
    }

    public static final Creator<NearbyPlace> CREATOR = new Creator<>() {
        @Override public NearbyPlace createFromParcel(Parcel in) { return new NearbyPlace(in); }
        @Override public NearbyPlace[] newArray(int size)        { return new NearbyPlace[size]; }
    };

    @NonNull public String getId()      { return id; }
    @NonNull public String getName()    { return name; }
    @NonNull public String getAddress() { return address; }
    @Nullable public String getPhotoRef() { return photoRef; }

    @Override public int describeContents() { return 0; }

    @Override
    public void writeToParcel(Parcel dest, int flags) {
        dest.writeString(id);
        dest.writeString(name);
        dest.writeString(address);
        dest.writeString(photoRef);
    }
}
