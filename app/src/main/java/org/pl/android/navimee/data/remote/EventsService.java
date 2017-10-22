package org.pl.android.navimee.data.remote;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;

import org.pl.android.navimee.data.model.Event;
import org.pl.android.navimee.data.model.Ribot;
import org.pl.android.navimee.util.MyGsonTypeAdapterFactory;

import java.util.List;

import io.reactivex.Observable;
import retrofit2.Retrofit;
import retrofit2.adapter.rxjava2.RxJava2CallAdapterFactory;
import retrofit2.converter.gson.GsonConverterFactory;
import retrofit2.http.GET;

public interface EventsService {

    String ENDPOINT = "http://navimeeapi.azurewebsites.net/";

    @GET("/api/events")
    Observable<List<Event>> getEvents();

    /******** Helper class that sets up a new services *******/
    class Creator {

        public static EventsService newEventsService() {
            Gson gson = new GsonBuilder()
                    .registerTypeAdapterFactory(MyGsonTypeAdapterFactory.create())
                    .setDateFormat("yyyy-MM-dd'T'HH:mm:ss.SSS'Z'")
                    .create();
            Retrofit retrofit = new Retrofit.Builder()
                    .baseUrl(EventsService.ENDPOINT)
                    .addConverterFactory(GsonConverterFactory.create(gson))
                    .addCallAdapterFactory(RxJava2CallAdapterFactory.create())
                    .build();
            return retrofit.create(EventsService.class);
        }
    }
}
