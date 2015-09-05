package io.kazak.api.json.model;

import java.util.List;

import org.jetbrains.annotations.Nullable;

public class JsonEvent {

    @Nullable public final String id;
    @Nullable public final String name;
    @Nullable public final String description;
    @Nullable public final String startDate;
    @Nullable public final String endDate;
    @Nullable public final String type;
    @Nullable public final List<JsonPresenter> presenters;
    @Nullable public final List<JsonRoom> rooms;

    public JsonEvent(
            @Nullable String id,
            @Nullable String name,
            @Nullable String description,
            @Nullable String startDate,
            @Nullable String endDate,
            @Nullable String type,
            @Nullable List<JsonPresenter> presenters,
            @Nullable List<JsonRoom> rooms
    ) {
        this.id = id;
        this.name = name;
        this.description = description;
        this.startDate = startDate;
        this.endDate = endDate;
        this.type = type;
        this.presenters = presenters;
        this.rooms = rooms;
    }

}
