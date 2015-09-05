package io.kazak.api.json.model;

import org.jetbrains.annotations.Nullable;

public class JsonRoom {

    @Nullable public final String id;
    @Nullable public final String name;

    public JsonRoom(
            @Nullable String id,
            @Nullable String name
    ) {
        this.id = id;
        this.name = name;
    }

}
