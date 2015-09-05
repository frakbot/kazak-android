package io.kazak.api.json;

import java.util.List;

import io.kazak.api.json.model.JsonEvent;
import retrofit.http.GET;
import rx.Observable;

public interface JsonKazakApi {

    @GET("/events?expand=room&expand=presenters")
    Observable<List<JsonEvent>> fetchSchedule();

}
