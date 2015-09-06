package io.kazak.api.json;

import com.google.gson.JsonArray;

import retrofit.http.GET;
import rx.Observable;

public interface JsonKazakApi {

    @GET("/events?expand=rooms&expand=presenters")
    Observable<JsonArray> fetchSchedule();

}
