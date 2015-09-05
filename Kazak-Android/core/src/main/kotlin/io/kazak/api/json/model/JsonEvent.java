package io.kazak.api.json.model;

import java.util.List;

import org.jetbrains.annotations.Nullable;

public class JsonEvent {

    public final String id;
    public final String name;
    public final String description;
    public final String startDate;
    public final String endDate;
    @Nullable public final List<JsonPresenter> presenters;
    @Nullable public final JsonRoom room;

    public JsonEvent(
            String id,
            String name,
            String description,
            String startDate,
            String endDate,
            @Nullable List<JsonPresenter> presenters,
            @Nullable JsonRoom room
    ) {
        this.id = id;
        this.name = name;
        this.description = description;
        this.startDate = startDate;
        this.endDate = endDate;
        this.presenters = presenters;
        this.room = room;
    }

}
