package io.kazak.api.json.model;

import org.jetbrains.annotations.Nullable;

public class JsonTrack {

    @Nullable public final String id;
    @Nullable public final String name;
    @Nullable public final String color;

    public JsonTrack(
            @Nullable String id,
            @Nullable String name,
            @Nullable String color
    ) {
        this.id = id;
        this.name = name;
        this.color = color;
    }

}
