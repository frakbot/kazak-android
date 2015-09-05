package io.kazak.api.json.model;

import java.net.URI;
import java.util.List;

import org.jetbrains.annotations.Nullable;

public class JsonPresenter {

    @Nullable public final String id;
    @Nullable public final String name;
    @Nullable public final String company;
    @Nullable public final String bio;
    @Nullable public final URI pic;
    @Nullable public final List<String> social;

    public JsonPresenter(
            @Nullable String id,
            @Nullable String name,
            @Nullable String company,
            @Nullable String bio,
            @Nullable URI pic,
            @Nullable List<String> social
    ) {
        this.id = id;
        this.name = name;
        this.company = company;
        this.bio = bio;
        this.pic = pic;
        this.social = social;
    }

}
