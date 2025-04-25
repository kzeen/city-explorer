package com.kzeen.cityexplorer.model;

public class Place {
    public final String name;
    public final String description;
    public final String imageURL;

    public Place(String name, String description, String imageURL) {
        this.name = name;
        this.description = description;
        this.imageURL = imageURL;
    }
}
